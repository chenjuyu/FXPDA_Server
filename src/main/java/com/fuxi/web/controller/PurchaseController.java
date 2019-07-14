package com.fuxi.web.controller;

import java.util.ArrayList;
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
import com.fuxi.core.common.exception.BusinessException;
import com.fuxi.core.common.model.json.AjaxJson;
import com.fuxi.core.common.service.PurchaseService;
import com.fuxi.system.util.Client;
import com.fuxi.system.util.DataUtils;
import com.fuxi.system.util.ResourceUtil;
import com.fuxi.system.util.SysLogger;
import com.fuxi.system.util.oConvertUtils;

/**
 * Title: PurchaseController Description: 采购收货单,采购退货单逻辑控制器
 * 
 * @author LYJ
 * 
 */
@Controller
@RequestMapping("/purchase")
public class PurchaseController extends BaseController {

    private Logger log = Logger.getLogger(PurchaseController.class);
    private SelectController controller = new SelectController();
    private CommonController commonController = new CommonController();

    @Autowired
    private CommonDao commonDao;
    @Autowired
    private PurchaseService purchaseService;

    /**
     * 根据条件获取采购收货单(主表信息)
     * 
     * @param req
     * @return
     */
    @RequestMapping(params = "getPurchase")
    @ResponseBody
    public AjaxJson getPurchase(HttpServletRequest req) {
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
            String supplierId = oConvertUtils.getString(req.getParameter("customerId"));
            String employeeId = oConvertUtils.getString(req.getParameter("employeeId"));
            StringBuffer sb = new StringBuffer();
            sb.append(" select so.PurchaseID, de.Department , No, CONVERT(varchar(100), Date, 111) Date,isnull(QuantitySum,0) QuantitySum,").append("AmountSum,AuditFlag,so.madebydate,isnull((select Supplier from Supplier s where so.SupplierId = s.SupplierId),'') Supplier,")
                    .append("(select Name from Employee where employeeId = so.EmployeeId) Employee,isnull(so.Memo,'') Memo,").append("(select Brand from Brand where BrandId = so.BrandId) Brand from Purchase so  ")
                    .append(" left join Department de on de.DepartmentID = so.DepartmentID where so.DepartmentID in (").append(userRight).append(") and direction = '1'  ");
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
                sb.append(" and so.departmentId = '" + departmentId + "' ");
            }
            // 客户
            if (supplierId != null && !"".equals(supplierId.trim()) && !"null".equalsIgnoreCase(supplierId)) {
                sb.append(" and so.supplierId = '" + supplierId + "' ");
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
     * 根据条件获取采购退货单(主表信息)
     * 
     * @param req
     * @return
     */
    @RequestMapping(params = "getPurchaseReturns")
    @ResponseBody
    public AjaxJson getPurchaseReturns(HttpServletRequest req) {
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
            String supplierId = oConvertUtils.getString(req.getParameter("customerId"));
            String employeeId = oConvertUtils.getString(req.getParameter("employeeId"));
            StringBuffer sb = new StringBuffer();
            sb.append(" select so.PurchaseID, de.Department , No, CONVERT(varchar(100), Date, 111) Date,isnull(QuantitySum,0) QuantitySum,").append("AmountSum,AuditFlag,so.madebydate,isnull((select Supplier from Supplier s where so.SupplierId = s.SupplierId),'') Supplier,")
                    .append("(select Name from Employee where employeeId = so.EmployeeId) Employee,isnull(so.Memo,'') Memo").append(",(select Brand from Brand where BrandId = so.BrandId) Brand from Purchase so  ")
                    .append(" left join Department de on de.DepartmentID = so.DepartmentID where so.DepartmentID in (").append(userRight).append(") and direction = '-1'  ");
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
                sb.append(" and so.departmentId = '" + departmentId + "' ");
            }
            // 客户
            if (supplierId != null && !"".equals(supplierId.trim()) && !"null".equalsIgnoreCase(supplierId)) {
                sb.append(" and so.supplierId = '" + supplierId + "' ");
            }
            // 经手人
            if (employeeId != null && !"".equals(employeeId.trim()) && !"null".equalsIgnoreCase(employeeId)) {
                sb.append(" and so.employeeId = '" + employeeId + "' ");
            }
            sb.append(" order by so.date desc,No desc ");
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
     * 根据单据ID获取采购收(退)货单明细信息(子表信息)
     * 
     * @param req
     * @return
     */
    @RequestMapping(params = "purchaseEdit")
    @ResponseBody
    public AjaxJson purchaseEdit(HttpServletRequest req) {
        AjaxJson j = new AjaxJson();
        j.setAttributes(new HashMap<String, Object>());
        Client client = ResourceUtil.getClientFromSession(req);
        try {
            String PurchaseID = oConvertUtils.getString(req.getParameter("PurchaseID"));
            int boxQtySum = Integer.parseInt(String.valueOf(commonDao.getData("select isnull(sum(isnull(sodt.BoxQty,0)),0) BoxQty from PurchaseDetailTemp sodt where PurchaseID = ? ", PurchaseID)));
            int sizeStrCount = Integer.parseInt(String.valueOf(commonDao.getData("select count(1) from PurchaseDetailTemp sodt where PurchaseID = ? ", PurchaseID)));
            int notNullSizeStrCount = Integer.parseInt(String.valueOf(commonDao.getData("select count(1) from PurchaseDetailTemp sodt where PurchaseID = ? and sodt.SizeStr is not null and sodt.SizeStr <> '' ", PurchaseID)));
            StringBuffer sb = new StringBuffer();
            sb.append(" select so.SupplierID ,so.No,isnull(so.AuditFlag,0)  AuditFlag,").append(boxQtySum).append(" BoxQtySum, isnull(d1.Supplier,'') Supplier, so.BrandID,(select Brand from Brand where brandId = so.BrandId) Brand ,isnull((select Department from Department where DepartmentID = BusinessDeptID),'') BusinessDeptName, ")
                    .append("  so.DepartmentID DepartmentID ,isnull(d2.Department,'') Department,isnull(so.Memo,'') Memo,isnull(so.QuantitySum,0) QuantitySum, Type,so.EmployeeID,(select Name from Employee where EmployeeID = so.EmployeeID) Employee  from Purchase so  ")
                    .append(" left join Supplier d1 on d1.SupplierID = so.SupplierID ").append(" left join Department d2 on d2.DepartmentID = so.DepartmentID ").append(" where PurchaseID = '").append(PurchaseID).append("'");
            List list = commonDao.findForJdbc(sb.toString());
            if (list.size() > 0) {
                Map map = (Map) list.get(0);
                j.setAttributes(map);
                sb = new StringBuffer();
                List detailList = null;
                if (sizeStrCount == notNullSizeStrCount) {
                    // 更新箱条码配码
                    commonDao.validInvoiceSizeStr(22, PurchaseID);
                    sb.append(" select a.*,g.code GoodsCode ,g.name GoodsName,c.No ColorCode,isnull(pdt.BoxQty,0) BoxQty,pdt.Quantity QuantitySum,a.SizeStr,isnull(pdt.UnitPrice,0) DiscountPrice,isnull(pdt.UnitPrice,0) UnitPrice, ")
                            .append(" c.Color ,sg.SizeGroupID,isnull(pdt.RetailSales,0) RetailSales,isnull(pdt.memo,'') meno from GoodsBoxBarcode a join goods g on a.goodsid=g.goodsid ").append(" join color c on a.colorid=c.colorid ").append(" join GoodsType gt on gt.GoodsTypeID = g.GoodsTypeID ")
                            .append(" join SizeGroup sg on sg.SizeGroupID =gt.SizeGroupID ").append(" join purchasedetailtemp pdt on pdt.goodsId = a.goodsId and pdt.colorId = a.colorid and pdt.sizeStr = a.sizeStr ").append("  where pdt.PurchaseID = '").append(PurchaseID)
                            .append("' order by pdt.GoodsID ");
                    detailList = controller.getDetailTemp(commonDao.findForJdbc(sb.toString()), client, commonDao);
                } else if (notNullSizeStrCount != 0 && sizeStrCount > notNullSizeStrCount) {
                    // 更新箱条码配码
                    commonDao.validInvoiceSizeStr(22, PurchaseID);
                    // 装箱
                    sb.append(" select a.*,g.code GoodsCode ,g.name GoodsName,c.No ColorCode,isnull(a.BoxQty,0) BoxQty,a.Quantity QuantitySum,a.SizeStr,isnull(a.UnitPrice,0) DiscountPrice,isnull(a.UnitPrice,0) UnitPrice, ")
                            .append(" c.Color ,sg.SizeGroupID,isnull(a.RetailSales,0) RetailSales,isnull(a.memo,'') meno from purchasedetailtemp a join goods g on a.goodsid=g.goodsid ").append(" join color c on a.colorid=c.colorid ").append(" join GoodsType gt on gt.GoodsTypeID = g.GoodsTypeID ")
                            .append(" join SizeGroup sg on sg.SizeGroupID =gt.SizeGroupID ").append("  where a.PurchaseID = '").append(PurchaseID).append("' and a.SizeStr is not null and a.SizeStr <> '' order by a.GoodsID ");
                    detailList = controller.getDetailTemp(commonDao.findForJdbc(sb.toString()), client, commonDao);
                    // 重置SQL
                    sb = new StringBuffer();
                    // 散件
                    sb.append(" select detail.GoodsID,g.Name GoodsName,c.No ColorCode,s.No SizeCode,detail.ColorID,c.Color, ")
                            .append(" detail.SizeID,s.Size,detail.Quantity,sodt.Quantity QuantitySum,'' Barcode,'' SizeGroupID,g.Code GoodsCode,ss.No IndexNo,isnull(sodt.UnitPrice,0) DiscountPrice,isnull(sodt.UnitPrice,0) UnitPrice ")
                            .append(" ,isnull(sodt.BoxQty,0) BoxQty,isnull(sodt.Quantity/nullif(sodt.BoxQty,0),0) OneBoxQty,isnull(sodt.RetailSales,0) RetailSales,sodt.SizeStr,isnull(sodt.memo,'') meno from PurchaseDetail detail ").append(" left join Goods g on g.GoodsID = detail.GoodsID ")
                            .append(" left join Color c on c.ColorID = detail.ColorID ").append(" left join Size s on s.SizeID = detail.SizeID ").append(" left join PurchaseDetailtemp sodt on sodt.PurchaseID = detail.PurchaseID and sodt.GoodsID = detail.GoodsID and sodt.ColorID = detail.ColorID ")
                            .append(" left join GoodsType gt on gt.GoodsTypeID = g.GoodsTypeID ").append(" left join SizeGroup sg on sg.SizeGroupID = gt.SizeGroupID ").append(" left join SizeGroupSize ss on ss.SizeGroupID = sg.SizeGroupID and ss.SizeID = detail.SizeID ")
                            .append("  where detail.PurchaseID = '").append(PurchaseID).append("' and sodt.SizeStr is null order by detail.GoodsID ");
                    List tempList = commonDao.findForJdbc(sb.toString());
                    detailList.addAll(tempList);
                } else {
                    sb.append(" select detail.GoodsID,g.Name GoodsName,c.No ColorCode,s.No SizeCode,detail.ColorID,c.Color, ")
                            .append(" detail.SizeID,s.Size,detail.Quantity,sodt.Quantity QuantitySum,'' Barcode,'' SizeGroupID,g.Code GoodsCode,ss.No IndexNo,isnull(sodt.UnitPrice,0) DiscountPrice,isnull(sodt.UnitPrice,0) UnitPrice ")
                            .append(" ,isnull(sodt.BoxQty,0) BoxQty,isnull(sodt.Quantity/nullif(sodt.BoxQty,0),0) OneBoxQty,isnull(sodt.RetailSales,0) RetailSales,sodt.SizeStr,isnull(sodt.memo,'') meno from PurchaseDetail detail ").append(" left join Goods g on g.GoodsID = detail.GoodsID ")
                            .append(" left join Color c on c.ColorID = detail.ColorID ").append(" left join Size s on s.SizeID = detail.SizeID ").append(" left join PurchaseDetailtemp sodt on sodt.PurchaseID = detail.PurchaseID and sodt.GoodsID = detail.GoodsID and sodt.ColorID = detail.ColorID ")
                            .append(" left join GoodsType gt on gt.GoodsTypeID = g.GoodsTypeID ").append(" left join SizeGroup sg on sg.SizeGroupID = gt.SizeGroupID ").append(" left join SizeGroupSize ss on ss.SizeGroupID = sg.SizeGroupID and ss.SizeID = detail.SizeID ")
                            .append("  where detail.PurchaseID = '").append(PurchaseID).append("' order by detail.GoodsID ");
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
     * 保存采购收(退)货单[新增,修改]
     * 
     * @param req
     * @return
     */
    @RequestMapping(params = "savePurchase")
    @ResponseBody
    public AjaxJson savePurchase(HttpServletRequest req) {
        Client client = ResourceUtil.getClientFromSession(req);
        AjaxJson j = new AjaxJson();
        j.setAttributes(new HashMap<String, Object>());
        try {
            String PurchaseId = null;
            String PurchaseID = oConvertUtils.getString(req.getParameter("PurchaseID"));
            String supplierid = oConvertUtils.getString(req.getParameter("supplierid"));
            String departmentid = oConvertUtils.getString(req.getParameter("departmentid"));
            String employeeId = oConvertUtils.getString(req.getParameter("employeeId"));
            String brandId = oConvertUtils.getString(req.getParameter("brandId"));
            String businessDeptId = oConvertUtils.getString(req.getParameter("businessDeptId"));
            String memo = oConvertUtils.getString(req.getParameter("memo"));
            String type = oConvertUtils.getString(req.getParameter("type"));
            String direction = oConvertUtils.getString(req.getParameter("direction"));
            String typeEName = oConvertUtils.getString(req.getParameter("typeEName"));
            String jsonStr = oConvertUtils.getString(req.getParameter("data"));
            JSONArray datas = JSONArray.fromObject(jsonStr);
            List<Map<String, Object>> dataList = JSONArray.toList(datas, Map.class);
            List<Map<String, Object>> tmpList = new ArrayList<Map<String, Object>>();
            List<Map<String, Object>> tempList = new ArrayList<Map<String, Object>>();
            List<Map<String, Object>> tdatas = new ArrayList<Map<String, Object>>();
            List<String> supplierIds = new ArrayList<String>();
            tdatas.addAll(dataList);
            if (supplierid == null || "".equals(supplierid) || "null".equalsIgnoreCase(supplierid)) {
                // 去除重复的厂商信息
                for (int i = 0; i < tdatas.size() - 1; i++) {
                    Map temp1 = (Map) tdatas.get(i);
                    for (int a = tdatas.size() - 1; a > i; a--) {
                        Map temp2 = (Map) tdatas.get(a);
                        if (temp1.get("SupplierID").equals(temp2.get("SupplierID"))) {
                            tdatas.remove(a);
                        }
                    }
                }
                // 得到不同的厂商
                for (int i = 0; i < tdatas.size(); i++) {
                    Map<String, Object> map = tdatas.get(i);
                    supplierIds.add(String.valueOf(map.get("SupplierID")));
                }
                // 根据不同的厂商生成单据
                for (int i = 0; i < supplierIds.size(); i++) {
                    for (int k = 0; k < dataList.size(); k++) {
                        Map<String, Object> map = dataList.get(k);
                        String supplierId = String.valueOf(map.get("SupplierID"));
                        if (supplierId.equals(supplierIds.get(i))) {
                            tempList.add(map);
                        }
                    }
                    // 采购退货单检查负库存
                    if (Integer.parseInt(direction) == -1) {
                        boolean mustExistsGoodsFlag = Boolean.parseBoolean(String.valueOf(commonDao.getData(" select MustExistsGoodsFlag from Department where DepartmentID = ? ", departmentid)));
                        if (mustExistsGoodsFlag && !client.isSuperSalesFlag()) {
                            if (PurchaseID == null || "".equals(PurchaseID) || "null".equalsIgnoreCase(PurchaseID)) {
                                tmpList = commonController.checkNegativeInventoryForBackStage(commonDao, dataList, client.getOnLineId(), client.getUserID(), departmentid, 95, PurchaseID, 0, 2, 0, 0, 0, "");
                            } else {
                                tmpList = commonController.checkNegativeInventoryForBackStage(commonDao, dataList, client.getOnLineId(), client.getUserID(), departmentid, 95, PurchaseID, 0, 2, 1, 0, 0, "");
                            }
                        }
                    }
                    if (tmpList.size() == 0) {
                        // 保存单据
                        PurchaseId = purchaseService.savePurchase(direction, tempList, PurchaseID, supplierIds.get(i), departmentid, employeeId, businessDeptId, memo, type, typeEName, brandId, client);
                        tempList.clear();
                    }
                }
            } else {
                // 采购退货单检查负库存
                if (Integer.parseInt(direction) == -1) {
                    boolean mustExistsGoodsFlag = Boolean.parseBoolean(String.valueOf(commonDao.getData(" select MustExistsGoodsFlag from Department where DepartmentID = ? ", departmentid)));
                    if (mustExistsGoodsFlag && !client.isSuperSalesFlag()) {
                        if (PurchaseID == null || "".equals(PurchaseID) || "null".equalsIgnoreCase(PurchaseID)) {
                            tmpList = commonController.checkNegativeInventoryForBackStage(commonDao, dataList, client.getOnLineId(), client.getUserID(), departmentid, 95, PurchaseID, 0, 2, 0, 0, 0, "");
                        } else {
                            tmpList = commonController.checkNegativeInventoryForBackStage(commonDao, dataList, client.getOnLineId(), client.getUserID(), departmentid, 95, PurchaseID, 0, 2, 1, 0, 0, "");
                        }
                    }
                }
                if (tmpList.size() == 0) {
                    // 保存单据
                    PurchaseId = purchaseService.savePurchase(direction, dataList, PurchaseID, supplierid, departmentid, employeeId, businessDeptId, memo, type, typeEName, brandId, client);
                }
            }
            j.getAttributes().put("PurchaseID", PurchaseId);
            j.getAttributes().put("tempList", tmpList);
        } catch (Exception e) {
            j.setSuccess(false);
            j.setMsg(e.getMessage());
            SysLogger.error(e.getMessage(), e);
        }
        return j;
    }

    /**
     * 审核采购收(退)货单,审核后生成对应的进出仓单
     * 
     * @param req
     * @return
     */
    @RequestMapping(params = "auditOrder")
    @ResponseBody
    public AjaxJson auditOrder(HttpServletRequest req) {
        Client client = ResourceUtil.getClientFromSession(req);
        AjaxJson j = new AjaxJson();
        j.setAttributes(new HashMap<String, Object>());
        try {
            String direction = oConvertUtils.getString(req.getParameter("direction"));
            String PurchaseID = oConvertUtils.getString(req.getParameter("PurchaseID"));
            String departmentid = oConvertUtils.getString(req.getParameter("departmentid"));
            if ("-1".equals(direction)) {
                // 调用存储过程生成进仓单
                commonDao.getStock(95, 1, PurchaseID, departmentid, client.getUserName());
            } else if ("1".equals(direction)) {
                // 调用存储过程生成出仓单
                commonDao.getStock(22, 1, PurchaseID, departmentid, client.getUserName());
            }
            // 更新主表
            StringBuilder sb = new StringBuilder();
            sb.append(" Update Purchase set AuditFlag = 1, AuditDate = getdate(), Year = '").append(DataUtils.getYear()).append("' , Month = '").append(DataUtils.getStringMonth()).append("' ").append(" where PurchaseID = '").append(PurchaseID).append("' ; ");
            commonDao.executeSql(sb.toString());
        } catch (Exception e) {
            j.setSuccess(false);
            j.setMsg(e.getMessage());
            SysLogger.error(e.getMessage(), e);
        }
        return j;
    }

    /**
     * 采购收(退)货单条码校验后,以校验结果覆盖原始单据[新增,修改,删除]
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
            String PurchaseID = oConvertUtils.getString(req.getParameter("PurchaseID"));
            String jsonStr = oConvertUtils.getString(req.getParameter("data"));
            JSONArray datas = JSONArray.fromObject(jsonStr);
            List<Map<String, Object>> dataList = JSONArray.toList(datas, Map.class);
            int count = purchaseService.coverSave(PurchaseID, dataList, client);
            j.setObj(count);
        } catch (Exception e) {
            j.setSuccess(false);
            j.setMsg(e.getMessage());
            SysLogger.error(e.getMessage(), e);
        }
        return j;
    }

    /**
     * 检查货品资料的厂商是否为空(用于不选择厂商时新增采购收(退)货单前的检查判断)
     * 
     * @param req
     * @return
     */
    @RequestMapping(params = "checkGoodsHasSupplier")
    @ResponseBody
    public AjaxJson checkGoodsHasSupplier(HttpServletRequest req) {
        AjaxJson j = new AjaxJson();
        j.setAttributes(new HashMap<String, Object>());
        try {
            String goodsId = oConvertUtils.getString(req.getParameter("goodsId"));
            String supplierId = String.valueOf(commonDao.getData(" select supplierId from goods where goodsId = ? ", goodsId));
            if (null == supplierId || "".equals(supplierId) || "null".equalsIgnoreCase(supplierId)) {
                throw new BusinessException("货品厂商为空");
            }
            j.setObj(supplierId);
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
     * @param req
     * @return
     */
    @RequestMapping(params = "updateMemo")
    @ResponseBody
    public AjaxJson updateMemo(HttpServletRequest req) {
        AjaxJson j = new AjaxJson();
        j.setAttributes(new HashMap<String, Object>());
        try {
            String PurchaseID = oConvertUtils.getString(req.getParameter("PurchaseID"));
            String memo = oConvertUtils.getString(req.getParameter("memo"));
            StringBuilder sb = new StringBuilder();
            sb.append(" update Purchase set memo = ? where PurchaseID = ? ");
            commonDao.executeSql(sb.toString(), memo, PurchaseID);
        } catch (Exception e) {
            j.setSuccess(false);
            j.setMsg(e.getMessage());
            SysLogger.error(e.getMessage(), e);
        }
        return j;
    }

    /**
     * 删除采购收(退)货单单据的货品记录(单据子记录信息)
     * 
     * @param req
     * @return
     */
    @RequestMapping(params = "deleteItem")
    @ResponseBody
    public AjaxJson deleteItem(HttpServletRequest req) {
        AjaxJson j = new AjaxJson();
        j.setAttributes(new HashMap<String, Object>());
        try {
            String PurchaseID = oConvertUtils.getString(req.getParameter("PurchaseID"));
            String jsonStr = oConvertUtils.getString(req.getParameter("data"));
            JSONArray datas = JSONArray.fromObject(jsonStr);
            List<Map<String, Object>> dataList = JSONArray.toList(datas, Map.class);
            purchaseService.deletePurchasedetail(dataList, PurchaseID);
        } catch (Exception e) {
            j.setSuccess(false);
            j.setMsg(e.getMessage());
            SysLogger.error(e.getMessage(), e);
        }
        return j;
    }

}
