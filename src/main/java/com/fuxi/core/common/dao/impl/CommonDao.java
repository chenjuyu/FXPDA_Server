package com.fuxi.core.common.dao.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import com.fuxi.core.common.dao.ICommonDao;
import com.fuxi.core.vo.base.Period;
import com.fuxi.system.util.DataUtils;


/**
 * 公共扩展方法
 * 
 * @author
 * 
 */
@Repository
public class CommonDao extends GenericBaseCommonDao implements ICommonDao {

    /**
     * 生成新单据的ID
     */
    @Override
    public String getNewIDValue(int type) {
        Procedure procedure = new Procedure();
        procedure.setSql("GetNewIDValue");
        // procedure.setReturnParam("rows", new ColumnMapRowMapper());
        procedure.setIntegerParam("@TableTag");
        procedure.setValue("@TableTag", type);
        procedure.setVarcharOutParam("@IDValue");
        Map map = callableStatementByName(procedure);

        return (String) map.get("@IDValue");
    }


    /**
     * 生成单据的新单号
     */
    @Override
    public String getNewNOValue(int type, String id, String deptCode) {
        Procedure procedure = new Procedure();
        procedure.setSql("GetMaxNo");
        // procedure.setReturnParam("rows", new ColumnMapRowMapper());
        procedure.setIntegerParam("@TableTag");
        procedure.setValue("@TableTag", type);
        procedure.setVarcharOutParam("@No");
        procedure.setVarcharParam("@DeptCode");
        procedure.setValue("@DeptCode", deptCode);

        Map map = callableStatementByName(procedure);

        return map.get("@No").toString();
    }

    /**
     * 生成单据的新单号
     */
    @Override
    public String getNewNOValue(int type, String id) {
        Procedure procedure = new Procedure();
        procedure.setSql("GetMaxNo");
        // procedure.setReturnParam("rows", new ColumnMapRowMapper());
        procedure.setIntegerParam("@TableTag");
        procedure.setValue("@TableTag", type);
        procedure.setVarcharOutParam("@No");
        Map map = callableStatementByName(procedure);

        return map.get("@No").toString();
    }

    /**
     * 会计区间的年份和月份
     * 
     * @param date
     * @return
     */
    public Period getPeriod(String date) {
        StringBuilder sb = new StringBuilder();
        sb.append(" select PeriodYear, PeriodMonth from Period where  '").append(date).append("' between BeginDate and EndDate ");
        List list = findForJdbc(sb.toString());
        Period p = new Period();
        if (list.size() > 0) {
            Map map = (Map) list.get(0);
            p.setPeriodMonth(map.get("PeriodMonth").toString());
            p.setPeriodYeay(map.get("PeriodYear").toString());
        } else {
            p.setPeriodMonth("");
            p.setPeriodYeay("");
        }
        return p;
    }

    /**
     * 单据审核后生成出仓单
     * 
     * @param tag
     * @param flag
     * @param salesId
     * @param warehouseId
     * @param userName
     */
    public void getStock(int tag, int flag, String salesId, String warehouseId, String userName) {
        Procedure procedure = new Procedure();
        procedure.setSql("ExecOutportStock");
        procedure.setIntegerParam("@TableTag");
        procedure.setValue("@TableTag", tag);
        procedure.setIntegerParam("@AuditFlag");
        procedure.setValue("@AuditFlag", flag);
        procedure.setVarcharParam("@IDStr");
        procedure.setValue("@IDStr", salesId);
        procedure.setVarcharParam("@WarehouseID");
        procedure.setValue("@WarehouseID", warehouseId);
        procedure.setVarcharParam("@AuditName");
        procedure.setValue("@AuditName", userName);
        callableStatementByName(procedure);
    }


    /**
     * 根据参数生成库存信息报表
     * 
     * @param hostName
     * @param endDate
     * @param departmentId
     * @param goodsId
     * @param colorId
     * @param sizeId
     * @param sendType
     * @param stockType
     * @param userId
     */
    public void queryStock(String hostName, String endDate, String departmentId, String goodsId, String colorId, String sizeId, int sendType, int stockType, String userId) {
        Procedure procedure = new Procedure();
        procedure.setSql("sys_GetStockState_Rpt");
        procedure.setVarcharParam("@HostName");
        procedure.setValue("@HostName", hostName);
        procedure.setVarcharParam("@EndDate");
        procedure.setValue("@EndDate", endDate);
        procedure.setVarcharParam("@DepartmentID");
        procedure.setValue("@DepartmentID", departmentId);
        procedure.setVarcharParam("@GoodsID");
        procedure.setValue("@GoodsID", goodsId);
        procedure.setVarcharParam("@ColorID");
        procedure.setValue("@ColorID", colorId);
        procedure.setVarcharParam("@SizeID");
        procedure.setValue("@SizeID", sizeId);
        procedure.setIntegerParam("@SendType");
        procedure.setValue("@SendType", sendType);
        procedure.setIntegerParam("@StockType");
        procedure.setValue("@StockType", stockType);
        procedure.setVarcharParam("@UserID");
        procedure.setValue("@UserID", userId);
        callableStatementByName(procedure);
    }

