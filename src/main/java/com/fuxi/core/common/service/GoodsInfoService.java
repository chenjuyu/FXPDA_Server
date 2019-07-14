package com.fuxi.core.common.service;

import java.util.List;
import java.util.Map;

import com.fuxi.system.util.Client;

/**
 * Title: GoodsInfoService Description: 货品业务逻辑接口
 * 
 * @author LYJ
 * 
 */
public interface GoodsInfoService {

    // 保存货品信息
    public String saveGoodsInfoMsg(String goodsCode, String goodsName, String goodsTypeId, String goodsSubType, String brandId, String brandSerialId, String kind, String age, String season, String supplierId, String supplierCode, String purchasePrice, String tradePrice, String retailSales,
            String retailSales1, String retailSales2, String retailSales3, String salesPrice1, String salesPrice2, String salesPrice3, String colorId1, String colorId2, String colorId3, String colorId4, String colorId5, String colorId6, String colorId7, String colorId8, Client client);

    // 修改货品信息
    public int updateGoodsInfoMsg(String goodsId, String goodsSubType, String brandId, String brandSerialId, String kind, String age, String season, String supplierId, String supplierCode, String purchasePrice, String tradePrice, String retailSales, String retailSales1, String retailSales2,
            String retailSales3, String salesPrice1, String salesPrice2, String salesPrice3, String colorId1, String colorId2, String colorId3, String colorId4, String colorId5, String colorId6, String colorId7, String colorId8, Client client);

    public List<Map<String, Object>> goodslist(String Code,String Type,String DiscountRate,int currpage,int pagesize);
    	
}
