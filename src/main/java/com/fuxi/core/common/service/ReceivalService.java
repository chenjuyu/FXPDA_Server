package com.fuxi.core.common.service;

import com.fuxi.system.util.Client;

public interface ReceivalService {
	
	public String save(String ReceivalID,String CustomerID,String DepartmentID
			,String PaymentTypeID,String Type,String Date,String ValidBeginDate,String BrandID,
			String EmpID,String BusinessDeptID,String ReceivalAmount,String Memo,Client client) throws Exception;
	
	
	

}
