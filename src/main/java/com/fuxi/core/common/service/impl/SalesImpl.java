package com.fuxi.core.common.service.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fuxi.core.common.dao.impl.CommonDao;
import com.fuxi.core.common.exception.BusinessException;
import com.fuxi.core.common.service.SalesService;
import com.fuxi.system.util.Client;
import com.fuxi.system.util.DataUtils;
import com.fuxi.system.util.ResourceUtil;

/**
 * Title: SalesImpl Description: 销售发(退)货业务逻辑接口实现类
 * 
 * @author LJ,LYJ
 * 
 */
@Service("publicService")
@Transactional
public class SalesImpl implements SalesService {

    @Autowired
    private CommonDao commonDao;

    @Override
    public synchronized String saveSales(String directionStr, List<Map<String, Object>> dataList, String SalesID, String customerid, String departmentid, String employeeId, String businessDeptId, String memo, String type, String typeEName, String brandId, String discountRateSumStr,
            String lastARAmountStr, String orderAmountStr, String privilegeAmountStr, String paymentTypeIdStr, Client client) throws Exception {
        // boolean stockFlag =
        // Boolean.valueOf(String.valueOf(commonDao.getData(" select StockFlag from Customer where CustomerId = ? ",
        // customerid)));
        String warehouseId = null;
        String result = String.valueOf(commonDao.getData(" select top 1 departmentId from department where SettleCustID is not null and SettleCustID <> '' and SettleCustID = ? ", customerid));
        if (result != null && !"".equals(result) && !"null".equalsIgnoreCase(result)) {
            warehouseId = "'" + result + "'";
        }
        int direction = Integer.parseInt(directionStr);
        String No = null;
        String deptCode = String.valueOf(commonDao.getData(" select Code  from Department where DepartmentID = ? ", departmentid));
        // 判断SalesID是否为空，做新增主表
        if ("".equals(SalesID)) {
            int tag = 0;
            if (direction == -1) {
                tag = 97;
            } else {
                tag = 30;
            }
            // 生成ID
            SalesID = commonDao.getNewIDValue(tag);
            // 生成No
            String deptType = client.getDeptType();
            if (deptType != null && ("直营店".equals(deptType) || "加盟店".equals(deptType))) {
                No = commonDao.getNewNOValue(tag, SalesID, client.getDeptCode());
            } else {
                No = commonDao.getNewNOValue(tag, SalesID);
            }
            if (SalesID == null || No == null) {
                throw new BusinessException("生成主键/单号失败");
            }
            StringBuilder insertMaster = new StringBuilder();
            insertMaster.append("insert into Sales(SalesID, No,Date ").append(" , CustomerID,DepartmentID,WarehouseID,EmployeeID,  ").append("AmountSum, QuantitySum,  MadeBy, MadeByDate, Type, memo, DiscountSum, Year, Month,  RetailAmountSum, BusinessDeptID,direction, brandId  )")
                    .append(" values('").append(SalesID).append("', '").append(No).append("', '" + DataUtils.str2Timestamp(DataUtils.formatDate()) + "', '").append(customerid).append("','").append(departmentid).append("', ").append(warehouseId).append(", '").append(employeeId).append("', ")
                    .append("100").append(", null").append(", '").append(client.getUserName()).append("', getdate() ").append(",'").append(type).append("','").append(memo).append("' , null, '").append(DataUtils.getYear()).append("','").append(DataUtils.getStringMonth()).append("', null, '")
                    .append(businessDeptId).append("',").append(direction).append(",'").append(brandId).append("' )");
            commonDao.executeSql(insertMaster.toString());
        }

        // -----------------其它操作------------------//
        // 获取同货品,同颜色的总数量
        for (int i = 0; i < dataList.size(); i++) {
            Map map = dataList.get(i);
            String sizeStr = String.valueOf(map.get("SizeStr"));
            int quantitySum = 0;
            if (sizeStr != null && !"".equals(sizeStr) && !"null".equalsIgnoreCase(sizeStr)) {
                for (int j = 0; j < dataList.size(); j++) {
                    Map temp = dataList.get(j);
                    if (String.valueOf(temp.get("GoodsID")).equals(String.valueOf(map.get("GoodsID"))) && String.valueOf(temp.get("ColorID")).equals(String.valueOf(map.get("ColorID"))) && String.valueOf(temp.get("SizeStr")).equals(sizeStr)) {
                        int count = Integer.parseInt(String.valueOf(temp.get("Quantity")));
                        quantitySum += count;
                    }
                }
            } else {
                for (int j = 0; j < dataList.size(); j++) {
                    Map temp = dataList.get(j);
                    if (String.valueOf(temp.get("GoodsID")).equals(String.valueOf(map.get("GoodsID"))) && String.valueOf(temp.get("ColorID")).equals(String.valueOf(map.get("ColorID")))) {
                        int count = Integer.parseInt(String.valueOf(temp.get("Quantity")));
                        quantitySum += count;
                    }
                }
            }
            map.put("QuantitySum", quantitySum);
        }
        // 整单折扣
        BigDecimal discountRateSum = null;
        if (null == discountRateSumStr || "".equals(discountRateSumStr) || "null".equalsIgnoreCase(discountRateSumStr)) {
            discountRateSum = null;
        } else {
            discountRateSum = new BigDecimal(discountRateSumStr);
        }
        // 应收金额
        BigDecimal lastARAmount = null;
        if (null == lastARAmountStr || "".equals(lastARAmountStr) || "null".equalsIgnoreCase(lastARAmountStr)) {
            lastARAmount = null;
        } else {
            lastARAmount = new BigDecimal(lastARAmountStr);
        }
        // 应收金额
        BigDecimal orderAmount = null;
        if (null == orderAmountStr || "".equals(orderAmountStr) || "null".equalsIgnoreCase(orderAmountStr)) {
            orderAmount = null;
        } else {
            orderAmount = new BigDecimal(orderAmountStr);
        }
        // 优惠金额
        BigDecimal privilegeAmount = null;
        if (null == privilegeAmountStr || "".equals(privilegeAmountStr) || "null".equalsIgnoreCase(privilegeAmountStr)) {
            privilegeAmount = null;
        } else {
            privilegeAmount = new BigDecimal(privilegeAmountStr);
        }
        // 结算方式
        String paymentTypeId = null;
        if (null != paymentTypeIdStr && !"".equals(paymentTypeIdStr) && !"null".equalsIgnoreCase(paymentTypeIdStr)) {
            paymentTypeId = "'" + paymentTypeIdStr + "'";
        }
        // 获取最大的下标值
        int maxIndexNo = getMaxIndexNo(SalesID);
        // 循环查询或更新
        dataList = mergeData(dataList);
        for (int i = 0; i < dataList.size(); i++) {
            Map firstMap = (Map) dataList.get(i);
            String goodsId = String.valueOf(firstMap.get("GoodsID"));
            String colorId = String.valueOf(firstMap.get("ColorID"));
            String sizeId = String.valueOf(firstMap.get("SizeID"));
            String meno = null;
            if (firstMap.containsKey("meno")) {
                meno = String.valueOf(firstMap.get("meno")).trim();
            }
            int quantity = Integer.parseInt(String.valueOf(firstMap.get("Quantity")));
            int quantitySum = Integer.parseInt(String.valueOf(firstMap.get("QuantitySum")));
            String x = String.valueOf(commonDao.getData("select no from sizegroupsize where sizegroupid = (select GroupID from goods where GoodsID = ?) and SizeId = '" + sizeId + "' ; ", goodsId));
            BigDecimal UnitPrice = firstMap.get("UnitPrice") == null ? null : new BigDecimal(String.valueOf(firstMap.get("UnitPrice"))).setScale(2, BigDecimal.ROUND_HALF_UP);
            BigDecimal RetailSales = firstMap.get("RetailSales") == null ? null : new BigDecimal(String.valueOf(firstMap.get("RetailSales"))).setScale(2, BigDecimal.ROUND_HALF_UP);
            BigDecimal DiscountPrice = firstMap.get("DiscountPrice") == null ? null : new BigDecimal(String.valueOf(firstMap.get("DiscountPrice"))).setScale(2, BigDecimal.ROUND_HALF_UP);
            BigDecimal DiscountRate = firstMap.get("DiscountRate") == null ? null : new BigDecimal(String.valueOf(firstMap.get("DiscountRate"))).setScale(2, BigDecimal.ROUND_HALF_UP);
            String sizeStr = String.valueOf(firstMap.get("SizeStr"));
            // 获取sizIndex
            int sizIndex = getMaxSize(goodsId);
            // 计算折扣
            if (UnitPrice != null && UnitPrice.compareTo(BigDecimal.ZERO) != 0 && DiscountPrice != null && DiscountPrice.compareTo(BigDecimal.ZERO) != 0) {
                DiscountRate = DiscountPrice.divide(UnitPrice, BigDecimal.ROUND_CEILING, 2).multiply(new BigDecimal(10));
                if (null != DiscountRate && DiscountRate.compareTo(BigDecimal.ZERO) == 0) {
                    DiscountRate = null;
                }
            }
            // 判断货品是否有单价
            if (DiscountPrice != null && DiscountPrice.compareTo(BigDecimal.ZERO) != 0 && UnitPrice != null && UnitPrice.compareTo(BigDecimal.ZERO) == 0) {
                UnitPrice = DiscountPrice;
            }
            // 判断折扣价和单价是否相等
            if (DiscountPrice != null && UnitPrice != null && UnitPrice.compareTo(DiscountPrice) == 0) {
                DiscountPrice = null;
                DiscountRate = null;
            }
            // 箱数
            String boxQtyStr = String.valueOf(firstMap.get("BoxQty"));
            if (null == boxQtyStr || "".equals(boxQtyStr) || "null".equalsIgnoreCase(boxQtyStr)) {
                boxQtyStr = "0";
            }
            Integer boxQty = Integer.parseInt(boxQtyStr);
            if (null == boxQty || boxQty == 0) {
                boxQty = null;
            }
            // 每箱的件数
            Integer oneBoxQty = Integer.parseInt(String.valueOf(firstMap.get("OneBoxQty") == null ? "0" : firstMap.get("OneBoxQty")));
            if (null == oneBoxQty || oneBoxQty == 0) {
                oneBoxQty = null;
            }

            Object obj = null;
            // 判断是新增还是更新
            if (null != boxQty && 0 != boxQty) {
                obj = commonDao.getData(" select count(1) from salesdetailtemp where goodsId = '" + goodsId + "' and colorid  = '" + colorId + "' and salesid = '" + SalesID + "' and sizeStr = '" + sizeStr + "' ");
            } else {
                obj = commonDao.getData(" select count(1) from salesdetailtemp where goodsId = '" + goodsId + "' and colorid  = '" + colorId + "' and salesid = '" + SalesID + "' ");
            }
            int count = -1;
            if (null != obj) {
                count = Integer.parseInt(String.valueOf(obj));
            }
            // ----------------------------------------//
            // --------------插入子表数据-----------------//
            // ----------------------------------------//
            if (count < 1) {
                maxIndexNo++;
                StringBuffer sql = new StringBuffer();
                sql.append(" Insert into SalesDetailTemp(IndexNo,SalesID,GoodsID,ColorID,x_").append(x).append(",Quantity,UnitPrice,DiscountPrice,DiscountRate,RetailSales,Amount,RetailAmount,Discount,BoxQty,SizeStr,SizeIndex,memo) Values(").append(maxIndexNo).append(",'").append(SalesID)
                        .append("', '").append(goodsId).append("','").append(colorId).append("' ");
                if (null == x || "".equals(x) || "null".equalsIgnoreCase(x)) {
                    sql.append(",null ");
                } else {
                    if (null != boxQty && 0 != boxQty) {
                        sql.append(",").append(quantity / boxQty);
                    } else {
                        sql.append(",").append(quantity);
                    }
                }
                sql.append(", ").append(quantity).append(", ").append(UnitPrice);
                // 折扣价
                if (null == DiscountPrice || DiscountPrice.compareTo(BigDecimal.ZERO) == 0) {
                    sql.append(", null ");
                } else {
                    sql.append(", ").append(DiscountPrice);
                }
                sql.append(", ").append(DiscountRate).append(", ").append(RetailSales);
                // 实收金额
                if (null != DiscountPrice && DiscountPrice.compareTo(BigDecimal.ZERO) != 0) {
                    sql.append(", ").append(quantity).append("*").append(DiscountPrice);
                } else {
                    sql.append(", ").append(quantity).append("*").append(UnitPrice);
                }
                // 零售金额
                if (RetailSales == null) {
                    sql.append(", null ");
                } else {
                    sql.append(",  ").append(quantity).append("*").append(RetailSales);
                }
                // 折扣额
                if (null == DiscountPrice || DiscountPrice.compareTo(BigDecimal.ZERO) == 0 || RetailSales.compareTo(UnitPrice) == 0 || UnitPrice.compareTo(DiscountPrice) == 0) {// 无折扣
                    sql.append(",  null ");
                } else {
                    sql.append(",  (isnull(").append(UnitPrice).append(",0)-isnull(").append(DiscountPrice).append(",0))*").append(quantity);
                }
                // 箱数
                sql.append(",").append(boxQty).append(",");
                // 配码
                if (null != boxQty && 0 != boxQty) {
                    sql.append("'").append(sizeStr).append("'");
                } else {
                    sql.append("null");
                }
                sql.append(",").append(sizIndex).append(",");
                // 备注
                if (meno != null && !"".equals(meno) && !"null".equalsIgnoreCase(meno)) {
                    sql.append("'").append(meno).append("'");
                } else {
                    sql.append("null");
                }
                sql.append(") ");
                commonDao.executeSql(sql.toString());
            } else {
                // ----------------------------------------//
                // ------------修改子表数据-------------------//
                // ----------------------------------------//
                // 判断客户对应的货品是否存在价格
                // String priceColumn = getTypeColumn(customerid, typeEName);
                // double uprice = Double.valueOf(String.valueOf(commonDao
                // .getData(" select isnull(" + priceColumn
                // + ",0) from goods g where goodsId = ? ",
                // goodsId)));
                // if (DiscountPrice != null
                // && DiscountPrice.compareTo(BigDecimal.ZERO) != 0
                // && uprice <= 0) {// 该用户对应的货品没有单价
                // UnitPrice = DiscountPrice;
                // DiscountPrice = null;
                // DiscountRate = null;
                // }
                StringBuffer sql = new StringBuffer();
                sql.append(" Update SalesDetailTemp set  UnitPrice =   ").append(UnitPrice).append(", RetailSales = ").append(RetailSales).append(",BoxQty = ").append(boxQty).append(",DiscountPrice = ");
                if (null == DiscountPrice || DiscountPrice.compareTo(BigDecimal.ZERO) == 0) {
                    sql.append(" null ");
                } else {
                    sql.append(DiscountPrice);
                }
                sql.append(", DiscountRate = ").append(DiscountRate);

                if (null != boxQty && 0 != boxQty) {
                    sql.append(", x_").append(x).append(" =  ").append(quantity / boxQty);
                } else {
                    sql.append(", x_").append(x).append(" =  ").append(quantity);
                }

                sql.append(", Quantity=").append(quantitySum);
                // 实收金额
                if (UnitPrice == null && DiscountPrice == null) {
                    sql.append(" , Amount=null ");
                } else {
                    if (null != DiscountPrice && DiscountPrice.compareTo(BigDecimal.ZERO) != 0) {
                        sql.append(",Amount =  ").append(quantitySum).append("*").append(DiscountPrice);
                    } else {
                        sql.append(",Amount =  ").append(quantitySum).append("*").append(UnitPrice);
                    }
                }
                // 零售金额合计
                if (RetailSales == null) {
                    sql.append(", RetailAmount = null ");
                } else {
                    sql.append(", RetailAmount =  ").append(quantitySum).append("*").append(RetailSales);
                }
                // 折扣额
                if (DiscountPrice == null) {
                    sql.append(" , Discount = null ");
                } else {
                    sql.append(" , Discount =  (isnull(").append(UnitPrice).append(",0)-isnull(").append(DiscountPrice).append(",0))").append("*").append(quantitySum);
                }
                // 备注
                if (meno != null && !"".equals(meno) && !"null".equalsIgnoreCase(meno)) {
                    sql.append(" , memo = '" + meno + "' ");
                } else {
                    sql.append(" , memo = null ");
                }
                sql.append(", SizeIndex = ").append(sizIndex).append(" where SalesID = '").append(SalesID).append("' and GoodsID = '").append(goodsId).append("' ");
                if (null != boxQty && 0 != boxQty) {
                    sql.append(" and SizeStr = '").append(sizeStr).append("' ");
                }
                sql.append(" and ColorID = '").append(colorId).append("' ");
                commonDao.executeSql(sql.toString());
            }
        }
        // 更新主表信息
        StringBuilder sumSql = new StringBuilder();
        sumSql.append(" Update Sales  set QuantitySum = t.QtySum,AmountSum = t.AmountSum, discountRateSum = " + discountRateSum + ", ReceivalAmount = " + lastARAmount + ",orderAmount = " + orderAmount + ",privilegeAmount = " + privilegeAmount + ",paymentTypeId = " + paymentTypeId + ", ")
                .append("  RetailAmountSum = t.RetailAmountSum, DiscountSum = t.DiscountSum ,displaySizeGroup = ").append(" (SELECT STUFF((select DISTINCT ','''+g.GroupID +'''' from SalesDetailTemp a JOIN goods g ON a.goodsid=g.goodsid ")
                .append(" WHERE salesid='" + SalesID + "' FOR XML PATH('')),1,1,''))  ").append(" from  (Select SUM(Quantity) QtySum,Sum(Amount) AmountSum, Sum(RetailAmount) RetailAmountSum,Sum(Discount) DiscountSum  ").append("  from SalesDetailTemp  where SalesID ='").append(SalesID)
                .append("'   ) t  ").append(" where SalesID = '").append(SalesID).append("' ");
        commonDao.executeSql(sumSql.toString());
        if (lastARAmount != null && lastARAmount.compareTo(BigDecimal.ZERO) != 0 && direction != -1) {
            // 收款金额不为空的时候生成收款单
            String receivalType = "货款";
            BigDecimal arAmount = new BigDecimal(String.valueOf(commonDao.getData(" select AmountSum from sales where salesId = ? ", SalesID)));
            generalReceival(deptCode, customerid, departmentid, employeeId, paymentTypeIdStr, arAmount, lastARAmount, No, SalesID, brandId, receivalType, client);
        }
        if (privilegeAmount != null && privilegeAmount.compareTo(BigDecimal.ZERO) != 0 && direction != -1) {
            // 优惠金额不为空的时候生成收款单
            String receivalType = "优惠";
            BigDecimal arAmount = new BigDecimal(String.valueOf(commonDao.getData(" select AmountSum from sales where salesId = ? ", SalesID)));
            generalReceival(deptCode, customerid, departmentid, employeeId, paymentTypeIdStr, arAmount, privilegeAmount, No, SalesID, brandId, receivalType, client);
        }
        if (orderAmount != null && orderAmount.compareTo(BigDecimal.ZERO) != 0 && direction != -1) {
            // 优惠金额不为空的时候生成收款单
            String receivalType = "订金";
            BigDecimal arAmount = new BigDecimal(String.valueOf(commonDao.getData(" select AmountSum from sales where salesId = ? ", SalesID)));
            generalReceival(deptCode, customerid, departmentid, employeeId, paymentTypeIdStr, arAmount, orderAmount, No, SalesID, brandId, receivalType, client);
        }
        return SalesID;
    }

