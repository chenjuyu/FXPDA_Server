package com.fuxi.web.controller;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONArray;

import org.apache.commons.fileupload.DiskFileUpload;
import org.apache.commons.fileupload.FileItem;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fuxi.core.common.dao.impl.CommonDao;
import com.fuxi.core.common.exception.BusinessException;
import com.fuxi.core.common.model.json.AjaxJson;
import com.fuxi.system.util.Client;
import com.fuxi.system.util.DataUtils;
import com.fuxi.system.util.ExcelUtil;
import com.fuxi.system.util.ImgCompress;
import com.fuxi.system.util.LoadUserCount;
import com.fuxi.system.util.MyTools;
import com.fuxi.system.util.ResourceUtil;
import com.fuxi.system.util.SysLogger;
import com.fuxi.system.util.oConvertUtils;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;

/*
 * Title: CommonController Description: 通用逻辑方法控制器[请求时无需验证Client是否过期]
 * 
 * @author LYJ
 * 
 */
@Controller
@RequestMapping("/common")
public class CommonController extends BaseController {

    private Logger log = Logger.getLogger(CommonController.class);

    @Autowired
    private CommonDao commonDao;

    /**
     * 图片下载方法
     * 
     * @param request
     * @param response
     */
    @SuppressWarnings("resource")
    @RequestMapping(params = "image")
    public synchronized void getImage(HttpServletRequest request, HttpServletResponse response) {
        FileInputStream fis = null;
        response.setContentType("image/gif");
        String imageName = oConvertUtils.getString(request.getParameter("code"));
        try {
            String path = new String(ResourceUtil.getConfigByName("imgPath").getBytes("iso-8859-1"), "UTF-8");
            OutputStream out = response.getOutputStream();
            String filePath = path + File.separator + imageName + ".jpg";
            File file = new File(filePath);
            if (!file.exists()) {
                File dis = new File(path);
                if (!dis.exists()) {
                    dis.mkdir();
                }
                File sourceFile = new File(path + File.separator + imageName + ".jpg");
                FileInputStream sourceFis = new FileInputStream(sourceFile);
                byte[] sourceB = new byte[sourceFis.available()];
                sourceFis.read(sourceB);
                ByteArrayInputStream in = new ByteArrayInputStream(sourceB);
                ImgCompress ic = new ImgCompress(in, filePath);
                ic.resizeByHeight(300);
                file = new File(filePath);
            }
            fis = new FileInputStream(file);
            byte[] b = new byte[fis.available()];
            fis.read(b);
            out.write(b);
            out.flush();
        } catch (Exception e) {

        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 客户端检测新版本
     * 
     * @param req
     * @return
     */
    @RequestMapping(params = "checkVersion")
    @ResponseBody
    public AjaxJson checkVersion(HttpServletRequest req) {
        AjaxJson j = new AjaxJson();
        j.setAttributes(new HashMap<String, Object>());
        try {
            Map map = new HashMap();
            String version = ResourceUtil.getConfigByName("appVersion");
            String path = ResourceUtil.getConfigByName("appUrl");
            String description = new String(ResourceUtil.getConfigByName("description").getBytes("iso-8859-1"), "UTF-8");
            String forceUpdate = ResourceUtil.getConfigByName("forceUpdate");
            map.put("Version", version);
            map.put("Url", path);
            map.put("Description", description);
            map.put("ForceUpdate", Boolean.parseBoolean(forceUpdate));
            // 传递登录参数(用于登录)
            map.put("corpName", LoadUserCount.corpName);
            map.put("regId", LoadUserCount.regId);
            j.setObj(map);
        } catch (Exception e) {
            j.setSuccess(false);
            j.setMsg(e.getMessage());
            SysLogger.error(e.getMessage(), e);
        }
        return j;
    }

    /**
     * 打印单据前获取单据显示数据方法(默认打印格式)
     * 
     * @param req
     * @return
     */
    @RequestMapping(params = "print")
    @ResponseBody
    public synchronized void print(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        try {
            String id = oConvertUtils.getString(req.getParameter("id"));
            String tableName = oConvertUtils.getString(req.getParameter("tableName"));
            String userName = oConvertUtils.getString(req.getParameter("userName"));
            String printIp = oConvertUtils.getString(req.getParameter("printIp"));
            String printPort = oConvertUtils.getString(req.getParameter("printPort"));
            String docType = oConvertUtils.getString(req.getParameter("docType"));
            String shop = null;
            try {
                shop = new String(ResourceUtil.getConfigByName("shopTitle").getBytes("iso-8859-1"), "UTF-8");
                if (shop == null || "".equals(shop) || shop.isEmpty()) {
                    shop = LoadUserCount.regName;
                }
            } catch (Exception e) {
                System.out.println(e);
                if (shop == null || "".equals(shop) || shop.isEmpty()) {
                    shop = LoadUserCount.regName;
                }
            }
            int maxSize = getMaxSize(id, tableName);
            StringBuffer sb = new StringBuffer();
            // 采购
            if ("Purchase".equals(tableName)) {
                sb.append(" select isnull(d1.Supplier,'') customer,  ").append("  isnull(d2.Department,'') department,so.quantitySum,so.amountSum,so.date,so.no,so.audit, so.type from " + tableName + " so  ").append(" left join Supplier d1 on d1.SupplierID = so.SupplierID ")
                        .append(" left join Department d2 on d2.DepartmentID = so.DepartmentID ").append(" where " + tableName + "ID = '").append(id).append("'");
            } else if ("StockMove".equals(tableName)) {// 转仓单
                sb.append(" select isnull(d1.Customer,'') customer,  ").append("  isnull(d2.Department,'') department,so.quantitySum,so.amountSum,so.date,so.no,so.audit from " + tableName + " so  ").append(" left join Customer d1 on d1.CustomerID = so.CustomerID ")
                        .append(" left join Department d2 on d2.DepartmentID = so.DepartmentID ").append(" where " + tableName + "ID = '").append(id).append("'");
            } else {
                sb.append(" select isnull(d1.Customer,'') customer,  ").append("  isnull(d2.Department,'') department,so.quantitySum,so.amountSum,so.date,so.no,so.audit, so.type from " + tableName + " so  ").append(" left join Customer d1 on d1.CustomerID = so.CustomerID ")
                        .append(" left join Department d2 on d2.DepartmentID = so.DepartmentID ").append(" where " + tableName + "ID = '").append(id).append("'");
            }
            List list = commonDao.findForJdbc(sb.toString());
            if (list.size() > 0) {
                Map map = (Map) list.get(0);
                sb = new StringBuffer();
                sb.append(" select g.name ,c.color, ").append(" detail.Quantity quantity,boxQty").append(getSizeStr(maxSize)).append(",br.brand, g.Code code,");
                if ("StockMove".equalsIgnoreCase(tableName) || "Stock".equalsIgnoreCase(tableName)) {
                    sb.append("isnull(UnitPrice,0) price,");
                } else {
                    sb.append("(case when isnull(DiscountPrice,0)=0 then isnull(UnitPrice,0) else isnull(DiscountPrice,0) end) price,");
                }
                sb.append("isnull(detail.amount,0) amount  ").append("  from " + tableName + "DetailTemp detail ").append(" left join Goods g on g.GoodsID = detail.GoodsID ").append(" left join Color c on c.ColorID = detail.ColorID ")
                        .append(" left join GoodsType gt on gt.GoodsTypeID = g.GoodsTypeID ").append(" left join Brand br on br.BrandId = g.BrandId ").append("  where detail." + tableName + "ID = '").append(id).append("'");
                List detailList = commonDao.findForJdbc(sb.toString());
                String displaySizeGroup = (String) commonDao.getData(" select displaysizegroup from " + tableName + " where " + tableName + "ID = '" + id + "' ");
                List sizeTitle = getSizeTitle(displaySizeGroup, maxSize);
                map.put("details", detailList);
                map.put("docType", docType);
                map.put("shop", shop);
                map.put("size", detailList.size());
                map.put("page", (int) Math.ceil(detailList.size() / 15.0));
                map.put("userName", userName);
                map.put("printIp", printIp);
                map.put("printPort", printPort);
                map.put("tableName", tableName);
                map.put("maxSize", maxSize);
                map.put("sizeTitle", sizeTitle);
                // 末尾固定显示设置
                map.put("address", LoadUserCount.address);
                map.put("phone", LoadUserCount.phone);
                map.put("mobile", LoadUserCount.mobile);
                map.put("bankTypeOne", LoadUserCount.bankTypeOne);
                map.put("bankOneNo", LoadUserCount.bankOneNo);
                map.put("bankCardOneName", LoadUserCount.bankCardOneName);
                map.put("bankTypeTwo", LoadUserCount.bankTypeTwo);
                map.put("bankTwoNo", LoadUserCount.bankTwoNo);
                map.put("bankCardTwoName", LoadUserCount.bankCardTwoName);
                // 转发
                req.setAttribute("datas", map);
                req.getRequestDispatcher("context/print.jsp").forward(req, resp);
            }
        } catch (Exception e) {
            SysLogger.error(e.getMessage(), e);
        }
    }

    /**
     * 打印单据前获取单据显示数据方法(歌天丽客户打印格式)
     * 
     * @param req
     * @return
     */
    @RequestMapping(params = "printGtl")
    @ResponseBody
    public synchronized void printGtl(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        try {
            String id = oConvertUtils.getString(req.getParameter("id"));
            String tableName = oConvertUtils.getString(req.getParameter("tableName"));
            String userName = oConvertUtils.getString(req.getParameter("userName"));
            String printIp = oConvertUtils.getString(req.getParameter("printIp"));
            String printPort = oConvertUtils.getString(req.getParameter("printPort"));
            String docType = oConvertUtils.getString(req.getParameter("docType"));
            String shop = null;
            try {
                shop = new String(ResourceUtil.getConfigByName("shopTitle").getBytes("iso-8859-1"), "UTF-8");
                if (shop == null || "".equals(shop) || shop.isEmpty()) {
                    shop = LoadUserCount.regName;
                }
            } catch (Exception e) {
                System.out.println(e);
                if (shop == null || "".equals(shop) || shop.isEmpty()) {
                    shop = LoadUserCount.regName;
                }
            }
            int maxSize = getMaxSize(id, tableName);
            StringBuffer sb = new StringBuffer();
            // 采购
            if ("Purchase".equals(tableName)) {
                sb.append(" select isnull(d1.Supplier,'') customer,  ")
                        .append("  isnull(d2.Department,'') department,isnull(d3.PaymentType,'') paymentType,isnull(d4.Name,'') employee,so.quantitySum,so.amountSum,so.retailAmountSum,so.date,so.no,so.audit,so.memo,(select Brand from brand where brandID = so.brandId) brand,so.type from "
                                + tableName + " so  ").append(" left join Supplier d1 on d1.SupplierID = so.SupplierID ").append(" left join Department d2 on d2.DepartmentID = so.DepartmentID ").append(" left join PaymentType d3 on d3.PaymentTypeID = so.PaymentTypeID ")
                        .append(" left join Employee d4 on d4.EmployeeID = so.EmployeeID ").append(" where " + tableName + "ID = '").append(id).append("'");
            } else if ("StockMove".equals(tableName)) {// 转仓单
                sb.append(" select isnull(d1.Customer,'') customer,  ")
                        .append("  isnull(d2.Department,'') department,isnull(d3.PaymentType,'') paymentType,isnull(d4.Name,'') employee,so.quantitySum,so.amountSum,so.retailAmountSum,so.date,so.no,so.memo,(select Brand from brand where brandID = so.brandId) brand,so.audit from " + tableName
                                + " so  ").append(" left join Customer d1 on d1.CustomerID = so.CustomerID ").append(" left join Department d2 on d2.DepartmentID = so.DepartmentID ").append(" left join PaymentType d3 on d3.PaymentTypeID = so.PaymentTypeID ")
                        .append(" left join Employee d4 on d4.EmployeeID = so.EmployeeID ").append(" where " + tableName + "ID = '").append(id).append("'");
            } else {
                sb.append(" select isnull(d1.Customer,'') customer,  ")
                        .append("  isnull(d2.Department,'') department,isnull(d3.PaymentType,'') paymentType,isnull(d4.Name,'') employee,so.quantitySum,so.amountSum,so.retailAmountSum,so.date,so.no,so.memo,(select Brand from brand where brandID = so.brandId) brand,so.audit, so.type from "
                                + tableName + " so  ").append(" left join Customer d1 on d1.CustomerID = so.CustomerID ");
                if ("SalesOrder".equals(tableName)) {// 销售订单
                    sb.append(" left join Department d2 on d2.DepartmentID = so.WarehouseId ");
                } else {
                    sb.append(" left join Department d2 on d2.DepartmentID = so.DepartmentID ");
                }
                sb.append(" left join PaymentType d3 on d3.PaymentTypeID = so.PaymentTypeID ").append(" left join Employee d4 on d4.EmployeeID = so.EmployeeID ").append(" where " + tableName + "ID = '").append(id).append("'");
            }
            List list = commonDao.findForJdbc(sb.toString());
            if (list.size() > 0) {
                Map map = (Map) list.get(0);
                sb = new StringBuffer();
                sb.append(" select g.name ,c.color, ").append(" detail.Quantity quantity,boxQty").append(getSizeStr(maxSize)).append(",br.brand, g.Code code,detail.memo,");
                if ("StockMove".equalsIgnoreCase(tableName) || "Stock".equalsIgnoreCase(tableName)) {
                    sb.append("isnull(UnitPrice,0) price,");
                } else {
                    sb.append("(case when isnull(DiscountPrice,0)=0 then isnull(UnitPrice,0) else isnull(DiscountPrice,0) end) price,");
                }
                sb.append("isnull(detail.amount,0) amount,isnull(detail.retailAmount,0) retailAmount,detail.discountRate,g.model ").append("  from " + tableName + "DetailTemp detail ").append(" left join Goods g on g.GoodsID = detail.GoodsID ")
                        .append(" left join Color c on c.ColorID = detail.ColorID ").append(" left join GoodsType gt on gt.GoodsTypeID = g.GoodsTypeID ").append(" left join Brand br on br.BrandId = g.BrandId ").append("  where detail." + tableName + "ID = '").append(id).append("'");
                List detailList = commonDao.findForJdbc(sb.toString());
                String displaySizeGroup = (String) commonDao.getData(" select displaysizegroup from " + tableName + " where " + tableName + "ID = '" + id + "' ");
                List sizeTitle = getSizeTitle(displaySizeGroup, maxSize);
                map.put("details", detailList);
                map.put("docType", docType);
                map.put("shop", shop);
                map.put("size", detailList.size());
                map.put("page", (int) Math.ceil(detailList.size() / 37.0));
                map.put("userName", userName);
                map.put("printIp", printIp);
                map.put("printPort", printPort);
                map.put("tableName", tableName);
                map.put("maxSize", maxSize);
                map.put("sizeTitle", sizeTitle);
                // 末尾固定显示设置
                map.put("address", LoadUserCount.address);
                map.put("phone", LoadUserCount.phone);
                map.put("mobile", LoadUserCount.mobile);
                map.put("bankTypeOne", LoadUserCount.bankTypeOne);
                map.put("bankOneNo", LoadUserCount.bankOneNo);
                map.put("bankCardOneName", LoadUserCount.bankCardOneName);
                map.put("bankTypeTwo", LoadUserCount.bankTypeTwo);
                map.put("bankTwoNo", LoadUserCount.bankTwoNo);
                map.put("bankCardTwoName", LoadUserCount.bankCardTwoName);
                // 转发
                req.setAttribute("datas", map);
                req.getRequestDispatcher("context/print-gtl.jsp").forward(req, resp);
            }
        } catch (Exception e) {
            SysLogger.error(e.getMessage(), e);
        }
    }

    /**
     * 获取打印机列表
     * 
     * @param req
     * @param resp
     * @throws Exception
     */
    @RequestMapping(params = "getPrinterList")
    @ResponseBody
    public synchronized void getPrinterList(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        AjaxJson j = new AjaxJson();
        j.setAttributes(new HashMap<String, Object>());
        try {
            Map<String, String> map = new HashMap<String, String>();
            String printIp = oConvertUtils.getString(req.getParameter("printIp"));
            String printPort = oConvertUtils.getString(req.getParameter("printPort"));
            map.put("printIp", printIp);
            map.put("printPort", printPort);
            // 转发
            req.setAttribute("datas", map);
            req.getRequestDispatcher("context/get-printer-list.jsp").forward(req, resp);
        } catch (Exception e) {
            SysLogger.error(e.getMessage(), e);
        }
    }

    /**
     * 装箱单打印格式[显示单价和金额]
     * 
     * @param req
     * @return
     */
    @RequestMapping(params = "packingBoxPrintHasPrice")
    @ResponseBody
    public synchronized void packingBoxPrintHasPrice(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        AjaxJson j = new AjaxJson();
        j.setAttributes(new HashMap<String, Object>());
        try {
            Map map = null;
            String id = oConvertUtils.getString(req.getParameter("id"));
            String tableName = oConvertUtils.getString(req.getParameter("tableName"));// 关联的单据类别
            String userName = oConvertUtils.getString(req.getParameter("userName"));
            String printIp = oConvertUtils.getString(req.getParameter("printIp"));
            String printPort = oConvertUtils.getString(req.getParameter("printPort"));
            String docType = oConvertUtils.getString(req.getParameter("docType"));
            String printer = oConvertUtils.getString(req.getParameter("printer"));
            StringBuffer sb = new StringBuffer();
            sb.append(" select no,(select customer from customer c where c.customerId = p.customerId) customer,customerId,date,relationNo,memo,quantitySum,(select count(distinct boxNo) from packingboxdetailpda where packingBoxId = '" + id
                    + "') boxCount,(select sum(retailAmount) from packingBoxdetailPDA where packingBoxId = '" + id + "') retailAmountSum from packingBox p where packingBoxId = ? ");
            List list = commonDao.findForJdbc(sb.toString(), id);
            if (list.size() > 0) {
                map = (Map) list.get(0);
                sb = new StringBuffer();
                sb.append(" select boxNo,(select code from goods where goodsId = p.goodsId) goodsCode,(select name from goods where goodsId = p.goodsId) goodsName,sum(quantity) quantity,"
                        + "isnull(retailSales,0) retailSales,isnull(retailAmount,0) retailAmount from packingBoxdetailPDA p where packingBoxId = ? group by goodsId,boxNo,retailSales,retailAmount having isnull(Sum(quantity),0) <> 0 order by boxNo ");
                List detailList = commonDao.findForJdbc(sb.toString(), id);
                if (detailList.size() < 1) {// 没有数据时不执行打印
                    SysLogger.error("无打印数据 ==> packingBoxPrintHasPrice");
                } else {
                    map.put("details", detailList);
                    map.put("printIp", printIp);
                    map.put("docType", docType);
                    map.put("userName", userName);
                    map.put("tableName", tableName);
                    map.put("printPort", printPort);
                    map.put("printer", printer);
                    // 转发
                    req.setAttribute("datas", map);
                    req.getRequestDispatcher("context/packing-box-print-hasprice.jsp").forward(req, resp);
                }
            }
        } catch (Exception e) {
            SysLogger.error(e.getMessage(), e);
        }
    }

    /**
     * 装箱单打印格式[显示单价和金额] 明细
     * 
     * @param req
     * @return
     */
    @RequestMapping(params = "packingBoxDetailPrintHasPrice")
    @ResponseBody
    public synchronized void packingBoxDetailPrintHasPrice(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        AjaxJson j = new AjaxJson();
        j.setAttributes(new HashMap<String, Object>());
        try {
            Map map = null;
            String id = oConvertUtils.getString(req.getParameter("id"));
            String tableName = oConvertUtils.getString(req.getParameter("tableName"));// 关联的单据类别
            String userName = oConvertUtils.getString(req.getParameter("userName"));
            String printIp = oConvertUtils.getString(req.getParameter("printIp"));
            String printPort = oConvertUtils.getString(req.getParameter("printPort"));
            String docType = oConvertUtils.getString(req.getParameter("docType"));
            String boxNo = oConvertUtils.getString(req.getParameter("boxNo"));
            String printer = oConvertUtils.getString(req.getParameter("printer"));
            StringBuffer sb = new StringBuffer();
            sb.append(" select no,(select customer from customer c where c.customerId = p.customerId) customer,customerId,date,relationNo,memo,(select sum(Quantity) from packingBoxdetailPDA where packingBoxId = '" + id + "' and boxNo = '" + boxNo
                    + "') quantitySum,(select sum(retailAmount) from packingBoxdetailPDA where packingBoxId = '" + id + "' and boxNo = '" + boxNo + "') retailAmountSum from packingBox p where packingBoxId = ? ");
            List list = commonDao.findForJdbc(sb.toString(), id);
            if (list.size() > 0) {
                map = (Map) list.get(0);
                sb = new StringBuffer();
                sb.append(" select boxNo,(select code from goods where goodsId = p.goodsId) goodsCode,(select name from goods where goodsId = p.goodsId) goodsName,sum(quantity) quantity,"
                        + "isnull(retailSales,0) retailSales,isnull(retailAmount,0) retailAmount from packingBoxdetailPDA p where packingBoxId = ? and boxNo = ? group by goodsId,boxNo,retailSales,retailAmount having isnull(Sum(quantity),0) <> 0 ");
                List detailList = commonDao.findForJdbc(sb.toString(), id, boxNo);
                if (detailList.size() < 1) {// 没有数据时不执行打印
                    SysLogger.error("无打印数据 ==> packingBoxDetailPrintHasPrice");
                } else {
                    map.put("details", detailList);
                    map.put("printIp", printIp);
                    map.put("docType", docType);
                    map.put("userName", userName);
                    map.put("tableName", tableName);
                    map.put("printPort", printPort);
                    map.put("printer", printer);
                    map.put("boxNo", boxNo);
                    // 转发
                    req.setAttribute("datas", map);
                    req.getRequestDispatcher("context/packing-box-detail-print-hasprice.jsp").forward(req, resp);
                }
            }
        } catch (Exception e) {
            SysLogger.error(e.getMessage(), e);
        }
    }

    /**
     * 装箱单打印格式[显示单价和金额]
     * 
     * @param req
     * @return
     */
    @RequestMapping(params = "packingBoxPrintNoPrice")
    @ResponseBody
    public synchronized void packingBoxPrint(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        AjaxJson j = new AjaxJson();
        j.setAttributes(new HashMap<String, Object>());
        try {
            Map map = null;
            String id = oConvertUtils.getString(req.getParameter("id"));
            String tableName = oConvertUtils.getString(req.getParameter("tableName"));// 关联的单据类别
            String userName = oConvertUtils.getString(req.getParameter("userName"));
            String printIp = oConvertUtils.getString(req.getParameter("printIp"));
            String printPort = oConvertUtils.getString(req.getParameter("printPort"));
            String docType = oConvertUtils.getString(req.getParameter("docType"));
            String boxNo = oConvertUtils.getString(req.getParameter("boxNo"));
            String printer = oConvertUtils.getString(req.getParameter("printer"));
            StringBuffer sb = new StringBuffer();
            sb.append(" select no,(select customer from customer c where c.customerId = p.customerId) customer,customerId,date,relationNo,memo,quantitySum,(select count(distinct boxNo) from packingboxdetailpda where packingBoxId = '" + id
                    + "') boxCount,(select sum(retailAmount) from packingBoxdetailPDA where packingBoxId = '" + id + "') retailAmountSum from packingBox p where packingBoxId = ? ");
            List list = commonDao.findForJdbc(sb.toString(), id);
            if (list.size() > 0) {
                map = (Map) list.get(0);
                sb = new StringBuffer();
                sb.append(" select boxNo,(select code from goods where goodsId = p.goodsId) goodsCode,(select name from goods where goodsId = p.goodsId) goodsName,sum(quantity) quantity,"
                        + "isnull(retailSales,0) retailSales,isnull(retailAmount,0) retailAmount from packingBoxdetailPDA p where packingBoxId = ? group by goodsId,boxNo,retailSales,retailAmount having isnull(Sum(quantity),0) <> 0 order by boxNo ");
                List detailList = commonDao.findForJdbc(sb.toString(), id);
                if (detailList.size() < 1) {// 没有数据时不执行打印
                    SysLogger.error("无打印数据 ==> packingBoxPrintNoPrice");
                } else {
                    map.put("details", detailList);
                    map.put("printIp", printIp);
                    map.put("docType", docType);
                    map.put("userName", userName);
                    map.put("tableName", tableName);
                    map.put("printPort", printPort);
                    map.put("printer", printer);
                    // 转发
                    req.setAttribute("datas", map);
                    req.getRequestDispatcher("context/packing-box-print-noprice.jsp").forward(req, resp);
                }
            }
        } catch (Exception e) {
            SysLogger.error(e.getMessage(), e);
        }
    }

    /**
     * 装箱单打印格式[显示单价和金额] 明细
     * 
     * @param req
     * @return
     */
    @RequestMapping(params = "packingBoxDetailPrintNoPrice")
    @ResponseBody
    public synchronized void packingBoxDetailPrintNoPrice(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        AjaxJson j = new AjaxJson();
        j.setAttributes(new HashMap<String, Object>());
        try {
            Map map = null;
            String id = oConvertUtils.getString(req.getParameter("id"));
            String tableName = oConvertUtils.getString(req.getParameter("tableName"));// 关联的单据类别
            String userName = oConvertUtils.getString(req.getParameter("userName"));
            String printIp = oConvertUtils.getString(req.getParameter("printIp"));
            String printPort = oConvertUtils.getString(req.getParameter("printPort"));
            String docType = oConvertUtils.getString(req.getParameter("docType"));
            String boxNo = oConvertUtils.getString(req.getParameter("boxNo"));
            String printer = oConvertUtils.getString(req.getParameter("printer"));
            StringBuffer sb = new StringBuffer();
            sb.append(" select no,(select customer from customer c where c.customerId = p.customerId) customer,customerId,date,relationNo,memo,(select sum(Quantity) from packingBoxdetailPDA where packingBoxId = '" + id + "' and boxNo = '" + boxNo
                    + "') quantitySum,(select sum(retailAmount) from packingBoxdetailPDA where packingBoxId = '" + id + "' and boxNo = '" + boxNo + "') retailAmountSum from packingBox p where packingBoxId = ? ");
            List list = commonDao.findForJdbc(sb.toString(), id);
            if (list.size() > 0) {
                map = (Map) list.get(0);
                sb = new StringBuffer();
                sb.append(" select boxNo,(select code from goods where goodsId = p.goodsId) goodsCode,(select name from goods where goodsId = p.goodsId) goodsName,sum(quantity) quantity,"
                        + "isnull(retailSales,0) retailSales,isnull(retailAmount,0) retailAmount from packingBoxdetailPDA p where packingBoxId = ? and boxNo = ? group by goodsId,boxNo,retailSales,retailAmount having isnull(Sum(quantity),0) <> 0 ");
                List detailList = commonDao.findForJdbc(sb.toString(), id, boxNo);
                if (detailList.size() < 1) {// 没有数据时不执行打印
                    SysLogger.error("无打印数据 ==> packingBoxDetailPrintNoPrice");
                } else {
                    map.put("details", detailList);
                    map.put("printIp", printIp);
                    map.put("docType", docType);
                    map.put("userName", userName);
                    map.put("tableName", tableName);
                    map.put("printPort", printPort);
                    map.put("printer", printer);
                    map.put("boxNo", boxNo);
                    // 转发
                    req.setAttribute("datas", map);
                    req.getRequestDispatcher("context/packing-box-detail-print-noprice.jsp").forward(req, resp);
                }
            }
        } catch (Exception e) {
            SysLogger.error(e.getMessage(), e);
        }
    }

    /**
     * 装箱单打印格式(唯品会装箱单)
     * 
     * @param req
     * @return
     */
    @RequestMapping(params = "packingBoxPrintVip")
    @ResponseBody
    public synchronized void packingBoxPrintVip(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        AjaxJson j = new AjaxJson();
        j.setAttributes(new HashMap<String, Object>());
        try {
            String id = oConvertUtils.getString(req.getParameter("id"));
            String tableName = oConvertUtils.getString(req.getParameter("tableName"));// 关联的单据类别
            String userName = oConvertUtils.getString(req.getParameter("userName"));
            String printIp = oConvertUtils.getString(req.getParameter("printIp"));
            String printPort = oConvertUtils.getString(req.getParameter("printPort"));
            String docType = oConvertUtils.getString(req.getParameter("docType"));
            String customer = oConvertUtils.getString(req.getParameter("customer"));
            String boxNo = oConvertUtils.getString(req.getParameter("boxNo"));
            String printer = oConvertUtils.getString(req.getParameter("printer"));
            String warehouse = "前往唯品会" + customer + "";// 物流仓库名称
            StringBuffer sb = new StringBuffer();
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("printIp", printIp);
            map.put("docType", docType);
            map.put("userName", userName);
            map.put("tableName", tableName);
            map.put("warehouse", warehouse);
            map.put("printPort", printPort);
            map.put("printer", printer);
            map.put("quantitySum", commonDao.getDataToInt(" select sum(quantity) from packingBoxdetailPDA p where packingBoxId = ? ", id));
            map.put("boxCount", commonDao.getDataToInt(" select count(distinct boxNo) from packingboxdetailpda where packingBoxId = ? ", id));
            sb.append(" select boxNo,(select code from goods where goodsId = p.goodsId) goodsCode,(select name from goods where goodsId = p.goodsId) goodsName,sum(quantity) quantity,"
                    + "(select size from size where sizeId = p.sizeId) size,(select StandardCode from goods where goodsId = p.goodsId) barcode " + " from packingBoxdetailPDA p where packingBoxId = ? group by boxNo,goodsId,colorId,sizeId having isnull(Sum(quantity),0) <> 0 order by boxNo ");
            List detailList = commonDao.findForJdbc(sb.toString(), id);
            if (detailList.size() < 1) {// 没有数据时不执行打印
                SysLogger.error("无打印数据 ==> packingBoxPrintVip");
            } else {
                map.put("dataList", detailList);
                // 转发
                req.setAttribute("datas", map);
                req.getRequestDispatcher("context/packing-box-print-vip.jsp").forward(req, resp);
            }
        } catch (Exception e) {
            SysLogger.error(e.getMessage(), e);
        }
    }

    /**
     * 装箱单打印格式(唯品会装箱单) 明细
     * 
     * @param req
     * @return
     */
    @RequestMapping(params = "packingBoxDetailPrintVip")
    @ResponseBody
    public synchronized void packingBoxDetailPrintVip(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        AjaxJson j = new AjaxJson();
        j.setAttributes(new HashMap<String, Object>());
        try {
            String id = oConvertUtils.getString(req.getParameter("id"));
            String tableName = oConvertUtils.getString(req.getParameter("tableName"));// 关联的单据类别
            String userName = oConvertUtils.getString(req.getParameter("userName"));
            String printIp = oConvertUtils.getString(req.getParameter("printIp"));
            String printPort = oConvertUtils.getString(req.getParameter("printPort"));
            String docType = oConvertUtils.getString(req.getParameter("docType"));
            String customer = oConvertUtils.getString(req.getParameter("customer"));
            String boxNo = oConvertUtils.getString(req.getParameter("boxNo"));
            String printer = oConvertUtils.getString(req.getParameter("printer"));
            String warehouse = "前往唯品会" + customer + "";// 物流仓库名称
            StringBuffer sb = new StringBuffer();
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("printIp", printIp);
            map.put("docType", docType);
            map.put("userName", userName);
            map.put("tableName", tableName);
            map.put("warehouse", warehouse);
            map.put("boxNo", boxNo);
            map.put("printPort", printPort);
            map.put("printer", printer);
            map.put("quantitySum", commonDao.getDataToInt(" select sum(quantity) from packingBoxdetailPDA p where packingBoxId = ? and boxNo = ? ", id, boxNo));
            sb.append(" select boxNo,(select code from goods where goodsId = p.goodsId) goodsCode,(select name from goods where goodsId = p.goodsId) goodsName,sum(quantity) quantity,"
                    + "(select size from size where sizeId = p.sizeId) size,(select StandardCode from goods where goodsId = p.goodsId) barcode " + " from packingBoxdetailPDA p where packingBoxId = ? and boxNo = ? group by boxNo,goodsId,colorId,sizeId having isnull(Sum(quantity),0) <> 0 ");
            List detailList = commonDao.findForJdbc(sb.toString(), id, boxNo);
            if (detailList.size() < 1) {// 没有数据时不执行打印
                SysLogger.error("无打印数据 ==> packingBoxDetailPrintVip");
            } else {
                map.put("dataList", detailList);
                // 转发
                req.setAttribute("datas", map);
                req.getRequestDispatcher("context/packing-box-detail-print-vip.jsp").forward(req, resp);
            }
        } catch (Exception e) {
            SysLogger.error(e.getMessage(), e);
        }
    }

    /**
     * 图片上传方法
     * 
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    @SuppressWarnings("deprecation")
    @RequestMapping(params = "uploadImages")
    @ResponseBody
    public synchronized AjaxJson uploadImages(HttpServletRequest request, HttpServletResponse response) throws Exception {
        AjaxJson j = new AjaxJson();
        String url=null;
        try {
        	//System.out.println("上传图片时带过来的salesID:"+request.getParameter("SalesID"));
        	
        	//System.out.println("request对象："+request.toString());
        	String SalesID =request.getParameter("SalesID");
            request.setCharacterEncoding("UTF-8");
            response.setCharacterEncoding("UTF-8");
            
            
            // String temp =
            // request.getSession().getServletContext().getRealPath("/") +
            // "images"; // 临时目录
            // System.out.println("images=" + temp);
            String path = ResourceUtil.getConfigByName("imgPath");
            String loadpath = path; // 上传文件存放目录
            System.out.println("loadpath=" + loadpath);
            DiskFileUpload fu = new DiskFileUpload();
            fu.setSizeMax(1024 * 1024 * 1024); // 设置允许用户上传文件大小,单位:字节
            fu.setSizeThreshold(40960000); // 设置最多只允许在内存中存储的数据,单位:字节40960
            // fu.setRepositoryPath(temp); //
            // 设置一旦文件大小超过getSizeThreshold()的值时数据存放在硬盘的目录
            // 开始读取上传信息
            List fileItems = null;
            try {
                fileItems = fu.parseRequest(request);
                System.out.println("fileItems=" + fileItems);
            } catch (Exception e) {
                e.printStackTrace();
                throw new BusinessException("图片上传失败");
            }
            Iterator iter = fileItems.iterator(); // 依次处理每个上传的文件
            while (iter.hasNext()) {
                FileItem item = (FileItem) iter.next();// 忽略其他不是文件域的所有表单信息
                if (!item.isFormField()) {
                    String name = item.getName();// 获取上传文件名,包括路径
                    name = name.substring(name.lastIndexOf("\\") + 1);// 从全路径中提取文件名
                    long size = item.getSize();
                    if ((name == null || name.equals("")) && size == 0)
                        continue;
                    // 判断目录是否存在
                    File file = new File(loadpath);
                    if (!file.exists()) {
                        file.mkdirs();
                    }
                    // 写入文件
                    File fNew = new File(loadpath, name);
                    try {
                        item.write(fNew);  //name.substring(name.indexOf("."), name.length()) 拿后缀名
                        renameFile(loadpath,name,SalesID+name.substring(name.indexOf("."), name.length())); //把文件 名改为传过来的名字，本地存在就删除，再命名
                        url =SalesID+name.substring(name.indexOf("."), name.length());
                        // 生成指定大小的图片
                        // zoomImage(fNew.getAbsolutePath(),
                        // fNew.getAbsolutePath(), 480, 800);
                        
                    } catch (Exception e) {
                        e.printStackTrace();
                        throw new BusinessException("图片上传失败");
                    }
                }
            }
            //j.setObj(fileItems);
            String path1 = request.getContextPath();
            String basePath = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+path1+"/";
            
            url=basePath+url;
            
            j.setObj(url);
            j.setMsg("上传成功");
            } catch (Exception e) {
            j.setSuccess(false);
            j.setMsg(e.getMessage());
            SysLogger.error(e.getMessage(), e);
        }
        return j;
    }
    
    /** *//**文件重命名 
     * @param path 文件目录 
     * @param oldname  原来的文件名 
     * @param newname 新文件名 
     */ 
     public void renameFile(String path,String oldname,String newname){ 
         if(!oldname.equals(newname)){//新的文件名和以前文件名不同时,才有必要进行重命名 
             File oldfile=new File(path+"/"+oldname); 
             File newfile=new File(path+"/"+newname); 
             if(!oldfile.exists()){
                 return;//重命名文件不存在
             }
             if(newfile.exists())//若在该目录下已经有一个文件和新文件名相同，则不允许重命名 
              //   System.out.println(newname+"已经存在！"); 
             {
            	 newfile.delete(); //先删除旧的
            	 oldfile.renameTo(newfile);
             }
             
             else{ 
                 oldfile.renameTo(newfile); 
             } 
         }else{
             System.out.println("新文件名和旧文件名相同...");
         }
     }

    /**
     * 模糊查询仓库信息(获取仓库)
     * 
     * @param req
     * @return
     */
    @RequestMapping(params = "getWarehouse")
    @ResponseBody
    public AjaxJson getWarehouse(HttpServletRequest req) {
        AjaxJson j = new AjaxJson();
        j.setAttributes(new HashMap<String, Object>());
        Client client = ResourceUtil.getClientFromSession(req);
        Map<String, String> map = client.getMap();
        String userRight = map.get(client.getUserID());
        try {
            StringBuffer sb = new StringBuffer();
            sb.append(" select Department Name,DepartmentID,MustExistsGoodsFlag,Code DepartmentCode from Department where WarehouseFlag = '1' and DepartmentID in (").append(userRight).append(") ");
            List list = commonDao.findForJdbc(sb.toString());
            j.setObj(list);
        } catch (Exception e) {
            j.setSuccess(false);
            j.setMsg(e.getMessage());
            SysLogger.error(e.getMessage(), e);
        }
        return j;
    }

    /**
     * 生成Excel条码校验差异文件
     * 
     * @param req
     * @return
     */
    @RequestMapping(params = "generateExcel")
    @ResponseBody
    public synchronized AjaxJson generateExcel(HttpServletRequest req) {
        AjaxJson j = new AjaxJson();
        j.setAttributes(new HashMap<String, Object>());
        try {
            // 输出路径
            String no = oConvertUtils.getString(req.getParameter("no"));
            String path = new String(ResourceUtil.getConfigByName("exportPath").getBytes("iso-8859-1"), "UTF-8");
            File direct = new File(path);
            if (!direct.exists()) {
                direct.mkdirs();
            }
            path = path + File.separator + no + "校验差异.xls";
            File file = new File(path);
            if (!file.exists()) {
                file.createNewFile();
            }
            // 获取数据
            String jsonStr = oConvertUtils.getString(req.getParameter("listTitle"));
            JSONArray data = JSONArray.fromObject(jsonStr);
            List<String> listTitle = JSONArray.toList(data, String.class);
            String jsonStrs = oConvertUtils.getString(req.getParameter("objs"));
            JSONArray datas = JSONArray.fromObject(jsonStrs);
            // 转换List<Object[]>
            List<Object[]> listContent = new ArrayList<Object[]>();
            List objs = JSONArray.toList(datas, Object.class);
            for (Object obj : objs) {
                ArrayList<Object> list = (ArrayList<Object>) obj;
                Object[] o = (Object[]) list.toArray();
                listContent.add(o);
            }
            ExcelUtil.exportExcel(path, LoadUserCount.corpName, listTitle, listContent);
            j.setObj(path);
        } catch (Exception e) {
            j.setSuccess(false);
            j.setMsg(e.getMessage());
            SysLogger.error(e.getMessage(), e);
        }
        return j;
    }

    /**
     * 生成离线盘点文件
     * 
     * @param req
     * @return
     */
    @RequestMapping(params = "generateText")
    @ResponseBody
    public synchronized AjaxJson generateText(HttpServletRequest req) {
        AjaxJson j = new AjaxJson();
        j.setAttributes(new HashMap<String, Object>());
        try {
            // 输出路径
            String departmentCode = oConvertUtils.getString(req.getParameter("departmentCode"));
            String shelvesNo = oConvertUtils.getString(req.getParameter("shelvesNo"));
            String quantitySum = oConvertUtils.getString(req.getParameter("quantitySum"));
            String path = new String(ResourceUtil.getConfigByName("exportPath").getBytes("iso-8859-1"), "UTF-8");
            File direct = new File(path);
            if (!direct.exists()) {
                direct.mkdirs();
            }
            path = path + File.separator + departmentCode + "_" + shelvesNo + "_" + quantitySum + ".txt";
            String jsonStr = oConvertUtils.getString(req.getParameter("dataList"));
            JSONArray datas = JSONArray.fromObject(jsonStr);
            List<Map<String, Object>> dataList = JSONArray.toList(datas, Map.class);
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < dataList.size(); i++) {
                Map<String, Object> map = dataList.get(i);
                String barcode = String.valueOf(map.get("Barcode"));
                int quantity = Integer.parseInt(String.valueOf(map.get("Quantity")));
                sb.append(barcode + "," + quantity + "\n");
            }
            MyTools.contentToTxt(path, sb.toString());
            j.setObj(path);
        } catch (Exception e) {
            j.setSuccess(false);
            j.setMsg(e.getMessage());
            SysLogger.error(e.getMessage(), e);
        }
        return j;
    }

    /**
     * 生成离线盘点文件(合并)
     * 
     * @param req
     * @return
     */
    @RequestMapping(params = "generateMulitText")
    @ResponseBody
    public synchronized AjaxJson generateMulitText(HttpServletRequest req) {
        AjaxJson j = new AjaxJson();
        j.setAttributes(new HashMap<String, Object>());
        try {
            // 输出路径
            String departmentCode = oConvertUtils.getString(req.getParameter("departmentCode"));
            String quantitySum = oConvertUtils.getString(req.getParameter("quantitySum"));
            String path = new String(ResourceUtil.getConfigByName("exportPath").getBytes("iso-8859-1"), "UTF-8");
            File direct = new File(path);
            if (!direct.exists()) {
                direct.mkdirs();
            }
            path = path + File.separator + departmentCode + "_" + MyTools.generateCurrentTimeCode() + "_" + quantitySum + ".txt";
            String jsonStr = oConvertUtils.getString(req.getParameter("dataList"));
            JSONArray datas = JSONArray.fromObject(jsonStr);
            List<Map<String, Object>> dataList = JSONArray.toList(datas, Map.class);
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < dataList.size(); i++) {
                Map<String, Object> map = dataList.get(i);
                String barcode = String.valueOf(map.get("Barcode"));
                int quantity = Integer.parseInt(String.valueOf(map.get("Quantity")));
                sb.append(barcode + "," + quantity + "\n");
            }
            MyTools.contentToTxt(path, sb.toString());
            j.setObj(path);
        } catch (Exception e) {
            j.setSuccess(false);
            j.setMsg(e.getMessage());
            SysLogger.error(e.getMessage(), e);
        }
        return j;
    }

