package com.fuxi.web.controller;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import com.fuxi.core.common.dao.impl.CommonDao;
import com.fuxi.core.common.exception.BusinessException;
import com.fuxi.core.common.model.json.AjaxJson;
import com.fuxi.system.util.Client;
import com.fuxi.system.util.DataUtils;
import com.fuxi.system.util.LoadUserCount;
import com.fuxi.system.util.ResourceUtil;
import com.fuxi.system.util.SysLogger;
import com.fuxi.system.util.UUIDGenerator;
import com.fuxi.system.util.oConvertUtils;

/**
 * Title: LoginController Description: 登陆初始化逻辑控制器[请求时无需验证Client是否过期]
 * 
 * @author LJ,LYJ
 * 
 */
@Controller
@RequestMapping("/login")
public class LoginController extends BaseController {

    private Logger log = Logger.getLogger(LoginController.class);
    public static Map<String, Object> onlineMap = new HashMap<String, Object>();

    @Autowired
    private CommonDao commonDao;

    /**
     * 根据用户ID获取用户的常用信息
     * 
     * @param userID
     * @return
     * @throws BusinessException
     */
    public Map getUserInfo(String userID) throws BusinessException {
        StringBuilder sb = new StringBuilder();
        sb.append(" Select dbo.GetCustPriceTypeOfFieldName(PriceType) PriceType,d.Code DeptCode,d.WarehouseFlag,d.Department,d.DeptType,d.SettleCustID,d.PosNonZeroStockFlag,No, a.* From dbo.[User] a  left ");
        sb.append(" join Department d on a.DepartmentID=d.DepartmentID where d.StopFlag=0 and  ");
        sb.append(" (a.[No]= '").append(userID).append("' or a.UserName='").append(userID).append("')");
        List list = commonDao.findForJdbc(sb.toString());
        if (list.size() <= 0) {
            throw new BusinessException("用户姓名不存在");
        }
        Map map = (Map) list.get(0);
        return map;
    }

    /**
     * 获取前台和后台的在线登录人数
     * 
     * @return
     */
    public int getOnlineCount() {
        int num1 = 0, num2 = 0, sum = 0;
        num1 = commonDao.getDataToSystem("select count(1) from online where regID = ?", LoadUserCount.regId);
        num2 = commonDao.getDataToSystem("select count(1) from posonline where regID = ? ", LoadUserCount.regId);
        sum = num1 + num2;
        return sum;
    }

    /**
     * 获取RelationMovein(用户仓位管理区分是否关联单号)
     * 
     * @return
     */
    public String getRelationMovein() {
        String relationMovein = ResourceUtil.getConfigByName("relationMovein");
        LoadUserCount.relationMovein = Integer.parseInt(relationMovein);;
        return relationMovein;
    }

    /**
     * 获取RelationMovein(用户仓位管理区分是否关联单号)
     * 
     * @return
     */
    public boolean getQueryStockTotal() {
        String queryStock = ResourceUtil.getConfigByName("queryStockTotal");
        boolean flag = Boolean.valueOf(queryStock);
        return flag;
    }

    /**
     * 检查用户是否使用品牌权限
     * 
     * @param userId
     * @return
     */
    public boolean useBrandPower(String userId) {
        boolean flag = false;
        flag = commonDao.getDataForBoolean(" select UseBrandRight from [user] where userId = ? ", userId);
        return flag;
    }

    /**
     * 读取用户的部门权限
     * 
     * @param userId
     * @return
     */
    private Map<String, String> getUserRight(String userId) {
        Map<String, String> map = new HashMap<String, String>();
        StringBuffer sb = new StringBuffer();
        sb.append(" select DepartmentID from departmentRight dr  where  userid='").append(userId).append("' and rightFlag = 1 ");
        map.put(userId, sb.toString());
        return map;
    }

    /**
     * 获取用户的操作权限
     * 
     * @param userId
     * @param menuId
     * @return
     */
    private List<Map<String, Object>> getUserMenuRight(String userId, String menuId) {
        List list = commonDao.findForJdbc(" select BrowseRight,AddRight,ModifyRight,AuditRight from MenuRight where UserID = ? and MenuID = ? ", userId, menuId);
        return list;
    }

