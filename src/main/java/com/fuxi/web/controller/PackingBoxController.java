package com.fuxi.web.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import com.fuxi.core.common.dao.impl.CommonDao;
import com.fuxi.core.common.model.json.AjaxJson;
import com.fuxi.core.common.service.PackingBoxService;
import com.fuxi.system.util.Client;
import com.fuxi.system.util.MyTools;
import com.fuxi.system.util.ResourceUtil;
import com.fuxi.system.util.SysLogger;
import com.fuxi.system.util.oConvertUtils;

/**
 * Title: PackingBoxController Description: 装箱单逻辑控制器
 * 
 * @author LYJ
 * 
 */
@Controller
@RequestMapping("/packing")
public class PackingBoxController extends BaseController {

    private Logger log = Logger.getLogger(PackingBoxController.class);
    public static List<Map<String, String>> packingBoxMap = new ArrayList<Map<String, String>>();

    @Autowired
    private CommonDao commonDao;
    @Autowired
    private PackingBoxService packingBoxService;

    /**
     * 获取装箱单列表
     * 
     * @param req
     * @return
     */
    @RequestMapping(params = "getList")
    @ResponseBody
    public AjaxJson getList(HttpServletRequest req) {
        AjaxJson j = new AjaxJson();
        j.setAttributes(new HashMap<String, Object>());
        Client client = ResourceUtil.getClientFromSession(req);
        Map<String, String> map = client.getMap();
        String userRight = map.get(client.getUserID());
        try {
            int page = oConvertUtils.getInt(req.getParameter("currPage"));
            String no = oConvertUtils.getString(req.getParameter("no"));
            String customerId = oConvertUtils.getString(req.getParameter("customerId"));
            String employeeId = oConvertUtils.getString(req.getParameter("employeeId"));
            String relationType = oConvertUtils.getString(req.getParameter("relationType"));
            StringBuffer sql = new StringBuffer();
            // 创建新表
            int exits = Integer.parseInt(String.valueOf(commonDao.getData(" select count(1) from dbo.[sysobjects] where name = 'AlreadyPackingBoxPDA' ")));
            if (exits < 1) {
                commonDao.executeSql("  create table AlreadyPackingBoxPDA(ID int primary key identity(1,1),RelationID varchar(50) not null) ");
            }
            if ("sales".equals(relationType)) {
                sql.append(
                        "  select no No,salesID ID,EmployeeID,BrandID,(select Code from brand where brandId = s.brandId) BrandCode,DepartmentID,Type,(select Department from Department where DepartmentID = s.DepartmentID) Warehouse,CustomerID, "
                                + " (select Customer from Customer where CustomerID = s.CustomerID) Customer,isnull(QuantitySum,0) QuantitySum from Sales s  where auditFlag = 1 and " + " direction='1' and departmentId in (").append(userRight)
                        .append(") and salesId not in (select relationId from PackingBox p where p.relationId is not null and relationId <> '' and s.QuantitySum = p.QuantitySum) " + " and salesId not in (select relationId from AlreadyPackingBoxPDA a where a.relationId = s.salesId)");
            } else if ("stockOut".equals(relationType)) {
                sql.append(
                        " select no No,stockID ID,EmployeeID,BrandID,(select Code from brand where brandId = s.brandId) BrandCode,DepartmentID,Type,(select Department from Department where DepartmentID = s.DepartmentID) Warehouse, "
                                + " CustomerID,(select Customer from Customer where CustomerID = s.CustomerID) Customer,isnull(QuantitySum,0) QuantitySum from Stock s " + " where s.direction = -1 and departmentId in (").append(userRight)
                        .append(") and s.stockId not in (select relationId from PackingBox p where relationId is not null and relationId <> '' and s.QuantitySum = p.QuantitySum) " + " and stockID not in (select relationId from AlreadyPackingBoxPDA a where a.relationId = s.stockID) ");
            }
            // 添加筛选条件
            if (null != no && !"".equals(no) && !"null".equalsIgnoreCase(no)) {
                sql.append(" and no = '" + no + "' ");
            }
            if (null != customerId && !"".equals(customerId) && !"null".equalsIgnoreCase(customerId)) {
                sql.append(" and customerId = '" + customerId + "' ");
            }
            if (null != employeeId && !"".equals(employeeId) && !"null".equalsIgnoreCase(employeeId)) {
                sql.append(" and employeeId = '" + employeeId + "' ");
            }
            sql.append(" order by madebydate desc,No desc ");
            List list = commonDao.findForJdbc(sql.toString(), page, 15);
            j.setObj(list);
        } catch (Exception e) {
            j.setSuccess(false);
            j.setMsg(e.getMessage());
            SysLogger.error(e.getMessage(), e);
        }
        return j;
    }

