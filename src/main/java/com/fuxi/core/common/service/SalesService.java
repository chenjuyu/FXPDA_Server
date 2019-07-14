package com.fuxi.core.common.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import com.fuxi.system.util.Client;

/**
 * Title: SalesService Description: 销售发(退)货业务逻辑接口
 *  
 * @author LJ,LYJ
 * 
 */
public interface SalesService {

    // 保存销售发货单
    public String saveSales(String direction, List<Map<String, Object>> dataList, String salesOrderID, String customerid, String departmentid, String employeeId, String businessDeptId, String memo, String type, String typeEName, String brandId, String discountRateSum, String lastARAmount,
            String orderAmountStr, String privilegeAmount, String paymentTypeId, Client client) throws Exception;
    
    // 保存销售发货单 优化版本by cjy
    public String saveSalesX(String direction, List<Map<String, Object>> dataList, String salesOrderID, String customerid, String departmentid, String employeeId, String businessDeptId, String memo, String type, String typeEName, String brandId, String discountRateSum, String lastARAmount,
            String orderAmountStr, String privilegeAmount, String paymentTypeId, Client client) throws Exception;
    
    
    // 保存销售发货单 优化版本by 横向显示尺码的保存，前端会过滤掉数量为0的尺码，
    public String saveSalesX2(String direction, List<Map<String, Object>> dataList, String salesOrderID, String customerid, String departmentid, String employeeId, String businessDeptId, String memo, String type, String typeEName, String brandId, String discountRateSum, String lastARAmount,
            String orderAmountStr, String privilegeAmount, String paymentTypeId, Client client) throws Exception;
    

    // 删除销售发货单明细
    public void deleteSalesdetail(List<Map<String, Object>> dataList, String salesID) throws Exception;

    // 条码校验时覆盖原来的单据
    public int coverSave(String SalesID, List<Map<String, Object>> dataList, Client client) throws Exception;

    // 生成收款单
    public void generalReceival(String deptCode, String customerId, String departmentId, String employeeId, String paymentTypeId, BigDecimal arAmount, BigDecimal receivalAmount, String relationNo, String relationId, String brandId, String type, Client client) throws Exception;

}