    /**
     * 生成Excel条码校验差异文件
     * 
     * @param req
     * @return
     */
    @RequestMapping(params = "getData")
    @ResponseBody
    public synchronized String getData(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        String jsonStr = null;
        try {
            // 输出路径
            jsonStr = oConvertUtils.getString(req.getParameter("data"));
            // JSONArray datas = JSONArray.fromObject(jsonStr);
            // List<Map<String, Object>> dataList = JSONArray.toList(datas, Map.class);
            // StringBuffer sb = new StringBuffer();
            // for (int i = 0; i < dataList.size(); i++) {
            // Map<String,Object> map = dataList.get(i);
            // String barcode = String.valueOf(map.get("Barcode"));
            // int quantity = Integer.parseInt(String.valueOf(map.get("Quantity")));
            // sb.append(barcode+","+quantity+"\n");
            // }
        } catch (Exception e) {
            // j.setSuccess(false);
            // j.setMsg(e.getMessage());
            SysLogger.error(e.getMessage(), e);
        }
        return jsonStr;
    }
    
    
    @RequestMapping(params = "testwebview")
   public void testwebview(HttpServletRequest req, HttpServletResponse resp) {
    	try{
    		String test = oConvertUtils.getString(req.getParameter("test"));
    		
    		req.setAttribute("test", test);
            //req.setAttribute("sizeList", sizeList);
            req.getRequestDispatcher("context/test.jsp").forward(req, resp);
    	}catch(Exception e){
    		e.printStackTrace();
    	}
    	
    	
    }
    