    /**
     * 转仓单审核后生成进出仓单
     * 
     * @param aId
     * @param flag
     * @param auditer
     * @param fromStockFlag
     */
    public void auditStockMove(String aId, int flag, String auditer, int fromStockFlag) {
        Procedure procedure = new Procedure();
        procedure.setSql("AuditStockMove");
        procedure.setVarcharParam("@aID");
        procedure.setValue("@aID", aId);
        procedure.setIntegerParam("@AuditFlag");
        procedure.setValue("@AuditFlag", flag);
        procedure.setVarcharParam("@Auditer");
        procedure.setValue("@Auditer", auditer);
        procedure.setIntegerParam("@FromStockFlag");
        procedure.setValue("@FromStockFlag", fromStockFlag);
        callableStatementByName(procedure);
    }

    /**
     * 箱条码配码改变时更新单据中箱条码录入记录的SizeStr
     * 
     * @param aId
     * @param flag
     * @param auditer
     * @param fromStockFlag
     */
    public void validInvoiceSizeStr(int tableTag, String tableId) {
        Procedure procedure = new Procedure();
        procedure.setSql("ValidInvoiceSizeStr");
        procedure.setIntegerParam("@TableTag");
        procedure.setValue("@TableTag", tableTag);
        procedure.setVarcharParam("@TableID");
        procedure.setValue("@TableID", tableId);
        callableStatementByName(procedure);
    }

    /**
     * 根据条件生成库存报表
     * 
     * @param hostName
     * @param deptId
     * @param goodsType
     * @param year
     * @param season
     * @param goodsId
     * @param brand
     * @param userId
     */
    public void getStockReport(String hostName, String deptId, String goodsType, String year, String season, String goodsId, String brand, String userId) {
        Procedure procedure = new Procedure();
        procedure.setSql("mpos_stock");
        procedure.setVarcharParam("@HostName");
        procedure.setValue("@HostName", hostName);
        procedure.setVarcharParam("@DeptID");
        procedure.setValue("@DeptID", deptId);
        procedure.setVarcharParam("@GType");
        procedure.setValue("@GType", formatString(goodsType));
        procedure.setVarcharParam("@Age");
        procedure.setValue("@Age", formatString(year));
        procedure.setVarcharParam("@Season");
        procedure.setValue("@Season", formatString(season));
        procedure.setVarcharParam("@goodsid");
        procedure.setValue("@goodsid", formatString(goodsId));
        procedure.setVarcharParam("@Brand");
        procedure.setValue("@Brand", formatString(brand));
        procedure.setVarcharParam("@userID");
        procedure.setValue("@userID", userId);
        procedure.setVarcharParam("@date2");
        String date = DataUtils.formatDate();
        procedure.setValue("@date2", date);
        callableStatementByName(procedure);
    }

