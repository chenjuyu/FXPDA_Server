package com.fuxi.web.controller;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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
import com.fuxi.core.common.service.ReceivalService;
import com.fuxi.system.util.Client;
import com.fuxi.system.util.DataUtils;
import com.fuxi.system.util.ResourceUtil;
import com.fuxi.system.util.SysLogger;
import com.fuxi.system.util.oConvertUtils;


@Controller
@RequestMapping("/receival")
public class ReceivalController extends BaseController {
	
	   private Logger log = Logger.getLogger(ReceivalController.class);
	    private SelectController controller = new SelectController();
	    private CommonController commonController = new CommonController();

	    @Autowired
	    private CommonDao commonDao;
	
	    @Autowired
	    private ReceivalService receivalService;
	    
	    
	    @RequestMapping(params ="reclist")
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
                   String customerId = oConvertUtils.getString(req.getParameter("customerId"));
                   String employeeId = oConvertUtils.getString(req.getParameter("employeeId"));
                   StringBuffer sb = new StringBuffer();
                   
                   sb.append("Select a.TallyFlag,a.ReceivalID,a.No,Convert(varchar(10),a.Date,121) Date, Convert(varchar(10),a.ValidBeginDate,121) ValidBeginDate,a.QuantitySum,a.AmountSum,a.Type,a.PaymentTypeID,a.ReceivalAmount,a.AuditFlag,a.EmployeeID,a.DepartmentID,Convert(varchar(10),a.MadeByDate,121) MadeByDate,a.MadeBy,a.BrandID,a.ReceiDeptID,Convert(varchar(10),a.AuditDate,121) AuditDate, a.CustomerID ,b.Customer,c.Department,d.Name,e.PaymentType,f.[No] as OrderNo, g.FreightCorp,h.department ReceiDepartment,br.Brand,i.Department as BusinessDept"+
                    " from Receival a Left Outer Join Customer b On a.CustomerID=b.CustomerID "+
                    " Left Outer Join Department c On a.DepartmentID=c.DepartmentID "+
                    " Left Outer Join Employee d On a.EmployeeID=d.EmployeeID "+
                    " Left Outer Join PaymentType e On a.PaymentTypeID=e.PaymentTypeID "+
                    " Left Outer Join SalesOrder f On a.SalesOrderID=f.SalesOrderID "+
                    " Left Outer Join FreightCorp g On a.FreightCorpID=g.FreightCorpID "+
                    " left outer join department h on a.ReceiDeptID=h.departmentid "+
                    " left outer join Brand br on a.BrandID=br.BrandID "+
                    " Left outer join Department i on a.BusinessDeptID=i.DepartmentID "+
                    " Where 1=1 ");
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
                       sb.append(" and a.Date between convert(datetime,'" + beginDate + "', 120) and convert(datetime,'" + endDate + "', 120) ");
                   }
                   // 部门
                   if (departmentId != null && !"".equals(departmentId.trim()) && !"null".equalsIgnoreCase(departmentId)) {
                       sb.append(" and a.departmentId = '" + departmentId + "' ");
                   }
                   // 客户
                   if (customerId != null && !"".equals(customerId.trim()) && !"null".equalsIgnoreCase(customerId)) {
                       sb.append(" and a.customerId = '" + customerId + "' ");
                   }
                   // 经手人
                   if (employeeId != null && !"".equals(employeeId.trim()) && !"null".equalsIgnoreCase(employeeId)) {
                       sb.append(" and a.employeeId = '" + employeeId + "' ");
                   }
                   
                   sb.append("and  Exists(Select x.DepartmentID From DepartmentRight x, Customer y Where a.CustomerID=y.CustomerID and x.DepartmentID=y.DepartmentID and x.UserID='"+client.getUserID()+"' and x.RightFlag=1 ) "+
                    " Order by a.[Date] DESC,(a.[No]) DESC");
                   List<Map<String, Object>> list = commonDao.findForJdbc(sb.toString(), page, 15);
            	   
                   for(int i=0;i<list.size();i++){
                	   Map<String, Object> map=list.get(i);
                	   
                	   if(!"".equals(String.valueOf(map.get("ReceivalAmount"))) && map.get("ReceivalAmount") !=null){
                		   map.put("ReceivalAmount", new BigDecimal(String.valueOf(map.get("ReceivalAmount"))).setScale(2,BigDecimal.ROUND_DOWN)); 
                 	  }else{
                 		 map.put("ReceivalAmount", ""); 
                 	  }
                	   
                	   if(!"".equals(String.valueOf(map.get("QuantitySum"))) && map.get("QuantitySum") !=null){
                		   map.put("QuantitySum", Integer.parseInt(String.valueOf(map.get("QuantitySum")))); 
                 	  }else{
                 		 map.put("QuantitySum", ""); 
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
                   j.setObj(list);
                   j.setMsg("成功返回数据");
                   
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
	         
	            String ReceivalID = oConvertUtils.getString(req.getParameter("ReceivalID"));
	            int AuditFlag =Integer.parseInt(req.getParameter("AuditFlag"));
	            
	            // 更新主表
	            StringBuilder sb = new StringBuilder();
	            if(AuditFlag==1){
	            sb.append(" Update Receival set AuditDate = getdate(),AuditFlag=1 ,Audit ='").append(client.getUserName()).append("'").append(" where ReceivalID = '").append(ReceivalID).append("' ; ");
	            }else if(AuditFlag==0){
	            sb.append(" Update Receival set Audit=Null,AuditFlag=0,AuditDate=Null  ").append(" where ReceivalID = '").append(ReceivalID).append("' ; ");
	                  	
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
	    

}