    /**
     * 获取已装箱的装箱单列表
     * 
     * @param req
     * @return
     */
    @RequestMapping(params = "getCompletePackingList")
    @ResponseBody
    public AjaxJson getCompletePackingList(HttpServletRequest req) {
        AjaxJson j = new AjaxJson();
        j.setAttributes(new HashMap<String, Object>());
        try {
            int page = oConvertUtils.getInt(req.getParameter("currPage"));
            String no = oConvertUtils.getString(req.getParameter("no"));
            String customerId = oConvertUtils.getString(req.getParameter("customerId"));
            StringBuffer sql = new StringBuffer();
            sql.append(" select so.PackingBoxID, de.Department , No, CONVERT(varchar(100), Date, 111) Date,isnull(QuantitySum,0) QuantitySum," + "so.madebydate,(select Name from Employee where employeeId = so.EmployeeId) Employee,"
                    + "isnull(so.Memo,'') Memo,(select Customer from Customer where CustomerId = so.CustomerId) Customer,CustomerID," + "(select Brand from Brand where BrandId = so.BrandId) Brand,(select Department from Department "
                    + " where so.DepartmentId = DepartmentID) Warehouse from PackingBox so left join Department de on de.DepartmentID = so.DepartmentID where 1=1 ");
            // 添加筛选条件
            if (null != no && !"".equals(no) && !"null".equalsIgnoreCase(no)) {
                sql.append(" and relationNo = '" + no + "' ");
            }
            if (null != customerId && !"".equals(customerId) && !"null".equalsIgnoreCase(customerId)) {
                sql.append(" and customerId = '" + customerId + "' ");
            }
            sql.append(" order by madebydate desc,No desc ");
            List list = commonDao.findForJdbc(sql.toString(), page, 15);
            j.setObj(list);
        } catch (Exception e) {
            j.setSuccess(false);
            j.setMsg(e.getMessage());
            SysLogger.error(e.getMessage(), e);
        }
        return j;
    }

    /**
     * 获取已装箱的装箱单列表
     * 
     * @param req
     * @return
     */
    @RequestMapping(params = "getCompletePackingBoxNoList")
    @ResponseBody
    public AjaxJson getCompletePackingBoxNoList(HttpServletRequest req) {
        AjaxJson j = new AjaxJson();
        j.setAttributes(new HashMap<String, Object>());
        try {
            String packingBoxId = oConvertUtils.getString(req.getParameter("packingBoxId"));
            List boxNoList = commonDao.findForJdbc(" select distinct BoxNo from PackingBoxDetailPDA where PackingBoxID = ? ", packingBoxId);
            j.setObj(boxNoList);
        } catch (Exception e) {
            j.setSuccess(false);
            j.setMsg(e.getMessage());
            SysLogger.error(e.getMessage(), e);
        }
        return j;
    }