    /**
     * 伏羲库存查询表(后台库存)
     * 
     * @param hostName
     * @param departmentId
     * @param goodsId
     * @param colorId
     * @param sizeId
     * @param userId
     * @param tableTag
     * @param invoiceId
     */
    public void getStockState(String hostName, String departmentId, String goodsId, String colorId, String sizeId, String userId, int tableTag, String invoiceId, int sendType, int disType, int optStata, int controlFlag, int auditFlag, String noDate) {
        Procedure procedure = new Procedure();
        procedure.setSql("sys_GetStockState");
        procedure.setVarcharParam("@HostName");
        procedure.setValue("@HostName", hostName);
        procedure.setVarcharParam("@DepartmentID");
        procedure.setValue("@DepartmentID", departmentId);
        procedure.setVarcharParam("@GoodsID");
        procedure.setValue("@GoodsID", goodsId);
        procedure.setVarcharParam("@ColorID");
        procedure.setValue("@ColorID", colorId);
        procedure.setVarcharParam("@SizeID");
        procedure.setValue("@SizeID", sizeId);
        procedure.setVarcharParam("@UserID");
        procedure.setValue("@UserID", userId);
        procedure.setIntegerParam("@Tabletag");
        procedure.setValue("@Tabletag", tableTag);
        procedure.setVarcharParam("@InvoiceID");
        procedure.setValue("@InvoiceID", invoiceId);
        procedure.setIntegerParam("@SendType");
        procedure.setValue("@SendType", sendType);
        procedure.setIntegerParam("@DisType");
        procedure.setValue("@DisType", -1);
        procedure.setIntegerParam("@OptStata");
        procedure.setValue("@OptStata", optStata);
        procedure.setIntegerParam("@ControlFlag");
        procedure.setValue("@ControlFlag", controlFlag);
        procedure.setVarcharParam("@GetStockState");
        procedure.setValue("@GetStockState", "_GetStockState");
        procedure.setIntegerParam("@AuditFlag");
        procedure.setValue("@AuditFlag", auditFlag);
        procedure.setVarcharParam("@NoDate");
        procedure.setValue("@NoDate", noDate);
        procedure.setIntegerParam("@DebugFlag");
        procedure.setValue("@DebugFlag", 0);
        callableStatementByName(procedure);
    }

    /**
     * 伏羲库存查询表(前台库存)
     * 
     * @param departmentId
     * @param goodsId
     * @param colorId
     * @param sizeId
     * @return
     */
    public int getStockGCS(String departmentId, String goodsId, String colorId, String sizeId) {
        Procedure procedure = new Procedure();
        procedure.setSql("GetStock_GCS");
        procedure.setIntegerOutParam("@Qty");
        procedure.setVarcharParam("@WareID");
        procedure.setValue("@WareID", departmentId);
        procedure.setVarcharParam("@GoodsID");
        procedure.setValue("@GoodsID", goodsId);
        procedure.setVarcharParam("@ColorID");
        procedure.setValue("@ColorID", colorId);
        procedure.setVarcharParam("@SizeID");
        procedure.setValue("@SizeID", sizeId);
        Map map = callableStatementByName(procedure);
        return (Integer) map.get("@Qty");
    }
    
    
    //Exec ExecPOSSalesUnDo 'SM0006J','900','GLY' 冲销销售小票的方法
    
    public void ExecPOSSalesUnDo(String posid,String deptNo,String MadeBy){
    	 Procedure procedure = new Procedure();
    	 procedure.setSql("ExecPOSSalesUnDo");
    	 procedure.setVarcharParam("@posid");
    	 procedure.setValue("@posid", posid);
    	 procedure.setVarcharParam("@deptNo");
    	 procedure.setValue("@deptNo", deptNo);
    	 
    	 procedure.setVarcharParam("@MadeBy");
    	 procedure.setValue("@MadeBy", MadeBy);
    	 callableStatementByName(procedure);
    	
    }
    
    //查询报表使用 ，并返回List<Map>
    
  //注意有返回结果集的时候，第一个参数必须设置为返回结果集参数，不然会报错。
  		
