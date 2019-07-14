package com.fuxi.core.common.service;

import com.fuxi.system.util.Client;

/**
 * Title: CustomerService Description: 装箱单业务逻辑接口
 * 
 * @author LYJ
 * 
 */
public interface PackingBoxService {

    // 保存装箱单
    public String savePackingBox(String goodsId, String colorId, String sizeId, String qty, String packingBoxId, String relationType, String relationId, String relationNo, String customerId, String departmentId, String employeeId, String brandId, String boxNo, String type, String memo,
            String retailSales, Client client) throws Exception;

    // 修改装箱单
    public void updatePacking(String packingBoxId, String boxNo, String goodsId, String colorId, String sizeId, String retailSales, String qtyStr) throws Exception;

    // 删除装箱单
    public boolean deleteAlreadyPackingBoxNo(String packingBoxId);

    // 完成装箱操作
    public boolean completePackingBox(String relationId) throws Exception;
}
