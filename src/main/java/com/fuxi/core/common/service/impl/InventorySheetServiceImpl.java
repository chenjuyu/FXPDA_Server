package com.fuxi.core.common.service.impl;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.fuxi.core.common.dao.impl.CommonDao;
import com.fuxi.core.common.exception.BusinessException;
import com.fuxi.core.common.service.InventorySheetService;
import com.fuxi.system.util.Client;
import com.fuxi.system.util.DataUtils;

/**
 * Title: InventorySheetServiceImpl Description: 盘点业务逻辑接口实现类
 * 
 * @author LYJ
 * 
 */
@Service("sheetService")
@Transactional
public class InventorySheetServiceImpl implements InventorySheetService {

    @Autowired
    private CommonDao commonDao;

    @Override
    public String saveInventorySheet(List<Map<String, Object>> dataList, String stocktakingId, String departmentId, String employeeId, String memo, String brandId, Client client) throws Exception {
        // 判断stocktakingId是否为空，做新增主表
        if (null == stocktakingId || "".equals(stocktakingId) || "null".equalsIgnoreCase(stocktakingId)) {
            int tag = 38;
            // 生成ID
            stocktakingId = commonDao.getNewIDValue(tag);
            // 生成No
            String deptType = client.getDeptType();
            String No = null;
            if (deptType != null && ("直营店".equals(deptType) || "加盟店".equals(deptType))) {
                No = commonDao.getNewNOValue(tag, stocktakingId, client.getDeptCode());
            } else {
                No = commonDao.getNewNOValue(tag, stocktakingId);
            }
            if (stocktakingId == null || No == null) {
                throw new BusinessException("生成主键/单号失败");
            }
            StringBuilder insertMaster = new StringBuilder();
            insertMaster.append(" insert into Stocktaking(StocktakingID, No, Date, WarehouseID, DepartmentID, EmployeeID, MadeBy, MadeByDate, Memo, Year, Month,BrandId) ").append(" values('").append(stocktakingId).append("','").append(No).append("','")
                    .append(DataUtils.str2Timestamp(DataUtils.formatDate())).append("','").append(departmentId).append("','" + departmentId).append("','").append(employeeId).append("','").append(client.getUserName()).append("','").append(DataUtils.gettimestamp()).append("','").append(memo)
                    .append("','").append(DataUtils.getYear()).append("','").append(DataUtils.getStringMonth()).append("','").append(brandId).append("'); ");
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
                        int boxQtyTotal = Integer.parseInt(String.valueOf(temp.get("BoxQtyTotal")));
                        int oneBoxQty = Integer.parseInt(String.valueOf(temp.get("OneBoxQty")));
                        quantitySum += (boxQtyTotal * oneBoxQty);
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
        // 获取最大的下标值
        int maxIndexNo = getMaxIndexNo(stocktakingId);
        // 循环查询或更新
        // dataList = mergeData(dataList);
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
            BigDecimal RetailSales = firstMap.get("RetailSales") == null ? null : new BigDecimal(String.valueOf(firstMap.get("RetailSales"))).setScale(2, BigDecimal.ROUND_HALF_UP);
            String sizeStr = String.valueOf(firstMap.get("SizeStr"));
            // 获取sizIndex
            int sizIndex = getMaxSize(goodsId);
            // 箱数
            String boxQtyStr = String.valueOf(firstMap.get("BoxQty"));
            if (null == boxQtyStr || "".equals(boxQtyStr) || "null".equalsIgnoreCase(boxQtyStr)) {
                boxQtyStr = "0";
            }
            Integer boxQty = Integer.parseInt(boxQtyStr);
            if (null == boxQty || boxQty == 0) {
                boxQty = null;
            }
            // 总箱数
            String boxQtyTotalStr = String.valueOf(firstMap.get("BoxQtyTotal"));
            if (null == boxQtyTotalStr || "".equals(boxQtyTotalStr) || "null".equalsIgnoreCase(boxQtyTotalStr)) {
                boxQtyTotalStr = "0";
            }
            Integer boxQtyTotal = Integer.parseInt(boxQtyTotalStr);
            if (null == boxQtyTotal || boxQtyTotal == 0) {
                boxQtyTotal = null;
            }
            // 每箱的件数
            Integer oneBoxQty = Integer.parseInt(String.valueOf(firstMap.get("OneBoxQty") == null ? "0" : firstMap.get("OneBoxQty")));
            if (null == oneBoxQty || oneBoxQty == 0) {
                oneBoxQty = null;
            }

            Object obj = null;
            // 判断是新增还是更新
            if (null != boxQty && 0 != boxQty) {
                obj = commonDao.getData(" select count(1) from stocktakingDetailtemp where goodsId = '" + goodsId + "' and colorid  = '" + colorId + "' and stocktakingId = '" + stocktakingId + "' and sizeStr = '" + sizeStr + "' ");
            } else {
                obj = commonDao.getData(" select count(1) from stocktakingDetailtemp where goodsId = '" + goodsId + "' and colorid  = '" + colorId + "' and stocktakingId = '" + stocktakingId + "' ");
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
                sql.append(" Insert into stocktakingDetailTemp(IndexNo,stocktakingId,GoodsID,ColorID,x_").append(x).append(",Quantity,RetailSales,RetailAmount,BoxQty,SizeStr,SizeIndex,memo) Values(").append(maxIndexNo).append(",'").append(stocktakingId).append("', '").append(goodsId).append("','")
                        .append(colorId).append("' ");
                if (null == x || "".equals(x) || "null".equalsIgnoreCase(x)) {
                    sql.append(",null ");
                } else {
                    if (null != boxQty && 0 != boxQty) {
                        sql.append(",").append(quantity / boxQty);
                    } else {
                        sql.append(",").append(quantity);
                    }
                }
                sql.append(", ").append(quantity).append(", ").append(RetailSales);
                // 零售金额
                if (RetailSales == null) {
                    sql.append(", null ");
                } else {
                    sql.append(",  ").append(quantity).append("*").append(RetailSales);
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
                StringBuffer sql = new StringBuffer();
                sql.append(" Update stocktakingDetailTemp set BoxQty = ").append(boxQtyTotal);
                if (null != boxQty && 0 != boxQty) {
                    sql.append(", x_").append(x).append(" = ").append(oneBoxQty);
                    sql.append(", Quantity = ").append(quantitySum);
                } else {
                    sql.append(", x_").append(x).append(" = isnull(x_").append(x).append(",0) + ").append(quantity);
                    sql.append(", Quantity = Quantity + ").append(quantity);
                }
                // 零售金额合计
                if (RetailSales == null) {
                    sql.append(", RetailAmount = null ");
                } else {
                    if (null != boxQty && 0 != boxQty) {
                        sql.append(", RetailAmount =  RetailAmount + ").append(quantitySum).append("*").append(RetailSales);
                    } else {
                        sql.append(", RetailAmount =  RetailAmount + ").append(quantity).append("*").append(RetailSales);
                    }
                }
                // 备注
                if (meno != null && !"".equals(meno) && !"null".equalsIgnoreCase(meno)) {
                    sql.append(" , memo = '" + meno + "' ");
                } else {
                    sql.append(" , memo = null ");
                }
                sql.append(", SizeIndex = ").append(sizIndex).append(" where stocktakingId = '").append(stocktakingId).append("' and GoodsID = '").append(goodsId).append("' ");
                if (null != boxQty && 0 != boxQty) {
                    sql.append(" and SizeStr = '").append(sizeStr).append("' ");
                }
                sql.append(" and ColorID = '").append(colorId).append("' ");
                commonDao.executeSql(sql.toString());
            }
        }
        // 更新主表信息
        StringBuilder sumSql = new StringBuilder();
        sumSql.append(" Update stocktaking set QuantitySum = t.QtySum, RetailAmountSum = t.RetailAmountSum ,displaySizeGroup = ").append(" (SELECT STUFF((select DISTINCT ','''+g.GroupID +'''' from stocktakingDetailTemp a JOIN goods g ON a.goodsid=g.goodsid ")
                .append(" WHERE stocktakingId='" + stocktakingId + "' FOR XML PATH('')),1,1,''))  ").append(" from  (Select SUM(Quantity) QtySum, Sum(RetailAmount) RetailAmountSum ").append("  from stocktakingDetailTemp  where stocktakingId ='").append(stocktakingId).append("'   ) t  ")
                .append(" where stocktakingId = '").append(stocktakingId).append("' ");
        commonDao.executeSql(sumSql.toString());
        return stocktakingId;
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

    /**
     * 获取最大的下标
     * 
     * @param stocktakingId
     * @return
     */
    private int getMaxIndexNo(String stocktakingId) {
        int IndexNo = 0;
        StringBuffer maxNoSql = new StringBuffer();
        maxNoSql.append(" select max(IndexNo) IndexNo from stocktakingDetailtemp where stocktakingId =  '").append(stocktakingId).append("'");
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
    public void deleteInventorySheetDetail(List<Map<String, Object>> dataList, String stocktakingId) throws Exception {
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
                        commonDao.findForJdbc(" select sdt.stocktakingDetailId,isnull(x_" + x + ",0) xCount,isnull(sdt.Quantity,0) counts,isnull(boxQty,0) boxQty from stocktakingDetailTemp sdt with(updlock) "
                                + " join stocktakingDetail sd with(updlock) on sdt.stocktakingId = sd.stocktakingId and sdt.goodsId = sd.goodsId and sdt.colorId = sd.colorId where sdt.stocktakingId = '" + stocktakingId + "' and sdt.goodsId = '" + goodsId + "' and sdt.colorId = '" + colorId
                                + "' and sdt.sizeStr = '" + sizeStr + "' ; ");
            } else {
                list =
                        commonDao
                                .findForJdbc(" select sdt.stocktakingDetailId,isnull(x_" + x + ",0) xCount,isnull(sdt.Quantity,0) counts,isnull(boxQty,0) boxQty from stocktakingDetailTemp sdt with(updlock) "
                                        + " join stocktakingDetail sd with(updlock) on sdt.stocktakingId = sd.stocktakingId and sdt.goodsId = sd.goodsId and sdt.colorId = sd.colorId where sdt.stocktakingId = '" + stocktakingId + "' and sdt.goodsId = '" + goodsId + "' and sdt.colorId = '" + colorId
                                        + "' ; ");
            }
            if (list.size() > 0) {
                Map dataMap = (Map) list.get(0);
                int xCount = 0, count = 0, boxQty = 0;
                String stocktakingDetailId = null;
                if (null != dataMap) {
                    xCount = Integer.parseInt(String.valueOf(dataMap.get("xCount")));
                    count = Integer.parseInt(String.valueOf(dataMap.get("counts")));
                    boxQty = Integer.parseInt(String.valueOf(dataMap.get("boxQty")));
                    stocktakingDetailId = String.valueOf(dataMap.get("stocktakingDetailId"));
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
                    sb.append(" delete from stocktakingDetailTemp where stocktakingDetailId = '").append(stocktakingDetailId).append("' ; delete from stocktakingDetail where GoodsID = '").append(goodsId).append("' and stocktakingId = '").append(stocktakingId)
                            .append("' and ColorID = '" + colorId + "'  ; ");
                    // }else{
                    // sb.append(" update stocktakingDetailTemp set x_"+x+" = null,Quantity = ").append((count-(xCount*boxQty)))
                    // .append(" ,amount = (isnull(DiscountPrice,UnitPrice)*(").append((count-(xCount*boxQty)))
                    // .append(")),Discount =  (isnull(UnitPrice,0)-isnull(DiscountPrice,0))").append("*").append(count-(xCount*boxQty))
                    // .append(",RetailAmount = (RetailSales*(").append(count-(xCount*boxQty)).append(")) where stocktakingDetailId = '")
                    // .append(stocktakingDetailId).append("' ; update stocktakingDetail set Quantity = "+(count-(xCount*boxQty))+" where GoodsID = '")
                    // .append(goodsId).append("' and stocktakingId = '").append(stocktakingId).append("' and ColorID = '"+colorId+"'  ; ");
                    // }
                } else {// 散件
                        // 删除明细
                    if (count == xCount) {// 只有一个尺码
                        sb.append(" delete from stocktakingDetailTemp where stocktakingDetailId = '").append(stocktakingDetailId).append("' ; delete from stocktakingDetail where GoodsID = '").append(goodsId).append("' and stocktakingId = '").append(stocktakingId)
                                .append("' and ColorID = '" + colorId + "' and SizeID = '" + sizeId + "' ; ");
                    } else {
                        sb.append(" update stocktakingDetailTemp set x_" + x + " = null,Quantity = ").append(count - xCount).append(" ,RetailAmount = (RetailSales*(").append(count - xCount).append(")) where stocktakingDetailId = '").append(stocktakingDetailId)
                                .append("' ; update stocktakingDetail set Quantity = " + (count - xCount) + " where GoodsID = '").append(goodsId).append("' and stocktakingId = '").append(stocktakingId).append("' and ColorID = '" + colorId + "' and SizeID = '" + sizeId + "' ; ");
                    }
                }
                commonDao.executeSql(sb.toString());
                // 判断明细表是否还有记录
                String sizeStrSum = getSizeStrSum(getMaxSize(goodsId));
                String countStr = String.valueOf(commonDao.getData("select isnull(0" + sizeStrSum + ",0) from stocktakingDetailTemp where stocktakingDetailId = ? ", stocktakingDetailId));
                if (countStr != null && !countStr.isEmpty() && !"null".equalsIgnoreCase(countStr) && Integer.parseInt(countStr) == 0) {
                    commonDao.executeSql(" delete from stocktakingDetailTemp where stocktakingDetailId = ? ", stocktakingDetailId);
                }
                sb = new StringBuilder();
            }
        }
        // 重新计算货品价格
        sb.append(" update stocktaking set RetailAmountSum = (select sum(RetailAmount) from stocktakingDetailtemp where stocktakingId = '").append(stocktakingId).append("'), QuantitySum = (select sum(Quantity) from stocktakingDetailtemp where stocktakingId = '").append(stocktakingId)
                .append("') where stocktakingId = '").append(stocktakingId).append("' ;");
        commonDao.executeSql(sb.toString());
        // 若主表无数据时则删除主表
        List list = commonDao.findForJdbc(" select stocktakingId from stocktaking where quantitysum is null ");
        for (int i = 0; i < list.size(); i++) {
            Map map = (Map) list.get(i);
            String stocktakingID = String.valueOf(map.get("stocktakingId"));
            commonDao.executeSql(" delete from stocktaking where stocktakingId = ? ", stocktakingID);
        }
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
}