  	    public List<Map<String,Object>> Exec8088Rpt(String searchType,String Condition ,String DisType,String DepartmentID,String DistrictID,String Orderby
  	    		,String OrderField,int OrderFieldNo,String BeginDate,String EndDate,String userID){
  	    	 Procedure procedure = new Procedure();
  	    	List<Map<String,Object>> ls=new ArrayList<>();
  	    	
  	    	 if("Department".equals(searchType)){
  	    	 procedure.setSql("z_8088Rpt");  	    	 
  	    	 procedure.setVarcharParam("@Condition");
  	    	 procedure.setValue("@Condition", Condition);
  	    	 
  	    	 procedure.setVarcharParam("@DisType");
  	    	 procedure.setValue("@DisType", DisType);
  	  
  	    	 procedure.setVarcharParam("@DepartmentID");
  	    	 procedure.setValue("@DepartmentID", DepartmentID);
  	    	 
  	    	 procedure.setVarcharParam("@DistrictID");
 	    	 procedure.setValue("@DistrictID", DistrictID);
 	    	 
 	    	 procedure.setVarcharParam("@Orderby");
 	    	 procedure.setValue("@Orderby", Orderby);
 	    	 procedure.setVarcharParam("@OrderField");
	    	 procedure.setValue("@OrderField", OrderField);
	    	 procedure.setVarcharParam("@OrderFieldNo");
	    	 procedure.setValue("@OrderFieldNo", OrderFieldNo);
	    	 procedure.setVarcharParam("@BeginDate");
	    	 procedure.setValue("@BeginDate", BeginDate);
	    	 procedure.setVarcharParam("@EndDate");
	    	 procedure.setValue("@EndDate", EndDate);
	    	 procedure.setVarcharParam("@userID");
	    	 procedure.setValue("@userID", userID);
  	    	 }else if("Brand".equals(searchType)){
  	    		procedure.setSql("z_8089Rpt");  	    	 
  	  	    	 procedure.setVarcharParam("@Condition");
  	  	    	 procedure.setValue("@Condition", Condition);
  	  	    	 
  	  	    	 procedure.setVarcharParam("@DisType");
  	  	    	 procedure.setValue("@DisType", DisType);
  	  	  
  	  	    	 procedure.setVarcharParam("@BrandID");
  	  	    	 procedure.setValue("@BrandID", DepartmentID);
  	  	    	 
  	  	    	 procedure.setVarcharParam("@DistrictID");
  	 	    	 procedure.setValue("@DistrictID", DistrictID);
  	 	    	 
  	 	    	 procedure.setVarcharParam("@Orderby");
  	 	    	 procedure.setValue("@Orderby", Orderby);
  	 	    
  		    
  		    	 procedure.setVarcharParam("@BeginDate");
  		    	 procedure.setValue("@BeginDate", BeginDate);
  		    	 procedure.setVarcharParam("@EndDate");
  		    	 procedure.setValue("@EndDate", EndDate);
  		    	 procedure.setVarcharParam("@userID");
  		    	 procedure.setValue("@userID", userID); 
  	    	 }else if("Employee".equals(searchType)){ //导购
  	    		 procedure.setSql("z_8090Rpt");  	    	 
  	  	    	 procedure.setVarcharParam("@Condition");
  	  	    	 procedure.setValue("@Condition", Condition);
  	  	    	 
  	  	    	 procedure.setVarcharParam("@DisType");
  	  	    	 procedure.setValue("@DisType", DisType);
  	  	  
  	  	    	 procedure.setVarcharParam("@EmployeeID");
  	  	    	 procedure.setValue("@EmployeeID", DepartmentID);
  	  	    	 
  	  	    	 procedure.setVarcharParam("@DistrictID");
  	 	    	 procedure.setValue("@DistrictID", DistrictID);
  	 	    	 
  	 	    	 procedure.setVarcharParam("@Orderby");
  	 	    	 procedure.setValue("@Orderby", Orderby);
  	 	    	 procedure.setVarcharParam("@OrderField");
  		    	 procedure.setValue("@OrderField", OrderField);
  		    	 procedure.setVarcharParam("@OrderFieldNo");
  		    	 procedure.setValue("@OrderFieldNo", OrderFieldNo);
  		    	 procedure.setVarcharParam("@BeginDate");
  		    	 procedure.setValue("@BeginDate", BeginDate);
  		    	 procedure.setVarcharParam("@EndDate");
  		    	 procedure.setValue("@EndDate", EndDate);
  		    	 procedure.setVarcharParam("@userID");
  		    	 procedure.setValue("@userID", userID);
 
  	    	 }else if("GoodsType".equals(searchType)){//品类
  	    	    	 
  	    		 procedure.setSql("z_8091Rpt");  	    	 
 	  	    	 procedure.setVarcharParam("@Condition");
 	  	    	 procedure.setValue("@Condition", Condition);
 	  	    	 
 	  	    	 procedure.setVarcharParam("@DisType");
 	  	    	 procedure.setValue("@DisType", DisType);
 	  	  
 	  	    	 procedure.setVarcharParam("@GoodsTypeID");
 	  	    	 procedure.setValue("@GoodsTypeID", DepartmentID);
 	  	    	 
 	  	    	 procedure.setVarcharParam("@DistrictID");
 	 	    	 procedure.setValue("@DistrictID", DistrictID);
 	 	    	 
 	 	    	 procedure.setVarcharParam("@Orderby");
 	 	    	 procedure.setValue("@Orderby", Orderby);
 	 	    
 		    
 		    	 procedure.setVarcharParam("@BeginDate");
 		    	 procedure.setValue("@BeginDate", BeginDate);
 		    	 procedure.setVarcharParam("@EndDate");
 		    	 procedure.setValue("@EndDate", EndDate);
 		    	 procedure.setVarcharParam("@userID");
 		    	 procedure.setValue("@userID", userID); 
  	    	 }else if("Supplier".equals(searchType)){ //厂商
  	    		procedure.setSql("z_8092Rpt");  	    	 
	  	    	 procedure.setVarcharParam("@Condition");
	  	    	 procedure.setValue("@Condition", Condition);
	  	    	 
	  	    	 procedure.setVarcharParam("@DisType");
	  	    	 procedure.setValue("@DisType", DisType);
	  	  
	  	    	 procedure.setVarcharParam("@SupplierID");
	  	    	 procedure.setValue("@SupplierID", DepartmentID);
	  	    	 
	  	    	 procedure.setVarcharParam("@DistrictID");
	 	    	 procedure.setValue("@DistrictID", DistrictID);
	 	    	 
	 	    	 procedure.setVarcharParam("@Orderby");
	 	    	 procedure.setValue("@Orderby", Orderby);
	 	    
		    
		    	 procedure.setVarcharParam("@BeginDate");
		    	 procedure.setValue("@BeginDate", BeginDate);
		    	 procedure.setVarcharParam("@EndDate");
		    	 procedure.setValue("@EndDate", EndDate);
		    	 procedure.setVarcharParam("@userID");
		    	 procedure.setValue("@userID", userID); 
  	    		 
  	    	 }else if("Goods".equals(searchType)){ //排行按货品显示  这里没有返回查询结结果要的，要另外 写
  	  		     procedure.setSql("z_8104Rpt");  	    	 
	  	    	 procedure.setVarcharParam("@Condition");
	  	    	 procedure.setValue("@Condition", Condition);
	  	    	 procedure.setVarcharParam("@BeginDate");
		    	 procedure.setValue("@BeginDate", BeginDate);
		    	 procedure.setVarcharParam("@EndDate");
		    	 procedure.setValue("@EndDate", EndDate);
		    	 procedure.setVarcharParam("@userID");
		    	 procedure.setValue("@userID", userID);
  	    		
  	    	 }
  	    	 
  	    	Map<String,Object> map= callableStatementByName(procedure); //这个得看过程是否返回的是列表
  	    	
  	    	System.out.println("map:"+String.valueOf(map));
  	    	
  	    	if(map!=null && !"{}".equals(map)){ //不返回数据时为{} 所以这个也要的的的排除
  	    	for(String key : map.keySet()){
    		    ls =(List<Map<String,Object>>) map.get(key);
    		  
    	    }
  	    	}
  	    	if("Goods".equals(searchType))
  	    	{
  	    	   ls =this.findForJdbc("select *,AvgPrice=Case when isnull(Qty,0)=0 then '' else round(Amt/Qty,2) end from GoodsRankings where userid= ? ", userID);
  	    		
  	    	}
  	    
  	    	//作字段转换 FactAmount 转为Amount 作显示用  这里判断 键位即可
  	    	for(int i=0;i<ls.size();i++){
  	    		Map<String,Object> m=ls.get(i);
  	    		if(m.containsKey("FactAmount")){
  	    		 if(!"".equals(String.valueOf(m.get("FactAmount"))) && m.get("FactAmount") !=null){
         			 m.put("Amount", new BigDecimal(String.valueOf(m.get("FactAmount"))).setScale(2,BigDecimal.ROUND_DOWN));
         			 }else{
         			 m.put("Amount", "");	 
         			 }
  	    		}
  	    		if(m.containsKey("Department")){
  	    	    m.put("Name", m.get("Department"));
  	    		}
  	    		if(m.containsKey("Brand")){
  	    		m.put("Name", m.get("Brand"));
  	    		}
  	    		if(m.containsKey("GoodsType")){
  	    		m.put("Name", m.get("GoodsType"));	
  	    		}
  	    		if(m.containsKey("Supplier")){	
  	    		m.put("Name", m.get("Supplier"));		
  	    		}
  	    		if(m.containsKey("Qty") && "Goods".equals(searchType)){
  	    		m.put("Quantity", m.get("Qty"));	
  	    		
  	    		}
  	    		if(m.containsKey("Amt") && "Goods".equals(searchType)){
  	    			
  	    			 if(!"".equals(String.valueOf(m.get("Amt"))) && m.get("Amt") !=null){
  	         			 m.put("Amount", new BigDecimal(String.valueOf(m.get("Amt"))).setScale(2,BigDecimal.ROUND_DOWN));
  	         			 }else{
  	         			 m.put("Amount", "");	 
  	         			 }
  	    			
  	  	    		
  	  	    		}
  	    		if(m.containsKey("Code") && "Goods".equals(searchType)){ //把货号当着Name 显示 会覆盖原来的值
  	  	    		m.put("Name", m.get("Code"));	
  	  	    		
  	  	    		}
  	    		
  	    		if(m.containsKey("AvgPrice") && "Goods".equals(searchType)){ //把货号当着Name 显示 会覆盖原来的值
  	  	    		
  	    			 if(!"".equals(String.valueOf(m.get("AvgPrice"))) && m.get("AvgPrice") !=null){
  	         			 m.put("AvgPrice", new BigDecimal(String.valueOf(m.get("AvgPrice"))).setScale(2,BigDecimal.ROUND_DOWN));
  	         			 }else{
  	         			 m.put("AvgPrice", "");	 
  	         			 }
  	    			
  	  	    		
  	  	    		}
  	    	}
  	    
  	    	return ls;
  	    	
  	    }
    
    
    
    