    /**
     * 删除已装箱的装箱单
     * 
     * @param req
     * @return
     */
    @RequestMapping(params = "deleteAlreadyPackingBoxNo")
    @ResponseBody
    public AjaxJson deleteAlreadyPackingBoxNo(HttpServletRequest req) {
        AjaxJson j = new AjaxJson();
        j.setAttributes(new HashMap<String, Object>());
        try {
            String packingBoxId = oConvertUtils.getString(req.getParameter("packingBoxId"));
            // 查询客户对应装箱单的起始箱号
            String boxNo = String.valueOf(commonDao.getData(" select top 1 boxNo from packingboxdetailpda where packingBoxId = ? order by boxNo ", packingBoxId));
            boolean flag = packingBoxService.deleteAlreadyPackingBoxNo(packingBoxId);
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("boxNo", boxNo);
            map.put("flag", flag);
            j.setObj(map);
        } catch (Exception e) {
            j.setSuccess(false);
            j.setMsg(e.getMessage());
            SysLogger.error(e.getMessage(), e);
        }
        return j;
    }

    /**
     * 获取装箱单列表
     * 
     * @param req
     * @return
     */
    @RequestMapping(params = "getData")
    @ResponseBody
    public synchronized AjaxJson getData(HttpServletRequest req) {
        AjaxJson j = new AjaxJson();
        Client client = ResourceUtil.getClientFromSession(req);
        j.setAttributes(new HashMap<String, Object>());
        try {
            int alreadyBoxing = 0;
            boolean inPacking = false;
            List<Map<String, Object>> tList = new ArrayList<Map<String, Object>>();
            String relationType = oConvertUtils.getString(req.getParameter("relationType"));
            String relationId = oConvertUtils.getString(req.getParameter("relationId"));
            for (int i = 0; i < packingBoxMap.size(); i++) {
                Map<String, String> map = packingBoxMap.get(i);
                if (relationId.equals(map.get("relationId")) && client.getUserID().equals(map.get("userId"))) {
                    inPacking = true;
                }
            }
            // 创建新表
            int exits = Integer.parseInt(String.valueOf(commonDao.getData(" select count(1) from dbo.[sysobjects] where name = 'PackingBoxDetailPDA' ")));
            if (exits < 1) {
                commonDao
                        .executeSql(" create table PackingBoxDetailPDA(PackingBoxDetailID int primary key identity(1,1),RelationID varchar(50) not null,PackingBoxID varchar(50) not null, BoxNo varchar(50) not null, GoodsID varchar(20) not null,ColorID varchar(20) not null,SizeID varchar(20) not null,Quantity int not null,RetailSales money,RetailAmount money);");
            }
            if (!inPacking) {
                List detailList = null;// 返回的货品信息
                List boxNoList = null;
                if ("0".equals(relationType)) {
                    int boxQtySum = Integer.parseInt(String.valueOf(commonDao.getData("select isnull(sum(isnull(sodt.BoxQty,0)),0) BoxQty from SalesDetailTemp sodt where SalesID = ? ", relationId)));
                    StringBuffer sb = new StringBuffer();
                    sb.append(" select so.CustomerID ,so.No,isnull(so.AuditFlag,0)  AuditFlag,")
                            .append(boxQtySum)
                            .append(" BoxQtySum, isnull(d1.Customer,'') Customer,d2.MustExistsGoodsFlag, ")
                            .append(" so.DepartmentID DepartmentID ,isnull(d2.Department,'') Department,isnull(so.Memo,'') Memo,isnull(so.QuantitySum,0) QuantitySum, Type,so.EmployeeID,(select Name from Employee where EmployeeID = so.EmployeeID) Employee,so.BrandID,(select Brand from Brand where brandId = so.BrandId) Brand,"
                                    + "(select PackingBoxID from PackingBox p where p.relationId = so.salesID) PackingBoxID,(select No from PackingBox p where p.relationId = so.salesID) PackingBoxNo from Sales so  ").append(" left join Customer d1 on d1.CustomerID = so.CustomerID ")
                            .append(" left join Department d2 on d2.DepartmentID = so.DepartmentID ").append(" where SalesID = '").append(relationId).append("'");
                    List list = commonDao.findForJdbc(sb.toString());
                    if (list.size() > 0) {
                        Map tmap = (Map) list.get(0);
                        j.setAttributes(tmap);
                        sb = new StringBuffer();
                        sb.append(" select detail.GoodsID,g.Name GoodsName,c.No ColorCode,s.No SizeCode,detail.ColorID,c.Color,isnull(g.RetailSales,0) RetailSales, ").append(" detail.SizeID,s.Size,detail.Quantity,'0' Qty,'' BoxNo,g.Code GoodsCode ").append(" from SalesDetail detail ")
                                .append(" join Goods g on g.GoodsID = detail.GoodsID ").append(" join Color c on c.ColorID = detail.ColorID ").append(" join Size s on s.SizeID = detail.SizeID ").append(" join GoodsType gt on gt.GoodsTypeID = g.GoodsTypeID ")
                                .append(" join SizeGroup sg on sg.SizeGroupID = gt.SizeGroupID ").append("  where detail.SalesID = '").append(relationId).append("' order by detail.GoodsID ");
                        detailList = commonDao.findForJdbc(sb.toString());
                        // tList = commonDao.findForJdbc(sb.toString());
                    }
                } else if ("1".equals(relationType)) {
                    int boxQtySum = Integer.parseInt(String.valueOf(commonDao.getData("select isnull(sum(isnull(sodt.BoxQty,0)),0) BoxQty from stockDetailtemp sodt where stockId = ? ", relationId)));
                    StringBuffer sb = new StringBuffer();
                    sb.append("  select de.No,RelationNo,RelationWarehouseID, WarehouseID ,isnull(de.AuditFlag,0)  AuditFlag,")
                            .append(boxQtySum)
                            .append(" BoxQtySum,( select isnull(Department,'') from Department where DepartmentId = WarehouseID) Warehouse,( select isnull(Department,'') from Department where DepartmentId = RelationWarehouseID) RelationWarehouse,")
                            .append(" isnull(de.Memo,'') Memo,de.EmployeeID,(select Name from Employee where EmployeeID = de.EmployeeID) Employee,de.BrandID,(select Brand from Brand where brandId = de.BrandId) Brand ,isnull(de.QuantitySum,0) QuantitySum,"
                                    + "(select PackingBoxID from PackingBox p where p.relationId = de.stockID) PackingBoxID,(select No from PackingBox p where p.relationId = de.stockID) PackingBoxNo from stock de  ").append(" where stockId = '").append(relationId).append("'");
                    List list = commonDao.findForJdbc(sb.toString());
                    if (list.size() > 0) {
                        Map temp = (Map) list.get(0);
                        j.setAttributes(temp);
                        sb = new StringBuffer();
                        sb.append(" select detail.GoodsID,g.Name GoodsName,c.No ColorCode,s.No SizeCode,detail.ColorID,c.Color,isnull(g.RetailSales,0) RetailSales, ").append(" detail.SizeID,s.Size,detail.Quantity,'0' Qty,'' BoxNo,g.Code GoodsCode ").append(" from StockDetail detail ")
                                .append(" join Goods g on g.GoodsID = detail.GoodsID ").append(" join Color c on c.ColorID = detail.ColorID ").append(" join Size s on s.SizeID = detail.SizeID ").append(" join GoodsType gt on gt.GoodsTypeID = g.GoodsTypeID ")
                                .append(" join SizeGroup sg on sg.SizeGroupID = gt.SizeGroupID ").append("  where detail.StockID = '").append(relationId).append("' order by detail.GoodsID ");
                        detailList = commonDao.findForJdbc(sb.toString());
                        // tList = commonDao.findForJdbc(sb.toString());
                    }
                }
                // 获取箱号
                boxNoList = commonDao.findForJdbc(" select distinct BoxNo from PackingBoxDetailPDA where relationID = ? ", relationId);
                StringBuilder boxNoStr = new StringBuilder();
                for (int i = 0; i < boxNoList.size(); i++) {
                    Map<String, Object> map = (Map<String, Object>) boxNoList.get(i);
                    String boxNo = (String) map.get("BoxNo");
                    if (i == boxNoList.size() - 1) {
                        boxNoStr.append(boxNo);
                    } else {
                        boxNoStr.append(boxNo + ",");
                    }
                }
                // 获取装箱记录
                List tempList = commonDao.findForJdbc(" select GoodsID,ColorID,SizeID,sum(Quantity) Qty from PackingBoxDetailPDA where relationID = ? group by GoodsID,ColorID,SizeID ", relationId);
                if (tempList.size() > 0) {// 有装箱记录
                    // 箱号对应已经装箱的数量
                    Map<String, Object> m = (Map<String, Object>) boxNoList.get(boxNoList.size() - 1);
                    String boxNo = (String) m.get("BoxNo");
                    alreadyBoxing = commonDao.getDataToInt(" select sum(quantity) from packingboxdetailpda where packingboxId = (select top 1 packingboxId from packingboxdetailpda where relationID = ?) and boxNo = ? ", relationId, boxNo);
                    for (int i = 0; i < detailList.size(); i++) {
                        Map<String, Object> map = (Map<String, Object>) detailList.get(i);
                        String goodsId = (String) map.get("GoodsID");
                        String colorId = (String) map.get("ColorID");
                        String sizeId = (String) map.get("SizeID");
                        for (int k = 0; k < tempList.size(); k++) {
                            Map<String, Object> tmap = (Map<String, Object>) tempList.get(k);
                            String tgoodsId = (String) tmap.get("GoodsID");
                            String tcolorId = (String) tmap.get("ColorID");
                            String tsizeId = (String) tmap.get("SizeID");
                            // String tboxNo = (String) tmap.get("BoxNo");
                            int tqty = (Integer) tmap.get("Qty");
                            if (goodsId.equals(tgoodsId) && colorId.equals(tcolorId) && sizeId.equals(tsizeId)) {
                                map.put("Qty", tqty);
                                map.put("BoxNo", boxNoStr.toString());
                            }
                        }
                    }
                }
                // 限制单据唯一操作
                Map<String, String> map = new HashMap<String, String>();
                map.put("relationId", relationId);
                map.put("userId", client.getUserID());
                packingBoxMap.add(map);//servlet-api-3.0 没有这个了tomcat中的有req.getServletContext().getRealPath("/")
                Map<String, String> codeMap = MyTools.parserXml(req.getServletContext().getRealPath("/") + "/resources/typeLetter.xml");
                j.getAttributes().put("detailList", detailList);
                j.getAttributes().put("boxNoList", boxNoList);
                j.getAttributes().put("codeMap", codeMap);
                j.getAttributes().put("alreadyBoxing", String.valueOf(alreadyBoxing));
            }
            j.setObj(inPacking);
        } catch (Exception e) {
            j.setSuccess(false);
            j.setMsg(e.getMessage());
            SysLogger.error(e.getMessage(), e);
        }
        return j;
    }

