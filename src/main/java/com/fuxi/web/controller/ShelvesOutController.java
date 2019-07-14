package com.fuxi.web.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import com.fuxi.core.common.dao.impl.CommonDao;
import com.fuxi.core.common.exception.BusinessException;
import com.fuxi.core.common.model.json.AjaxJson;
import com.fuxi.core.common.service.ShelvesOutService;
import com.fuxi.system.util.Client;
import com.fuxi.system.util.ResourceUtil;
import com.fuxi.system.util.SysLogger;
import com.fuxi.system.util.oConvertUtils;

/**
 * Title: ShelvesOutController Description: 仓位下架逻辑控制器
 * 
 * @author LYJ
 * 
 */
@Controller
@RequestMapping("/shelvesOut")
public class ShelvesOutController extends BaseController {

    private Logger log = Logger.getLogger(ShelvesOutController.class);

    @Autowired
    private CommonDao commonDao;
    @Autowired
    private ShelvesOutService shelvesOutService;

    /**
     * 根据筛选条件获取出仓单列表
     * 
     * @param req
     * @return
     */
    @RequestMapping(params = "getStockOut")
    @ResponseBody
    public AjaxJson getStock(HttpServletRequest req) {
        Client client = ResourceUtil.getClientFromSession(req);
        AjaxJson j = new AjaxJson();
        j.setAttributes(new HashMap<String, Object>());
        Map<String, String> map = client.getMap();
        String userRight = map.get(client.getUserID());
        try {
            int page = oConvertUtils.getInt(req.getParameter("currPage"));
            String type = oConvertUtils.getString(req.getParameter("type"));
            String docType = oConvertUtils.getString(req.getParameter("docType"));
            String warehouseId = oConvertUtils.getString(req.getParameter("warehouseId"));
            StringBuffer sb = new StringBuffer();
            sb.append(" select so.StockID, de.Department ,so.DepartmentID, No, CONVERT(varchar(100), Date, 111) Date,abs(isnull(QuantitySum,0)) QuantitySum,RelationAmountSum,so.madebydate  from stock so  ")
                    .append(" join Department de on de.DepartmentID = so.DepartmentID where datediff(d,so.date,getdate()) < 8 and so.DepartmentID in (").append(userRight).append(") and direction='-1' and so.DepartmentID in (select departmentID from storage group by departmentID) ");
            // 按条件查询
            if ("下架".equals(type)) {
                if ("全部".equals(docType)) {
                    docType = null;
                }
                if (docType != null && !docType.isEmpty()) {
                    sb.append(" and type = '" + docType + "' and so.AuditFlag = '0' ");
                } else {
                    sb.append(" and type not in ('转仓出仓','盘亏','调整','赠品','差异','领用','返修','报损') and so.AuditFlag = '0' ");
                }
            } else if ("调出".equals(type)) {
                sb.append(" and type in('转仓出仓')  ");
            }
            if (warehouseId != null && !warehouseId.isEmpty() && !"".equals(warehouseId)) {
                sb.append(" and so.DepartmentID =  '" + warehouseId + "'  ");
            } else {
                sb.append(" and 1 = 2 ");
            }
            sb.append(" and so.No not in (select StockNo from AlreadyStock) and QuantitySum > 0 ");
            if ("下架".equals(type) && warehouseId != null && !warehouseId.isEmpty() && !"".equals(warehouseId)) {
                sb.append(" or so.StockID in (select StockID from stock where datediff(d,date,getdate()) < 8  and direction = '1'  and auditflag = '1' " + " and QuantitySum < 0 and No not in (select StockNo from AlreadyStock)) and so.DepartmentID = '" + warehouseId + "' ");
            }
            sb.append(" order by so.date desc ");
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
     * 根据算法获取推荐下架的仓位信息
     * 
     * @param req
     * @return
     */
    @RequestMapping(params = "getStorageOutDetail")
    @ResponseBody
    public AjaxJson getStorageOutDetail(HttpServletRequest req) {
        Client client = ResourceUtil.getClientFromSession(req);
        AjaxJson j = new AjaxJson();
        j.setAttributes(new HashMap<String, Object>());
        try {
            String departmentId = oConvertUtils.getString(req.getParameter("departmentId"));
            String stockNo = oConvertUtils.getString(req.getParameter("stockNo"));
            int storageOutType = oConvertUtils.getInt(req.getParameter("storageOutType"));
            Map map = null;
            int exit = Integer.parseInt(String.valueOf(commonDao.getData(" select count(1) from StorageOutTemp where relationNo = ? ", stockNo)));
            String userId = String.valueOf(commonDao.getData(" select top 1 userId from StorageOutTemp where relationNo = ? ", stockNo));
            if (exit == 0 || userId.equals(client.getUserID())) {
                map = shelvesOutService.generateStorageOutTemp(storageOutType, departmentId, stockNo, client);
            }
            StringBuffer sb = new StringBuffer();
            sb.append(
                    " select sot.TempID,g.Code,st.Storage,Color,Size,sot.Quantity,name+'('+g.code+')' Name,sot.GoodsID,sot.ColorID,sot.SizeID,sot.StorageID,isnull(g.SupplierCode,'无厂商货品编码') SupplierCode,(select userName from [user] where userId = '" + userId
                            + "' ) UserName from StorageOutTemp sot left join goods g on sot.goodsid = g.goodsid ").append(
                    " left join color c on sot.colorid = c.colorid left join size s on sot.sizeid = s.sizeid left join Storage st on st.storageID = sot.storageID where relationNo = '" + stockNo + "' ");
            // 按条件查询
            sb.append(" order by Storage asc,Quantity asc ");
            List list = commonDao.findForJdbc(sb.toString());
            j.setAttributes(map);
            j.setObj(list);
        } catch (Exception e) {
            j.setSuccess(false);
            j.setMsg(e.getMessage());
            SysLogger.error(e.getMessage(), e);
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
        }
        return j;
    }

    /**
     * 货品出仓下架
     * 
     * @param req
     * @return
     */
    @RequestMapping(params = "shelvesOut")
    @ResponseBody
    public AjaxJson shelvesOut(HttpServletRequest req) {
        Client client = ResourceUtil.getClientFromSession(req);
        AjaxJson j = new AjaxJson();
        j.setAttributes(new HashMap<String, Object>());
        try {
            String type = oConvertUtils.getString(req.getParameter("type"));
            String tempId = oConvertUtils.getString(req.getParameter("tempId"));
            String storageId = oConvertUtils.getString(req.getParameter("storageId"));
            String departmentId = oConvertUtils.getString(req.getParameter("departmentId"));
            String goodsId = oConvertUtils.getString(req.getParameter("goodsId"));
            String colorId = oConvertUtils.getString(req.getParameter("colorId"));
            String sizeId = oConvertUtils.getString(req.getParameter("sizeId"));
            String qtyStr = oConvertUtils.getString(req.getParameter("qty"));
            String stockNo = oConvertUtils.getString(req.getParameter("stockNo"));
            int count = shelvesOutService.singleGoodsShelvesOut(departmentId, type, tempId, storageId, goodsId, colorId, sizeId, qtyStr, stockNo, null, client);
            j.setObj(count);
        } catch (Exception e) {
            j.setSuccess(false);
            j.setMsg(e.getMessage());
            SysLogger.error(e.getMessage(), e);
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
        }
        return j;
    }

    /**
     * 货品快速出仓下架(关联单号下架)
     * 
     * @param req
     * @return
     */
    @RequestMapping(params = "quickShelvesOut")
    @ResponseBody
    public AjaxJson quickShelvesOut(HttpServletRequest req) {
        Client client = ResourceUtil.getClientFromSession(req);
        AjaxJson j = new AjaxJson();
        j.setAttributes(new HashMap<String, Object>());
        try {
            String stockNo = oConvertUtils.getString(req.getParameter("stockNo"));
            String type = oConvertUtils.getString(req.getParameter("type"));
            String tempId = "-1";
            String departmentId = oConvertUtils.getString(req.getParameter("departmentId"));
            String storageId = String.valueOf(commonDao.getData(" select storageId from department where departmentId = ? ", departmentId));
            String memo = "货品快速下架";
            if (null == storageId || "".equals(storageId) || "null".equalsIgnoreCase(storageId)) {
                throw new BusinessException("仓库的默认仓位为空,无法完成快速下架操作");
            }
            int exit = Integer.parseInt(String.valueOf(commonDao.getData(" select count(1) from AlreadyStockTemp where stockNo = ?", stockNo)));
            String sql = " select GoodsID,ColorID,SizeID,Quantity from stockdetail where stockId = (select stockId from stock where no = ?) ";
            if (exit > 0) {
                sql = " select GoodsID,ColorID,SizeID,Quantity from AlreadyStockTemp where stockNo = ? ";
            }
            List<Map<String, Object>> list = commonDao.findForJdbc(sql, stockNo);
            for (int i = 0; i < list.size(); i++) {
                Map<String, Object> map = list.get(i);
                String goodsId = String.valueOf(map.get("GoodsID"));
                String colorId = String.valueOf(map.get("ColorID"));
                String sizeId = String.valueOf(map.get("SizeID"));
                String qtyStr = String.valueOf(map.get("Quantity"));
                // 快速下架
                shelvesOutService.singleGoodsShelvesOut(departmentId, type, tempId, storageId, goodsId, colorId, sizeId, qtyStr, stockNo, memo, client);
            }
        } catch (Exception e) {
            j.setSuccess(false);
            j.setMsg(e.getMessage());
            SysLogger.error(e.getMessage(), e);
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
        }
        return j;
    }

    /**
     * 货品扫码出仓下架
     * 
     * @param req
     * @return
     */
    @RequestMapping(params = "scanningShelvesOut")
    @ResponseBody
    public AjaxJson scanningShelvesOut(HttpServletRequest req) {
        Client client = ResourceUtil.getClientFromSession(req);
        AjaxJson j = new AjaxJson();
        j.setAttributes(new HashMap<String, Object>());
        try {
            String departmentId = oConvertUtils.getString(req.getParameter("departmentId"));
            String type = oConvertUtils.getString(req.getParameter("type"));
            String memo = oConvertUtils.getString(req.getParameter("memo"));
            String stockNo = oConvertUtils.getString(req.getParameter("stockNo"));
            String storageId = oConvertUtils.getString(req.getParameter("storageId"));
            String goodsId = oConvertUtils.getString(req.getParameter("goodsId"));
            String colorId = oConvertUtils.getString(req.getParameter("colorId"));
            String sizeId = oConvertUtils.getString(req.getParameter("sizeId"));
            String qtyStr = oConvertUtils.getString(req.getParameter("qty"));
            int count = shelvesOutService.singleGoodsScanningShelvesOut(departmentId, type, storageId, goodsId, colorId, sizeId, qtyStr, stockNo, memo, client);
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("count", count);
            map.put("flag", true);
            j.setAttributes(map);
        } catch (Exception e) {
            j.setSuccess(false);
            j.setMsg(e.getMessage());
            SysLogger.error(e.getMessage(), e);
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
        }
        return j;
    }

    /**
     * 完成货品出仓下架
     * 
     * @param req
     * @return
     */
    @RequestMapping(params = "completeShelvesOut")
    @ResponseBody
    public AjaxJson completeShelvesOut(HttpServletRequest req) {
        Client client = ResourceUtil.getClientFromSession(req);
        AjaxJson j = new AjaxJson();
        j.setAttributes(new HashMap<String, Object>());
        try {
            String stockNo = oConvertUtils.getString(req.getParameter("stockNo"));
            String departmentId = oConvertUtils.getString(req.getParameter("departmentId"));
            int count = shelvesOutService.auditStock(stockNo, departmentId, client);
            j.setObj(count);
        } catch (Exception e) {
            j.setSuccess(false);
            j.setMsg(e.getMessage());
            SysLogger.error(e.getMessage(), e);
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
        }
        return j;
    }

    /**
     * 释放锁定的库位
     * 
     * @param req
     * @return
     */
    @RequestMapping(params = "releasingResources")
    @ResponseBody
    public AjaxJson releasingResources(HttpServletRequest req) {
        Client client = ResourceUtil.getClientFromSession(req);
        AjaxJson j = new AjaxJson();
        j.setAttributes(new HashMap<String, Object>());
        try {
            String stockNo = oConvertUtils.getString(req.getParameter("stockNo"));
            int count = shelvesOutService.releasingResources(stockNo);
            j.setObj(count);
        } catch (Exception e) {
            j.setSuccess(false);
            j.setMsg(e.getMessage());
            SysLogger.error(e.getMessage(), e);
        }
        return j;
    }

}
