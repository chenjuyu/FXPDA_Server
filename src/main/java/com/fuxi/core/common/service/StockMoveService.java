package com.fuxi.core.common.service;

import java.util.List;
import java.util.Map;
import com.fuxi.system.util.Client;

/**
 * Title: StockMoveService Description: 转仓单业务逻辑接口
 * 
 * @author LYJ
 * 
 */
public interface StockMoveService {

    // 保存转仓单
    public String saveStockMove(String warehouseInId, String warehouseOutId, String employeeId, String stockMoveId, String memo, List<Map<String, Object>> dataList, String brandId, Client client) throws Exception;

    // 修改转仓单的数量
    public void deleteStockMovedetail(List<Map<String, Object>> dataList, String stockMoveId) throws Exception;

    // 转仓单条码校验
    public int coverSave(String stockMoveId, List<Map<String, Object>> dataList, Client client) throws Exception;
}
