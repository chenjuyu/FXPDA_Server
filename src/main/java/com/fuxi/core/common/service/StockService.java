package com.fuxi.core.common.service;

import java.util.List;
import java.util.Map;
import com.fuxi.system.util.Client;

/**
 * Title: StockService Description: 进(出)仓单业务逻辑接口
 * 
 * @author LYJ
 * 
 */
public interface StockService {

    // 保存进(出)仓单
    public String saveStockMoveIn(String employeeId, String stockMoveId, String memo, List<Map<String, Object>> dataList, String brandId, Client client) throws Exception;

    // 修改进(出)仓单的数量
    public void deleteStockMoveInDetail(List<Map<String, Object>> dataList, String stockMoveId) throws Exception;

    // 进(出)仓单条码校验
    public int coverSave(String stockMoveId, List<Map<String, Object>> dataList, Client client) throws Exception;
}
