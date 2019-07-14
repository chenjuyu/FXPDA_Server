package com.fuxi.web.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import com.fuxi.core.common.dao.impl.CommonDao;
import com.fuxi.core.common.model.json.AjaxJson;
import com.fuxi.system.util.Client;
import com.fuxi.system.util.DataUtils;
import com.fuxi.system.util.ResourceUtil;
import com.fuxi.system.util.SysLogger;
import com.fuxi.system.util.oConvertUtils;

/**
 * Title: DailyKnotsController Description: 日结逻辑控制器
 * 
 * @author LYJ
 * 
 */
@Controller
@RequestMapping("/dailyknots")
public class DailyKnotsController extends BaseController {

    private Logger log = Logger.getLogger(DailyKnotsController.class);

    @Autowired
    private CommonDao commonDao;

    /**
     * 查询当日小票明细
     * 
     * @param req Request对象
     * @return 当日小票明细
     */
    @RequestMapping(params = "getPossalesDetail")
    @ResponseBody
    public AjaxJson getPossalesDetail(HttpServletRequest req) {
        AjaxJson j = new AjaxJson();
        j.setAttributes(new HashMap<String, Object>());
        try {
            StringBuffer sql = new StringBuffer();
            Map<String, Object> map = new HashMap<String, Object>();
            String date = oConvertUtils.getString(req.getParameter("date"));
            date = DataUtils.dataformat(date, "yyyy-MM-dd");
            String departmentId = oConvertUtils.getString(req.getParameter("departmentId"));
            String factAmountSum = commonDao.getDataForString("select sum(FactAmountSum) FactAmountSum from possales  where [Date]<='" + date + " 23:59:59.997' and DaySumFlag=0 and AuditFlag=1  and DepartmentID='" + departmentId + "'");
            map.put("factAmountSum", factAmountSum);
            sql.append(" select  g.Code GoodsCode,g.Name GoodsName,c.Color,s.Size,b.Quantity,isnull(b.Amount,0) Amount,isnull(b.UnitPrice,0) UnitPrice,isnull(b.Discount,0) Discount, ")
            .append(" isnull(b.DiscountRate,10) DiscountRate from possales a join possalesDetail b on a.possalesid=b.possalesid ").append(" join goods g on b.goodsid=g.goodsid join color c on b.colorid=c.colorid ")
            .append(" join [size] s on b.sizeid=s.sizeid where [Date]<='" + date + " 23:59:59.997' and DaySumFlag=0 and AuditFlag=1 and a.DepartmentID = ? ").append(" order by g.Code,c.Color,s.Size ");
            List list = commonDao.findForJdbc(sql.toString(), departmentId);
            j.setAttributes(map);
            j.setObj(list);
        } catch (Exception e) {
            j.setSuccess(false);
            j.setMsg(e.getMessage());
            SysLogger.error(e.getMessage(), e);
        }
        return j;
    }

    /**
     * 日结操作
     * 
     * @param req
     * @return
     */
    @SuppressWarnings("unchecked")
    @RequestMapping(params = "dailyKnots")
    @ResponseBody
    public AjaxJson dailyKnots(HttpServletRequest req) {
        AjaxJson j = new AjaxJson();
        j.setAttributes(new HashMap<String, Object>());
        Client client = ResourceUtil.getClientFromSession(req);
        try {
            // 是否存在未审核的单据
            boolean unAuditFlag = false;
            String date = oConvertUtils.getString(req.getParameter("date"));
            date = DataUtils.dataformat(date, "yyyy-MM-dd");
            // 查询到截止日期前是否有未审核的小票
            List unAuditData = commonDao.findForJdbc(" select top 2 [no] from possales where AuditFlag=0 and [Date]<='" + date + " 23:59:59.997' and DepartmentID=? ", client.getDeptID());
            if (unAuditData == null || unAuditData.size() == 0) {
                unAuditFlag = true;
                // 查询已审核未日结的日期
                List dates = commonDao.findForJdbc(" select Convert(varchar(10),[Date],121) Date from " + " possales d where DaySumFlag=0 and DepartmentID=? and [Date]<='" + date + " 23:59:59.997' " + " and AuditFlag=1 Group By Convert(varchar(10),[Date],121) order by 1 ", client.getDeptID());
                // 查询部门信息
                Map<String, Object> map = (Map<String, Object>) commonDao.findForJdbc("select SettleCustID,LocalWarehouseID  from department  where departmentId = ? ", client.getDeptID()).get(0);
                String customerId = (String) map.get("SettleCustID");
                String localWarehouseID = (String) map.get("LocalWarehouseID");
                for (int i = 0; i < dates.size(); i++) {
                    Map<String, Object> temp = (Map<String, Object>) dates.get(i);
                    String tdate = (String) temp.get("Date");
                    Map tmap = commonDao.GetDatePeriodExt(tdate);
                    String year = (String) tmap.get("@Y1");
                    String month = (String) tmap.get("@M1");
                    commonDao.dailyKnots(year, month, tdate, client.getDeptID(), customerId, "", localWarehouseID, client.getUserName(), 0);
                }
            }
            j.setObj(unAuditFlag);
        } catch (Exception e) {
            j.setSuccess(false);
            j.setMsg(e.getMessage());
            SysLogger.error(e.getMessage(), e);
        }
        return j;
    }

    /**
     * 查询是否日结
     * 
     * @param req
     * @return
     */
    @RequestMapping(params = "checkDailyKnots")
    @ResponseBody
    public AjaxJson checkDailyKnots(HttpServletRequest req) {
        AjaxJson j = new AjaxJson();
        j.setAttributes(new HashMap<String, Object>());
        try {
            boolean daySumFlag = false;
            String date = oConvertUtils.getString(req.getParameter("date"));
            date = DataUtils.dataformat(date, "yyyy-MM-dd");
            String departmentId = oConvertUtils.getString(req.getParameter("departmentId"));
            StringBuffer sql = new StringBuffer();
            sql.append("select distinct DaySumFlag  from POSSales where Convert(varchar(10),[date],121) between '" + date + "' and '" + date + "' and DepartmentID='" + departmentId + "' ");
            Object obj = commonDao.getData(sql.toString());
            if (Boolean.valueOf(String.valueOf(obj))) {
                daySumFlag = true;
            }
            j.setObj(daySumFlag);
        } catch (Exception e) {
            j.setSuccess(false);
            j.setMsg(e.getMessage());
            SysLogger.error(e.getMessage(), e);
        }
        return j;
    }

}
