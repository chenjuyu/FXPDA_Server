package com.fuxi.web.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import net.sf.json.JSONArray;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import com.fuxi.core.common.dao.impl.CommonDao;
import com.fuxi.core.common.model.json.AjaxJson;
import com.fuxi.core.common.service.SalesTicketService;
import com.fuxi.system.util.Client;
import com.fuxi.system.util.ResourceUtil;
import com.fuxi.system.util.SysLogger;
import com.fuxi.system.util.oConvertUtils;

/**
 * Title: SalesTicketController Description: 销售小票逻辑控制器
 * 
 * @author LYJ
 * 
 */
@Controller
@RequestMapping("/salesTicket")
public class SalesTicketController extends BaseController {

    private Logger log = Logger.getLogger(SalesTicketController.class);
    private CommonController commonController = new CommonController();

    @Autowired
    private CommonDao commonDao;

    @Autowired
    private SalesTicketService salesTicketService;

    /**
     * 保存销售小票记录[新增]
     * 
     * @param req
     * @return
     */
    @SuppressWarnings("unchecked")
    @RequestMapping(params = "saveSalesTicket")
    @ResponseBody
    public AjaxJson saveSalesTicket(HttpServletRequest req) {
        AjaxJson j = new AjaxJson();
        j.setAttributes(new HashMap<String, Object>());
        Client client = ResourceUtil.getClientFromSession(req);
        try {
            String employeeId = oConvertUtils.getString(req.getParameter("employeeId"));
            String memo = oConvertUtils.getString(req.getParameter("memo"));
            String type = oConvertUtils.getString(req.getParameter("type"));
            String qty = oConvertUtils.getString(req.getParameter("qty"));
            String vipPointRate = oConvertUtils.getString(req.getParameter("vipPointRate"));
            String vipDiscount = oConvertUtils.getString(req.getParameter("vipDiscount"));
            String amount = oConvertUtils.getString(req.getParameter("amount"));
            String retailAmount = oConvertUtils.getString(req.getParameter("retailAmount"));
            String discountMoney = oConvertUtils.getString(req.getParameter("discountMoney"));
            String exchangedPoint = oConvertUtils.getString(req.getParameter("exchangedPoint"));
            boolean posBackAudit = Boolean.valueOf(oConvertUtils.getString(req.getParameter("posBackAudit")));
            String vipId = oConvertUtils.getString(req.getParameter("vipId"));
            String vipCode = oConvertUtils.getString(req.getParameter("vipCode"));
            String jsonStr = oConvertUtils.getString(req.getParameter("data"));
            System.out.println("接收的数据格式:"+jsonStr);
            JSONArray datas = JSONArray.fromObject(jsonStr);
            List<Map<String, Object>> dataList = JSONArray.toList(datas, Map.class);
            List<Map<String, Object>> checkList = JSONArray.toList(datas, Map.class);
            // 判断检查负库存
            List<Map<String, Object>> tempList = new ArrayList<Map<String, Object>>();
            if (client.getPOSNonZeroStockFlag() && !client.isSuperSalesFlag()) {
                checkList = mergeData(checkList);
                tempList = commonController.checkNegativeInventoryForFrontDesk(commonDao, checkList, client.getDeptID());
            }
            if (tempList.size() == 0) {
                Map<String, String> map = salesTicketService.saveSalesTicket(dataList, employeeId, qty, amount, retailAmount, discountMoney, exchangedPoint, memo, vipPointRate, vipDiscount, vipId, vipCode, posBackAudit, type, client);
                j.getAttributes().put("PosSalesID", map.get("PosSalesID"));
                j.getAttributes().put("PosSalesNo", map.get("PosSalesNo"));
                j.getAttributes().put("AvailableIntegral", commonDao.getData("select UsablePoint from vip where vipid = ?", vipId));
            } else {
                j.getAttributes().put("PosSalesID", "");
                j.getAttributes().put("PosSalesNo", "");
            }
            j.getAttributes().put("tempList", tempList);
        } catch (Exception e) {
            j.setSuccess(false);
            j.setMsg(e.getMessage());
            SysLogger.error(e.getMessage(), e);
        }
        return j;
    }

