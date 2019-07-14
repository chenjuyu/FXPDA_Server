package com.fuxi.core.common.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.fuxi.core.common.dao.impl.CommonDao;
import com.fuxi.core.common.exception.BusinessException;
import com.fuxi.core.common.service.CustomerService;
import com.fuxi.system.util.Client;
import com.fuxi.system.util.DataUtils;

/**
 * Title: CustomerServiceImpl Description: 客户业务逻辑接口实现类
 * 
 * @author LYJ
 * 
 */
@Service("customerService")
@Transactional
public class CustomerServiceImpl implements CustomerService {

    @Autowired
    private CommonDao commonDao;

    @Override
    public String saveCustomer(String code, String name, String cusTypeId, String departmentId, String salesPriceType, String mobile, String memo, Client client) throws Exception {
        String customerId = commonDao.getNewIDValue(13);
        if (null == customerId || "".equals(customerId.trim())) {
            throw new BusinessException("生成客户ID失败");
        }
        String helpCode = String.valueOf(commonDao.getData("select [dbo].[F_GetPY](?)", name));
        StringBuffer sql = new StringBuffer();
        sql.append("insert into customer(customerId,customerTypeId,code,helpCode,customer,departmentId,").append("priceType,mobilePhone,modifyDate,editor,memo) values(?,?,?,?,?,?,?,?,?,?,?)");
        commonDao.executeSql(sql.toString(), customerId, cusTypeId, code, helpCode, name, departmentId, salesPriceType, mobile, DataUtils.gettimestamp(), client.getUserName(), memo);
        return customerId;
    }

}
