package com.fuxi.web.controller;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fuxi.core.common.dao.impl.CommonDao;
import com.fuxi.core.common.model.json.AjaxJson;
import com.fuxi.core.common.service.StockMoveService;
import com.fuxi.system.util.Client;
import com.fuxi.system.util.DataUtils;
import com.fuxi.system.util.MyTools;
import com.fuxi.system.util.ResourceUtil;
import com.fuxi.system.util.SysLogger;
import com.fuxi.system.util.oConvertUtils;

/**
 * Title: StockMoveController Description: 转仓单逻辑控制器
 * 
 * @author LYJ
 * 
 */
@Controller
@RequestMapping("/stockMove")
public class StockMoveController extends BaseController {

    private Logger log = Logger.getLogger(StockMoveController.class);
    private SelectController controller = new SelectController();
    private CommonController commonController = new CommonController();

    @Autowired
    private CommonDao commonDao;
    @Autowired
    private StockMoveService stockMoveService;

    /**
     * 根据筛选条件获取转仓单列表(主表信息)
     * 
     * @param user
     * @param req
     * @return
     */
    @RequestMapping(params = "getStockMove")
    @ResponseBody
    public AjaxJson getStockMove(HttpServletRequest req) {
        Client client = ResourceUtil.getClientFromSession(req);
        AjaxJson j = new AjaxJson();
        j.setAttributes(new HashMap<String, Object>());
        Map<String, String> map = client.getMap();
        String userRight = map.get(client.getUserID());
        try {
            int page = oConvertUtils.getInt(req.getParameter("currPage"));
            String audit = oConvertUtils.getString(req.getParameter("audit"));
            String no = oConvertUtils.getString(req.getParameter("no"));
            String beginDate = oConvertUtils.getString(req.getParameter("beginDate"));
            String endDate = oConvertUtils.getString(req.getParameter("endDate"));
            String departmentId = oConvertUtils.getString(req.getParameter("departmentId"));
            String customerId = oConvertUtils.getString(req.getParameter("customerId"));
            String employeeId = oConvertUtils.getString(req.getParameter("employeeId"));
            StringBuffer sb = new StringBuffer();
            sb.append(
                    " select st.stockMoveId, de.Department, No, CONVERT(varchar(100), Date, 111) Date,isnull(QuantitySum,0) QuantitySum," + " RetailAmountSum RelationAmountSum, AuditFlag,(select Name from Employee where employeeId = st.EmployeeId) Employee,"
                            + "isnull(st.Memo,'') Memo,(select Department from Department where DepartmentId = st.WarehouseInID) WarehouseIn," + "(select Brand from Brand where BrandId = st.BrandID) Brand from stockMove st  ")
                    .append(" left join Department de on de.DepartmentID = st.WarehouseOutID where de.DepartmentID in (").append(userRight).append(")   ");
            // 按条件查询
            if (null != audit && "0".equals(audit)) {
                // 未审核
                sb.append(" and AuditFlag = '0' ");
            } else if (null != audit && "1".equals(audit)) {
                // 已审核
                sb.append(" and AuditFlag = '1' ");
            }
            // 查询单号时
            if (no != null && !"".equals(no.trim()) && !"null".equalsIgnoreCase(no)) {
                sb.append(" and No = '" + no + "' ");
            }
            // 时间区间
            if (beginDate != null && !"".equals(beginDate.trim()) && !"null".equalsIgnoreCase(beginDate) && endDate != null && !"".equals(endDate.trim()) && !"null".equalsIgnoreCase(endDate)) {
                sb.append(" and Date between convert(datetime,'" + beginDate + "', 120) and convert(datetime,'" + endDate + "', 120) ");
            }
            // 转进仓库
            if (departmentId != null && !"".equals(departmentId.trim()) && !"null".equalsIgnoreCase(departmentId)) {
                sb.append(" and st.WarehouseInID = '" + departmentId + "' ");
            }
            // 转出仓库
            if (customerId != null && !"".equals(customerId.trim()) && !"null".equalsIgnoreCase(customerId)) {
                sb.append(" and st.WarehouseOutID = '" + customerId + "' ");
            }
            // 经手人
            if (employeeId != null && !"".equals(employeeId.trim()) && !"null".equalsIgnoreCase(employeeId)) {
                sb.append(" and st.employeeId = '" + employeeId + "' ");
            }
            sb.append(" order by madebydate desc,No desc ");
            List list = commonDao.findForJdbc(sb.toString(), page, 15);
            j.setObj(list);
        } catch (Exception e) {
            j.setSuccess(false);
            j.setMsg(e.getMessage());
            SysLogger.error(e.getMessage(), e);
        }
        return j;
    }

