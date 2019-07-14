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
import com.fuxi.system.util.SysLogger;
import com.fuxi.system.util.oConvertUtils;

/**
 * Title: OPMLoginController Description: 订货会业务逻辑控制器
 * 
 * @author LYJ
 * 
 */
@Controller
@RequestMapping("/OPMManager")
public class OPMManagerController extends BaseController {

    private Logger log = Logger.getLogger(OPMManagerController.class);

    @Autowired
    private CommonDao commonDao;

    /**
     * 登录
     * 
     * @param req
     * @return
     */
    @RequestMapping(params = "login")
    @ResponseBody
    public AjaxJson queryStock(HttpServletRequest req) {
        AjaxJson j = new AjaxJson();
        j.setAttributes(new HashMap<String, Object>());
        try {
            StringBuffer sql = new StringBuffer();
            String userName = oConvertUtils.getString(req.getParameter("username"));
            String password = oConvertUtils.getString(req.getParameter("password"));
            String customerId = oConvertUtils.getString(req.getParameter("customerId"));
            int count = commonDao.getDataToInt(" select count(1) from [user] where no = ? and password = ? ", userName, password);
            if (count == 0) {
                throw new BusinessException("密码错误");
            }
            sql.append(" select userId, no userNo, userName, ").append(" departmentId, (select dbo.getCustPriceTypeOfFieldName(c.OrderPriceType) OrderField ").append(" from Customer c where customerId = ?) orderField from [user] where no = ? ");
            List<Map<String, Object>> dataList = commonDao.findForJdbc(sql.toString(), customerId, userName);
            j.setObj(dataList);
        } catch (Exception e) {
            j.setSuccess(false);
            j.setMsg(e.getMessage());
            SysLogger.error(e.getMessage(), e);
        }
        return j;
    }

    /**
     * 客户模式下检查客户编码是否存在并返回客户信息
     * 
     * @param req
     * @return
     */
    @RequestMapping(params = "checkUserOfCustomerStyle")
    @ResponseBody
    public AjaxJson checkUserOfCustomerStyle(HttpServletRequest req) {
        AjaxJson j = new AjaxJson();
        j.setAttributes(new HashMap<String, Object>());
        try {
            String userNo = oConvertUtils.getString(req.getParameter("userNo"));
            StringBuffer sql = new StringBuffer();
            sql.append("select c.Customer,c.CustomerID from [User] a ").append(" join Customer c on a.customerid=c.customerid where a.[No] = ?");
            List list = commonDao.findForJdbc(sql.toString(), userNo);
            if (list.size() == 0) {
                throw new BusinessException("用户编码不存在");
            }
            j.setObj(list);
        } catch (Exception e) {
            j.setSuccess(false);
            j.setMsg(e.getMessage());
            SysLogger.error(e.getMessage(), e);
        }
        return j;
    }

    /**
     * 
     * 根据货品编码/货品条码查询货品信息
     * 
     * @param req
     * @return
     */
    @RequestMapping(params = "goodsQueryPath")
    @ResponseBody
    public AjaxJson goodsQueryPath(HttpServletRequest req) {
        AjaxJson j = new AjaxJson();
        j.setAttributes(new HashMap<String, Object>());
        try {
            String barcode = oConvertUtils.getString(req.getParameter("barcode"));
            String orderField = oConvertUtils.getString(req.getParameter("orderField"));
            StringBuffer sql = new StringBuffer();
            // 根据条码获取货品ID
            String goodsId = new SelectController().getGoodsId(barcode, commonDao);
            sql.append(" select Name GoodsName,isnull(TradePrice,0) TradePrice,isnull(RetailSales,0) RetailSales,GoodsID, Code GoodsCode, ").append(" isnull(" + orderField + ",0) UnitPrice,(select GoodsType from GoodsType where GoodsTypeID = g.GoodsTypeID) GoodsType,SubType,Age,Season,")
                    .append("(select Serial from BrandSerial where BrandSerialID = g.BrandSerialID) BrandSerial,Style,Sex,Kind,Model  from  Goods g where g.goodsId = ? ");
            List list = commonDao.findForJdbc(sql.toString(), goodsId);
            if (list.size() == 0) {
                throw new BusinessException("条码或货号错误");
            }
            j.setObj(list);
        } catch (Exception e) {
            j.setSuccess(false);
            j.setMsg(e.getMessage());
            SysLogger.error(e.getMessage(), e);
        }
        return j;
    }

    /**
     * 
     * 根据货品ID查询货品信息
     * 
     * @param req
     * @return
     */
    @RequestMapping(params = "goodsInfoQueryPath")
    @ResponseBody
    public AjaxJson goodsInfoQueryPath(HttpServletRequest req) {
        AjaxJson j = new AjaxJson();
        j.setAttributes(new HashMap<String, Object>());
        try {
            String goodsId = oConvertUtils.getString(req.getParameter("goodsId"));
            StringBuffer sql = new StringBuffer();
            sql.append(" select Name GoodsName,isnull(TradePrice,0) TradePrice,isnull(RetailSales,0) RetailSales,GoodsID, Code GoodsCode, ").append(" isnull(TradePrice,0) UnitPrice,(select GoodsType from GoodsType where GoodsTypeID = g.GoodsTypeID) GoodsType,SubType,Age,Season,")
                    .append("(select Serial from BrandSerial where BrandSerialID = g.BrandSerialID) BrandSerial,Style,Sex,Kind,Model  from  Goods g where g.goodsId = ? ");
            List list = commonDao.findForJdbc(sql.toString(), goodsId);
            if (list.size() == 0) {
                throw new BusinessException("条码或货号错误");
            }
            j.setObj(list);
        } catch (Exception e) {
            j.setSuccess(false);
            j.setMsg(e.getMessage());
            SysLogger.error(e.getMessage(), e);
        }
        return j;
    }