    /**
     * 店铺库存汇总表
     * 
     * @param aId
     * @param flag
     * @param auditer
     * @param fromStockFlag
     */
    public void shopStockRpt(String hostName, String date, String deptId, String goodsId, int disType, String userID, int debugFlag, String gType, String age, String season, String brand) {
        Procedure procedure = new Procedure();
        procedure.setSql("CreateShopStockRpt");
        procedure.setVarcharParam("@HostName");
        procedure.setValue("@HostName", hostName);
        procedure.setVarcharParam("@date2");
        procedure.setValue("@date2", date);
        procedure.setVarcharParam("@DeptID");
        procedure.setValue("@DeptID", deptId);
        procedure.setVarcharParam("@goodsid");
        procedure.setValue("@goodsid", goodsId);
        procedure.setIntegerParam("@DisType");
        procedure.setValue("@DisType", disType);
        procedure.setVarcharParam("@userID");
        procedure.setValue("@userID", userID);
        procedure.setIntegerParam("@DebugFlag");
        procedure.setValue("@DebugFlag", debugFlag);
        procedure.setVarcharParam("@GType");
        procedure.setValue("@GType", gType);
        procedure.setVarcharParam("@Age");
        procedure.setValue("@Age", age);
        procedure.setVarcharParam("@Season");
        procedure.setValue("@Season", season);
        procedure.setVarcharParam("@Brand");
        procedure.setValue("@Brand", brand);
        callableStatementByName(procedure);
    }
    
