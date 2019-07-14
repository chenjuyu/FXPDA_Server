package com.fuxi.web.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import com.fuxi.core.common.dao.impl.CommonDao;
import com.fuxi.core.common.exception.BusinessException;
import com.fuxi.core.common.model.json.AjaxJson;
import com.fuxi.core.common.service.CustomerService;
import com.fuxi.core.common.service.SalesService;
import com.fuxi.system.util.Client;
import com.fuxi.system.util.DataUtils;
import com.fuxi.system.util.LoadUserCount;
import com.fuxi.system.util.ResourceUtil;
import com.fuxi.system.util.SysLogger;
import com.fuxi.system.util.UUIDGenerator;
import com.fuxi.system.util.oConvertUtils;

/**
 * Title: CustomerController Description: 客户资料逻辑控制器
 * 
 * @author LYJ
 * 
 */
@Controller
@RequestMapping("/customer")
public class CustomerController extends BaseController {

    private Logger log = Logger.getLogger(CustomerController.class);

    @Autowired
    private CommonDao commonDao;
    @Autowired
    private CustomerService customerService;

    /**
     * 新增客户方法
     * 
     * @param req
     * @return
     */
    @RequestMapping(params = "addCustomer")
    @ResponseBody
    public AjaxJson addCustomer(HttpServletRequest req) {
        Client client = ResourceUtil.getClientFromSession(req);
        AjaxJson j = new AjaxJson();
        j.setAttributes(new HashMap<String, Object>());
        try {
            String code = oConvertUtils.getString(req.getParameter("code"));
            String name = oConvertUtils.getString(req.getParameter("name"));
            String cusTypeId = oConvertUtils.getString(req.getParameter("cusTypeId"));
            String departmentId = oConvertUtils.getString(req.getParameter("departmentId"));
            String salesPriceType = oConvertUtils.getString(req.getParameter("salesPriceType"));
            String mobile = oConvertUtils.getString(req.getParameter("mobile"));
            String memo = oConvertUtils.getString(req.getParameter("memo"));
            String customerId = customerService.saveCustomer(code, name, cusTypeId, departmentId, salesPriceType, mobile, memo, client);
            if (null == customerId || "".equals(customerId) || "null".equalsIgnoreCase(customerId)) {
                throw new BusinessException("新增客户失败");
            }
        } catch (Exception e) {
            j.setSuccess(false);
            j.setMsg(e.getMessage());
            SysLogger.error(e.getMessage(), e);
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
        }
        return j;
    }

}