    /**
     * 去除List中为负数的集合
     * 
     * @param lists
     * @return
     */
    private List<Map<String, Object>> mergeData(List<Map<String, Object>> lists) {
        for (int i = 0; i < lists.size(); i++) {
            Map<String, Object> map = lists.get(i);
            int quantity = Integer.parseInt(String.valueOf(map.get("Quantity")));
            if (quantity < 0) {
                lists.remove(i);
                i--;
            }
        }
        return lists;
    }

    /**
     * 检查货品是否为促销货品
     * 
     * @param req
     * @return
     */
    @SuppressWarnings("unchecked")
    @RequestMapping(params = "checkSPGoods")
    @ResponseBody
    public AjaxJson checkSPGoods(HttpServletRequest req) {
        AjaxJson j = new AjaxJson();
        Map<String, Object> map = new HashMap<String, Object>();
        j.setAttributes(new HashMap<String, Object>());
        Client client = ResourceUtil.getClientFromSession(req);
        try {
            String goodsId = oConvertUtils.getString(req.getParameter("goodsId"));

            // 根据部门价格检查货品的价格
            List iapList = commonDao.findForJdbc(" select * from DepartmentPrice where Convert(Varchar(10),BeginDate,121) <= getDate() and Convert(Varchar(10),EndDate,121) >= getDate() and DepartmentId = ? and GoodsID = ? order by BeginDate desc ", client.getDeptID(), goodsId);
            if (iapList.size() > 0) {
                Map<String, Object> tmap = (Map<String, Object>) iapList.get(0);
                map.put("UnitPrice", tmap.get("SalesPrice"));
                map.put("DiscountRate", tmap.get("DiscountRate"));
                map.put("SpecialPriceFlag", tmap.get("SpecialPriceFlag"));
            }

            // 调价单检查货品的价格
            StringBuffer spSql = new StringBuffer();
            spSql.append(" select * from InvoiceAdjustPrice a join InvoiceAdjustPriceDetail b on a.InvoiceAdjustPriceID = b.InvoiceAdjustPriceID ");
            spSql.append(" where Convert(Varchar(10),BeginDate,121) <= getDate() and Convert(Varchar(10),EndDate,121) >= getDate() ");
            spSql.append(" and DepartmentId = ? and GoodsID = ? and AuditFlag = 1  order by BeginDate asc,EndDate desc ");
            List dpList = commonDao.findForJdbc(spSql.toString(), client.getDeptID(), goodsId);
            if (dpList.size() > 0) {
                Map<String, Object> tmap = (Map<String, Object>) dpList.get(0);
                map.put("UnitPrice", tmap.get("UnitPrice1"));
                map.put("DiscountRate", tmap.get("DiscountRate1"));
                map.put("SpecialPriceFlag", tmap.get("SpecialPriceFlag"));
            }

            // 根据促销活动检查货品的价格
            String jsonStr = oConvertUtils.getString(req.getParameter("spIDs"));
            JSONArray datas = JSONArray.fromObject(jsonStr);
            List<String> spIDs = JSONArray.toList(datas, String.class);
            for (int i = 0; i < spIDs.size(); i++) {
                String spId = spIDs.get(i);
                List<Map<String, Object>> spGoodsList = commonDao.findForJdbc(" select * from SPGoods where SPID = ? ", spId);
                for (int k = 0; k < spGoodsList.size(); k++) {
                    Map<String, Object> tmap = spGoodsList.get(k);
                    String beginGoodsId = (String) tmap.get("GoodsID");
                    String endGoodsId = (String) tmap.get("EndGoodsID");
                    if ((beginGoodsId == null || "".equals(beginGoodsId) || "null".equalsIgnoreCase(beginGoodsId)) && (endGoodsId == null || "".equals(endGoodsId) || "null".equalsIgnoreCase(endGoodsId))) {
                        continue;
                    }
                    if (beginGoodsId != null && !"".equals(beginGoodsId) && !"null".equalsIgnoreCase(beginGoodsId) || (endGoodsId != null && !"".equals(endGoodsId) && !"null".equalsIgnoreCase(endGoodsId))) {
                        endGoodsId = beginGoodsId;
                    }
                    int exit = commonDao.getDataToInt(" select count(1) from goods where goodsId between ? and ? and goodsId = ? ", beginGoodsId, endGoodsId, goodsId);
                    if (exit > 0) {
                        String unitPriceStr = String.valueOf(tmap.get("UnitPrice"));
                        String discountRateStr = String.valueOf(tmap.get("DiscountRate"));
                        if (unitPriceStr != null && !"".equals(unitPriceStr) && !"null".equalsIgnoreCase(unitPriceStr)) {
                            map.put("UnitPrice", tmap.get("UnitPrice"));
                        }
                        if (discountRateStr != null && !"".equals(discountRateStr) && !"null".equalsIgnoreCase(discountRateStr)) {
                            if (Double.parseDouble(discountRateStr) == 0) {
                                map.put("DiscountRate", "10");
                            } else {
                                map.put("DiscountRate", tmap.get("DiscountRate"));
                            }
                        } else {
                            map.put("DiscountRate", "10");
                        }
                        map.put("SpecialPriceFlag", "true");
                        break;
                    }
                }
            }
            j.setObj(map);
        } catch (Exception e) {
            j.setSuccess(false);
            j.setMsg(e.getMessage());
            SysLogger.error(e.getMessage(), e);
        }
        return j;
    }