    /**
     * 保存/修改装箱单
     * 
     * @param req
     * @return
     */
    @RequestMapping(params = "savePacking")
    @ResponseBody
    public AjaxJson savePacking(HttpServletRequest req) {
        AjaxJson j = new AjaxJson();
        j.setAttributes(new HashMap<String, Object>());
        Client client = ResourceUtil.getClientFromSession(req);
        try {
            String relationId = oConvertUtils.getString(req.getParameter("relationId"));
            String relationNo = oConvertUtils.getString(req.getParameter("relationNo"));
            String customerId = oConvertUtils.getString(req.getParameter("customerId"));
            String type = oConvertUtils.getString(req.getParameter("type"));
            String departmentId = oConvertUtils.getString(req.getParameter("departmentId"));
            String employeeId = oConvertUtils.getString(req.getParameter("employeeId"));
            String memo = oConvertUtils.getString(req.getParameter("memo"));
            String brandId = oConvertUtils.getString(req.getParameter("brandId"));
            String boxNo = oConvertUtils.getString(req.getParameter("boxNo"));
            String relationType = oConvertUtils.getString(req.getParameter("relationType"));
            String packingBoxId = oConvertUtils.getString(req.getParameter("packingBoxId"));
            String goodsId = oConvertUtils.getString(req.getParameter("goodsId"));
            String colorId = oConvertUtils.getString(req.getParameter("colorId"));
            String sizeId = oConvertUtils.getString(req.getParameter("sizeId"));
            String retailSales = oConvertUtils.getString(req.getParameter("retailSales"));
            String qty = oConvertUtils.getString(req.getParameter("qty"));
            packingBoxId = packingBoxService.savePackingBox(goodsId, colorId, sizeId, qty, packingBoxId, relationType, relationId, relationNo, customerId, departmentId, employeeId, brandId, boxNo, type, memo, retailSales, client);
            Map<String, String> temp = new HashMap<String, String>();
            String packingBoxNo = String.valueOf(commonDao.getData(" select No from packingBox where packingBoxId = ?", packingBoxId));
            // 箱号对应已经装箱的数量
            int alreadyBoxing = commonDao.getDataToInt(" select sum(quantity) from packingboxdetailpda where packingboxId = ? and boxNo = ? ", packingBoxId, boxNo);
            temp.put("packingBoxId", packingBoxId);
            temp.put("packingBoxNo", packingBoxNo);
            temp.put("alreadyBoxing", String.valueOf(alreadyBoxing));
            j.setObj(temp);
        } catch (Exception e) {
            j.setSuccess(false);
            j.setMsg(e.getMessage());
            SysLogger.error(e.getMessage(), e);
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
        }
        return j;
    }