    //用于wx的方法，返回货品颜色与尺码列表 测试
    //http://192.168.1.105:8080/FPOS/common.do?getColorAndSize&GoodsID=00980&DeptID=007&onLineId=0000-0000&userId=1
    @RequestMapping(params="getColorAndSize")
    @ResponseBody
    public AjaxJson getColorAndSize(HttpServletRequest req){
    	AjaxJson j=new AjaxJson();
    	 String goodsId = oConvertUtils.getString(req.getParameter("GoodsID"));
        // String tableName = oConvertUtils.getString(req.getParameter("tableName"));
         String warehouseId = oConvertUtils.getString(req.getParameter("DeptID"));
         String onLineId = oConvertUtils.getString(req.getParameter("onLineId"));
         String userId = oConvertUtils.getString(req.getParameter("userId"));
     
         System.out.println(goodsId);
         
         if (goodsId != null && !"".equals(goodsId) && !"null".equalsIgnoreCase(goodsId)) {
        	   //查询 库存
             commonDao.getStockState(onLineId, warehouseId, goodsId, "", "", userId, 0, "", 0, -1, 0, 0, 0, "");
             List<Map<String, Object>> stockList = commonDao.findForJdbc(" select * from tempdb.dbo.[sys_GetStockState" + onLineId + "] ");
             
        	 String groupid=commonDao.getDataForString("Select SizeGroupID From Goods a,GoodsType b Where a.GoodsTypeID=b.GoodsTypeID and GoodsID=? ",goodsId);	
        	 List<Map<String, Object>> colorList = commonDao.findForJdbc(" select ColorID,No,Color,0 stock from Color where ColorID in (select colorId from goodsColor where goodsId = ?) ", goodsId);
        	 List<Map<String, Object>> sizeList=commonDao.findForJdbc("Select 'x_'+cast(a.No as varchar(10)) x,b.Size+'('+convert(varchar,b.No)+')' Name,b.SizeID, b.No as SizeCode,b.Size  From SizeGroupSize a,Size b Where a.SizeID=b.SizeID and a.SizeGroupID='"+groupid+"' ");
        	 Map<String,Object> goods=commonDao.findOneForJdbc("select GoodsID,Code,Name,RetailSales from Goods where GoodsID =? ", goodsId);
        	 
        	 List<Map<String, Object>> tempList = new ArrayList<Map<String, Object>>();
        	 
        	    //组合显示数据
             for (int n = 0;  n <colorList.size(); n++) {
            	  
            	 
            	 Map<String,Object> temp = colorList.get(n);
            	 
            	 
            	 
             for (int i = 0; i < sizeList.size(); i++) {
            	 Map<String,Object> czlist=new HashMap<String,Object>();//装数据 的map
            	 Map<String,Object> map= sizeList.get(i);
            	 
            	 czlist.put("GoodsID", goodsId);
            	 czlist.put("GoodsCode", goods.get("Code"));
            	 
            	 czlist.put("ColorID", temp.get("ColorID"));
            	 czlist.put("ColorName",temp.get("Color"));
            	czlist.put("SizeID",map.get("SizeID"));
            	czlist.put("SizeName",map.get("Size"));
          	    czlist.put("Size",map.get("Size"));
          	    czlist.put("x",map.get("x")); 
                  czlist.put("Quantity", "");
                  czlist.put("Amount", ""); 
                tempList.add(czlist);
            	    
             }
             //添加其他属性                
              
             
              }
             
             
             
             Map<String,Object> gc=new HashMap<String,Object>();
             for(int l=0;l<colorList.size();l++){
            	 Map m=colorList.get(l);
            	 m.put("GoodsID", goodsId);
            	 m.put("title", m.get("Color"));
            	 m.put("tipqty", "");//颜色提示数量小红点
             }
             colorList.get(0).put("checked", true);
             for(Map m:tempList){
            	
             for(Map n:stockList)//库存列表	 
             {
            	if(m.get("GoodsID").equals(n.get("GoodsID"))
            	  && m.get("ColorID").equals(n.get("ColorID"))
            	  && m.get("SizeID").equals(n.get("SizeID")) && Integer.parseInt(String.valueOf(n.get("Quantity")))>0 )
            	{
            	 m.put("stock", n.get("Quantity"));	
            	}	
            			
             } 	 
            	 
             }//显示尺码
             
           //  System.out.println("tempList:"+tempList.toString());
             
             gc.put("colors", colorList);
             gc.put("goods",goods);
             
             j.setAttributes(gc);
        	 j.setObj(tempList);
        	 j.setSuccess(true);
         }
         if(j.getObj()==null){
        	 return null;
         }
    	return j;
    }
    
    
    
