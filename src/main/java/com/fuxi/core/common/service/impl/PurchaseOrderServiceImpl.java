package com.fuxi.core.common.service.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fuxi.core.common.dao.impl.CommonDao;
import com.fuxi.core.common.exception.BusinessException;
import com.fuxi.core.common.service.PurchaseOrderService;
import com.fuxi.system.util.Client;
import com.fuxi.system.util.DataUtils;

@Service("purchaseOrderService")
@Transactional
public class PurchaseOrderServiceImpl implements PurchaseOrderService {
	
	@Autowired
    private CommonDao commonDao;

	@Override
	public String savePurchaseOrderX(String directionStr,
			List<Map<String, Object>> dataList, String PurchaseOrderID,
			String supplierid, String departmentid, String employeeId,
			String businessDeptId, String memo, String type, String typeEName,
			String brandId, Client client) throws Exception {
		// TODO Auto-generated method stub
		int direction =1;
    	String DiscountPrice=null;
    	String Field="";
    	String FieldValue="";
    	String sql="";
    	String UpdateStr="";
    	List<Map<String,Object>> oldlist=new ArrayList<Map<String,Object>>();//用于修改单据使用，记录原单的记录
    	List<Map<String,Object>> newlist=new ArrayList<Map<String,Object>>();//修改后新记录，最要要保存的结果
    	   // 判断PurchaseOrderID是否为空，做新增主表
        if ("".equals(PurchaseOrderID)) {
            int tag = 20;
            
            // 生成ID
            PurchaseOrderID = commonDao.getNewIDValue(tag);
            // 生成No
            String deptType = client.getDeptType();
            String No = null;
            if (deptType != null && ("直营店".equals(deptType) || "加盟店".equals(deptType))) {
                No = commonDao.getNewNOValue(tag, PurchaseOrderID, client.getDeptCode());
            } else {
                No = commonDao.getNewNOValue(tag, PurchaseOrderID);
            }
            if (PurchaseOrderID == null || No == null) {
                throw new BusinessException("生成主键/单号失败");
            }
            
            String DisplaySizeGroup="";//单据尺码组，不重复
            
            int QuantitySum=0;
            BigDecimal AmountSum =new  BigDecimal(0.0);
            BigDecimal	Discount =new  BigDecimal(0.0);
            
            for(int i=0;i<dataList.size();i++){ //每一条
               
            	Map<String, Object> map=dataList.get(i); 
            	// 获取sizIndex
                int sizIndex = getMaxSize(String.valueOf(map.get("GoodsID")));
                
               String groupid= getGroupid(String.valueOf(map.get("GoodsID")));
               
               if(DisplaySizeGroup.indexOf(groupid) ==-1){
            	   DisplaySizeGroup=DisplaySizeGroup+"'''"+groupid+"''',";
               }
               System.out.println("尺码组的:"+DisplaySizeGroup);
            
               QuantitySum =QuantitySum+Integer.parseInt(String.valueOf(map.get("Quantity")))*direction; //总数数量
               
               System.out.println("Amount:"+String.valueOf((map.get("Amount"))));
               System.out.println("Discount:"+String.valueOf((map.get("Discount"))));
               
               if(map.get("Amount") !=null && !"".equals(String.valueOf((map.get("Amount")))) && !"null".equals(String.valueOf(map.get("Amount"))) ){
            	   AmountSum= AmountSum.add(new BigDecimal(String.valueOf(map.get("Amount"))).multiply(new BigDecimal(direction))).setScale(2,BigDecimal.ROUND_DOWN);
               }
               
               if(map.get("Discount") !=null && !"".equals(String.valueOf(map.get("Discount"))) && !"null".equals(String.valueOf(map.get("Discount")))){
            	   Discount= Discount.add(new BigDecimal(String.valueOf(map.get("Discount"))).multiply(new BigDecimal(direction))).setScale(2, BigDecimal.ROUND_DOWN);
               }
               
               //AmountSum.add((map.get("Amount")==null || "".equals(map.get("Discount")))?new BigDecimal(0):new BigDecimal(String.valueOf(map.get("Amount")))).setScale(2,BigDecimal.ROUND_DOWN);
               //Discount.add((map.get("Discount")==null || "".equals(map.get("Discount"))) ?new BigDecimal(0):new BigDecimal(String.valueOf(map.get("Discount")))).setScale(2,BigDecimal.ROUND_DOWN);
            
               
               
               
            	int IndexNo=i+1;
            	List<Map<String,Object>> sizeData=(List<Map<String,Object>>)map.get("sizeData");
            	
            	
            	Field="";
            	FieldValue="";
            	//尺码数量
            	for(int j=0;j<sizeData.size();j++){
            		
            		Map<String, Object>  sizemap=	sizeData.get(j);
            		if(sizemap.get("Quantity") !=null &&  !"null".equals(String.valueOf(sizemap.get("Quantity"))) 
            		 && !"0".equals(String.valueOf(sizemap.get("Quantity"))) && !"".equals(String.valueOf(sizemap.get("Quantity")))){
            			Field=Field+String.valueOf(sizemap.get("x"))+",";
            			FieldValue=FieldValue+String.valueOf(Integer.parseInt(String.valueOf(sizemap.get("Quantity")))*direction)+",";
            			
            			System.out.println("字段名："+String.valueOf(sizemap.get("x")));
            		}
            		
            	}
            	
            	System.out.println("Field的"+Field);
            	
            	String UnitPrice =null;
            	if("".equals(map.get("UnitPrice")) || map.get("UnitPrice")==null || new BigDecimal(String.valueOf(map.get("UnitPrice"))).compareTo(BigDecimal.ZERO)  == 0){
            		UnitPrice=null;
            	}else {
            		UnitPrice =String.valueOf(map.get("UnitPrice"));
            	}
            	
            	String  DiscountRate =null;//DiscountPrice 后台算吧，因为前台 已经算好金额了
            	  if(!"".equals(map.get("DiscountRate")) && map.get("DiscountRate") !=null)
	                {
            		  DiscountRate =String.valueOf(map.get("DiscountRate"));
            		  if(UnitPrice !=null ){
            		  DiscountPrice =String.valueOf(new BigDecimal(UnitPrice).multiply(new BigDecimal(DiscountRate)).divide(new BigDecimal(10.0)).setScale(2,BigDecimal.ROUND_DOWN)) ;//自动算
            		  }
	                }
            	
            	  
            	  
            	
            	String dDiscount =null;
                if(!"".equals(map.get("Discount")) && map.get("Discount") !=null)
                {
                	dDiscount=String.valueOf(new BigDecimal(String.valueOf(map.get("Discount"))).multiply(new BigDecimal(direction)).setScale(2,BigDecimal.ROUND_DOWN));
                }
                String Amount=null;
                if(!"".equals(map.get("Amount")) && map.get("Amount") !=null)
                {
                	Amount=String.valueOf(new BigDecimal(String.valueOf(map.get("Amount"))).multiply(new BigDecimal(direction)).setScale(2,BigDecimal.ROUND_DOWN));
                }
            	String RetailSales=null;
            	
            	 if(!"".equals(map.get("RetailSales")) && map.get("RetailSales") !=null)
	                {
            		 RetailSales=String.valueOf(map.get("RetailSales"));
	                }
            
            	String RetailAmount =null;
            	 if(!"".equals(map.get("RetailAmount")) && map.get("RetailAmount") !=null)
	                {
            		 RetailAmount=String.valueOf(new BigDecimal(String.valueOf(map.get("RetailAmount"))).multiply(new BigDecimal(direction)).setScale(2,BigDecimal.ROUND_DOWN));
            				 
	                }
            	 
            	 
            	//RetailSales RetailAmount 前台算好
                sql="Insert into PurchaseOrderDetailTemp(IndexNo,PurchaseOrderID,GoodsID,ColorID,"+Field+"Quantity,UnitPrice,DiscountRate,DiscountPrice,Discount,Amount,sizeIndex,RetailSales,RetailAmount)"+
            		   "select "+IndexNo+",'"+PurchaseOrderID+"','"+String.valueOf(map.get("GoodsID"))+"','"+String.valueOf(map.get("ColorID"))+"',"
            		      +FieldValue+""+String.valueOf(Integer.parseInt(String.valueOf(map.get("Quantity")))*direction)+","+UnitPrice+","+DiscountRate+","+DiscountPrice+","+dDiscount+","+Amount+","+sizIndex+","+RetailSales+","+RetailAmount; 	
               System.out.println("sql语句："+sql);
                commonDao.executeSql(sql); //一条条写入	
               
            }
            if(!"".equals(DisplaySizeGroup)){
            	DisplaySizeGroup =DisplaySizeGroup.substring(0, DisplaySizeGroup.length()-1);
           }
           System.out.println("尺码组去掉最后一位:"+DisplaySizeGroup);
          
            
            sql ="select isnull(Sum(Quantity),0) Qty,isnull(Sum(Discount),0) DiscountSum,isnull(Sum(Amount),0) Amt,isnull(Sum(RetailAmount),0) RAmt from PurchaseOrderDetailTemp where PurchaseOrderID= ? ";
	    	    List<Map<String,Object>> ls= commonDao.findForJdbc(sql, PurchaseOrderID);
	    	    BigDecimal  DiscountSum =new BigDecimal(String.valueOf(ls.get(0).get("DiscountSum"))).setScale(2,BigDecimal.ROUND_DOWN);
	    	    BigDecimal RAmt =new BigDecimal(String.valueOf(ls.get(0).get("RAmt"))).setScale(2,BigDecimal.ROUND_DOWN);
         /*
	    	 StringBuilder insertMaster = new StringBuilder();
            insertMaster.append("insert into Sales(SalesID, No,Date ").append(" , CustomerID,DepartmentID,WarehouseID,EmployeeID,  ").append("AmountSum, QuantitySum,  MadeBy, MadeByDate, Type, memo, DiscountSum, Year, Month,  RetailAmountSum, BusinessDeptID,direction, brandId ,DisplaySizeGroup,ReceivalAmount,orderAmount,privilegeAmount,paymentTypeId)")
                    .append(" values('").append(SalesID).append("', '").append(No).append("', '" + DataUtils.str2Timestamp(DataUtils.formatDate()) + "', '").append(customerid).append("','").append(departmentid).append("', ").append(warehouseId).append(", '").append(employeeId).append("', ")
                    .append(AmountSum).append(",").append(QuantitySum).append(", '").append(client.getUserName()).append("', getdate() ").append(",'").append(type).append("','").append(memo).append("' , "+String.valueOf(DiscountSum)+", '").append(DataUtils.getYear()).append("','").append(DataUtils.getStringMonth()).append("', "+String.valueOf(RAmt)+", '")
                    .append(businessDeptId).append("',").append(direction).append(",'").append(brandId).append("',").append(DisplaySizeGroup).append(",").append(lastARAmount).append(",").append(orderAmount).append(",").append(privilegeAmount).append(",").append(paymentTypeId).append(")");
            commonDao.executeSql(insertMaster.toString());
            */
            
            
            
            
            StringBuilder insertMaster = new StringBuilder();
            insertMaster.append(" insert into PurchaseOrder(PurchaseOrderID, No,Date ").append(" , SupplierID,DepartmentID, EmployeeID,  ").append("AmountSum, QuantitySum,  MadeBy, MadeByDate, Type, memo, DiscountSum, Year, Month,  RetailAmountSum, BusinessDeptID, brandId,DisplaySizeGroup  )").append(" values('")
                    .append(PurchaseOrderID).append("', '").append(No).append("', '" + DataUtils.str2Timestamp(DataUtils.formatDate()) + "', '").append(supplierid).append("','").append(departmentid).append("', '").append(employeeId).append("', ").append(AmountSum).append(",").append(QuantitySum).append(",'")
                    .append(client.getUserName()).append("', getdate() ").append(",'").append(type).append("','").append(memo).append("' , "+DiscountSum+", '").append(DataUtils.getYear()).append("','").append(DataUtils.getStringMonth()).append("', "+RAmt+", '").append(businessDeptId).append("',")
                    .append("'").append(brandId).append("',").append(DisplaySizeGroup).append(")");
            commonDao.executeSql(insertMaster.toString());
            
        }else{ //主表id 不为空就为修改
        	//原单记录
        	oldlist =commonDao.findForJdbc("select PurchaseOrderDetailID,PurchaseOrderID,GoodsID,ColorID from PurchaseOrderDetailTemp where PurchaseOrderID =? ", PurchaseOrderID);
        	
        	
        	 int index=0;
        	 Object obj=null;
	    	  for(int i=0;i<dataList.size();i++){ //每一条
	    	      Map<String,Object> map2 =dataList.get(i);
	    		  String goodsId =String.valueOf(map2.get("GoodsID"));
	    		  String colorId =String.valueOf(map2.get("ColorID"));
	    		  String DetailID=	String.valueOf(map2.get("PurchaseOrderDetailID"));
	    		  List<Map<String,Object>> sizeData=(List<Map<String,Object>>)map2.get("sizeData");
	    		  
	    		  int sizIndex = getMaxSize(String.valueOf(map2.get("GoodsID")));
	    		  index =getMaxIndexNo(PurchaseOrderID);
	    		  index++;
	    		  obj = commonDao.getData(" select count(1) from PurchaseOrderdetailtemp where goodsId = '" + goodsId + "' and colorid  = '" + colorId + "' and PurchaseOrderID = '" + PurchaseOrderID + "' and PurchaseOrderDetailID= '"+DetailID+"'");
	    	    int count =-1;
	    		  if(obj !=null){
	    			  count =Integer.parseInt(String.valueOf(obj));
	    	      }
	    		 // String Field=""; //尺码字段名
	            //	String FieldValue=""; //数量值 
	    	
	    		  Field="";
	    		  FieldValue ="";
	    		  UpdateStr ="";
	    		  	//尺码数量
	            	for(int j=0;j<sizeData.size();j++){
	            		
	            		Map<String, Object>  sizemap=	sizeData.get(j);
	            		
	            		//只会涉及到有数量的，没有数量的尺码 不会出现 	修改就会出现0的情况，所以这里要让0的进来
	            		if(sizemap.get("Quantity") !=null &&  !"null".equals(String.valueOf(sizemap.get("Quantity")) ) 
	            		  && !"".equals(String.valueOf(sizemap.get("Quantity")))){
	            		    Field=Field+String.valueOf(sizemap.get("x"))+",";
	            		    if("".equals(sizemap.get("Quantity")) || sizemap.get("Quantity") ==null || "0".equals(String.valueOf(sizemap.get("Quantity")))){
	            		    	FieldValue =FieldValue+"null,";
	            		    	 UpdateStr=UpdateStr+String.valueOf(sizemap.get("x"))+"=null,";
	            		    }else{
	            		    FieldValue =FieldValue+String.valueOf(Integer.parseInt(String.valueOf(sizemap.get("Quantity")))*direction)+",";
	            		   UpdateStr=UpdateStr+String.valueOf(sizemap.get("x"))+"="+String.valueOf(Integer.parseInt(String.valueOf(sizemap.get("Quantity")))*direction)+",";
	            		    }
	            		   
	            		}	
	            	}   	    		  
	    		  
	    		  
	           	String UnitPrice =null;
          	if("".equals(map2.get("UnitPrice")) || map2.get("UnitPrice")==null || new BigDecimal(String.valueOf(map2.get("UnitPrice"))).compareTo(BigDecimal.ZERO)  == 0){
          		UnitPrice=null;
          	}else {
          		UnitPrice =String.valueOf(map2.get("UnitPrice"));
          	}
          	
          	String  DiscountRate =null;
          	  if(!"".equals(map2.get("DiscountRate")) && map2.get("DiscountRate") !=null)
                {
          		  DiscountRate=String.valueOf(map2.get("DiscountRate"));
          		  if(UnitPrice !=null){
	            		  DiscountPrice =String.valueOf(new BigDecimal(UnitPrice).multiply(new BigDecimal(DiscountRate)).divide(new BigDecimal(10.0)).setScale(2,BigDecimal.ROUND_DOWN)) ;//自动算
	            	 }
                }
          	
          	String dDiscount =null;
              if(!"".equals(map2.get("Discount")) && map2.get("Discount") !=null)
              {
              	dDiscount=String.valueOf(new BigDecimal(String.valueOf(map2.get("Discount"))).multiply(new BigDecimal(direction)).setScale(2,BigDecimal.ROUND_DOWN));
              }
              String Amount=null;
              if(!"".equals(map2.get("Amount")) && map2.get("Amount") !=null)
              {
              	Amount=String.valueOf(new BigDecimal(String.valueOf(map2.get("Amount"))).multiply(new BigDecimal(direction)).setScale(2,BigDecimal.ROUND_DOWN));
              }
          	String RetailSales=null;
          	
          	 if(!"".equals(map2.get("RetailSales")) && map2.get("RetailSales") !=null)
	                {
          		 RetailSales=String.valueOf(map2.get("RetailSales"));
	                }
          
          	String RetailAmount =null;
          	 if(!"".equals(map2.get("RetailAmount")) && map2.get("RetailAmount") !=null)
	                {
          		 RetailAmount=String.valueOf(new BigDecimal(String.valueOf(map2.get("RetailAmount"))).multiply(new BigDecimal(direction)).setScale(2,BigDecimal.ROUND_DOWN));
	                }
	            	
	            	
	    		  
	    	   if(count>=1){ //更新存在的一行
	            	if(!"".equals(FieldValue) && FieldValue !=null){
	            		//FieldValue =FieldValue.substring(0, FieldValue.length()-1);
	            		 sql="Update PurchaseOrderdetailtemp set "+UpdateStr+"Quantity="+String.valueOf(Integer.parseInt(String.valueOf(map2.get("Quantity")))*direction)+",UnitPrice="+UnitPrice+",Discount="+dDiscount+",DiscountRate="+DiscountRate+",Amount="+Amount+" where goodsId='"+goodsId+"' and colorid='"+colorId+"' and PurchaseOrderID = '" + PurchaseOrderID + "' and PurchaseOrderDetailID= '"+DetailID+"'";	
	            		System.out.println("sql语句："+sql);
	            		 commonDao.executeSql(sql);
	            	}   
	    	   }else{ //单据里存在就是新增
	    		   
	    
	    		   
	    		   sql="Insert into PurchaseOrderDetailTemp(IndexNo,PurchaseOrderID,GoodsID,ColorID,"+Field+"Quantity,UnitPrice,DiscountRate,DiscountPrice,Discount,Amount,sizeIndex,RetailSales,RetailAmount)"+
	            		   "select "+index+",'"+PurchaseOrderID+"','"+String.valueOf(map2.get("GoodsID"))+"','"+String.valueOf(map2.get("ColorID"))+"',"
	            		      +FieldValue+"'"+String.valueOf(Integer.parseInt(String.valueOf(map2.get("Quantity")))*direction)+"',"+UnitPrice+","+DiscountRate+","+DiscountPrice+","+dDiscount+","+Amount+","+sizIndex+","+RetailSales+","+RetailAmount+""; 	
	               commonDao.executeSql(sql);
	    		      
	    	   } 	
	    		//把要删除的 goodsid,colorid 记录下来
	    	   //salesid ,goodsid,colorid 判断 是否还存在单据     此都是已存在的   放在一个表里面 上面已经写入就会有  SalesDetailID
	    	   sql="select  PurchaseOrderDetailID from PurchaseOrderDetailTemp where PurchaseOrderID='"+PurchaseOrderID+"' and GoodsID ='"+String.valueOf(map2.get("GoodsID"))+"' and ColorID='"+String.valueOf(map2.get("ColorID"))+"'";
               Map<String,Object> nmap=new LinkedHashMap<>();
               nmap.put("PurchaseOrderDetailID",commonDao.getData(sql)); 
               newlist.add(nmap);
	    	  }//list 结束    	
        	
	    	  for(int i=0;i<newlist.size();i++){
	    		  	 for(int j=0;j<oldlist.size();j++){
	    		    if(newlist.get(i).get("PurchaseOrderDetailID").equals(oldlist.get(j).get("PurchaseOrderDetailID"))){//把有的 的的排除掉，因为要保留 删除的,新增的不会在旧的list里面
	    		    	oldlist.remove(j);
	    		    	j--;
	    			  }
	    		  }
	    		  
	    	  }
	    	  //oldlist  处理后，只剩下要删除的了
	    	  for(int i=0;i<oldlist.size();i++){
	    		  commonDao.executeSql("delete from PurchaseOrderDetailTemp where PurchaseOrderDetailID = '"+String.valueOf(oldlist.get(i).get("PurchaseOrderDetailID"))+"' and PurchaseOrderID= '"+PurchaseOrderID+"'");
	    	  }
	    	  
	    	  	 //总和
	    	   sql ="select isnull(Sum(Quantity),0) Qty,isnull(Sum(Amount),0) Amt,isnull(Sum(RetailAmount),0) RAmt from PurchaseOrderDetailTemp where PurchaseOrderID= ? ";
	    	   List<Map<String,Object>> ls= commonDao.findForJdbc(sql, PurchaseOrderID);
	    	   sql ="Update PurchaseOrder set QuantitySum="+String.valueOf(ls.get(0).get("Qty"))+",AmountSum="+String.valueOf(ls.get(0).get("Amt")) +",RetailAmountSum="+String.valueOf(ls.get(0).get("RAmt"))+",displaySizeGroup =(SELECT STUFF((select DISTINCT ','''+g.GroupID +'''' from PurchaseOrderDetailTemp a JOIN goods g ON a.goodsid=g.goodsid WHERE PurchaseOrderID='" + PurchaseOrderID + "' FOR XML PATH('')),1,1,'')) where PurchaseOrderID='"+PurchaseOrderID+"'";
	    	   commonDao.executeSql(sql);
        	
        }
    	
    	
    	
    	return PurchaseOrderID;
	}
	 //获取尺码组ID
	private String getGroupid(String goodsid){
		
	
	String sql = "select groupId from goods where goodsId = ?";
	Map<String,Object> m= (Map) commonDao.findForJdbc(sql, goodsid).get(0);
	String 	groupId =String.valueOf(m.get("groupId"));
	
	return 	groupId;
	} 
	  // 获取尺码组中的最大尺码
    private int getMaxSize(String goodsId) {
        String maxSizeSql = "select max(no) as maxsize from SizeGroupSize where sizeGroupId = (select groupId from goods where goodsId = ?)";
        Map sizeMap = (Map) commonDao.findForJdbc(maxSizeSql, goodsId).get(0);
        int maxSize = (Integer) sizeMap.get("maxsize");
        if (maxSize < 1) {
            maxSize = 1;
        }
        return maxSize;
    }
    // 获取最大的下标
    private int getMaxIndexNo(String PurchaseID) {
        int IndexNo = 0;
        StringBuffer maxNoSql = new StringBuffer();
        maxNoSql.append(" select max(IndexNo) IndexNo from PurchaseOrderDetailtemp where PurchaseOrderId =  '").append(PurchaseID).append("'");
        List rsList = commonDao.findForJdbc(maxNoSql.toString());
        if (rsList.size() > 0) {
            if (((Map) rsList.get(0)).get("IndexNo") != null) {
                IndexNo = (Integer) ((Map) rsList.get(0)).get("IndexNo");
            }
        }
        return IndexNo;
    }
}