    /**
     * 修改装箱记录
     * 
     * @param req
     * @return
     */
    @RequestMapping(params = "updatePacking")
    @ResponseBody
    public AjaxJson updatePacking(HttpServletRequest req) {
        AjaxJson j = new AjaxJson();
        try {
            String packingBoxId = oConvertUtils.getString(req.getParameter("packingBoxId"));
            String boxNo = oConvertUtils.getString(req.getParameter("boxNo"));
            String goodsId = oConvertUtils.getString(req.getParameter("goodsId"));
            String colorId = oConvertUtils.getString(req.getParameter("colorId"));
            String sizeId = oConvertUtils.getString(req.getParameter("sizeId"));
            String retailSales = oConvertUtils.getString(req.getParameter("retailSales"));
            String qtyStr = oConvertUtils.getString(req.getParameter("count"));
            packingBoxService.updatePacking(packingBoxId, boxNo, goodsId, colorId, sizeId, retailSales, qtyStr);
            // 箱号对应已经装箱的数量
            int alreadyBoxing = commonDao.getDataToInt(" select sum(quantity) from packingboxdetailpda where packingboxId = ? and boxNo = ? ", packingBoxId, boxNo);
            j.setObj(alreadyBoxing);
        } catch (Exception e) {
            j.setSuccess(false);
            j.setMsg(e.getMessage());
            SysLogger.error(e.getMessage(), e);
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
        }
        return j;
    }