    /**
     * 根据转仓单ID获取转仓单明细信息(子表信息)
     * 
     * @param req
     * @return
     */
    @RequestMapping(params = "stockMoveEdit")
    @ResponseBody
    public AjaxJson stockMoveEdit(HttpServletRequest req) {
        AjaxJson j = new AjaxJson();
        j.setAttributes(new HashMap<String, Object>());
        Client client = ResourceUtil.getClientFromSession(req);
        try {
            String stockMoveId = oConvertUtils.getString(req.getParameter("stockMoveId"));
            int boxQtySum = Integer.parseInt(String.valueOf(commonDao.getData("select isnull(sum(isnull(sodt.BoxQty,0)),0) BoxQty from stockMoveDetailtemp sodt where stockMoveId = ? ", stockMoveId)));
            int sizeStrCount = Integer.parseInt(String.valueOf(commonDao.getData("select count(1) from stockMoveDetailtemp sodt where stockMoveId = ? ", stockMoveId)));
            int notNullSizeStrCount = Integer.parseInt(String.valueOf(commonDao.getData("select count(1) from stockMoveDetailtemp sodt where stockMoveId = ? and sodt.SizeStr is not null and sodt.SizeStr <> '' ", stockMoveId)));
            StringBuffer sb = new StringBuffer();
            sb.append("  select WarehouseInID,de.No, WarehouseOutID ,isnull(de.AuditFlag,0)  AuditFlag,").append(boxQtySum)
                    .append(" BoxQtySum,( select isnull(Department,'') from Department where DepartmentId = WarehouseInID) InDepartment,( select isnull(Department,'') from Department where DepartmentId = WarehouseOutID) OutDepartment,")
                    .append(" isnull(de.Memo,'') Memo,de.EmployeeID,(select Name from Employee where EmployeeID = de.EmployeeID) Employee,de.BrandID,(select Brand from Brand where brandId = de.BrandId) Brand ,isnull(de.QuantitySum,0) QuantitySum from stockMove de  ")
                    .append(" where stockMoveId = '").append(stockMoveId).append("'");
            List list = commonDao.findForJdbc(sb.toString());
            if (list.size() > 0) {
                Map map = (Map) list.get(0);
                j.setAttributes(map);
                sb = new StringBuffer();
                List detailList = null;
                if (sizeStrCount == notNullSizeStrCount) {
                    sb.append(" select a.*,g.code GoodsCode ,g.name GoodsName,c.No ColorCode,isnull(pdt.BoxQty,0) BoxQty,pdt.Quantity QuantitySum,a.SizeStr,isnull(pdt.RetailSales,0) DiscountPrice,isnull(pdt.UnitPrice,0) UnitPrice, ")
                            .append(" c.Color ,sg.SizeGroupID,isnull(pdt.RetailSales,0) RetailSales,isnull(pdt.memo,'') meno from GoodsBoxBarcode a join goods g on a.goodsid=g.goodsid ").append(" join color c on a.colorid=c.colorid ").append(" join GoodsType gt on gt.GoodsTypeID = g.GoodsTypeID ")
                            .append(" join SizeGroup sg on sg.SizeGroupID =gt.SizeGroupID ").append(" join stockMoveDetailtemp pdt on pdt.goodsId = a.goodsId and pdt.colorId = a.colorid and pdt.sizeStr = a.sizeStr ").append("  where pdt.stockMoveId = '").append(stockMoveId)
                            .append("' order by pdt.GoodsID ");
                    detailList = controller.getDetailTemp(commonDao.findForJdbc(sb.toString()), client, commonDao);
                } else {
                    sb.append(" select detail.GoodsID,g.Name GoodsName,c.No ColorCode,s.No SizeCode,detail.ColorID,c.Color, ")
                            .append(" detail.SizeID,s.Size,detail.Quantity,sodt.Quantity QuantitySum, '' Barcode, g.Code GoodsCode, isnull(sodt.RetailSales,0) DiscountPrice,isnull(sodt.RetailSales,0) RetailSales,isnull(sodt.UnitPrice,0) UnitPrice, ")
                            .append(" isnull(sodt.BoxQty,0) BoxQty,isnull(sodt.Quantity/nullif(sodt.BoxQty,0),0) OneBoxQty, sodt.IndexNo,sodt.SizeStr,isnull(sodt.memo,'') meno from stockMoveDetail detail ").append(" left join Goods g on g.GoodsID = detail.GoodsID ")
                            .append(" left join Color c on c.ColorID = detail.ColorID ").append(" left join Size s on s.SizeID = detail.SizeID ")
                            .append(" left join stockMoveDetailtemp sodt on sodt.stockMoveId = detail.stockMoveId and sodt.GoodsID = detail.GoodsID and sodt.ColorID = detail.ColorID ").append("  where sodt.stockMoveId = '").append(stockMoveId).append("' order by detail.GoodsID ");
                    detailList = commonDao.findForJdbc(sb.toString());
                }
                j.getAttributes().put("detailList", detailList);
            }
        } catch (Exception e) {
            j.setSuccess(false);
            j.setMsg(e.getMessage());
            SysLogger.error(e.getMessage(), e);
        }
        return j;
    }