    /**
     * 获取店铺促销的积分兑换规则
     * 
     * @param req
     * @return
     */
    @SuppressWarnings("unchecked")
    @RequestMapping(params = "getSP")
    @ResponseBody
    public AjaxJson getSP(HttpServletRequest req) {
        AjaxJson j = new AjaxJson();
        j.setAttributes(new HashMap<String, Object>());
        Map<String, Object> map = new HashMap<String, Object>();
        try {
            String deptId = oConvertUtils.getString(req.getParameter("deptId"));
            // 非促销部门中的促销规则
            List spList = commonDao.findForJdbc(" select * from SP t where Convert(Varchar(10),t.BeginDate,121) <= getDate() and Convert(Varchar(10),t.EndDate,121) >= getDate() and AuditFlag = 1 and spid not in (select spid from SPDepartment) ");
            // 获取登录部门的促销活动
            List spDepartmentList = commonDao.findForJdbc(" select * from SP t where Convert(Varchar(10),t.BeginDate,121) <= getDate() and Convert(Varchar(10),t.EndDate,121) >= getDate() and AuditFlag = 1 and spid in (select Spid from SPDepartment where DepartmentID = ?)", deptId);
            // 促销VIP
            List spVipList = commonDao.findForJdbc(" select * from SPVIP where SPID in (select SPID from SP t where Convert(Varchar(10),t.BeginDate,121) <= getDate() and Convert(Varchar(10),t.EndDate,121) >= getDate() and AuditFlag = 1) ");
            if (spDepartmentList.size() > 0) {
                spList.addAll(spDepartmentList);
            }
            map.put("spList", spList);
            map.put("spVipList", spVipList);
            j.setObj(map);
        } catch (Exception e) {
            j.setSuccess(false);
            j.setMsg(e.getMessage());
            SysLogger.error(e.getMessage(), e);
        }
        return j;
    }