    /**
     * 获取赠品单模块的显示权限
     * 
     * @param userId
     * @return
     */
    private boolean getGiftMenuFlag(String userId) {
        boolean flag = commonDao.getDataForBoolean(" SELECT CASE  WHEN BrowseRight = 0 AND AddRight = 0 " 
        + " AND ModifyRight = 0 AND DeleteRight = 0 AND PrintRight = 0 " + "AND AuditRight = 0 AND InUseRight = 0 " 
        + " AND ExportRight = 0 AND UnAuditRight = 0 AND RatifyRight = 0 "
        + " THEN 'false' ELSE 'true' END AS GiftMenuFlag FROM MenuRight WHERE UserID = ? AND MenuID = '922' ", userId);
        return flag;
    }

    /**
     * 获取用户的字段权限
     * 
     * @param userId
     * @return
     */
    private boolean getUserTableRight(String userId, String tableId, String fieldId) {
        boolean flag = commonDao.getDataForBoolean(" select RightFlag from TableRight where UserID = ? and TableID = ? and FieldID = ? ", userId, tableId, fieldId);
        return flag;
    }

    /**
     * 用户登录方法,检查用户名密码是否正确 正确:加载用户的基本信息 错误:返回错误登录信息
     * 
     * @param req
     * @return
     */
    @RequestMapping(params = "login")
    @ResponseBody
    public synchronized AjaxJson login(HttpServletRequest req) {
        AjaxJson j = new AjaxJson();
        j.setAttributes(new HashMap<String, Object>());
        try {
            if (!LoadUserCount.flag) {
                throw new BusinessException("系统参数错误,请检查服务器参数设置");
            }
            String userName = oConvertUtils.getString(req.getParameter("username"));
            String psw = oConvertUtils.getString(req.getParameter("password"));
            String hostName = oConvertUtils.getString(req.getParameter("hostName"));
            Map map = getUserInfo(userName);
            if (!psw.equals(map.get("password"))) {
                throw new BusinessException("密码错误");
            }
            // 保存登录对象的ID
            String userId = (String) map.get("UserID");
            // 限制用户唯一登录
            checkUser(userId);
            // 删除上一次的登录信息
            commonDao.executeDataToSystem("delete from online where TerminalName='PDA' and UserID =? ; ", userId);
            int count = getOnlineCount();
            if (LoadUserCount.userCount == 0 || count >= LoadUserCount.userCount) {
                throw new BusinessException("登录设备达到上限");
            }
            // 插入登录记录
            StringBuffer sql = new StringBuffer();
            sql.append("insert into online values(?, ?, ?, ?, ?, ?, ?, ?) ; ");
            String onLineId = UUIDGenerator.generate();
            userName = (String) map.get("UserName");
            commonDao.executeDataToSystem(sql.toString(), onLineId, "PDA", userName, LoadUserCount.regId, hostName, userId, DataUtils.gettimestamp(), LoadUserCount.corpName);
            Client c = new Client();
            c.setOnLineId(onLineId);
            c.setDeptType((String) map.get("DeptType"));
            c.setDeptID((String) map.get("DepartmentID"));
            c.setUserID((String) map.get("UserID"));
            c.setUserName((String) map.get("UserName"));
            c.setPriceType((String) map.get("PriceType"));
            c.setDeptCode(map.get("DeptCode").toString());
            c.setDeptName(map.get("Department").toString());
            c.setSettleCustID((String) map.get("SettleCustID"));
            c.setNo((String) map.get("No"));
            c.setUnitPricePermitFlag((Boolean) map.get("UnitPricePermitFlag"));
            c.setPOSNonZeroStockFlag((Boolean) map.get("POSNonZeroStockFlag"));
            c.setSuperSalesFlag((Boolean) map.get("SuperSalesFlag"));
            String maxSizeSql = "select max(no) as maxsize from SizeGroupSize";
            Map sizeMap = (Map) commonDao.findForJdbc(maxSizeSql).get(0);
            int maxSize = (Integer) sizeMap.get("maxsize");
            c.setMaxSize(maxSize);
            // 获取用户对应的权限
            c.setMap(getUserRight(userId));
            ResourceUtil.addClientToSession(req, c);
            String imagePath = new String(ResourceUtil.getConfigByName("imgPath").getBytes("iso-8859-1"), "UTF-8");
            String usingOrderGoodsModule = new String(ResourceUtil.getConfigByName("usingOrderGoodsModule").getBytes("iso-8859-1"), "UTF-8");
            map.put("onLineId", onLineId);
            map.put("deptId", c.getDeptID());
            map.put("deptName", c.getDeptName());
            map.put("userName", c.getUserName());
            map.put("hasStorage", judgeHasStorage(c.getDeptID()));
            map.put("relationMovein", getRelationMovein());
            map.put("queryStockTotal", getQueryStockTotal());
            map.put("printTemplate", getPrintTemplate());
            // 判断用户是否允许修改单价
            map.put("unitPricePermitFlag", c.getUnitPricePermitFlag());
            // 判断用户是否允许修改折扣(后台)
            map.put("discountRatePermitFlag2", (Boolean) map.get("DiscountRatePermitFlag2"));
            // 判断用户是否允许修改折扣(前台)
            map.put("discountRatePermitFlag", (Boolean) map.get("DiscountRatePermitFlag"));
            // 前台最小货品折扣范围
            map.put("discountRate", map.get("DiscountRate"));
            // 登录部门是否仓库
            map.put("warehouseFlag", (Boolean) map.get("WarehouseFlag"));

            // 读取小票打印参数
            String possalesTile = new String(ResourceUtil.getConfigByName("possalesTile").getBytes("iso-8859-1"), "UTF-8");
            String possalesParam1 = new String(ResourceUtil.getConfigByName("possalesParam1").getBytes("iso-8859-1"), "UTF-8");
            String possalesParam2 = new String(ResourceUtil.getConfigByName("possalesParam2").getBytes("iso-8859-1"), "UTF-8");
            String possalesParam3 = new String(ResourceUtil.getConfigByName("possalesParam3").getBytes("iso-8859-1"), "UTF-8");
            String possalesParam4 = new String(ResourceUtil.getConfigByName("possalesParam4").getBytes("iso-8859-1"), "UTF-8");
            String possalesParam5 = new String(ResourceUtil.getConfigByName("possalesParam5").getBytes("iso-8859-1"), "UTF-8");
            map.put("possalesTile", possalesTile);
            map.put("possalesParam1", possalesParam1);
            map.put("possalesParam2", possalesParam2);
            map.put("possalesParam3", possalesParam3);
            map.put("possalesParam4", possalesParam4);
            map.put("possalesParam5", possalesParam5);
            // 判断是否显示赠品单
            map.put("showGiftMenuFlag", getGiftMenuFlag(userId));
            // 判断登录的用户是否使用品牌权限
            map.put("useBrandPower", useBrandPower(userId));
            // 用户货品资料的操作权限
            map.put("goodsUserMenuRight", getUserMenuRight(userId, "001"));
            // 用户客户资料的操作权限
            map.put("customerUserMenuRight", getUserMenuRight(userId, "003"));
            // 销售订单操作权限
            map.put("salesOrderMenuRight", getUserMenuRight(userId, "201"));
            // 销售发货单操作权限
            map.put("salesMenuRight", getUserMenuRight(userId, "202"));
            // 销售退货单操作权限
            map.put("salesReturnMenuRight", getUserMenuRight(userId, "203"));
            // 采购收货单操作权限
            map.put("purchaseMenuRight", getUserMenuRight(userId, "102"));
            // 采购退货单操作权限
            map.put("purchaseReturnMenuRight", getUserMenuRight(userId, "103"));
            // 转仓单操作权限
            map.put("stockMoveMenuRight", getUserMenuRight(userId, "303"));
            // 盘点单操作权限
            map.put("stocktakingMenuRight", getUserMenuRight(userId, "304"));
            // 进仓单操作权限
            map.put("stockInMenuRight", getUserMenuRight(userId, "301"));
            // 出仓单操作权限
            map.put("stockOutMenuRight", getUserMenuRight(userId, "302"));
            // 库存查询操作权限
            map.put("stocktakingQueryMenuRight", getUserMenuRight(userId, "307"));
            // 销售小票操作权限
            map.put("posSalesMenuRight", getUserMenuRight(userId, "502"));
            // 装箱单浏览权限
            map.put("packingBoxMenuRight", getUserMenuRight(userId, "1000"));
            // 赠品单操作权限
            map.put("giftMenuRight", getUserMenuRight(userId, "922"));
            // 销售单日结操作权限
            map.put("dailyKnotsMenuRight", getUserMenuRight(userId, "933"));
            
            //收款单操作权限 
            map.put("receivalMenuRight", getUserMenuRight(userId, "205"));
            
            //付款单操作权限 
            map.put("paymentMenuRight", getUserMenuRight(userId, "104"));
            //采购订单操作权限
            map.put("purchaseOrderMenuRight", getUserMenuRight(userId, "101"));
            
            // 用户字段权限(参考进价)
            map.put("purchasePriceRight", getUserTableRight(userId, "04", "39"));
            // 用户字段权限(厂商)
            map.put("supplierRight", getUserTableRight(userId, "04", "19"));
            // 用户字段权限(厂商货品编码)
            map.put("supplierCodeRight", getUserTableRight(userId, "04", "20"));
            // 首采日期
            map.put("purchasedDateRight", getUserTableRight(userId, "04", "89"));
            // 末采日期
            map.put("lastPurchasedDateRight", getUserTableRight(userId, "04", "90"));
            // 厂商电话
            map.put("supplierPhoneRight", getUserTableRight(userId, "12", "16"));
            // 用户字段权限(货品子类别)
            map.put("subTypeRight", getUserTableRight(userId, "04", "116"));
            // 用户字段权限(品牌)
            map.put("brandRight", getUserTableRight(userId, "04", "11"));
            // 用户字段权限(系列)
            map.put("brandSerialRight", getUserTableRight(userId, "04", "16"));
            // 用户字段权限(性质)
            map.put("kindRight", getUserTableRight(userId, "04", "23"));
            // 用户字段权限(年份)
            map.put("ageRight", getUserTableRight(userId, "04", "14"));
            // 用户字段权限(季节)
            map.put("seasonRight", getUserTableRight(userId, "04", "15"));
            // 用户字段权限(零售价)
            map.put("retailSalesRight", getUserTableRight(userId, "04", "43"));
            // 用户字段权限(零售价2)
            map.put("retailSales1Right", getUserTableRight(userId, "04", "44"));
            // 用户字段权限(零售价3)
            map.put("retailSales2Right", getUserTableRight(userId, "04", "45"));
            // 用户字段权限(零售价4)
            map.put("retailSales3Right", getUserTableRight(userId, "04", "46"));
            // 用户字段权限(批发价)
            map.put("tradePriceRight", getUserTableRight(userId, "04", "52"));
            // 用户字段权限(批发价2)
            map.put("salesPrice1Right", getUserTableRight(userId, "04", "53"));
            // 用户字段权限(批发价3)
            map.put("salesPrice2Right", getUserTableRight(userId, "04", "54"));
            // 用户字段权限(批发价4)
            map.put("salesPrice3Right", getUserTableRight(userId, "04", "55"));
            // 采购收货单金额合计
            map.put("purchaseAmountSumRight", getUserTableRight(userId, "22", "16"));
            // 采购收货单子表单价
            map.put("purchaseUnitPriceRight", getUserTableRight(userId, "23", "12"));
            // 采购退货单金额合计
            map.put("purchaseReturnAmountSumRight", getUserTableRight(userId, "207", "16"));
            // 采购收货单子表单价
            map.put("purchaseReturnUnitPriceRight", getUserTableRight(userId, "208", "12"));
            // 销售发货单金额合计
            map.put("salesAmountSumRight", getUserTableRight(userId, "30", "15"));
            // 销售发货单子表单价
            map.put("salesUnitPriceRight", getUserTableRight(userId, "31", "12"));
            // 销售退货单金额合计
            map.put("salesReturnAmountSumRight", getUserTableRight(userId, "209", "16"));
            // 销售退货单子表单价
            map.put("salesReturnUnitPriceRight", getUserTableRight(userId, "210", "12"));
            // 销售订单金额合计
            map.put("salesOrderAmountSumRight", getUserTableRight(userId, "28", "14"));
            // 销售订单子表单价
            map.put("salesOrderUnitPriceRight", getUserTableRight(userId, "29", "21"));
            // 转仓单金额合计
            map.put("stockMoveAmountSumRight", getUserTableRight(userId, "36", "27"));
            // 转仓单子表单价
            map.put("stockMoveUnitPriceRight", getUserTableRight(userId, "37", "16"));
            // 盘点单金额合计
            map.put("stocktakingAmountSumRight", getUserTableRight(userId, "38", "21"));
            // 进仓单金额合计
            map.put("stockInAmountSumRight", getUserTableRight(userId, "24", "34"));
            // 进仓单子表单价
            map.put("stockInUnitPriceRight", getUserTableRight(userId, "25", "36"));
            // 出仓单金额合计
            map.put("stockOutAmountSumRight", getUserTableRight(userId, "32", "34"));
            // 出仓单子表单价
            map.put("stockOutUnitPriceRight", getUserTableRight(userId, "33", "36"));
            map.put("userId", userId);
            // 服务器图片存储路径
            map.put("imagePath", imagePath);
            // 是否启用订货功能
            map.put("usingOrderGoodsModule", usingOrderGoodsModule);
            j.setObj(map);
            // 保存用户的session
            onlineMap.put(userId, req.getSession());
        } catch (Exception e) {
            j.setSuccess(false);
            j.setMsg(e.getMessage());
            SysLogger.error(e.getMessage(), e);
        }
        return j;
    }

