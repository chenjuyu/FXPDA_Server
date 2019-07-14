package com.fuxi.web.controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import com.fuxi.core.common.dao.impl.CommonDao;
import com.fuxi.core.common.exception.BusinessException;
import com.fuxi.core.common.model.json.AjaxJson;
import com.fuxi.system.util.Client;
import com.fuxi.system.util.DataUtils;
import com.fuxi.system.util.ResourceUtil;
import com.fuxi.system.util.SysLogger;
import com.fuxi.system.util.oConvertUtils;

/**
 * Title: InventoryQueryController Description: 库存查询逻辑控制器
 * 
 * @author LYJ
 * 
 */
@Controller
@RequestMapping("/inventoryQuery")
public class InventoryQueryController extends BaseController {

    private Logger log = Logger.getLogger(InventoryQueryController.class);

    @Autowired
    private CommonDao commonDao;

    /**
     * 根据条件生成货品库存报表方法
     * 
     * @param req
     * @return
     */
    @RequestMapping(params = "queryStock")
    @ResponseBody
    public AjaxJson queryStock(HttpServletRequest req) {
        AjaxJson j = new AjaxJson();
        j.setAttributes(new HashMap<String, Object>());
        try {
            boolean hasStock = true; // 默认有库存
            Client client = ResourceUtil.getClientFromSession(req);
            String userId = oConvertUtils.getString(client.getUserID());
            String deptId = oConvertUtils.getString(req.getParameter("deptId"));
            String productId = oConvertUtils.getString(req.getParameter("productId"));
            String goodsId = oConvertUtils.getString(req.getParameter("goodsId"));
            String colorId = null;
            String sizeId = null;
            // 当使用条码查询时,是否忽略颜色尺码
            boolean preciseQueryStock = Boolean.valueOf(oConvertUtils.getString(req.getParameter("preciseQueryStock")));
            // 获取货品编号
            if (null == goodsId || "".equals(goodsId) || "null".equalsIgnoreCase(goodsId)) {
                if (preciseQueryStock) {
                    // 精确查询
                    List barcodeList = commonDao.findForJdbc(" select GoodsID,ColorID,SizeID from barcode where barcode = ? ", productId);
                    if (barcodeList.size() > 0) {
                        Map<String, Object> map = (Map<String, Object>) barcodeList.get(0);
                        goodsId = (String) map.get("GoodsID");
                        colorId = (String) map.get("ColorID");
                        sizeId = (String) map.get("SizeID");
                    }
                } else {
                    // 忽略颜色尺码查询
                    goodsId = new SelectController().getGoodsId(productId, commonDao);
                }
            }
            if (null == goodsId || "".equals(goodsId) || "null".equalsIgnoreCase(goodsId)) {
                throw new BusinessException("条码或货号错误");
            }
            // 根据输入参数查询库存 库存信息为伏羲后台设定的库存类别
            StringBuffer sb = new StringBuffer();
            sb.append("  select isnull(g.Code,'') GoodsCode, sd.GoodsID, isnull(c.Color,'') Color, sd.ColorID,isnull(s.Size,'') Size,g.name GoodsName,").append(" isnull((select Brand from brand where brandId = g.brandId),'') Brand,isnull(g.SupplierCode,'') SupplierCode,")
                    .append("g.PurchasedDate, g.LastPurchasedDate, ").append(" sd.SizeID ,isnull(g.RetailSales,'0') as RetailSales,isnull(g.TradePrice,'0') TradePrice, 0 as Qty from  ").append(" stockdetail sd join goods g on g.goodsId = sd.goodsId join color c on c.colorId = sd.colorId ")
                    .append(" join size s on s.sizeId = sd.sizeId where sd.goodsId = ? ");
            if (colorId != null && sizeId != null) {
                sb.append(" and sd.colorId = '" + colorId + "' and sd.sizeId = '" + sizeId + "' ");
            }
            sb.append(" group by g.Code,c.Color,s.Size,g.RetailSales,g.TradePrice,sd.GoodsID,sd.ColorID,sd.SizeID,g.name,g.brandId,g.SupplierCode,g.PurchasedDate,g.LastPurchasedDate ");
            List datas = commonDao.findForJdbc(sb.toString(), goodsId);
            // 没有库存的情况下显示货品信息
            if (datas.size() == 0) {
                sb = new StringBuffer();
                sb.append(" select isnull(g.Code,'') GoodsCode, GoodsID,g.name GoodsName,'' Color, '' Size,isnull((select Brand from brand where brandId = g.brandId),'') Brand, ")
                        .append("isnull(g.SupplierCode,'') SupplierCode,g.PurchasedDate, g.LastPurchasedDate,isnull(g.RetailSales,'0') as RetailSales, ").append("isnull(g.TradePrice,'0') TradePrice, 0 as Qty from goods g where g.goodsId = ? group by g.Code,g.RetailSales, ")
                        .append("g.TradePrice,g.GoodsID,g.name,g.brandId,g.SupplierCode,g.PurchasedDate,g.LastPurchasedDate  ");
                datas = commonDao.findForJdbc(sb.toString(), goodsId);
            }
            if (preciseQueryStock) {// 精确查询
                commonDao.getStockState(client.getOnLineId(), deptId, goodsId, colorId, sizeId, userId, 0, "", 0, -1, 0, 0, 0, "");
            } else {
                commonDao.getStockState(client.getOnLineId(), deptId, goodsId, "", "", userId, 0, "", 0, -1, 0, 0, 0, "");
            }
            List list = commonDao.findForJdbc(" select * from tempdb.dbo.[sys_GetStockState" + client.getOnLineId() + "] ");
            if (list.size() <= 0) {
                hasStock = false;
            } else {
                for (int i = 0; i < datas.size(); i++) {
                    Map temp = (Map) datas.get(i);
                    String tcolorId = String.valueOf(temp.get("ColorID"));
                    String tsizeId = String.valueOf(temp.get("SizeID"));
                    for (int k = 0; k < list.size(); k++) {
                        Map tmp = (Map) list.get(k);
                        String colorID = String.valueOf(tmp.get("ColorID"));
                        String sizeID = String.valueOf(tmp.get("SizeID"));
                        if (colorID.equals(tcolorId) && sizeID.equals(tsizeId)) {
                            temp.put("Qty", String.valueOf(tmp.get("Quantity")));
                        }
                    }
                }
            }
            if (list.size() > 0 && datas.size() <= 0) {
                datas = list;
            }
            j.getAttributes().put("list", datas);
            j.getAttributes().put("hasStock", hasStock);
        } catch (Exception e) {
            j.setSuccess(false);
            j.setMsg(e.getMessage());
            SysLogger.error(e.getMessage(), e);
        }
        return j;
    }

