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
import com.fuxi.core.common.service.StockMoveService;
import com.fuxi.system.util.Client;
import com.fuxi.system.util.DataUtils;
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
            String stockMoveId = oConvertUtils.getString(req.getParameter("stockMoveId"));
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

}
