package com.fuxi.web.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import net.sf.json.JSONArray;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import com.fuxi.core.common.dao.impl.CommonDao;
import com.fuxi.core.common.model.json.AjaxJson;
import com.fuxi.core.common.service.InventorySheetService;
import com.fuxi.system.util.Client;
import com.fuxi.system.util.DataUtils;
import com.fuxi.system.util.ResourceUtil;
import com.fuxi.system.util.SysLogger;
import com.fuxi.system.util.oConvertUtils;

/**
 * Title: InventorySheetController Description: 盘点单逻辑控制器
 * 
 * @author Administrator
 * 
 */
@Controller
@RequestMapping("/inventorySheet")
public class InventorySheetController extends BaseController {
    private Logger log = Logger.getLogger(InventorySheetController.class);
    private SelectController controller = new SelectController();
    @Autowired
    private CommonDao commonDao;
    @Autowired
    private InventorySheetService sheetService;

    /**
     * 获取盘点单列表(主表)
     * 
     * @param user
     * @param req
     * @return
     */
    @RequestMapping(params = "inventory")
    @ResponseBody
    public AjaxJson inventory(HttpServletRequest req) {
        Client client = ResourceUtil.getClientFromSession(req);
        AjaxJson j = new AjaxJson();
        j.setAttributes(new HashMap<String, Object>());
        Map<String, String> map = client.getMap();
        String userRight = map.get(client.getUserID());
        try {
            int page = oConvertUtils.getInt(req.getParameter("currPage"));
            String audit = oConvertUtils.getString(req.getParameter("audit"));
            String no = oConvertUtils.getString(req.getParameter("no"));
            String beginDate = oConvertUtils.getString(req.getParameter("beginDate"));
            String endDate = oConvertUtils.getString(req.getParameter("endDate"));
            String departmentId = oConvertUtils.getString(req.getParameter("departmentId"));
            String employeeId = oConvertUtils.getString(req.getParameter("employeeId"));
            StringBuffer sb = new StringBuffer();
            sb.append(
                    " select st.StocktakingID, de.Department, No, CONVERT(varchar(100), Date, 111) Date,isnull(QuantitySum,0) QuantitySum," + " RetailAmountSum, AuditFlag,(select Name from Employee where employeeId = st.EmployeeId) Employee,"
                            + "isnull(st.Memo,'') Memo,(select Brand from Brand where BrandId = st.BrandID) Brand  from Stocktaking st  ").append(" left join Department de on de.DepartmentID = st.DepartmentID where de.DepartmentID in (").append(userRight).append(")   ");
            // 按条件查询
            if (null != audit && "0".equals(audit)) {
                // 未审核
                sb.append(" and AuditFlag = '0' ");
            } else if (null != audit && "1".equals(audit)) {
                // 已审核
                sb.append(" and AuditFlag = '1' ");
            }
            // 查询单号时
            if (no != null && !"".equals(no.trim()) && !"null".equalsIgnoreCase(no)) {
                sb.append(" and No = '" + no + "' ");
            }
            // 时间区间
            if (beginDate != null && !"".equals(beginDate.trim()) && !"null".equalsIgnoreCase(beginDate) && endDate != null && !"".equals(endDate.trim()) && !"null".equalsIgnoreCase(endDate)) {
                sb.append(" and Date between convert(datetime,'" + beginDate + "', 120) and convert(datetime,'" + endDate + "', 120) ");
            }
            // 部门
            if (departmentId != null && !"".equals(departmentId.trim()) && !"null".equalsIgnoreCase(departmentId)) {
                sb.append(" and st.departmentId = '" + departmentId + "' ");
            }
            // 经手人
            if (employeeId != null && !"".equals(employeeId.trim()) && !"null".equalsIgnoreCase(employeeId)) {
                sb.append(" and st.employeeId = '" + employeeId + "' ");
            }
            sb.append(" order by madebydate desc,No desc ");
            List list = commonDao.findForJdbc(sb.toString(), page, 15);
            j.setObj(list);
        } catch (Exception e) {
            j.setSuccess(false);
            j.setMsg(e.getMessage());
            SysLogger.error(e.getMessage(), e);
        }
        return j;
    }