    /**
     * 用户注销/退出
     * 
     * @param req
     * @return
     */
    @RequestMapping(params = "logout")
    @ResponseBody
    public AjaxJson logout(HttpServletRequest req) {
        AjaxJson j = new AjaxJson();
        j.setAttributes(new HashMap<String, Object>());
        try {
            String onLineId = oConvertUtils.getString(req.getParameter("onLineId"));
            commonDao.executeDataToSystem("delete from online where onLineId = ?", onLineId);
        } catch (Exception e) {
            j.setSuccess(false);
            j.setMsg(e.getMessage());
            SysLogger.error(e.getMessage(), e);
        }
        return j;
    }

    /**
     * 验证系统管理员身份
     * 
     * @param req
     * @return
     */
    @RequestMapping(params = "verificationAdmin")
    @ResponseBody
    public AjaxJson verificationAdmin(HttpServletRequest req) {
        AjaxJson j = new AjaxJson();
        j.setAttributes(new HashMap<String, Object>());
        try {
            boolean flag = false;
            String userNo = oConvertUtils.getString(req.getParameter("userNo"));
            String userPassword = oConvertUtils.getString(req.getParameter("userPassword"));
            List list = commonDao.findForJdbc(" select No,Password from [user] where userId = '1' ");
            for (int i = 0; i < list.size(); i++) {
                Map<String, Object> map = (Map<String, Object>) list.get(i);
                String no = (String) map.get("No");
                String password = (String) map.get("Password");
                if (userNo.equals(no) && userPassword.equals(password)) {
                    flag = true;
                }
            }
            j.setObj(flag);
        } catch (Exception e) {
            j.setSuccess(false);
            j.setMsg(e.getMessage());
            SysLogger.error(e.getMessage(), e);
        }
        return j;
    }

