package com.fuxi.core.common.interceptors;

import java.util.List;
import java.util.Map;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import com.fuxi.core.common.dao.impl.CommonDao;
import com.fuxi.core.common.exception.BusinessException;
import com.fuxi.system.util.BarcodeUtil;
import com.fuxi.system.util.Client;
import com.fuxi.system.util.Coder;
import com.fuxi.system.util.LoadUserCount;
import com.fuxi.system.util.ResourceUtil;
import com.fuxi.web.controller.LoginController;


/**
 * Title: OnlineListener Description: 监听在线用户上线下线
 * 
 * @author LJ,LYJ
 * 
 */
public class OnlineListener implements ServletContextListener, HttpSessionListener {

    private static ApplicationContext ctx = null;


    private CommonDao commonDao;



    public CommonDao getCommonDao() {
        return commonDao;
    }


    public void setCommonDao(CommonDao commonDao) {
        this.commonDao = commonDao;
    }

    public void sessionCreated(HttpSessionEvent httpSessionEvent) {

    }

    /**
     * session注册时的操作
     */
    public void sessionDestroyed(HttpSessionEvent httpSessionEvent) {
        StringBuilder sql = new StringBuilder();
        // 删除失效的用户信息
        sql.append("delete from online where TerminalName='PDA' and UserID =?");
        // 仓位(改变正在操作的单据状态)
        int exit = Integer.parseInt(String.valueOf(commonDao.getOneData(" select count(1) from dbo.[sysobjects] where name = 'StorageOutTemp' ")));
        if (exit > 0) {
            commonDao.executeSql(" delete from StorageOutTemp ; ");
        }
        // 获取要注销的用户ID
        Client client = (Client) httpSessionEvent.getSession().getAttribute("CLIENT");
        if (client != null) {
            String userId = client.getUserID();
            // 移除对应的session
            LoginController.onlineMap.remove(userId);
            commonDao.executeDataToSystem(sql.toString(), userId);
        }
    }

    /**
     * 服务器初始化
     */
    public void contextInitialized(ServletContextEvent evt) {
        ctx = WebApplicationContextUtils.getWebApplicationContext(evt.getServletContext());
        ServletContext servletContext = evt.getServletContext();
        WebApplicationContext webApplicationContext = WebApplicationContextUtils.getWebApplicationContext(servletContext);
        AutowireCapableBeanFactory autowireCapableBeanFactory = webApplicationContext.getAutowireCapableBeanFactory();
        autowireCapableBeanFactory.configureBean(this, "commonDao");
        // 重启时候清空在线
        StringBuilder sb = new StringBuilder();
        sb.append("delete from online where TerminalName='PDA'");
        commonDao.executeDataToSystem(sb.toString());
        // 获取货品使用的颜色类型
        int colorOption = commonDao.getOneData(" select ColorOption from  Parameter ");
        LoadUserCount.colorOption = colorOption;
        // 登录参数
        String dbName = String.valueOf(commonDao.getObjectData(" select DB_NAME() "));
        String corpName = String.valueOf(commonDao.getDataToSystemForObj(" select corpName from corp where corpCode = (?) ", dbName));
        LoadUserCount.corpName = corpName.trim();
        try {
            // 获取允许用户在线总数
            getUserCount();
            // 获取账套尺码,颜色的编码数(检查尺码,颜色编码的位数是否相等)
            checkSizeAndColorCode();
            // 获取打印公共设置
            getPrintCommonSettings();
            // 获取负库存检查时用的库存类型
            getCheckStockOptType();
        } catch (Exception e) {
            LoadUserCount.flag = false;
            e.printStackTrace();
        }
    }

    public static ApplicationContext getCtx() {
        return ctx;
    }

    public void contextDestroyed(ServletContextEvent paramServletContextEvent) {

    }