    /**
     * 保存转仓单信息[新增,修改]
     * 
     * @param req
     * @return
     */
    @RequestMapping(params = "addStockMove")
    @ResponseBody
    public AjaxJson addStockMove(HttpServletRequest req) {
        Client client = ResourceUtil.getClientFromSession(req);
        AjaxJson j = new AjaxJson();
        j.setAttributes(new HashMap<String, Object>());
        try {
            String warehouseInId = oConvertUtils.getString(req.getParameter("warehouseInId"));
            String warehouseOutId = oConvertUtils.getString(req.getParameter("warehouseOutId"));
            String stockMoveId = oConvertUtils.getString(req.getParameter("stockMoveId"));
            String employeeId = oConvertUtils.getString(req.getParameter("employeeId"));
            String brandId = oConvertUtils.getString(req.getParameter("brandId"));
            String memo = oConvertUtils.getString(req.getParameter("memo"));
            String notUseNegativeInventoryCheck = oConvertUtils.getString(req.getParameter("notUseNegativeInventoryCheck"));
            String jsonStr = oConvertUtils.getString(req.getParameter("data"));
            JSONArray datas = JSONArray.fromObject(jsonStr);
            List<Map<String, Object>> dataList = JSONArray.toList(datas, Map.class);
            // 判断检查负库存
            String id = null;
            List<Map<String, Object>> tempList = new ArrayList<Map<String, Object>>();
            if (!"true".equalsIgnoreCase(notUseNegativeInventoryCheck)) {
                boolean mustExistsGoodsFlag = Boolean.parseBoolean(String.valueOf(commonDao.getData(" select MustExistsGoodsFlag from Department where DepartmentID = ? ", warehouseOutId)));
                if (mustExistsGoodsFlag && !client.isSuperSalesFlag()) {
                    tempList = commonController.checkNegativeInventoryForBackStage(commonDao, dataList, client.getOnLineId(), client.getUserID(), warehouseOutId, 36, stockMoveId, 0, 2, 0, 0, 0, "");
                }
            }
            if (tempList.size() == 0) {
                // 保存单据
                id = stockMoveService.saveStockMove(warehouseInId, warehouseOutId, employeeId, stockMoveId, memo, dataList, brandId, client);
            }
            j.getAttributes().put("stockMoveId", id);
            j.getAttributes().put("tempList", tempList);
        } catch (Exception e) {
            j.setSuccess(false);
            j.setMsg(e.getMessage());
            SysLogger.error(e.getMessage(), e);
        }
        return j;
    }

    /**
     * 修改转仓单明细数量[子表记录]
     * 
     * @param req
     * @return
     */
    @RequestMapping(params = "updateCount")
    @ResponseBody
    public AjaxJson updateCount(HttpServletRequest req) {
        AjaxJson j = new AjaxJson();
        j.setAttributes(new HashMap<String, Object>());
        try {
            String stockMoveId = oConvertUtils.getString(req.getParameter("stockMoveId"));
            String jsonStr = oConvertUtils.getString(req.getParameter("data"));
            JSONArray datas = JSONArray.fromObject(jsonStr);
            List<Map<String, Object>> dataList = JSONArray.toList(datas, Map.class);
            stockMoveService.deleteStockMovedetail(dataList, stockMoveId);
        } catch (Exception e) {
            j.setSuccess(false);
            j.setMsg(e.getMessage());
            SysLogger.error(e.getMessage(), e);
        }
        return j;
    }

    /**
     * 审核转仓单,审核后生成对应的进出仓单
     * 
     * @param user
     * @param req
     * @return
     */
    @RequestMapping(params = "auditStockMove")
    @ResponseBody
    public AjaxJson auditStockMove(HttpServletRequest req) {
        int result = 0;
        AjaxJson j = new AjaxJson();
        j.setAttributes(new HashMap<String, Object>());
        try {
            String stockMoveId = oConvertUtils.getString(req.getParameter("StockMoveID"));//stockMoveId
            String user = ((Client) ResourceUtil.getClientFromSession(req)).getUserName();
            commonDao.auditStockMove(stockMoveId, 1, user, 1);
            StringBuffer sql = new StringBuffer();
            sql.append(" update stockMove set Audit = ? , AuditDate = ? , AuditFlag = ? where stockMoveId = ? ; ");
            result = commonDao.executeSql(sql.toString(), user, DataUtils.gettimestamp(), true, stockMoveId);
            j.getAttributes().put("result", result);
        } catch (Exception e) {
            j.setSuccess(false);
            j.setMsg(e.getMessage());
            SysLogger.error(e.getMessage(), e);
        }
        return j;
    }

    /**
     * 转仓单条码校验后,以校验结果覆盖原始单据[新增,修改,删除]
     * 
     * @param req
     * @return
     */
    @RequestMapping(params = "coverSave")
    @ResponseBody
    public AjaxJson coverSave(HttpServletRequest req) {
        Client client = ResourceUtil.getClientFromSession(req);
        AjaxJson j = new AjaxJson();
        j.setAttributes(new HashMap<String, Object>());
        try {
            String stockMoveId = oConvertUtils.getString(req.getParameter("stockMoveId"));
            String jsonStr = oConvertUtils.getString(req.getParameter("data"));
            JSONArray datas = JSONArray.fromObject(jsonStr);
            List<Map<String, Object>> dataList = JSONArray.toList(datas, Map.class);
            int count = stockMoveService.coverSave(stockMoveId, dataList, client);
            j.setObj(count);
        } catch (Exception e) {
            j.setSuccess(false);
            j.setMsg(e.getMessage());
            SysLogger.error(e.getMessage(), e);
        }
        return j;
    }

