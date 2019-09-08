package com.fuxi.web.controller;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

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
import com.fuxi.system.util.MyTools;
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
    
    
    /*
     * 采购收货单主表信息，新写 ，退货也应该在这里处理了 这个退货的为变成正数
     * */
    @RequestMapping(params = "purchaselist")
    @ResponseBody  
    public AjaxJson purchaselist(HttpServletRequest req){
    	 Client client = ResourceUtil.getClientFromSession(req);
         AjaxJson j = new AjaxJson();
         j.setAttributes(new HashMap<String, Object>());
         Map<String, String> map = client.getMap();
         String userRight = map.get(client.getUserID());
         try{
        	  int page = oConvertUtils.getInt(req.getParameter("currPage"));
              String audit = oConvertUtils.getString(req.getParameter("audit"));
              String no = oConvertUtils.getString(req.getParameter("no"));
              String beginDate = oConvertUtils.getString(req.getParameter("beginDate"));
              String endDate = oConvertUtils.getString(req.getParameter("endDate"));
              String departmentId = oConvertUtils.getString(req.getParameter("departmentId"));
              String supplierId = oConvertUtils.getString(req.getParameter("supplierId"));
              String employeeId = oConvertUtils.getString(req.getParameter("employeeId"));
              
              int direction =Integer.parseInt(oConvertUtils.getString(req.getParameter("direction")));//代表收，退
              
              StringBuffer sb = new StringBuffer();
              sb.append(" select so.PurchaseID,so.SupplierID,so.TallyFlag,so.DepartmentID, de.Department ,so.Type, No, CONVERT(varchar(100), Date, 111) Date,isnull(QuantitySum*so.direction,0) QuantitySum,").append("(AmountSum*so.direction) AmountSum,AuditFlag,so.MadeBy,so.madebydate,isnull((select Supplier from Supplier s where so.SupplierId = s.SupplierId),'') Supplier,")
                      .append("(select Name from Employee where employeeId = so.EmployeeId) Employee,isnull(so.Memo,'') Memo,").append("(select Brand from Brand where BrandId = so.BrandId) Brand from Purchase so  ")
                      .append(" left join Department de on de.DepartmentID = so.DepartmentID where so.DepartmentID in (").append(userRight).append(") and direction ="+direction+"  ");
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
                 // sb.append(" and Date between convert(datetime,'" + beginDate + "', 120) and convert(datetime,'" + endDate + "', 120) ");
            	  sb.append(" and so.Date >= '" + beginDate + "' and so.Date <='" + endDate + " 23:59:59.997'");
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
             
              List<Map<String,Object>> list = commonDao.findForJdbc(sb.toString(), page, 15);
              
              for(int i=0;i<list.size();i++){
            	  Map<String,Object> m=list.get(i);
            	  if(!"".equals(String.valueOf(m.get("AmountSum"))) && m.get("AmountSum") !=null){
            		  m.put("AmountSum", new BigDecimal(String.valueOf(m.get("AmountSum"))).setScale(2,BigDecimal.ROUND_DOWN)); 
            	  }else{
            		  m.put("AmountSum", ""); 
            	  }
            	  List<Map<String,Object>> right=new ArrayList<>();
            	  
            	  for(int n=0;n<3;n++){
            	  Map<String,Object> rmap=new LinkedHashMap<>();
            	  Map<String,Object> stylemap=new LinkedHashMap<>();
            	  if(n==0){
                	  rmap.put("text", "删除");
                	  stylemap.put("backgroundColor", "orange");
                	  stylemap.put("color", "white");
                	  rmap.put("style", stylemap);
                 }else if(n==1){
            	  rmap.put("text", "审核");
            	  stylemap.put("backgroundColor", "mediumspringgreen");
            	  stylemap.put("color", "white");
            	  rmap.put("style", stylemap);
            	  
            	  }else if(n==2){
            	  rmap.put("text", "反审");
            	  stylemap.put("backgroundColor", "#F4333C");
            	  stylemap.put("color", "white");
            	  rmap.put("style", stylemap);
            	  }
            	  right.add(rmap);
              }
            	  m.put("right", right);
              }
              
             System.out.print("list:"+list.toString());
              if(list.size()>0){
              j.setObj(list);
              }else{
              j.setMsg("暂无数据");	  
              }
        	 
         }catch(Exception e){
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
            int AuditFlag= Integer.parseInt(oConvertUtils.getString(req.getParameter("AuditFlag")));
            if ("-1".equals(direction)) {
                // 调用存储过程生成进仓单
                commonDao.getStock(95, 1, PurchaseID, departmentid, client.getUserName());
            } else if ("1".equals(direction)) {
                // 调用存储过程生成出仓单
                commonDao.getStock(22, 1, PurchaseID, departmentid, client.getUserName());
            }
            // 更新主表, Year = '   .append(DataUtils.getYear()).append("' , Month = '").append(DataUtils.getStringMonth()).append("' ")
            
            StringBuilder sb = new StringBuilder();
            if(AuditFlag==1){
            sb.append(" Update Purchase set AuditFlag = 1, AuditDate = getdate() ").append(" where PurchaseID = '").append(PurchaseID).append("' ; ");
            }else{
            sb.append("Update Purchase Set Audit=Null,AuditFlag=0,AuditDate=Null Where PurchaseID='"+PurchaseID+"'");	
            }
            commonDao.executeSql(sb.toString());
            j.setSuccess(true);
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
    @RequestMapping(params = "savePurchaseX")
    @ResponseBody
    public AjaxJson savePurchaseX(HttpServletRequest req){
    	 Client client = ResourceUtil.getClientFromSession(req);
         AjaxJson j = new AjaxJson();
         j.setAttributes(new HashMap<String, Object>());
         try{
        String PurchaseId=null;	 
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
        List<List<Map<String, Object>>> sizeDatalist =new ArrayList<>(); //sizeData 所有元素的所有货品的，不是单个的，
        
        for(int i=0;i<datas.size(); i++){
            System.out.println("datas 没有删除键前 i串："+datas.get(i));
            JSONObject json =datas.getJSONObject(i); //单个对象
            JSONArray sizeData =json.getJSONArray("sizeData");
            
            List<Map<String, Object>> ls=JSONArray.toList(sizeData, Map.class);
            sizeDatalist.add(ls);
            json.remove("sizetitle");
            json.remove("right");
            json.remove("sizeData");
            System.out.println("json第一项："+datas.get(i));
          System.out.println("datas 删除键后 i串："+datas.get(i));
          //dataList.add(datas.get(i));	
          }  
        List<Map<String, Object>> dataList = JSONArray.toList(datas, Map.class); //上面删除后，才能转  收货部门，默认取用户登录的所属部门
        
        for(int k=0;k<dataList.size();k++){                       //因不能一次转 所以要重新整理后台数据 
        	Map<String, Object> map=dataList.get(k);
        	 for(int m=0;m<sizeDatalist.size();m++){ //不知道 顺序是否对，不对再判断 
        		if(String.valueOf(map.get("GoodsID")).equals(String.valueOf(sizeDatalist.get(m).get(0).get("GoodsID"))) && String.valueOf(map.get("ColorID")).equals(String.valueOf(sizeDatalist.get(m).get(0).get("ColorID")))){   //拿一个出来就可以
        		 map.put("sizeData",sizeDatalist.get(m));
        		}
        	 } 	
        }
         
        System.out.println("最终的dataList:"+dataList.toString());
        
        
        
        List<Map<String, Object>> tmpList = new ArrayList<Map<String, Object>>();
        List<Map<String, Object>> tempList = new ArrayList<Map<String, Object>>();
        
        List<Map<String, Object>> tdatas = new ArrayList<Map<String, Object>>();
        List<String> supplierIds = new ArrayList<String>();
        
        
        tdatas.addAll(dataList);
        if("".equals(supplierid) || supplierid==null){//如果为空，从货品明细取的，不是表头
        	
        	
     
        	
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
                if (tmpList.size() == 0) { //新的保存方法
                    // 保存单据
                	if(departmentid ==null || "".equals(departmentid)){
                		//为了相同厂商只生成一张单，所以部门只能取一个   前端部门不能为空，必须有
                		departmentid = (String)tempList.get(0).get("DepartmentID");
                	}
                    PurchaseId = purchaseService.savePurchaseX(direction, tempList, PurchaseID, supplierIds.get(i), departmentid, employeeId, businessDeptId, memo, type, typeEName, brandId, client);
                    tempList.clear();
                }
                
            }
        	
        	
        }else {  
            // 采购退货单检查负库存 修改单据
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
                PurchaseId = purchaseService.savePurchaseX(direction, dataList, PurchaseID, supplierid, departmentid, employeeId, businessDeptId, memo, type, typeEName, brandId, client);
            }
        }
        j.getAttributes().put("PurchaseID", PurchaseId);
        j.getAttributes().put("tempList", tmpList);
        j.setSuccess(true);
        j.setMsg("保存成功");
        
         }catch(Exception e){
        	 
        	 j.setSuccess(false);
        	 j.setMsg(e.getMessage());
        	 SysLogger.error(e.getMessage(), e);
        	 
         }
         
         return j;
    }
    
    
    
    /*
  * 根据 purchaseid 显示 单据详情，显示横向尺码
  * 
  * */
 @RequestMapping(params = "purchaseEditX")
 @ResponseBody
 
 public AjaxJson salesEditX(HttpServletRequest req){
     AjaxJson j =new AjaxJson();
     j.setAttributes(new HashMap<String, Object>());
     Client client = ResourceUtil.getClientFromSession(req);
     List<Map<String, Object>> list =new ArrayList<>();
 	try{
 		String PurchaseID = oConvertUtils.getString(req.getParameter("PurchaseID"));
 		int direction=Integer.parseInt(oConvertUtils.getString(req.getParameter("direction"))); //退货时，要按-1让显示 变成正数
 		
 		System.out.print("direction:"+direction);
 		
 		StringBuffer sb = new StringBuffer();
 	  sb.append("Select a.*,b.Code,b.SupplierCode, b.Name,b.Model,b.Unit,c.Color,b.GroupID,b.GroupNo,d.No as SalesOrderNo,"+
 		"b.StopFlag,b.age,b.Season,br.brand,b.RetailSales1,b.RetailSales2,b.PurchasePrice,bs.Serial,st.Storage "+
 		"from PurchaseDetailTemp a join Goods b on a.GoodsID = b.GoodsID "+
 		"left join PurchaseOrder d on a.PurchaseOrderID = d.PurchaseOrderID "+
 		"join Color c on a.ColorID=c.ColorID left join brand br on b.brandid = br.brandid "+
 		"left join BrandSerial bs on b.BrandSerialID = bs.BrandSerialID "+
 		"left join storage st on a.StorageID = st.StorageID "+
 		"where a.PurchaseID= '"+PurchaseID+"'" + 
 		"order by a.PurchaseID, a.IndexNo, b.Code,c.No");	
 	 //子表的所有数据
		List<Map<String, Object>> datalist= commonDao.findForJdbc(sb.toString());
 	  if(datalist.size() >0){
 		 for(int i=0 ;i<datalist.size();i++){ //提取数据
 			Map<String,Object> datamap=new LinkedHashMap<>(); 
 			Map<String,Object> map = datalist.get(i); //每一条
 			String sql="select * from SizeGroupSize where SizeGroupID='"+String.valueOf(map.get("GroupID"))+"'";
 			List<Map<String, Object>> sizelist=commonDao.findForJdbc(sql); //查询
 			List<Map<String, Object>> sizetitle =new ArrayList<>();
 			List<Map<String, Object>> sizeData =new ArrayList<>();
 			 for(int k=0;k<sizelist.size() ;k++){ //sizetitle 查询所嘱的尺码
 				 Map<String,Object> smap=sizelist.get(k);
 				 Map<String,Object> newmap=new LinkedHashMap<>();
 				 newmap.put("field", "x_"+String.valueOf(smap.get("No")));
 				 newmap.put("title", String.valueOf(smap.get("Size")));
 				 sizetitle.add(newmap);
 				 //--------------数据-------------------
 				 Map<String,Object> sdata=new LinkedHashMap<>();
 				 sdata.put("PurchaseDetailID", String.valueOf(map.get("PurchaseDetailID")));
 				 sdata.put("PurchaseID", String.valueOf(map.get("PurchaseID")));
 				 sdata.put("GoodsID",String.valueOf(map.get("GoodsID")));
 				 sdata.put("ColorID",String.valueOf(map.get("ColorID")));
 				 sdata.put("x","x_"+String.valueOf(smap.get("No")));
 				 sdata.put("Size",String.valueOf(smap.get("Size")));
 				 sdata.put("Color",String.valueOf(map.get("Color")));
 				 sdata.put("SizeID",String.valueOf(smap.get("SizeID")));
 				 System.out.println("尺码显示列："+"x_"+String.valueOf(smap.get("No")));
 				 //有箱数的情况
 				 if(!"0".equals(String.valueOf(map.get("BoxQty"))) && map.get("BoxQty") !=null){
 					 if(String.valueOf(map.get("x_"+String.valueOf(smap.get("No")))) !=null && map.get("x_"+String.valueOf(smap.get("No"))) !=null){
 						 
 					// BigDecimal Quantity = new BigDecimal(Double.valueOf(String.valueOf(map.get("x_"+String.valueOf(smap.get("No"))))))
 					//.multiply(new BigDecimal(String.valueOf(map.get("BoxQty")))).setScale(2,BigDecimal.ROUND_DOWN);
 					if(!"0".equals(String.valueOf(map.get("BoxQty"))) && map.get("BoxQty") !=null){
 						sdata.put("BoxQty",String.valueOf(map.get("BoxQty")));
 					 } 
 				    if(map.get("x_"+String.valueOf(smap.get("No"))) !=null && !"null".equalsIgnoreCase(String.valueOf(map.get("x_"+String.valueOf(smap.get("No"))))) && !"".equals(String.valueOf(map.get("x_"+String.valueOf(smap.get("No"))))))	 
 					  {	 
 					  sdata.put("Quantity",Integer.parseInt(String.valueOf(map.get("x_"+String.valueOf(smap.get("No")))))*direction);
 					  }    					
 					 }else{
 					 sdata.put("Quantity",""); 
 					 }
 					 
 				 }else{
 					 if(!"0".equals(String.valueOf(map.get("x_"+String.valueOf(smap.get("No"))))) && map.get("x_"+String.valueOf(smap.get("No"))) !=null && !"".equals(String.valueOf(map.get("x_"+String.valueOf(smap.get("No"))))))
 				     {
 						 sdata.put("Quantity",Integer.parseInt(String.valueOf(map.get("x_"+String.valueOf(smap.get("No")))))*direction);
 				     }else {
 				    	 sdata.put("Quantity",""); 
 				     }
 				 }
 				 System.out.println("get尺码数量："+String.valueOf(sdata.get("Quantity")));
 				 
 				 if(!"".equals(String.valueOf(map.get("UnitPrice"))) && map.get("UnitPrice") !=null)//单价
 				 {
 					 sdata.put("UnitPrice", new BigDecimal(Double.valueOf(String.valueOf(map.get("UnitPrice")))).setScale(2,BigDecimal.ROUND_DOWN));
 				 }else{
 					 sdata.put("UnitPrice","");
 				 }
 				 
 				  String DiscountRate="10";
 				 if(!"".equals(String.valueOf(map.get("UnitPrice"))) && map.get("UnitPrice") !=null && sdata.get("Quantity") !=null && String.valueOf(sdata.get("Quantity"))!=null && !"".equals(String.valueOf(sdata.get("Quantity")))){
 				 
 					 if(map.get("DiscountRate") !=null && !"".equals(String.valueOf(map.get("DiscountRate")))){
 						 DiscountRate =String.valueOf(map.get("DiscountRate"));
 				     }	 
 				System.out.println("DiscountRate:"+DiscountRate);	 
 				
 				//因为上面 数量 已经乘以 direction 了，后面两个都是 正数,所以金额这里不能再乘以 direction .multiply(new BigDecimal(direction))
 				
 				  sdata.put("Amount",new BigDecimal(Double.valueOf(String.valueOf(map.get("UnitPrice")))).multiply(new BigDecimal(DiscountRate)).divide(new BigDecimal(10.0))
 				 .multiply(new BigDecimal(Double.valueOf(String.valueOf(sdata.get("Quantity"))))).setScale(2,BigDecimal.ROUND_DOWN));
 				  
 				  System.out.println("单价："+new BigDecimal(Double.valueOf(String.valueOf(map.get("UnitPrice")))));
 				  System.out.println("尺码中的数量："+ String.valueOf(sdata.get("Quantity")));
 				 System.out.println("尺码中的折扣："+ new BigDecimal(DiscountRate));
 				
 				  System.out.println("尺码中的金额："+new BigDecimal(Double.valueOf(String.valueOf(map.get("UnitPrice")))).multiply(new BigDecimal(DiscountRate)).divide(new BigDecimal(10.0))
 		 				 .multiply(new BigDecimal(Double.valueOf(String.valueOf(sdata.get("Quantity"))))).setScale(2,BigDecimal.ROUND_DOWN));
 				 }else{
 					 sdata.put("Amount",""); 
 				 } //精确到尺码的金额，为置空
 				 
 				 sizeData.add(sdata);
 			 }
 			 datamap.put("PurchaseDetailID", String.valueOf(map.get("PurchaseDetailID")));
 			 datamap.put("PurchaseID", String.valueOf(map.get("PurchaseID")));
 			 datamap.put("GoodsID", String.valueOf(map.get("GoodsID")));
 			 datamap.put("Code", String.valueOf(map.get("Code")));
 			 datamap.put("Name", String.valueOf(map.get("Name")));
 			 datamap.put("ColorTitle", "颜色");
 			 datamap.put("ColorID", String.valueOf(map.get("ColorID")));
 			 datamap.put("Color", String.valueOf(map.get("Color")));
 			 
 			 
 			 //09.01 加载入货品图片
			 if(MyTools.isExists(String.valueOf(map.get("Code"))) !=null )
			 {
				 String path1 = req.getContextPath();//项目的名称 
		            String basePath = req.getScheme()+"://"+req.getServerName()+":"+req.getServerPort()+"/";
		            
		          String  url=basePath+"images/"+MyTools.isExists(String.valueOf(map.get("Code")));
		            
				 datamap.put("img", url);	   
			 }else{
				 datamap.put("img", ""); 
			 }
 			 
 			 
 			 if(!"".equals(String.valueOf(map.get("Discount"))) && map.get("Discount") !=null){
 			 datamap.put("Discount", new BigDecimal(String.valueOf(map.get("Discount"))).multiply(new BigDecimal(direction)).setScale(2,BigDecimal.ROUND_DOWN));
 			 }else{
 				 datamap.put("Discount","");	 
 			 }
 			 if(!"".equals(String.valueOf(map.get("DiscountRate"))) && map.get("DiscountRate") !=null){
 			 datamap.put("DiscountRate", new BigDecimal(String.valueOf(map.get("DiscountRate"))).setScale(2,BigDecimal.ROUND_DOWN));
 			 }else{
 			 datamap.put("DiscountRate","");
 			 }
 			 if(!"".equals(String.valueOf(map.get("Quantity"))) && map.get("Quantity") !=null){
 			 datamap.put("Quantity", Integer.valueOf(String.valueOf(map.get("Quantity"))).intValue()*direction); 
 			 }else{
 				 datamap.put("Quantity","");	 
 			 }
 			 
 			 if(!"".equals(String.valueOf(map.get("UnitPrice"))) && map.get("UnitPrice") !=null)//单价
				 {
 				 datamap.put("UnitPrice", new BigDecimal(Double.valueOf(String.valueOf(map.get("UnitPrice")))).setScale(2,BigDecimal.ROUND_DOWN));
				 }else{
					 datamap.put("UnitPrice","");
				 }
 			 
 			 
 			 if(!"".equals(String.valueOf(map.get("Amount"))) && map.get("Amount") !=null){
 			 datamap.put("Amount", new BigDecimal(String.valueOf(map.get("Amount"))).multiply(new BigDecimal(direction)).setScale(2,BigDecimal.ROUND_DOWN));
 			 }else{
 			 datamap.put("Amount", "");	 
 			 }
 			 
 			 datamap.put("sizetitle", sizetitle);
 			 datamap.put("sizeData", sizeData);
 			 
 			 List<Map<String,Object>> right =new ArrayList<>();
 			 Map<String,Object> m=new LinkedHashMap<>();	
 			/* for(int n=0;n<2 ;n++){
    			  Map<String,Object> m=new LinkedHashMap<>();	
    			  if(n==0){
    				 m.put("text", "审核");
    				 m.put("onPress", "function() {"+
                                       "      modal.toast({ "+
                                       "      message: '审核', "+
                                       "       duration: 0.3 "+
                                       "      }); "+
                                       " }"
                                       );
    			  }else if(n==1){
    				  m.put("text", "删除");
     				 m.put("onPress", "()=> {"+
                                        "      modal.toast({ "+
                                        "      message: '删除', "+
                                        "       duration: 0.3 "+
                                        "      }); "+
                                        " }"
                                        );  
     				 m.put("style", "{ backgroundColor: '#F4333C', color: 'white' }");
    				  
    			  }
    			  
    			  right.add(m);
    			 } */
    			m.put("text", "删除");
				 m.put("onPress", "()=> {"+
                               "      modal.toast({ "+
                               "      message: '删除', "+
                               "       duration: 0.3 "+
                               "      }); "+
                               " }"
                               );  
				 Map<String,Object> stylemap= new LinkedHashMap<>();
				 stylemap.put("backgroundColor", "#F4333C");
				 stylemap.put("color", "white");
				 m.put("style",stylemap );
				 right.add(m);
 			 datamap.put("right", right);
 			 list.add(datamap);
 		 }	  
 		 
 		 j.setMsg("成功返回数据");
 	  }else{
 		 j.setMsg("暂无数据"); 
 	  }	
 	  
          j.setSuccess(true);
          j.setObj(list);
 	  
 	}catch(Exception e){
 		  j.setSuccess(false);
           j.setMsg(e.getMessage());
           SysLogger.error(e.getMessage(), e);
 	}
 	return j;
 }

}
