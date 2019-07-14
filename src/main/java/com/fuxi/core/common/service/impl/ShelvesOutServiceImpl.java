package com.fuxi.core.common.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.fuxi.core.common.dao.impl.CommonDao;
import com.fuxi.core.common.exception.BusinessException;
import com.fuxi.core.common.service.ShelvesOutService;
import com.fuxi.system.util.Client;
import com.fuxi.system.util.DataUtils;
import com.fuxi.system.util.LoadUserCount;
import com.fuxi.system.util.MyTools;
import com.fuxi.web.controller.LoginController;

/**
 * Title: ShelvesOutServiceImpl Description: 仓储下架业务逻辑接口(仓位管理)
 * 
 * @author LYJ
 * 
 */
@Service("shelvesOutService")
@Transactional
public class ShelvesOutServiceImpl implements ShelvesOutService {

    @Autowired
    private CommonDao commonDao;

    @Override
    public synchronized int singleGoodsShelvesOut(String departmentId, String type, String tempId, String storageId, String goodsId, String colorId, String sizeId, String qtyStr, String stockNo, String memo, Client client) {
        int count = 0, num = 0;
        boolean exit = false;
        String storageOutId = null;
        StringBuilder sql = new StringBuilder();
        if (qtyStr == null || qtyStr.isEmpty()) {
            qtyStr = "1";
        }
        int qty = Integer.parseInt(qtyStr);
        new LoginController().getRelationMovein();
        if (LoadUserCount.relationMovein == 1) {
            exit = true;
        } else {
            exit = checkStorageStocking(departmentId, storageId, goodsId, colorId, sizeId, qty, client);
        }
        if (exit) {
            // 检查货架上是否已经存在该货品
            List list = checkTheSameRecord(departmentId, storageId, goodsId, colorId, sizeId, stockNo, type, client);
            if (list.size() > 0) {
                Map map = (Map) list.get(0);
                num = Integer.parseInt(String.valueOf(map.get("Quantity")));
                storageOutId = String.valueOf(map.get("StorageOutID"));
            }
            if (num > 0) {
                // 判断单据中上架记录的货品总数是否与单据中的货品数量相等
                int quantity = Integer.parseInt(MyTools.formatObjectOfNumber(commonDao.getData(" select Quantity from StockDetail where goodsId = '" + goodsId + "' and colorId = '" + colorId + "' and sizeId = '" + sizeId + "' and stockId = (select stockId from Stock where No = ?) ", stockNo)));
                int complete = Integer.parseInt(MyTools.formatObjectOfNumber(commonDao.getData(" select sum(Quantity) from StorageOut where goodsID = '" + goodsId + "' and colorID = '" + colorId + "' and sizeID = '" + sizeId + "' and relationNo = '" + stockNo + "' and type = '" + type + "' ")));
                if (complete < quantity && (complete + qty) <= quantity) {
                    sql.append(" update StorageOut set quantity = quantity + ?, madeby = ?, madeDate = ?, type = ? where storageOutID = ? ");
                    count = commonDao.executeSql(sql.toString(), qty, client.getUserName(), DataUtils.gettimestamp(), type, storageOutId);
                    if (count == 0) {
                        System.out.println("下架单据" + stockNo + "时修改上架表返回0,storageOutId=" + storageOutId);
                    }
                }
            } else {
                sql.append(" insert into storageOut(storageID, departmentID, goodsID, colorID, sizeID, quantity, memo, relationNo, madeby, madeDate, type) ").append(" values (?,?,?,?,?,?,?,?,?,?,?) ");
                count = commonDao.executeSql(sql.toString(), storageId, departmentId, goodsId, colorId, sizeId, qty, memo, stockNo, client.getUserName(), DataUtils.gettimestamp(), type);
                if (count == 0) {
                    throw new BusinessException("仓位货品数量不足,货品" + goodsId + "下架失败");
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
                        count = commonDao.executeSql(" delete from StorageOutTemp where tempId = ?; ", tempId);
                        if (count == 0) {
                            System.out.println("货品" + goodsId + "下架时删除StorageOutTemp中的记录返回0,tempId=" + tempId);
                        }
                        count = commonDao.executeSql(" update AlreadyStockTemp set Quantity = 0 where id = ? ", id);
                        if (count == 0) {
                            throw new BusinessException("货品" + goodsId + "下架时修改AlreadyStockTemp的quantity返回0,id=" + id);
                        }
                    } else {
                        count = commonDao.executeSql(" update AlreadyStockTemp set Quantity = (Quantity - ?) where id = ? ", qty, id);
                        if (count == 0) {
                            throw new BusinessException("货品" + goodsId + "下架时修改AlreadyStockTemp的Quantity返回0,id=" + id);
                        }
                    }
                    count = commonDao.executeSql(" update AlreadyStockTemp set CompleteSum = (CompleteSum + ?) where stockNo = ? ", qty, stockNo);
                    if (count == 0) {
                        throw new BusinessException("货品" + goodsId + "下架时修改AlreadyStockTemp的CompleteSum返回0,stockNo=" + stockNo);
                    }
                }
            }
        }
        return count;
    }

    @Override
    public synchronized Map<String, Object> generateStorageOutTemp(int storageOutType, String departmentId, String stockNo, Client client) {
        Map<String, Object> m = new HashMap<String, Object>();
        boolean flag = true;// 是否完全生成出仓单所对应推荐库架
        boolean isEmpty = false;// 库位商品是否全为空
        // 删除同一出仓单对应的库位推荐信息
        releasingResources(stockNo);
        // 生成出仓单对应的临时表
        // 删除StorageOutTTemp表并生成新表
        int exit = Integer.parseInt(MyTools.formatObjectOfNumber(commonDao.getData(" select count(1) from dbo.[sysobjects] where name = 'StorageOutTTemp_" + client.getUserID() + "' ")));
        if (exit > 0) {
            commonDao.executeSql(" drop table StorageOutTTemp_" + client.getUserID() + " ");
        }
        commonDao.executeSql(" select (select Storage from Storage where storageId = a.storageId) Storage, storageID,goodsID,colorID,sizeID,departmentId,sum(quantity) Quantity into StorageOutTTemp_" + client.getUserID() + " from "
                + " (select storageID,goodsID,colorID,sizeID,departmentId,sum(quantity) as quantity from storageIn with(nolock) " + " group by storageID,goodsID,colorID,sizeID,departmentId union all select storageID,goodsID,colorID,sizeID,departmentId,-sum(quantity) as quantity "
                + " from storageOut with(nolock) group by storageID,goodsID,colorID,sizeID,departmentId union all " + " select storageID,goodsID,colorID,sizeID,departmentId,-sum(quantity) as quantity "
                + " from StorageOutTemp with(nolock) where storageID <> '-1' group by storageID,goodsID,colorID,sizeID,departmentId) a  " + " group by storageID,goodsID,colorID,sizeID,departmentId having  isnull(sum(quantity),0) <> 0 order by storage ");
        List list = commonDao.findForJdbc(" select GoodsID,ColorID,SizeID,sum(isnull(quantity,0)) Quantity from AlreadyStockTemp where stockNo = ? and Quantity > 0 group by GoodsID,ColorID,SizeID ", stockNo);
        int i = 0, empty = 0;
        for (; i < list.size(); i++) {
            Map<String, String> temp = null;
            Map map = (Map) list.get(i);
            String goodsId = String.valueOf(map.get("GoodsID"));
            String colorId = String.valueOf(map.get("ColorID"));
            String sizeId = String.valueOf(map.get("SizeID"));
            int qty = Integer.parseInt(String.valueOf(map.get("Quantity")));
            if (storageOutType == 0) {
                // 查询货品对应的最小数量
                temp = getGoodsMinQty(goodsId, colorId, sizeId, departmentId, client);
            } else if (storageOutType == 1) {
                // 查询货品对应的最大数量
                temp = getGoodsMaxQty(goodsId, colorId, sizeId, departmentId, client);
            }
            if (null != temp) {// 查询库架上对应货品的最小数量
                int num = Integer.parseInt(temp.get("Quantity"));
                String storageId = temp.get("StorageID");
                if (num > 0) {
                    if (num < qty) {// 库架上存在比出仓单上对应货品数量少的货品
                        int sum = num;
                        // 添加推荐
                        addDataToStorageOutTemp(storageId, goodsId, colorId, sizeId, departmentId, num, stockNo, client.getUserID());
                        // 删除临时表中的对应的记录
                        deleteDataFromTTemp(storageId, goodsId, colorId, sizeId, num, client);
                        // 获取临时表的最大数量-->防止特殊情况下死循环
                        // int maxCount =
                        // Integer.parseInt(String.valueOf(commonDao.getData(" select count(1) from StorageOutTTemp_"+client.getUserID()+" ")));
                        int a = 0;
                        while (sum < qty) {
                            if (storageOutType == 0) {
                                temp = getGoodsMinQty(goodsId, colorId, sizeId, departmentId, client);
                            } else {
                                temp = getGoodsMaxQty(goodsId, colorId, sizeId, departmentId, client);
                            }
                            a++;
                            if (null != temp) {// 查询库架上对应货品的最小数量
                                num = Integer.parseInt(temp.get("Quantity"));
                                if (sum + num > qty) {
                                    num = qty - sum;
                                }
                                storageId = temp.get("StorageID");
                                // 添加推荐
                                addDataToStorageOutTemp(storageId, goodsId, colorId, sizeId, departmentId, num, stockNo, client.getUserID());
                                // 删除临时表中的对应的记录
                                deleteDataFromTTemp(storageId, goodsId, colorId, sizeId, num, client);
                                sum += num;
                            } else {
                                addDataToStorageOutTemp("-1", goodsId, colorId, sizeId, departmentId, qty - sum, stockNo, client.getUserID());
                                flag = false;
                                break;
                            }
                        }
                    } else if (num == qty) {
                        // 添加推荐
                        addDataToStorageOutTemp(storageId, goodsId, colorId, sizeId, departmentId, num, stockNo, client.getUserID());
                        // 删除临时表中的对应的记录
                        deleteDataFromTTemp(storageId, goodsId, colorId, sizeId, num, client);
                    } else if (num > qty) {// 推荐离货品库架最近的库架
                        temp = getGoodsLatelyQty(goodsId, colorId, sizeId, departmentId, client);
                        if (null != temp) {
                            num = Integer.parseInt(temp.get("Quantity"));
                            storageId = temp.get("StorageID");
                            // 添加推荐
                            addDataToStorageOutTemp(storageId, goodsId, colorId, sizeId, departmentId, qty, stockNo, client.getUserID());
                            // 修改临时表中的对应的记录
                            updateDataFromTTemp(storageId, goodsId, colorId, sizeId, qty, client);
                        } else {
                            addDataToStorageOutTemp("-1", goodsId, colorId, sizeId, departmentId, qty, stockNo, client.getUserID());
                        }
                    }
                } else {
                    addDataToStorageOutTemp("-1", goodsId, colorId, sizeId, departmentId, qty, stockNo, client.getUserID());
                }
            } else {
                addDataToStorageOutTemp("-1", goodsId, colorId, sizeId, departmentId, qty, stockNo, client.getUserID());
                flag = false;
                // 检查库架上对应出仓单中的货品是否全部为空
                empty++;
            }
        }
        if (empty == list.size()) {
            isEmpty = true;
        }
        // 删除临时表
        commonDao.executeSql(" drop table StorageOutTTemp_" + client.getUserID() + "; ");
        m.put("flag", flag);
        m.put("isEmpty", isEmpty);
        return m;
    }

    // 删除临时表里面已经添加的数据
    private int deleteDataFromTTemp(String storageID, String goodsID, String colorID, String sizeID, int num, Client client) {
        // 删除临时表里面已经添加的数据
        int count = commonDao.executeSql(" delete from StorageOutTTemp_" + client.getUserID() + " where storageID = ? and goodsID = ? and colorID = ? and sizeID = ? and Quantity = ?", storageID, goodsID, colorID, sizeID, num);
        return count;
    }

    // 删除临时表里面已经添加的数据
    private int updateDataFromTTemp(String storageID, String goodsID, String colorID, String sizeID, int num, Client client) {
        // 删除临时表里面已经添加的数据
        int count = commonDao.executeSql(" update StorageOutTTemp_" + client.getUserID() + " set Quantity = Quantity - ? where storageID = ? and goodsID = ? and colorID = ? and sizeID = ? ", num, storageID, goodsID, colorID, sizeID);
        if (count == 0) {
            throw new BusinessException("获取仓位推荐下架失败");
        }
        return count;
    }

    // 往库位推荐表中插入记录
    private int addDataToStorageOutTemp(String storageID, String goodsID, String colorID, String sizeID, String departmentId, int num, String stockNo, String userId) {
        int count = commonDao.executeSql(" insert into StorageOutTemp(storageID,goodsID,colorID,sizeID,departmentId,quantity,relationNo,userId) values(?,?,?,?,?,?,?,?) ", storageID, goodsID, colorID, sizeID, departmentId, num, stockNo, userId);
        if (count == 0) {
            throw new BusinessException("获取仓位推荐下架失败");
        }
        return count;
    }

    // 查询库位中对应的最小数量的货品
    private Map<String, String> getGoodsMinQty(String goodsId, String colorId, String sizeId, String departmentId, Client client) {
        List tempList =
                commonDao.findForJdbc(" select top 1 StorageID,GoodsID,ColorID,SizeID, sum(Quantity) Quantity from StorageOutTTemp_" + client.getUserID()
                        + " where goodsId = ? and colorId = ? and sizeId = ? and departmentId = ? and quantity > 0 group by Storage,StorageID,GoodsID,ColorID,SizeID,departmentId order by Storage asc,Quantity asc ", goodsId, colorId, sizeId, departmentId);
        if (tempList.size() > 0) {
            Map<String, String> map = new HashMap<String, String>();
            Map temp = (Map) tempList.get(0);
            map.put("Quantity", String.valueOf(temp.get("Quantity")));
            map.put("StorageID", String.valueOf(temp.get("StorageID")));
            return map;
        }
        return null;
    }

    // 查询库位中对应的最小数量的货品
    private Map<String, String> getGoodsMaxQty(String goodsId, String colorId, String sizeId, String departmentId, Client client) {
        List tempList =
                commonDao.findForJdbc(" select top 1 StorageID,GoodsID,ColorID,SizeID, sum(Quantity) Quantity from StorageOutTTemp_" + client.getUserID()
                        + " where goodsId = ? and colorId = ? and sizeId = ? and departmentId = ? and quantity > 0 group by Storage,StorageID,GoodsID,ColorID,SizeID,departmentId order by Storage asc,Quantity desc ", goodsId, colorId, sizeId, departmentId);
        if (tempList.size() > 0) {
            Map<String, String> map = new HashMap<String, String>();
            Map temp = (Map) tempList.get(0);
            map.put("Quantity", String.valueOf(temp.get("Quantity")));
            map.put("StorageID", String.valueOf(temp.get("StorageID")));
            return map;
        }
        return null;
    }

    // 查询库位中对应的最近的货品
    private Map<String, String> getGoodsLatelyQty(String goodsId, String colorId, String sizeId, String departmentId, Client client) {
        List tempList =
                commonDao.findForJdbc(" select top 1 StorageID,GoodsID,ColorID,SizeID,sum(Quantity) Quantity from StorageOutTTemp_" + client.getUserID()
                        + " where goodsId = ? and colorId = ? and sizeId = ? and departmentId = ?  and quantity > 0  group by Storage,StorageID,GoodsID,ColorID,SizeID,departmentId order by Storage asc ", goodsId, colorId, sizeId, departmentId);
        if (tempList.size() > 0) {
            Map<String, String> map = new HashMap<String, String>();
            Map temp = (Map) tempList.get(0);
            map.put("Quantity", String.valueOf(temp.get("Quantity")));
            map.put("StorageID", String.valueOf(temp.get("StorageID")));
            return map;
        }
        return null;
    }

    // 检查是否存在相同的下架记录
    private List checkTheSameRecord(String departmentId, String storageId, String goodsId, String colorId, String sizeId, String stockNo, String type, Client client) {
        StringBuilder sb = new StringBuilder();
        sb.append(" select StorageOutID, count(1) Quantity from StorageOut where storageID = '").append(storageId).append("' and departmentID = '").append(departmentId).append("' and goodsID = '").append(goodsId).append("' and colorID = '").append(colorId).append("' and sizeID = '").append(sizeId)
                .append("' and type = '").append(type).append("' ");
        if (stockNo != null && !stockNo.isEmpty() && !"".equals(stockNo)) {
            sb.append(" and relationNo = '").append(stockNo).append("' ");
        }
        sb.append(" group by StorageOutID ");
        return commonDao.findForJdbc(sb.toString());
    }

    @Override
    public synchronized int auditStock(String stockNo, String departmentId, Client client) {
        int count = 0;
        // 判断是否已经完成下架工作
        int qtySum = Integer.parseInt(MyTools.formatObjectOfNumber(commonDao.getData(" select  top 1 isnull(QuantitySum,0) from AlreadyStockTemp where stockNo = ?", stockNo)));
        int completeSum = Integer.parseInt(MyTools.formatObjectOfNumber(commonDao.getData(" select  top 1 isnull(CompleteSum,0) from AlreadyStockTemp where stockNo = ?", stockNo)));
        if (qtySum > completeSum) {// 存在未下架的货品
            // 剩余的货品均无库存,生成下架差异单
            List<Map<String, Object>> datas = commonDao.findForJdbc(" select GoodsID, ColorID, SizeID, Quantity from AlreadyStockTemp where StockNo = ? and Quantity <> 0 ", stockNo);
            for (int i = 0; i < datas.size(); i++) {
                Map<String, Object> map = datas.get(i);
                String goodsId = String.valueOf(map.get("GoodsID"));
                String colorId = String.valueOf(map.get("ColorID"));
                String sizeId = String.valueOf(map.get("SizeID"));
                int quantity = Integer.parseInt(MyTools.formatObjectOfNumber(map.get("Quantity")));
                String storageId = String.valueOf(commonDao.getData(" select top 1 StorageID from storageouttemp where goodsId = '" + goodsId + "' and colorId = '" + colorId + "' and sizeId = '" + sizeId + "' and relationNo = '" + stockNo + "' "));
                count = commonDao.executeSql(" insert into StorageOutDifferent(StorageID,DepartmentID,GoodsID,ColorID,SizeID,Quantity,RelationNo) values (?,?,?,?,?,?,?) ", storageId, departmentId, goodsId, colorId, sizeId, quantity, stockNo);
                if (count == 0) {
                    throw new BusinessException("生成下架差异单失败");
                }
            }
            if (count > 0) {
                count = commonDao.executeSql(" insert into AlreadyStock(StockNo,UserID) values(?,?) ", stockNo, client.getUserID());
                if (count == 0) {
                    throw new BusinessException("完成下架失败");
                }
                count = commonDao.executeSql(" delete from AlreadyStockTemp where stockNo = ? ", stockNo);
                if (count == 0) {
                    throw new BusinessException("完成下架失败");
                }
                count = commonDao.executeSql(" delete from storageouttemp where relationNo = ? ", stockNo);
                if (count == 0) {
                    System.out.println("完成下架时删除storageouttemp中的记录返回0,relationNo=" + stockNo);
                }
            } else {
                throw new BusinessException("完成下架失败");
            }
        } else if (qtySum == completeSum) {
            int flag = Integer.parseInt(MyTools.formatObjectOfNumber(commonDao.getData(" select count(1) from AlreadyStock where stockNo = ?", stockNo)));
            if (flag < 1) {
                count = commonDao.executeSql(" insert into AlreadyStock(StockNo,UserID) values(?,?) ", stockNo, client.getUserID());
                if (count == 0) {
                    throw new BusinessException("完成下架失败");
                }
                count = commonDao.executeSql(" delete from AlreadyStockTemp where stockNo = ? ", stockNo);
                if (count == 0) {
                    throw new BusinessException("完成下架失败");
                }
            }
        } else if (qtySum < completeSum) {
            List<Map<String, Object>> datas = commonDao.findForJdbc(" select GoodsID, ColorID, SizeID, Quantity from AlreadyStockTemp where StockNo = ? and Quantity <> 0 ", stockNo);
            for (int i = 0; i < datas.size(); i++) {
                Map<String, Object> map = datas.get(i);
                String goodsId = String.valueOf(map.get("GoodsID"));
                String colorId = String.valueOf(map.get("ColorID"));
                String sizeId = String.valueOf(map.get("SizeID"));
                int quantity = Integer.parseInt(String.valueOf(map.get("Quantity")));
                if (quantity < 0) {
                    count = commonDao.executeSql(" update storageOut set Quantity = (Quantity + ?) where relationNo = ? and goodsId = ? and colorId = ? and sizeId = ? and departmentId = ? and Quantity > 0", quantity, stockNo, goodsId, colorId, sizeId, departmentId);
                    if (count == 0) {
                        throw new BusinessException("完成下架失败");
                    }
                }
            }
            int storageOutSum = Integer.parseInt(MyTools.formatObjectOfNumber(commonDao.getData("select sum(Quantity) from storageOut where relationNo = ? ", stockNo)));
            if (storageOutSum == qtySum) {
                int flag = Integer.parseInt(MyTools.formatObjectOfNumber(commonDao.getData(" select count(1) from AlreadyStock where stockNo = ?", stockNo)));
                if (flag < 1) {
                    count = commonDao.executeSql(" insert into AlreadyStock(StockNo,UserID) values(?,?) ", stockNo, client.getUserID());
                    if (count == 0) {
                        throw new BusinessException("完成下架失败");
                    }
                    count = commonDao.executeSql(" delete from AlreadyStockTemp where stockNo = ? ", stockNo);
                    if (count == 0) {
                        throw new BusinessException("完成下架失败");
                    }
                }
            }
        }
        return count > 0 ? 1 : 0;
    }

    @Override
    public synchronized int releasingResources(String stockNo) {
        return commonDao.executeSql(" delete from StorageOutTemp where relationNo = ? ", stockNo);
    }

    @Override
    public synchronized int singleGoodsScanningShelvesOut(String departmentId, String type, String storageId, String goodsId, String colorId, String sizeId, String qtyStr, String stockNo, String memo, Client client) {
        int count = 0, num = 0;
        String storageOutId = null;
        StringBuilder sql = new StringBuilder();
        if (qtyStr == null || qtyStr.isEmpty()) {
            qtyStr = "1";
        }
        int qty = Integer.parseInt(qtyStr);
        // 检查仓位货品库存
        boolean flag = checkStorageStocking(departmentId, storageId, goodsId, colorId, sizeId, qty, client);
        if (flag) {
            List list = checkTheSameRecord(departmentId, storageId, goodsId, colorId, sizeId, stockNo, type, client);
            if (list.size() > 0) {
                Map map = (Map) list.get(0);
                num = Integer.parseInt(String.valueOf(map.get("Quantity")));
                storageOutId = String.valueOf(map.get("StorageOutID"));
            }
            if (num > 0) {
                // 判断单据中上架记录的货品总数是否与单据中的货品数量相等
                if (stockNo != null && !stockNo.isEmpty() && !"".equals(stockNo)) {
                    int quantity = Integer.parseInt(MyTools.formatObjectOfNumber(commonDao.getData(" select Quantity from StockDetail where goodsId = '" + goodsId + "' and colorId = '" + colorId + "' and sizeId = '" + sizeId + "' and stockId = (select stockId from Stock where No = ?) ", stockNo)));
                    int complete = Integer.parseInt(MyTools.formatObjectOfNumber(commonDao.getData(" select sum(Quantity) from StorageOut where goodsID = '" + goodsId + "' and colorID = '" + colorId + "' and sizeID = '" + sizeId + "' and relationNo = '" + stockNo + "' and type = '" + type + "' ")));
                    if (complete < quantity && (complete + qty) <= quantity) {
                        sql.append(" update StorageOut set quantity = quantity + ?, madeby = ?, madeDate = ?, type = ? where storageOutID = ? ");
                        count = commonDao.executeSql(sql.toString(), qty, client.getUserName(), DataUtils.gettimestamp(), type, storageOutId);
                        if (count == 0) {
                            throw new BusinessException("单个货品下架失败");
                        }
                    }
                } else {
                    sql.append(" update StorageOut set quantity = quantity + ?, madeby = ?, madeDate = ?, type = ? where storageOutID = ? ");
                    count = commonDao.executeSql(sql.toString(), qty, client.getUserName(), DataUtils.gettimestamp(), type, storageOutId);
                    if (count == 0) {
                        throw new BusinessException("单个货品下架失败");
                    }
                }
            } else {
                sql.append(" insert into storageOut(storageID, departmentID, goodsID, colorID, sizeID, quantity, memo, relationNo, madeby, madeDate, type) ").append(" values (?,?,?,?,?,?,?,?,?,?,?) ");
                count = commonDao.executeSql(sql.toString(), storageId, departmentId, goodsId, colorId, sizeId, qty, memo, stockNo, client.getUserName(), DataUtils.gettimestamp(), type);
                if (count == 0) {
                    throw new BusinessException("单个货品下架失败");
                }
            }
        }
        return count;
    }

    // 检查货架库存
    private boolean checkStorageStocking(String departmentId, String storageId, String goodsId, String colorId, String sizeId, int quantity, Client client) {
        boolean flag = false;
        int exit = Integer.parseInt(MyTools.formatObjectOfNumber(commonDao.getData(" select count(1) from dbo.[sysobjects] where name = 'StorageOutTTemp_" + client.getUserID() + "' ")));
        if (exit > 0) {
            commonDao.executeSql(" drop table StorageOutTTemp_" + client.getUserID() + " ");
        }
        commonDao.executeSql(" select  storageID,goodsID,colorID,sizeID,departmentId,sum(quantity) Quantity into StorageOutTTemp_" + client.getUserID() + " from " + " (select storageID,goodsID,colorID,sizeID,departmentId,sum(quantity) as quantity from storageIn with(nolock) "
                + " group by storageID,goodsID,colorID,sizeID,departmentId union all select storageID,goodsID,colorID,sizeID,departmentId,-sum(quantity) as quantity " + " from storageOut with(nolock) group by storageID,goodsID,colorID,sizeID,departmentId union all "
                + " select storageID,goodsID,colorID,sizeID,departmentId,-sum(quantity) as quantity " + " from StorageOutTemp with(nolock) group by storageID,goodsID,colorID,sizeID,departmentId) a  " + " group by storageID,goodsID,colorID,sizeID,departmentId having  isnull(sum(quantity),0) <> 0 ");
        StringBuffer sb = new StringBuffer();
        sb.append(" select isnull(sum(Quantity),0) from StorageOutTTemp_" + client.getUserID() + " sott where departmentId = '" + departmentId + "' ");
        if (storageId != null && !storageId.isEmpty() && !"".equals(storageId)) {
            sb.append(" and storageId = '" + storageId + "' ");
        }
        sb.append(" and goodsId = '" + goodsId + "' and colorId = '" + colorId + "' and sizeId = '" + sizeId + "' ");
        int total = Integer.parseInt(MyTools.formatObjectOfNumber(commonDao.getData(sb.toString())));
        if (total >= quantity) {
            flag = true;
        }
        // 删除临时表
        commonDao.executeSql(" drop table StorageOutTTemp_" + client.getUserID() + "; ");
        return flag;
    }

}