    /**
     * 修改单据备注信息
     * 
     * @param user
     * @param req
     * @return
     */
    @RequestMapping(params = "updateMemo")
    @ResponseBody
    public AjaxJson updateMemo(HttpServletRequest req) {
        AjaxJson j = new AjaxJson();
        j.setAttributes(new HashMap<String, Object>());
        try {
            String stockMoveId = oConvertUtils.getString(req.getParameter("stockMoveId"));
            String memo = oConvertUtils.getString(req.getParameter("memo"));
            StringBuilder sb = new StringBuilder();
            sb.append(" update stockMove set memo = ? where stockMoveId = ? ");
            commonDao.executeSql(sb.toString(), memo, stockMoveId);
        } catch (Exception e) {
            j.setSuccess(false);
            j.setMsg(e.getMessage());
            SysLogger.error(e.getMessage(), e);
        }
        return j;
    }
    
   //2019 新写的
    @RequestMapping(params = "stockmovelist")
    @ResponseBody
    public AjaxJson stockmovelist(HttpServletRequest req) {
        Client client = ResourceUtil.getClientFromSession(req);
        AjaxJson j = new AjaxJson();
        j.setAttributes(new HashMap<String, Object>());
        Map<String, String> map = client.getMap();
        String userRight = map.get(client.getUserID());
        try {
            int page = oConvertUtils.getInt(req.getParameter("currPage"));
            String audit = oConvertUtils.getString(req.getParameter("audit"));
            String no = oConvertUtils.getString(req.getParameter("no"));
            String beginDate = oConvertUtils.getString(req.getParameter("beginDate"));
            String endDate = oConvertUtils.getString(req.getParameter("endDate"));
            String departmentId = oConvertUtils.getString(req.getParameter("departmentId"));
            String customerId = oConvertUtils.getString(req.getParameter("customerId"));
            String employeeId = oConvertUtils.getString(req.getParameter("employeeId"));
            StringBuffer sb = new StringBuffer();  //增加一个CustomerID返回，转进 仓库的 用于新增单据的货品价格取值
            sb.append(
                    " select st.StockMoveID,st.AuditFlag,st.MoveInType,st.MoveOutType,st.AmountSum,st.DepartmentID,st.MadeBy,st.No,st.WarehouseOutID,st.WarehouseInID, de.Department, CONVERT(varchar(100), Date, 111) Date,CONVERT(varchar(100), st.AuditDate, 111) AuditDate,CONVERT(varchar(100), st.MadeByDate, 111) MadeByDate,isnull(QuantitySum,0) QuantitySum," + " RetailAmountSum RelationAmountSum,(select Name from Employee where employeeId = st.EmployeeId) Name,"
                            + "isnull(st.Memo,'') Memo,(select Department from Department where DepartmentId = st.WarehouseInID) WarehouseIn," +
                    		"(select SettleCustID from Department where DepartmentId = st.WarehouseInID) CustomerID,"+
                    "(select Department from Department where DepartmentId = st.WarehouseOutID) WarehouseOut,"+		
            		"(select Brand from Brand where BrandId = st.BrandID) Brand from stockMove st  ")
                    .append(" left join Department de on de.DepartmentID = st.WarehouseOutID where de.DepartmentID in (").append(userRight).append(")   ");
            // 按条件查询
            if (null != audit && "0".equals(audit)) {
                // 未审核
                sb.append(" and AuditFlag = '0' ");
            } else if (null != audit && "1".equals(audit)) {
                // 已审核
                sb.append(" and AuditFlag = '1' ");
            }
            // 查询单号时
            if (no != null && !"".equals(no.trim()) && !"null".equalsIgnoreCase(no)) {
                sb.append(" and No like '%" + no + "%' ");
            }
            // 时间区间
            if (beginDate != null && !"".equals(beginDate.trim()) && !"null".equalsIgnoreCase(beginDate) && endDate != null && !"".equals(endDate.trim()) && !"null".equalsIgnoreCase(endDate)) {
              //  sb.append(" and Date between convert(datetime,'" + beginDate + "', 120) and convert(datetime,'" + endDate + "', 120) ");
            	 sb.append(" and st.Date >= '" + beginDate + "' and st.Date <='" + endDate + " 23:59:59.997'");
            }
            // 转进仓库
            if (departmentId != null && !"".equals(departmentId.trim()) && !"null".equalsIgnoreCase(departmentId)) {
                sb.append(" and st.WarehouseInID = '" + departmentId + "' ");
            }
            // 转出仓库
            if (customerId != null && !"".equals(customerId.trim()) && !"null".equalsIgnoreCase(customerId)) {
                sb.append(" and st.WarehouseOutID = '" + customerId + "' ");
            }
            // 经手人
            if (employeeId != null && !"".equals(employeeId.trim()) && !"null".equalsIgnoreCase(employeeId)) {
                sb.append(" and st.employeeId = '" + employeeId + "' ");
            }
            sb.append(" order by madebydate desc,No desc ");
            List<Map<String,Object>> list = commonDao.findForJdbc(sb.toString(), page, 15);
            
            //无不做取消审核的
            for(int i=0;i<list.size();i++){
          	  Map<String,Object> m=list.get(i);
          	  if(!"".equals(String.valueOf(m.get("AmountSum"))) && m.get("AmountSum") !=null){
          		  m.put("AmountSum", new BigDecimal(String.valueOf(m.get("AmountSum"))).setScale(2,BigDecimal.ROUND_DOWN)); 
          	  }else{
          		  m.put("AmountSum", ""); 
          	  }
          	  List<Map<String,Object>> right=new ArrayList<>();
          	  
          	  for(int n=0;n<2;n++){
          	  Map<String,Object> rmap=new LinkedHashMap<>();
          	  Map<String,Object> stylemap=new LinkedHashMap<>();
          	  if(n==0){
              	  rmap.put("text", "删除");
              	  stylemap.put("backgroundColor", "orange");
              	  stylemap.put("color", "white");
              	  rmap.put("style", stylemap);
               }else if(n==1){
          	  rmap.put("text", "审核");
          	  stylemap.put("backgroundColor", "mediumspringgreen");
          	  stylemap.put("color", "white");
          	  rmap.put("style", stylemap);
          	  
          	  }/* else if(n==2){
          	  rmap.put("text", "反审");
          	  stylemap.put("backgroundColor", "#F4333C");
          	  stylemap.put("color", "white");
          	  rmap.put("style", stylemap);
          	  } */ 
          	  right.add(rmap);
            }
          	  m.put("right", right);
            }
            
           System.out.print("list:"+list.toString());
            if(list.size()>0){
            j.setObj(list);
            }else{
            j.setMsg("暂无数据");	  
            }
            
            
            
            j.setObj(list);
        } catch (Exception e) {
            j.setSuccess(false);
            j.setMsg(e.getMessage());
            SysLogger.error(e.getMessage(), e);
        }
        return j;
    }
    
