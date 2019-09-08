package com.fuxi.core.common.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fuxi.core.common.dao.impl.CommonDao;
import com.fuxi.core.common.exception.BusinessException;
import com.fuxi.core.common.service.ReceivalService;
import com.fuxi.system.util.Client;
@Service("receivalService")
@Transactional
public class ReceivalServiceImpl implements ReceivalService {

	  @Autowired
	  private CommonDao commonDao;
	
	@Override
	public String save(String ReceivalID, String CustomerID,
			String DepartmentID, String PaymentTypeID, String Type,
			String Date, String ValidBeginDate, String BrandID, String EmpID,
			String BusinessDeptID,String ReceivalAmount,String LastNeedRAmount,String Memo,Client client) throws Exception {
	
		if ("".equals(ReceivalID) || ReceivalID==null) { //新增
			 int tag = 34; 
			  // 生成ID
			     ReceivalID = commonDao.getNewIDValue(tag);
	            // 生成No
	            String deptType = client.getDeptType();
	            String No = null;
	            if (deptType != null && ("直营店".equals(deptType) || "加盟店".equals(deptType))) {
	                No = commonDao.getNewNOValue(tag, ReceivalID, client.getDeptCode());
	            } else {
	                No = commonDao.getNewNOValue(tag, ReceivalID);
	            }
	            if (ReceivalID == null || No == null) {
	                throw new BusinessException("生成主键/单号失败");
	            }
	            
	            if((BusinessDeptID==null || "".equals(BusinessDeptID)) && (EmpID !=null && !"".equals(EmpID))){
	            	BusinessDeptID =commonDao.getDataForString("select DepartmentID from Employee where EmployeeID = ? ", EmpID);
	         
	            }
	            
	            StringBuilder insertMaster = new StringBuilder();
	            
	            if(LastNeedRAmount==null || "".equals(LastNeedRAmount.trim())){
	            	LastNeedRAmount =null;
	            }
	        
	            
	            insertMaster.append("insert into Receival(ReceivalID,Type,No, Date, CustomerID, DepartmentID, EmployeeID, PaymentTypeID,MadeBy,MadeByDate,Memo,Year, Month,ReceivalAmount,LastNeedRAmount,ValidBeginDate) select ")
	            .append("'").append(ReceivalID).append("','").append(Type).append("','").append(No).append("','").append(Date).append("','").append(CustomerID).append("','").append(DepartmentID).append("','")
	            .append(EmpID).append("','").append(PaymentTypeID).append("','").append(client.getUserName()).append("',").append("getDate()").append(",'").append(Memo).append("','")
	            .append(Date.substring(0, 4)).append("','").append(Date.substring(5, 7)).append("',").append(ReceivalAmount).append(",")
	            .append(LastNeedRAmount)
	            .append(",'").append(ValidBeginDate).append("'");
	            commonDao.executeSql(insertMaster.toString());
	            
	            System.out.println("写入语句:"+insertMaster.toString());
	            
		}else { //修改
			
			  StringBuilder sb=new StringBuilder();
			  
			  sb.append("update Receival set CustomerID='"+CustomerID+"',ReceivalAmount='"+ReceivalAmount+"',")
			  .append("Type='"+Type+"',Date='"+Date+"',Year='"+Date.substring(0, 4)+"',Month='"+Date.substring(5, 7)+"',Memo='"+Memo+"',ValidBeginDate='"+ValidBeginDate+"'")
			  .append(",EmployeeID ='"+EmpID+"',").append("PaymentTypeID='"+PaymentTypeID+"'")
			  .append(" where ReceivalID='"+ReceivalID+"'");
			  commonDao.executeSql(sb.toString());
			  System.out.println("更新语句:"+sb.toString());
			  
		}
		
		
		
		return ReceivalID;
	}

}
