package com.fuxi.core.common.service;

import java.util.List;
import java.util.Map;

import com.fuxi.system.util.Client;

public interface PurchaseOrderService {
	   public  String savePurchaseOrderX(String directionStr, List<Map<String, Object>> dataList, String PurchaseOrderID, String supplierid, String departmentid, String employeeId, String businessDeptId, String memo, String type, String typeEName, String brandId, Client client) throws Exception;
	     
}