    /**
     * 检查用户是否已经登录,限制用户唯一登录
     * 
     * @param userId
     */
    private void checkUser(String userId) {
        for (int i = 0; i < onlineMap.size(); i++) {
            if (onlineMap.containsKey(userId)) {
                // 注销session
                HttpSession session = (HttpSession) onlineMap.get(userId);
                session.removeAttribute("CLIENT");
                onlineMap.remove(userId);
            }
        }
    }

    /**
     * 获取默认打印模板
     * 
     * @return
     */
    private String getPrintTemplate() {
        String description = "print";
        try {
            description = new String(ResourceUtil.getConfigByName("printTemplate").getBytes("iso-8859-1"), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return description;
    }

    /**
     * 检查判断用户的部门是否含有仓位
     * 
     * @param departmentId
     * @return
     */
    private boolean judgeHasStorage(String departmentId) {
        boolean flag = false;
        int exit = Integer.parseInt(String.valueOf(commonDao.getData(" select count(1) from Department where DepartmentId = ? and  WarehouseFlag = '1' and DepartmentID in ( select distinct departmentID from storage ) ", departmentId)));
        if (exit > 0) {
            flag = true;
        }
        return flag;
    }

    /**
     * 客户端检测新版本
     * 
     * @param req
     * @return
     */
    @RequestMapping(params = "checkVersion")
    @ResponseBody
    public AjaxJson checkVersion(HttpServletRequest req) {
        AjaxJson j = new AjaxJson();
        j.setAttributes(new HashMap<String, Object>());
        try {
            Map map = new HashMap();
            String version = ResourceUtil.getConfigByName("appVersion");
            String path = ResourceUtil.getConfigByName("appUrl");
            String description = new String(ResourceUtil.getConfigByName("description").getBytes("iso-8859-1"), "UTF-8");
            String forceUpdate = ResourceUtil.getConfigByName("forceUpdate");
            map.put("Version", version);
            map.put("Url", path);
            map.put("Description", description);
            map.put("ForceUpdate", Boolean.parseBoolean(forceUpdate));
            // 传递登录参数(用于登录)
            map.put("corpName", LoadUserCount.corpName);
            map.put("regId", LoadUserCount.regId);
            j.setObj(map);
        } catch (Exception e) {
            j.setSuccess(false);
            j.setMsg(e.getMessage());
            SysLogger.error(e.getMessage(), e);
        }
        return j;
    }

}