    /**
     * 根据箱号获取已经装箱的明细记录
     * 
     * @param req
     * @return
     */
    @RequestMapping(params = "getAlreadyPackingBoxByBoxNo")
    @ResponseBody
    public synchronized AjaxJson getAlreadyPackingBoxByBoxNo(HttpServletRequest req) {
        AjaxJson j = new AjaxJson();
        try {
            String relationType = oConvertUtils.getString(req.getParameter("relationType"));
            String packingBoxId = oConvertUtils.getString(req.getParameter("packingBoxId"));
            String relationId = oConvertUtils.getString(req.getParameter("relationId"));
            String boxNo = oConvertUtils.getString(req.getParameter("boxNo"));
            String tableName = null;
            StringBuilder sql = new StringBuilder();
            if ("0".equals(relationType)) {
                tableName = "SalesDetail";
            } else if ("1".equals(relationType)) {
                tableName = "StockDetail";
            }
            Map<String, Object> temp = new HashMap<String, Object>();
            sql.append(" select (select code from goods where goodsId = p.goodsId) GoodsCode,GoodsID,(select color from color where colorId = p.colorId) Color,ColorID,(select size from size where sizeId = p.sizeId) Size,SizeID,sum(Quantity) Qty,(select quantity from " + tableName
                    + " where salesId = ? and goodsId = p.goodsId and colorId = p.colorId and sizeId = p.sizeId) Quantity from PackingBoxDetailPDA p where packingBoxId = ? and boxNo = ? group by GoodsID,ColorID,SizeID ");
            List datas = commonDao.findForJdbc(sql.toString(), relationId, packingBoxId, boxNo);
            // 箱号对应已经装箱的数量
            int alreadyBoxing = commonDao.getDataToInt(" select sum(quantity) from packingboxdetailpda where packingboxId = ? and boxNo = ? ", packingBoxId, boxNo);
            temp.put("alreadyBoxing", String.valueOf(alreadyBoxing));
            temp.put("datas", datas);
            j.setObj(temp);
        } catch (Exception e) {
            j.setSuccess(false);
            j.setMsg(e.getMessage());
            SysLogger.error(e.getMessage(), e);
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
        }
        return j;
    }

