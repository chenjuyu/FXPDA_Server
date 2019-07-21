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
import com.fuxi.core.common.model.json.AjaxJson;
import com.fuxi.core.common.service.SalesService;
import com.fuxi.system.util.Client;
import com.fuxi.system.util.DataUtils;
import com.fuxi.system.util.ResourceUtil;
import com.fuxi.system.util.SysLogger;
import com.fuxi.system.util.oConvertUtils;

/**
 * Title: SalesController Description: 销售发货单,销售退货单逻辑控制器
 * 
 * @author LJ,LYJ
 * 
 */
@Controller
@RequestMapping("/sales")
public class SalesController extends BaseController {

    private Logger log = Logger.getLogger(SalesController.class);
    private SelectController controller = new SelectController();
    private CommonController commonController = new CommonController();

    @Autowired
    private CommonDao commonDao;
    @Autowired
    private SalesService publicService;

    /**
     * 根据条件获取销售发货单(主表信息)
     * 
     * @param req
     * @return
     */
    @RequestMapping(params = "sales")
    @ResponseBody
    public AjaxJson sales(HttpServletRequest req) {
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
            sb.append(
                    " select so.SalesID, de.Department , No,so.CustomerID,so.so.EmployeeID, CONVERT(varchar(100), Date, 111) Date,isnull(QuantitySum,0) QuantitySum," + "AmountSum,AuditFlag,so.madebydate,(select Name from Employee where employeeId = so.EmployeeId) Employee,"
                            + "isnull(so.Memo,'') Memo,(select Customer from Customer where CustomerId = so.CustomerId) Customer," + "(select Brand from Brand where BrandId = so.BrandId) Brand,(select no from salesOrder where salesorderId = so.salesorderId) OrderNo from Sales so  ")
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
    
    /*
     * 销售发货单 主表信息 新写
     * 
     * */
    @RequestMapping(params = "saleslist")
    @ResponseBody
    public AjaxJson saleslist(HttpServletRequest req) {
    	  Client client = ResourceUtil.getClientFromSession(req);
          AjaxJson j = new AjaxJson();
          j.setAttributes(new HashMap<String, Object>());
          Map<String, String> map = client.getMap();
          try{
          String userRight = map.get(client.getUserID());
          int page = oConvertUtils.getInt(req.getParameter("currPage"));
          String audit = oConvertUtils.getString(req.getParameter("audit"));
          String no = oConvertUtils.getString(req.getParameter("no"));
          String beginDate = oConvertUtils.getString(req.getParameter("beginDate"));
          String endDate = oConvertUtils.getString(req.getParameter("endDate"));
          String departmentId = oConvertUtils.getString(req.getParameter("departmentId"));
          String customerId = oConvertUtils.getString(req.getParameter("customerId"));
          String employeeId = oConvertUtils.getString(req.getParameter("employeeId"));
          StringBuffer sb = new StringBuffer();
          sb.append(
                  " select so.SalesID, de.Department,so.DepartmentID ,so.PaymentTypeID,so.ReceivalAmount, No,so.Type,so.CustomerID,so.EmployeeID,LastNeedRAmount='', CONVERT(varchar(10), Date, 121) Date,isnull(QuantitySum,0) QuantitySum," + "AmountSum,AuditFlag,Convert(varchar(10),so.AuditDate,121) AuditDate,Convert(varchar(19),so.madebydate,121) MadeByDate,(select Name from Employee where employeeId = so.EmployeeId) Name,"
                  +"(select PaymentType from PaymentType where PaymentTypeID=so.PaymentTypeID) PaymentType ,"        + "isnull(so.Memo,'') Memo,(select Customer from Customer where CustomerId = so.CustomerId) Customer," + "(select Brand from Brand where BrandId = so.BrandId) Brand,(select no from salesOrder where salesorderId = so.salesorderId) OrderNo from Sales so  ")
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
          if (customerId != null && !"".equals(customerId.trim()) && !"null".equalsIgnoreCase(customerId)) {
              sb.append(" and so.customerId = '" + customerId + "' ");
          }
          // 经手人
          if (employeeId != null && !"".equals(employeeId.trim()) && !"null".equalsIgnoreCase(employeeId)) {
              sb.append(" and so.employeeId = '" + employeeId + "' ");
          }
          sb.append(" order by so.madebydate desc,No desc ");
          List list = commonDao.findForJdbc(sb.toString(), page, 15);
          
          for(int i=0;i<list.size() ;i++){
        	 
        	  Map<String,Object> listmap=(Map<String, Object>) list.get(i);
        	  
        	  if(!"".equals(String.valueOf(listmap.get("AmountSum"))) && listmap.get("AmountSum") !=null){
        		  listmap.put("AmountSum", new BigDecimal(String.valueOf(listmap.get("AmountSum"))).setScale(2,BigDecimal.ROUND_DOWN)); 
        	  }else{
        		  listmap.put("AmountSum", ""); 
        	  }
        	  if(!"".equals(String.valueOf(listmap.get("ReceivalAmount"))) && listmap.get("ReceivalAmount") !=null){
        		  listmap.put("ReceivalAmount", new BigDecimal(String.valueOf(listmap.get("ReceivalAmount"))).setScale(2,BigDecimal.ROUND_DOWN)); 
        	  }else{
        		  listmap.put("ReceivalAmount", ""); 
        	  }
        	  
        	  
        	  List<Map<String,Object>> right=new ArrayList<>();
        	  
        	  for(int n=0;n<3;n++){
        	  Map<String,Object> rmap=new LinkedHashMap<>();
        	  Map<String,Object> stylemap=new LinkedHashMap<>();
        	  if(n==0){
        	  rmap.put("text", "审核");
        	  stylemap.put("backgroundColor", "mediumspringgreen");
        	  stylemap.put("color", "white");
        	  rmap.put("style", stylemap);
        	  
        	  }else if(n==1){
        	  rmap.put("text", "反审");
        	  stylemap.put("backgroundColor", "#F4333C");
        	  stylemap.put("color", "white");
        	  rmap.put("style", stylemap);
        	  }else if(n==2){
            	  rmap.put("text", "拍照");
            	  stylemap.put("backgroundColor", "orange");
            	  stylemap.put("color", "white");
            	  rmap.put("style", stylemap);
             }

        	  right.add(rmap);
        	}//for n 结束
        	  listmap.put("right", right);
          }
          
          
          
          
          j.setObj(list);
      } catch (Exception e) {
          j.setSuccess(false);
          j.setMsg(e.getMessage());
          SysLogger.error(e.getMessage(), e);
      }
      return j;
    	
    	
    }
    

    /**
     * 根据条件获取销售退货单(主表信息)
     * 
     * @param req
     * @return
     */
    @RequestMapping(params = "salesReturns")
    @ResponseBody
    public AjaxJson salesReturns(HttpServletRequest req) {
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
            sb.append(" select so.SalesID, de.Department , No, CONVERT(varchar(100), Date, 111) Date,isnull(QuantitySum,0) QuantitySum,").append("AmountSum,AuditFlag,so.madebydate,(select Name from Employee where employeeId = so.EmployeeId) Employee,")
                    .append("isnull(so.Memo,'') Memo,(select Customer from Customer where CustomerId = so.CustomerId) Customer,").append("(select Brand from Brand where BrandId = so.BrandId) Brand,(select Department from Department ")
                    .append(" where Department = so.warehouseId) Warehouse from Sales so  ").append(" left join Department de on de.DepartmentID = so.DepartmentID where so.DepartmentID in (").append(userRight).append(") and direction = '-1'  ");
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
            if (customerId != null && !"".equals(customerId.trim()) && !"null".equalsIgnoreCase(customerId)) {
                sb.append(" and so.customerId = '" + customerId + "' ");
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
     * 根据单据ID获取销售发(退)货单明细信息(子表信息)
     * 
     * @param req
     * @return
     */
    @RequestMapping(params = "salesEdit")
    @ResponseBody
    public AjaxJson SalesEdit(HttpServletRequest req) {
        AjaxJson j = new AjaxJson();
        j.setAttributes(new HashMap<String, Object>());
        Client client = ResourceUtil.getClientFromSession(req);
        try {
            String SalesID = oConvertUtils.getString(req.getParameter("SalesID"));
            int boxQtySum = Integer.parseInt(String.valueOf(commonDao.getData("select isnull(sum(isnull(sodt.BoxQty,0)),0) BoxQty from SalesDetailTemp sodt where SalesID = ? ", SalesID)));
            int sizeStrCount = Integer.parseInt(String.valueOf(commonDao.getData("select count(1) from SalesDetailTemp sodt where SalesID = ? ", SalesID)));
            int notNullSizeStrCount = Integer.parseInt(String.valueOf(commonDao.getData("select count(1) from SalesDetailTemp sodt where SalesID = ? and sodt.SizeStr is not null and sodt.SizeStr <> '' ", SalesID)));
            StringBuffer sb = new StringBuffer();
            sb.append(" select so.CustomerID ,so.No,isnull(so.AuditFlag,0)  AuditFlag,")
                    .append(boxQtySum)
                    .append(" BoxQtySum, isnull(d1.Customer,'') Customer,d2.MustExistsGoodsFlag,(isnull(so.DiscountRateSum,10)/10) DiscountRateSum,isnull(so.ReceivalAmount,0) LastARAmount,isnull(so.PrivilegeAmount,0) PrivilegeAmount,isnull(so.OrderAmount,0) OrderAmount,")
                    .append(" so.DepartmentID DepartmentID ,isnull(d2.Department,'') Department,isnull(so.Memo,'') Memo,isnull(so.QuantitySum,0) QuantitySum, Type,so.EmployeeID,(select Name from Employee where EmployeeID = so.EmployeeID) Employee,so.BrandID,isnull((select Department from Department where DepartmentID = BusinessDeptID),'') BusinessDeptName,"
                            + "(select Brand from Brand where brandId = so.BrandId) Brand,so.PaymentTypeID,(select PaymentType from PaymentType where PaymentTypeID = so.PaymentTypeID) PaymentType  from Sales so  ").append(" left join Customer d1 on d1.CustomerID = so.CustomerID ")
                    .append(" left join Department d2 on d2.DepartmentID = so.DepartmentID ").append(" where SalesID = '").append(SalesID).append("'");
            List list = commonDao.findForJdbc(sb.toString());
            if (list.size() > 0) {
                Map map = (Map) list.get(0);
                j.setAttributes(map);
                sb = new StringBuffer();
                List detailList = null;
                if (notNullSizeStrCount != 0 && sizeStrCount == notNullSizeStrCount) {
                    // 更新箱条码配码
                    commonDao.validInvoiceSizeStr(30, SalesID);
                    sb.append(" select a.*,g.code GoodsCode ,g.name GoodsName,c.No ColorCode,isnull(pdt.DiscountRate,10) DiscountRate,isnull(pdt.BoxQty,0) BoxQty,pdt.Quantity QuantitySum,a.SizeStr,isnull(pdt.DiscountPrice,isnull(pdt.UnitPrice,0)) DiscountPrice,isnull(pdt.UnitPrice,0) UnitPrice, ")
                            .append(" c.Color ,sg.SizeGroupID,isnull(pdt.RetailSales,0) RetailSales,isnull(pdt.memo,'') meno from GoodsBoxBarcode a join goods g on a.goodsid=g.goodsid ").append(" join color c on a.colorid=c.colorid ").append(" join GoodsType gt on gt.GoodsTypeID = g.GoodsTypeID ")
                            .append(" join SizeGroup sg on sg.SizeGroupID =gt.SizeGroupID ").append(" join SalesDetailtemp pdt on pdt.goodsId = a.goodsId and pdt.colorId = a.colorid and pdt.sizeStr = a.sizeStr ").append("  where pdt.SalesID = '").append(SalesID)
                            .append("' order by pdt.GoodsID,pdt.ColorID ");
                    detailList = controller.getDetailTemp(commonDao.findForJdbc(sb.toString()), client, commonDao);
                } else if (notNullSizeStrCount != 0 && sizeStrCount > notNullSizeStrCount) {
                    // 更新箱条码配码
                    commonDao.validInvoiceSizeStr(30, SalesID);
                    // 装箱
                    sb.append(" select a.*,g.code GoodsCode ,g.name GoodsName,c.No ColorCode,isnull(a.BoxQty,0) BoxQty,isnull(a.DiscountRate,10) DiscountRate,a.Quantity QuantitySum,a.SizeStr,isnull(a.DiscountPrice,isnull(a.UnitPrice,0)) DiscountPrice,isnull(a.UnitPrice,0) UnitPrice, ")
                            .append(" c.Color ,sg.SizeGroupID,isnull(a.RetailSales,0) RetailSales,isnull(a.memo,'') meno from SalesDetailtemp a join goods g on a.goodsid=g.goodsid ").append(" join color c on a.colorid=c.colorid ").append(" join GoodsType gt on gt.GoodsTypeID = g.GoodsTypeID ")
                            .append(" join SizeGroup sg on sg.SizeGroupID =gt.SizeGroupID ").append("  where a.SalesID = '").append(SalesID).append("' and a.SizeStr is not null and a.SizeStr <> '' order by a.GoodsID,a.ColorID ");
                    detailList = controller.getDetailTemp(commonDao.findForJdbc(sb.toString()), client, commonDao);
                    // 重置SQL
                    sb = new StringBuffer();
                    // 散件
                    sb.append(" select detail.GoodsID,g.Name GoodsName,c.No ColorCode,isnull(sodt.DiscountRate,10) DiscountRate,s.No SizeCode,detail.ColorID,c.Color, ")
                            .append(" detail.SizeID,s.Size,detail.Quantity,sodt.Quantity QuantitySum,'' Barcode,'' SizeGroupID,g.Code GoodsCode,ss.No IndexNo,(case when isnull(sodt.DiscountPrice,0)=0 then isnull(sodt.UnitPrice,0) else isnull(sodt.DiscountPrice,0) end) DiscountPrice,isnull(sodt.UnitPrice,0) UnitPrice ")
                            .append(" ,'0' BoxQty,'0' OneBoxQty,isnull(sodt.RetailSales,0) RetailSales,sodt.SizeStr,isnull(sodt.memo,'') meno from SalesDetail detail ").append(" join Goods g on g.GoodsID = detail.GoodsID ").append(" join Color c on c.ColorID = detail.ColorID ")
                            .append(" join Size s on s.SizeID = detail.SizeID ").append(" join SalesDetailtemp sodt on sodt.SalesID = detail.SalesID and sodt.GoodsID = detail.GoodsID and sodt.ColorID = detail.ColorID ").append(" join GoodsType gt on gt.GoodsTypeID = g.GoodsTypeID ")
                            .append(" join SizeGroup sg on sg.SizeGroupID = gt.SizeGroupID ").append(" join SizeGroupSize ss on ss.SizeGroupID = sg.SizeGroupID and ss.SizeID = detail.SizeID ").append("  where detail.SalesID = '").append(SalesID)
                            .append("' and sodt.SizeStr is null order by detail.GoodsID,detail.ColorID,detail.SizeID,sodt.Quantity ");
                    List tempList = commonDao.findForJdbc(sb.toString());
                    detailList.addAll(tempList);
                } else {
                    sb.append(" select detail.GoodsID,g.Name GoodsName,isnull(sodt.DiscountRate,10) DiscountRate,c.No ColorCode,s.No SizeCode,detail.ColorID,c.Color, ")
                            .append(" detail.SizeID,s.Size,detail.Quantity,sodt.Quantity QuantitySum,'' Barcode,'' SizeGroupID,g.Code GoodsCode,ss.No IndexNo,(case when isnull(sodt.DiscountPrice,0)=0 then isnull(sodt.UnitPrice,0) else isnull(sodt.DiscountPrice,0) end) DiscountPrice,isnull(sodt.UnitPrice,0) UnitPrice ")
                            .append(" ,isnull(sodt.BoxQty,0) BoxQty,isnull(sodt.Quantity/nullif(sodt.BoxQty,0),0) OneBoxQty,isnull(sodt.RetailSales,0) RetailSales,sodt.SizeStr,isnull(sodt.memo,'') meno from SalesDetail detail ").append(" join Goods g on g.GoodsID = detail.GoodsID ")
                            .append(" join Color c on c.ColorID = detail.ColorID ").append(" join Size s on s.SizeID = detail.SizeID ").append(" join SalesDetailtemp sodt on sodt.SalesID = detail.SalesID and sodt.GoodsID = detail.GoodsID and sodt.ColorID = detail.ColorID ")
                            .append(" join GoodsType gt on gt.GoodsTypeID = g.GoodsTypeID ").append(" join SizeGroup sg on sg.SizeGroupID = gt.SizeGroupID ").append(" join SizeGroupSize ss on ss.SizeGroupID = sg.SizeGroupID and ss.SizeID = detail.SizeID ").append("  where detail.SalesID = '")
                            .append(SalesID).append("' order by detail.GoodsID,detail.ColorID,detail.SizeID,sodt.Quantity ");
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

    
       /*
     * 根据 salesid 显示 单据详情，显示横向尺码
     * 
     * */
    @RequestMapping(params = "salesEditX")
    @ResponseBody
    
    public AjaxJson salesEditX(HttpServletRequest req){
        AjaxJson j =new AjaxJson();
        j.setAttributes(new HashMap<String, Object>());
        Client client = ResourceUtil.getClientFromSession(req);
        List<Map<String, Object>> list =new ArrayList<>();
    	try{
    		String SalesID = oConvertUtils.getString(req.getParameter("SalesID"));
    		StringBuffer sb = new StringBuffer();
    	  sb.append("Select a.*,b.Code,b.SupplierCode, b.Name,b.Model,b.Unit,c.Color,b.GroupID,b.GroupNo,d.No as SalesOrderNo,"+
    		"b.StopFlag,b.age,b.Season,br.brand,b.RetailSales1,b.RetailSales2,b.PurchasePrice,bs.Serial,st.Storage "+
    		"from SalesDetailTemp a join Goods b on a.GoodsID = b.GoodsID "+
    		"left join SalesOrder d on a.SalesOrderID = d.SalesOrderID "+
    		"join Color c on a.ColorID=c.ColorID left join brand br on b.brandid = br.brandid "+
    		"left join BrandSerial bs on b.BrandSerialID = bs.BrandSerialID "+
    		"left join storage st on a.StorageID = st.StorageID "+
    		"where a.SalesID= '"+SalesID+"'" + 
    		"order by a.SalesID, a.IndexNo, b.Code,c.No");	
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
    				 sdata.put("SalesDetailID", String.valueOf(map.get("SalesDetailID")));
    				 sdata.put("SalesID", String.valueOf(map.get("SalesID")));
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
    					  sdata.put("Quantity",Integer.parseInt(String.valueOf(map.get("x_"+String.valueOf(smap.get("No"))))));
    					  }    					
    					 }else{
    					 sdata.put("Quantity",""); 
    					 }
    					 
    				 }else{
    					 if(!"0".equals(String.valueOf(map.get("x_"+String.valueOf(smap.get("No"))))) && map.get("x_"+String.valueOf(smap.get("No"))) !=null && !"".equals(String.valueOf(map.get("x_"+String.valueOf(smap.get("No"))))))
    				     {
    						 sdata.put("Quantity",Integer.parseInt(String.valueOf(map.get("x_"+String.valueOf(smap.get("No"))))));
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
    				  sdata.put("Amount",new BigDecimal(Double.valueOf(String.valueOf(map.get("UnitPrice")))).multiply(new BigDecimal(DiscountRate)).divide(new BigDecimal(10.0))
    				 .multiply(new BigDecimal(Double.valueOf(String.valueOf(sdata.get("Quantity"))))).setScale(2,BigDecimal.ROUND_DOWN));
    				 }else{
    					 sdata.put("Amount",""); 
    				 } //精确到尺码的金额，为置空
    				 
    				 sizeData.add(sdata);
    			 }
    			 datamap.put("SalesDetailID", String.valueOf(map.get("SalesDetailID")));
    			 datamap.put("SalesID", String.valueOf(map.get("SalesID")));
    			 datamap.put("GoodsID", String.valueOf(map.get("GoodsID")));
    			 datamap.put("Code", String.valueOf(map.get("Code")));
    			 datamap.put("Name", String.valueOf(map.get("Name")));
    			 datamap.put("ColorTitle", "颜色");
    			 datamap.put("ColorID", String.valueOf(map.get("ColorID")));
    			 datamap.put("Color", String.valueOf(map.get("Color")));
    			 if(!"".equals(String.valueOf(map.get("Discount"))) && map.get("Discount") !=null){
    			 datamap.put("Discount", new BigDecimal(String.valueOf(map.get("Discount"))).setScale(2,BigDecimal.ROUND_DOWN));
    			 }else{
    				 datamap.put("Discount","");	 
    			 }
    			 if(!"".equals(String.valueOf(map.get("DiscountRate"))) && map.get("DiscountRate") !=null){
    			 datamap.put("DiscountRate", new BigDecimal(String.valueOf(map.get("DiscountRate"))).setScale(2,BigDecimal.ROUND_DOWN));
    			 }else{
    			 datamap.put("DiscountRate","");
    			 }
    			 if(!"".equals(String.valueOf(map.get("Quantity"))) && map.get("Quantity") !=null){
    			 datamap.put("Quantity", Integer.valueOf(String.valueOf(map.get("Quantity"))).intValue()); 
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
    			 datamap.put("Amount", new BigDecimal(String.valueOf(map.get("Amount"))).setScale(2,BigDecimal.ROUND_DOWN));
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
    
    
    
    /**
     * 保存销售发(退)货单[新增,修改]
     * 
     * @param req
     * @return
     */
    @RequestMapping(params = "saveSales")
    @ResponseBody
    public AjaxJson saveSales(HttpServletRequest req) {
        Client client = ResourceUtil.getClientFromSession(req);
        AjaxJson j = new AjaxJson();
        j.setAttributes(new HashMap<String, Object>());
        try {
            String SalesID = oConvertUtils.getString(req.getParameter("SalesID"));
            String customerid = oConvertUtils.getString(req.getParameter("customerid"));
            String discountRateSum = oConvertUtils.getString(req.getParameter("discountRateSum"));
            String lastARAmount = oConvertUtils.getString(req.getParameter("lastARAmount"));
            String orderAmount = oConvertUtils.getString(req.getParameter("orderAmount"));
            String privilegeAmount = oConvertUtils.getString(req.getParameter("privilegeAmount"));
            String departmentid = oConvertUtils.getString(req.getParameter("departmentid"));
            String employeeId = oConvertUtils.getString(req.getParameter("employeeId"));
            String brandId = oConvertUtils.getString(req.getParameter("brandId"));
            String businessDeptId = oConvertUtils.getString(req.getParameter("businessDeptId"));
            String paymentTypeId = oConvertUtils.getString(req.getParameter("paymentTypeId"));
            String memo = oConvertUtils.getString(req.getParameter("memo"));
            String type = oConvertUtils.getString(req.getParameter("type"));
            String direction = oConvertUtils.getString(req.getParameter("direction"));
            String typeEName = oConvertUtils.getString(req.getParameter("typeEName"));
            String notUseNegativeInventoryCheck = oConvertUtils.getString(req.getParameter("notUseNegativeInventoryCheck"));
            String jsonStr = oConvertUtils.getString(req.getParameter("data"));
            System.out.println("JSON串："+jsonStr);
            
            JSONArray datas = JSONArray.fromObject(jsonStr);
            List<Map<String, Object>> dataList =new ArrayList<>();
            
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
           
            dataList = JSONArray.toList(datas, Map.class); //删除掉其他list才能转的一下正常的  
             
            for(int k=0;k<dataList.size();k++){                       //因不能一次转 所以要重新整理后台数据 
            	Map<String, Object> map=dataList.get(k);
            	 for(int m=0;m<sizeDatalist.size();m++){ //不知道 顺序是否对，不对再判断 
            		if(String.valueOf(map.get("GoodsID")).equals(String.valueOf(sizeDatalist.get(m).get(0).get("GoodsID"))) && String.valueOf(map.get("ColorID")).equals(String.valueOf(sizeDatalist.get(m).get(0).get("ColorID")))){   //拿一个出来就可以
            		 map.put("sizeData",sizeDatalist.get(m));
            		}
            	 } 	
            }
           
           System.out.println("dataList最终的组成："+dataList.toString());
           
           System.out.println("SalesID:"+SalesID);
           
            //原来的
            //JSONArray datas = JSONArray.fromObject(jsonStr);
            //List<Map<String, Object>> dataList = JSONArray.toList(datas, Map.class);
            // 判断检查负库存
            String salesId = null;
            List<Map<String, Object>> tempList = new ArrayList<Map<String, Object>>();
            if (!"true".equalsIgnoreCase(notUseNegativeInventoryCheck)) {
                if (Integer.parseInt(direction) == 1) {
                    boolean mustExistsGoodsFlag = Boolean.parseBoolean(String.valueOf(commonDao.getData(" select MustExistsGoodsFlag from Department where DepartmentID = ? ", departmentid)));
                    if (mustExistsGoodsFlag && !client.isSuperSalesFlag()) {
                        if (SalesID == null || "".equals(SalesID) || "null".equalsIgnoreCase(SalesID)) {
                            tempList = commonController.checkNegativeInventoryForBackStage(commonDao, dataList, client.getOnLineId(), client.getUserID(), departmentid, 30, "", 0, 2, 0, 0, 0, "");
                        } else {
                            tempList = commonController.checkNegativeInventoryForBackStage(commonDao, dataList, client.getOnLineId(), client.getUserID(), departmentid, 30, SalesID, 0, 2, 1, 0, 1, "");
                        }
                    }
                }
            }
            System.out.println("wxflag的值："+req.getParameter("wxflag"));
            String wxflag =req.getParameter("wxflag");
           // boolean wxflag =Boolean.getBoolean(req.getParameter("wxflag")); 
            if (tempList.size() == 0) {
                // 保存单据
            	if("true".equals(wxflag)){ 
                  //  salesId = publicService.saveSalesX(direction, dataList, SalesID, customerid, departmentid, employeeId, businessDeptId, memo, type, typeEName, brandId, discountRateSum, lastARAmount, orderAmount, privilegeAmount, paymentTypeId, client);

            		salesId = publicService.saveSalesX2(direction, dataList, SalesID, customerid, departmentid, employeeId, businessDeptId, memo, type, typeEName, brandId, discountRateSum, lastARAmount, orderAmount, privilegeAmount, paymentTypeId, client);

            	}
            	else{	
                salesId = publicService.saveSales(direction, dataList, SalesID, customerid, departmentid, employeeId, businessDeptId, memo, type, typeEName, brandId, discountRateSum, lastARAmount, orderAmount, privilegeAmount, paymentTypeId, client);
            	}
                
            }
            j.getAttributes().put("SalesID", salesId);
            j.getAttributes().put("tempList", tempList);
        } catch (Exception e) {
            j.setSuccess(false);
            j.setMsg(e.getMessage());
            SysLogger.error(e.getMessage(), e);
        }
        return j;
    }

    /**
     * 销售发(退)货单条码校验后,以校验结果覆盖原始单据[新增,修改,删除]
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
            String SalesID = oConvertUtils.getString(req.getParameter("SalesID"));
            String jsonStr = oConvertUtils.getString(req.getParameter("data"));
            JSONArray datas = JSONArray.fromObject(jsonStr);
            List<Map<String, Object>> dataList = JSONArray.toList(datas, Map.class);
            int count = publicService.coverSave(SalesID, dataList, client);
            j.setObj(count);
        } catch (Exception e) {
            j.setSuccess(false);
            j.setMsg(e.getMessage());
            SysLogger.error(e.getMessage(), e);
        }
        return j;
    }

    /**
     * 审核销售发(退)货单,审核后生成对应的进出仓单
     * 
     * @param user
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
            String SalesID = oConvertUtils.getString(req.getParameter("SalesID"));
            String departmentid = oConvertUtils.getString(req.getParameter("departmentid"));
            if ("-1".equals(direction)) {
                // 调用存储过程生成进仓单
                commonDao.getStock(97, 1, SalesID, departmentid, client.getUserName());
            } else if ("1".equals(direction)) {
                // 调用存储过程生成出仓单
                commonDao.getStock(30, 1, SalesID, departmentid, client.getUserName());
            }
            // 更新主表
            StringBuilder sb = new StringBuilder();
            sb.append(" Update Sales set AuditDate = getdate(), Delivered=1, Year = '").append(DataUtils.getYear()).append("' , Month = '").append(DataUtils.getStringMonth()).append("' ").append(" where SalesID = '").append(SalesID).append("' ; ");
            commonDao.executeSql(sb.toString());
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
            String SalesID = oConvertUtils.getString(req.getParameter("SalesID"));
            String memo = oConvertUtils.getString(req.getParameter("memo"));
            StringBuilder sb = new StringBuilder();
            sb.append(" update Sales set memo = ? where SalesID = ? ");
            commonDao.executeSql(sb.toString(), memo, SalesID);
        } catch (Exception e) {
            j.setSuccess(false);
            j.setMsg(e.getMessage());
            SysLogger.error(e.getMessage(), e);
        }
        return j;
    }

    /**
     * 删除销售发(退)货单单据的货品记录(单据子记录信息)
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
            String salesID = oConvertUtils.getString(req.getParameter("SalesID"));
            String jsonStr = oConvertUtils.getString(req.getParameter("data"));
            JSONArray datas = JSONArray.fromObject(jsonStr);
            List<Map<String, Object>> dataList = JSONArray.toList(datas, Map.class);
            publicService.deleteSalesdetail(dataList, salesID);
        } catch (Exception e) {
            j.setSuccess(false);
            j.setMsg(e.getMessage());
            SysLogger.error(e.getMessage(), e);
        }
        return j;
    }

}
