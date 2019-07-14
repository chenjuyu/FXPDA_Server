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
import com.fuxi.core.common.service.StockService;
import com.fuxi.system.util.Client;
import com.fuxi.system.util.DataUtils;

/**
 * Title: StockServiceImpl Description: 进(出)仓单业务逻辑接口实现类
 * 
 * @author LYJ
 * 
 */
@Service("stockService")
@Transactional
public class StockServiceImpl implements StockService {

    @Autowired
    private CommonDao commonDao;

    @Override
    public String saveStockMoveIn(String employeeId, String StockId, String memo, List<Map<String, Object>> dataList, String brandId, Client client) {
        // 判断StockId是否为空，做新增主表
        // if("".equals(StockId)){
        // // 生成ID
        // StockId = commonDao.getNewIDValue(36);
        // // 生成No
        // String No = commonDao.getNewNOValue(36, StockId ,
        // client.getDeptCode());
        // if(StockId == null || No == null){
        // throw new BusinessException("生成主键/单号失败");
        // }
        // StringBuilder insertMaster = new StringBuilder();
        // // 插入转仓单(主表)
        // insertMaster.append(" insert into Stock(StockID, No, Date, WarehouseInID, WarehouseOutID, DepartmentID, EmployeeID,")
        // .append(" QuantitySum, AmountSum, MadeBy, MadeByDate, StockInNo, StockOutNo, Memo, Year, Month, DisplaySizeGroup,")
        // .append(" RetailAmountSum, MoveInType, MoveOutType, OutAmountSum,brandId) ")
        // .append(" values('").append(StockId).append("','").append(No).append("','")
        // .append(DataUtils.str2Timestamp(DataUtils.formatDate())).append("','")
        // .append(warehouseInId).append("','").append(warehouseOutId).append("','")
        // .append(client.getDeptID()).append("','").append(employeeId)
        // .append("', null , null , '").append(client.getUserName()).append("','")
        // .append(DataUtils.gettimestamp()).append("', null,null,'")
        // .append(memo).append("','").append(DataUtils.getYear())
        // .append("','").append(DataUtils.getStringMonth())
        // .append("',null,null,null,null,null,'").append(brandId).append("'); ");
        // commonDao.executeSql(insertMaster.toString());
        // }

        // -----------------其它操作------------------//
        // 获取同货品,同颜色的总数量
        for (int i = 0; i < dataList.size(); i++) {
            Map map = dataList.get(i);
            int quantitySum = 0;
            for (int j = 0; j < dataList.size(); j++) {
                Map temp = dataList.get(j);
                if (String.valueOf(temp.get("GoodsID")).equals(String.valueOf(map.get("GoodsID"))) && String.valueOf(temp.get("ColorID")).equals(String.valueOf(map.get("ColorID")))) {
                    int count = Integer.parseInt(String.valueOf(temp.get("Quantity")));
                    quantitySum += count;
                }
            }
            map.put("QuantitySum", quantitySum);
        }
        // 获取最大的下标值
        int maxIndexNo = getMaxIndexNo(StockId);
        // 循环查询或更新
        dataList = mergeData(dataList);
        for (int i = 0; i < dataList.size(); i++) {
            Map firstMap = (Map) dataList.get(i);
            String goodsId = String.valueOf(firstMap.get("GoodsID"));
            String colorId = String.valueOf(firstMap.get("ColorID"));
            String sizeId = String.valueOf(firstMap.get("SizeID"));
            int quantity = Integer.parseInt(String.valueOf(firstMap.get("Quantity")));
            int quantitySum = Integer.parseInt(String.valueOf(firstMap.get("QuantitySum")));
            String x = String.valueOf(commonDao.getData("select no from sizegroupsize where sizegroupid = (select GroupID from goods where GoodsID = ?) and SizeId = '" + sizeId + "' ; ", goodsId));
            BigDecimal RelationUnitPrice = firstMap.get("DiscountPrice") == null ? null : new BigDecimal(String.valueOf(firstMap.get("DiscountPrice"))).setScale(2, BigDecimal.ROUND_HALF_UP);
            BigDecimal UnitPrice = firstMap.get("UnitPrice") == null ? null : new BigDecimal(String.valueOf(firstMap.get("UnitPrice"))).setScale(2, BigDecimal.ROUND_HALF_UP);
            BigDecimal RetailSales = firstMap.get("RetailSales") == null ? null : new BigDecimal(String.valueOf(firstMap.get("RetailSales"))).setScale(2, BigDecimal.ROUND_HALF_UP);
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
            // 获取sizIndex
            int sizIndex = getMaxSize(goodsId);
            // 判断是新增还是更新
            Object obj = commonDao.getData(" select count(1) from StockDetailtemp where goodsId = '" + goodsId + "' and colorid  = '" + colorId + "' and StockId = '" + StockId + "' ");
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
                sql.append(" Insert into StockDetailtemp(IndexNo,StockId,GoodsID,ColorID,x_").append(x).append(",Quantity,RelationUnitPrice,UnitPrice,RetailSales,Amount,RelationAmount,RetailAmount,BoxQty,SizeIndex) Values(").append(maxIndexNo).append(",'").append(StockId).append("', '")
                        .append(goodsId).append("','").append(colorId).append("' ");
                if (null == x || "".equals(x) || "null".equalsIgnoreCase(x)) {
                    sql.append(",null ");
                } else {
                    if (null != boxQty && 0 != boxQty) {
                        sql.append(",").append(quantity / boxQty);
                    } else {
                        sql.append(",").append(quantity);
                    }
                }
                sql.append(", ").append(quantity).append(", ").append(RelationUnitPrice).append(", ").append(UnitPrice);
                sql.append(", ").append(RetailSales);
                // 实收金额
                if (UnitPrice == null) {
                    sql.append(", Amount=null ");
                } else {
                    sql.append(", Amount =  ").append(quantitySum).append("*").append(UnitPrice);
                }
                // 实收金额
                if (RelationUnitPrice == null) {
                    sql.append(", RelationAmount=null ");
                } else {
                    sql.append(", RelationAmount =  ").append(quantitySum).append("*").append(RelationUnitPrice);
                }
                // 零售金额
                if (RetailSales == null) {
                    sql.append(", null ");
                } else {
                    sql.append(", ").append(quantity).append("*").append(RetailSales);
                }
                // 箱数
                sql.append(",").append(boxQty).append(",").append(sizIndex).append(") ");
                commonDao.executeSql(sql.toString());
            } else {
                // ----------------------------------------//
                // ------------修改子表数据-------------------//
                // ----------------------------------------//
                // 判断客户对应的货品是否存在价格
                StringBuffer sql = new StringBuffer();
                sql.append(" Update StockDetailtemp set RelationUnitPrice = ").append(RelationUnitPrice).append(", UnitPrice = ").append(UnitPrice).append(", RetailSales = ").append(RetailSales).append(",BoxQty = ").append(boxQty).append("");
                if (null != boxQty && 0 != boxQty) {
                    sql.append(", x_").append(x).append(" =  ").append(quantity / boxQty);
                } else {
                    sql.append(", x_").append(x).append(" =  ").append(quantity);
                }
                sql.append(", Quantity=").append(quantitySum).append(", PurchaseAmount = PurchasePrice*").append(quantitySum);
                // 实收金额
                sql.append(",Amount =  ").append(quantitySum).append("*").append(UnitPrice);
                sql.append(",RelationAmount =  ").append(quantitySum).append("*").append(RelationUnitPrice);
                sql.append(", SizeIndex = ").append(sizIndex).append(" where StockId = '").append(StockId).append("' and GoodsID = '").append(goodsId).append("' and ColorID = '").append(colorId).append("' ");
                commonDao.executeSql(sql.toString());
            }
        }
        // 更新主表信息
        StringBuffer sumSql = new StringBuffer();
        sumSql.append(" update Stock set MadeByDate = '").append(DataUtils.gettimestamp()).append("' ,QuantitySum = (select sum(Quantity) from StockDetailTemp where StockId = '").append(StockId).append("'), RelationAmountSum = (select sum(RelationAmount) from StockDetailTemp where StockId = '")
                .append(StockId).append("'), RetailAmountSum = (select sum(RetailAmount) from StockDetailTemp where StockId = '").append(StockId).append("'), PurchaseAmountSum = (select sum(PurchaseAmount) from Stockdetailtemp where StockId = '").append(StockId)
                .append("'), AmountSum = (select sum(Amount) from StockDetailTemp where StockId = '").append(StockId).append("'), displaySizeGroup = ").append(" (SELECT STUFF((select DISTINCT ','''+g.GroupID +'''' from StockDetailTemp a JOIN goods g ON a.goodsid=g.goodsid ")
                .append(" WHERE StockId='" + StockId + "' FOR XML PATH('')),1,1,''))  where StockId = '").append(StockId).append("' ; ");
        commonDao.executeSql(sumSql.toString());
        return StockId;
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
    private int getMaxIndexNo(String StockId) {
        int IndexNo = 0;
        StringBuffer maxNoSql = new StringBuffer();
        maxNoSql.append(" select max(IndexNo) IndexNo from StockDetailtemp where StockId =  '").append(StockId).append("'");
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
    public void deleteStockMoveInDetail(List<Map<String, Object>> dataList, String StockId) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < dataList.size(); i++) {
            Map map = (Map) dataList.get(i);
            String goodsId = String.valueOf(map.get("GoodsID"));
            String colorId = String.valueOf(map.get("ColorID"));
            String sizeId = String.valueOf(map.get("SizeID"));
            String oneBoxQtyStr = String.valueOf(map.get("OneBoxQty"));
            // 查询尺码对应的尺码组Code
            String x = String.valueOf(commonDao.getData("select no from sizegroupsize where sizegroupid = (select GroupID from goods where GoodsID = ?) and SizeId = '" + sizeId + "' ; ", goodsId));
            // 获取原来同单,同货,同码,同色,的货品数量,明细ID,尺码数量
            List list =
                    commonDao.findForJdbc(" select sdt.StockdetailId,isnull(x_" + x + ",0) xCount,isnull(sdt.Quantity,0) counts,isnull(boxQty,0) boxQty from StockDetailtemp sdt with(updlock) "
                            + " join Stockdetail sd with(updlock) on sdt.StockId = sd.StockId and sdt.goodsId = sd.goodsId and sdt.colorId = sd.colorId where sdt.StockId = '" + StockId + "' and sdt.goodsId = '" + goodsId + "' and sdt.colorId = '" + colorId + "' ; ");
            if (list.size() > 0) {
                Map dataMap = (Map) list.get(0);
                int xCount = 0, count = 0, boxQty = 0;
                String StockdetailId = null;
                if (null != dataMap) {
                    xCount = Integer.parseInt(String.valueOf(dataMap.get("xCount")));
                    count = Integer.parseInt(String.valueOf(dataMap.get("counts")));
                    boxQty = Integer.parseInt(String.valueOf(dataMap.get("boxQty")));
                    StockdetailId = String.valueOf(dataMap.get("StockdetailId"));
                } else {
                    throw new BusinessException("修改货品数量或价格失败");
                }
                if (boxQty > 0) {// 箱条码
                    int oneBoxQty = 1;
                    if (null != oneBoxQtyStr && !oneBoxQtyStr.isEmpty() && !"null".equals(oneBoxQtyStr)) {
                        oneBoxQty = Integer.parseInt(oneBoxQtyStr);
                    }
                    // 删除明细
                    if (count == oneBoxQty) {// 只有一个尺码
                        sb.append(" delete from StockDetailtemp where StockdetailId = '").append(StockdetailId).append("' ; delete from StockDetail where GoodsID = '").append(goodsId).append("' and StockId = '").append(StockId).append("' and ColorID = '" + colorId + "'  ; ");
                    } else {
                        sb.append(" update StockDetailtemp set x_" + x + " = null,Quantity = ").append((count - (xCount * boxQty))).append(" ,amount = (isnull(UnitPrice,0)*(").append((count - (xCount * boxQty))).append(")), RelationAmount = (RelationUnitPrice*(").append(count - (xCount * boxQty))
                                .append(")),").append(" RetailAmount = (RetailSales*(").append(count - (xCount * boxQty)).append(")) where StockdetailId = '").append(StockdetailId).append("' ; update StockDetail set Quantity = " + (count - (xCount * boxQty)) + " where GoodsID = '").append(goodsId)
                                .append("' and StockId = '").append(StockId).append("' and ColorID = '" + colorId + "'  ; ");
                    }
                } else {// 散件
                        // 删除明细
                    if (count == xCount) {// 只有一个尺码
                        sb.append(" delete from StockDetailtemp where StockdetailId = '").append(StockdetailId).append("' ; delete from StockDetail where GoodsID = '").append(goodsId).append("' and StockId = '").append(StockId)
                                .append("' and ColorID = '" + colorId + "' and SizeID = '" + sizeId + "' ; ");
                    } else {
                        sb.append(" update StockDetailtemp set x_" + x + " = null,Quantity = ").append(count - xCount).append(" ,RelationAmount = RelationUnitPrice *(").append(count - xCount).append("),amount = UnitPrice*(").append(count - xCount).append("),RetailAmount = (RetailSales*(")
                                .append(count - xCount).append(")) where StockdetailId = '").append(StockdetailId).append("' ; update StockDetail set Quantity = " + (count - xCount) + " where GoodsID = '").append(goodsId).append("' and StockId = '").append(StockId)
                                .append("' and ColorID = '" + colorId + "' and SizeID = '" + sizeId + "' ; ");
                    }
                }
                commonDao.executeSql(sb.toString());
                // 判断明细表是否还有记录
                String sizeStrSum = getSizeStrSum(getMaxSize(goodsId));
                String countStr = String.valueOf(commonDao.getData("select isnull(0" + sizeStrSum + ",0) from StockDetailtemp where StockdetailId = ? ", StockdetailId));
                if (countStr != null && !countStr.isEmpty() && !"null".equalsIgnoreCase(countStr) && Integer.parseInt(countStr) == 0) {
                    commonDao.executeSql(" delete from StockDetailtemp where StockdetailId = ? ", StockdetailId);
                }
                sb = new StringBuilder();
            }
        }
        // 重新计算货品价格
        sb.append(" update Stock set AmountSum = (select sum(Amount) from StockDetailtemp where StockId = '").append(StockId).append("'), RelationAmountSum = (select sum(RelationAmount) from StockDetailTemp where StockId = '").append(StockId)
                .append("'), RetailAmountSum = (select sum(RetailAmount) from StockDetailtemp where StockId = '").append(StockId).append("'), PurchaseAmountSum = (select sum(PurchaseAmount) from Stockdetailtemp where StockId = '").append(StockId)
                .append("'), QuantitySum = (select sum(Quantity) from StockDetailtemp where StockId = '").append(StockId).append("') where StockId = '").append(StockId).append("' ;");
        commonDao.executeSql(sb.toString());
        // 若主表无数据时则删除主表
        List list = commonDao.findForJdbc(" select StockId from Stock where quantitysum is null ");
        for (int i = 0; i < list.size(); i++) {
            Map map = (Map) list.get(i);
            String StockID = String.valueOf(map.get("StockId"));
            commonDao.executeSql(" delete from Stock where StockId = ? ", StockID);
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

    // 去除集合中的重复记录
    public List<Map<String, Object>> mergeData(List<Map<String, Object>> dataList) {
        for (int i = 0; i < dataList.size() - 1; i++) {
            Map temp1 = (Map) dataList.get(i);
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
        return dataList;
    }

    @Override
    public int coverSave(String StockId, List<Map<String, Object>> dataList, Client client) throws Exception {
        int dir = Integer.parseInt(String.valueOf(commonDao.getData(" select QuantitySum from Stock where StockId = ? ", StockId)));
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
        coverSaveToUpdate(StockId, dir, updateList);
        // 删除
        deleteStockMoveInDetail(delList, StockId);
        // 更新主表
        // 重新计算货品价格
        StringBuffer sb = new StringBuffer();
        sb.append(" update Stock set AmountSum = (select sum(Amount) from Stockdetailtemp where StockId = '").append(StockId).append("'), RelationAmountSum = (select sum(RelationAmount) from StockDetailTemp where StockId = '").append(StockId)
                .append("'), RetailAmountSum = (select sum(RetailAmount) from Stockdetailtemp where StockId = '").append(StockId).append("'), PurchaseAmountSum = (select sum(PurchaseAmount) from Stockdetailtemp where StockId = '").append(StockId)
                .append("'), QuantitySum = (select sum(Quantity) from Stockdetailtemp where StockId = '").append(StockId).append("') where StockId = '").append(StockId).append("' ; ");
        int count = commonDao.executeSql(sb.toString());
        return count;
    }

    /**
     * 修改子表记录(条码校验)
     * 
     * @param StockId
     * @param direction
     * @param updateList
     */
    private void coverSaveToUpdate(String StockId, int dir, List<Map<String, Object>> updateList) {
        for (int i = 0; i < updateList.size(); i++) {
            StringBuffer sql = new StringBuffer();
            Map<String, Object> temp = updateList.get(i);
            BigDecimal RelationUnitPrice = new BigDecimal(String.valueOf(temp.get("DiscountPrice"))).setScale(2, BigDecimal.ROUND_HALF_UP);
            BigDecimal UnitPrice = new BigDecimal(String.valueOf(temp.get("UnitPrice"))).setScale(2, BigDecimal.ROUND_HALF_UP);
            BigDecimal RetailSales = new BigDecimal(String.valueOf(temp.get("RetailSales"))).setScale(2, BigDecimal.ROUND_HALF_UP);
            String tGoodsId = String.valueOf(temp.get("GoodsID"));
            String tColorId = String.valueOf(temp.get("ColorID"));
            String tSizeId = String.valueOf(temp.get("SizeID"));
            int tBoxQty = Math.abs(Integer.parseInt(String.valueOf(temp.get("BoxQty"))));
            int tQuantity = Math.abs(Integer.parseInt(String.valueOf(temp.get("Quantity"))));
            int tQty = Math.abs(Integer.parseInt(String.valueOf(temp.get("Qty"))));
            String x = String.valueOf(commonDao.getData("select no from sizegroupsize where sizegroupid = (select GroupID from goods where GoodsID = ?) and SizeId = '" + tSizeId + "' ; ", tGoodsId));
            Object obj = commonDao.getData(" select count(1) from Stockdetailtemp where goodsId = '" + tGoodsId + "' and colorid  = '" + tColorId + "' and StockId = '" + StockId + "' ");
            // 获取sizIndex
            int sizIndex = getMaxSize(tGoodsId);
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
                int maxIndexNo = getMaxIndexNo(StockId);
                maxIndexNo++;
                sql.append(" Insert into StockDetailTemp(IndexNo,StockId,GoodsID,ColorID,x_").append(x).append(",Quantity,UnitPrice,RetailSales,Amount,RelationAmount,RetailAmount,BoxQty,SizeIndex) Values(").append(maxIndexNo).append(",'").append(StockId).append("', '").append(tGoodsId)
                        .append("','").append(tColorId).append("' ");
                if (null == x || "".equals(x) || "null".equalsIgnoreCase(x)) {
                    sql.append(",null ");
                } else {
                    if (Math.abs(tBoxQty) > 0) {
                        sql.append(",").append(tQty / tBoxQty);
                    } else {
                        sql.append(",").append(tQty);
                    }
                }
                sql.append(", ").append(tQty).append(", ").append(UnitPrice).append(", ").append(RetailSales);
                // 实收金额
                sql.append(", ").append(tQty).append("*").append(UnitPrice);
                // 结算金额
                if (RelationUnitPrice == null) {
                    sql.append(", null ");
                } else {
                    sql.append(",  ").append(tQty).append("*").append(RelationUnitPrice);
                }
                // 零售金额
                if (RetailSales == null) {
                    sql.append(", null ");
                } else {
                    sql.append(",  ").append(tQty).append("*").append(RetailSales);
                }
                // 箱数
                sql.append(",").append(tBoxQty == 0 ? null : tBoxQty).append(",").append(sizIndex).append(") ;");
            } else {// 修改
                int quantitySum = 0;
                if (Math.abs(tBoxQty) > 0) {
                    quantitySum = Math.abs(Integer.parseInt(String.valueOf(temp.get("QuantitySum"))));
                } else {
                    int ttQty = Integer.parseInt(String.valueOf(commonDao.getData(" select sum(Quantity) from StockDetailTemp where StockId = '" + StockId + "' and goodsId = '" + tGoodsId + "' and colorId = '" + tColorId + "' ")));
                    quantitySum = Math.abs(ttQty) + Math.abs(tQty) - Math.abs(tQuantity);
                }
                if (dir < 0) {
                    quantitySum = -quantitySum;
                    tBoxQty = -tBoxQty;
                    tQty = -tQty;
                }
                sql.append(" Update StockDetailTemp set  UnitPrice =   ").append(UnitPrice).append(", RetailSales = ").append(RetailSales).append(",BoxQty = ").append(tBoxQty == 0 ? null : tBoxQty);

                if (Math.abs(tBoxQty) > 0) {
                    sql.append(", x_").append(x).append(" =  ").append(tQty / tBoxQty);
                } else {
                    sql.append(", x_").append(x).append(" =  ").append(tQty);
                }

                sql.append(", Quantity=").append(quantitySum).append(", PurchaseAmount = PurchasePrice*").append(quantitySum);
                // 结算金额
                if (RelationUnitPrice == null) {
                    sql.append(" , RelationAmount=null ");
                } else {
                    sql.append(", RelationAmount =  ").append(quantitySum).append("*").append(RelationUnitPrice);
                }
                // 实收金额
                if (UnitPrice == null) {
                    sql.append(" , Amount=null ");
                } else {
                    sql.append(", Amount =  ").append(quantitySum).append("*").append(UnitPrice);
                }
                sql.append(", SizeIndex = ").append(sizIndex).append(" where StockId = '").append(StockId).append("' and GoodsID = '").append(tGoodsId).append("' and ColorID = '").append(tColorId).append("' ; ");
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

}
