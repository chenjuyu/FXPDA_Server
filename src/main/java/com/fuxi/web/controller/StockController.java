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
import com.fuxi.core.common.service.StockService;
import com.fuxi.system.util.Client;
import com.fuxi.system.util.DataUtils;
import com.fuxi.system.util.MyTools;
import com.fuxi.system.util.ResourceUtil;
import com.fuxi.system.util.SysLogger;
import com.fuxi.system.util.oConvertUtils;

/**
 * Title: StockMoveInOutController Description: 进仓单,出仓单逻辑控制器
 * 
 * @author LYJ
 * 
 */
@Controller
@RequestMapping("/stock")
public class StockController extends BaseController {

    private Logger log = Logger.getLogger(StockController.class);
    private SelectController controller = new SelectController();

    private CommonController commonController =new CommonController();
    
    
    @Autowired
    private CommonDao commonDao;
    @Autowired
    private StockService stockService;

    /**
     * 根据筛选条件获取进仓单列表[主表信息]
     * 
     * @param user
     * @param req
     * @return
     */
    @RequestMapping(params = "getStockIn")
    @ResponseBody
    public AjaxJson getStockMoveIn(HttpServletRequest req) {
        Client client = ResourceUtil.getClientFromSession(req);
        AjaxJson j = new AjaxJson();
        j.setAttributes(new HashMap<String, Object>());
        Map<String, String> map = client.getMap();
        String userRight = map.get(client.getUserID());
        try {
            int page = oConvertUtils.getInt(req.getParameter("currPage"));
            String audit = oConvertUtils.getString(req.getParameter("audit"));
            String type = oConvertUtils.getString(req.getParameter("type"));
            String warehouseId = oConvertUtils.getString(req.getParameter("warehouseId"));
            StringBuffer sb = new StringBuffer();
            sb.append(
                    " select st.stockId, de.Department, No, CONVERT(varchar(100), Date, 111) Date,isnull(QuantitySum,0) QuantitySum," + " RelationAmountSum, AuditFlag,(select Name from Employee where employeeId = st.EmployeeId) Employee,"
                            + "isnull(st.Memo,'') Memo,(select Department from Department where DepartmentId = st.RelationWarehouseID) RelationWarehouse, " + "(select Brand from Brand where BrandId = st.BrandID) Brand,st.RelationNo from stock st  ")
                    .append(" left join Department de on de.DepartmentID = st.DepartmentID where de.DepartmentID in (").append(userRight).append(")   ");
            // 按条件查询
            if (null != audit && "0".equals(audit)) {
                // 未审核
                sb.append(" and AuditFlag = '0' ");
            } else if (null != audit && "1".equals(audit)) {
                // 已审核
                sb.append(" and AuditFlag = '1' ");
            }
            // 仓库条件
            if (warehouseId != null && !warehouseId.trim().isEmpty() && !"null".equalsIgnoreCase(warehouseId)) {
                sb.append(" and st.DepartmentID = '" + warehouseId + "' ");
            }
            // 类别条件
            if (!"全部".equals(type) && type != null && !type.trim().isEmpty() && !"type".equalsIgnoreCase(type)) {
                sb.append(" and type = '" + type + "' ");
            }
            sb.append(" and direction='1' order by madebydate desc,No desc ");
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
     * 根据筛选条件获取出仓单列表[主表信息]
     * 
     * @param user
     * @param req
     * @return
     */
    @RequestMapping(params = "getStockOut")
    @ResponseBody
    public AjaxJson getStockMoveOut(HttpServletRequest req) {
        Client client = ResourceUtil.getClientFromSession(req);
        AjaxJson j = new AjaxJson();
        j.setAttributes(new HashMap<String, Object>());
        Map<String, String> map = client.getMap();
        String userRight = map.get(client.getUserID());
        try {
            int page = oConvertUtils.getInt(req.getParameter("currPage"));
            String audit = oConvertUtils.getString(req.getParameter("audit"));
            String type = oConvertUtils.getString(req.getParameter("type"));
            String warehouseId = oConvertUtils.getString(req.getParameter("warehouseId"));
            StringBuffer sb = new StringBuffer();
            sb.append(
                    " select st.stockId, de.Department, No, CONVERT(varchar(100), Date, 111) Date,isnull(QuantitySum,0) QuantitySum," + " RelationAmountSum, AuditFlag,(select Name from Employee where employeeId = st.EmployeeId) Employee,"
                            + "isnull(st.Memo,'') Memo, (select Department from Department where DepartmentId = st.RelationWarehouseID) RelationWarehouse, " + "(select Brand from Brand where BrandId = st.BrandID) Brand,st.RelationNo from stock st  ")
                    .append(" left join Department de on de.DepartmentID = st.DepartmentID where de.DepartmentID in (").append(userRight).append(")   ");
            // 按条件查询
            if (null != audit && "0".equals(audit)) {
                // 未审核
                sb.append(" and AuditFlag = '0' ");
            } else if (null != audit && "1".equals(audit)) {
                // 已审核
                sb.append(" and AuditFlag = '1' ");
            }
            // 仓库条件
            if (warehouseId != null && !warehouseId.trim().isEmpty() && !"null".equalsIgnoreCase(warehouseId)) {
                sb.append(" and st.DepartmentID = '" + warehouseId + "' ");
            }
            // 类别条件
            if (!"全部".equals(type) && type != null && !type.trim().isEmpty() && !"type".equalsIgnoreCase(type)) {
                sb.append(" and type = '" + type + "' ");
            }
            sb.append(" and direction='-1' order by madebydate desc,No desc ");
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
     * 根据进仓单/出仓单ID获取进仓单明细信息[子表信息]
     * 
     * @param req
     * @return
     */
    @RequestMapping(params = "stockInEdit")
    @ResponseBody
    public AjaxJson stockMoveInEdit(HttpServletRequest req) {
        AjaxJson j = new AjaxJson();
        j.setAttributes(new HashMap<String, Object>());
        Client client = ResourceUtil.getClientFromSession(req);
        try {
            String stockId = oConvertUtils.getString(req.getParameter("stockId"));
            int boxQtySum = Integer.parseInt(String.valueOf(commonDao.getData("select isnull(sum(isnull(sodt.BoxQty,0)),0) BoxQty from stockDetailtemp sodt where stockId = ? ", stockId)));
            int sizeStrCount = Integer.parseInt(String.valueOf(commonDao.getData("select count(1) from stockDetailtemp sodt where stockId = ? ", stockId)));
            int notNullSizeStrCount = Integer.parseInt(String.valueOf(commonDao.getData("select count(1) from stockDetailtemp sodt where stockId = ? and sodt.SizeStr is not null and sodt.SizeStr <> '' ", stockId)));
            StringBuffer sb = new StringBuffer();
            sb.append("  select de.No,RelationNo,RelationWarehouseID, WarehouseID ,isnull(de.AuditFlag,0)  AuditFlag,").append(boxQtySum)
                    .append(" BoxQtySum,( select isnull(Department,'') from Department where DepartmentId = WarehouseID) Warehouse,( select isnull(Department,'') from Department where DepartmentId = RelationWarehouseID) RelationWarehouse,")
                    .append(" isnull(de.Memo,'') Memo,de.EmployeeID,(select Name from Employee where EmployeeID = de.EmployeeID) Employee,de.BrandID,(select Brand from Brand where brandId = de.BrandId) Brand ,isnull(de.QuantitySum,0) QuantitySum from stock de  ").append(" where stockId = '")
                    .append(stockId).append("'");
            List list = commonDao.findForJdbc(sb.toString());
            if (list.size() > 0) {
                Map map = (Map) list.get(0);
                j.setAttributes(map);
                sb = new StringBuffer();
                List detailList = null;
                if (sizeStrCount == notNullSizeStrCount) {
                    sb.append(" select a.*,g.code GoodsCode ,g.name GoodsName,c.No ColorCode,isnull(pdt.BoxQty,0) BoxQty,pdt.Quantity QuantitySum,a.SizeStr,isnull(pdt.RelationUnitPrice,0) DiscountPrice,isnull(pdt.UnitPrice,0) UnitPrice, ")
                            .append(" c.Color ,sg.SizeGroupID,isnull(pdt.RetailSales,0) RetailSales from GoodsBoxBarcode a join goods g on a.goodsid=g.goodsid ").append(" join color c on a.colorid=c.colorid ").append(" join GoodsType gt on gt.GoodsTypeID = g.GoodsTypeID ")
                            .append(" join SizeGroup sg on sg.SizeGroupID =gt.SizeGroupID ").append(" join stockDetailtemp pdt on pdt.goodsId = a.goodsId and pdt.colorId = a.colorid and pdt.sizeStr = a.sizeStr ").append("  where pdt.stockId = '").append(stockId).append("' order by pdt.GoodsID ");
                    detailList = controller.getDetailTemp(commonDao.findForJdbc(sb.toString()), client, commonDao);
                } else {
                    sb.append(" select detail.GoodsID,g.Name GoodsName,c.No ColorCode,s.No SizeCode,detail.ColorID,c.Color, ")
                            .append(" detail.SizeID,s.Size,detail.Quantity,sodt.Quantity QuantitySum, '' Barcode, g.Code GoodsCode, isnull(sodt.RelationUnitPrice,0) DiscountPrice,isnull(sodt.RetailSales,0) RetailSales,isnull(sodt.UnitPrice,0) UnitPrice, ")
                            .append(" isnull(sodt.BoxQty,0) BoxQty,isnull(sodt.Quantity/nullif(sodt.BoxQty,0),0) OneBoxQty, sodt.IndexNo,sodt.SizeStr from stockDetail detail ").append(" left join Goods g on g.GoodsID = detail.GoodsID ").append(" left join Color c on c.ColorID = detail.ColorID ")
                            .append(" left join Size s on s.SizeID = detail.SizeID ").append(" left join stockDetailtemp sodt on sodt.stockId = detail.stockId and sodt.GoodsID = detail.GoodsID and sodt.ColorID = detail.ColorID ").append("  where sodt.stockId = '").append(stockId).append("'");
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
     * 修改出仓单/进仓单明细记录
     * 
     * @param req
     * @return
     */
    @RequestMapping(params = "addStockIn")
    @ResponseBody
    public AjaxJson addStockMoveIn(HttpServletRequest req) {
        Client client = ResourceUtil.getClientFromSession(req);
        AjaxJson j = new AjaxJson();
        j.setAttributes(new HashMap<String, Object>());
        try {
            String stockId = oConvertUtils.getString(req.getParameter("stockId"));
            String employeeId = oConvertUtils.getString(req.getParameter("employeeId"));
            String brandId = oConvertUtils.getString(req.getParameter("brandId"));
            String memo = oConvertUtils.getString(req.getParameter("memo"));
            String jsonStr = oConvertUtils.getString(req.getParameter("data"));
            JSONArray datas = JSONArray.fromObject(jsonStr);
            List<Map<String, Object>> dataList = JSONArray.toList(datas, Map.class);
            String id = stockService.saveStockMoveIn(employeeId, stockId, memo, dataList, brandId, client);
            j.getAttributes().put("stockId", id);
        } catch (Exception e) {
            j.setSuccess(false);
            j.setMsg(e.getMessage());
            SysLogger.error(e.getMessage(), e);
        }
        return j;
    }

    /**
     * 修改出仓单/进仓单明细的数量
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
            String stockId = oConvertUtils.getString(req.getParameter("stockId"));
            String jsonStr = oConvertUtils.getString(req.getParameter("data"));
            JSONArray datas = JSONArray.fromObject(jsonStr);
            List<Map<String, Object>> dataList = JSONArray.toList(datas, Map.class);
            stockService.deleteStockMoveInDetail(dataList, stockId);
        } catch (Exception e) {
            j.setSuccess(false);
            j.setMsg(e.getMessage());
            SysLogger.error(e.getMessage(), e);
        }
        return j;
    }

    /**
     * 审核出仓单/进仓单
     * 
     * @param user
     * @param req
     * @return
     */
    @RequestMapping(params = "auditStockIn")
    @ResponseBody
    public AjaxJson auditStockMoveIn(HttpServletRequest req) {
        int result = 0;
        AjaxJson j = new AjaxJson();
        j.setAttributes(new HashMap<String, Object>());
        try {
            String stockId = oConvertUtils.getString(req.getParameter("stockId"));
            String user = ((Client) ResourceUtil.getClientFromSession(req)).getUserName();
            // commonDao.auditStockMove(stockId, 1, user, 1);
            StringBuffer sql = new StringBuffer();
            sql.append(" update stock set Audit = ? , AuditDate = ? , AuditFlag = ? where stockId = ? ; ");
            result = commonDao.executeSql(sql.toString(), user, DataUtils.gettimestamp(), true, stockId);
            j.getAttributes().put("result", result);
        } catch (Exception e) {
            j.setSuccess(false);
            j.setMsg(e.getMessage());
            SysLogger.error(e.getMessage(), e);
        }
        return j;
    }

    /**
     * 出仓单/进仓单条码校验后,以校验结果覆盖原始单据[新增,修改,删除]
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
            String stockId = oConvertUtils.getString(req.getParameter("stockId"));
            String jsonStr = oConvertUtils.getString(req.getParameter("data"));
            JSONArray datas = JSONArray.fromObject(jsonStr);
            List<Map<String, Object>> dataList = JSONArray.toList(datas, Map.class);
            int count = stockService.coverSave(stockId, dataList, client);
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
            String stockId = oConvertUtils.getString(req.getParameter("stockId"));
            String memo = oConvertUtils.getString(req.getParameter("memo"));
            StringBuilder sb = new StringBuilder();
            sb.append(" update stock set memo = ? where stockId = ? ");
            commonDao.executeSql(sb.toString(), memo, stockId);
        } catch (Exception e) {
            j.setSuccess(false);
            j.setMsg(e.getMessage());
            SysLogger.error(e.getMessage(), e);
        }
        return j;
    }
    
    
    /*
     * 进出 单主表信息，新写 ，退货也应该在这里处理了 这个退货的为变成正数
     * */
    @RequestMapping(params = "stocklist")
    @ResponseBody  
    public AjaxJson stocklist(HttpServletRequest req){
    	 Client client = ResourceUtil.getClientFromSession(req);
         AjaxJson j = new AjaxJson();
         j.setAttributes(new HashMap<String, Object>());
         Map<String, String> map = client.getMap();
         String userRight = map.get(client.getUserID());
         try{
        	  int page = oConvertUtils.getInt(req.getParameter("currPage"));
              String audit = oConvertUtils.getString(req.getParameter("audit"));
              String no = oConvertUtils.getString(req.getParameter("no"));
              String beginDate = oConvertUtils.getString(req.getParameter("beginDate"));
              String endDate = oConvertUtils.getString(req.getParameter("endDate"));
              String departmentId = oConvertUtils.getString(req.getParameter("departmentId"));
              String supplierId = oConvertUtils.getString(req.getParameter("supplierId"));
              String employeeId = oConvertUtils.getString(req.getParameter("employeeId"));
              String customerId =    oConvertUtils.getString(req.getParameter("customerId")); //出仓单客户
              int direction =Integer.parseInt(oConvertUtils.getString(req.getParameter("direction")));//代表收，退 *so.direction 进出都是以正数写入单据，所以不用这个
              
              StringBuffer sb = new StringBuffer();
              sb.append(" select so.StockID,so.SupplierID,so.CustomerID,so.WarehouseID,so.TallyFlag,so.DepartmentID, de.Department ,so.Type, No, CONVERT(varchar(100), Date, 111) Date,isnull(QuantitySum,0) QuantitySum,").append("(AmountSum) AmountSum,so.OtherAmount,so.RelationAmountSum,AuditFlag,so.MadeBy,Convert(varchar(19),so.MadeByDate,121) MadeByDate,isnull((select Supplier from Supplier s where so.SupplierId = s.SupplierId),'') Supplier,isnull((select Customer from Customer c where so.CustomerId = c.CustomerId),'') Customer,")
                      .append("(select Name from Employee where employeeId = so.EmployeeId) Employee,isnull(so.Memo,'') Memo,").append("(select Brand from Brand where BrandId = so.BrandId) Brand from Stock so  ")
                      .append(" left join Department de on de.DepartmentID = so.DepartmentID where so.DepartmentID in (").append(userRight).append(") and direction ="+direction+"  ");
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
                 // sb.append(" and Date between convert(datetime,'" + beginDate + "', 120) and convert(datetime,'" + endDate + "', 120) ");
            	  sb.append(" and so.Date >= '" + beginDate + "' and so.Date <='" + endDate + " 23:59:59.997'");
              }
              // 部门
              if (departmentId != null && !"".equals(departmentId.trim()) && !"null".equalsIgnoreCase(departmentId)) {
                  sb.append(" and so.departmentId = '" + departmentId + "' ");
              }
              // 客户
              if (supplierId != null && !"".equals(supplierId.trim()) && !"null".equalsIgnoreCase(supplierId)) {
                  sb.append(" and so.supplierId = '" + supplierId + "' ");
              }
              
              if (customerId != null && !"".equals(customerId.trim()) && !"null".equalsIgnoreCase(customerId)) {
                  sb.append(" and so.customerId = '" + customerId + "' ");
              }
              
              // 经手人
              if (employeeId != null && !"".equals(employeeId.trim()) && !"null".equalsIgnoreCase(employeeId)) {
                  sb.append(" and so.employeeId = '" + employeeId + "' ");
              }
              sb.append(" order by so.madebydate desc,No desc ");
             
              List<Map<String,Object>> list = commonDao.findForJdbc(sb.toString(), page, 15);
              
              for(int i=0;i<list.size();i++){
            	  Map<String,Object> m=list.get(i);
            	  if(!"".equals(String.valueOf(m.get("AmountSum"))) && m.get("AmountSum") !=null){
            		  m.put("AmountSum", new BigDecimal(String.valueOf(m.get("AmountSum"))).setScale(2,BigDecimal.ROUND_DOWN)); 
            	  }else{
            		  m.put("AmountSum", ""); 
            	  }
            	  
            	  if(!"".equals(String.valueOf(m.get("RelationAmountSum"))) && m.get("RelationAmountSum") !=null){
            		  m.put("RelationAmountSum", new BigDecimal(String.valueOf(m.get("RelationAmountSum"))).setScale(2,BigDecimal.ROUND_DOWN)); 
            	  }else{
            		  m.put("RelationAmountSum", ""); 
            	  }
            	  List<Map<String,Object>> right=new ArrayList<>();
            	  
            	  for(int n=0;n<2;n++){
            	  Map<String,Object> rmap=new LinkedHashMap<>();
            	  Map<String,Object> stylemap=new LinkedHashMap<>();
            	/*  if(n==0){
                	  rmap.put("text", "删除");
                	  stylemap.put("backgroundColor", "orange");
                	  stylemap.put("color", "white");
                	  rmap.put("style", stylemap);
                 }else */ if(n==0){
            	  rmap.put("text", "审核");
            	  stylemap.put("backgroundColor", "mediumspringgreen");
            	  stylemap.put("color", "white");
            	  rmap.put("style", stylemap);
            	  
            	  }else if(n==1){
            	  rmap.put("text", "反审");
            	  stylemap.put("backgroundColor", "#F4333C");
            	  stylemap.put("color", "white");
            	  rmap.put("style", stylemap);
            	  }
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
        	 
         }catch(Exception e){
        	  j.setSuccess(false);
              j.setMsg(e.getMessage());
              SysLogger.error(e.getMessage(), e);	 
         }
    	return j;
    }
    
    /*
  * 根据 stockid 显示 单据详情，显示横向尺码  新写
  * 
  * */
 @RequestMapping(params = "stockEditX")
 @ResponseBody
 
 public AjaxJson stockEditX(HttpServletRequest req){
     AjaxJson j =new AjaxJson();
     j.setAttributes(new HashMap<String, Object>());
     Client client = ResourceUtil.getClientFromSession(req);
     List<Map<String, Object>> list =new ArrayList<>();
 	try{
 		String StockID = oConvertUtils.getString(req.getParameter("StockID"));
 		int direction=Integer.parseInt(oConvertUtils.getString(req.getParameter("direction"))); //退货时，要按-1让显示 变成正数
 		
 		System.out.print("direction:"+direction);
 		
 		StringBuffer sb = new StringBuffer();
 	  sb.append("Select a.*,b.Code,b.SupplierCode, b.Name,b.Model,b.Unit,c.Color,b.GroupID,b.GroupNo,"+
 		"b.StopFlag,b.age,b.Season,br.brand,b.RetailSales1,b.RetailSales2,b.PurchasePrice,bs.Serial,st.Storage "+
 		"from StockDetailTemp a join Goods b on a.GoodsID = b.GoodsID "+
 		"join Color c on a.ColorID=c.ColorID left join brand br on b.brandid = br.brandid "+
 		"left join BrandSerial bs on b.BrandSerialID = bs.BrandSerialID "+
 		"left join storage st on a.StorageID = st.StorageID "+
 		"where a.StockID= '"+StockID+"'" + 
 		"order by a.StockID, a.IndexNo, b.Code,c.No");	
 	  System.out.println("sql查询语句："+sb.toString());
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
 				 sdata.put("StockDetailID", String.valueOf(map.get("StockDetailID")));
 				 sdata.put("StockID", String.valueOf(map.get("StockID")));
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
 					  sdata.put("Quantity",Integer.parseInt(String.valueOf(map.get("x_"+String.valueOf(smap.get("No"))))));
 					  }    					
 					 }else{
 					 sdata.put("Quantity",""); 
 					 }
 					 
 				 }else{
 					 if(!"0".equals(String.valueOf(map.get("x_"+String.valueOf(smap.get("No"))))) && map.get("x_"+String.valueOf(smap.get("No"))) !=null && !"".equals(String.valueOf(map.get("x_"+String.valueOf(smap.get("No"))))))
 				     {
 						 sdata.put("Quantity",Integer.parseInt(String.valueOf(map.get("x_"+String.valueOf(smap.get("No"))))));
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
 				 if(!"".equals(String.valueOf(map.get("UnitPrice"))) && map.get("UnitPrice") !=null && sdata.get("Quantity") !=null 
 						 && String.valueOf(sdata.get("Quantity"))!=null && !"".equals(String.valueOf(sdata.get("Quantity")))){
 				 
 					 if(map.get("DiscountRate") !=null && !"".equals(String.valueOf(map.get("DiscountRate"))) && new BigDecimal(String.valueOf(map.get("DiscountRate"))).compareTo(BigDecimal.ZERO) !=0){
 						 DiscountRate =String.valueOf(map.get("DiscountRate"));
 				     }	 
 				System.out.println("DiscountRate:"+DiscountRate);	 
 				
 				//因为上面 数量 已经乘以 direction 了，后面两个都是 正数,所以金额这里不能再乘以 direction .multiply(new BigDecimal(direction))
 				
 				  sdata.put("Amount",new BigDecimal(Double.valueOf(String.valueOf(map.get("UnitPrice"))))
 				 .multiply(new BigDecimal(Double.valueOf(String.valueOf(sdata.get("Quantity"))))).setScale(2,BigDecimal.ROUND_DOWN));
 				  
 			
 				  
 				  System.out.println("单价："+new BigDecimal(Double.valueOf(String.valueOf(map.get("UnitPrice")))));
 				  System.out.println("尺码中的数量："+ String.valueOf(sdata.get("Quantity")));
 				 System.out.println("尺码中的折扣："+ new BigDecimal(DiscountRate));
 				 
 				 
 				 
 				 
 				
 				  System.out.println("尺码中的金额："+new BigDecimal(Double.valueOf(String.valueOf(map.get("UnitPrice"))))
 		 				 .multiply(new BigDecimal(Double.valueOf(String.valueOf(sdata.get("Quantity"))))).setScale(2,BigDecimal.ROUND_DOWN));
 				 }else{
 					 sdata.put("Amount",""); 
 				 } //精确到尺码的金额，为置空
 				 
 				 //结算金额
 				 if(!"".equals(String.valueOf(map.get("RelationUnitPrice"))) && map.get("RelationUnitPrice") !=null )//单价
 				 {
 					sdata.put("RelationUnitPrice",new BigDecimal(Double.valueOf(String.valueOf(map.get("RelationUnitPrice")))).setScale(2,BigDecimal.ROUND_DOWN));
 					
 				if(sdata.get("Quantity") !=null 
 						 && String.valueOf(sdata.get("Quantity"))!=null && !"".equals(String.valueOf(sdata.get("Quantity"))) && new BigDecimal(String.valueOf(sdata.get("Quantity"))).compareTo(BigDecimal.ZERO) !=0)
 				{
 				 sdata.put("RelationAmount",new BigDecimal(Double.valueOf(String.valueOf(map.get("RelationUnitPrice")))).multiply(new BigDecimal(String.valueOf(sdata.get("Quantity"))))
 				  .multiply(new BigDecimal(DiscountRate)).divide(new BigDecimal(10.0)).setScale(2,BigDecimal.ROUND_DOWN));
 				}else{
 					sdata.put("RelationAmount","");
 				}
 				 
 				 }else{
 					sdata.put("RelationAmount","");
 					sdata.put("RelationUnitPrice","");
 				 }
 				 
 				 
 				 sizeData.add(sdata);
 			 }
 			 datamap.put("StockDetailID", String.valueOf(map.get("StockDetailID")));
 			 datamap.put("StockID", String.valueOf(map.get("StockID")));
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
 			 
 			 /*
 			 if(!"".equals(String.valueOf(map.get("Discount"))) && map.get("Discount") !=null){
 			 datamap.put("Discount", new BigDecimal(String.valueOf(map.get("Discount"))).setScale(2,BigDecimal.ROUND_DOWN));
 			 }else{
 				 datamap.put("Discount","");	 
 			 } */
 			 if(!"".equals(String.valueOf(map.get("DiscountRate"))) && map.get("DiscountRate") !=null){
 			 datamap.put("DiscountRate", new BigDecimal(String.valueOf(map.get("DiscountRate"))).setScale(2,BigDecimal.ROUND_DOWN));
 			 }else{
 			 datamap.put("DiscountRate","");
 			 }
 			 if(!"".equals(String.valueOf(map.get("Quantity"))) && map.get("Quantity") !=null){
 			 datamap.put("Quantity", Integer.valueOf(String.valueOf(map.get("Quantity"))).intValue()); 
 			 }else{
 				 datamap.put("Quantity","");	 
 			 }
 			 
 			    if(!"".equals(String.valueOf(map.get("UnitPrice"))) && map.get("UnitPrice") !=null)//单价 这里代表成本价了
				 {
 				 datamap.put("UnitPrice", new BigDecimal(Double.valueOf(String.valueOf(map.get("UnitPrice")))).setScale(2,BigDecimal.ROUND_DOWN));
				 }else{
					 datamap.put("UnitPrice","");
				 }
 			
 			    //这个是结算价 RelationUnitPrice
 			    
 			   if(!"".equals(String.valueOf(map.get("RelationUnitPrice"))) && map.get("RelationUnitPrice") !=null)//单价 这里代表结算价
				 {
				 datamap.put("RelationUnitPrice", new BigDecimal(Double.valueOf(String.valueOf(map.get("RelationUnitPrice")))).setScale(2,BigDecimal.ROUND_DOWN));
				 }else{
					 datamap.put("RelationUnitPrice","");
				 }
 			   
 			  if(!"".equals(String.valueOf(map.get("RelationAmount"))) && map.get("RelationAmount") !=null)//单价 这里代表结算价
				 {
				 datamap.put("RelationAmount", new BigDecimal(Double.valueOf(String.valueOf(map.get("RelationAmount")))).setScale(2,BigDecimal.ROUND_DOWN));
				 }else{
					 datamap.put("RelationAmount","");
				 }
			    
 			   
 			   
 			   
 			    
 			 
 			 if(!"".equals(String.valueOf(map.get("Amount"))) && map.get("Amount") !=null){
 			 datamap.put("Amount", new BigDecimal(String.valueOf(map.get("Amount"))).setScale(2,BigDecimal.ROUND_DOWN));
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
 
 //新的保存方法 2019-09-09
 
 @RequestMapping(params = "saveStockX")
 @ResponseBody
 public AjaxJson savePurchaseX(HttpServletRequest req){
 	 Client client = ResourceUtil.getClientFromSession(req);
      AjaxJson j = new AjaxJson();
      j.setAttributes(new HashMap<String, Object>());
      try{
     String StockId=null;	 
     String StockID = oConvertUtils.getString(req.getParameter("StockID"));
     String supplierid = oConvertUtils.getString(req.getParameter("supplierid"));
     String departmentid = oConvertUtils.getString(req.getParameter("departmentid"));
     String employeeId = oConvertUtils.getString(req.getParameter("employeeId"));
     String brandId = oConvertUtils.getString(req.getParameter("brandId"));
     String businessDeptId = oConvertUtils.getString(req.getParameter("businessDeptId"));
     String memo = oConvertUtils.getString(req.getParameter("memo"));
     String type = oConvertUtils.getString(req.getParameter("type"));
     String direction = oConvertUtils.getString(req.getParameter("direction"));
     String typeEName = oConvertUtils.getString(req.getParameter("typeEName"));
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
     
     
     
     List<Map<String, Object>> tmpList = new ArrayList<Map<String, Object>>();
     List<Map<String, Object>> tempList = new ArrayList<Map<String, Object>>();
     
     List<Map<String, Object>> tdatas = new ArrayList<Map<String, Object>>();
     List<String> supplierIds = new ArrayList<String>();
     
     
     tdatas.addAll(dataList);
    // if("".equals(supplierid) || supplierid==null){//如果为空，从货品明细取的，不是表头  ，类别为其它时仓时，这个是可以为空的
    //	 supplierid =null;
    // 	
     //}  
         // 采购退货单检查负库存 修改单据
         if (Integer.parseInt(direction) == -1) {
             boolean mustExistsGoodsFlag = Boolean.parseBoolean(String.valueOf(commonDao.getData(" select MustExistsGoodsFlag from Department where DepartmentID = ? ", departmentid)));
             if (mustExistsGoodsFlag && !client.isSuperSalesFlag()) {
                 if (StockID == null || "".equals(StockID) || "null".equalsIgnoreCase(StockID)) {
                     tmpList = commonController.checkNegativeInventoryForBackStage(commonDao, dataList, client.getOnLineId(), client.getUserID(), departmentid, 32, StockID, 0, 2, 0, 0, 0, "");
                 } else {
                     tmpList = commonController.checkNegativeInventoryForBackStage(commonDao, dataList, client.getOnLineId(), client.getUserID(), departmentid, 32, StockID, 0, 2, 1, 0, 0, "");
                 }
             }
         }
         if (tmpList.size() == 0) {
             // 保存单据
        	 StockId = stockService.saveStockX(direction, dataList, StockID, supplierid, departmentid, employeeId, businessDeptId, memo, type, typeEName, brandId, client);
         }
     
     j.getAttributes().put("StockID", StockId);
     j.getAttributes().put("tempList", tmpList);
     j.setSuccess(true);
     j.setMsg("保存成功");
     
      }catch(Exception e){
     	 
     	 j.setSuccess(false);
     	 j.setMsg(e.getMessage());
     	 SysLogger.error(e.getMessage(), e);
     	 
      }
      
      return j;
 }
 

 @RequestMapping(params = "auditOrder")
 @ResponseBody 
 
 public AjaxJson auditOrder(HttpServletRequest req){
	 Client client = ResourceUtil.getClientFromSession(req);
     AjaxJson j = new AjaxJson();
     j.setAttributes(new HashMap<String, Object>());
     
     try{
    	 int AuditFlag =oConvertUtils.getInt(req.getParameter("AuditFlag"));
    	 String StockID =oConvertUtils.getString(req.getParameter("StockID"));
    	 
    	 StringBuilder sb = new StringBuilder();
         if(AuditFlag==1){
         sb.append(" Update Stock set AuditFlag = 1, AuditDate = getdate(),Audit='"+client.getUserName()+"' ").append(" where StockID = '").append(StockID).append("' ; ");
         }else{
         sb.append("Update Stock Set Audit=Null,AuditFlag=0,AuditDate=Null Where StockID='"+StockID+"'");	
         }
         commonDao.executeSql(sb.toString());
         j.setSuccess(true);
    	 
    	 
     }catch(Exception e){
     	 
     	 j.setSuccess(false);
     	 j.setMsg(e.getMessage());
     	 SysLogger.error(e.getMessage(), e);
     	 
      }
     return j;
 }
 
 

}
