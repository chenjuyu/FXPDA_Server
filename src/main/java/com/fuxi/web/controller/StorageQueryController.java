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
import com.fuxi.core.common.exception.BusinessException;
import com.fuxi.core.common.model.json.AjaxJson;
import com.fuxi.system.util.Client;
import com.fuxi.system.util.ResourceUtil;
import com.fuxi.system.util.SysLogger;
import com.fuxi.system.util.oConvertUtils;

/**
 * Title: StorageQueryController Description: 仓位库存分布查询逻辑控制器
 * 
 * @author LYJ
 * 
 */
@Controller
@RequestMapping("/storageQuery")
public class StorageQueryController extends BaseController {

    private Logger log = Logger.getLogger(StorageQueryController.class);

    @Autowired
    private CommonDao commonDao;

    /**
     * 根据货品编码或条码获取货品信息
     * 
     * @param productId
     * @return
     */
    private List<Map<String, Object>> getGoodsMsg(String productId) {
        int count = 0;
        List list = null;
        count = Integer.parseInt(String.valueOf(commonDao.getData("select count(1) from barcode where barcode = ?", productId)));
        if (count > 0) {
            list = commonDao.findForJdbc("select GoodsID,ColorID,SizeID from barcode where barcode = ?", productId);
        } else {
            String sizeNo = productId.substring(productId.length() - 2, productId.length());
            String colorNo = productId.substring(productId.length() - 4, productId.length() - 2);
            String goodsCode = productId.substring(0, productId.length() - 4);
            StringBuilder sb = new StringBuilder();
            sb.append(" select g.GoodsID,c.ColorID,s.SizeID from Goods g, ").append(" Color c,size s,GoodsType gt,SizeGroup sg,SizeGroupSize ss where s.SizeID = (select sizeId from size s where s.no = ?)  ")
                    .append(" and c.colorId = (select colorId from color c where c.no = ?) and g.goodsId = (select goodsId from goods g where g.code = ?) ").append(" and gt.GoodsTypeID = g.GoodsTypeID and sg.SizeGroupID = gt.SizeGroupID ")
                    .append(" and ss.SizeGroupID = sg.SizeGroupID and ss.SizeID = (select sizeId from size s where s.no = ?) ");
            list = commonDao.findForJdbc(sb.toString(), sizeNo, colorNo, goodsCode, sizeNo);
        }
        return list;
    }

    /**
     * 根据仓位编码获取仓位ID
     * 
     * @param storageCode
     * @return
     */
    private String getStorageId(String storageCode) {
        String storageId = null;
        storageId = String.valueOf(commonDao.getData("select storageId from storage where code = ?", storageCode));
        return storageId;
    }

