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
import com.fuxi.core.common.service.GoodsInfoService;
import com.fuxi.system.util.Client;
import com.fuxi.system.util.DataUtils;

/**
 * Title: GoodsInfoServiceImpl Description: 货品业务逻辑接口实现类
 * 
 * @author LYJ
 * 
 */
@Service("goodsInfoService")
@Transactional
public class GoodsInfoServiceImpl implements GoodsInfoService {

    @Autowired
    private CommonDao commonDao;

    @Override
    public String saveGoodsInfoMsg(String goodsCode, String goodsName, String goodsTypeId, String goodsSubType, String brandId, String brandSerialId, String kind, String age, String season, String supplierId, String supplierCode, String purchasePrice, String tradePrice, String retailSales,
            String retailSales1, String retailSales2, String retailSales3, String salesPrice1, String salesPrice2, String salesPrice3, String colorId1, String colorId2, String colorId3, String colorId4, String colorId5, String colorId6, String colorId7, String colorId8, Client client) {
        // 检查货品编码是否存在
        boolean flag = checkGoodsCode(goodsCode);
        if (flag) {
            throw new BusinessException("货品编码重复");
        }
        StringBuffer sql = new StringBuffer();
        // 生成货品ID
        String goodsId = commonDao.getNewIDValue(4);
        if (goodsId == null) {
            throw new BusinessException("生成货品ID失败");
        }
        // 获取尺码组信息
        Map<String, Object> map = getGroupIDAndNoByID(goodsTypeId);
        String groupId = null;
        String groupNo = null;
        if (map == null) {
            throw new BusinessException("获取尺码组ID失败");
        } else {
            groupId = String.valueOf(map.get("SizeGroupID"));
            groupNo = String.valueOf(map.get("No"));
        }
        // 获取尺码组中最大的尺码编号
        int maxSizeNo = getMaxSizeNo(groupId);
        // 获取助记码
        String helpCode = String.valueOf(commonDao.getData("select [dbo].[F_GetPY](?)", goodsName));
        // 发送SQL
        sql.append(" insert into goods(GoodsID, GoodsTypeID, Code, Name, BrandID, Age, Season, ").append(" BrandSerialID, Kind, HelpCode, SupplierID, SupplierCode, PurchasePrice,").append(" RetailSales, TradePrice, SalesPrice1, SalesPrice2, SalesPrice3, DiscountFlag, ")
                .append(" MadeDate, Creator, ModifyDate, Editor, GroupNo, GroupID, RetailSales1, RetailSales2, RetailSales3, ").append(" Designer, GoodsAudit, GoodsAuditDate, SubType, PointRate, IsNew, MaxSizeNo) ")
                .append(" values(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) ");
        int count =
                commonDao.executeSql(sql.toString(), goodsId, goodsTypeId, goodsCode, goodsName, brandId, age, season, brandSerialId, kind, helpCode, supplierId, supplierCode, purchasePrice, retailSales, tradePrice, salesPrice1, salesPrice2, salesPrice3, 1, DataUtils.gettimestamp(),
                        client.getUserName(), DataUtils.gettimestamp(), client.getUserName(), groupNo, groupId, retailSales1, retailSales2, retailSales3, client.getUserID(), client.getUserName(), DataUtils.str2Timestamp(DataUtils.formatDate()), goodsSubType, 1.00, 1, maxSizeNo);
        if (count < 1) {
            throw new BusinessException("货品资料保存失败");
        }
        // 插入货品颜色
        List<String> colors = new ArrayList<String>();
        colors.add(colorId1);
        colors.add(colorId2);
        colors.add(colorId3);
        colors.add(colorId4);
        colors.add(colorId5);
        colors.add(colorId6);
        colors.add(colorId7);
        colors.add(colorId8);
        try {
            addGoodsColor(colors, goodsId, client.getUserName());
        } catch (Exception e) {
            throw new BusinessException("货品颜色重复");
        }
        return goodsId;
    }

    private boolean checkGoodsCode(String goodsCode) {
        int exit = Integer.parseInt(String.valueOf(commonDao.getData(" select count(1) from goods where code = ? ", goodsCode)));
        return exit > 0 ? true : false;
    }

