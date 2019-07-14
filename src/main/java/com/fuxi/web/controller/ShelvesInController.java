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
import com.fuxi.core.common.service.ShelvesInService;
import com.fuxi.system.util.Client;
import com.fuxi.system.util.ResourceUtil;
import com.fuxi.system.util.SysLogger;
import com.fuxi.system.util.oConvertUtils;

/**
 * Title: ShelvesInController Description: 仓位上架逻辑控制器
 * 
 * @author LYJ
 * 
 */
@Controller
@RequestMapping("/shelvesIn")
public class ShelvesInController extends BaseController {

    private Logger log = Logger.getLogger(ShelvesInController.class);

    @Autowired
    private CommonDao commonDao;
    @Autowired
    private ShelvesInService shelvesInService;

    /**
     * 根据筛选条件获取进仓单列表
     * 
     * @param req
     * @return
     */
    @RequestMapping(params = "getStockIn")
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
                    .append(" join Department de on de.DepartmentID = so.DepartmentID where  datediff(d,so.date,getdate()) < 8 and so.DepartmentID in (").append(userRight)
                    .append(") and direction='1' and so.AuditFlag = '1' and so.DepartmentID in (select departmentID from storage group by departmentID) ");
            // 按条件查询
            if ("上架".equals(type)) {
                if ("全部".equals(docType)) {
                    docType = null;
                }
                if (docType != null && !docType.isEmpty()) {
                    sb.append(" and type = '" + docType + "' ");
                } else {
                    sb.append(" and type not in ('转仓进仓','盘盈','调整','差异','返修','报溢') ");
                }
            } else if ("调入".equals(type)) {
                sb.append(" and type in('转仓进仓')  ");
            }
            if (warehouseId != null && !warehouseId.isEmpty() && !"".equals(warehouseId)) {
                sb.append(" and so.DepartmentID =  '" + warehouseId + "' ");
            } else {
                sb.append(" and 1 = 2 ");
            }
            sb.append(" and so.No not in (select StockNo from AlreadyStock) and QuantitySum > 0 ");
            if ("上架".equals(type) && warehouseId != null && !warehouseId.isEmpty() && !"".equals(warehouseId)) {
                sb.append(" or so.StockID in (select StockID from stock where  datediff(d,date,getdate()) < 8 and direction = '-1' and AuditFlag = '0' " + " and QuantitySum < 0 and No not in (select StockNo from AlreadyStock)) and so.DepartmentID = '" + warehouseId + "'  ");
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
     * 保存货品上架信息
     * 
     * @param req
     * @return
     */
    @RequestMapping(params = "saveStorageIn")
    @ResponseBody
    public AjaxJson saveStorageIn(HttpServletRequest req) {
        Client client = ResourceUtil.getClientFromSession(req);
        AjaxJson j = new AjaxJson();
        j.setAttributes(new HashMap<String, Object>());
        try {
            int count = 0;
            String stockNo = oConvertUtils.getString(req.getParameter("stockNo"));
            String storageId = oConvertUtils.getString(req.getParameter("storageId"));
            String departmentId = oConvertUtils.getString(req.getParameter("departmentId"));
            String type = oConvertUtils.getString(req.getParameter("type"));
            String barcode = oConvertUtils.getString(req.getParameter("barcode"));
            String qtyStr = oConvertUtils.getString(req.getParameter("qty"));
            String memo = oConvertUtils.getString(req.getParameter("memo"));
            String goodsId = oConvertUtils.getString(req.getParameter("goodsId"));
            String colorId = oConvertUtils.getString(req.getParameter("colorId"));
            String sizeId = oConvertUtils.getString(req.getParameter("sizeId"));
            if (null == qtyStr || qtyStr.isEmpty() || "".equals(qtyStr)) {
                qtyStr = "1";
            }
            int qty = Integer.parseInt(qtyStr);
            // 检测该仓位是否可存放
            boolean flag = true;
            // boolean flag = checkTopStock(storageId,qty);
            if (flag) {
                // 新货上架
                count = shelvesInService.saveStorageInMsg(departmentId, type, stockNo, storageId, barcode, qty, memo, goodsId, colorId, sizeId, client);
            }
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("count", count);
            map.put("flag", flag);
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
     * 货品快速上架操作(关联单号上架)
     * 
     * @param req
     * @return
     */
    @RequestMapping(params = "quickStorageIn")
    @ResponseBody
    public AjaxJson quickStorageIn(HttpServletRequest req) {
        Client client = ResourceUtil.getClientFromSession(req);
        AjaxJson j = new AjaxJson();
        j.setAttributes(new HashMap<String, Object>());
        try {
            String stockNo = oConvertUtils.getString(req.getParameter("stockNo"));
            String departmentId = oConvertUtils.getString(req.getParameter("departmentId"));
            String useLastTimePosition = oConvertUtils.getString(req.getParameter("useLastTimePosition"));
            if ("true".equals(useLastTimePosition)) {
                // 使用最近一次上架的仓位快速上架
                String type = oConvertUtils.getString(req.getParameter("type"));
                String barcode = null;
                String memo = "货品快速上架";
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
                    int qty = Integer.parseInt(String.valueOf(map.get("Quantity")));
                    String storageId = String.valueOf(commonDao.getData(" select top 1 StorageID from storageIn where goodsId = '" + goodsId + "' and colorId = '" + colorId + "' and sizeId = '" + sizeId + "' and departmentId = '" + departmentId + "' order by madedate desc "));
                    if (null == storageId || "".equals(storageId) || "null".equalsIgnoreCase(storageId)) {
                        throw new BusinessException("仓库的默认仓位为空,无法完成快速上架操作");
                    }
                    // 新货上架
                    shelvesInService.saveStorageInMsg(departmentId, type, stockNo, storageId, barcode, qty, memo, goodsId, colorId, sizeId, client);
                }
            } else {
                // 使用默认仓位快速上架
                String storageId = String.valueOf(commonDao.getData(" select storageId from department where departmentId = ? ", departmentId));
                if (null == storageId || "".equals(storageId) || "null".equalsIgnoreCase(storageId)) {
                    throw new BusinessException("仓库的默认仓位为空,无法完成快速上架操作");
                }
                String type = oConvertUtils.getString(req.getParameter("type"));
                String barcode = null;
                String memo = "货品快速上架";
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
                    int qty = Integer.parseInt(String.valueOf(map.get("Quantity")));
                    // 新货上架
                    shelvesInService.saveStorageInMsg(departmentId, type, stockNo, storageId, barcode, qty, memo, goodsId, colorId, sizeId, client);
                }
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
     * 获取初始化上架货品信息
     * 
     * @param req
     * @return
     */
    @RequestMapping(params = "getInitStorageIn")
    @ResponseBody
    public AjaxJson getInitStorageIn(HttpServletRequest req) {
        Client client = ResourceUtil.getClientFromSession(req);
        AjaxJson j = new AjaxJson();
        j.setAttributes(new HashMap<String, Object>());
        try {
            String storageId = oConvertUtils.getString(req.getParameter("storageId"));
            String departmentId = oConvertUtils.getString(req.getParameter("departmentId"));
            String type = oConvertUtils.getString(req.getParameter("type"));
            List list =
                    commonDao.findForJdbc(" select g.code GoodsCode,Color,Size,si.GoodsID,si.ColorID,si.SizeID,Quantity from storageIn si join Goods g on g.goodsId = si.goodsId "
                            + " join Color c on c.colorId = si.colorId join Size s on s.sizeId = si.sizeId where departmentId = ? and StorageId = ? and type = ? ", departmentId, storageId, type);
            j.setObj(list);
        } catch (Exception e) {
            j.setSuccess(false);
            j.setMsg(e.getMessage());
            SysLogger.error(e.getMessage(), e);
        }
        return j;
    }

    /**
     * 修改货品上架数量
     * 
     * @param req
     * @return
     */
    @RequestMapping(params = "updateStorageInCount")
    @ResponseBody
    public AjaxJson updateStorageInCount(HttpServletRequest req) {
        Client client = ResourceUtil.getClientFromSession(req);
        AjaxJson j = new AjaxJson();
        j.setAttributes(new HashMap<String, Object>());
        try {
            String storageId = oConvertUtils.getString(req.getParameter("storageId"));
            String departmentId = oConvertUtils.getString(req.getParameter("departmentId"));
            String type = oConvertUtils.getString(req.getParameter("type"));
            String qtyStr = oConvertUtils.getString(req.getParameter("qty"));
            String goodsId = oConvertUtils.getString(req.getParameter("goodsId"));
            String colorId = oConvertUtils.getString(req.getParameter("colorId"));
            String sizeId = oConvertUtils.getString(req.getParameter("sizeId"));
            if (null == qtyStr || qtyStr.isEmpty() || "".equals(qtyStr)) {
                qtyStr = "1";
            }
            int qty = Integer.parseInt(qtyStr);
            // 修改上架数量
            int count = shelvesInService.updateStorageInCount(departmentId, storageId, qty, type, goodsId, colorId, sizeId, client);
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
     * 检测该仓位是否可存放
     * 
     * @param storageId
     * @param qty
     * @return
     */
    private boolean checkTopStock(String storageId, int qty) {
        boolean flag = true;
        // 生成出仓单对应的临时表
        // 删除StorageOutTTemp表并生成新表
        int exit = Integer.parseInt(String.valueOf(commonDao.getData(" select count(1) from dbo.[sysobjects] where name = 'StorageOutTTemp' ")));
        if (exit > 0) {
            commonDao.executeSql(" drop table StorageOutTTemp ");
        }
        commonDao.executeSql(" select si.storageID,si.goodsID,si.colorID,si.sizeID, " + " (isnull(sum(si.quantity),0)-isnull((select sum(quantity) quantity from storageOut where storageID = si.storageID and goodsID = si.goodsID "
                + " and colorID = si.colorID and sizeID = si.sizeID group by storageID,goodsID,colorID,sizeID ),0)) quantity into StorageOutTTemp " + " from storageIn si group by storageID,goodsID,colorID,sizeID ");
        int topStock = Integer.parseInt(String.valueOf(commonDao.getData(" select TopStock from Storage where StorageId = ? ", storageId)));
        int quantity = Integer.parseInt(String.valueOf(commonDao.getData(" select sum(quantity) Quantity from StorageOutTTemp where StorageId = ? ", storageId)));
        // 库位剩余数量
        int surplus = topStock - quantity;
        if (qty > surplus) {
            flag = false;
        }
        return flag;
    }

    /**
     * 检查条码是否在进仓单内
     * 
     * @param req
     * @return
     */
    @RequestMapping(params = "checkBarcodeAndQty")
    @ResponseBody
    public AjaxJson checkBarcodeAndQty(HttpServletRequest req) {
        AjaxJson j = new AjaxJson();
        j.setAttributes(new HashMap<String, Object>());
        try {
            int count = 0, quantity = 0;
            String stockNo = oConvertUtils.getString(req.getParameter("stockNo"));
            String goodsId = oConvertUtils.getString(req.getParameter("goodsId"));
            String colorId = oConvertUtils.getString(req.getParameter("colorId"));
            String sizeId = oConvertUtils.getString(req.getParameter("sizeId"));
            String departmentId = oConvertUtils.getString(req.getParameter("departmentId"));
            String useLastTimePosition = oConvertUtils.getString(req.getParameter("useLastTimePosition"));
            StringBuilder sql = new StringBuilder();
            sql.append(" select count(1) count,Quantity from AlreadyStockTemp where stockNo = '").append(stockNo).append("' and goodsid = '").append(goodsId + "' and colorid = '").append(colorId).append("' and sizeid = '").append(sizeId).append("' group by Quantity ");
            List list = commonDao.findForJdbc(sql.toString());
            Map<String, Object> temp = new HashMap<String, Object>();
            if (list.size() > 0) {
                Map map = (Map) list.get(0);
                count = Integer.parseInt(String.valueOf(map.get("count")));
                quantity = Integer.parseInt(String.valueOf(map.get("Quantity")));
            }
            // 获取最近一次上架的仓位
            List storageList = null;
            if ("true".equals(useLastTimePosition)) {
                storageList = commonDao.findForJdbc(" select top 1 StorageID,(select Storage from Storage where StorageId = si.StorageId) Storage from storageIn si where goodsId = ? and colorId = ? and sizeId = ? and departmentId = ? order by madedate desc ", goodsId, colorId, sizeId, departmentId);
            }
            temp.put("count", count);
            temp.put("quantity", quantity);
            temp.put("storageList", storageList);
            j.setObj(temp);
        } catch (Exception e) {
            j.setSuccess(false);
            j.setMsg(e.getMessage());
            SysLogger.error(e.getMessage(), e);
        }
        return j;
    }

    /**
     * 获取出仓单明细并保存到AlreadyStockTemp(关联进仓单上架时)
     * 
     * @param req
     * @return
     */
    @RequestMapping(params = "getStockDetail")
    @ResponseBody
    public AjaxJson getStockDetail(HttpServletRequest req) {
        AjaxJson j = new AjaxJson();
        j.setAttributes(new HashMap<String, Object>());
        try {
            Client client = ResourceUtil.getClientFromSession(req);
            String stockNo = oConvertUtils.getString(req.getParameter("stockNo"));
            shelvesInService.getStockDetail(stockNo, client);
        } catch (Exception e) {
            j.setSuccess(false);
            j.setMsg(e.getMessage());
            SysLogger.error(e.getMessage(), e);
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
        }
        return j;
    }

    /**
     * 释放锁定的进仓单
     * 
     * @param req
     * @return
     */
    @RequestMapping(params = "releasingResources")
    @ResponseBody
    public AjaxJson releasingResources(HttpServletRequest req) {
        AjaxJson j = new AjaxJson();
        j.setAttributes(new HashMap<String, Object>());
        try {
            String stockNo = oConvertUtils.getString(req.getParameter("stockNo"));
            int count = shelvesInService.releasingResources(stockNo);
            j.setObj(count);
        } catch (Exception e) {
            j.setSuccess(false);
            j.setMsg(e.getMessage());
            SysLogger.error(e.getMessage(), e);
        }
        return j;
    }

    /**
     * 检查AlreadyStockTemp中是否已经存在单据信息(关联进仓单上架时限制用户唯一操作单据)
     * 
     * @param req
     * @return
     */
    @RequestMapping(params = "checkUniqueOperation")
    @ResponseBody
    public AjaxJson checkUniqueOperation(HttpServletRequest req) {
        AjaxJson j = new AjaxJson();
        j.setAttributes(new HashMap<String, Object>());
        try {
            Map<String, Object> map = new HashMap<String, Object>();
            Client client = ResourceUtil.getClientFromSession(req);
            String stockNo = oConvertUtils.getString(req.getParameter("stockNo"));
            String userName = null;
            commonDao.executeSql(" update AlreadyStockTemp set OperateFlag = '0' where OperateFlag is null or OperateFlag = '' ");
            int count = Integer.parseInt(String.valueOf(commonDao.getData(" select count(1) from AlreadyStockTemp where stockNo = '" + stockNo + "' and OperateFlag = '1' and userID <> '" + client.getUserID() + "' ")));
            if (count > 1) {
                userName = String.valueOf(commonDao.getData(" select UserName from [user] where userId = (select distinct userId from AlreadyStockTemp where stockNo = '" + stockNo + "' and userId is not null and userId <> '') "));
            }
            map.put("count", count);
            map.put("userName", userName);
            j.setObj(map);
        } catch (Exception e) {
            j.setSuccess(false);
            j.setMsg(e.getMessage());
            SysLogger.error(e.getMessage(), e);
        }
        return j;
    }

    /**
     * 获取出仓单未上架的明细(关联进仓单上架时)
     * 
     * @param req
     * @return
     */
    @RequestMapping(params = "getDifferentialStockDetail")
    @ResponseBody
    public AjaxJson getDifferentialStockDetail(HttpServletRequest req) {
        AjaxJson j = new AjaxJson();
        j.setAttributes(new HashMap<String, Object>());
        try {
            String stockNo = oConvertUtils.getString(req.getParameter("stockNo"));
            // 判断AlreadyStock中是否存在stockId
            List list =
                    commonDao.findForJdbc(" select g.GoodsID,g.Code GoodsCode, c.ColorID,c.Color, s.SizeID,s.Size, Quantity from AlreadyStockTemp ast join " + " Goods g on g.goodsId = ast.goodsId join Color c on c.colorId = ast.colorId join Size s on s.sizeId = ast.sizeId where stockNo = ? ",
                            stockNo);
            j.setObj(list);
        } catch (Exception e) {
            j.setSuccess(false);
            j.setMsg(e.getMessage());
            SysLogger.error(e.getMessage(), e);
        }
        return j;
    }

    /**
     * 获取出仓单未上架的总数量(关联进仓单上架时)
     * 
     * @param req
     * @return
     */
    @RequestMapping(params = "getUnShelvesInTotal")
    @ResponseBody
    public AjaxJson getUnShelvesInTotal(HttpServletRequest req) {
        AjaxJson j = new AjaxJson();
        j.setAttributes(new HashMap<String, Object>());
        try {
            String stockNo = oConvertUtils.getString(req.getParameter("stockNo"));
            // 查询AlreadyStockTemp中stockId的总数量
            int count = Integer.parseInt(String.valueOf(commonDao.getData(" select isnull(sum(isnull(Quantity,0)),0) from AlreadyStockTemp where stockNo = ? ", stockNo)));
            j.setObj(count);
        } catch (Exception e) {
            j.setSuccess(false);
            j.setMsg(e.getMessage());
            SysLogger.error(e.getMessage(), e);
        }
        return j;
    }

}
