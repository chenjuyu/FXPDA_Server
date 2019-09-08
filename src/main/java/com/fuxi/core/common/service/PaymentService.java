package com.fuxi.core.common.service;

import com.fuxi.system.util.Client;

public interface PaymentService {
	
	public String save(String PaymentID,String SupplierID,String DepartmentID
			,String PaymentTypeID,String Date,String BrandID,
			String EmpID,String BusinessDeptID,String PaymentAmount,String LastMustPayAmount,String Memo,Client client) throws Exception;
	

}
