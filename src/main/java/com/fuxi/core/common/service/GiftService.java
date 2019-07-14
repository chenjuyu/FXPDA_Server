package com.fuxi.core.common.service;

import java.util.List;
import java.util.Map;
import com.fuxi.system.util.Client;

/**
 * Title: SalesTicketService Description: 赠品单业务逻辑接口
 * 
 * @author LYJ
 * 
 */
public interface GiftService {

    // 保存赠品单
    public Map<String, String> saveGift(List<Map<String, Object>> dataList, String employeeId, String qty, String amount, String memo, String vipId, String vipCode, Client client) throws Exception;

}