    /**
     * 查询货品库存总数方法
     * 
     * @param user
     * @param req
     * @return
     */
    @RequestMapping(params = "getStockTotal")
    @ResponseBody
    public AjaxJson getStockTotal(HttpServletRequest req) {
        AjaxJson j = new AjaxJson();
        Client client = ResourceUtil.getClientFromSession(req);
        j.setAttributes(new HashMap<String, Object>());
        try {
            String goodsId = "'" + oConvertUtils.getString(req.getParameter("goodsId")) + "'";
            String colorId = "'" + oConvertUtils.getString(req.getParameter("colorId")) + "'";
            String sizeId = "'" + oConvertUtils.getString(req.getParameter("sizeId")) + "'";
            StringBuffer departmentId = new StringBuffer();
            // 总仓
            List data = commonDao.findForJdbc(" select DepartmentID from department where DefaultFlag = '1' ");
            for (int i = 0; i < data.size(); i++) {
                Map map = (Map) data.get(i);
                if (map != null) {
                    String deptId = String.valueOf(map.get("DepartmentID"));
                    if (i == data.size() - 1) {
                        deptId = "'" + deptId + "'";
                    } else {
                        deptId = "'" + deptId + "',";
                    }
                    departmentId.append(deptId);
                }
            }
            commonDao.queryStock(client.getUserID(), DataUtils.formatDate(new Date()), departmentId.toString(), goodsId, colorId, sizeId, 0, -1, client.getUserID());
            String qtyzStr = String.valueOf(commonDao.getData(" select sum(Quantity) Quantity from tempdb.dbo.[sys_GetStockState_Rpt" + client.getUserID() + "] "));
            if (null == qtyzStr || qtyzStr.isEmpty() || "null".equalsIgnoreCase(qtyzStr)) {
                qtyzStr = "0";
            }
            int qtyz = Integer.parseInt(qtyzStr);
            // 其它仓库
            departmentId = new StringBuffer();
            departmentId.append(" select DepartmentID from departmentRight dr  where  userid='" + client.getUserID() + "' and rightFlag=1 and DepartmentID in (select DepartmentID from Department where DefaultFlag != '1') ");
            commonDao.queryStock(client.getUserID(), DataUtils.formatDate(new Date()), departmentId.toString(), goodsId, colorId, sizeId, 0, -1, client.getUserID());
            String qtyoStr = String.valueOf(commonDao.getData(" select sum(Quantity) Quantity from tempdb.dbo.[sys_GetStockState_Rpt" + client.getUserID() + "] "));
            if (null == qtyoStr || qtyoStr.isEmpty() || "null".equalsIgnoreCase(qtyoStr)) {
                qtyoStr = "0";
            }
            int qtyo = Integer.parseInt(qtyoStr);
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("qtyz", qtyz);
            map.put("qtyo", qtyo);
            j.setObj(map);
        } catch (Exception e) {
            j.setSuccess(false);
            j.setMsg(e.getMessage());
            SysLogger.error(e.getMessage(), e);
        }
        return j;
    }

