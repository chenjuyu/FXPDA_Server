package com.fuxi.web.controller;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
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
    
    //单据冲销功能  新写 2019。09。13
    
    @RequestMapping(params = "POSSalesUnDo")
    @ResponseBody
    public AjaxJson POSSalesUnDo (HttpServletRequest req){
    	  AjaxJson j = new AjaxJson();
          j.setAttributes(new HashMap<String, Object>());
          Client client = ResourceUtil.getClientFromSession(req);
          try {
        	  
        	  String POSSalesID =oConvertUtils.getString(req.getParameter("POSSalesID"));
        	  String deptNo =oConvertUtils.getString(req.getParameter("deptNo"));
        	  
        	  commonDao.ExecPOSSalesUnDo(POSSalesID, deptNo, client.getUserName());
        	  j.setMsg("冲销成功");
          }catch(Exception e){
        	  j.setSuccess(false);
              j.setMsg(e.getMessage());
              SysLogger.error(e.getMessage(), e);
          }
    	
    	return j;
    }
    
    @RequestMapping(params = "report")
    @ResponseBody
    public AjaxJson report (HttpServletRequest req){
    	  AjaxJson j = new AjaxJson();
          j.setAttributes(new HashMap<String, Object>());
          Client client = ResourceUtil.getClientFromSession(req);
          try {
        	  
        	  String Condition =oConvertUtils.getString(req.getParameter("Condition"));
        	  String searchType =oConvertUtils.getString(req.getParameter("searchType"));//查看是按哪个类型汇总的
        	  String DisType =oConvertUtils.getString(req.getParameter("DisType"));
        	  String DepartmentID =oConvertUtils.getString(req.getParameter("DepartmentID"));
        	  String DistrictID =oConvertUtils.getString(req.getParameter("DistrictID"));
        	  String Orderby =oConvertUtils.getString(req.getParameter("Orderby"));
        	  String OrderField ="";		  
        	  int OrderFieldNo =2;
        	  String BeginDate =oConvertUtils.getString(req.getParameter("BeginDate"));
        	  String EndDate =oConvertUtils.getString(req.getParameter("EndDate"));
        	  String userID =client.getUserID();
        	  List<Map<String,Object>> ls= commonDao.Exec8088Rpt(searchType,Condition, DisType, DepartmentID, DistrictID, Orderby, OrderField, OrderFieldNo, BeginDate, EndDate, userID);
        	  j.setObj(ls);
        	  
          }catch(Exception e){
        	  
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
            String beginDate = oConvertUtils.getString(req.getParameter("beginDate"));
            String endDate = oConvertUtils.getString(req.getParameter("endDate"));
            int page =oConvertUtils.getInt(req.getParameter("currPage"));
            String no = oConvertUtils.getString(req.getParameter("no"));
            String audit= oConvertUtils.getString(req.getParameter("audit"));
            String departmentId=oConvertUtils.getString(req.getParameter("departmentId"));
            String employeeId =oConvertUtils.getString(req.getParameter("employeeId"));
            
            StringBuffer sql = new StringBuffer(); //and a.DaySumFlag=0 再返回一个部门编码，用于冲销功能 DelFlag 冲销标志
            sql.append(" Select a.Audit,a.AuditFlag,a.TallyFlag,a.DelFlag,a.EmployeeID,a.DaySumFlag,a.DepartmentID,d.Code deptNo,d.Department,a.CashMoney,isNull(a.CashMoney,0)-isNull(a.ReturnMoney,0) RealCash,a.POSSalesID,a.VipID,v.Code VipCode,v.Vip,a.[No],Convert(Varchar(19),a.[Date],121) as Date,e.Name Employee,a.QuantitySum,a.FactAmountSum,a.AmountSum, "
                    + " isNull(a.CashMoney,0)+isNull(a.CardMoney,0)+isNull(CashPaper,0)+isNull(PresentAmount,0)+isNull(a.CashAmount1,0)*Convert(float,isNull(a.CashRate1,0)/100.0)+isNull(a.DepositAmount,0)+ isNull(a.CashAmount2,0)*Convert(float,isNull(a.CashRate2,0)/100.0)+isNull(a.OrderAmount,0) FactAmt,a.Memo "
                    + " From POSSales a left join Vip v on a.VipID=v.VipID Join Department d on a.DepartmentID=d.DepartmentID left join Employee e on a.EmployeeID=e.EmployeeID where  a.Type='销售单'  ");
          
            // 时间区间
            if (beginDate != null && !"".equals(beginDate.trim()) && !"null".equalsIgnoreCase(beginDate) && endDate != null && !"".equals(endDate.trim()) && !"null".equalsIgnoreCase(endDate)) {
               // sb.append(" and Date between convert(datetime,'" + beginDate + "', 120) and convert(datetime,'" + endDate + "', 120) ");
            	sql.append(" and a.Date >= '" + beginDate + "' and a.Date <='" + endDate + " 23:59:59.997'");
            }
            
            
            // 部门
            if (departmentId != null && !"".equals(departmentId.trim()) && !"null".equalsIgnoreCase(departmentId)) {
                sql.append(" and a.departmentId = '" + departmentId + "' ");
            }
            // 经手人
            if (employeeId != null && !"".equals(employeeId.trim()) && !"null".equalsIgnoreCase(employeeId)) {
                sql.append(" and a.employeeId = '" + employeeId + "' ");
            }
            
            // 经手人
            if (audit != null && !"".equals(audit.trim()) && !"null".equalsIgnoreCase(audit)) {
                sql.append(" and a.audit = '" + audit + "' ");
            }
            
            // 经手人 这个是
            if (no != null && !"".equals(no.trim()) && !"null".equalsIgnoreCase(no)) {
                sql.append(" and a.no like '%" + no + "%' ");
            }
            
            
            sql.append(" and  a.DepartmentID in (Select DepartmentID From DepartmentRight Where UserID='"+client.getUserID()+"' and RightFlag=1) "+ 
                       " Order by a.[Date] DESC,(a.[No]) DESC");
            
            List<Map<String,Object>> list = commonDao.findForJdbc(sql.toString(), page, 15);
            System.out.println("sql语句:"+sql.toString());
            
            System.out.println("list记录数:"+list.size());
            
            for(int i=0;i<list.size();i++){
            	Map<String,Object> map=list.get(i);
            	
            	 if(!"".equals(String.valueOf(map.get("FactAmountSum"))) && map.get("FactAmountSum") !=null){
         			 map.put("FactAmountSum", new BigDecimal(String.valueOf(map.get("FactAmountSum"))).setScale(2,BigDecimal.ROUND_DOWN));
         			 }else{
         			 map.put("FactAmountSum", "");	 
         			 }
            	 
            	 if(!"".equals(String.valueOf(map.get("AmountSum"))) && map.get("AmountSum") !=null){
         			 map.put("AmountSum", new BigDecimal(String.valueOf(map.get("AmountSum"))).setScale(2,BigDecimal.ROUND_DOWN));
         			 }else{
         			 map.put("AmountSum", "");	 
         			 } 
            	 if(!"".equals(String.valueOf(map.get("FactAmt"))) && map.get("FactAmt") !=null){
         			 map.put("FactAmt", new BigDecimal(String.valueOf(map.get("FactAmt"))).setScale(2,BigDecimal.ROUND_DOWN));
         			 }else{
         			 map.put("FactAmt", "");	 
         			 } 
            	 
            	 
            	 if(!"".equals(String.valueOf(map.get("RealCash"))) && map.get("RealCash") !=null){
         			 map.put("RealCash", new BigDecimal(String.valueOf(map.get("RealCash"))).setScale(2,BigDecimal.ROUND_DOWN));
         			 }else{
         			 map.put("RealCash", "");	 
         			 } 
            	 
            	 
            	 List<Map<String,Object>> right =new ArrayList<>();
     			 Map<String,Object> m=new LinkedHashMap<>();
     			 m.put("text", "冲销");
				 Map<String,Object> stylemap= new LinkedHashMap<>();
				 stylemap.put("backgroundColor", "#F4333C");
				 stylemap.put("color", "white");
				 m.put("style",stylemap );
				 right.add(m);
				 
				 map.put("right", right);
	 			 
            }
           
           if(list.size() >0){
           j.setMsg("成功返回数据");
           j.setObj(list);
 	       }else{
 		    j.setMsg("暂无数据"); 
 	       }
            
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
            System.out.println("possalesId:"+possalesId);
            StringBuffer sql = new StringBuffer(); //join [size] s on b.sizeid=s.sizeid
            sql.append(" select a.*,b.Code GoodsCode,b.Name GoodsName,GoodsBarcode Barcode,c.Color,c.No ColorNo,d.Size,a.Quantity,isnull(a.FactAmount,0) Amount,")
            .append(" isnull(a.UnitPrice,0) UnitPrice, isnull(a.DiscountRate,10) DiscountRate ")
            .append(" from possalesDetail a join goods b on a.goodsid=b.goodsid join color c on a.colorid=c.colorid ")
            .append(" join GoodsType e on b.GoodsTypeID=e.GoodsTypeID ")
            .append(" join SizeGroupSize d on e.SizeGroupID=d.SizeGroupID and a.SizeID=d.SizeID")
            .append("  where a.possalesid = ?  order by  a.SN,(b.[Code]),(c.[No]),d.[No] ");
            List<Map<String,Object>> list = commonDao.findForJdbc(sql.toString(), possalesId);
            //保留两位小数
            for(int i=0;i<list.size();i++){
            	Map<String,Object> datamap=list.get(i);
            	 if(!"".equals(String.valueOf(datamap.get("UnitPrice"))) && datamap.get("UnitPrice") !=null){
            		 datamap.put("UnitPrice", new BigDecimal(String.valueOf(datamap.get("UnitPrice"))).setScale(2,BigDecimal.ROUND_DOWN));
         			 }else{
         				datamap.put("UnitPrice", "");	 
         			 } 
            	 if(!"".equals(String.valueOf(datamap.get("DiscountRate"))) && datamap.get("DiscountRate") !=null){
            		 datamap.put("DiscountRate", new BigDecimal(String.valueOf(datamap.get("DiscountRate"))).setScale(2,BigDecimal.ROUND_DOWN));
         			 }else{
         				datamap.put("DiscountRate", "");	 
         			 } 
            	 if(!"".equals(String.valueOf(datamap.get("Discount"))) && datamap.get("Discount") !=null){
            		 datamap.put("Discount", new BigDecimal(String.valueOf(datamap.get("Discount"))).setScale(2,BigDecimal.ROUND_DOWN));
         			 }else{
         				datamap.put("Discount", "");	 
         			 } 	 
            	 if(!"".equals(String.valueOf(datamap.get("Amount"))) && datamap.get("Amount") !=null){
            		 datamap.put("Amount", new BigDecimal(String.valueOf(datamap.get("Amount"))).setScale(2,BigDecimal.ROUND_DOWN));
         			 }else{
         				datamap.put("Amount", "");	 
         			 } 
            	 if(!"".equals(String.valueOf(datamap.get("RetailSales"))) && datamap.get("RetailSales") !=null){
            		 datamap.put("RetailSales", new BigDecimal(String.valueOf(datamap.get("RetailSales"))).setScale(2,BigDecimal.ROUND_DOWN));
         			 }else{
         				datamap.put("RetailSales", "");	 
         			 } 	 
            	 if(!"".equals(String.valueOf(datamap.get("RetailAmount"))) && datamap.get("RetailAmount") !=null){
            		 datamap.put("RetailAmount", new BigDecimal(String.valueOf(datamap.get("RetailAmount"))).setScale(2,BigDecimal.ROUND_DOWN));
         			 }else{
         				datamap.put("RetailAmount", "");	 
         			 }
            	 
            	 if(!"".equals(String.valueOf(datamap.get("FactAmount"))) && datamap.get("FactAmount") !=null){
            		 datamap.put("FactAmount", new BigDecimal(String.valueOf(datamap.get("FactAmount"))).setScale(2,BigDecimal.ROUND_DOWN));
         			 }else{
         				datamap.put("FactAmount", "");	 
         			 } 
            	 if(!"".equals(String.valueOf(datamap.get("PointRate"))) && datamap.get("PointRate") !=null){
            		 datamap.put("PointRate", new BigDecimal(String.valueOf(datamap.get("PointRate"))).setScale(2,BigDecimal.ROUND_DOWN));
         			 }else{
         				datamap.put("PointRate", "");	 
         			 }  
            	 
            }  
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
