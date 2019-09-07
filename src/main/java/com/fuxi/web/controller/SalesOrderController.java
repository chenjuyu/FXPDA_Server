package com.fuxi.web.controller;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
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
import com.fuxi.core.common.service.SalesOrderService;
import com.fuxi.core.vo.base.Period;
import com.fuxi.system.util.Client;
import com.fuxi.system.util.DataUtils;
import com.fuxi.system.util.MyTools;
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

    
    /*
     * 销售 订单 主表信息 2019-09-01新写
     * 
     * */
    @RequestMapping(params = "salesOrderlist")
    @ResponseBody
    public AjaxJson salesOrderlist(HttpServletRequest req){
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
              String customerId = oConvertUtils.getString(req.getParameter("customerId"));
              String employeeId = oConvertUtils.getString(req.getParameter("employeeId"));
              StringBuffer sb = new StringBuffer();
              
              sb.append(" select so.SalesOrderID,so.CustomerID,so.Type,so.AuditDate, (select Department from Department  where so.DepartmentID = DepartmentID) Department , No, CONVERT(varchar(100), Date, 111) Date, "
                      + " isnull(QuantitySum,0) QuantitySum,AmountSum,AuditFlag,so.MadeByDate,(select Name from Employee where employeeId = so.EmployeeId) Name,isnull(so.Memo,'') Memo, "
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
              
              for(int i=0;i<list.size() ;i++){
             	 
            	  Map<String,Object> listmap=(Map<String, Object>) list.get(i);
            	  
            	  if(!"".equals(String.valueOf(listmap.get("AmountSum"))) && listmap.get("AmountSum") !=null){
            		  listmap.put("AmountSum", new BigDecimal(String.valueOf(listmap.get("AmountSum"))).setScale(2,BigDecimal.ROUND_DOWN)); 
            	  }else{
            		  listmap.put("AmountSum", ""); 
            	  }
            	 /* if(!"".equals(String.valueOf(listmap.get("ReceivalAmount"))) && listmap.get("ReceivalAmount") !=null){
            		  listmap.put("ReceivalAmount", new BigDecimal(String.valueOf(listmap.get("ReceivalAmount"))).setScale(2,BigDecimal.ROUND_DOWN)); 
            	  }else{
            		  listmap.put("ReceivalAmount", ""); 
            	  } */
            	  
            	  
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
            	  listmap.put("right", right);
              }
              
              
              
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
    
    
    /*
   * 根据 SalesOrderID 显示 单据详情，显示横向尺码
   * 
   * */
  @RequestMapping(params = "salesOrderEditX")
  @ResponseBody
  public AjaxJson salesOrderEditX(HttpServletRequest req){
      AjaxJson j =new AjaxJson();
      j.setAttributes(new HashMap<String, Object>());
      Client client = ResourceUtil.getClientFromSession(req);
      List<Map<String, Object>> list =new ArrayList<>();
  	try{
  		String SalesOrderID = oConvertUtils.getString(req.getParameter("SalesOrderID"));
  		int direction =1; //销售退货单要以正数显示
  		StringBuffer sb = new StringBuffer();
  	  sb.append("Select a.*,b.Code,b.SupplierCode, b.Name,b.Model,b.Unit,c.Color,b.GroupID,b.GroupNo,"+
  		"b.StopFlag,b.age,b.Season,br.brand,b.RetailSales1,b.RetailSales2,b.PurchasePrice,bs.Serial "+
  		"from SalesOrderDetailTemp a join Goods b on a.GoodsID = b.GoodsID "+
  		" "+
  		"join Color c on a.ColorID=c.ColorID left join brand br on b.brandid = br.brandid "+
  		"left join BrandSerial bs on b.BrandSerialID = bs.BrandSerialID "+
  		
  		"where a.SalesOrderID= '"+SalesOrderID+"'" + 
  		"order by a.SalesOrderID, a.IndexNo, b.Code,c.No");	
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
  				 sdata.put("SalesOrderDetailID", String.valueOf(map.get("SalesOrderDetailID")));
  				 sdata.put("SalesOrderID", String.valueOf(map.get("SalesOrderID")));
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
  				  sdata.put("Amount",new BigDecimal(Double.valueOf(String.valueOf(map.get("UnitPrice")))).multiply(new BigDecimal(DiscountRate)).divide(new BigDecimal(10.0))
  				 .multiply(new BigDecimal(Double.valueOf(String.valueOf(sdata.get("Quantity"))))).setScale(2,BigDecimal.ROUND_DOWN));
  				 }else{
  					 sdata.put("Amount",""); 
  				 } //精确到尺码的金额，为置空
  				 
  				 sizeData.add(sdata);
  			 }
  			 datamap.put("SalesOrderDetailID", String.valueOf(map.get("SalesOrderDetailID")));
  			 datamap.put("SalesOrderID", String.valueOf(map.get("SalesOrderID")));
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
  		 
  	 if(list.size() >0)
  		 j.setMsg("成功返回数据");
  	  }else{
  		 j.setMsg("暂无数据"); 
  	  }	
  	  
           j.setSuccess(true);
           j.setObj(list);
  	  
           System.out.println("SalesOrderID的值："+SalesOrderID);
           System.out.println("list的值："+list.toString());
  	}catch(Exception e){
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
    @RequestMapping(params = "saveSalesOrderX")
    @ResponseBody
    public AjaxJson saveSalesOrderX(HttpServletRequest req){
    	  Client client = ResourceUtil.getClientFromSession(req);
          AjaxJson j = new AjaxJson();
          j.setAttributes(new HashMap<String, Object>());
          try{
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
              
              System.out.println("JSON串："+jsonStr);
              
              
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
             
             System.out.println("salesOrderID:"+salesOrderID);
              
             salesOrderID =salesOrderService.saveSalesOrderX(dataList, salesOrderID, customerid, departmentid, employeeId, businessDeptId, memo, type, typeEName, brandId, discountRateSum, lastARAmount, null, privilegeAmount, paymentTypeId, client);
          
            j.setObj(salesOrderID);
            j.setMsg("操作成功");
            j.setSuccess(true);
        	  
          }catch(Exception e){
        	  j.setMsg(e.getMessage());
        	  j.setSuccess(false);
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
            
            int AuditFlag =Integer.parseInt(oConvertUtils.getString(req.getParameter("AuditFlag")));
            
            Period p = commonDao.getPeriod(DataUtils.formatDate(new Date()));
            StringBuilder sb = new StringBuilder();
            if(AuditFlag ==1){
            sb.append(" Update SalesOrder set AuditFlag = 1, AuditDate = getdate(), Year = '").append(p.getPeriodYeay()).append("' , Month = '").append(p.getPeriodMonth()).append("' ").append(" where SalesOrderID = '").append(SalesOrderID).append("' ");
            }else{
            	 sb.append(" Update SalesOrder set AuditFlag = 0 ").append(" where SalesOrderID = '").append(SalesOrderID).append("' ");
                 	
            }
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