    /**
     * 查询货品库存方法
     * 
     * @param user
     * @param req
     * @return
     */
    @RequestMapping(params = "getAvailableStock")
    @ResponseBody
    public AjaxJson getAvailableStock(HttpServletRequest req) {
        AjaxJson j = new AjaxJson();
        Client client = ResourceUtil.getClientFromSession(req);
        j.setAttributes(new HashMap<String, Object>());
        try {
            String departmentId = oConvertUtils.getString(req.getParameter("departmentId"));
            String tableTagStr = oConvertUtils.getString(req.getParameter("tableTag"));
            String invoiceId = oConvertUtils.getString(req.getParameter("invoiceId"));
            String goodsId = oConvertUtils.getString(req.getParameter("goodsId"));
            String colorId = oConvertUtils.getString(req.getParameter("colorId"));
            String sizeId = oConvertUtils.getString(req.getParameter("sizeId"));
            String userId = client.getUserID();
            int tableTag = Integer.parseInt(tableTagStr);
            // 查询库存
            if (tableTag == 30) {// 发货单
                if (invoiceId == null || "".equals(invoiceId) || "null".equalsIgnoreCase(invoiceId)) {
                    commonDao.getStockState(client.getOnLineId(), departmentId, goodsId, colorId, sizeId, userId, tableTag, invoiceId, 0, 2, 0, 0, 0, "");
                } else {
                    commonDao.getStockState(client.getOnLineId(), departmentId, goodsId, colorId, sizeId, userId, tableTag, invoiceId, 0, 2, 1, 0, 1, "");
                }
            } else {
                commonDao.getStockState(client.getOnLineId(), departmentId, goodsId, colorId, sizeId, userId, tableTag, invoiceId, 0, 2, 0, 0, 0, "");
            }
            String qtyzStr = String.valueOf(commonDao.getData(" select Quantity from tempdb.dbo.[sys_GetStockState" + client.getOnLineId() + "] "));
            if (null == qtyzStr || qtyzStr.isEmpty() || "null".equalsIgnoreCase(qtyzStr)) {
                qtyzStr = "0";
            }
            int stock = Integer.parseInt(qtyzStr);
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("stock", stock);
            j.setObj(map);
        } catch (Exception e) {
            j.setSuccess(false);
            j.setMsg(e.getMessage());
            SysLogger.error(e.getMessage(), e);
        }
        return j;
    }

    /**
     * 查询货品库存分布方法
     * 
     * @param user
     * @param req
     * @return
     */
    @RequestMapping(params = "selectDistribution")
    @ResponseBody
    public AjaxJson selectDistribution(HttpServletRequest req) {
        AjaxJson j = new AjaxJson();
        Client client = ResourceUtil.getClientFromSession(req);
        Map<String, String> map = client.getMap();
        String userRight = map.get(client.getUserID());
        j.setAttributes(new HashMap<String, Object>());
        try {
            StringBuffer sql = new StringBuffer();
            String goodsId = oConvertUtils.getString(req.getParameter("goodsId"));
            String colorId = oConvertUtils.getString(req.getParameter("colorId"));
            String sizeId = oConvertUtils.getString(req.getParameter("sizeId"));
            sql.append(" select d.Code DeptCode ,d.Department  DeptName , sum(a.Direction*b.Quantity) Qty ").append(" from stock a join stockdetail b on a.stockid=b.stockid join Department d on d.DepartmentID = a.Departmentid ").append(" where a.Departmentid in (").append(userRight)
                    .append(") and a.AuditFlag=1 and b.GoodsID = '" + goodsId + "'  ");
            if (colorId != null && !colorId.isEmpty() && !"".equals(colorId)) {
                sql.append(" and b.ColorID =  '" + colorId + "' ");
            }
            if (sizeId != null && !sizeId.isEmpty() && !"".equals(sizeId)) {
                sql.append("  and b.SizeID =  '" + sizeId + "' ");
            }
            sql.append(" group by a.DepartmentID, b.GoodsID ,d.Code,d.Department  ").append("  having sum(a.Direction*b.Quantity)<>0  ");
            List list = commonDao.findForJdbc(sql.toString());
            j.setObj(list);
        } catch (Exception e) {
            j.setSuccess(false);
            j.setMsg(e.getMessage());
            SysLogger.error(e.getMessage(), e);
        }
        return j;
    }

