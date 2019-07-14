package com.fuxi.core.common.service.impl;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.fuxi.core.common.dao.impl.CommonDao;
import com.fuxi.core.common.exception.BusinessException;
import com.fuxi.core.common.service.PackingBoxService;
import com.fuxi.system.util.Client;
import com.fuxi.system.util.DataUtils;

/**
 * Title: PackingBoxServiceImpl Description: 装箱单业务逻辑接口实现类
 * 
 * @author LYJ
 * 
 */
@Service("packingBoxService")
@Transactional
public class PackingBoxServiceImpl implements PackingBoxService {

    @Autowired
    private CommonDao commonDao;

    @Override
    public String savePackingBox(String goodsId, String colorId, String sizeId, String qtyStr, String packingBoxId, String relationType, String relationId, String relationNo, String customerId, String departmentId, String employeeId, String brandId, String boxNo, String type, String memo,
            String retailSales, Client client) throws Exception {
        if (null == packingBoxId || "".equals(packingBoxId) || "null".equalsIgnoreCase(packingBoxId)) {
            // 生成单号和ID
            int tag = 584;
            // 生成ID
            packingBoxId = commonDao.getNewIDValue(tag);
            // 生成No
            String deptType = client.getDeptType();
            String No = null;
            if (deptType != null && ("直营店".equals(deptType) || "加盟店".equals(deptType))) {
                No = commonDao.getNewNOValue(tag, packingBoxId, client.getDeptCode());
            } else {
                No = commonDao.getNewNOValue(tag, packingBoxId);
            }
            if (packingBoxId == null || No == null) {
                throw new BusinessException("生成主键/单号失败");
            }
            StringBuilder sql = new StringBuilder();
            sql.append(" insert into PackingBox(PackingBoxID, Type, No, Date, DepartmentID, EmployeeID, QuantitySum, MadeBy, MadeByDate, Memo, RelationID, " + " RelationNo, Year, Month, CustomerID, BrandID, RelationWarehouseID) " + " values(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?); ");
            commonDao.executeSql(sql.toString(), packingBoxId, type, No, DataUtils.str2Timestamp(DataUtils.formatDate()), departmentId, employeeId, 0, client.getUserName(), DataUtils.gettimestamp(), memo, relationId, relationNo, DataUtils.getYear(), DataUtils.getStringMonth(), customerId, brandId,
                    departmentId);
        }
        // 获取其它数据//
        // 转换零售价类型
        int qty = Integer.parseInt(qtyStr);
        if (null == retailSales || "".equals(retailSales) || "null".equals(retailSales)) {
            retailSales = "0";
        }
        BigDecimal bdRetailSales = new BigDecimal(retailSales).setScale(2, BigDecimal.ROUND_HALF_UP);
        BigDecimal retailAmount = bdRetailSales.multiply(new BigDecimal(qty));
        // 获取最大的下标值
        int maxIndexNo = getMaxIndexNo(packingBoxId);
        // 获取尺码对应的横向下标
        String x = String.valueOf(commonDao.getData("select no from sizegroupsize where sizegroupid = (select GroupID from goods where GoodsID = ?) and SizeId = '" + sizeId + "' ; ", goodsId));
        // 获取sizIndex
        int sizIndex = getMaxSize(goodsId);
        // 判断新增还是修改
        int isExit1 = commonDao.getDataToInt(" select count(1) from packingBoxDetailtemp where goodsId = ? and colorId = ? and packingBoxId = ? and boxNo = ? ", goodsId, colorId, packingBoxId, boxNo);
        int isExit2 = commonDao.getDataToInt(" select count(1) from PackingBoxDetailPDA where goodsId = ? and colorId = ? and sizeId = ? and packingBoxId = ? and boxNo = ?  ", goodsId, colorId, sizeId, packingBoxId, boxNo);
        StringBuffer sb = new StringBuffer();
        // PackingBoxDetailTemp
        if (isExit1 < 1) {
            // 新增
            maxIndexNo++;
            sb.append(" insert into PackingBoxDetailTemp(PackingBoxID, x_" + x + " ,IndexNo, GoodsID, ColorID, Quantity, Memo, SizeIndex, BoxNo) ").append(" values('").append(packingBoxId).append("' ");
            if (null == x || "".equals(x) || "null".equalsIgnoreCase(x)) {
                sb.append(",null ");
            } else {
                sb.append(",").append(qty);
            }
            sb.append(",'" + maxIndexNo + "', '" + goodsId + "', '" + colorId + "', " + qty + ", null, '" + sizIndex + "', '" + boxNo + "' ); ");
        } else {
            // 修改
            sb.append(" update PackingBoxDetailTemp set Quantity = Quantity + " + qty + " ");
            if (null == x || "".equals(x) || "null".equalsIgnoreCase(x)) {
                sb.append(", x_").append(x).append(" = null ");
            } else {
                sb.append(", x_").append(x).append(" = isnull(x_").append(x).append(",0)+").append(qty);
            }
            sb.append(" where PackingBoxID = '" + packingBoxId + "' and BoxNo = '" + boxNo + "' and GoodsID = '" + goodsId + "' and ColorID = '" + colorId + "' ; ");
        }
        // PackingBoxDetailPDA
        if (isExit2 < 1) {
            // 新增
            sb.append(" insert into PackingBoxDetailPDA(RelationID, PackingBoxID, BoxNo, GoodsID, ColorID, SizeID, Quantity, retailSales, retailAmount) values('" + relationId + "'," + "'" + packingBoxId + "','" + boxNo + "','" + goodsId + "','" + colorId + "','" + sizeId + "'," + qty + ","
                    + bdRetailSales + "," + retailAmount + ") ; ");
        } else {
            // 修改
            sb.append(" update PackingBoxDetailPDA set Quantity = Quantity + " + qty + ",retailSales = " + bdRetailSales + ",retailAmount = retailAmount+" + retailAmount + " " + " where PackingBoxID = '" + packingBoxId + "' and BoxNo = '" + boxNo + "' and GoodsID = '" + goodsId
                    + "' and ColorID = '" + colorId + "' and SizeID = '" + sizeId + "' ;  ");
        }
        commonDao.executeSql(sb.toString());
        // 重新计算数量和尺码组
        // 更新主表信息
        StringBuilder sumSql = new StringBuilder();
        sumSql.append(" Update PackingBox set QuantitySum = t.QtySum, displaySizeGroup = ").append(" (SELECT STUFF((select DISTINCT ','''+g.GroupID +'''' from PackingBoxDetailTemp a JOIN goods g ON a.goodsid=g.goodsid ")
                .append(" WHERE packingBoxId='" + packingBoxId + "' FOR XML PATH('')),1,1,''))  ").append(" from  (Select SUM(Quantity) QtySum from PackingBoxDetailTemp  where packingBoxId ='").append(packingBoxId).append("'   ) t  ").append(" where packingBoxId = '").append(packingBoxId)
                .append("' ");
        commonDao.executeSql(sumSql.toString());
        return packingBoxId;
    }

