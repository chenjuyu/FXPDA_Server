package com.fuxi.web.controller;

import java.math.BigDecimal;
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
import com.fuxi.system.util.SysLogger;
import com.fuxi.system.util.oConvertUtils;

/**
 * Title: BarcodePrintController Description: 条码打印逻辑控制器
 * 
 * @author LYJ
 * 
 */
@Controller
@RequestMapping("/barcodePrint")
public class BarcodePrintController extends BaseController {

    private Logger log = Logger.getLogger(BarcodePrintController.class);

    @Autowired
    private CommonDao commonDao;

    /**
     * 查询货品信息
     * 
     * @param req
     * @return
     */
    @RequestMapping(params = "queryGoods")
    @ResponseBody
    public AjaxJson queryStock(HttpServletRequest req) {
        AjaxJson j = new AjaxJson();
        j.setAttributes(new HashMap<String, Object>());
        try {
            boolean flag = false;
            String productId = oConvertUtils.getString(req.getParameter("productId"));
            String goodsId = null;
            String colorId = null;
            String sizeId = null;
            String goodsName = null;
            String goodsCode = null;
            String retailSales = null;
            String colorCode = null;
            String sizeCode = null;
            String colorName = null;
            String sizeName = null;
            // 精确查询
            List barcodeList =
                    commonDao.findForJdbc(" select GoodsID,(select Name from Goods where goodsId = b.goodsID) GoodsName," + "(select Code from Goods where goodsId = b.goodsID) GoodsCode,(select RetailSales from Goods where goodsId = b.goodsID) RetailSales,"
                            + "(select No from color where colorId = b.colorId) ColorCode,(select Color from Color where colorId = b.colorId) ColorName, (select Size from Size where sizeId = b.sizeId) SizeName, ColorID, (select No from size where sizeId = b.sizeId) SizeCode,"
                            + "SizeID from barcode b where barcode = ? ", productId);
            if (barcodeList.size() > 0) {
                Map<String, Object> map = (Map<String, Object>) barcodeList.get(0);
                goodsId = (String) map.get("GoodsID");
                colorId = (String) map.get("ColorID");
                sizeId = (String) map.get("SizeID");
                goodsName = (String) map.get("GoodsName");
                goodsCode = (String) map.get("GoodsCode");
                retailSales = String.valueOf(((BigDecimal) map.get("RetailSales")));
                colorCode = (String) map.get("ColorCode");
                sizeCode = (String) map.get("SizeCode");
                colorName = (String) map.get("ColorName");
                sizeName = (String) map.get("SizeName");
                flag = true;
            }
            // 忽略颜色尺码查询
            goodsId = new SelectController().getGoodsId(productId, commonDao);
            if (null == goodsId || "".equals(goodsId) || "null".equalsIgnoreCase(goodsId)) {
                throw new BusinessException("条码或货号错误");
            }
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("goodsId", goodsId);
            map.put("colorId", colorId);
            map.put("sizeId", sizeId);
            map.put("goodsName", goodsName);
            map.put("goodsCode", goodsCode);
            map.put("retailSales", retailSales);
            map.put("colorCode", colorCode);
            map.put("sizeCode", sizeCode);
            map.put("colorName", colorName);
            map.put("sizeName", sizeName);
            j.setObj(map);
        } catch (Exception e) {
            j.setSuccess(false);
            j.setMsg(e.getMessage());
            SysLogger.error(e.getMessage(), e);
        }
        return j;
    }

    /**
     * 查询厂商条码
     * 
     * @param req
     * @return
     */
    @RequestMapping(params = "queryBarcode")
    @ResponseBody
    public AjaxJson queryBarcode(HttpServletRequest req) {
        AjaxJson j = new AjaxJson();
        j.setAttributes(new HashMap<String, Object>());
        try {
            String goodsId = oConvertUtils.getString(req.getParameter("goodsId"));
            String colorId = oConvertUtils.getString(req.getParameter("colorId"));
            String sizeId = oConvertUtils.getString(req.getParameter("sizeId"));
            String barcode = commonDao.getDataForString(" select top 1 barcode from barcode where goodsId = ? and colorId = ? and sizeId = ? ", goodsId, colorId, sizeId);
            j.setObj(barcode);
        } catch (Exception e) {
            j.setSuccess(false);
            j.setMsg(e.getMessage());
            SysLogger.error(e.getMessage(), e);
        }
        return j;
    }


}
