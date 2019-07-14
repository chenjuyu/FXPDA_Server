package com.fuxi.web.controller;

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
import com.fuxi.core.common.service.StockService;
import com.fuxi.system.util.Client;
import com.fuxi.system.util.DataUtils;
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

}
