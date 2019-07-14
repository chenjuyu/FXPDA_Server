package com.fuxi.core.common.service;

import com.fuxi.system.util.Client;

/**
 * Title: ShelvesInService Description: 仓储上架业务逻辑接口(仓位管理)
 * 
 * @author LYJ
 * 
 */
public interface ShelvesInService {

    // 保存货品上架信息
    public int saveStorageInMsg(String departmentId, String type, String stockNo, String storageId, String barcode, int qty, String memo, String goodsId, String colorId, String sizeId, Client client) throws Exception;

    // 修改货品上架数量
    public int updateStorageInCount(String departmentId, String storageId, int qty, String type, String goodsId, String colorId, String sizeId, Client client) throws Exception;

    // 获取单据中的货品保存到上下架进度表中
    public int getStockDetail(String stockNo, Client client) throws Exception;

    // 退出时释放锁定的单据
    public int releasingResources(String stockNo) throws Exception;
}