    /**
     * 获取盘点单明细(子表)
     * 
     * @param req
     * @return
     */
    @RequestMapping(params = "inventoryEdit")
    @ResponseBody
    public AjaxJson inventoryEdit(HttpServletRequest req) {
        AjaxJson j = new AjaxJson();
        j.setAttributes(new HashMap<String, Object>());
        Client client = ResourceUtil.getClientFromSession(req);
        try {
            String stocktakingId = oConvertUtils.getString(req.getParameter("StocktakingID"));
            int boxQtySum = Integer.parseInt(String.valueOf(commonDao.getData("select isnull(sum(isnull(sodt.BoxQty,0)),0) BoxQty from stocktakingDetailTemp sodt where stocktakingId = ? ", stocktakingId)));
            int sizeStrCount = Integer.parseInt(String.valueOf(commonDao.getData("select count(1) from stocktakingDetailTemp sodt where stocktakingId = ? ", stocktakingId)));
            int notNullSizeStrCount = Integer.parseInt(String.valueOf(commonDao.getData("select count(1) from stocktakingDetailTemp sodt where stocktakingId = ? and sodt.SizeStr is not null and sodt.SizeStr <> '' ", stocktakingId)));
            StringBuffer sb = new StringBuffer();
            sb.append(" select so.No,isnull(so.AuditFlag,0) AuditFlag,")
                    .append(boxQtySum)
                    .append(" BoxQtySum, so.DepartmentID DepartmentID ,isnull(d2.Department,'') Department,isnull(so.Memo,'') Memo,isnull(so.QuantitySum,0) QuantitySum,so.EmployeeID,(select Name from Employee where EmployeeID = so.EmployeeID) Employee,so.BrandID,(select Brand from Brand where brandId = so.BrandId) Brand  from Stocktaking so  ")
                    .append(" left join Department d2 on d2.DepartmentID = so.DepartmentID ").append(" where stocktakingId = '").append(stocktakingId).append("'");
            List list = commonDao.findForJdbc(sb.toString());
            if (list.size() > 0) {
                Map map = (Map) list.get(0);
                j.setAttributes(map);
                sb = new StringBuffer();
                List detailList = null;
                if (notNullSizeStrCount != 0 && sizeStrCount == notNullSizeStrCount) {
                    // 更新箱条码配码
                    commonDao.validInvoiceSizeStr(30, stocktakingId);
                    sb.append(" select a.*,g.code GoodsCode ,g.name GoodsName,c.No ColorCode,isnull(pdt.BoxQty,0) BoxQty,pdt.Quantity QuantitySum,a.SizeStr, ")
                            .append(" c.Color ,sg.SizeGroupID,isnull(pdt.RetailSales,0) RetailSales,isnull(pdt.memo,'') meno from GoodsBoxBarcode a join goods g on a.goodsid=g.goodsid ").append(" join color c on a.colorid=c.colorid ").append(" join GoodsType gt on gt.GoodsTypeID = g.GoodsTypeID ")
                            .append(" join SizeGroup sg on sg.SizeGroupID =gt.SizeGroupID ").append(" join stocktakingDetailtemp pdt on pdt.goodsId = a.goodsId and pdt.colorId = a.colorid and pdt.sizeStr = a.sizeStr ").append("  where pdt.stocktakingId = '").append(stocktakingId)
                            .append("' order by pdt.GoodsID ");
                    detailList = controller.getDetailTemp(commonDao.findForJdbc(sb.toString()), client, commonDao);
                } else if (notNullSizeStrCount != 0 && sizeStrCount > notNullSizeStrCount) {
                    // 更新箱条码配码
                    commonDao.validInvoiceSizeStr(30, stocktakingId);
                    // 装箱
                    sb.append(" select a.*,g.code GoodsCode ,g.name GoodsName,c.No ColorCode,isnull(a.BoxQty,0) BoxQty,a.Quantity QuantitySum,a.SizeStr, ")
                            .append(" c.Color ,sg.SizeGroupID,isnull(a.RetailSales,0) RetailSales,isnull(a.memo,'') meno from stocktakingDetailtemp a join goods g on a.goodsid=g.goodsid ").append(" join color c on a.colorid=c.colorid ")
                            .append(" join GoodsType gt on gt.GoodsTypeID = g.GoodsTypeID ").append(" join SizeGroup sg on sg.SizeGroupID =gt.SizeGroupID ").append("  where a.stocktakingId = '").append(stocktakingId).append("' and a.SizeStr is not null and a.SizeStr <> '' order by a.GoodsID ");
                    detailList = controller.getDetailTemp(commonDao.findForJdbc(sb.toString()), client, commonDao);
                    // 重置SQL
                    sb = new StringBuffer();
                    // 散件
                    sb.append(" select detail.GoodsID,g.Name GoodsName,c.No ColorCode,s.No SizeCode,detail.ColorID,c.Color, ").append(" detail.SizeID,s.Size,detail.Quantity,sodt.Quantity QuantitySum,'' Barcode,'' SizeGroupID,g.Code GoodsCode,ss.No IndexNo ")
                            .append(" ,'0' BoxQty,'0' OneBoxQty,isnull(sodt.RetailSales,0) RetailSales,sodt.SizeStr,isnull(sodt.memo,'') meno from stocktakingDetail detail ").append(" join Goods g on g.GoodsID = detail.GoodsID ").append(" join Color c on c.ColorID = detail.ColorID ")
                            .append(" join Size s on s.SizeID = detail.SizeID ").append(" join stocktakingDetailtemp sodt on sodt.stocktakingId = detail.stocktakingId and sodt.GoodsID = detail.GoodsID and sodt.ColorID = detail.ColorID ")
                            .append(" join GoodsType gt on gt.GoodsTypeID = g.GoodsTypeID ").append(" join SizeGroup sg on sg.SizeGroupID = gt.SizeGroupID ").append(" join SizeGroupSize ss on ss.SizeGroupID = sg.SizeGroupID and ss.SizeID = detail.SizeID ").append("  where detail.stocktakingId = '")
                            .append(stocktakingId).append("' and sodt.SizeStr is null order by detail.GoodsID ");
                    List tempList = commonDao.findForJdbc(sb.toString());
                    detailList.addAll(tempList);
                } else {
                    sb.append(" select detail.GoodsID,g.Name GoodsName,c.No ColorCode,s.No SizeCode,detail.ColorID,c.Color, ").append(" detail.SizeID,s.Size,detail.Quantity,sodt.Quantity QuantitySum,'' Barcode,'' SizeGroupID,g.Code GoodsCode,ss.No IndexNo,")
                            .append(" isnull(sodt.BoxQty,0) BoxQty,isnull(sodt.Quantity/nullif(sodt.BoxQty,0),0) OneBoxQty,isnull(sodt.RetailSales,0) RetailSales,sodt.SizeStr,isnull(sodt.memo,'') meno from stocktakingDetail detail ").append(" join Goods g on g.GoodsID = detail.GoodsID ")
                            .append(" join Color c on c.ColorID = detail.ColorID ").append(" join Size s on s.SizeID = detail.SizeID ").append(" join stocktakingDetailtemp sodt on sodt.stocktakingId = detail.stocktakingId and sodt.GoodsID = detail.GoodsID and sodt.ColorID = detail.ColorID ")
                            .append(" join GoodsType gt on gt.GoodsTypeID = g.GoodsTypeID ").append(" join SizeGroup sg on sg.SizeGroupID = gt.SizeGroupID ").append(" join SizeGroupSize ss on ss.SizeGroupID = sg.SizeGroupID and ss.SizeID = detail.SizeID ").append("  where detail.stocktakingId = '")
                            .append(stocktakingId).append("' order by detail.GoodsID ");
                    detailList = commonDao.findForJdbc(sb.toString());
                }
                j.getAttributes().put("detailList", detailList);
            }
        } catch (Exception e) {
            j.setSuccess(false);
            j.setMsg(e.getMessage());
            SysLogger.error(e.getMessage(), e);
        }
        return j;
    }