    @Override
    public boolean completePackingBox(String relationId) throws Exception {
        int count = 0;
        // 创建新表
        int exits = Integer.parseInt(String.valueOf(commonDao.getData(" select count(1) from dbo.[sysobjects] where name = 'AlreadyPackingBoxPDA' ")));
        if (exits < 1) {
            commonDao.executeSql("  create table AlreadyPackingBoxPDA(ID int primary key identity(1,1),RelationID varchar(50) not null) ");
        }
        int flag = Integer.parseInt(String.valueOf(commonDao.getData(" select count(1) from AlreadyPackingBoxPDA where RelationID = ? ", relationId)));
        if (flag < 1) {
            count = commonDao.executeSql(" insert into AlreadyPackingBoxPDA(RelationID) values (?)", relationId);
        }
        return count > 0 ? true : false;
    }

    /**
     * 获取最大的下标
     * 
     * @param stocktakingId
     * @return
     */
    private int getMaxIndexNo(String packingBoxId) {
        int IndexNo = 0;
        StringBuffer maxNoSql = new StringBuffer();
        maxNoSql.append(" select max(IndexNo) IndexNo from packingBoxDetailTemp where packingBoxId =  '").append(packingBoxId).append("'");
        List rsList = commonDao.findForJdbc(maxNoSql.toString());
        if (rsList.size() > 0) {
            if (((Map) rsList.get(0)).get("IndexNo") != null) {
                IndexNo = (Integer) ((Map) rsList.get(0)).get("IndexNo");
            }
        }
        return IndexNo;
    }