    /*
     * 根据 purchaseid 显示 单据详情，显示横向尺码
     * 
     * */
    @RequestMapping(params = "stockMoveEditX")
    @ResponseBody
    
    public AjaxJson stockMoveEditX(HttpServletRequest req){
        AjaxJson j =new AjaxJson();
        j.setAttributes(new HashMap<String, Object>());
        Client client = ResourceUtil.getClientFromSession(req);
        List<Map<String, Object>> list =new ArrayList<>();
    	try{
    		String StockMoveID = oConvertUtils.getString(req.getParameter("StockMoveID"));
    		int direction=1; //Integer.parseInt(oConvertUtils.getString(req.getParameter("direction"))); //退货时，要按-1让显示 变成正数
    		
    		System.out.print("direction:"+direction);
    		
    		StringBuffer sb = new StringBuffer();
    	  sb.append("Select a.*,b.Code,b.SupplierCode, b.Name,b.Model,b.Unit,c.Color,b.GroupID,b.GroupNo,"+
    		"b.StopFlag,b.age,b.Season,br.brand,b.RetailSales1,b.RetailSales2,b.PurchasePrice,bs.Serial "+
    		"from StockMoveDetailTemp a join Goods b on a.GoodsID = b.GoodsID "+
    		"join Color c on a.ColorID=c.ColorID left join brand br on b.brandid = br.brandid "+
    		"left join BrandSerial bs on b.BrandSerialID = bs.BrandSerialID "+
    		"where a.StockMoveID= '"+StockMoveID+"'" + 
    		"order by a.StockMoveID, a.IndexNo, b.Code,c.No");	
    	 //子表的所有数据
   		List<Map<String, Object>> datalist= commonDao.findForJdbc(sb.toString());
    	  if(datalist.size() >0){
    		 for(int i=0 ;i<datalist.size();i++){ //提取数据
    			Map<String,Object> datamap=new LinkedHashMap<>(); 
    			Map<String,Object> map = datalist.get(i); //每一条
    			String sql="select * from SizeGroupSize where SizeGroupID='"+String.valueOf(map.get("GroupID"))+"'";
    			List<Map<String, Object>> sizelist=commonDao.findForJdbc(sql); //查询
    			List<Map<String, Object>> sizetitle =new ArrayList<>();
    			List<Map<String, Object>> sizeData =new ArrayList<>();
    			 for(int k=0;k<sizelist.size() ;k++){ //sizetitle 查询所嘱的尺码
    				 Map<String,Object> smap=sizelist.get(k);
    				 Map<String,Object> newmap=new LinkedHashMap<>();
    				 newmap.put("field", "x_"+String.valueOf(smap.get("No")));
    				 newmap.put("title", String.valueOf(smap.get("Size")));
    				 sizetitle.add(newmap);
    				 //--------------数据-------------------
    				 Map<String,Object> sdata=new LinkedHashMap<>();
    				 sdata.put("StockMoveDetailID", String.valueOf(map.get("StockMoveDetailID")));
    				 sdata.put("StockMoveID", String.valueOf(map.get("StockMoveID")));
    				 sdata.put("GoodsID",String.valueOf(map.get("GoodsID")));
    				 sdata.put("ColorID",String.valueOf(map.get("ColorID")));
    				 sdata.put("x","x_"+String.valueOf(smap.get("No")));
    				 sdata.put("Size",String.valueOf(smap.get("Size")));
    				 sdata.put("Color",String.valueOf(map.get("Color")));
    				 sdata.put("SizeID",String.valueOf(smap.get("SizeID")));
    				 System.out.println("尺码显示列："+"x_"+String.valueOf(smap.get("No")));
    				 //有箱数的情况
    				 if(!"0".equals(String.valueOf(map.get("BoxQty"))) && map.get("BoxQty") !=null){
    					 if(String.valueOf(map.get("x_"+String.valueOf(smap.get("No")))) !=null && map.get("x_"+String.valueOf(smap.get("No"))) !=null){
    						 
    					// BigDecimal Quantity = new BigDecimal(Double.valueOf(String.valueOf(map.get("x_"+String.valueOf(smap.get("No"))))))
    					//.multiply(new BigDecimal(String.valueOf(map.get("BoxQty")))).setScale(2,BigDecimal.ROUND_DOWN);
    					if(!"0".equals(String.valueOf(map.get("BoxQty"))) && map.get("BoxQty") !=null){
    						sdata.put("BoxQty",String.valueOf(map.get("BoxQty")));
    					 } 
    				    if(map.get("x_"+String.valueOf(smap.get("No"))) !=null && !"null".equalsIgnoreCase(String.valueOf(map.get("x_"+String.valueOf(smap.get("No"))))) && !"".equals(String.valueOf(map.get("x_"+String.valueOf(smap.get("No"))))))	 
    					  {	 
    					  sdata.put("Quantity",Integer.parseInt(String.valueOf(map.get("x_"+String.valueOf(smap.get("No")))))*direction);
    					  }    					
    					 }else{
    					 sdata.put("Quantity",""); 
    					 }
    					 
    				 }else{
    					 if(!"0".equals(String.valueOf(map.get("x_"+String.valueOf(smap.get("No"))))) && map.get("x_"+String.valueOf(smap.get("No"))) !=null && !"".equals(String.valueOf(map.get("x_"+String.valueOf(smap.get("No"))))))
    				     {
    						 sdata.put("Quantity",Integer.parseInt(String.valueOf(map.get("x_"+String.valueOf(smap.get("No")))))*direction);
    				     }else {
    				    	 sdata.put("Quantity",""); 
    				     }
    				 }
    				 System.out.println("get尺码数量："+String.valueOf(sdata.get("Quantity")));
    				 
    				 if(!"".equals(String.valueOf(map.get("UnitPrice"))) && map.get("UnitPrice") !=null)//单价
    				 {
    					 sdata.put("UnitPrice", new BigDecimal(Double.valueOf(String.valueOf(map.get("UnitPrice")))).setScale(2,BigDecimal.ROUND_DOWN));
    				 }else{
    					 sdata.put("UnitPrice","");
    				 }
    				 
    				  String DiscountRate="10";
    				 if(!"".equals(String.valueOf(map.get("UnitPrice"))) && map.get("UnitPrice") !=null && sdata.get("Quantity") !=null && String.valueOf(sdata.get("Quantity"))!=null && !"".equals(String.valueOf(sdata.get("Quantity")))){
    				 
    					 if(map.get("DiscountRate") !=null && !"".equals(String.valueOf(map.get("DiscountRate")))){
    						 DiscountRate =String.valueOf(map.get("DiscountRate"));
    				     }	 
    				System.out.println("DiscountRate:"+DiscountRate);	 
    				
    				//因为上面 数量 已经乘以 direction 了，后面两个都是 正数,所以金额这里不能再乘以 direction .multiply(new BigDecimal(direction))
    				
    				  sdata.put("Amount",new BigDecimal(Double.valueOf(String.valueOf(map.get("UnitPrice")))).multiply(new BigDecimal(DiscountRate)).divide(new BigDecimal(10.0))
    				 .multiply(new BigDecimal(Double.valueOf(String.valueOf(sdata.get("Quantity"))))).setScale(2,BigDecimal.ROUND_DOWN));
    				  
    				  System.out.println("单价："+new BigDecimal(Double.valueOf(String.valueOf(map.get("UnitPrice")))));
    				  System.out.println("尺码中的数量："+ String.valueOf(sdata.get("Quantity")));
    				 System.out.println("尺码中的折扣："+ new BigDecimal(DiscountRate));
    				
    				  System.out.println("尺码中的金额："+new BigDecimal(Double.valueOf(String.valueOf(map.get("UnitPrice")))).multiply(new BigDecimal(DiscountRate)).divide(new BigDecimal(10.0))
    		 				 .multiply(new BigDecimal(Double.valueOf(String.valueOf(sdata.get("Quantity"))))).setScale(2,BigDecimal.ROUND_DOWN));
    				 }else{
    					 sdata.put("Amount",""); 
    				 } //精确到尺码的金额，为置空
    				 
    				 sizeData.add(sdata);
    			 }
    			 datamap.put("StockMoveDetailID", String.valueOf(map.get("StockMoveDetailID")));
    			 datamap.put("StockMoveID", String.valueOf(map.get("StockMoveID")));
    			 datamap.put("GoodsID", String.valueOf(map.get("GoodsID")));
    			 datamap.put("Code", String.valueOf(map.get("Code")));
    			 datamap.put("Name", String.valueOf(map.get("Name")));
    			 datamap.put("ColorTitle", "颜色");
    			 datamap.put("ColorID", String.valueOf(map.get("ColorID")));
    			 datamap.put("Color", String.valueOf(map.get("Color")));
    			 
    			 
    			 //09.01 加载入货品图片
   			 if(MyTools.isExists(String.valueOf(map.get("Code"))) !=null )
   			 {
   				 String path1 = req.getContextPath();//项目的名称 
   		            String basePath = req.getScheme()+"://"+req.getServerName()+":"+req.getServerPort()+"/";
   		            
   		          String  url=basePath+"images/"+MyTools.isExists(String.valueOf(map.get("Code")));
   		            
   				 datamap.put("img", url);	   
   			 }else{
   				 datamap.put("img", ""); 
   			 }
    			 
    			 
    			 if(!"".equals(String.valueOf(map.get("Discount"))) && map.get("Discount") !=null){
    			 datamap.put("Discount", new BigDecimal(String.valueOf(map.get("Discount"))).multiply(new BigDecimal(direction)).setScale(2,BigDecimal.ROUND_DOWN));
    			 }else{
    				 datamap.put("Discount","");	 
    			 }
    			 if(!"".equals(String.valueOf(map.get("DiscountRate"))) && map.get("DiscountRate") !=null){
    			 datamap.put("DiscountRate", new BigDecimal(String.valueOf(map.get("DiscountRate"))).setScale(2,BigDecimal.ROUND_DOWN));
    			 }else{
    			 datamap.put("DiscountRate","");
    			 }
    			 if(!"".equals(String.valueOf(map.get("Quantity"))) && map.get("Quantity") !=null){
    			 datamap.put("Quantity", Integer.valueOf(String.valueOf(map.get("Quantity"))).intValue()*direction); 
    			 }else{
    				 datamap.put("Quantity","");	 
    			 }
    			 
    			 if(!"".equals(String.valueOf(map.get("UnitPrice"))) && map.get("UnitPrice") !=null)//单价
   				 {
    				 datamap.put("UnitPrice", new BigDecimal(Double.valueOf(String.valueOf(map.get("UnitPrice")))).setScale(2,BigDecimal.ROUND_DOWN));
   				 }else{
   					 datamap.put("UnitPrice","");
   				 }
    			 
    			 
    			 if(!"".equals(String.valueOf(map.get("Amount"))) && map.get("Amount") !=null){
    			 datamap.put("Amount", new BigDecimal(String.valueOf(map.get("Amount"))).multiply(new BigDecimal(direction)).setScale(2,BigDecimal.ROUND_DOWN));
    			 }else{
    			 datamap.put("Amount", "");	 
    			 }
    			 
    			 datamap.put("sizetitle", sizetitle);
    			 datamap.put("sizeData", sizeData);
    			 
    			 List<Map<String,Object>> right =new ArrayList<>();
    			 Map<String,Object> m=new LinkedHashMap<>();	
    			/* for(int n=0;n<2 ;n++){
       			  Map<String,Object> m=new LinkedHashMap<>();	
       			  if(n==0){
       				 m.put("text", "审核");
       				 m.put("onPress", "function() {"+
                                          "      modal.toast({ "+
                                          "      message: '审核', "+
                                          "       duration: 0.3 "+
                                          "      }); "+
                                          " }"
                                          );
       			  }else if(n==1){
       				  m.put("text", "删除");
        				 m.put("onPress", "()=> {"+
                                           "      modal.toast({ "+
                                           "      message: '删除', "+
                                           "       duration: 0.3 "+
                                           "      }); "+
                                           " }"
                                           );  
        				 m.put("style", "{ backgroundColor: '#F4333C', color: 'white' }");
       				  
       			  }
       			  
       			  right.add(m);
       			 } */
       			m.put("text", "删除");
   				 m.put("onPress", "()=> {"+
                                  "      modal.toast({ "+
                                  "      message: '删除', "+
                                  "       duration: 0.3 "+
                                  "      }); "+
                                  " }"
                                  );  
   				 Map<String,Object> stylemap= new LinkedHashMap<>();
   				 stylemap.put("backgroundColor", "#F4333C");
   				 stylemap.put("color", "white");
   				 m.put("style",stylemap );
   				 right.add(m);
    			 datamap.put("right", right);
    			 list.add(datamap);
    		 }	  
    		 
    		 j.setMsg("成功返回数据");
    	  }else{
    		 j.setMsg("暂无数据"); 
    	  }	
    	  
             j.setSuccess(true);
             j.setObj(list);
    	  
    	}catch(Exception e){
    		  j.setSuccess(false);
              j.setMsg(e.getMessage());
              SysLogger.error(e.getMessage(), e);
    	}
    	return j;
    }
    