    /**
     * 店铺日结
     * @param year 年
     * @param month 月
     * @param date 日期
     * @param departmentId 部门ID
     * @param customerId 客户ID
     * @param employeeId 员工ID
     * @param localWarehouseId 本地仓库ID
     * @param username 操作人名称
     * @param stockFlag 
     * @return
     */
    public void dailyKnots(String year, String month,String date, String departmentId, String customerId, String employeeId, String localWarehouseId,String username, int stockFlag) {
        Procedure procedure = new Procedure();
        procedure.setSql("ExecPOSSalesToSales");
        procedure.setVarcharParam("@y1");
        procedure.setValue("@y1", year);
        procedure.setVarcharParam("@m1");
        procedure.setValue("@m1", month);
        procedure.setVarcharParam("@d1");
        procedure.setValue("@d1", date);
        procedure.setVarcharParam("@deptid");
        procedure.setValue("@deptid", departmentId);
        procedure.setVarcharParam("@customerid");
        procedure.setValue("@customerid", customerId);
        procedure.setVarcharParam("@employeeid");
        procedure.setValue("@employeeid", employeeId);
        procedure.setVarcharParam("@LocalWarehouseID");
        procedure.setValue("@LocalWarehouseID", localWarehouseId);
        procedure.setVarcharParam("@username");
        procedure.setValue("@username", username);
        procedure.setVarcharParam("@stockFlag");
        procedure.setValue("@stockFlag", stockFlag);
        callableStatementByName(procedure);
    }
    
    /**
     * 获取指定日期的会计区间
     * @param date
     */
    public Map GetDatePeriodExt(String date) {
        Procedure procedure = new Procedure();
        procedure.setSql("GetDatePeriodExt");
        procedure.setVarcharOutParam("@Y1");
        procedure.setVarcharOutParam("@M1");
        procedure.setVarcharParam("@Date");
        procedure.setValue("@Date", date);
        Map map = callableStatementByName(procedure);
        return map;
    }



    /**
     * 格式化传入字符串
     * 
     * @param data
     * @return
     */
    public String formatString(String data) {
        if (null == data || "".equals(data)) {
            data = "";
        } else {
            data = "'" + data.replace(",", "','") + "'";
        }
        return data;
    }
}