    /**
     * 根据货品ID生成货品颜色尺码信息
     * 
     * @param req
     * @return
     */
    @SuppressWarnings("unchecked")
    @RequestMapping(params = "generalColorAndSizeByGoodsId")
    public void generalColorAndSizeByGoodsId(HttpServletRequest req, HttpServletResponse resp) {
        try {
            List<Map<String, Object>> colorList = new ArrayList<Map<String, Object>>();
            List<Map<String, Object>> sizeList = new ArrayList<Map<String, Object>>();
            String goodsId = oConvertUtils.getString(req.getParameter("GoodsID"));
            String tableName = oConvertUtils.getString(req.getParameter("tableName"));
            String warehouseId = oConvertUtils.getString(req.getParameter("DeptID"));
            String onLineId = oConvertUtils.getString(req.getParameter("onLineId"));
            String userId = oConvertUtils.getString(req.getParameter("userId"));
            if (goodsId != null && !"".equals(goodsId) && !"null".equalsIgnoreCase(goodsId)) {
                //查询 库存
                commonDao.getStockState(onLineId, warehouseId, goodsId, "", "", userId, 0, "", 0, -1, 0, 0, 0, "");
                List<Map<String, Object>> stockList = commonDao.findForJdbc(" select * from tempdb.dbo.[sys_GetStockState" + onLineId + "] ");
                //查询颜色尺码
                colorList = commonDao.findForJdbc(" select ColorID,No,Color,0 stock from Color where ColorID in (select colorId from goodsColor where goodsId = ?) ", goodsId);
                sizeList = commonDao.findForJdbc(" select SizeID,Size,(select No from Size where SizeID = t.SizeID) No from SizeGroupSize t where t.SizeGroupID = (select GroupID from Goods where GoodsID = ?) order by No ", goodsId);
                //组合显示数据
                for (int i = 0; i < sizeList.size(); i++) {
                    List<Map<String, Object>> tempList = new ArrayList<Map<String, Object>>();
                    Map<String,Object> map = sizeList.get(i);
                    String sizeId = (String) map.get("SizeID");
                    for (int j = 0; j < colorList.size(); j++) {
                        Map<String,Object> temp = colorList.get(j);
                        Map<String,Object> ttemp = new HashMap<String, Object>();
                        ttemp.put("ColorID", temp.get("ColorID"));
                        ttemp.put("No", temp.get("No"));
                        ttemp.put("Color", temp.get("Color"));
                        ttemp.put("stock", temp.get("stock"));
                        String colorId = (String) ttemp.get("ColorID");
                        for (int k = 0; k < stockList.size(); k++) {
                            Map<String,Object> tmap = stockList.get(k);
                            String tcolorId = (String) tmap.get("ColorID");
                            String tsizeId = (String) tmap.get("SizeID");
                            if(tcolorId.equals(colorId) && tsizeId.equals(sizeId)){
                                ttemp.put("stock", tmap.get("Quantity"));
                            }
                        }
                        tempList.add(ttemp);
                    }
                    map.put("colors", tempList);
                }
                //显示不同色码的库存
//                for (int i = 0; i < sizeList.size(); i++) {
//                    Map<String,Object> map = sizeList.get(i);
//                    String sizeId = (String) map.get("SizeID");
//                    List<Map<String, Object>> colors = (List<Map<String, Object>>) map.get("colors");
//                    for (int j = 0; j < colors.size(); j++) {
//                        Map<String,Object> temp = colors.get(j);
//                        String colorId = (String) temp.get("ColorID");
//                        for (int k = 0; k < stockList.size(); k++) {
//                            Map<String,Object> tmap = stockList.get(k);
//                            String tcolorId = (String) tmap.get("ColorID");
//                            String tsizeId = (String) tmap.get("SizeID");
//                            if(tcolorId.equals(colorId) && tsizeId.equals(sizeId)){
//                                temp.put("stock", tmap.get("Quantity"));
//                            }
//                        }
//                    }
//                }
            }
            req.setAttribute("colorList", colorList);
            req.setAttribute("sizeList", sizeList);
            req.getRequestDispatcher("context/multi-select-new-way.jsp").forward(req, resp);
        } catch (Exception e) {
            SysLogger.error(e.getMessage(), e);
        }
    }