    /**
     * 获取尺码组中的最大尺码
     * 
     * @param goodsId
     * @return
     */
    private int getMaxSize(String goodsId) {
        String maxSizeSql = "select max(no) as maxsize from SizeGroupSize where sizeGroupId = (select groupId from goods where goodsId = ?)";
        Map sizeMap = (Map) commonDao.findForJdbc(maxSizeSql, goodsId).get(0);
        int maxSize = (Integer) sizeMap.get("maxsize");
        if (maxSize < 1) {
            maxSize = 1;
        }
        return maxSize;
    }

    @Override
    public void updatePacking(String packingBoxId, String boxNo, String goodsId, String colorId, String sizeId, String retailSales, String qtyStr) throws Exception {
        // 转换零售价类型
        int qty = Integer.parseInt(qtyStr);
        if (null == retailSales || "".equals(retailSales) || "null".equals(retailSales)) {
            retailSales = "0";
        }
        BigDecimal bdRetailSales = new BigDecimal(retailSales).setScale(2, BigDecimal.ROUND_HALF_UP);
        BigDecimal retailAmount = bdRetailSales.multiply(new BigDecimal(qty));
        StringBuilder sb = new StringBuilder();
        String x = String.valueOf(commonDao.getData("select no from sizegroupsize where sizegroupid = (select GroupID from goods where GoodsID = ?) and SizeId = '" + sizeId + "' ; ", goodsId));
        // PackingBoxDetailTemp
        // 修改
        sb.append(" update PackingBoxDetailTemp set Quantity = " + qty + " ");
        if (null == x || "".equals(x) || "null".equalsIgnoreCase(x)) {
            sb.append(", x_").append(x).append(" = null ");
        } else {
            sb.append(", x_").append(x).append(" = ").append(qty);
        }
        sb.append(" where PackingBoxID = '" + packingBoxId + "' and BoxNo = '" + boxNo + "' and GoodsID = '" + goodsId + "' and ColorID = '" + colorId + "' ; ");
        commonDao.executeSql(sb.toString());
        sb = new StringBuilder();
        // PackingBoxDetailPDA
        // 修改
        sb.append(" update PackingBoxDetailPDA set Quantity = " + qty + ",retailSales = " + bdRetailSales + ",retailAmount = " + retailAmount + " " + " where PackingBoxID = '" + packingBoxId + "' and BoxNo = '" + boxNo + "' and GoodsID = '" + goodsId + "' and ColorID = '" + colorId
                + "' and SizeID = '" + sizeId + "' ;  ");
        commonDao.executeSql(sb.toString());
        // 更新主表信息
        StringBuilder sumSql = new StringBuilder();
        sumSql.append(" Update PackingBox set QuantitySum = t.QtySum, displaySizeGroup = ").append(" (SELECT STUFF((select DISTINCT ','''+g.GroupID +'''' from PackingBoxDetailTemp a JOIN goods g ON a.goodsid=g.goodsid ")
                .append(" WHERE packingBoxId='" + packingBoxId + "' FOR XML PATH('')),1,1,''))  ").append(" from  (Select SUM(Quantity) QtySum from PackingBoxDetailTemp  where packingBoxId ='").append(packingBoxId).append("'   ) t  ").append(" where packingBoxId = '").append(packingBoxId)
                .append("' ");
        commonDao.executeSql(sumSql.toString());
    }

    @Override
    public boolean deleteAlreadyPackingBoxNo(String packingBoxId) {
        int count = 0;
        count += commonDao.executeSql(" delete from alreadypackingboxpda where relationId = (select relationId from packingbox where packingBoxId = ?) ", packingBoxId);
        count += commonDao.executeSql(" delete from packingboxdetailtemp where packingBoxId = ? ", packingBoxId);
        count += commonDao.executeSql(" delete from packingboxdetailpda where packingBoxId = ? ", packingBoxId);
        count += commonDao.executeSql(" delete from packingbox where packingBoxId = ? ", packingBoxId);
        return count > 0 ? true : false;
    }

}