    // 通过货品类别ID获取货品尺码组
    private Map<String, Object> getGroupIDAndNoByID(String goodsTypeId) {
        Map<String, Object> map = null;
        List datas = commonDao.findForJdbc(" select SizeGroupID,No from SizeGroup where SizeGroupID = (select SizeGroupID from goodstype where goodstypeId = ?) ", goodsTypeId);
        if (datas.size() > 0) {
            map = (Map<String, Object>) datas.get(0);
        }
        return map;
    }

    // 获取尺码组中最大的尺码
    private int getMaxSizeNo(String groupId) {
        int no = Integer.parseInt(String.valueOf(commonDao.getData(" select max(no) from SizeGroupSize where SizeGroupID = ? ", groupId)));
        return no;
    }

    private int addGoodsColor(String goodsId, String colorId, String userName) {
        int count = commonDao.executeSql(" insert into GoodsColor(GoodsID,ColorID,Editor,ModifyDate) values(?,?,?,?) ", goodsId, colorId, userName, DataUtils.gettimestamp());
        return count;
    }

    private void addGoodsColor(List<String> colors, String goodsId, String userName) throws Exception {
        for (int i = 0; i < colors.size(); i++) {
            if (colors.get(i) != null && !colors.get(i).isEmpty() && !"null".equalsIgnoreCase(colors.get(i))) {
                addGoodsColor(goodsId, colors.get(i), userName);
            }
        }
    }

    @Override
    public int updateGoodsInfoMsg(String goodsId, String goodsSubType, String brandId, String brandSerialId, String kind, String age, String season, String supplierId, String supplierCode, String purchasePrice, String tradePrice, String retailSales, String retailSales1, String retailSales2,
            String retailSales3, String salesPrice1, String salesPrice2, String salesPrice3, String colorId1, String colorId2, String colorId3, String colorId4, String colorId5, String colorId6, String colorId7, String colorId8, Client client) {
        StringBuffer sql = new StringBuffer();
        sql.append(" update goods set BrandID = ?, Age = ?, Season = ?, BrandSerialID = ?, Kind = ?, SupplierID = ?, SupplierCode = ?, PurchasePrice = ?,").append(" RetailSales = ?, TradePrice = ?, SalesPrice1 = ?, SalesPrice2 = ?, SalesPrice3 = ?, ModifyDate = ?, Editor = ?,")
                .append(" RetailSales1 = ?, RetailSales2 = ?, RetailSales3 = ?, SubType = ? where goodsId = ? ");
        // 数据处理

        int count =
                commonDao.executeSql(sql.toString(), brandId, age, season, brandSerialId, kind, supplierId, supplierCode, purchasePrice, retailSales, tradePrice, salesPrice1, salesPrice2, salesPrice3, DataUtils.gettimestamp(), client.getUserName(), retailSales1, retailSales2, retailSales3,
                        goodsSubType, goodsId);
        // 插入货品颜色
        List<String> colors = new ArrayList<String>();
        colors.add(colorId1);
        colors.add(colorId2);
        colors.add(colorId3);
        colors.add(colorId4);
        colors.add(colorId5);
        colors.add(colorId6);
        colors.add(colorId7);
        colors.add(colorId8);
        try {
            addGoodsColor(colors, goodsId, client.getUserName());
        } catch (Exception e) {
            throw new BusinessException("货品颜色重复");
        }
        return count;
    }