    /**
     * 获取店铺销售明细表
     * 
     * @param req
     * @return
     */
    @RequestMapping(params = "getPossalesDetail")
    @ResponseBody
    public AjaxJson getPossalesDetail(HttpServletRequest req) {
        AjaxJson j = new AjaxJson();
        j.setAttributes(new HashMap<String, Object>());
        Client client = ResourceUtil.getClientFromSession(req);
        try {
            String begindate = oConvertUtils.getString(req.getParameter("beginDate"));
            String enddate = oConvertUtils.getString(req.getParameter("endDate"));
            StringBuffer sql = new StringBuffer();
            sql.append(" select a.No,a.Type,isnull(a.VIPCode,'') VIPCode,g.Code,g.Name,isnull(g.Unit,'') Unit,c.Color,s.Size,b.Quantity,isnull(b.UnitPrice,0) UnitPrice,isnull(b.DiscountRate,0) DiscountRate,isnull(b.Discount,0) Discount,isnull(b.Amount,0) Amount "
                    + " from possales a join possalesDetail b on a.possalesid=b.possalesid " + " join goods g on b.goodsid=g.goodsid join color c on b.colorid=c.colorid " + " join [size] s on b.sizeid=s.sizeid where Convert(varchar(10),[date],121) between ? and ? and a.DepartmentID=? "
                    + " order by a.No,a.Type,a.VIPCode,g.Code,c.Color,s.Size ");
            List list = commonDao.findForJdbc(sql.toString(), begindate, enddate, client.getDeptID());
            j.setObj(list);
        } catch (Exception e) {
            j.setSuccess(false);
            j.setMsg(e.getMessage());
            SysLogger.error(e.getMessage(), e);
        }
        return j;
    }

    /**
     * 获取店铺销售单
     * 
     * @param req
     * @return
     */
    @RequestMapping(params = "getPossales")
    @ResponseBody
    public AjaxJson getPossales(HttpServletRequest req) {
        AjaxJson j = new AjaxJson();
        j.setAttributes(new HashMap<String, Object>());
        Client client = ResourceUtil.getClientFromSession(req);
        try {
            String begindate = oConvertUtils.getString(req.getParameter("beginDate"));
            String enddate = oConvertUtils.getString(req.getParameter("endDate"));
            StringBuffer sql = new StringBuffer();
            sql.append(" Select a.[No],CONVERT(varchar(100), a.[Date], 111) Date,a.VIPCode,e.Name Employee,a.QuantitySum,a.AmountSum, "
                    + " isNull(a.CashMoney,0)+isNull(a.CardMoney,0)+isNull(CashPaper,0)+isNull(PresentAmount,0)+isNull(a.CashAmount1,0)*isNull(a.CashRate1,0)/100.0+isNull(a.DepositAmount,0)+ isNull(a.CashAmount2,0)*isNull(a.CashRate2,0)/100.0+isNull(a.OrderAmount,0) FactAmt,a.Memo "
                    + " From POSSales a left join Employee e on a.EmployeeID=e.EmployeeID where Convert(varchar(10),[date],121) between ? and ? and a.DepartmentID=? and a.Type='销售单' and a.DaySumFlag=0 order by a.No ");
            List list = commonDao.findForJdbc(sql.toString(), begindate, enddate, client.getDeptID());
            j.setObj(list);
        } catch (Exception e) {
            j.setSuccess(false);
            j.setMsg(e.getMessage());
            SysLogger.error(e.getMessage(), e);
        }
        return j;
    }

    /**
     * 获取店铺销售单
     * 
     * @param req
     * @return
     */
    @SuppressWarnings("unchecked")
    @RequestMapping(params = "getPossalesDetailByNo")
    @ResponseBody
    public AjaxJson getPossalesDetailByNo(HttpServletRequest req) {
        AjaxJson j = new AjaxJson();
        j.setAttributes(new HashMap<String, Object>());
        try {
            String no = oConvertUtils.getString(req.getParameter("no"));
            StringBuffer sb = new StringBuffer();
            sb.append(" select PosSalesID,CONVERT(varchar(100), Date, 121) Date,QuantitySum,AmountSum,FactAmountSum," )
            .append(" ISNULL((select name  from employee where employeeId = a.employeeId),'') Employee " )
            .append(" from possales a where No = ? ");
            Map<String,String> tmap = (Map<String, String>) commonDao.findForJdbc(sb.toString(), no).get(0);
            String possalesId = tmap.get("PosSalesID");
            StringBuffer sql = new StringBuffer();
            sql.append(" select g.Code GoodsCode,g.Name GoodsName,GoodsBarcode Barcode,c.Color,s.Size,b.Quantity,isnull(b.FactAmount,0) Amount,")
            .append(" isnull(b.UnitPrice,0) UnitPrice, isnull(b.DiscountRate,10) DiscountRate ")
            .append(" from possalesDetail b join goods g on b.goodsid=g.goodsid join color c on b.colorid=c.colorid ")
            .append(" join [size] s on b.sizeid=s.sizeid where b.possalesid = ?  order by g.Code,c.Color,s.Size  ");
            List list = commonDao.findForJdbc(sql.toString(), possalesId);
            Map<String,Object> map = new HashMap<String, Object>();
            map.put("possales", tmap);
            map.put("possalesDetail", list);
            j.setObj(map);
        } catch (Exception e) {
            j.setSuccess(false);
            j.setMsg(e.getMessage());
            SysLogger.error(e.getMessage(), e);
        }
        return j;
    }