	    @Override  //2019-07-11 新写
    public synchronized String saveSalesX2(String directionStr, List<Map<String, Object>> dataList, String  SalesID, String customerid, String departmentid, String employeeId, String businessDeptId, String memo, String type, String typeEName, String brandId, String discountRateSumStr,
            String lastARAmountStr, String orderAmountStr, String privilegeAmountStr, String paymentTypeIdStr, Client client) throws Exception
            {
    	 String warehouseId = null;
    	 String deptCode = String.valueOf(commonDao.getData(" select Code  from Department where DepartmentID = ? ", departmentid));
    	 String Field=""; //尺码字段名
     	 String FieldValue="",UpdateStr="", sql=""; //数量值
     	 int index=0;
     	 
     	  // 整单折扣
         BigDecimal discountRateSum = null;
         if (null == discountRateSumStr || "".equals(discountRateSumStr) || "null".equalsIgnoreCase(discountRateSumStr)) {
             discountRateSum = null;
         } else {
             discountRateSum = new BigDecimal(discountRateSumStr);
         }
         // 应收金额
         BigDecimal lastARAmount = null;
         if (null == lastARAmountStr || "".equals(lastARAmountStr) || "null".equalsIgnoreCase(lastARAmountStr)) {
             lastARAmount = null;
         } else {
             lastARAmount = new BigDecimal(lastARAmountStr);
         }
         // 应收金额
         BigDecimal orderAmount = null;
         if (null == orderAmountStr || "".equals(orderAmountStr) || "null".equalsIgnoreCase(orderAmountStr)) {
             orderAmount = null;
         } else {
             orderAmount = new BigDecimal(orderAmountStr);
         }
         // 优惠金额
         BigDecimal privilegeAmount = null;
         if (null == privilegeAmountStr || "".equals(privilegeAmountStr) || "null".equalsIgnoreCase(privilegeAmountStr)) {
             privilegeAmount = null;
         } else {
             privilegeAmount = new BigDecimal(privilegeAmountStr);
         }
         // 结算方式
         String paymentTypeId = null;
         if (null != paymentTypeIdStr && !"".equals(paymentTypeIdStr) && !"null".equalsIgnoreCase(paymentTypeIdStr)) {
             paymentTypeId = "'" + paymentTypeIdStr + "'";
         }
    	 
         String result = String.valueOf(commonDao.getData(" select top 1 departmentId from department where SettleCustID is not null and SettleCustID <> '' and SettleCustID = ? ", customerid));
         if (result != null && !"".equals(result) && !"null".equalsIgnoreCase(result)) {
             warehouseId = "'" + result + "'";
         }
         int direction = Integer.parseInt(directionStr);
         String No = null;
         
         System.out.println("iml的ID"+SalesID);
    	     if("".equals(SalesID) || SalesID ==null){//主表的 判断为新增单据
    	    	 
    	    	 System.out.println("进入ID为空的");
    	    	 
    	    	   int tag = 0;
    	            if (direction == -1) {
    	                tag = 97;
    	            } else {
    	                tag = 30;
    	            }
    	            // 生成ID
    	            SalesID = commonDao.getNewIDValue(tag);
    	            // 生成No
    	            String deptType = client.getDeptType();
    	            if (deptType != null && ("直营店".equals(deptType) || "加盟店".equals(deptType))) {
    	                No = commonDao.getNewNOValue(tag, SalesID, client.getDeptCode());
    	            } else {
    	                No = commonDao.getNewNOValue(tag, SalesID);
    	            }
    	            if (SalesID == null || No == null) {
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
    	            
    	               QuantitySum =QuantitySum+Integer.parseInt(String.valueOf(map.get("Quantity"))); //总数数量
    	               
    	               System.out.println("Amount:"+String.valueOf((map.get("Amount"))));
    	               System.out.println("Discount:"+String.valueOf((map.get("Discount"))));
    	               
    	               if(map.get("Amount") !=null && !"".equals(String.valueOf((map.get("Amount")))) && !"null".equals(String.valueOf(map.get("Amount"))) ){
    	            	   AmountSum.add(new BigDecimal(String.valueOf(map.get("Amount")))).setScale(2,BigDecimal.ROUND_DOWN);
    	               }
    	               
    	               if(map.get("Discount") !=null && !"".equals(String.valueOf(map.get("Discount"))) && !"null".equals(String.valueOf(map.get("Discount")))){
    	            	   Discount.add(new BigDecimal(String.valueOf(map.get("Discount")))).setScale(2, BigDecimal.ROUND_DOWN);
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
    	            			FieldValue=FieldValue+String.valueOf(sizemap.get("Quantity"))+",";
    	            			
    	            			System.out.println("字段名："+String.valueOf(sizemap.get("x")));
    	            		}
    	            		
    	            	}
    	            	
    	            	System.out.println("Field的"+Field);
    	            	
    	            	String UnitPrice =null;
    	            	if("".equals(map.get("UnitPrice")) || map.get("UnitPrice")==null){
    	            		UnitPrice=null;
    	            	}else {
    	            		UnitPrice =String.valueOf(map.get("UnitPrice"));
    	            	}
    	            	
    	            	String  DiscountRate =null;
    	            	  if(!"".equals(map.get("DiscountRate")) && map.get("DiscountRate") !=null)
      	                {
    	            		  DiscountRate=String.valueOf(map.get("DiscountRate"));
      	                }
    	            	
    	            	String dDiscount =null;
    	                if(!"".equals(map.get("Discount")) && map.get("Discount") !=null)
    	                {
    	                	dDiscount=String.valueOf(map.get("Discount"));
    	                }
    	                String Amount=null;
    	                if(!"".equals(map.get("Amount")) && map.get("Amount") !=null)
    	                {
    	                	Amount=String.valueOf(map.get("Amount"));
    	                }
    	            	String RetailSales=null;
    	            	
    	            	 if(!"".equals(map.get("RetailSales")) && map.get("RetailSales") !=null)
       	                {
    	            		 RetailSales=String.valueOf(map.get("RetailSales"));
       	                }
    	            
    	            	String RetailAmount =null;
    	            	 if(!"".equals(map.get("RetailAmount")) && map.get("RetailAmount") !=null)
        	                {
    	            		 RetailAmount=String.valueOf(map.get("RetailAmount"));
        	                }
    	            	 
    	            	 
    	            	//RetailSales RetailAmount 前台算好
    	                sql="Insert into SalesDetailTemp(IndexNo,SalesID,GoodsID,ColorID,"+Field+"Quantity,UnitPrice,DiscountRate,Discount,Amount,sizeIndex,RetailSales,RetailAmount)"+
    	            		   "select "+IndexNo+",'"+SalesID+"','"+String.valueOf(map.get("GoodsID"))+"','"+String.valueOf(map.get("ColorID"))+"',"
    	            		      +FieldValue+""+String.valueOf(map.get("Quantity"))+","+UnitPrice+","+DiscountRate+","+dDiscount+","+Amount+","+sizIndex+","+RetailSales+","+RetailAmount; 	
    	               commonDao.executeSql(sql); //一条条写入	
    	               
    	            }
    	            if(!"".equals(DisplaySizeGroup)){
	                	DisplaySizeGroup =DisplaySizeGroup.substring(0, DisplaySizeGroup.length()-1);
	               }
	               System.out.println("尺码组去掉最后一位:"+DisplaySizeGroup);
	              
    	            
    	            sql ="select isnull(Sum(Quantity),0) Qty,isnull(Sum(Discount),0) DiscountSum,isnull(Sum(Amount),0) Amt,isnull(Sum(RetailAmount),0) RAmt from SalesDetailTemp where SalesID= ? ";
     	    	    List<Map<String,Object>> ls= commonDao.findForJdbc(sql, SalesID);
     	    	    BigDecimal  DiscountSum =new BigDecimal(String.valueOf(ls.get(0).get("DiscountSum"))).setScale(2,BigDecimal.ROUND_DOWN);
     	    	    BigDecimal RAmt =new BigDecimal(String.valueOf(ls.get(0).get("RAmt"))).setScale(2,BigDecimal.ROUND_DOWN);
    	            StringBuilder insertMaster = new StringBuilder();
    	            insertMaster.append("insert into Sales(SalesID, No,Date ").append(" , CustomerID,DepartmentID,WarehouseID,EmployeeID,  ").append("AmountSum, QuantitySum,  MadeBy, MadeByDate, Type, memo, DiscountSum, Year, Month,  RetailAmountSum, BusinessDeptID,direction, brandId ,DisplaySizeGroup,ReceivalAmount,orderAmount,privilegeAmount,paymentTypeId)")
    	                    .append(" values('").append(SalesID).append("', '").append(No).append("', '" + DataUtils.str2Timestamp(DataUtils.formatDate()) + "', '").append(customerid).append("','").append(departmentid).append("', ").append(warehouseId).append(", '").append(employeeId).append("', ")
    	                    .append(AmountSum).append(",").append(QuantitySum).append(", '").append(client.getUserName()).append("', getdate() ").append(",'").append(type).append("','").append(memo).append("' , "+String.valueOf(DiscountSum)+", '").append(DataUtils.getYear()).append("','").append(DataUtils.getStringMonth()).append("', "+String.valueOf(RAmt)+", '")
    	                    .append(businessDeptId).append("',").append(direction).append(",'").append(brandId).append("',").append(DisplaySizeGroup).append(",").append(lastARAmount).append(",").append(orderAmount).append(",").append(privilegeAmount).append(",").append(paymentTypeId).append(")");
    	            commonDao.executeSql(insertMaster.toString());
    	            
    	            if (lastARAmount != null && lastARAmount.compareTo(BigDecimal.ZERO) != 0 && direction != -1) {
    	                // 收款金额不为空的时候生成收款单
    	                String receivalType = "货款";
    	                BigDecimal arAmount = new BigDecimal(String.valueOf(commonDao.getData(" select AmountSum from sales where salesId = ? ", SalesID)));
    	                generalReceival(deptCode, customerid, departmentid, employeeId, paymentTypeIdStr, arAmount, lastARAmount, No, SalesID, brandId, receivalType, client);
    	            }
    	            if (privilegeAmount != null && privilegeAmount.compareTo(BigDecimal.ZERO) != 0 && direction != -1) {
    	                // 优惠金额不为空的时候生成收款单
    	                String receivalType = "优惠";
    	                BigDecimal arAmount = new BigDecimal(String.valueOf(commonDao.getData(" select AmountSum from sales where salesId = ? ", SalesID)));
    	                generalReceival(deptCode, customerid, departmentid, employeeId, paymentTypeIdStr, arAmount, privilegeAmount, No, SalesID, brandId, receivalType, client);
    	            }
    	            if (orderAmount != null && orderAmount.compareTo(BigDecimal.ZERO) != 0 && direction != -1) {
    	                // 优惠金额不为空的时候生成收款单
    	                String receivalType = "订金";
    	                BigDecimal arAmount = new BigDecimal(String.valueOf(commonDao.getData(" select AmountSum from sales where salesId = ? ", SalesID)));
    	                generalReceival(deptCode, customerid, departmentid, employeeId, paymentTypeIdStr, arAmount, orderAmount, No, SalesID, brandId, receivalType, client);
    	            }
    	            
    	            
    	     }else{//修改   这里涉及到没有 的，就要删除货品
    	    	 Object obj = null;String temptable="tempdb..SalesTemp";
    	    	 
    	    	 sql ="if not Exists(select name from tempdb..SysObjects where Name ='SalesTemp') Create table "+temptable+
    	    		  "(SalesDetailID varchar(50),UserID varchar(30))";
    	    	
    	    	  commonDao.executeSql(sql);
    	    	  
    	    	 
    	    	  
    	    	  commonDao.executeSql("delete from tempdb..SalesTemp where UserID='"+client.getUserID()+"'"); // 删除之前的
    	    	 // commonDao.executeSql("truncate table tempdb..SalesTemp"); //清表 Client
     
    	    	 
    	    	  for(int i=0;i<dataList.size();i++){ //每一条
    	    	      Map<String,Object> map2 =dataList.get(i);
    	    		  String goodsId =String.valueOf(map2.get("GoodsID"));
    	    		  String colorId =String.valueOf(map2.get("ColorID"));
    	    		  String DetailID=	String.valueOf(map2.get("SalesDetailID"));
    	    		  List<Map<String,Object>> sizeData=(List<Map<String,Object>>)map2.get("sizeData");
    	    		  
    	    		  int sizIndex = getMaxSize(String.valueOf(map2.get("GoodsID")));
    	    		  index =getMaxIndexNo(SalesID);
    	    		  
    	    		  obj = commonDao.getData(" select count(1) from salesdetailtemp where goodsId = '" + goodsId + "' and colorid  = '" + colorId + "' and salesid = '" + SalesID + "' and SalesDetailID= '"+DetailID+"'");
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
     	            		    FieldValue =FieldValue+String.valueOf(sizemap.get("Quantity"))+",";
     	            		   UpdateStr=UpdateStr+String.valueOf(sizemap.get("x"))+"="+String.valueOf(sizemap.get("Quantity"))+",";
     	            		    }
     	            		   
     	            		}	
     	            	}   	    		  
    	    		  
    	    		  
     	           	String UnitPrice =null;
	            	if("".equals(map2.get("UnitPrice")) || map2.get("UnitPrice")==null){
	            		UnitPrice=null;
	            	}else {
	            		UnitPrice =String.valueOf(map2.get("UnitPrice"));
	            	}
	            	
	            	String  DiscountRate =null;
	            	  if(!"".equals(map2.get("DiscountRate")) && map2.get("DiscountRate") !=null)
  	                {
	            		  DiscountRate=String.valueOf(map2.get("DiscountRate"));
  	                }
	            	
	            	String dDiscount =null;
	                if(!"".equals(map2.get("Discount")) && map2.get("Discount") !=null)
	                {
	                	dDiscount=String.valueOf(map2.get("Discount"));
	                }
	                String Amount=null;
	                if(!"".equals(map2.get("Amount")) && map2.get("Amount") !=null)
	                {
	                	Amount=String.valueOf(map2.get("Amount"));
	                }
	            	String RetailSales=null;
	            	
	            	 if(!"".equals(map2.get("RetailSales")) && map2.get("RetailSales") !=null)
   	                {
	            		 RetailSales=String.valueOf(map2.get("RetailSales"));
   	                }
	            
	            	String RetailAmount =null;
	            	 if(!"".equals(map2.get("RetailAmount")) && map2.get("RetailAmount") !=null)
    	                {
	            		 RetailAmount=String.valueOf(map2.get("RetailAmount"));
    	                }
     	            	
     	            	
    	    		  
    	    	   if(count>=1){ //更新存在的一行
   	            	if(!"".equals(FieldValue) && FieldValue !=null){
   	            		//FieldValue =FieldValue.substring(0, FieldValue.length()-1);
   	            		 sql="Update salesdetailtemp set "+UpdateStr+"Quantity="+String.valueOf(map2.get("Quantity"))+",UnitPrice="+UnitPrice+",Discount="+dDiscount+",DiscountRate="+DiscountRate+",Amount="+Amount+" where goodsId='"+goodsId+"' and colorid='"+colorId+"' and salesid = '" + SalesID + "' and SalesDetailID= '"+DetailID+"'";	
   	            		System.out.println("sql语句："+sql);
   	            		 commonDao.executeSql(sql);
   	            	}   
    	    	   }else{ //单据里存在就是新增
    	    		   
    	    
    	    		   
    	    		   sql="Insert into SalesDetailTemp(IndexNo,SalesID,GoodsID,ColorID,"+Field+"Quantity,UnitPrice,DiscountRate,Discount,Amount,sizeIndex,RetailSales,RetailAmount)"+
    	            		   "select "+index+",'"+SalesID+"','"+String.valueOf(map2.get("GoodsID"))+"','"+String.valueOf(map2.get("ColorID"))+"',"
    	            		      +FieldValue+"'"+String.valueOf(map2.get("Quantity"))+"',"+UnitPrice+","+DiscountRate+","+dDiscount+","+Amount+","+sizIndex+","+RetailSales+","+RetailAmount+""; 	
    	               commonDao.executeSql(sql);
    	    		      
    	    	   } 	
    	    		
    	    	   //salesid ,goodsid,colorid 判断 是否还存在单据     此都是已存在的   放在一个表里面 上面已经写入就会有  SalesDetailID
    	    	   sql="Insert into "+temptable+"(UserID,SalesDetailID)"+
    	    	       "select '"+client.getUserID()+"', SalesDetailID from SalesDetailTemp where SalesID='"+SalesID+"' and GoodsID ='"+String.valueOf(map2.get("GoodsID"))+"' and ColorID='"+String.valueOf(map2.get("ColorID"))+"'";
    	    	   commonDao.executeSql(sql); 
    	    	  }//list 结束    	 
    	    	 //不存在 的删除 ，存在的保留 SalesDetailID唯一，不重复
    	    	  sql="delete a from SalesDetailTemp a where a.SalesID='"+SalesID+"' and not Exists(select SalesDetailID from "+temptable+" b where a.SalesDetailID=b.SalesDetailID and b.UserID='"+client.getUserID()+"' )";
    	    	  System.out.println("删除语句："+sql);
    	    	  commonDao.executeSql(sql); 
    	    	  
    	    	 //总和
    	    	   sql ="select isnull(Sum(Quantity),0) Qty,isnull(Sum(Amount),0) Amt,isnull(Sum(RetailAmount),0) RAmt from SalesDetailTemp where SalesID= ? ";
    	    	   List<Map<String,Object>> ls= commonDao.findForJdbc(sql, SalesID);
    	    	   sql ="Update Sales set QuantitySum="+String.valueOf(ls.get(0).get("Qty"))+",AmountSum="+String.valueOf(ls.get(0).get("Amt")) +",RetailAmountSum="+String.valueOf(ls.get(0).get("RAmt"))+",displaySizeGroup =(SELECT STUFF((select DISTINCT ','''+g.GroupID +'''' from SalesDetailTemp a JOIN goods g ON a.goodsid=g.goodsid WHERE salesid='" + SalesID + "' FOR XML PATH('')),1,1,'')) where SalesID='"+SalesID+"'";
    	    	   commonDao.executeSql(sql);
    	    	   
    	    	   if (lastARAmount != null && lastARAmount.compareTo(BigDecimal.ZERO) != 0 && direction != -1) {
    	              
    	    		   sql ="update sales set ReceivalAmount ="+lastARAmount+",PaymentTypeID="+paymentTypeId+" where salesid = '"+SalesID+"'";
    	    		   System.out.println("更新收款语句："+sql);
    	    	    	  commonDao.executeSql(sql);
    	    		   // 收款金额不为空的时候生成收款单
    	               String receivalType = "货款";
    	               BigDecimal arAmount = new BigDecimal(String.valueOf(commonDao.getData(" select AmountSum from sales where salesId = ? ", SalesID)));
    	               generalReceival(deptCode, customerid, departmentid, employeeId, paymentTypeIdStr, arAmount, lastARAmount, No, SalesID, brandId, receivalType, client);
    	           }
    	           if (privilegeAmount != null && privilegeAmount.compareTo(BigDecimal.ZERO) != 0 && direction != -1) {
    	               // 优惠金额不为空的时候生成收款单
    	               String receivalType = "优惠";
    	               BigDecimal arAmount = new BigDecimal(String.valueOf(commonDao.getData(" select AmountSum from sales where salesId = ? ", SalesID)));
    	               generalReceival(deptCode, customerid, departmentid, employeeId, paymentTypeIdStr, arAmount, privilegeAmount, No, SalesID, brandId, receivalType, client);
    	           }
    	           if (orderAmount != null && orderAmount.compareTo(BigDecimal.ZERO) != 0 && direction != -1) {
    	               // 优惠金额不为空的时候生成收款单
    	               String receivalType = "订金";
    	               BigDecimal arAmount = new BigDecimal(String.valueOf(commonDao.getData(" select AmountSum from sales where salesId = ? ", SalesID)));
    	               generalReceival(deptCode, customerid, departmentid, employeeId, paymentTypeIdStr, arAmount, orderAmount, No, SalesID, brandId, receivalType, client);
    	           }
    	    	 
    	     }
    	    
    	     return SalesID;
            }
	
	
	 //获取尺码组ID
	private String getGroupid(String goodsid){
		
	
	String sql = "select groupId from goods where goodsId = ?";
	Map<String,Object> m= (Map) commonDao.findForJdbc(sql, goodsid).get(0);
	String 	groupId =String.valueOf(m.get("groupId"));
	
	return 	groupId;
	} 
	    
	    
	    
	    
	
    // 获取尺码
    private String getSizeStr(int maxSize) {
        StringBuffer sizeStr = new StringBuffer();
        for (int i = 1; i <= maxSize; i++) {
            sizeStr.append(",x_").append(i);
        }
        return sizeStr.toString();
    }

    // 获取尺码
    private String getSizeStrSum(int maxSize) {
        StringBuffer sizeStr = new StringBuffer();
        for (int i = 1; i <= maxSize; i++) {
            sizeStr.append("+sum(isnull(x_").append(i).append(",0))");
        }
        return sizeStr.toString();
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
    private int getMaxIndexNo(String SalesID) {
        int IndexNo = 0;
        StringBuffer maxNoSql = new StringBuffer();
        maxNoSql.append(" select max(IndexNo) IndexNo from SalesDetailtemp where salesId =  '").append(SalesID).append("'");
        List rsList = commonDao.findForJdbc(maxNoSql.toString());
        if (rsList.size() > 0) {
            if (((Map) rsList.get(0)).get("IndexNo") != null) {
                IndexNo = (Integer) ((Map) rsList.get(0)).get("IndexNo");
            }
        }
        return IndexNo;
    }

    // 删除发货单数量和价格
    @Override
    public void deleteSalesdetail(List<Map<String, Object>> dataList, String SalesID) throws Exception {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < dataList.size(); i++) {
            Map map = (Map) dataList.get(i);
            String goodsId = String.valueOf(map.get("GoodsID"));
            String colorId = String.valueOf(map.get("ColorID"));
            String sizeId = String.valueOf(map.get("SizeID"));
            String oneBoxQtyStr = String.valueOf(map.get("OneBoxQty"));
            String sizeStr = String.valueOf(map.get("SizeStr"));
            // 查询尺码对应的尺码组Code
            String x = String.valueOf(commonDao.getData("select no from sizegroupsize where sizegroupid = (select GroupID from goods where GoodsID = ?) and SizeId = '" + sizeId + "' ; ", goodsId));
            // 获取原来同单,同货,同码,同色,的货品数量,明细ID,尺码数量
            List list = null;
            if (null != sizeStr && !"".equals(sizeStr) && !"null".equals(sizeStr)) {
                list =
                        commonDao.findForJdbc(" select sdt.salesdetailId,isnull(x_" + x + ",0) xCount,isnull(sdt.Quantity,0) counts,isnull(boxQty,0) boxQty from SalesDetailTemp sdt with(updlock) "
                                + " join salesdetail sd with(updlock) on sdt.salesId = sd.salesId and sdt.goodsId = sd.goodsId and sdt.colorId = sd.colorId where sdt.salesid = '" + SalesID + "' and sdt.goodsId = '" + goodsId + "' and sdt.colorId = '" + colorId + "' and sdt.sizeStr = '" + sizeStr
                                + "' ; ");
            } else {
                list =
                        commonDao.findForJdbc(" select sdt.salesdetailId,isnull(x_" + x + ",0) xCount,isnull(sdt.Quantity,0) counts,isnull(boxQty,0) boxQty from SalesDetailTemp sdt with(updlock) "
                                + " join salesdetail sd with(updlock) on sdt.salesId = sd.salesId and sdt.goodsId = sd.goodsId and sdt.colorId = sd.colorId where sdt.salesid = '" + SalesID + "' and sdt.goodsId = '" + goodsId + "' and sdt.colorId = '" + colorId + "' ; ");
            }
            if (list.size() > 0) {
                Map dataMap = (Map) list.get(0);
                int xCount = 0, count = 0, boxQty = 0;
                String salesdetailId = null;
                if (null != dataMap) {
                    xCount = Integer.parseInt(String.valueOf(dataMap.get("xCount")));
                    count = Integer.parseInt(String.valueOf(dataMap.get("counts")));
                    boxQty = Integer.parseInt(String.valueOf(dataMap.get("boxQty")));
                    salesdetailId = String.valueOf(dataMap.get("salesdetailId"));
                } else {
                    throw new BusinessException("修改货品数量或价格失败");
                }
                if (Math.abs(boxQty) > 0) {// 箱条码
                    // int oneBoxQty = 1;
                    // if (null != oneBoxQtyStr && !oneBoxQtyStr.isEmpty()) {
                    // oneBoxQty = Integer.parseInt(oneBoxQtyStr);
                    // }
                    // 删除明细
                    // if(count == oneBoxQty){//只有一个尺码
                    sb.append(" delete from SalesDetailTemp where salesdetailId = '").append(salesdetailId).append("' ; delete from SalesDetail where GoodsID = '").append(goodsId).append("' and SalesID = '").append(SalesID).append("' and ColorID = '" + colorId + "'  ; ");
                    // }else{
                    // sb.append(" update SalesDetailTemp set x_"+x+" = null,Quantity = ").append((count-(xCount*boxQty)))
                    // .append(" ,amount = (isnull(DiscountPrice,UnitPrice)*(").append((count-(xCount*boxQty)))
                    // .append(")),Discount =  (isnull(UnitPrice,0)-isnull(DiscountPrice,0))").append("*").append(count-(xCount*boxQty))
                    // .append(",RetailAmount = (RetailSales*(").append(count-(xCount*boxQty)).append(")) where salesdetailId = '")
                    // .append(salesdetailId).append("' ; update SalesDetail set Quantity = "+(count-(xCount*boxQty))+" where GoodsID = '")
                    // .append(goodsId).append("' and SalesID = '").append(SalesID).append("' and ColorID = '"+colorId+"'  ; ");
                    // }
                } else {// 散件
                        // 删除明细
                    if (count == xCount) {// 只有一个尺码
                        sb.append(" delete from SalesDetailTemp where salesdetailId = '").append(salesdetailId).append("' ; delete from SalesDetail where GoodsID = '").append(goodsId).append("' and SalesID = '").append(SalesID)
                                .append("' and ColorID = '" + colorId + "' and SizeID = '" + sizeId + "' ; ");
                    } else {
                        sb.append(" update SalesDetailTemp set x_" + x + " = null,Quantity = ").append(count - xCount).append(" ,amount = (isnull(DiscountPrice,UnitPrice)*(").append(count - xCount).append(")),Discount =  (isnull(UnitPrice,0)-isnull(DiscountPrice,0))").append("*")
                                .append(count - xCount).append(",RetailAmount = (RetailSales*(").append(count - xCount).append(")) where salesdetailId = '").append(salesdetailId).append("' ; update SalesDetail set Quantity = " + (count - xCount) + " where GoodsID = '").append(goodsId)
                                .append("' and SalesID = '").append(SalesID).append("' and ColorID = '" + colorId + "' and SizeID = '" + sizeId + "' ; ");
                    }
                }
                commonDao.executeSql(sb.toString());
                // 判断明细表是否还有记录
                String sizeStrSum = getSizeStrSum(getMaxSize(goodsId));
                String countStr = String.valueOf(commonDao.getData("select isnull(0" + sizeStrSum + ",0) from SalesDetailTemp where salesdetailId = ? ", salesdetailId));
                if (countStr != null && !countStr.isEmpty() && !"null".equalsIgnoreCase(countStr) && Integer.parseInt(countStr) == 0) {
                    commonDao.executeSql(" delete from SalesDetailTemp where salesdetailId = ? ", salesdetailId);
                }
                sb = new StringBuilder();
            }
        }
        // 重新计算货品价格
        sb.append(" update Sales set DiscountSum =(select sum(Discount) from Salesdetailtemp where SalesID = '").append(SalesID).append("' ) , AmountSum = (select sum(Amount) from Salesdetailtemp where SalesID = '").append(SalesID)
                .append("'), RetailAmountSum = (select sum(RetailAmount) from Salesdetailtemp where SalesID = '").append(SalesID).append("'), QuantitySum = (select sum(Quantity) from Salesdetailtemp where SalesID = '").append(SalesID).append("') where SalesID = '").append(SalesID).append("' ;");
        commonDao.executeSql(sb.toString());
        // 若主表无数据时则删除主表
        List list = commonDao.findForJdbc(" select SalesID from Sales where quantitysum is null ");
        for (int i = 0; i < list.size(); i++) {
            Map map = (Map) list.get(i);
            String salesId = String.valueOf(map.get("SalesID"));
            commonDao.executeSql(" delete from Sales where SalesID = ? ", salesId);
        }
    }

    @Override
    public void generalReceival(String deptCode, String customerId, String departmentId, String employeeId, String paymentTypeId, BigDecimal arAmount, BigDecimal receivalAmount, String relationNo, String relationId, String brandId, String type, Client client) throws Exception {
        // 先删除原来关联的收款单
        commonDao.executeSql(" delete from  Receival where receivalId = (select ReceivalID from ReceivalInvoiceDetail where SalesID = ?)  ", relationId);
        // 生成ID
        String receivalId = commonDao.getNewIDValue(34);
        // 生成No
        String receivalNo = commonDao.getNewNOValue(34, receivalId, deptCode);
        if (receivalId == null || receivalNo == null) {
            throw new BusinessException("生成主键/单号失败");
        }
        // 生成主表
        StringBuilder sb = new StringBuilder();
        sb.append(" Insert Into Receival (ReceivalID,No,Type,Date,CustomerID,DepartmentID,EmployeeID,PaymentTypeID,ReceivalAmount," + "MadeBy,MadeByDate,Audit,AuditDate,AuditFlag,Memo,Year,Month,RelationID,ValidBeginDate,BusinessDeptID,BrandID)"
                + " values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)  ");
        commonDao.executeSql(sb.toString(), receivalId, receivalNo, type, DataUtils.str2Timestamp(DataUtils.formatDate()), customerId, departmentId, employeeId, paymentTypeId, receivalAmount, client.getUserName(), DataUtils.gettimestamp(), null, null, 0, "销售单" + relationNo + "生成",
                DataUtils.getYear(), DataUtils.getStringMonth(), relationId, DataUtils.str2Timestamp(DataUtils.formatDate()), client.getDeptID(), brandId);
        // 生成子表
        // 生成ID
        String receivalInvoiceDetailId = commonDao.getNewIDValue(86);
        StringBuilder sql = new StringBuilder();
        sql.append(" Insert into ReceivalInvoiceDetail (ReceivalInvoiceDetailID,ReceivalID,SalesID,No,ARAmount,ReceivedAmount,RSAmount," + "ReceivalAmount,TypeInt) values(?,?,?,?,?,?,?,?,?)  ");
        if ("优惠".equals(type)) {
            commonDao.executeSql(sql.toString(), receivalInvoiceDetailId, receivalId, relationId, relationNo, arAmount, receivalAmount, receivalAmount, receivalAmount, 0);
        } else if ("货款".equals(type)) {
            commonDao.executeSql(sql.toString(), receivalInvoiceDetailId, receivalId, relationId, relationNo, arAmount, null, (arAmount.subtract(receivalAmount)), receivalAmount, 0);
        } else if ("订金".equals(type)) {
        }
    }

    // 获取货品对应的客户的价格
    private String getTypeColumn(String customerId, String type) {
        String column = null;
        StringBuffer sb = new StringBuffer();
        sb.append("select ").append(type).append(" from customer where customerId = '").append(customerId).append("'");
        String data = String.valueOf(commonDao.getData(sb.toString()));
        if (null == data || "".equals(data) || "null".equals(data)) {
            return null;
        }
        if (data.contains("零售价")) {
            if ("零售价".equals(data)) {
                column = "RetailSales";
            } else if ("零售价2".equals(data)) {
                column = "RetailSales1";
            } else {
                column = "RetailSales" + (Integer.parseInt(data.substring(data.length() - 1)) - 1);
            }
        } else if (data.contains("批发价")) {
            if ("批发价".equals(data)) {
                column = "TradePrice";
            } else if ("批发价2".equals(data)) {
                column = "SalesPrice1";
            } else {
                column = "SalesPrice" + (Integer.parseInt(data.substring(data.length() - 1)) - 1);
            }
        }
        column = "g." + column;
        return column;
    }

    private String getType(String typeName) {
        String type = null;
        if ("批发".equals(typeName)) {
            type = "PriceType";
        } else if ("订货".equals(typeName)) {
            type = "OrderPriceType";
        } else if ("配货".equals(typeName)) {
            type = "AllotPriceType";
        } else if ("补货".equals(typeName)) {
            type = "ReplenishType";
        }
        return type;
    }

    // 去除集合中的重复记录
    public List<Map<String, Object>> mergeData(List<Map<String, Object>> dataList) {
        for (int i = 0; i < dataList.size() - 1; i++) {
            Map temp1 = (Map) dataList.get(i);
            String sizeStr = String.valueOf(temp1.get("SizeStr"));
            if (sizeStr != null && !"".equals(sizeStr) && !"null".equalsIgnoreCase(sizeStr)) {
                for (int j = dataList.size() - 1; j > i; j--) {
                    Map temp2 = (Map) dataList.get(j);
                    if (temp1.get("GoodsID").equals(temp2.get("GoodsID")) && temp1.get("ColorID").equals(temp2.get("ColorID")) && temp1.get("SizeID").equals(temp2.get("SizeID")) && sizeStr.equals(String.valueOf(temp2.get("SizeStr")))) {
                        Map map = new HashMap();
                        int count1 = Integer.parseInt(String.valueOf(temp1.get("Quantity")));
                        int count2 = Integer.parseInt(String.valueOf(temp2.get("Quantity")));
                        int boxQty1 = Integer.parseInt(String.valueOf(temp1.get("BoxQty")).equalsIgnoreCase("null") ? "0" : String.valueOf(temp1.get("BoxQty")));
                        int boxQty2 = Integer.parseInt(String.valueOf(temp2.get("BoxQty")).equalsIgnoreCase("null") ? "0" : String.valueOf(temp2.get("BoxQty")));
                        int oneBoxQty = Integer.parseInt(String.valueOf(temp1.get("OneBoxQty") == null ? "0" : temp1.get("OneBoxQty")));
                        if (boxQty1 > 0 || boxQty2 > 0) {// 装箱
                            count1 = boxQty1 * oneBoxQty;
                            count2 = boxQty2 * oneBoxQty;
                            temp1.put("BoxQty", boxQty1 + boxQty2);
                        }
                        temp1.put("Quantity", count1 + count2);
                        dataList.remove(j);
                    }
                }
            } else {
                for (int j = dataList.size() - 1; j > i; j--) {
                    Map temp2 = (Map) dataList.get(j);
                    if (temp1.get("GoodsID").equals(temp2.get("GoodsID")) && temp1.get("ColorID").equals(temp2.get("ColorID")) && temp1.get("SizeID").equals(temp2.get("SizeID"))) {
                        Map map = new HashMap();
                        int count1 = Integer.parseInt(String.valueOf(temp1.get("Quantity")));
                        int count2 = Integer.parseInt(String.valueOf(temp2.get("Quantity")));
                        int boxQty1 = Integer.parseInt(String.valueOf(temp1.get("BoxQty")).equalsIgnoreCase("null") ? "0" : String.valueOf(temp1.get("BoxQty")));
                        int boxQty2 = Integer.parseInt(String.valueOf(temp2.get("BoxQty")).equalsIgnoreCase("null") ? "0" : String.valueOf(temp2.get("BoxQty")));
                        int oneBoxQty = Integer.parseInt(String.valueOf(temp1.get("OneBoxQty") == null ? "0" : temp1.get("OneBoxQty")));
                        if (boxQty1 > 0 || boxQty2 > 0) {// 装箱
                            count1 = boxQty1 * oneBoxQty;
                            count2 = boxQty2 * oneBoxQty;
                            temp1.put("BoxQty", boxQty1 + boxQty2);
                        }
                        temp1.put("Quantity", count1 + count2);
                        dataList.remove(j);
                    }
                }
            }
        }
        return dataList;
    }

    @Override
    public int coverSave(String SalesID, List<Map<String, Object>> dataList, Client client) throws Exception {
        int dir = Integer.parseInt(String.valueOf(commonDao.getData(" select QuantitySum from Sales where SalesID = ? ", SalesID)));
        List<Map<String, Object>> delList = new ArrayList<Map<String, Object>>(); // 记录要删除的集合
        List<Map<String, Object>> updateList = new ArrayList<Map<String, Object>>(); // 记录要修改的集合
        for (int i = 0; i < dataList.size(); i++) {
            Map<String, Object> temp = dataList.get(i);
            int tQuantity = Math.abs(Integer.parseInt(String.valueOf(temp.get("Quantity"))));
            int tQty = Math.abs(Integer.parseInt(String.valueOf(temp.get("Qty"))));
            if (tQuantity > 0) {// 原单中存在的记录
                if (tQty > tQuantity) {// 执行修改
                    updateList.add(temp);
                    dataList.remove(temp);
                    i--;
                } else if (tQty == tQuantity) {// 不需要修改
                    dataList.remove(temp);
                    i--;
                } else if (tQty < tQuantity) {// 执行修改或删除操作
                    if (tQty == 0) {
                        delList.add(temp);
                        dataList.remove(temp);
                        i--;
                    } else {
                        updateList.add(temp);
                        dataList.remove(temp);
                        i--;
                    }
                }
            } else {// 新增的记录
                updateList.add(temp);
                dataList.remove(temp);
                i--;
            }
        }
        // 新增或修改
        coverSaveToUpdate(SalesID, dir, updateList);
        // 删除
        deleteSalesdetail(delList, SalesID);
        // 更新主表
        // 重新计算货品价格
        StringBuffer sb = new StringBuffer();
        sb.append(" update Sales set DiscountSum =(select sum(Discount) from Salesdetailtemp where SalesID = '").append(SalesID).append("' ) , AmountSum = (select sum(Amount) from Salesdetailtemp where SalesID = '").append(SalesID)
                .append("'), RetailAmountSum = (select sum(RetailAmount) from Salesdetailtemp where SalesID = '").append(SalesID).append("'), QuantitySum = (select sum(Quantity) from Salesdetailtemp where SalesID = '").append(SalesID).append("') where SalesID = '").append(SalesID).append("' ; ");
        int count = commonDao.executeSql(sb.toString());
        return count;
    }

    /**
     * 修改子表记录(条码校验)
     * 
     * @param SalesID
     * @param dir
     * @param updateList
     */
    private void coverSaveToUpdate(String SalesID, int dir, List<Map<String, Object>> updateList) {
        for (int i = 0; i < updateList.size(); i++) {
            StringBuffer sql = new StringBuffer();
            Map<String, Object> temp = updateList.get(i);
            String tGoodsId = String.valueOf(temp.get("GoodsID"));
            String tColorId = String.valueOf(temp.get("ColorID"));
            String tSizeId = String.valueOf(temp.get("SizeID"));
            String meno = null;
            if (temp.containsKey("meno")) {
                meno = String.valueOf(temp.get("meno"));
            }
            int tBoxQty = Math.abs(Integer.parseInt(String.valueOf(temp.get("BoxQty"))));
            int tQuantity = Math.abs(Integer.parseInt(String.valueOf(temp.get("Quantity"))));
            int tQty = Math.abs(Integer.parseInt(String.valueOf(temp.get("Qty"))));
            String x = String.valueOf(commonDao.getData("select no from sizegroupsize where sizegroupid = (select GroupID from goods where GoodsID = ?) and SizeId = '" + tSizeId + "' ; ", tGoodsId));
            // 获取sizIndex
            int sizIndex = getMaxSize(tGoodsId);
            // 判断是否是箱条码
            String typeStr = String.valueOf(commonDao.getData(" select type from sales where salesId = ?", SalesID));
            String customerId = String.valueOf(commonDao.getData(" select customerId from sales where salesId = ?", SalesID));
            String type = getType(typeStr);
            String priceColumn = getTypeColumn(customerId, type);
            double uprice = Double.valueOf(String.valueOf(commonDao.getData(" select isnull(" + priceColumn + ",0) from goods g where goodsId = ? ", tGoodsId)));
            BigDecimal UnitPrice = new BigDecimal(String.valueOf(temp.get("UnitPrice"))).setScale(2, BigDecimal.ROUND_HALF_UP);
            BigDecimal DiscountPrice = new BigDecimal(uprice).setScale(2, BigDecimal.ROUND_HALF_UP);
            BigDecimal RetailSales = new BigDecimal(String.valueOf(temp.get("RetailSales"))).setScale(2, BigDecimal.ROUND_HALF_UP);
            BigDecimal DiscountRate = null;
            // 计算折扣
            if (UnitPrice != null && UnitPrice.compareTo(BigDecimal.ZERO) != 0 && DiscountPrice != null && DiscountPrice.compareTo(BigDecimal.ZERO) != 0) {
                DiscountRate = DiscountPrice.divide(UnitPrice, BigDecimal.ROUND_CEILING, 2).multiply(new BigDecimal(10));
                if (null != DiscountRate && DiscountRate.compareTo(BigDecimal.ZERO) == 0) {
                    DiscountRate = null;
                }
            }
            // 判断货品是否有单价
            if (DiscountPrice != null && DiscountPrice.compareTo(BigDecimal.ZERO) != 0 && UnitPrice != null && UnitPrice.compareTo(BigDecimal.ZERO) == 0) {
                UnitPrice = DiscountPrice;
            }
            // 判断折扣价和单价是否相等
            if (DiscountPrice != null && UnitPrice != null && UnitPrice.compareTo(DiscountPrice) == 0) {
                DiscountPrice = null;
                DiscountRate = null;
            }
            String sizeStr = String.valueOf(temp.get("SizeStr"));
            Object obj = null;
            if (tBoxQty > 0) {
                obj = commonDao.getData(" select count(1) from salesdetailtemp where goodsId = '" + tGoodsId + "' and colorid  = '" + tColorId + "' and salesid = '" + SalesID + "' and sizeStr = '" + String.valueOf(temp.get("SizeStr")) + "' ");
            } else {
                obj = commonDao.getData(" select count(1) from salesdetailtemp where goodsId = '" + tGoodsId + "' and colorid  = '" + tColorId + "' and salesid = '" + SalesID + "' ");
            }
            int exit = -1;
            if (null != obj) {
                exit = Integer.parseInt(String.valueOf(obj));
            }
            if (exit < 1) {// 新增
                if (tBoxQty > 0) {
                    int quantitySum = Math.abs(Integer.parseInt(String.valueOf(temp.get("QuantitySum"))));
                    tQty = quantitySum;
                }
                if (dir < 0) {
                    tQty = -tQty;
                    tBoxQty = -tBoxQty;
                }
                int maxIndexNo = getMaxIndexNo(SalesID);
                maxIndexNo++;
                sql.append(" Insert into SalesDetailTemp(IndexNo,SalesID,GoodsID,ColorID,x_").append(x).append(",Quantity,UnitPrice,DiscountPrice,DiscountRate,RetailSales,Amount,RetailAmount,Discount,BoxQty,SizeStr,SizeIndex) Values(").append(maxIndexNo).append(",'").append(SalesID).append("', '")
                        .append(tGoodsId).append("','").append(tColorId).append("' ");
                if (null == x || "".equals(x) || "null".equalsIgnoreCase(x)) {
                    sql.append(",null ");
                } else {
                    if (Math.abs(tBoxQty) > 0) {
                        sql.append(",").append(tQty / tBoxQty);
                    } else {
                        sql.append(",").append(tQty);
                    }
                }
                sql.append(", ").append(tQty).append(", ").append(UnitPrice);
                // 折扣价
                if (null == DiscountPrice || DiscountPrice.compareTo(BigDecimal.ZERO) == 0) {
                    sql.append(", null ");
                } else {
                    sql.append(", ").append(DiscountPrice);
                }
                sql.append(", ").append(DiscountRate).append(", ").append(RetailSales);
                // 实收金额
                if (null != DiscountPrice && DiscountPrice.compareTo(BigDecimal.ZERO) != 0) {
                    sql.append(", ").append(tQty).append("*").append(DiscountPrice);
                } else {
                    sql.append(", ").append(tQty).append("*").append(UnitPrice);
                }
                // 零售金额
                if (RetailSales == null) {
                    sql.append(", null ");
                } else {
                    sql.append(",  ").append(tQty).append("*").append(RetailSales);
                }
                // 折扣额
                if (null == DiscountPrice || DiscountPrice.compareTo(BigDecimal.ZERO) == 0 || RetailSales.compareTo(UnitPrice) == 0 || UnitPrice.compareTo(DiscountPrice) == 0) {// 无折扣
                    sql.append(",  null ");
                } else {
                    sql.append(",  (isnull(").append(UnitPrice).append(",0)-isnull(").append(DiscountPrice).append(",0))*").append(tQty);
                }
                // 箱数
                sql.append(",").append(tBoxQty == 0 ? null : tBoxQty).append(",");
                // 配码
                if (0 != tBoxQty) {
                    sql.append("'").append(sizeStr).append("'");
                } else {
                    sql.append("null");
                }
                sql.append(",").append(sizIndex).append(",");
                // 备注
                if (meno != null) {
                    sql.append("'").append(meno).append("'");
                } else {
                    sql.append("null");
                }
                sql.append(") ");
            } else {// 修改
                int quantitySum = 0;
                if (Math.abs(tBoxQty) > 0) {
                    quantitySum = Math.abs(Integer.parseInt(String.valueOf(temp.get("QuantitySum"))));
                } else {
                    int ttQty = Integer.parseInt(String.valueOf(commonDao.getData(" select sum(Quantity) from SalesDetailTemp where salesId = '" + SalesID + "' and goodsId = '" + tGoodsId + "' and colorId = '" + tColorId + "' ")));
                    quantitySum = Math.abs(ttQty) + Math.abs(tQty) - Math.abs(tQuantity);
                }
                if (dir < 0) {
                    quantitySum = -quantitySum;
                    tBoxQty = -tBoxQty;
                    tQty = -tQty;
                }
                sql.append(" Update SalesDetailTemp set  UnitPrice =   ").append(UnitPrice).append(", RetailSales = ").append(RetailSales).append(",BoxQty = ").append(tBoxQty == 0 ? null : tBoxQty).append(",DiscountPrice = ");
                if (null == DiscountPrice || DiscountPrice.compareTo(BigDecimal.ZERO) == 0) {
                    sql.append(" null ");
                } else {
                    sql.append(DiscountPrice);
                }
                sql.append(", DiscountRate = ").append(DiscountRate);

                if (Math.abs(tBoxQty) > 0) {
                    sql.append(", x_").append(x).append(" =  ").append(tQty / tBoxQty);
                } else {
                    sql.append(", x_").append(x).append(" =  ").append(tQty);
                }

                sql.append(", Quantity=").append(quantitySum);
                // 实收金额
                if (UnitPrice == null && DiscountPrice == null) {
                    sql.append(" , Amount=null ");
                } else {
                    if (null != DiscountPrice && DiscountPrice.compareTo(BigDecimal.ZERO) != 0) {
                        sql.append(",Amount =  ").append(quantitySum).append("*").append(DiscountPrice);
                    } else {
                        sql.append(",Amount =  ").append(quantitySum).append("*").append(UnitPrice);
                    }
                }
                // 零售金额合计
                if (RetailSales == null) {
                    sql.append(", RetailAmount = null ");
                } else {
                    sql.append(", RetailAmount =  ").append(quantitySum).append("*").append(RetailSales);
                }
                // 折扣额
                if (DiscountPrice == null) {
                    sql.append(" , Discount = null ");
                } else {
                    sql.append(" , Discount =  (isnull(").append(UnitPrice).append(",0)-isnull(").append(DiscountPrice).append(",0))").append("*").append(quantitySum);
                }
                // 备注
                if (meno != null) {
                    sql.append(" , memo = '" + meno + "' ");
                } else {
                    sql.append(" , memo = null ");
                }
                sql.append(", SizeIndex = ").append(sizIndex).append(" where SalesID = '").append(SalesID).append("' and GoodsID = '").append(tGoodsId).append("' ");
                if (0 != tBoxQty) {
                    sql.append(" and SizeStr = '").append(sizeStr).append("' ");
                }
                sql.append(" and ColorID = '").append(tColorId).append("' ; ");
            }
            int count = commonDao.executeSql(sql.toString());
            sql = new StringBuffer();
            if (count < 1) {
                throw new BusinessException("条码校验修改单据失败");
            }
        }
    }

    /**
     * 格式化箱条码集合
     * 
     * @param goodsId
     * @param colorId
     * @param dataList
     * @return
     */
    private List<Map<String, Object>> formatGoodsBoxBarcode(String goodsId, String colorId, List<Map<String, Object>> dataList) {
        for (int i = 0; i < dataList.size(); i++) {
            Map<String, Object> map = dataList.get(i);
            String tGoodsId = String.valueOf(map.get("GoodsID"));
            String tColorId = String.valueOf(map.get("ColorID"));
            if (tGoodsId.equals(goodsId) && tColorId.equals(colorId)) {
                dataList.remove(map);
            }
        }
        return dataList;
    }

	@Override
	public String saveSalesX(String directionStr, List<Map<String, Object>> dataList, String SalesID, String customerid, String departmentid, String employeeId, String businessDeptId, String memo, String type, String typeEName, String brandId, String discountRateSumStr,
            String lastARAmountStr, String orderAmountStr, String privilegeAmountStr, String paymentTypeIdStr, Client client) throws Exception {
		// TODO Auto-generated method stub
		
		 String warehouseId = null;
	        String result = String.valueOf(commonDao.getData(" select top 1 departmentId from department where SettleCustID is not null and SettleCustID <> '' and SettleCustID = ? ", customerid));
	        if (result != null && !"".equals(result) && !"null".equalsIgnoreCase(result)) {
	            warehouseId = "'" + result + "'";
	        }
	        int direction = Integer.parseInt(directionStr);
	        String No = null;
	        String deptCode = String.valueOf(commonDao.getData(" select Code  from Department where DepartmentID = ? ", departmentid));
	        // 判断SalesID是否为空，做新增主表
	        if ("".equals(SalesID)) {
	            int tag = 0;
	            if (direction == -1) {
	                tag = 97;
	            } else {
	                tag = 30;
	            }
	            // 生成ID
	            SalesID = commonDao.getNewIDValue(tag);
	            // 生成No
	            String deptType = client.getDeptType();
	            if (deptType != null && ("直营店".equals(deptType) || "加盟店".equals(deptType))) {
	                No = commonDao.getNewNOValue(tag, SalesID, client.getDeptCode());
	            } else {
	                No = commonDao.getNewNOValue(tag, SalesID);
	            }
	            if (SalesID == null || No == null) {
	                throw new BusinessException("生成主键/单号失败");
	            }
	      
	        }
	     // -----------------其它操作------------------//
	        // 获取同货品,同颜色的总数量
	        for (int i = 0; i < dataList.size(); i++) {
	            Map map = dataList.get(i);
	            String sizeStr = String.valueOf(map.get("SizeStr"));
	            int quantitySum = 0;
	            if (sizeStr != null && !"".equals(sizeStr) && !"null".equalsIgnoreCase(sizeStr)) {
	                for (int j = 0; j < dataList.size(); j++) {
	                    Map temp = dataList.get(j);
	                    if (String.valueOf(temp.get("GoodsID")).equals(String.valueOf(map.get("GoodsID"))) && String.valueOf(temp.get("ColorID")).equals(String.valueOf(map.get("ColorID"))) && String.valueOf(temp.get("SizeStr")).equals(sizeStr)) {
	                        int count = Integer.parseInt(String.valueOf(temp.get("Quantity")));
	                        quantitySum += count;
	                    }
	                }
	            } else {
	                for (int j = 0; j < dataList.size(); j++) {
	                    Map temp = dataList.get(j);
	                    if (String.valueOf(temp.get("GoodsID")).equals(String.valueOf(map.get("GoodsID"))) && String.valueOf(temp.get("ColorID")).equals(String.valueOf(map.get("ColorID")))) {
	                        int count = Integer.parseInt(String.valueOf(temp.get("Quantity")));
	                        quantitySum += count;
	                    }
	                }
	            }
	            map.put("QuantitySum", quantitySum);
	        }
	        // 整单折扣
	        BigDecimal discountRateSum = null;
	        if (null == discountRateSumStr || "".equals(discountRateSumStr) || "null".equalsIgnoreCase(discountRateSumStr)) {
	            discountRateSum = null;
	        } else {
	            discountRateSum = new BigDecimal(discountRateSumStr);
	        }
	        // 应收金额
	        BigDecimal lastARAmount = null;
	        if (null == lastARAmountStr || "".equals(lastARAmountStr) || "null".equalsIgnoreCase(lastARAmountStr)) {
	            lastARAmount = null;
	        } else {
	            lastARAmount = new BigDecimal(lastARAmountStr);
	        }
	        // 应收金额
	        BigDecimal orderAmount = null;
	        if (null == orderAmountStr || "".equals(orderAmountStr) || "null".equalsIgnoreCase(orderAmountStr)) {
	            orderAmount = null;
	        } else {
	            orderAmount = new BigDecimal(orderAmountStr);
	        }
	        // 优惠金额
	        BigDecimal privilegeAmount = null;
	        if (null == privilegeAmountStr || "".equals(privilegeAmountStr) || "null".equalsIgnoreCase(privilegeAmountStr)) {
	            privilegeAmount = null;
	        } else {
	            privilegeAmount = new BigDecimal(privilegeAmountStr);
	        }
	        // 结算方式
	        String paymentTypeId = null;
	        if (null != paymentTypeIdStr && !"".equals(paymentTypeIdStr) && !"null".equalsIgnoreCase(paymentTypeIdStr)) {
	            paymentTypeId = "'" + paymentTypeIdStr + "'";
	        }
	        // 获取最大的下标值
	        int maxIndexNo = getMaxIndexNo(SalesID);
	        // 循环查询或更新
	        dataList = mergeData(dataList);
	        for (int i = 0; i < dataList.size(); i++) {
	            Map firstMap = (Map) dataList.get(i);
	            String goodsId = String.valueOf(firstMap.get("GoodsID"));
	            String colorId = String.valueOf(firstMap.get("ColorID"));
	            String sizeId = String.valueOf(firstMap.get("SizeID"));
	            String meno = null;
	            if (firstMap.containsKey("meno")) {
	                meno = String.valueOf(firstMap.get("meno")).trim();
	            }
	            int quantity = Integer.parseInt(String.valueOf(firstMap.get("Quantity")));
	            int quantitySum = Integer.parseInt(String.valueOf(firstMap.get("QuantitySum")));
	          //  String x = String.valueOf(commonDao.getData("select no from sizegroupsize where sizegroupid = (select GroupID from goods where GoodsID = ?) and SizeId = '" + sizeId + "' ; ", goodsId));
	            BigDecimal UnitPrice = firstMap.get("UnitPrice") == null ? null : new BigDecimal(String.valueOf(firstMap.get("UnitPrice"))).setScale(2, BigDecimal.ROUND_HALF_UP);
	            BigDecimal RetailSales = firstMap.get("RetailSales") == null ? null : new BigDecimal(String.valueOf(firstMap.get("RetailSales"))).setScale(2, BigDecimal.ROUND_HALF_UP);
	            BigDecimal DiscountPrice = firstMap.get("DiscountPrice") == null ? null : new BigDecimal(String.valueOf(firstMap.get("DiscountPrice"))).setScale(2, BigDecimal.ROUND_HALF_UP);
	            BigDecimal DiscountRate = firstMap.get("DiscountRate") == null ? null : new BigDecimal(String.valueOf(firstMap.get("DiscountRate"))).setScale(2, BigDecimal.ROUND_HALF_UP);
	            String sizeStr = String.valueOf(firstMap.get("SizeStr"));
	            // 获取sizIndex
	            //int sizIndex = getMaxSize(goodsId);
	            int sizIndex=Integer.parseInt(String.valueOf(firstMap.get("sizIndex")));
	            // 计算折扣
	            if (UnitPrice != null && UnitPrice.compareTo(BigDecimal.ZERO) != 0 && DiscountPrice != null && DiscountPrice.compareTo(BigDecimal.ZERO) != 0) {
	                DiscountRate = DiscountPrice.divide(UnitPrice, BigDecimal.ROUND_CEILING, 2).multiply(new BigDecimal(10));
	                if (null != DiscountRate && DiscountRate.compareTo(BigDecimal.ZERO) == 0) {
	                    DiscountRate = null;
	                }
	            }
	            // 判断货品是否有单价
	            if (DiscountPrice != null && DiscountPrice.compareTo(BigDecimal.ZERO) != 0 && UnitPrice != null && UnitPrice.compareTo(BigDecimal.ZERO) == 0) {
	                UnitPrice = DiscountPrice;
	            }
	            // 判断折扣价和单价是否相等
	            if (DiscountPrice != null && UnitPrice != null && UnitPrice.compareTo(DiscountPrice) == 0) {
	                DiscountPrice = null;
	                DiscountRate = null;
	            }
	            // 箱数
	            String boxQtyStr = String.valueOf(firstMap.get("BoxQty"));
	            if (null == boxQtyStr || "".equals(boxQtyStr) || "null".equalsIgnoreCase(boxQtyStr)) {
	                boxQtyStr = "0";
	            }
	            Integer boxQty = Integer.parseInt(boxQtyStr);
	            if (null == boxQty || boxQty == 0) {
	                boxQty = null;
	            }
	            // 每箱的件数
	            Integer oneBoxQty = Integer.parseInt(String.valueOf(firstMap.get("OneBoxQty") == null ? "0" : firstMap.get("OneBoxQty")));
	            if (null == oneBoxQty || oneBoxQty == 0) {
	                oneBoxQty = null;
	            }

	            Object obj = null;
	            // 判断是新增还是更新
	            if (null != boxQty && 0 != boxQty) {
	                obj = commonDao.getData(" select count(1) from salesdetailtemp where goodsId = '" + goodsId + "' and colorid  = '" + colorId + "' and salesid = '" + SalesID + "' and sizeStr = '" + sizeStr + "' ");
	            } else {
	                obj = commonDao.getData(" select count(1) from salesdetailtemp where goodsId = '" + goodsId + "' and colorid  = '" + colorId + "' and salesid = '" + SalesID + "' ");
	            }
	            int count = -1;
	            if (null != obj) {
	                count = Integer.parseInt(String.valueOf(obj));
	            }
	            // ----------------------------------------//
	            // --------------插入子表数据-----------------//
	            // ----------------------------------------//
	            if (count < 1) {
	                maxIndexNo++;
	                StringBuffer sql = new StringBuffer();
	                sql.append(" Insert into SalesDetailTemp(IndexNo,SalesID,GoodsID,ColorID,").append(firstMap.get("x")).append(",Quantity,UnitPrice,DiscountPrice,DiscountRate,RetailSales,Amount,RetailAmount,Discount,BoxQty,SizeStr,SizeIndex,memo) Values(").append(maxIndexNo).append(",'").append(SalesID)
	                        .append("', '").append(goodsId).append("','").append(colorId).append("' ");
	                if (null == firstMap.get("x") || "".equals(firstMap.get("x")) || "null".equalsIgnoreCase(String.valueOf(firstMap.get("x")))) {
	                    sql.append(",null ");
	                } else {
	                    if (null != boxQty && 0 != boxQty) {
	                        sql.append(",").append(quantity / boxQty);
	                    } else {
	                        sql.append(",").append(quantity);
	                    }
	                }
	                sql.append(", ").append(quantity).append(", ").append(UnitPrice);
	                // 折扣价
	                if (null == DiscountPrice || DiscountPrice.compareTo(BigDecimal.ZERO) == 0) {
	                    sql.append(", null ");
	                } else {
	                    sql.append(", ").append(DiscountPrice);
	                }
	                sql.append(", ").append(DiscountRate).append(", ").append(RetailSales);
	                // 实收金额
	                if (null != DiscountPrice && DiscountPrice.compareTo(BigDecimal.ZERO) != 0) {
	                    sql.append(", ").append(quantity).append("*").append(DiscountPrice);
	                } else {
	                    sql.append(", ").append(quantity).append("*").append(UnitPrice);
	                }
	                // 零售金额
	                if (RetailSales == null) {
	                    sql.append(", null ");
	                } else {
	                    sql.append(",  ").append(quantity).append("*").append(RetailSales);
	                }
	                // 折扣额
	                if (null == DiscountPrice || DiscountPrice.compareTo(BigDecimal.ZERO) == 0 || RetailSales.compareTo(UnitPrice) == 0 || UnitPrice.compareTo(DiscountPrice) == 0) {// 无折扣
	                    sql.append(",  null ");
	                } else {
	                    sql.append(",  (isnull(").append(UnitPrice).append(",0)-isnull(").append(DiscountPrice).append(",0))*").append(quantity);
	                }
	                // 箱数
	                sql.append(",").append(boxQty).append(",");
	                // 配码
	                if (null != boxQty && 0 != boxQty) {
	                    sql.append("'").append(sizeStr).append("'");
	                } else {
	                    sql.append("null");
	                }
	                sql.append(",").append(sizIndex).append(",");
	                // 备注
	                if (meno != null && !"".equals(meno) && !"null".equalsIgnoreCase(meno)) {
	                    sql.append("'").append(meno).append("'");
	                } else {
	                    sql.append("null");
	                }
	                sql.append(") ");
	                commonDao.executeSql(sql.toString());
	            } else {
	                // ----------------------------------------//
	                // ------------修改子表数据-------------------//
	                // ----------------------------------------//
	                // 判断客户对应的货品是否存在价格
	                // String priceColumn = getTypeColumn(customerid, typeEName);
	                // double uprice = Double.valueOf(String.valueOf(commonDao
	                // .getData(" select isnull(" + priceColumn
	                // + ",0) from goods g where goodsId = ? ",
	                // goodsId)));
	                // if (DiscountPrice != null
	                // && DiscountPrice.compareTo(BigDecimal.ZERO) != 0
	                // && uprice <= 0) {// 该用户对应的货品没有单价
	                // UnitPrice = DiscountPrice;
	                // DiscountPrice = null;
	                // DiscountRate = null;
	                // }
	                StringBuffer sql = new StringBuffer();
	                sql.append(" Update SalesDetailTemp set  UnitPrice =   ").append(UnitPrice).append(", RetailSales = ").append(RetailSales).append(",BoxQty = ").append(boxQty).append(",DiscountPrice = ");
	                if (null == DiscountPrice || DiscountPrice.compareTo(BigDecimal.ZERO) == 0) {
	                    sql.append(" null ");
	                } else {
	                    sql.append(DiscountPrice);
	                }
	                sql.append(", DiscountRate = ").append(DiscountRate);

	                if (null != boxQty && 0 != boxQty) {
	                    sql.append(",").append(firstMap.get("x")).append(" =  ").append(quantity / boxQty);
	                } else {
	                    sql.append(",").append(firstMap.get("x")).append(" =  ").append(quantity);
	                }

	                sql.append(", Quantity=").append(quantitySum);
	                // 实收金额
	                if (UnitPrice == null && DiscountPrice == null) {
	                    sql.append(" , Amount=null ");
	                } else {
	                    if (null != DiscountPrice && DiscountPrice.compareTo(BigDecimal.ZERO) != 0) {
	                        sql.append(",Amount =  ").append(quantitySum).append("*").append(DiscountPrice);
	                    } else {
	                        sql.append(",Amount =  ").append(quantitySum).append("*").append(UnitPrice);
	                    }
	                }
	                // 零售金额合计
	                if (RetailSales == null) {
	                    sql.append(", RetailAmount = null ");
	                } else {
	                    sql.append(", RetailAmount =  ").append(quantitySum).append("*").append(RetailSales);
	                }
	                // 折扣额
	                if (DiscountPrice == null) {
	                    sql.append(" , Discount = null ");
	                } else {
	                    sql.append(" , Discount =  (isnull(").append(UnitPrice).append(",0)-isnull(").append(DiscountPrice).append(",0))").append("*").append(quantitySum);
	                }
	                // 备注
	                if (meno != null && !"".equals(meno) && !"null".equalsIgnoreCase(meno)) {
	                    sql.append(" , memo = '" + meno + "' ");
	                } else {
	                    sql.append(" , memo = null ");
	                }
	                sql.append(", SizeIndex = ").append(sizIndex).append(" where SalesID = '").append(SalesID).append("' and GoodsID = '").append(goodsId).append("' ");
	                if (null != boxQty && 0 != boxQty) {
	                    sql.append(" and SizeStr = '").append(sizeStr).append("' ");
	                }
	                sql.append(" and ColorID = '").append(colorId).append("' ");
	                commonDao.executeSql(sql.toString());
	            }
	        }//datalist 结束
	        
	        if ("".equals(SalesID)) {
	            int QtySum =0;
		        BigDecimal AmountSum=new BigDecimal(0.00);
		        BigDecimal DiscountSum=new BigDecimal(0.00);
		        BigDecimal RetailAmountSum=new BigDecimal(0.00);
		     //   String displaySizeGroup ="";
		        
		      @SuppressWarnings("unchecked")
			List<Map<String,Object>> ls= commonDao.findForJdbc("Select SUM(Quantity) QtySum,Sum(Amount) AmountSum, Sum(RetailAmount) RetailAmountSum,Sum(Discount) DiscountSum from SalesDetailTemp  where SalesID ='"+SalesID+"'");
		      for(Map<String,Object> map :ls){
		    	  QtySum =QtySum+(int)(map.get("QtySum"));
		    	  AmountSum=AmountSum.add((BigDecimal)map.get(AmountSum));
		    	  DiscountSum=DiscountSum.add((BigDecimal)map.get(DiscountSum));
		    	  RetailAmountSum=RetailAmountSum.add((BigDecimal)map.get(RetailAmountSum));  	  
		      }
		      
		       
		    
	        	
	        	
	        	
	        	
	        StringBuilder insertMaster = new StringBuilder();
	       insertMaster.append("insert into Sales(SalesID, No,Date ").append(" , CustomerID,DepartmentID,WarehouseID,EmployeeID,  ").append("AmountSum, QuantitySum,  MadeBy, MadeByDate, Type, memo, DiscountSum, Year, Month,  RetailAmountSum, BusinessDeptID,direction, brandId ,discountRateSum,ReceivalAmount,orderAmount,privilegeAmount, paymentTypeId)")
	                    .append(" values('").append(SalesID).append("', '").append(No).append("', '" + DataUtils.str2Timestamp(DataUtils.formatDate()) + "', '").append(customerid).append("','").append(departmentid).append("', ").append(warehouseId).append(", '").append(employeeId).append("', ")
	                    .append(AmountSum).append(",").append(QtySum).append(", '").append(client.getUserName()).append("', getdate() ").append(",'").append(type).append("','").append(memo).append("' ,"+DiscountSum+", '").append(DataUtils.getYear()).append("','").append(DataUtils.getStringMonth()).append("', "+RetailAmountSum+", '")
	                    .append(businessDeptId).append("',").append(direction).append(",'").append(brandId).append("',").append(discountRateSum).append(",").append(lastARAmount).append(",").append(orderAmount).append(",").append(privilegeAmount).append(",").append("'").append(paymentTypeId).append("')");
	            commonDao.executeSql(insertMaster.toString()); 
	            
	            commonDao.executeSql("update  sales set displaySizeGroup= (SELECT STUFF((select DISTINCT ','''+g.GroupID +'''' from SalesDetailTemp a JOIN goods g ON a.goodsid=g.goodsid where a.SalesID='"+SalesID+"' FOR XML PATH('')),1,1,'')) where SalesID='"+SalesID+"'");
	            
	            
	        }else{
	        	  // 更新主表信息
		        StringBuilder sumSql = new StringBuilder();
		        sumSql.append(" Update Sales  set QuantitySum = t.QtySum,AmountSum = t.AmountSum, discountRateSum = " + discountRateSum + ", ReceivalAmount = " + lastARAmount + ",orderAmount = " + orderAmount + ",privilegeAmount = " + privilegeAmount + ",paymentTypeId = " + paymentTypeId + ", ")
		                .append("  RetailAmountSum = t.RetailAmountSum, DiscountSum = t.DiscountSum ,displaySizeGroup = ").append(" (SELECT STUFF((select DISTINCT ','''+g.GroupID +'''' from SalesDetailTemp a JOIN goods g ON a.goodsid=g.goodsid ")
		                .append(" WHERE salesid='" + SalesID + "' FOR XML PATH('')),1,1,''))  ").append(" from  (Select SUM(Quantity) QtySum,Sum(Amount) AmountSum, Sum(RetailAmount) RetailAmountSum,Sum(Discount) DiscountSum  ").append("  from SalesDetailTemp  where SalesID ='").append(SalesID)
		                .append("'   ) t  ").append(" where SalesID = '").append(SalesID).append("' ");
		        commonDao.executeSql(sumSql.toString()); 
		        
	        }
	        
	        
	        
	        if (lastARAmount != null && lastARAmount.compareTo(BigDecimal.ZERO) != 0 && direction != -1) {
	            // 收款金额不为空的时候生成收款单
	            String receivalType = "货款";
	            BigDecimal arAmount = new BigDecimal(String.valueOf(commonDao.getData(" select AmountSum from sales where salesId = ? ", SalesID)));
	            generalReceival(deptCode, customerid, departmentid, employeeId, paymentTypeIdStr, arAmount, lastARAmount, No, SalesID, brandId, receivalType, client);
	        }
	        if (privilegeAmount != null && privilegeAmount.compareTo(BigDecimal.ZERO) != 0 && direction != -1) {
	            // 优惠金额不为空的时候生成收款单
	            String receivalType = "优惠";
	            BigDecimal arAmount = new BigDecimal(String.valueOf(commonDao.getData(" select AmountSum from sales where salesId = ? ", SalesID)));
	            generalReceival(deptCode, customerid, departmentid, employeeId, paymentTypeIdStr, arAmount, privilegeAmount, No, SalesID, brandId, receivalType, client);
	        }
	        if (orderAmount != null && orderAmount.compareTo(BigDecimal.ZERO) != 0 && direction != -1) {
	            // 优惠金额不为空的时候生成收款单
	            String receivalType = "订金";
	            BigDecimal arAmount = new BigDecimal(String.valueOf(commonDao.getData(" select AmountSum from sales where salesId = ? ", SalesID)));
	            generalReceival(deptCode, customerid, departmentid, employeeId, paymentTypeIdStr, arAmount, orderAmount, No, SalesID, brandId, receivalType, client);
	        }
	        return SalesID;
	        
	      
	}
}
