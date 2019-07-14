package com.fuxi.core.common.service;

import com.fuxi.system.util.Client;

/**
 * Title: CustomerService Description: 客户业务逻辑接口
 * 
 * @author LYJ
 * 
 */
public interface CustomerService {

    // 新增客户
    public String saveCustomer(String code, String name, String cusTypeId, String departmentId, String salesPriceType, String mobile, String memo, Client client) throws Exception;

}