    /**
     * 改变图片尺寸
     * 
     * @param srcFileName 源图片路径
     * @param tagFileName 目的图片路径
     * @param width 修改后的宽度
     * @param height 修改后的高度
     */
    public static void zoomImage(String srcFileName, String tagFileName, int width, int height) {
        try {
            BufferedImage bi = ImageIO.read(new File(srcFileName));
            // BufferedImage tag = new BufferedImage(width, height,
            // BufferedImage.TYPE_INT_RGB);
            // tag.getGraphics().drawImage(bi, 0, 0, width, height, null);
            BufferedImage tag = resize(bi, width, height);
            ImageIO.write(tag, "jpg", new File(tagFileName));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 生成指定大小的图片并压缩
     * 
     * @param source
     * @param targetW
     * @param targetH
     * @return
     */
    public static BufferedImage resize(BufferedImage source, int targetW, int targetH) {
        // targetW，targetH分别表示目标长和宽
        int type = source.getType();
        BufferedImage target = null;
        double sx = (double) targetW / source.getWidth();
        double sy = (double) targetH / source.getHeight();
        // 这里想实现在targetW，targetH范围内实现等比缩放。如果不需要等比缩放
        // 则将下面的if else语句注释即可
        if (sx < sy) {
            sy = sx;
            targetH = (int) (sx * source.getHeight());
        } else {
            sx = sy;
            targetW = (int) (sy * source.getWidth());
        }
        if (type == BufferedImage.TYPE_CUSTOM) { // handmade
            ColorModel cm = source.getColorModel();
            WritableRaster raster = cm.createCompatibleWritableRaster(targetW, targetH);
            boolean alphaPremultiplied = cm.isAlphaPremultiplied();
            target = new BufferedImage(cm, raster, alphaPremultiplied, null);
        } else
            target = new BufferedImage(targetW, targetH, type);
        Graphics2D g = target.createGraphics();
        // smoother than exlax:
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.drawRenderedImage(source, AffineTransform.getScaleInstance(sx, sy));
        g.dispose();
        return target;
    }

    /**
     * 获取尺码组中的最大尺码
     * 
     * @param id
     * @param tableName
     * @return
     */
    private int getMaxSize(String id, String tableName) {
        String maxSizeSql = null;
        String displaySizeGroup = (String) commonDao.getData(" select displaysizegroup from " + tableName + " where " + tableName + "ID = '" + id + "' ");
        if (null == displaySizeGroup || "".equals(displaySizeGroup) || "null".equals(displaySizeGroup)) {
            maxSizeSql = "select max(no) as maxsize from SizeGroupSize";
        } else {
            if (!displaySizeGroup.contains("'")) {
                displaySizeGroup = commonDao.formatString(displaySizeGroup);
            }
            maxSizeSql = "select max(no) as maxsize from SizeGroupSize where sizeGroupId in (" + displaySizeGroup + ")";
        }
        Map sizeMap = (Map) commonDao.findForJdbc(maxSizeSql).get(0);
        int maxSize = (Integer) sizeMap.get("maxsize");
        if (maxSize < 1) {
            maxSize = 1;
        }
        return maxSize;
    }

    /**
     * 获取显示的尺码
     * 
     * @param maxSize
     * @return
     */
    private String getSizeStr(int maxSize) {
        StringBuffer sizeStr = new StringBuffer();
        for (int i = 1; i <= maxSize; i++) {
            sizeStr.append(",x_").append(i);
        }
        return sizeStr.toString();
    }

    /**
     * 根据单据的尺码组生成打印单据的显示尺码标题
     * 
     * @param displaySizeGroup
     * @param maxSize
     * @return
     */
    private List<Map<String, Object>> getSizeTitle(String displaySizeGroup, int maxSize) {
        List sizeTitleList = null;
        if (!displaySizeGroup.contains("'")) {
            displaySizeGroup = commonDao.formatString(displaySizeGroup);
        }
        String[] str = displaySizeGroup.split(",");
        if (str.length > 0) {
            for (int i = 0; i < str.length; i++) {
                List tempList = commonDao.findForJdbc(" select No,Size from sizegroupsize where sizeGroupId in (" + str[i] + ")  ");
                if (sizeTitleList == null) {
                    sizeTitleList = new ArrayList<Map<String, Object>>();
                    sizeTitleList.addAll(tempList);
                } else {
                    for (int j = 0; j < tempList.size(); j++) {
                        boolean exit = false;
                        Map map = (Map) tempList.get(j);
                        for (int k = 0; k < sizeTitleList.size(); k++) {
                            Map temp = (Map) sizeTitleList.get(k);
                            if (map.get("No").equals(temp.get("No"))) {
                                String size = (String) map.get("Size");
                                String tsize = (String) temp.get("Size");
                                if (!size.equals(tsize)) {
                                    temp.put("Size", size + "<br/>" + tsize);
                                }
                                exit = true;
                            }
                        }
                        if (!exit) {
                            sizeTitleList.add(map);
                        }
                    }
                }
            }
        } else {
            sizeTitleList = commonDao.findForJdbc(" select No,Size from sizegroupsize where sizeGroupId in (" + displaySizeGroup + ")  ");
            for (int i = 0; i < sizeTitleList.size() - 1; i++) {
                Map map = (Map) sizeTitleList.get(i);
                for (int j = sizeTitleList.size() - 1; j > i; j--) {
                    Map temp = (Map) sizeTitleList.get(j);
                    if (map.get("No").equals(temp.get("No"))) {
                        String size = (String) map.get("Size");
                        String tsize = (String) temp.get("Size");
                        map.put("Size", size + "<br/>" + tsize);
                        sizeTitleList.remove(j);
                    }
                }
            }
        }
        return sizeTitleList;
    }

    /**
     * 检查单据是否存在负库存货品(用于后台保存单据前检查)
     * 
     * @param dao
     * @param dataList
     * @param userId
     * @param departmentId
     * @param tableTag
     * @param invoiceId
     * @return
     */
    public List<Map<String, Object>> checkNegativeInventoryForBackStage(CommonDao dao, List<Map<String, Object>> dataList, String hostName, String userId, String departmentId, int tableTag, String invoiceId, int sendType, int disType, int optStata, int controlFlag, int auditFlag, String noDate) {
        List<Map<String, Object>> tempList = new ArrayList<Map<String, Object>>();
        for (int i = 0; i < dataList.size(); i++) {
            Map<String, Object> temp = dataList.get(i);
            String goodsId = (String) temp.get("GoodsID");
            String colorId = (String) temp.get("ColorID");
            String sizeId = (String) temp.get("SizeID");
            int quantity = Integer.parseInt(String.valueOf(temp.get("Quantity")));
            int stockQty = queryStockByGoods(dao, hostName, userId, departmentId, goodsId, colorId, sizeId, tableTag, invoiceId, sendType, disType, optStata, controlFlag, auditFlag, noDate);
            if (stockQty <= 0 || Math.abs(quantity) > stockQty) {
                Map<String, Object> tmp = new HashMap<String, Object>();
                tmp.put("GoodsCode", temp.get("GoodsCode"));
                tmp.put("Color", temp.get("Color"));
                tmp.put("Size", temp.get("Size"));
                tmp.put("Quantity", temp.get("Quantity"));
                tmp.put("StockQty", String.valueOf(stockQty));
                tempList.add(tmp);
            }
        }
        return tempList;
    }

    /**
     * 检查单据是否存在负库存货品(用于前台保存单据前检查)
     * 
     * @param dao
     * @param dataList
     * @param departmentId
     * @return
     */
    public List<Map<String, Object>> checkNegativeInventoryForFrontDesk(CommonDao dao, List<Map<String, Object>> dataList, String departmentId) {
        List<Map<String, Object>> tempList = new ArrayList<Map<String, Object>>();
        for (int i = 0; i < dataList.size(); i++) {
            Map<String, Object> temp = dataList.get(i);
            String goodsId = (String) temp.get("GoodsID");
            String colorId = (String) temp.get("ColorID");
            String sizeId = (String) temp.get("SizeID");
            int quantity = Integer.parseInt(String.valueOf(temp.get("Quantity")));
            int stockQty = dao.getStockGCS(departmentId, goodsId, colorId, sizeId);
            if (stockQty <= 0 || Math.abs(quantity) > stockQty) {
                Map<String, Object> tmp = new HashMap<String, Object>();
                tmp.put("GoodsCode", temp.get("GoodsCode"));
                tmp.put("Color", temp.get("Color"));
                tmp.put("Size", temp.get("Size"));
                tmp.put("Quantity", temp.get("Quantity"));
                tmp.put("StockQty", String.valueOf(stockQty));
                tempList.add(tmp);
            }
        }
        return tempList;
    }

    /**
     * 根据部门和货品信息查询货品的剩余库存
     * 
     * @param dao
     * @param userId
     * @param departmentId
     * @param goodsId
     * @param colorId
     * @param sizeId
     * @param tableTag
     * @param invoiceId
     * @return
     */
    public int queryStockByGoods(CommonDao dao, String hostName, String userId, String departmentId, String goodsId, String colorId, String sizeId, int tableTag, String invoiceId, int sendType, int disType, int optStata, int controlFlag, int auditFlag, String noDate) {
        dao.getStockState(hostName, departmentId, goodsId, colorId, sizeId, userId, tableTag, invoiceId, sendType, disType, optStata, controlFlag, auditFlag, noDate);
        String qtyzStr = String.valueOf(dao.getData(" select Quantity from tempdb.dbo.[sys_GetStockState" + hostName + "] "));
        if (null == qtyzStr || qtyzStr.isEmpty() || "null".equalsIgnoreCase(qtyzStr)) {
            qtyzStr = "0";
        }
        int qtyz = Integer.parseInt(qtyzStr);
        return qtyz;
    }



    /**
     * 查询条码
     * 
     * @param req
     * @return
     */
    @RequestMapping(params = "getBarcode")
    @ResponseBody
    public AjaxJson getBarcode(HttpServletRequest req) {
        AjaxJson j = new AjaxJson();
        j.setAttributes(new HashMap<String, Object>());
        try {
            String barcode = oConvertUtils.getString(req.getParameter("barcode"));
            List datas = commonDao.findForJdbc("select goodsId,colorId,sizeId from barcode where barcode = ?", barcode);
            j.setObj(datas);
        } catch (Exception e) {
            j.setSuccess(false);
            j.setMsg(e.getMessage());
            SysLogger.error(e.getMessage(), e);
        }
        return j;
    }



}
