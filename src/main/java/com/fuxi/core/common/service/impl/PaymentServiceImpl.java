package com.fuxi.core.common.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fuxi.core.common.dao.impl.CommonDao;
import com.fuxi.core.common.exception.BusinessException;
import com.fuxi.core.common.service.PaymentService;
import com.fuxi.system.util.Client;
@Service("paymentService")
@Transactional
public class PaymentServiceImpl implements PaymentService {
	
	 @Autowired
	  private CommonDao commonDao;

	@Override
	public String save(String PaymentID, String SupplierID,
			String DepartmentID, String PaymentTypeID, String Date,
			String BrandID, String EmpID, String BusinessDeptID,
			String PaymentAmount, String LastMustPayAmount, String Memo,
			Client client) throws Exception {
		// TODO Auto-generated method stub
		if ("".equals(PaymentID) || PaymentID==null) { //新增
			 int tag = 26; 
			  // 生成ID
			 PaymentID = commonDao.getNewIDValue(tag);
	            // 生成No
	            String deptType = client.getDeptType();
	            String No = null;
	            if (deptType != null && ("直营店".equals(deptType) || "加盟店".equals(deptType))) {
	                No = commonDao.getNewNOValue(tag, PaymentID, client.getDeptCode());
	            } else {
	                No = commonDao.getNewNOValue(tag, PaymentID);
	            }
	            if (PaymentID == null || No == null) {
	                throw new BusinessException("生成主键/单号失败");
	            }
	            
	            if((BusinessDeptID==null || "".equals(BusinessDeptID)) && (EmpID !=null && !"".equals(EmpID))){
	            	BusinessDeptID =commonDao.getDataForString("select DepartmentID from Employee where EmployeeID = ? ", EmpID);
	         
	            }
	            
	            StringBuilder insertMaster = new StringBuilder();
	            
	            if(LastMustPayAmount==null || "".equals(LastMustPayAmount.trim())){
	            	LastMustPayAmount =null;
	            }
	            
	            
	            insertMaster.append("insert into Payment(PaymentID,No, Date, SupplierID, DepartmentID, EmployeeID, PaymentTypeID,MadeBy,MadeByDate,Memo,Year, Month,PaymentAmount,LastMustPayAmount) select ")
	            .append("'").append(PaymentID).append("','").append(No).append("','").append(Date).append("','").append(SupplierID).append("','").append(DepartmentID).append("','")
	            .append(EmpID).append("','").append(PaymentTypeID).append("','").append(client.getUserName()).append("',").append("getDate()").append(",'").append(Memo).append("','")
	            .append(Date.substring(0, 4)).append("','").append(Date.substring(5, 7)).append("',").append(PaymentAmount).append(",")
	            .append(LastMustPayAmount);
	            
	            commonDao.executeSql(insertMaster.toString());
	            
	            System.out.println("写入语句:"+insertMaster.toString());
	            
		}else { //修改
			
			  StringBuilder sb=new StringBuilder();
			  
			  sb.append("update Payment set SupplierID='"+SupplierID+"',PaymentAmount='"+PaymentAmount+"',")
			  .append("Date='"+Date+"',Year='"+Date.substring(0, 4)+"',Month='"+Date.substring(5, 7)+"',Memo='"+Memo+"'")
			  .append(",EmployeeID ='"+EmpID+"',").append("PaymentTypeID='"+PaymentTypeID+"'")
			  .append(" where PaymentID='"+PaymentID+"'");
			  commonDao.executeSql(sb.toString());
			  System.out.println("更新语句:"+sb.toString());
			  
		}
		
		
		
		return PaymentID;
	}

}