    /**
     * 根据箱号和获取指定货品已经装箱的数量
     * 
     * @param req
     * @return
     */
    @RequestMapping(params = "getAlreadyPackingBoxCount")
    @ResponseBody
    public AjaxJson getAlreadyPackingBoxCount(HttpServletRequest req) {
        AjaxJson j = new AjaxJson();
        try {
            String packingBoxId = oConvertUtils.getString(req.getParameter("packingBoxId"));
            String relationId = oConvertUtils.getString(req.getParameter("relationId"));
            String boxNo = oConvertUtils.getString(req.getParameter("boxNo"));
            String goodsId = oConvertUtils.getString(req.getParameter("goodsId"));
            String colorId = oConvertUtils.getString(req.getParameter("colorId"));
            String sizeId = oConvertUtils.getString(req.getParameter("sizeId"));
            int count = commonDao.getDataToInt(" select sum(Quantity) Quantity from packingboxdetailpda where goodsId = ? and colorId = ? and sizeId = ? and packingBoxId = ? and boxNo = ? and relationId = ?", goodsId, colorId, sizeId, packingBoxId, boxNo, relationId);
            j.setObj(count);
        } catch (Exception e) {
            j.setSuccess(false);
            j.setMsg(e.getMessage());
            SysLogger.error(e.getMessage(), e);
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
        }
        return j;
    }

    /**
     * 完成装箱单操作
     * 
     * @param req
     * @return
     */
    @RequestMapping(params = "completePacking")
    @ResponseBody
    public AjaxJson completePacking(HttpServletRequest req) {
        AjaxJson j = new AjaxJson();
        try {
            String relationId = oConvertUtils.getString(req.getParameter("relationId"));
            boolean flag = packingBoxService.completePackingBox(relationId);
            j.setObj(flag);
        } catch (Exception e) {
            j.setSuccess(false);
            j.setMsg(e.getMessage());
            SysLogger.error(e.getMessage(), e);
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
        }
        return j;
    }

    /**
     * 释放装箱单
     * 
     * @param req
     * @return
     */
    @RequestMapping(params = "releasePacking")
    @ResponseBody
    public AjaxJson releasePacking(HttpServletRequest req) {
        AjaxJson j = new AjaxJson();
        Client client = ResourceUtil.getClientFromSession(req);
        try {
            String relationId = oConvertUtils.getString(req.getParameter("relationId"));
            for (int i = 0; i < packingBoxMap.size(); i++) {
                Map<String, String> map = packingBoxMap.get(i);
                if (relationId.equals(map.get("relationId")) && client.getUserID().equals(map.get("userId"))) {
                    packingBoxMap.remove(map);
                }
            }
        } catch (Exception e) {
            j.setSuccess(false);
            j.setMsg(e.getMessage());
            SysLogger.error(e.getMessage(), e);
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
        }
        return j;
    }

}
