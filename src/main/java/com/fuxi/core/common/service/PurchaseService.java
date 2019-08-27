package com.fuxi.core.common.service;

import java.util.List;
import java.util.Map;

import com.fuxi.system.util.Client;

/**
 * Title: PurchaseService Description: 采购收(退)货业务逻辑接口
 * 
 * @author LYJ
 * 
 */
public interface PurchaseService {

    // 保存销售发货单
    public String savePurchase(String direction, List<Map<String, Object>> dataList, String purchaseID, String supplierid, String departmentid, String employeeId, String businessDeptId, String memo, String type, String typeEName, String brandId, Client client) throws Exception;

    // 删除销售发货单明细
    public void deletePurchasedetail(List<Map<String, Object>> dataList, String PurchaseID);

    // 条码校验修改单据
    public int coverSave(String PurchaseID, List<Map<String, Object>> dataList, Client client) throws Exception;

    public  String savePurchaseX(String directionStr, List<Map<String, Object>> dataList, String PurchaseID, String supplierid, String departmentid, String employeeId, String businessDeptId, String memo, String type, String typeEName, String brandId, Client client) throws Exception;
        
}