    /**
     * 根据销售单号返回退货信息
     * 
     * @param req
     * @return
     */
    @RequestMapping(params = "getSalesTicketReturnDateBySalesNo")
    @ResponseBody
    public AjaxJson getSalesTicketReturnDateBySalesNo(HttpServletRequest req) {
        AjaxJson j = new AjaxJson();
        j.setAttributes(new HashMap<String, Object>());
        Client client = ResourceUtil.getClientFromSession(req);
        try {
            String salesNo = oConvertUtils.getString(req.getParameter("salesNo"));
            String barcode = oConvertUtils.getString(req.getParameter("barcode"));
            StringBuffer sql = new StringBuffer();
            sql.append(" select goodsBarcode BarCode,g.GoodsID,g.code GoodsCode,g.name GoodsName,c.ColorID,c.no ColorCode,c.color ColorName,s.SizeID,s.size SizeName,s.no SizeCode,"
                    + " isnull(psd.DiscountRate,'10') Discount,-Quantity Quantity,isnull(psd.RetailSales,'0') RetailSales,(FactAmount/Quantity) DiscountPrice,isnull(UnitPrice,'0') UnitPrice from possalesdetail psd join goods g on g.goodsId = psd.goodsId join color c on c.colorId = psd.colorId "
                    + " join size s on s.sizeId = psd.sizeId where 1=1 ");
            if (salesNo != null && !"".equals(salesNo) && !"null".equalsIgnoreCase(salesNo)) {
                sql.append(" and possalesId = (select possalesId from possales where no = '" + salesNo + "') ");
                // 解析条码
                if (barcode != null && !"".equals(barcode) && "null".equalsIgnoreCase(barcode)) {
                    Map<String, String> map = new SelectController().simpleAnalyticalBarcode(barcode, commonDao);
                    if (map.size() > 0) {
                        sql.append(" and g.goodsId = '" + map.get("goodsId") + "' and c.colorId = '" + map.get("colorId") + "' and s.sizeId = '" + map.get("sizeId") + "' ");
                    }
                }
            }
            List list = commonDao.findForJdbc(sql.toString());
            j.setObj(list);
        } catch (Exception e) {
            j.setSuccess(false);
            j.setMsg(e.getMessage());
            SysLogger.error(e.getMessage(), e);
        }
        return j;
    }

    // /**
    // * 修改备注
    // *
    // * @param user
    // * @param req
    // * @return
    // */
    // @RequestMapping(params = "updateMemo")
    // @ResponseBody
    // public AjaxJson updateMemo(HttpServletRequest req) {
    // AjaxJson j = new AjaxJson();
    // j.setAttributes(new HashMap<String, Object>());
    // try{
    // String SalesID = oConvertUtils.getString(req.getParameter("PosSalesID"));
    // String memo = oConvertUtils.getString(req.getParameter("memo"));
    // StringBuilder sb = new StringBuilder();
    // sb.append(" update Possales set memo = ? where PosSalesID = ? ");
    // commonDao.executeSql(sb.toString(),memo,SalesID);
    // }catch( Exception e){
    // j.setSuccess(false);
    // j.setMsg(e.getMessage());
    // SysLogger.error(e.getMessage(),e);
    // }
    // return j;
    // }

}