    /**
     * 添加(修改)盘点单
     * 
     * @param req
     * @return
     */
    @RequestMapping(params = "addInventory")
    @ResponseBody
    public AjaxJson addInventory(HttpServletRequest req) {
        Client client = ResourceUtil.getClientFromSession(req);
        AjaxJson j = new AjaxJson();
        j.setAttributes(new HashMap<String, Object>());
        try {
            String deptId = oConvertUtils.getString(req.getParameter("deptId"));
            String stocktakingId = oConvertUtils.getString(req.getParameter("StocktakingID"));
            String employeeId = oConvertUtils.getString(req.getParameter("employeeId"));
            String memo = oConvertUtils.getString(req.getParameter("memo"));
            String brandId = oConvertUtils.getString(req.getParameter("brandId"));
            String jsonStr = oConvertUtils.getString(req.getParameter("data"));
            JSONArray datas = JSONArray.fromObject(jsonStr);
            List<Map<String, Object>> dataList = JSONArray.toList(datas, Map.class);
            String id = sheetService.saveInventorySheet(dataList, stocktakingId, deptId, employeeId, memo, brandId, client);
            j.getAttributes().put("stocktakingId", id);
        } catch (Exception e) {
            j.setSuccess(false);
            j.setMsg(e.getMessage());
            SysLogger.error(e.getMessage(), e);
        }
        return j;
    }