    //新增保存方法
    @RequestMapping(params = "saveStockMoveX")
    @ResponseBody
    public AjaxJson saveStockMoveX(HttpServletRequest req) {
        Client client = ResourceUtil.getClientFromSession(req);
        AjaxJson j = new AjaxJson();
        j.setAttributes(new HashMap<String, Object>());
        try {
            String warehouseInId = oConvertUtils.getString(req.getParameter("WarehouseInID"));
            String warehouseOutId = oConvertUtils.getString(req.getParameter("DepartmentID"));
            String stockMoveId = oConvertUtils.getString(req.getParameter("StockMoveID"));
            String employeeId = oConvertUtils.getString(req.getParameter("employeeId"));
            String brandId = oConvertUtils.getString(req.getParameter("brandId"));
            String memo = oConvertUtils.getString(req.getParameter("memo"));
            String MoveOutType =oConvertUtils.getString(req.getParameter("MoveOutType"));
            String MoveInType =oConvertUtils.getString(req.getParameter("MoveInType"));
            String notUseNegativeInventoryCheck = oConvertUtils.getString(req.getParameter("notUseNegativeInventoryCheck"));
            String jsonStr = oConvertUtils.getString(req.getParameter("data"));
            JSONArray datas = JSONArray.fromObject(jsonStr);
            
            
	        List<List<Map<String, Object>>> sizeDatalist =new ArrayList<>(); //sizeData 所有元素的所有货品的，不是单个的，
	        
	        for(int i=0;i<datas.size(); i++){
	            System.out.println("datas 没有删除键前 i串："+datas.get(i));
	            JSONObject json =datas.getJSONObject(i); //单个对象
	            JSONArray sizeData =json.getJSONArray("sizeData");
	            
	            List<Map<String, Object>> ls=JSONArray.toList(sizeData, Map.class);
	            sizeDatalist.add(ls);
	            json.remove("sizetitle");
	            json.remove("right");
	            json.remove("sizeData");
	            System.out.println("json第一项："+datas.get(i));
	          System.out.println("datas 删除键后 i串："+datas.get(i));
	          //dataList.add(datas.get(i));	
	          }  
	        List<Map<String, Object>> dataList = JSONArray.toList(datas, Map.class); //上面删除后，才能转  收货部门，默认取用户登录的所属部门
	        
	        for(int k=0;k<dataList.size();k++){                       //因不能一次转 所以要重新整理后台数据 
	        	Map<String, Object> map=dataList.get(k);
	        	 for(int m=0;m<sizeDatalist.size();m++){ //不知道 顺序是否对，不对再判断 
	        		if(String.valueOf(map.get("GoodsID")).equals(String.valueOf(sizeDatalist.get(m).get(0).get("GoodsID"))) && String.valueOf(map.get("ColorID")).equals(String.valueOf(sizeDatalist.get(m).get(0).get("ColorID")))){   //拿一个出来就可以
	        		 map.put("sizeData",sizeDatalist.get(m));
	        		}
	        	 } 	
	        }
	         
	        System.out.println("最终的dataList:"+dataList.toString());
            
         //   List<Map<String, Object>> dataList = JSONArray.toList(datas, Map.class);
            // 判断检查负库存
            String id = null;
            List<Map<String, Object>> tempList = new ArrayList<Map<String, Object>>();
          /*  if (!"true".equalsIgnoreCase(notUseNegativeInventoryCheck)) {
                boolean mustExistsGoodsFlag = Boolean.parseBoolean(String.valueOf(commonDao.getData(" select MustExistsGoodsFlag from Department where DepartmentID = ? ", warehouseOutId)));
                if (mustExistsGoodsFlag && !client.isSuperSalesFlag()) {
                    tempList = commonController.checkNegativeInventoryForBackStage(commonDao, dataList, client.getOnLineId(), client.getUserID(), warehouseOutId, 36, stockMoveId, 0, 2, 0, 0, 0, "");
                }
            }
            if (tempList.size() == 0) {
                // 保存单据
                id = stockMoveService.saveStockMove(warehouseInId, warehouseOutId, employeeId, stockMoveId, memo, dataList, brandId, client);
            } */
            
            id=stockMoveService.saveStockMoveX(warehouseInId, warehouseOutId, employeeId, stockMoveId, MoveOutType, MoveInType, memo, dataList, brandId, client);
            
            j.getAttributes().put("StockMoveID", id);
            j.getAttributes().put("tempList", tempList);
        } catch (Exception e) {
            j.setSuccess(false);
            j.setMsg(e.getMessage());
            SysLogger.error(e.getMessage(), e);
        }
        return j;
    }
    
}
