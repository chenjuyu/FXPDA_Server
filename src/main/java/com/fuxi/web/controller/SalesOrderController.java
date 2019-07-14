package com.fuxi.web.controller;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import net.sf.json.JSONArray;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import com.fuxi.core.common.dao.impl.CommonDao;
import com.fuxi.core.common.model.json.AjaxJson;
import com.fuxi.core.common.service.SalesOrderService;
import com.fuxi.core.vo.base.Period;
import com.fuxi.system.util.Client;
import com.fuxi.system.util.DataUtils;
import com.fuxi.system.util.ResourceUtil;
import com.fuxi.system.util.SysLogger;
import com.fuxi.system.util.oConvertUtils;

/**
 * Title: SalesOrderController Description: 销售订单逻辑控制器
 * 
 * @author LYJ
 * 
 */
@Controller
@RequestMapping("/salesOrder")
public class SalesOrderController extends BaseController {

    private Logger log = Logger.getLogger(SalesOrderController.class);
    private SelectController controller = new SelectController();

    @Autowired
    private CommonDao commonDao;
    @Autowired
    private SalesOrderService salesOrderService;

    /**
     * 根据条件获取销售订单(主表信息)
     * 
     * @param req
     * @return
     */
    @RequestMapping(params = "salesorder")
    @ResponseBody
    public AjaxJson salesorder(HttpServletRequest req) {
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
            String customerId = oConvertUtils.getString(req.getParameter("customerId"));
            String employeeId = oConvertUtils.getString(req.getParameter("employeeId"));
            StringBuffer sb = new StringBuffer();
            // 销售订单新增discountRateSum字段
            int exit = commonDao.getDataToInt(" select count(1) from syscolumns where id = object_id('salesorder') and name='DiscountRateSum'  ");
            if (exit < 1) {
                commonDao.executeSql(" alter table salesOrder add DiscountRateSum money ");
            }
            sb.append(" select so.SalesOrderID, (select Department from Department  where so.DepartmentID = DepartmentID) Department , No, CONVERT(varchar(100), Date, 111) Date, "
                    + " isnull(QuantitySum,0) QuantitySum,AmountSum,AuditFlag,so.madebydate,(select Name from Employee where employeeId = so.EmployeeId) Employee,isnull(so.Memo,'') Memo, "
                    + " (select Customer from Customer where CustomerId = so.CustomerId) Customer,(select Brand from Brand where BrandId = so.BrandId) Brand,(select Department from Department "
                    + " where so.WarehouseId = DepartmentID) Warehouse from SalesOrder so where so.CustomerId in ( select CustomerId from Customer where DepartmentID in (" + userRight + ")) ");
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
                sb.append(" and so.customerId in ( select CustomerId from Customer where DepartmentID = '" + departmentId + "' )");
            }
            // 客户
            if (customerId != null && !"".equals(customerId.trim()) && !"null".equalsIgnoreCase(customerId)) {
                sb.append(" and so.customerId = '" + customerId + "' ");
            }
            // 经手人
            if (employeeId != null && !"".equals(employeeId.trim()) && !"null".equalsIgnoreCase(employeeId)) {
                sb.append(" and so.employeeId = '" + employeeId + "' ");
            }
            sb.append(" order by so.madebydate desc,No desc ");
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
     * 根据单据ID获取销售订单明细信息(子表信息)
     * 
     * @param req
     * @return
     */
    @RequestMapping(params = "salesOrderEdit")
    @ResponseBody
    public AjaxJson salesOrderEdit(HttpServletRequest req) {
        AjaxJson j = new AjaxJson();
        j.setAttributes(new HashMap<String, Object>());
        Client client = ResourceUtil.getClientFromSession(req);
        try {
            String SalesOrderID = oConvertUtils.getString(req.getParameter("SalesOrderID"));
            int boxQtySum = commonDao.getDataToInt("select sum(isnull(sodt.BoxQty,0)) BoxQty from SalesOrderDetailTemp sodt where SalesOrderID = ? ", SalesOrderID);
            int sizeStrCount = commonDao.getDataToInt("select count(1) from SalesOrderDetailTemp sodt where SalesOrderID = ? ", SalesOrderID);
            int notNullSizeStrCount = commonDao.getDataToInt("select count(1) from SalesOrderDetailTemp sodt where SalesOrderID = ? and sodt.SizeStr is not null and sodt.SizeStr <> '' ", SalesOrderID);
            StringBuffer sb = new StringBuffer();
            sb.append(" select so.CustomerID ,so.No,isnull(so.AuditFlag,0)  AuditFlag,")
                    .append(boxQtySum)
                    .append(" BoxQtySum, isnull(d1.Customer,'') Customer,(isnull(so.DiscountRateSum,10)/10) DiscountRateSum, isnull(so.LastARAmount,0) LastARAmount,isnull(so.PrivilegeAmount,0) PrivilegeAmount,isnull(so.PreReceivalAmount,0) PreReceivalAmount, ")
                    .append("  so.WarehouseId DepartmentID ,isnull(d2.Department,'') Department,isnull(so.Memo,'') Memo,isnull(so.QuantitySum,0) QuantitySum, Type,so.EmployeeID,(select Name from Employee where EmployeeID = so.EmployeeID) Employee, isnull((select Department from Department where DepartmentID = BusinessDeptID),'') BusinessDeptName,"
                            + "so.BrandID,(select Brand from Brand where brandId = so.BrandId) Brand,so.PaymentTypeID,(select PaymentType from PaymentType where PaymentTypeID = so.PaymentTypeID) PaymentType  from SalesOrder so  ").append(" left join Customer d1 on d1.CustomerID = so.CustomerID ")
                    .append(" left join Department d2 on d2.DepartmentID = so.WarehouseID ").append(" where SalesOrderID = '").append(SalesOrderID).append("'");
            List list = commonDao.findForJdbc(sb.toString());
            if (list.size() > 0) {
                Map map = (Map) list.get(0);
                j.setAttributes(map);
                sb = new StringBuffer();
                List detailList = null;
                if (sizeStrCount == notNullSizeStrCount) {
                    sb.append(" select a.*,g.code GoodsCode ,g.name GoodsName,c.No ColorCode,isnull(pdt.DiscountRate,10) DiscountRate,isnull(pdt.BoxQty,0) BoxQty,pdt.Quantity QuantitySum,a.SizeStr,isnull(pdt.DiscountPrice,isnull(pdt.UnitPrice,0)) DiscountPrice,isnull(pdt.UnitPrice,0) UnitPrice, ")
                            .append(" c.Color ,sg.SizeGroupID,isnull(pdt.RetailSales,0) RetailSales,isnull(pdt.memo,'') meno from GoodsBoxBarcode a join goods g on a.goodsid=g.goodsid ").append(" join color c on a.colorid=c.colorid ").append(" join GoodsType gt on gt.GoodsTypeID = g.GoodsTypeID ")
                            .append(" join SizeGroup sg on sg.SizeGroupID =gt.SizeGroupID ").append(" join SalesOrderDetailtemp pdt on pdt.goodsId = a.goodsId and pdt.colorId = a.colorid and pdt.sizeStr = a.sizeStr ").append("  where pdt.SalesOrderID = '").append(SalesOrderID)
                            .append("' order by pdt.GoodsID,pdt.ColorID ");
                    detailList = controller.getDetailTemp(commonDao.findForJdbc(sb.toString()), client, commonDao);
                } else {
                    sb.append(" select detail.GoodsID,g.Name GoodsName,isnull(sodt.DiscountRate,10) DiscountRate,c.No ColorCode,s.No SizeCode,detail.ColorID,c.Color, ")
                            .append(" detail.SizeID,s.Size,detail.Quantity,sodt.Quantity QuantitySum,'' Barcode,'' SizeGroupID,g.Code GoodsCode,ss.No IndexNo,(case when isnull(sodt.DiscountPrice,0)=0 then isnull(sodt.UnitPrice,0) else isnull(sodt.DiscountPrice,0) end) DiscountPrice,isnull(sodt.UnitPrice,0) UnitPrice ")
                            .append(" ,isnull(sodt.BoxQty,0) BoxQty,isnull(sodt.Quantity/nullif(sodt.BoxQty,0),0) OneBoxQty,isnull(sodt.RetailSales,0) RetailSales,sodt.SizeStr,isnull(sodt.memo,'') meno from SalesOrderDetail detail ").append(" left join Goods g on g.GoodsID = detail.GoodsID ")
                            .append(" left join Color c on c.ColorID = detail.ColorID ").append(" left join Size s on s.SizeID = detail.SizeID ")
                            .append(" left join SalesOrderDetailtemp sodt on sodt.SalesOrderID = detail.SalesOrderID and sodt.GoodsID = detail.GoodsID and sodt.ColorID = detail.ColorID ").append(" left join GoodsType gt on gt.GoodsTypeID = g.GoodsTypeID ")
                            .append(" left join SizeGroup sg on sg.SizeGroupID = gt.SizeGroupID ").append(" left join SizeGroupSize ss on ss.SizeGroupID = sg.SizeGroupID and ss.SizeID = detail.SizeID ").append("  where detail.SalesOrderID = '").append(SalesOrderID)
                            .append("' order by detail.GoodsID,detail.ColorID,detail.SizeID,sodt.Quantity ");
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
     * 保存销售订单[新增,修改]
     * 
     * @param req
     * @return
     */
    @RequestMapping(params = "saveSalesOrder")
    @ResponseBody
    public AjaxJson saveSalesOrder(HttpServletRequest req) {
        Client client = ResourceUtil.getClientFromSession(req);
        AjaxJson j = new AjaxJson();
        j.setAttributes(new HashMap<String, Object>());
        try {
            String salesOrderID = oConvertUtils.getString(req.getParameter("SalesOrderID"));
            String type = oConvertUtils.getString(req.getParameter("type"));
            String discountRateSum = oConvertUtils.getString(req.getParameter("discountRateSum"));
            String lastARAmount = oConvertUtils.getString(req.getParameter("lastARAmount"));
            String preReceivalAmount = oConvertUtils.getString(req.getParameter("preReceivalAmount"));
            String privilegeAmount = oConvertUtils.getString(req.getParameter("privilegeAmount"));
            String customerid = oConvertUtils.getString(req.getParameter("customerid"));
            String departmentid = oConvertUtils.getString(req.getParameter("departmentid"));
            String employeeId = oConvertUtils.getString(req.getParameter("employeeId"));
            String brandId = oConvertUtils.getString(req.getParameter("brandId"));
            String businessDeptId = oConvertUtils.getString(req.getParameter("businessDeptId"));
            String paymentTypeId = oConvertUtils.getString(req.getParameter("paymentTypeId"));
            String memo = oConvertUtils.getString(req.getParameter("memo"));
            String typeEName = oConvertUtils.getString(req.getParameter("typeEName"));
            String jsonStr = oConvertUtils.getString(req.getParameter("data"));
            JSONArray datas = JSONArray.fromObject(jsonStr);
            List<Map<String, Object>> dataList = JSONArray.toList(datas, Map.class);
            String salesOrderId = salesOrderService.saveSalesOrder(dataList, salesOrderID, customerid, departmentid, employeeId, businessDeptId, memo, type, typeEName, brandId, discountRateSum, lastARAmount, preReceivalAmount, privilegeAmount, paymentTypeId, client);
            j.getAttributes().put("SalesOrderID", salesOrderId);
        } catch (Exception e) {
            j.setSuccess(false);
            j.setMsg(e.getMessage());
            SysLogger.error(e.getMessage(), e);
        }
        return j;
    }

    /**
     * 审核销售订单
     * 
     * @param user
     * @param req
     * @return
     */
    @RequestMapping(params = "auditOrder")
    @ResponseBody
    public AjaxJson auditOrder(HttpServletRequest req) {
        AjaxJson j = new AjaxJson();
        j.setAttributes(new HashMap<String, Object>());
        try {
            String SalesOrderID = oConvertUtils.getString(req.getParameter("SalesOrderID"));
            Period p = commonDao.getPeriod(DataUtils.formatDate(new Date()));
            StringBuilder sb = new StringBuilder();
            sb.append(" Update SalesOrder set AuditFlag = 1, AuditDate = getdate(), Year = '").append(p.getPeriodYeay()).append("' , Month = '").append(p.getPeriodMonth()).append("' ").append(" where SalesOrderID = '").append(SalesOrderID).append("' ");
            commonDao.executeSql(sb.toString());
        } catch (Exception e) {
            j.setSuccess(false);
            j.setMsg(e.getMessage());
            SysLogger.error(e.getMessage(), e);
        }
        return j;
    }

    /**
     * 销售订单条码校验后,以校验结果覆盖原始单据[新增,修改,删除]
     * 
     * @param req
     * @return
     */
    @RequestMapping(params = "coverSave")
    @ResponseBody
    public AjaxJson coverSave(HttpServletRequest req) {
        Client client = ResourceUtil.getClientFromSession(req);
        AjaxJson j = new AjaxJson();
        j.setAttributes(new HashMap<String, Object>());
        try {
            String salesOrderID = oConvertUtils.getString(req.getParameter("SalesOrderID"));
            String jsonStr = oConvertUtils.getString(req.getParameter("data"));
            JSONArray datas = JSONArray.fromObject(jsonStr);
            List<Map<String, Object>> dataList = JSONArray.toList(datas, Map.class);
            int count = salesOrderService.coverSave(salesOrderID, dataList, client);
            j.setObj(count);
        } catch (Exception e) {
            j.setSuccess(false);
            j.setMsg(e.getMessage());
            SysLogger.error(e.getMessage(), e);
        }
        return j;
    }

    /**
     * 修改单据备注信息
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
            String SalesOrderID = oConvertUtils.getString(req.getParameter("SalesOrderID"));
            String memo = oConvertUtils.getString(req.getParameter("memo"));
            StringBuilder sb = new StringBuilder();
            sb.append(" update salesorder set memo = ? where salesorderId = ? ");
            commonDao.executeSql(sb.toString(), memo, SalesOrderID);
        } catch (Exception e) {
            j.setSuccess(false);
            j.setMsg(e.getMessage());
            SysLogger.error(e.getMessage(), e);
        }
        return j;
    }

    /**
     * 删除销售订单单据的货品记录(单据子记录信息)
     * 
     * @param user
     * @param req
     * @return
     */
    @RequestMapping(params = "deleteItem")
    @ResponseBody
    public AjaxJson deleteItem(HttpServletRequest req) {
        AjaxJson j = new AjaxJson();
        j.setAttributes(new HashMap<String, Object>());
        try {
            String salesOrderID = oConvertUtils.getString(req.getParameter("SalesOrderID"));
            String jsonStr = oConvertUtils.getString(req.getParameter("data"));
            JSONArray datas = JSONArray.fromObject(jsonStr);
            List<Map<String, Object>> dataList = JSONArray.toList(datas, Map.class);
            salesOrderService.deleteSalesdetail(dataList, salesOrderID);
        } catch (Exception e) {
            j.setSuccess(false);
            j.setMsg(e.getMessage());
            SysLogger.error(e.getMessage(), e);
        }
        return j;
    }

}
