package com.fuxi.core.common.service.impl;

import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.fuxi.core.common.dao.impl.CommonDao;
import com.fuxi.core.common.exception.BusinessException;
import com.fuxi.core.common.service.ShelvesInService;
import com.fuxi.system.util.Client;
import com.fuxi.system.util.DataUtils;
import com.fuxi.system.util.MyTools;

/**
 * Title: ShelvesInServiceImpl Description: 仓储上架业务逻辑接口(仓位管理)
 * 
 * @author LYJ
 * 
 */
@Service("shelvesInService")
@Transactional
public class ShelvesInServiceImpl implements ShelvesInService {

    @Autowired
    private CommonDao commonDao;

    @Override
    public synchronized int saveStorageInMsg(String departmentId, String type, String stockNo, String storageId, String barcode, int qty, String memo, String goodsId, String colorId, String sizeId, Client client) {
        int count = 0, num = 0;
        String storageInId = null;
        StringBuilder sql = new StringBuilder();
        // 检查货架上是否已经存在该货品
        StringBuilder sb = new StringBuilder();
        sb.append(" select StorageInID, count(1) Quantity from StorageIn where storageID = '").append(storageId).append("' and departmentID = '").append(departmentId).append("' and goodsID = '").append(goodsId).append("' and colorID = '").append(colorId).append("' and sizeID = '").append(sizeId)
                .append("' ");
        if (stockNo != null && !stockNo.isEmpty() && !"".equals(stockNo)) {
            sb.append(" and relationNo = '").append(stockNo).append("' ");
        }
        sb.append(" and type = '").append(type).append("' group by StorageInID ");
        List list = commonDao.findForJdbc(sb.toString());
        if (list.size() < 1) {
            num = 0;
        } else {
            Map map = (Map) list.get(0);
            num = Integer.parseInt(MyTools.formatObjectOfNumber(map.get("Quantity")));
            storageInId = String.valueOf(map.get("StorageInID"));
        }
        if (num > 0) {
            // 判断单据中上架记录的货品总数是否与单据中的货品数量相等
            if (stockNo != null && !stockNo.isEmpty() && !"".equals(stockNo)) {
                int quantity = Integer.parseInt(MyTools.formatObjectOfNumber(commonDao.getData(" select Quantity from StockDetail where goodsId = '" + goodsId + "' and colorId = '" + colorId + "' and sizeId = '" + sizeId + "' and stockId = (select stockId from Stock where No = ?) ", stockNo)));
                int complete = Integer.parseInt(MyTools.formatObjectOfNumber(commonDao.getData(" select sum(Quantity) from StorageIn where goodsID = '" + goodsId + "' and colorID = '" + colorId + "' and sizeID = '" + sizeId + "' and relationNo = '" + stockNo + "' and type = '" + type + "' ")));
                if (complete < quantity && (complete + qty) <= quantity) {
                    sql.append(" update StorageIn set quantity = quantity + ?, madeby = ?, madeDate = ?, type = ? where storageInID = ? ");
                    count = commonDao.executeSql(sql.toString(), qty, client.getUserName(), DataUtils.gettimestamp(), type, storageInId);
                    if (count == 0) {
                        System.out.println("上架单据" + stockNo + "时修改上架表返回0,storageInId=" + storageInId);
                    }
                }
            } else {
                sql.append(" update StorageIn set quantity = quantity + ?, madeby = ?, madeDate = ?, type = ? where storageInID = ? ");
                count = commonDao.executeSql(sql.toString(), qty, client.getUserName(), DataUtils.gettimestamp(), type, storageInId);
                if (count == 0) {
                    System.out.println("上架单据" + stockNo + "时修改上架表返回0,storageInId=" + storageInId);
                }
            }
        } else {
            sql.append(" insert into StorageIn(storageID, departmentID, goodsID, colorID, sizeID, barcode, quantity, memo, relationNo, madeby, madeDate, type) ").append(" values(?,?,?,?,?,?,?,?,?,?,?,?) ");
            count = commonDao.executeSql(sql.toString(), storageId, departmentId, goodsId, colorId, sizeId, barcode, qty, memo, stockNo, client.getUserName(), DataUtils.gettimestamp(), type);
            if (count == 0) {
                throw new BusinessException(stockNo + "货品上架失败");
            }
        }
        // 判断进仓单是否操作过
        List data = commonDao.findForJdbc(" select ID,Quantity from AlreadyStockTemp where stockNo = ? and GoodsID = ? and ColorID = ? and SizeID = ? ", stockNo, goodsId, colorId, sizeId);
        if (null != data && data.size() > 0) {
            for (int i = 0; i < data.size(); i++) {
                Map map = (Map) data.get(i);
                int id = Integer.parseInt(String.valueOf(map.get("ID")));
                int quantity = Integer.parseInt(MyTools.formatObjectOfNumber(map.get("Quantity")));
                if (qty == quantity) {
                    count = commonDao.executeSql(" delete from AlreadyStockTemp where id = ? ", id);
                    if (count == 0) {
                        throw new BusinessException(stockNo + "货品上架失败");
                    }
                    int flag = Integer.parseInt(MyTools.formatObjectOfNumber(commonDao.getData(" select count(1) from AlreadyStockTemp where stockNo = ? ", stockNo)));
                    if (flag == 0) {
                        count = commonDao.executeSql(" insert into AlreadyStock(StockNo,UserID) values(?,?)  ", stockNo, client.getUserID());
                        if (count == 0) {
                            throw new BusinessException(stockNo + "货品上架失败");
                        }
                    }
                } else if (qty < quantity) {
                    count = commonDao.executeSql(" update AlreadyStockTemp set Quantity = Quantity - ? where Id = ? ", qty, id);
                    if (count == 0) {
                        throw new BusinessException(stockNo + "货品上架失败");
                    }
                } else {
                    throw new BusinessException(stockNo + "货品上架失败");
                }
                count = commonDao.executeSql(" update AlreadyStockTemp set CompleteSum = CompleteSum + ? where stockNo = ? ", qty, stockNo);
                if (count == 0) {
                    System.out.println(stockNo + "货品上架后未修改上下架进度表\tcount=" + count);
                    count = 1;
                }
            }
        }
        return count;
    }