    /**
     * 获取允许用户在线总数
     * 
     * @throws Exception
     */
    public void getUserCount() throws Exception {
        boolean flag = false;
        String sysParam = ResourceUtil.getConfigByName("sysParam");
        List datas = commonDao.findForJdbcSystem("select userId, convert(varchar(100),convert(binary(8000),userName)) userName, userCount,posCount from register");
        for (int i = 0; i < datas.size(); i++) {
            Map m = (Map) datas.get(i);
            String userId = String.valueOf(m.get("userId"));
            String userName = String.valueOf(m.get("userName"));
            String userCount = String.valueOf(m.get("userCount"));
            String posCount = String.valueOf(m.get("posCount"));
            if (sysParam.equals(Coder.getEncryption(userId))) {
                int countSum = Integer.parseInt(userCount.trim()) + Integer.parseInt(posCount.trim());
                LoadUserCount.regId = userId.trim();
                LoadUserCount.regName = userName.trim();
                LoadUserCount.userCount = countSum;
                flag = true;
                break;
            }
        }
        if (!flag) {
            throw new BusinessException("系统参数错误");
        }
    }

    /**
     * 获取账套尺码,颜色的编码数(检查尺码,颜色编码的位数是否相等)
     * 
     * @throws Exception
     */
    private void checkSizeAndColorCode() throws Exception {
        int colorMaxLenth = Integer.parseInt(String.valueOf(commonDao.getOneData("select max(len(No)) from color")));
        int colorMinLenth = Integer.parseInt(String.valueOf(commonDao.getOneData("select min(len(NO)) from color")));
        int sizeMaxLenth = Integer.parseInt(String.valueOf(commonDao.getOneData("select max(len(No)) from size")));
        int sizeMinLenth = Integer.parseInt(String.valueOf(commonDao.getOneData("select min(len(NO)) from size")));
        // 保存数据到服务器缓存
        LoadUserCount.maxSizeLength = sizeMaxLenth;
        LoadUserCount.minSizeLength = sizeMinLenth;
        LoadUserCount.maxColorLength = colorMaxLenth;
        LoadUserCount.minColorLength = colorMinLenth;
        if (colorMaxLenth - colorMinLenth == 0) {
            BarcodeUtil.colorLength = colorMaxLenth;
        }
        if (sizeMaxLenth - sizeMinLenth == 0) {
            BarcodeUtil.sizeLength = sizeMaxLenth;
        }
    }

    /**
     * 获取打印公共设置
     * 
     * @throws Exception
     */
    private void getPrintCommonSettings() throws Exception {
        String address = new String(ResourceUtil.getConfigByName("address").getBytes("iso-8859-1"), "UTF-8");
        String phone = new String(ResourceUtil.getConfigByName("phone").getBytes("iso-8859-1"), "UTF-8");
        String mobile = new String(ResourceUtil.getConfigByName("mobile").getBytes("iso-8859-1"), "UTF-8");
        String bankTypeOne = new String(ResourceUtil.getConfigByName("bankTypeOne").getBytes("iso-8859-1"), "UTF-8");
        String bankOneNo = new String(ResourceUtil.getConfigByName("bankOneNo").getBytes("iso-8859-1"), "UTF-8");
        String bankCardOneName = new String(ResourceUtil.getConfigByName("bankCardOneName").getBytes("iso-8859-1"), "UTF-8");
        String bankTypeTwo = new String(ResourceUtil.getConfigByName("bankTypeTwo").getBytes("iso-8859-1"), "UTF-8");
        String bankTwoNo = new String(ResourceUtil.getConfigByName("bankTwoNo").getBytes("iso-8859-1"), "UTF-8");
        String bankCardTwoName = new String(ResourceUtil.getConfigByName("bankCardTwoName").getBytes("iso-8859-1"), "UTF-8");
        LoadUserCount.address = address;
        LoadUserCount.phone = phone;
        LoadUserCount.mobile = mobile;
        LoadUserCount.bankTypeOne = bankTypeOne;
        LoadUserCount.bankOneNo = bankOneNo;
        LoadUserCount.bankCardOneName = bankCardOneName;
        LoadUserCount.bankTypeTwo = bankTypeTwo;
        LoadUserCount.bankTwoNo = bankTwoNo;
        LoadUserCount.bankCardTwoName = bankCardTwoName;
    }

    /**
     * 获取负库存检查时设置的库存类别
     */
    private void getCheckStockOptType() {
        int checkStockOpt = commonDao.getOneData(" select CheckStockOpt from Parameter ");
        LoadUserCount.checkStockOpt = checkStockOpt;
    }

}