    /**
     * 查询小票店铺库存汇总
     * 
     * @param user
     * @param req
     * @return
     */
    @RequestMapping(params = "posSalesQueryStock")
    @ResponseBody
    public AjaxJson posSalesQueryStock(HttpServletRequest req) {
        AjaxJson j = new AjaxJson();
        Client client = ResourceUtil.getClientFromSession(req);
        j.setAttributes(new HashMap<String, Object>());
        try {
            String productId = oConvertUtils.getString(req.getParameter("productId"));
            String goodsId = oConvertUtils.getString(req.getParameter("goodsId"));
            String colorId = oConvertUtils.getString(req.getParameter("colorId"));
            if (goodsId == null || "".equals(goodsId) || "null".equalsIgnoreCase(goodsId)) {
                goodsId = new SelectController().getGoodsId(productId, commonDao);
            }
            List sizeGroupList = new ArrayList();
            if (goodsId == null || "".equals(goodsId) || "null".equalsIgnoreCase(goodsId)) {
                // 执行存储过程
                commonDao.shopStockRpt(client.getUserID(), DataUtils.getDataString(DataUtils.date_sdf), "0" + client.getDeptID(), "", 0, client.getUserID(), 0, "", "", "", "");
                // 查询尺码组信息
                sizeGroupList = commonDao.findForJdbc(" Select a.* from SizeGroupSize a,Size b Where a.SizeID=b.SizeID and a.SizeGroupID in ('',(Select distinct b.Groupid as SizeGroupID From ShopStockReport a with (nolock),Goods b  with (nolock) Where a.GoodsID=b.GoodsID)) Order by a.SizeGroupID,a.[No] ");
            } else {
                // 执行存储过程
                commonDao.shopStockRpt(client.getUserID(), DataUtils.getDataString(DataUtils.date_sdf), "0" + client.getDeptID(), "0" + goodsId, 0, client.getUserID(), 0, "", "", "", "");
                sizeGroupList = commonDao.findForJdbc(" Select a.* from SizeGroupSize a,Size b Where a.SizeID=b.SizeID and a.SizeGroupID in ('',(Select SizeGroupID From Goods a,GoodsType b Where a.GoodsTypeID=b.GoodsTypeID and a.GoodsID='" + goodsId + "')) Order by a.SizeGroupID,a.[No] ");
            }
            // 拼接查询SQL
            StringBuffer sql = new StringBuffer();
            for (int i = 1; i < sizeGroupList.size() + 1; i++) {
                Map<String, Object> map = (Map<String, Object>) sizeGroupList.get(i - 1);
                String size = (String) map.get("Size");
                if (sizeGroupList.size() == 1) {
                    sql.append(" select (select Code from Goods where goodsID = t" + i + ".GoodsID) GoodsCode,GoodsID,(select Color from Color where colorID = t" + i + ".colorID) Color,ColorID,A_x_" + i + " Quantity,'" + size + "' Size from [tempdb].dbo.[_posRpt05" + client.getUserID() + "] t" + i
                            + " with(nolock) where A_x_" + i + " <> 0 ");
                    if (colorId != null && !"".equals(colorId) && !"null".equalsIgnoreCase(colorId)) {
                        sql.append(" and ColorID = '" + colorId + "' ");
                    }
                } else if (sizeGroupList.size() > 1 && i < sizeGroupList.size()) {
                    sql.append(" select (select Code from Goods where goodsID = t" + i + ".GoodsID) GoodsCode,GoodsID,(select Color from Color where colorID = t" + i + ".colorID) Color,ColorID,A_x_" + i + " Quantity,'" + size + "' Size from [tempdb].dbo.[_posRpt05" + client.getUserID() + "] t" + i
                            + " with(nolock) where A_x_" + i + " <> 0 ");
                    if (colorId != null && !"".equals(colorId) && !"null".equalsIgnoreCase(colorId)) {
                        sql.append(" and ColorID = '" + colorId + "' ");
                    }
                    sql.append(" union all ");
                } else if (sizeGroupList.size() > 1 && i == sizeGroupList.size()) {
                    sql.append(" select (select Code from Goods where goodsID = t" + i + ".GoodsID) GoodsCode,GoodsID,(select Color from Color where colorID = t" + i + ".colorID) Color,ColorID,A_x_" + i + " Quantity,'" + size + "' Size from [tempdb].dbo.[_posRpt05" + client.getUserID() + "] t" + i
                            + " with(nolock) where A_x_" + i + " <> 0 ");
                    if (colorId != null && !"".equals(colorId) && !"null".equalsIgnoreCase(colorId)) {
                        sql.append(" and ColorID = '" + colorId + "' ");
                    }
                    sql.append(" Order by GoodsCode, Color ");
                }
            }
            List list = commonDao.findForJdbc(sql.toString());
            j.setObj(list);
        } catch (Exception e) {
            j.setSuccess(false);
            j.setMsg(e.getMessage());
            SysLogger.error(e.getMessage(), e);
        }
        return j;
    }

}