    /**
     * 
     * 根据客户信息获取对应的订货记录
     * 
     * @param req
     * @return
     */
    @RequestMapping(params = "getOPMDataMethod")
    @ResponseBody
    public AjaxJson getOPMDataMethod(HttpServletRequest req) {
        AjaxJson j = new AjaxJson();
        j.setAttributes(new HashMap<String, Object>());
        try {
            String customerId = oConvertUtils.getString(req.getParameter("customerId"));
            StringBuffer sql = new StringBuffer();
            String salesOrderId = commonDao.getDataForString(" select top 1 SalesOrderID from SalesOrder where customerId = ? and AuditFlag = 0 order by madebydate desc ", customerId);
            sql.append(" select sodt.GoodsID,g.Code GoodsCode,isnull(sodt.UnitPrice,0) UnitPrice,sod.SizeID,(select No from Size where sizeId = sod.sizeId) SizeCode, ")
                    .append(" sod.Quantity,g.Name GoodsName,sodt.ColorID,(select No from Color where ColorID = sodt.ColorID) ColorCode from SalesOrder so ").append(" join SalesOrderDetailTemp sodt on so.SalesOrderID = sodt.SalesOrderID join SalesOrderDetail sod ")
                    .append(" on sod.SalesOrderID = sodt.SalesOrderID and sod.GoodsID = sodt.GoodsID and sod.ColorID = sodt.ColorID ").append(" join Goods g on g.GoodsID = sodt.GoodsID where so.SalesOrderID = ? order by sodt.GoodsID,sodt.ColorID,sod.SizeID ");
            List list = commonDao.findForJdbc(sql.toString(), salesOrderId);
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("salesOrderId", salesOrderId);
            map.put("datas", list);
            j.setObj(map);
        } catch (Exception e) {
            j.setSuccess(false);
            j.setMsg(e.getMessage());
            SysLogger.error(e.getMessage(), e);
        }
        return j;
    }

    /**
     * 
     * 根据条码/货号查询货品的订购信息
     * 
     * @param req
     * @return
     */
    @RequestMapping(params = "getOrderGoodsInfo")
    @ResponseBody
    public AjaxJson getOrderGoodsInfo(HttpServletRequest req) {
        AjaxJson j = new AjaxJson();
        j.setAttributes(new HashMap<String, Object>());
        try {
            String barcode = oConvertUtils.getString(req.getParameter("barcode"));
            // 根据条码获取货品ID
            String goodsId = new SelectController().getGoodsId(barcode, commonDao);
            StringBuffer sql = new StringBuffer();
            sql.append(" select g.Name GoodsName,c.Color,sum(b.Quantity) Qty,sum(b.Quantity*g.TradePrice) Amt from SalesOrder a join SalesOrderDetailTemp b on a.SalesOrderID=b.SalesOrderID "
                    + " join Goods g on b.goodsid=g.goodsid join color c on b.colorid=c.colorid where g.goodsId = ? group by g.Name,c.Color order by 1 ");
            List list = commonDao.findForJdbc(sql.toString(), goodsId);
            j.setObj(list);
        } catch (Exception e) {
            j.setSuccess(false);
            j.setMsg(e.getMessage());
            SysLogger.error(e.getMessage(), e);
        }
        return j;
    }

    /**
     * 
     * 根据货品类别查询货品的订购信息
     * 
     * @param req
     * @return
     */
    @RequestMapping(params = "getOrderGoodsType")
    @ResponseBody
    public AjaxJson getOrderGoodsType(HttpServletRequest req) {
        AjaxJson j = new AjaxJson();
        j.setAttributes(new HashMap<String, Object>());
        try {
            String goodsType = oConvertUtils.getString(req.getParameter("goodsType"));
            StringBuffer sql = new StringBuffer();
            sql.append(" select * from dbo.fun_OrdReport_subType(?) order by 1 ");
            List list = commonDao.findForJdbc(sql.toString(), goodsType);
            j.setObj(list);
        } catch (Exception e) {
            j.setSuccess(false);
            j.setMsg(e.getMessage());
            SysLogger.error(e.getMessage(), e);
        }
        return j;
    }

    /**
     * 
     * 根据货品类别查询货品的订购信息的排行榜
     * 
     * @param req
     * @return
     */
    @RequestMapping(params = "getOrderRankingList")
    @ResponseBody
    public AjaxJson getOrderRankingList(HttpServletRequest req) {
        AjaxJson j = new AjaxJson();
        j.setAttributes(new HashMap<String, Object>());
        try {
            String goodsType = oConvertUtils.getString(req.getParameter("goodsType"));
            StringBuffer sql = new StringBuffer();
            sql.append(" select g.Code,g.Name,c.Color,a.* from dbo.fun_OrdReport_TopLst(?) a join goods g on a.goodsid=g.goodsid join color c on a.colorid=c.colorid order by a.SN ");
            List list = commonDao.findForJdbc(sql.toString(), goodsType);
            j.setObj(list);
        } catch (Exception e) {
            j.setSuccess(false);
            j.setMsg(e.getMessage());
            SysLogger.error(e.getMessage(), e);
        }
        return j;
    }


}