    @Override
    public synchronized int updateStorageInCount(String departmentId, String storageId, int qty, String type, String goodsId, String colorId, String sizeId, Client client) {
        int count = 0;
        if (qty == 0) {
            // 删除货品
            count = commonDao.executeSql(" delete from storageIn where departmentId = ? and storageId = ? and goodsId = ? and colorId = ? and sizeId = ? and type = ? ", departmentId, storageId, goodsId, colorId, sizeId, type);
        } else {
            // 修改货品数量
            count = commonDao.executeSql(" update storageIn set quantity = ? where departmentId = ? and storageId = ? and goodsId = ? and colorId = ? and sizeId = ? and type = ? ", qty, departmentId, storageId, goodsId, colorId, sizeId, type);
        }
        if (count == 0) {
            throw new BusinessException("修改货品上架数量失败");
        }
        return count;
    }

    @Override
    public synchronized int getStockDetail(String stockNo, Client client) {
        int count = 0;
        // 判断AlreadyStock中是否存在stockId
        int exit = Integer.parseInt(MyTools.formatObjectOfNumber(commonDao.getData(" select count(1) from AlreadyStockTemp where stockNo = ? ", stockNo)));
        int flag = Integer.parseInt(MyTools.formatObjectOfNumber(commonDao.getData(" select count(1) from AlreadyStock where stockNo = ? ", stockNo)));
        int in = Integer.parseInt(MyTools.formatObjectOfNumber(commonDao.getData(" select count(1) from StorageIn where relationNo = ?  ", stockNo)));
        int out = Integer.parseInt(MyTools.formatObjectOfNumber(commonDao.getData(" select count(1) from StorageOut where relationNo = ?  ", stockNo)));
        if (exit == 0 && flag == 0 && in == 0 && out == 0) {
            StringBuilder sql = new StringBuilder();
            sql.append(" insert into AlreadyStockTemp(StockNo, GoodsID, ColorID, SizeID, Quantity,QuantitySum,CompleteSum,UserID) select ? ,goodsId,colorId,sizeId,abs(quantity),(select abs(quantitySum) from stock where No = ?),0,? from stockdetail where StockID = (select stockID from stock where No = ?)  ");
            count = commonDao.executeSql(sql.toString(), stockNo, stockNo, client.getUserID(), stockNo);
            if (count == 0) {
                throw new BusinessException("上下架操作时获取单据" + stockNo + "信息失败");
            }
        } else {
            StringBuilder sql = new StringBuilder();
            // 进(出)仓单中的单据总数
            int quantitySum = Integer.parseInt(MyTools.formatObjectOfNumber(commonDao.getData(" select top 1 isnull(abs(quantitySum),0) from stock where No = ? ", stockNo)));
            // 临时表中已完成的单据的总数
            int completeSum = Integer.parseInt(MyTools.formatObjectOfNumber(commonDao.getData(" select  top 1 isnull(CompleteSum,0) from AlreadyStockTemp where StockNo = ?", stockNo)));
            // 临时表中要完成的单据的总数
            int qtySum = Integer.parseInt(MyTools.formatObjectOfNumber(commonDao.getData(" select  top 1 isnull(QuantitySum,0) from AlreadyStockTemp where StockNo = ?", stockNo)));
            // 临时表中单据剩余未完成的数量
            int qty = Integer.parseInt(MyTools.formatObjectOfNumber(commonDao.getData(" select top 1 sum(isnull(Quantity,0)) from AlreadyStockTemp where StockNo = ? ", stockNo)));
            if ((completeSum == 0 && qty != qtySum) || quantitySum != qtySum) {
                count =
                        commonDao
                                .executeSql(
                                        " delete from AlreadyStockTemp where stockNo = ? ; insert into AlreadyStockTemp(stockNo, GoodsID, ColorID, SizeID, Quantity,QuantitySum,CompleteSum,UserID) select ? ,goodsId,colorId,sizeId,abs(quantity),(select abs(quantitySum) from stock where No = ?),0,? from stockdetail where StockID = (select stockID from stock where No = ?) ",
                                        stockNo, stockNo, stockNo, client.getUserID(), stockNo);
                if (count == 0) {
                    throw new BusinessException("上下架操作时获取单据" + stockNo + "信息失败");
                }
            }
            // 处理已经上过架或者下过架的货品
            if ((in > 0 || out > 0) && completeSum == 0) {
                count =
                        commonDao.executeSql(" update Alreadystocktemp set Quantity  = Quantity - t.Qty from Alreadystocktemp a,(select relationNo as StockNo,goodsId,colorId,sizeId,sum(Quantity) Qty from storageOut where relationNo = ? "
                                + " group by relationNo,goodsId,colorId,sizeId) as t where a.goodsId = t.goodsId and a.colorId= t.colorId and a.sizeId = t.sizeid and a.stockNo = t.stockNo and a.stockNo = ? ", stockNo, stockNo);
                System.out.println("处理已经上过架或者下过架的货品结果:" + count);
            }
            // 其它修改
            sql.append(" update AlreadyStockTemp set UserID = ?,OperateFlag = '1',QuantitySum = (select abs(quantitySum) from stock where No = ?) ");
            if (completeSum == quantitySum && completeSum == qtySum) {
                sql.append(" , Quantity = 0 ");
            }
            sql.append(" where stockNo = ? ");
            count = commonDao.executeSql(sql.toString(), client.getUserID(), stockNo, stockNo);
            if (count == 0) {
                throw new BusinessException("上下架操作时获取单据" + stockNo + "信息失败");
            }
        }
        // 删除进出仓单中不存在的上下架进度单据
        count = commonDao.executeSql(" delete from AlreadyStockTemp where stockNo not in (select No from Stock) ");
        return count;
    }

    @Override
    public synchronized int releasingResources(String stockNo) throws Exception {
        int count = 0;
        count = commonDao.executeSql(" update AlreadyStockTemp set OperateFlag = '0' where stockNo = ? ", stockNo);
        if (count == 0) {
            System.out.println("释放单据" + stockNo + "失败");
        }
        return count;
    }

}
