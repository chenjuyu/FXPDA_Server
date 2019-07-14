package com.fuxi.core.common.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import com.fuxi.system.util.Client;

/**
 * Title: SalesOrderService Description: 销售订单业务逻辑接口
 * 
 * @author LJ,LYJ
 * 
 */
public interface SalesOrderService {

    // 保存销售订单
    public String saveSalesOrder(List<Map<String, Object>> dataList, String salesOrderID, String customerid, String departmentid, String employeeId, String businessDeptId, String memo, String type, String typeEName, String brandId, String discountRateSum, String lastARAmount,
            String preReceivalAmountStr, String privilegeAmount, String paymentTypeId, Client client) throws Exception;

    // 删除销售订单明细

    public void deleteSalesdetail(List<Map<String, Object>> dataList, String salesID);

    // 条码校验修改单据
    public int coverSave(String SalesID, List<Map<String, Object>> dataList, Client client) throws Exception;

    // 生成收款单
    public void generalReceival(String deptCode, String customerId, String departmentId, String employeeId, String paymentTypeId, BigDecimal arAmount, BigDecimal receivalAmount, String relationNo, String relationId, String brandId, String type, Client client) throws Exception;
}
