package com.fuxi.web.controller;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fuxi.core.common.dao.impl.CommonDao;
import com.fuxi.core.common.model.json.AjaxJson;
import com.fuxi.core.common.service.PaymentService;
import com.fuxi.system.util.Client;
import com.fuxi.system.util.ResourceUtil;
import com.fuxi.system.util.SysLogger;
import com.fuxi.system.util.oConvertUtils;
@Controller
@RequestMapping("/payment")
public class PaymentController extends BaseController {
	
	   private Logger log = Logger.getLogger(PaymentController.class);
	   private SelectController controller = new SelectController();
	   private CommonController commonController = new CommonController();
	   
	   @Autowired
	    private CommonDao commonDao;
	   
	   @Autowired 
	   private PaymentService paymentService;
	   
	    @RequestMapping(params ="paylist")
	    @ResponseBody
	    public AjaxJson reclist(HttpServletRequest req)
	    {
	    	Client client = ResourceUtil.getClientFromSession(req);
           AjaxJson j = new AjaxJson();
           j.setAttributes(new HashMap<String, Object>());
           try{
           	   int page = oConvertUtils.getInt(req.getParameter("currPage"));
                  String audit = oConvertUtils.getString(req.getParameter("audit"));
                  String no = oConvertUtils.getString(req.getParameter("no"));
                  String beginDate = oConvertUtils.getString(req.getParameter("beginDate"));
                  String endDate = oConvertUtils.getString(req.getParameter("endDate"));
                  String departmentId = oConvertUtils.getString(req.getParameter("departmentId"));
                  String supplierId = oConvertUtils.getString(req.getParameter("supplierId"));
                  String employeeId = oConvertUtils.getString(req.getParameter("employeeId"));
                  StringBuffer sb = new StringBuffer();
                  
                  sb.append("select a.TallyFlag,a.PaymentID,a.SupplierID,a.Memo,a.No,Convert(varchar(10),a.Date,121) Date,a.PaymentTypeID,a.PaymentAmount,a.AuditFlag,a.EmployeeID,a.DepartmentID,Convert(varchar(10),a.MadeByDate,121) MadeByDate,a.MadeBy,a.BrandID,b.Code,b.Supplier,c.Department,d.Name,e.PaymentType,f.[No] as OrderNo,br.Brand,i.Department as BusinessDept "+
                		  "from Payment a Left Outer Join Supplier b On a.SupplierID=b.SupplierID "+
                		  " Left Outer Join Department c On a.DepartmentID=c.DepartmentID "+
                		  " Left Outer Join Employee d On  a.EmployeeID=d.EmployeeID "+
                		  " Left Outer Join PaymentType e On a.PaymentTypeID=e.PaymentTypeID "+
                		  " Left Outer Join PurchaseOrder f On a.PurchaseOrderID=f.PurchaseOrderID "+
                		  " left outer join Brand br on a.BrandID=br.BrandID "+
                		  " Left outer Join Department i on a.BusinessDeptID=i.DepartmentID "+
                          " Where 1=1 ");
                  // 按条件查询
                  if (null != audit && "0".equals(audit)) {
                      // 未审核
                      sb.append(" and a.AuditFlag = '0' ");
                  } else if (null != audit && "1".equals(audit)) {
                      // 已审核
                      sb.append(" and a.AuditFlag = '1' ");
                  }
                  // 查询单号时
                  if (no != null && !"".equals(no.trim()) && !"null".equalsIgnoreCase(no)) {
                      sb.append(" and a.No like '%" + no + "%' ");
                  }
                  // 时间区间
                  if (beginDate != null && !"".equals(beginDate.trim()) && !"null".equalsIgnoreCase(beginDate) && endDate != null && !"".equals(endDate.trim()) && !"null".equalsIgnoreCase(endDate)) {
                   //   sb.append(" and a.Date between convert(datetime,'" + beginDate + "', 120) and convert(datetime,'" + endDate + "', 120) ");
               	   sb.append(" and a.Date >= '" + beginDate + "' and a.Date <='" + endDate + " 23:59:59.997'");
                      
                  }
                  // 部门
                  if (departmentId != null && !"".equals(departmentId.trim()) && !"null".equalsIgnoreCase(departmentId)) {
                      sb.append(" and a.departmentId = '" + departmentId + "' ");
                  }
                  // 客户
                  if (supplierId != null && !"".equals(supplierId.trim()) && !"null".equalsIgnoreCase(supplierId)) {
                      sb.append(" and a.supplierId = '" + supplierId + "' ");
                  }
                  // 经手人
                  if (employeeId != null && !"".equals(employeeId.trim()) && !"null".equalsIgnoreCase(employeeId)) {
                      sb.append(" and a.employeeId = '" + employeeId + "' ");
                  }
                  
                  sb.append(" and  Exists(Select x.DepartmentID From DepartmentRight x, Supplier y Where a.SupplierID=y.SupplierID and x.DepartmentID=y.DepartmentID and x.UserID='"+client.getUserID()+"' and x.RightFlag=1 ) "+
                   " Order by a.[Date] DESC ,(a.[No])  DESC");
                  System.out.println("sql语句:"+sb.toString());
                  List<Map<String, Object>> list = commonDao.findForJdbc(sb.toString(), page, 15);
           	   
                  for(int i=0;i<list.size();i++){
               	   Map<String, Object> map=list.get(i);
               	   
               	   if(!"".equals(String.valueOf(map.get("PaymentAmount"))) && map.get("PaymentAmount") !=null){
               		   map.put("PaymentAmount", new BigDecimal(String.valueOf(map.get("PaymentAmount"))).setScale(2,BigDecimal.ROUND_DOWN)); 
                	  }else{
                		 map.put("PaymentAmount", ""); 
                	  }
               	   
               	
               	   List<Map<String,Object>> right=new ArrayList<>();
                	  
                	  for(int n=0;n<2;n++){
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
                	  }

                	  right.add(rmap);
                	}//for n 结束
                	  map.put("right", right); 
               	   
                  }
                  if(list.size()>0){
                  j.setObj(list);
                  j.setMsg("成功返回数据");
                  }else{
                  j.setMsg("暂无数据");	   
                  }
                  j.setSuccess(true);
           }catch(Exception e){
           	j.setSuccess(false);
           	j.setMsg(e.getMessage());
           	SysLogger.error(e.getMessage(),e);	
           }
           
	    	
	    	return j;
	    }
	    
	    
	    @RequestMapping(params = "auditOrder")
	    @ResponseBody
	    public AjaxJson auditOrder(HttpServletRequest req) {
	        Client client = ResourceUtil.getClientFromSession(req);
	        AjaxJson j = new AjaxJson();
	        j.setAttributes(new HashMap<String, Object>());
	        try {
	         
	            String ReceivalID = oConvertUtils.getString(req.getParameter("PaymentID"));
	            int AuditFlag =Integer.parseInt(req.getParameter("AuditFlag"));
	            
	            // 更新主表
	            StringBuilder sb = new StringBuilder();
	            if(AuditFlag==1){
	            sb.append(" Update Payment set AuditDate = getdate(),AuditFlag=1 ,Audit ='").append(client.getUserName()).append("'").append(" where PaymentID = '").append(ReceivalID).append("' ; ");
	            }else if(AuditFlag==0){
	            sb.append(" Update Payment set Audit=Null,AuditFlag=0,AuditDate=Null  ").append(" where PaymentID = '").append(ReceivalID).append("' ; ");
	                  	
	            }
	            commonDao.executeSql(sb.toString());
	            j.setSuccess(true);
	            j.setMsg("执行成功");
	        } catch (Exception e) {
	            j.setSuccess(false);
	            j.setMsg(e.getMessage());
	            SysLogger.error(e.getMessage(), e);
	        }
	        return j;
	    }
	    
	    
	    //保存单据
	    @RequestMapping(params = "savepay")
	    @ResponseBody
	    public AjaxJson saverec(HttpServletRequest req){
	    	  Client client = ResourceUtil.getClientFromSession(req);
		      AjaxJson j = new AjaxJson();
		      j.setAttributes(new HashMap<String, Object>());
		      try{
		    	  String PaymentID=oConvertUtils.getString(req.getParameter("PaymentID"));
		    	  String SupplierID=oConvertUtils.getString(req.getParameter("SupplierID"));
		    	  
		    	  String PaymentTypeID =oConvertUtils.getString(req.getParameter("PaymentTypeID"));
		   
		    	  String Date=oConvertUtils.getString(req.getParameter("Date"));
		  
		    	  String EmpID =oConvertUtils.getString(req.getParameter("EmployeeID"));
		    	  String PaymentAmount =oConvertUtils.getString(req.getParameter("PaymentAmount"));
		    	  String LastMustPayAmount =oConvertUtils.getString(req.getParameter("LastMustPayAmount"));
		    	  String Memo =oConvertUtils.getString(req.getParameter("Memo"));
		    	  
		    	  PaymentID=  paymentService.save(PaymentID, SupplierID, null, PaymentTypeID, Date, null, EmpID, null, PaymentAmount, LastMustPayAmount, Memo, client);
		    	  
		    	 j.setSuccess(true);
		    	 j.setMsg("操作成功");
		    	 j.setObj(PaymentID);
		    	  
		    	  
		      }catch(Exception e){
		    	  j.setSuccess(false);
		          j.setMsg(e.getMessage());
		          SysLogger.error(e.getMessage(), e);
		      }
		      return j;
	    }
	   
	   
}
