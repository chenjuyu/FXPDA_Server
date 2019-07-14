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
 * Title: InvoicingController Description: 进销存查询逻辑控制器
 * 
 * @author LYJ
 * 
 */
@Controller
@RequestMapping("/invoicing")
public class InvoicingController extends BaseController {

    private Logger log = Logger.getLogger(InvoicingController.class);

    @Autowired
    private CommonDao commonDao;


    @RequestMapping(params = "queryInvoicing")
    @ResponseBody
    public synchronized AjaxJson queryInvoicing(HttpServletRequest req) {
        AjaxJson j = new AjaxJson();
        j.setAttributes(new HashMap<String, Object>());
        Client client = ResourceUtil.getClientFromSession(req);
        Map<String, String> map = client.getMap();
        String userRight = map.get(client.getUserID());
        String userId = oConvertUtils.getString(client.getUserID());
        try {
            String productId = oConvertUtils.getString(req.getParameter("productId"));
            String goodsId = oConvertUtils.getString(req.getParameter("goodsId"));
            String colorId = null;
            String sizeId = null;
            boolean preciseQueryStock = Boolean.valueOf(oConvertUtils.getString(req.getParameter("preciseQueryStock")));
            // 获取货品编号
            if (null == goodsId || "".equals(goodsId) || "null".equalsIgnoreCase(goodsId)) {
                if (preciseQueryStock) {
                    // 精确查询
                    List barcodeList = commonDao.findForJdbc(" select GoodsID,ColorID,SizeID from barcode where barcode = ? ", productId);
                    if (barcodeList.size() > 0) {
                        Map<String, Object> tmap = (Map<String, Object>) barcodeList.get(0);
                        goodsId = (String) tmap.get("GoodsID");
                        colorId = (String) tmap.get("ColorID");
                        sizeId = (String) tmap.get("SizeID");
                    }
                } else {
                    // 忽略颜色尺码查询
                    goodsId = new SelectController().getGoodsId(productId, commonDao);
                }
            }
            if (null == goodsId || "".equals(goodsId) || "null".equalsIgnoreCase(goodsId)) {
                throw new BusinessException("条码或货号错误");
            }
            // 货品明细信息
            List goodsDetailed =
                    commonDao.findForJdbc(" select GoodsID,code GoodsCode,name GoodsName,isnull((select Brand from brand where brandId = g.brandId),'') Brand,isnull(SupplierCode,'') SupplierCode, "
                            + " isnull(PurchasePrice,'') PurchasePrice,isnull(RetailSales,'') RetailSales,isnull((select top 1 salesprice from departmentprice where goodsId = '" + goodsId + "' order by beginDate desc),'') SalesPrice,"
                            + "isnull((select Tel from supplier where supplierId = g.supplierId),'') SupplierTel,PurchasedDate,LastPurchasedDate,isnull(datediff(day,PurchasedDate,LastPurchasedDate),0) ArrivalDays from goods g where GoodsID = ?", goodsId);
            // 删除StorageOutTTemp表并生成新表
            int exit = Integer.parseInt(String.valueOf(commonDao.getData(" select count(1) from dbo.[sysobjects] where name = 'InvoicingTemp_" + userId + "' ")));
            if (exit > 0) {
                commonDao.executeSql(" drop table InvoicingTemp_" + userId + " ");
            }
            // 生成货品进销存信息临时表
            commonDao.executeSql(" select t3.DepartmentID,t3.GoodsID,t3.ColorID,t3.SizeID,isnull(PurchaseSum,0) PurchaseSum,isnull(SalesSum,0) SalesSum,isnull(StockSum,0) StockSum into InvoicingTemp_" + userId + " "
                    + " from (select DepartmentID,GoodsID,ColorID,SizeID,sum(PurchaseSum) PurchaseSum from (select DepartmentID,GoodsID,ColorID,SizeID,sum(Quantity) PurchaseSum "
                    + " from stockdetail sd join stock s on s.stockId = sd.stockId where type = '采购' group by departmentId,GoodsID,ColorID,SizeID union all "
                    + " select DepartmentID,GoodsID,ColorID,SizeID,sum(Quantity) PurchaseSum from stockdetail sd join stock s on s.stockId = sd.stockId where type = '采购退货' "
                    + " group by departmentId,GoodsID,ColorID,SizeID) as a group by departmentId,GoodsID,ColorID,SizeID having sum(PurchaseSum) <> 0) as t1 "
                    + " right join (select DepartmentID,GoodsID,ColorID,SizeID,sum(SalesSum) SalesSum from (select DepartmentID,GoodsID,ColorID,SizeID,sum(Quantity) SalesSum "
                    + " from stockdetail sd join stock s on s.stockId = sd.stockId where type = '销售' group by departmentId,GoodsID,ColorID,SizeID union all "
                    + " select DepartmentID,GoodsID,ColorID,SizeID,sum(Quantity) SalesSum from stockdetail sd join stock s on s.stockId = sd.stockId where type = '销售退货' "
                    + " group by departmentId,GoodsID,ColorID,SizeID) as b group by departmentId,GoodsID,ColorID,SizeID having sum(SalesSum) <> 0) as t2 " + " on t1.departmentId = t2.departmentId and t1.goodsId = t2.goodsId and t1.colorId = t2.colorId and t1.sizeId = t2.sizeId "
                    + " right join (select DepartmentID,GoodsID,ColorID,SizeID,sum(StockSum) StockSum from (select DepartmentID,GoodsID,ColorID,SizeID,StockSum from (select DepartmentID,GoodsID,ColorID,SizeID,sum(Quantity*Direction) StockSum "
                    + " from stockdetail sd join stock s on s.stockId = sd.stockId  where s.auditflag = 1 group by departmentId,GoodsID,ColorID,SizeID having sum(Direction*Quantity)<>0 union all "
                    + " select DepartmentID,GoodsID,ColorID,SizeID,sum(Quantity*Direction) StockSum from stockdetail sd join stock s on s.stockId = sd.stockId  where s.auditflag = 1 "
                    + " group by departmentId,GoodsID,ColorID,SizeID having sum(Direction*Quantity)<>0 union all select ps.DepartmentID,GoodsID,ColorID,SizeID,-sum(Quantity) StockSum from possalesdetail pss "
                    + " join possales ps on ps.possalesid = pss.possalesid where ps.DaySumFlag = 0 group by ps.departmentId,GoodsID,ColorID,SizeID having sum(Quantity)<>0) as t group by DepartmentID,GoodsID,ColorID,SizeID,StockSum) "
                    + " as c group by departmentId,GoodsID,ColorID,SizeID having sum(StockSum)<>0) as t3 on t3.departmentId = t1.departmentId and t1.goodsId = t3.goodsId and t1.colorId = t3.colorId and t1.sizeId = t3.sizeId " + " where t3.DepartmentID in(" + userRight + ") and t3.GoodsID = ? ",
                    goodsId);
            List invoicingDetail = null;
            if (preciseQueryStock) {
                invoicingDetail =
                        commonDao.findForJdbc(" select (select department from department where departmentId = it.departmentId) Department," + " DepartmentID,sum(PurchaseSum) PurchaseCount,sum(SalesSum) SalesCount,sum(StockSum) StockCount from InvoicingTemp_" + userId + " it "
                                + " where goodsId = ? and colorId = ? and sizeId = ? group by departmentId ", goodsId, colorId, sizeId);
            } else {
                invoicingDetail =
                        commonDao.findForJdbc(" select (select department from department where departmentId = it.departmentId) Department," + " DepartmentID,sum(PurchaseSum) PurchaseCount,sum(SalesSum) SalesCount,sum(StockSum) StockCount from InvoicingTemp_" + userId + " it "
                                + " where goodsId = ? group by departmentId ", goodsId);
            }
            j.getAttributes().put("goodsDetailed", goodsDetailed);
            j.getAttributes().put("invoicingDetail", invoicingDetail);
            j.getAttributes().put("colorId", colorId);
            j.getAttributes().put("sizeId", sizeId);
        } catch (Exception e) {
            j.setSuccess(false);
            j.setMsg(e.getMessage());
            SysLogger.error(e.getMessage(), e);
        }
        return j;
    }