    /**
     * 修改或删除盘点单数量
     * 
     * @param req
     * @return
     */
    @RequestMapping(params = "updateCount")
    @ResponseBody
    public AjaxJson updateCount(HttpServletRequest req) {
        AjaxJson j = new AjaxJson();
        j.setAttributes(new HashMap<String, Object>());
        try {
            String stocktakingId = oConvertUtils.getString(req.getParameter("StocktakingID"));
            String jsonStr = oConvertUtils.getString(req.getParameter("data"));
            JSONArray datas = JSONArray.fromObject(jsonStr);
            List<Map<String, Object>> dataList = JSONArray.toList(datas, Map.class);
            sheetService.deleteInventorySheetDetail(dataList, stocktakingId);
        } catch (Exception e) {
            j.setSuccess(false);
            j.setMsg(e.getMessage());
            SysLogger.error(e.getMessage(), e);
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
        }
        return j;
    }

    /**
     * 审核盘点单
     * 
     * @param user
     * @param req
     * @return
     */
    @RequestMapping(params = "inventoryAudit")
    @ResponseBody
    public AjaxJson inventoryAudit(HttpServletRequest req) {
        int result = 0;
        AjaxJson j = new AjaxJson();
        j.setAttributes(new HashMap<String, Object>());
        try {
            String stocktakingId = oConvertUtils.getString(req.getParameter("StocktakingID"));
            String user = ((Client) ResourceUtil.getClientFromSession(req)).getUserName();
            StringBuffer sql = new StringBuffer();
            sql.append(" update Stocktaking set Audit = ? , AuditDate = ? , AuditFlag = ? where StocktakingID = ? ; ");
            result = commonDao.executeSql(sql.toString(), user, DataUtils.gettimestamp(), true, stocktakingId);
            j.getAttributes().put("result", result);
        } catch (Exception e) {
            j.setSuccess(false);
            j.setMsg(e.getMessage());
            SysLogger.error(e.getMessage(), e);
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
        }
        return j;
    }

    /**
     * 修改备注
     * 
     * @param user
     * @param req
     * @return
     */
    @RequestMapping(params = "updateMemo")
    @ResponseBody
    public AjaxJson updateMemo(HttpServletRequest req) {
        AjaxJson j = new AjaxJson();
        j.setAttributes(new HashMap<String, Object>());
        try {
            String stocktakingId = oConvertUtils.getString(req.getParameter("StocktakingID"));
            String memo = oConvertUtils.getString(req.getParameter("memo"));
            StringBuilder sb = new StringBuilder();
            sb.append(" update Stocktaking set memo = ? where StocktakingID = ? ");
            commonDao.executeSql(sb.toString(), memo, stocktakingId);
        } catch (Exception e) {
            j.setSuccess(false);
            j.setMsg(e.getMessage());
            SysLogger.error(e.getMessage(), e);
        }
        return j;
    }

}