    /**
     * 查询仓位货架的货品库存分布
     * 
     * @param user
     * @param req
     * @return
     */
    @RequestMapping(params = "queryStorage")
    @ResponseBody
    public AjaxJson queryStorage(HttpServletRequest req) {
        AjaxJson j = new AjaxJson();
        Client client = ResourceUtil.getClientFromSession(req);
        Map<String, String> temp = client.getMap();
        String userRight = temp.get(client.getUserID());
        j.setAttributes(new HashMap<String, Object>());
        try {
            StringBuffer sql = new StringBuffer();
            String colorId = null;
            String sizeId = null;
            String storageId = oConvertUtils.getString(req.getParameter("storageId"));
            String storageCode = oConvertUtils.getString(req.getParameter("storageCode"));
            String goodsId = oConvertUtils.getString(req.getParameter("goodsId"));
            String goodsCode = oConvertUtils.getString(req.getParameter("goodsCode"));
            // 判断是否是货品条码
            if ((goodsId == null || goodsId.isEmpty() || "null".equalsIgnoreCase(goodsId)) && (goodsCode != null && !goodsCode.isEmpty() && !"null".equalsIgnoreCase(goodsCode))) {
                // 判断输入的是否为货号
                goodsId = String.valueOf(commonDao.getData("select goodsId from goods where code = ?", goodsCode));
                if (goodsId == null || goodsId.isEmpty() || "null".equalsIgnoreCase(goodsId)) {
                    List<Map<String, Object>> list = getGoodsMsg(goodsCode);
                    if (list != null && list.size() > 0) {
                        Map<String, Object> map = list.get(0);
                        goodsId = String.valueOf(map.get("GoodsID"));
                        colorId = String.valueOf(map.get("ColorID"));
                        sizeId = String.valueOf(map.get("SizeID"));
                    }
                }
                if (goodsId == null || goodsId.isEmpty() || "null".equalsIgnoreCase(goodsId)) {
                    goodsCode = goodsCode.substring(0, goodsCode.length() - 5);
                    goodsId = String.valueOf(commonDao.getData("select goodsId from goods where code = ?", goodsCode));
                }
            }
            if ((storageId == null || storageId.isEmpty() || "null".equalsIgnoreCase(storageId)) && (storageCode != null && !storageCode.isEmpty() && !"null".equalsIgnoreCase(storageCode))) {
                storageId = getStorageId(storageCode);
            }
            if ((goodsId == null || goodsId.isEmpty() || "null".equalsIgnoreCase(goodsId)) && (storageId == null || storageId.isEmpty() || "null".equalsIgnoreCase(storageId))) {
                if (goodsCode != null && !goodsCode.isEmpty() && !"null".equalsIgnoreCase(goodsCode)) {
                    throw new BusinessException("货品条码或货号错误");
                } else {
                    throw new BusinessException("仓位编码错误");
                }
            }
            // 删除StorageOutTTemp表并生成新表
            int exit = Integer.parseInt(String.valueOf(commonDao.getData(" select count(1) from dbo.[sysobjects] where name = 'StorageOutTTemp_" + client.getUserID() + "' ")));
            if (exit > 0) {
                commonDao.executeSql(" drop table StorageOutTTemp_" + client.getUserID() + " ");
            }
            commonDao.executeSql("  select  storageID,goodsID,colorID,sizeID,departmentId,Sum(quantity) Quantity into StorageOutTTemp_" + client.getUserID() + " from " + " (select storageID,goodsID,colorID,sizeID,departmentId,sum(quantity) as quantity " + " from storageIn with(nolock) "
                    + " group by storageID,goodsID,colorID,sizeID,departmentId union all " + " select storageID,goodsID,colorID,sizeID,departmentId,-sum(quantity) as quantity " + " from storageOut with(nolock) "
                    + " group by storageID,goodsID,colorID,sizeID,departmentId) a  group by storageID,goodsID,colorID,sizeID,departmentId having isnull(Sum(quantity),0) <>  0 ");
            sql.append(
                    " select st.StorageID,g.GoodsID,c.ColorID,s.SizeID,g.Code GoodsCode,c.Color,s.Size,st.Storage,d.Department,sum(isnull(t.Quantity,0)) Quantity from StorageOutTTemp_" + client.getUserID() + " t join "
                            + " Storage st on st.storageId = t.storageId join Goods g on g.goodsid = t.goodsid " + " join Color c on c.colorid = t.colorid join Size s on s.sizeid = t.sizeid join Department d on d.DepartmentId = t.DepartmentID and t.DepartmentID in (").append(userRight).append(") ");
            if (null != storageId && !storageId.isEmpty()) {
                sql.append(" and t.storageId = '" + storageId + "' ");
            }
            if (null != goodsId && !goodsId.isEmpty()) {
                sql.append(" and t.goodsId = '" + goodsId + "' ");
            }
            if (null != colorId && !colorId.isEmpty()) {
                sql.append(" and t.colorId = '" + colorId + "' ");
            }
            if (null != sizeId && !sizeId.isEmpty()) {
                sql.append(" and t.sizeId = '" + sizeId + "' ");
            }
            sql.append(" group by d.Department,st.StorageID,g.GoodsID,c.ColorID,s.SizeID,g.Code,c.Color,s.Size,st.Storage " + "  having sum(isnull(t.Quantity,0)) <> 0 order by st.Storage,g.GoodsID,c.ColorID,s.SizeID,Quantity ");
            List list = commonDao.findForJdbc(sql.toString());
            j.setObj(list);
            commonDao.executeSql(" drop table StorageOutTTemp_" + client.getUserID() + " ");
        } catch (Exception e) {
            j.setSuccess(false);
            j.setMsg(e.getMessage());
            SysLogger.error(e.getMessage(), e);
        }
        return j;
    }

    /**
     * 查询暂时锁定的货品库存分布
     * 
     * @param user
     * @param req
     * @return
     */
    @RequestMapping(params = "getLockDetail")
    @ResponseBody
    public AjaxJson getLockDetail(HttpServletRequest req) {
        AjaxJson j = new AjaxJson();
        j.setAttributes(new HashMap<String, Object>());
        try {
            StringBuffer sql = new StringBuffer();
            sql.append(" select Storage+'['+Department+']' Storage,g.Code GoodsCode,Color,Size,Quantity,UserName,relationNo StockNo from storageouttemp sot ").append(" join department d on sot.departmentId = d.departmentId join storage st on st.storageId = sot.storageId ")
                    .append(" join goods g on g.goodsId = sot.goodsId join size s on s.sizeId = sot.sizeId join color c on c.colorId = sot.colorId ").append(" join [User] u on u.userId = sot.userId where st.storageId <> '-1' and st.departmentId <> '' order by relationNo asc,Storage asc ");
            List list = commonDao.findForJdbc(sql.toString());
            j.setObj(list);
        } catch (Exception e) {
            j.setSuccess(false);
            j.setMsg(e.getMessage());
            SysLogger.error(e.getMessage(), e);
        }
        return j;
    }

    /**
     * 判断仓库是否在初始化
     * 
     * @param user
     * @param req
     * @return
     */
    @RequestMapping(params = "judgeWarehouseType")
    @ResponseBody
    public AjaxJson judgeWarehouseType(HttpServletRequest req) {
        AjaxJson j = new AjaxJson();
        j.setAttributes(new HashMap<String, Object>());
        try {
            String warehouseId = oConvertUtils.getString(req.getParameter("warehouseId"));
            StringBuffer sql = new StringBuffer();
            sql.append(" select count(1) from Department where DepartmentId = ? and WarehouseFlag = '1' and Init <> '1' and DepartmentID in ( select distinct departmentID from storage ) ");
            int count = Integer.parseInt(String.valueOf(commonDao.getData(sql.toString(), warehouseId)));
            j.setObj(count);
        } catch (Exception e) {
            j.setSuccess(false);
            j.setMsg(e.getMessage());
            SysLogger.error(e.getMessage(), e);
        }
        return j;
    }

}
