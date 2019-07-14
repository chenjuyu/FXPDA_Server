package com.fuxi.core.common.service;

import java.util.List;
import java.util.Map;
import com.fuxi.system.util.Client;

/**
 * Title: InventorySheetService Description: 盘点业务逻辑接口
 * 
 * @author LYJ
 * 
 */
public interface InventorySheetService {

    // 保存盘点单
    public String saveInventorySheet(List<Map<String, Object>> dataList, String stocktakingId, String departmentId, String employeeId, String memo, String brandId, Client client) throws Exception;

    // 修改盘点单数量
    public void deleteInventorySheetDetail(List<Map<String, Object>> dataList, String stocktakingId) throws Exception;

}