    @Override //此是返回货品的所有颜色的，不是只有货品 一个货品可能有多条记录  此方法还有价格 根据客户订单类型返回
	public List<Map<String, Object>> goodslist(String Code,String Type, String CustomerID,int currpage,int pagesize) {
		//代替货品的折折扣，客户的折扣  ,默认为0.0
		StringBuffer sb=new StringBuffer();
		String TypeStr ="PriceType";
		String DiscountRateStr="DiscountRate";
		if(Type=="批发"){
		  TypeStr ="PriceType";
		  DiscountRateStr="DiscountRate";
		}else if(Type=="订货"){
		 TypeStr ="OrderPriceType";
		 DiscountRateStr="OrderDiscount";
		}else if(Type=="配货"){
			 TypeStr ="AllotPriceType";		
			 DiscountRateStr="AllotDiscount";
		}else if(Type=="补货"){
			 TypeStr ="ReplenishType";	
			 DiscountRateStr="ReplenishDiscount";
		}
		String sql="select CustomerID,Customer,"+TypeStr+" PriceType , "+DiscountRateStr+" DiscountRate  from Customer where CustomerID = ? ";
	     
	   List<Map<String,Object>> cust=	commonDao.findForJdbc(sql,CustomerID);
	   
	   System.out.println("价格类型："+String.valueOf(cust.get(0).get("PriceType")));
	   System.out.println("折扣："+String.valueOf(cust.get(0).get("DiscountRate")));
	   
	   String PriceType="零售价";
	   if(!"".equals(String.valueOf(cust.get(0).get("PriceType"))) && cust.get(0).get("PriceType") !=null && !"null".equals(String.valueOf(cust.get(0).get("PriceType")))){
		   PriceType =String.valueOf(cust.get(0).get("PriceType"));  //零售价，批发价
	   }
	        //返回真实字段
	   
	  String PriceField ="RetailSales";
	  sql ="select  dbo.GetCustPriceTypeOfFieldName('"+PriceType+"') PriceType";
	  
	  List<Map<String,Object>> custPriceType=	commonDao.findForJdbc(sql);
	  
	  System.out.println("价格类型字段："+String.valueOf(custPriceType.get(0).get("PriceType")));
	  
	  if(custPriceType.get(0).get("PriceType") !=null && !"null".equals(custPriceType.get(0).get("PriceType")) && !"".equals(String.valueOf(custPriceType.get(0).get("PriceType")))){
		  PriceField =String.valueOf(custPriceType.get(0).get("PriceType"));
	  }
		String DiscountRate="0";
		
		if(cust.get(0).get("DiscountRate") !=null && !"".equals(String.valueOf(cust.get(0).get("DiscountRate"))) && !"null".equalsIgnoreCase(String.valueOf(cust.get(0).get("DiscountRate")))){
			DiscountRate =String.valueOf(cust.get(0).get("DiscountRate"));
		}
		
		
		
		if(!"".equals(Code) && Code !=null){//GoodsID in('000DY','000DZ','000E0')")
		sb.append("select GoodsID,Code,Name,GroupID,RetailSales,"+PriceField+" UnitPrice, Discount=0.0,DiscountRate="+DiscountRate+", Quantity=0,Amount=0  from Goods where (Code like '%"+Code+"%' or Name like '%"+Code+"%')");
		}else{
		sb.append("select GoodsID,Code,Name,GroupID,RetailSales,"+PriceField+" UnitPrice,Discount=0.0,DiscountRate="+DiscountRate+",Quantity=0,Amount=0  from Goods ");		
		}
		List<Map<String, Object>> ls =commonDao.findForJdbc(sb.toString(), currpage, pagesize);
		
		List<Map<String, Object>> datalist=new ArrayList<>();//用于返回
	
		//货品颜色
		for(int i=0;i< ls.size(); i++){
			Map<String,Object> map= ls.get(i);
			
			sql="select a.GoodsID,a.ColorID,c.Color from GoodsColor a join Color c on a.ColorID=c.ColorID where a.GoodsID =? ";
			List<Map<String, Object>> color=commonDao.findForJdbc(sql,String.valueOf(map.get("GoodsID")));
			for(int j =0;j<color.size();j++){ //第个颜色包三个属性 一个颜色 一个map
				
				Map<String,Object> datamap=new LinkedHashMap<>();
				
				Map<String,Object> m2=color.get(j);
				datamap.put("GoodsID", String.valueOf(map.get("GoodsID")));
				datamap.put("Code", String.valueOf(map.get("Code")));
				datamap.put("Name", String.valueOf(map.get("Name")));
				datamap.put("GroupID", String.valueOf(map.get("GroupID")));
				datamap.put("RetailSales", new BigDecimal(String.valueOf(map.get("RetailSales"))).setScale(2,BigDecimal.ROUND_DOWN));
				datamap.put("Discount", new BigDecimal(String.valueOf(map.get("Discount"))).setScale(2,BigDecimal.ROUND_DOWN));
				if(!"".equals(String.valueOf(map.get("UnitPrice"))) && map.get("UnitPrice") !=null && !"null".equals(String.valueOf(map.get("UnitPrice"))))
				{	
				datamap.put("UnitPrice", new BigDecimal(String.valueOf(map.get("UnitPrice"))).setScale(2,BigDecimal.ROUND_DOWN));
				}else{
					datamap.put("UnitPrice","");
				}
				if(!"".equals(String.valueOf(map.get("DiscountRate"))) && map.get("DiscountRate") !=null && !"null".equals(String.valueOf(map.get("DiscountRate"))))
				{
				datamap.put("DiscountRate", new BigDecimal(String.valueOf(map.get("DiscountRate"))).setScale(2,BigDecimal.ROUND_DOWN));
				}else{
					datamap.put("DiscountRate","");	
				}
				
				 System.out.println("货号："+String.valueOf(datamap.get("Code"))+"\t"+"单价："
				 +String.valueOf(datamap.get("UnitPrice"))+"\t"+"折扣："
				 + String.valueOf(datamap.get("DiscountRate"))	 
				 );
				
				if(String.valueOf(map.get("Quantity")).equals("0")){
					datamap.put("Quantity", "");
				}else{
					datamap.put("Quantity", String.valueOf(map.get("Quantity")));
				}
				if(String.valueOf(map.get("Amount")).equals("0")){
					datamap.put("Amount", "");
				}else{
					datamap.put("Amount",  new BigDecimal(String.valueOf(map.get("Amount"))).setScale(2,BigDecimal.ROUND_DOWN));
				}
				
				
				datamap.put("ColorID", String.valueOf(m2.get("ColorID")));
				datamap.put("Color", String.valueOf(m2.get("Color")));
				datamap.put("img", "");
				
				List<Map<String, Object>> sizetitle =new ArrayList<>();
				List<Map<String, Object>> sizeData =new ArrayList<>();
				List<Map<String, Object>> right =new ArrayList<>();
				//--------------颜色----------------
				
				sql="select * from SizeGroupSize where SizeGroupID = ?";
				List<Map<String, Object>> sizels=commonDao.findForJdbc(sql,String.valueOf(map.get("GroupID")));
				for(int k=0;k< sizels.size();k++){ 
				
					Map<String,Object> m3=sizels.get(k);
				
					Map<String,Object> m4=new LinkedHashMap<String,Object>();//sizeData 的map
					Map<String,Object> st=new LinkedHashMap<String,Object>();
					st.put("field", "x_"+String.valueOf(m3.get("No")));
					st.put("title", String.valueOf(m3.get("Size")));
					sizetitle.add(st);
					//----------显示尺码列--------------
					//--------数据---------
					m4.put("GoodsID", String.valueOf(map.get("GoodsID")));
					m4.put("ColorID", String.valueOf(datamap.get("ColorID")));
					m4.put("Color", String.valueOf(datamap.get("Color")));
					m4.put("x", "x_"+String.valueOf(m3.get("No")));
					
					m4.put("Quantity", "");
				
					
					if(!"".equals(String.valueOf(datamap.get("UnitPrice"))) && datamap.get("UnitPrice") !=null && !"null".equals(String.valueOf(datamap.get("UnitPrice"))))
					{	
					m4.put("UnitPrice", new BigDecimal(String.valueOf(datamap.get("UnitPrice"))).setScale(2,BigDecimal.ROUND_DOWN));
					
					}else{
						m4.put("UnitPrice","");
					}
					if(!"".equals(String.valueOf(datamap.get("DiscountRate"))) && datamap.get("DiscountRate") !=null && !"null".equals(String.valueOf(datamap.get("DiscountRate"))))
					{
						m4.put("DiscountRate", new BigDecimal(String.valueOf(datamap.get("DiscountRate"))).setScale(2,BigDecimal.ROUND_DOWN));
						
					}else{
						m4.put("DiscountRate","");	
					}
					
					
					
					m4.put("SizeID", String.valueOf(m3.get("SizeID")));
					m4.put("Size", String.valueOf(m3.get("Size")));
					m4.put("Amount", "");
					sizeData.add(m4);
				}
				
				for(int n=0;n<2;n++){
					Map<String,Object> m5=new LinkedHashMap<String,Object>();
					m5.put("text", "删除");
					m5.put("onPress", "() => {"+
	                                  "  modal.toast({ "+
	                                  "      message: '删除',"+
	                                  "      duration: 0.3 "+
	                                  "  });"+
	                                "}");
					m5.put("style", "{ backgroundColor: '#F4333C', color: 'white' }");
					right.add(m5);
				}
				
				datamap.put("sizetitle", sizetitle);
				datamap.put("sizeData", sizeData);
				datamap.put("right", right);
				
				datalist.add(datamap);
			}
		
			
		
			
		
			
			
		}
	
		
		
		
		return datalist;
	}
    
    
    
    
    

}