    /**
     * 查询店铺库存货品尺码明细
     * 
     * @param user
     * @param req
     * @return
     */
    @RequestMapping(params = "queryDistribution")
    @ResponseBody
    public synchronized AjaxJson queryDistribution(HttpServletRequest req) {
        AjaxJson j = new AjaxJson();
        Client client = ResourceUtil.getClientFromSession(req);
        String userId = oConvertUtils.getString(client.getUserID());
        j.setAttributes(new HashMap<String, Object>());
        try {
            StringBuffer sql = new StringBuffer();
            String goodsId = oConvertUtils.getString(req.getParameter("goodsId"));
            String departmentId = oConvertUtils.getString(req.getParameter("departmentId"));
            String colorId = oConvertUtils.getString(req.getParameter("colorId"));
            String sizeId = oConvertUtils.getString(req.getParameter("sizeId"));
            boolean preciseQueryStock = Boolean.valueOf(oConvertUtils.getString(req.getParameter("preciseQueryStock")));
            List dataList = null;
            List colorList = null;
            if (preciseQueryStock) {
                if (colorId != null && !"".equals(colorId) && !"null".equals(colorId) && sizeId != null && !"".equals(sizeId) && !"null".equals(sizeId)) {
                    sql.append(" select ColorID,SizeID,(select color from color ").append(" where colorId = it.colorId) Color,(select size from size ").append(" where sizeId = it.sizeId) Size,StockSum Quantity from InvoicingTemp_" + userId + " it ")
                            .append(" where departmentId = ? and goodsId = ? and colorId = ? and sizeId = ? ");
                    dataList = commonDao.findForJdbc(sql.toString(), departmentId, goodsId, colorId, sizeId);
                    colorList = commonDao.findForJdbc(" select distinct ColorID from InvoicingTemp_" + userId + " " + " where departmentId = ? and goodsId = ? and colorId = ? and sizeId = ?  ", departmentId, goodsId, colorId, sizeId);
                }
            } else {
                sql.append(" select ColorID,SizeID,(select color from color ").append(" where colorId = it.colorId) Color,(select size from size ").append(" where sizeId = it.sizeId) Size,StockSum Quantity from InvoicingTemp_" + userId + " it ").append(" where departmentId = ? and goodsId = ? ");
                dataList = commonDao.findForJdbc(sql.toString(), departmentId, goodsId);
                colorList = commonDao.findForJdbc(" select distinct ColorID from InvoicingTemp_" + userId + " " + " where departmentId = ? and goodsId = ? ", departmentId, goodsId);
            }
            j.getAttributes().put("dataList", dataList);
            j.getAttributes().put("colorList", colorList);
        } catch (Exception e) {
            j.setSuccess(false);
            j.setMsg(e.getMessage());
            SysLogger.error(e.getMessage(), e);
        }
        return j;
    }

}
