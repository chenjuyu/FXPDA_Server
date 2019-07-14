package com.fuxi.core.common.service;

import java.util.List;
import java.util.Map;
import com.fuxi.system.util.Client;

/**
 * Title: SalesTicketService Description: 销售小票业务逻辑接口
 * 
 * @author LYJ
 * 
 */
public interface SalesTicketService {

    // 保存销售小票
    public Map<String, String> saveSalesTicket(List<Map<String, Object>> dataList, String employeeId, String qty, String amount, String retailAmount, String discountMoney, String exchangedPoint, String memo, String vipPointRate, String vipDiscount, String vipId, String vipCode,
            boolean posBackAudit, String type, Client client) throws Exception;

}
