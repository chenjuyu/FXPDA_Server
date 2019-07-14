package com.fuxi.core.common.service;

import java.util.Map;
import com.fuxi.system.util.Client;

/**
 * Title: ShelvesOutService Description: 仓储下架业务逻辑接口(仓位管理)
 * 
 * @author LYJ
 * 
 */
public interface ShelvesOutService {

    // 自动生成StorageOutTemp表
    public Map<String, Object> generateStorageOutTemp(int storageOutType, String departmentId, String stockNo, Client client) throws Exception;

    // 单个货品下架出库
    public int singleGoodsShelvesOut(String departmentId, String type, String tempId, String storageId, String goodsId, String colorId, String sizeId, String qtyStr, String stockNo, String memo, Client client) throws Exception;

    // 单个货品扫码下架出仓
    public int singleGoodsScanningShelvesOut(String departmentId, String type, String storageId, String goodsId, String colorId, String sizeId, String qtyStr, String stockNo, String memo, Client client) throws Exception;

    // 审核出仓单
    public int auditStock(String stockNo, String departmentId, Client client) throws Exception;

    // 释放锁定的库位
    public int releasingResources(String stockNo) throws Exception;

}
