package com.fuxi.web.controller;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
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
import com.fuxi.system.util.BarcodeUtil;
import com.fuxi.system.util.Client;
import com.fuxi.system.util.LoadUserCount;
import com.fuxi.system.util.ResourceUtil;
import com.fuxi.system.util.SysLogger;
import com.fuxi.system.util.oConvertUtils;

/**
 * Title: SelectController Description: 通用信息选择加载逻辑控制器
 * 
 * @author LJ,LYJ
 * 
 */
@Controller
@RequestMapping("/select")
public class SelectController extends BaseController {

    private Logger log = Logger.getLogger(SelectController.class);
    // 部门ID --> 用于选取仓位
    private String deptId;
    // 货品ID --> 用于选取颜色,尺码
    private String goodsId;
    // 用户编码 --> 用于选取客户
    private String userId;

    @Autowired
    private CommonDao commonDao;

    /**
     * 模糊查询客户信息(获取客户)
     * 
     * @param req
     * @return
     */
    @RequestMapping(params = "getCustomer")
    @ResponseBody
    public AjaxJson getCustomer(HttpServletRequest req) {
        AjaxJson j = new AjaxJson();
        j.setAttributes(new HashMap<String, Object>());
        Client client = ResourceUtil.getClientFromSession(req);
        Map<String, String> map = client.getMap();
        String userRight = map.get(client.getUserID());
        try {
            String param = oConvertUtils.getString(req.getParameter("param"));
            int page = oConvertUtils.getInt(req.getParameter("currPage"));
            StringBuffer sb = new StringBuffer();
            sb.append(" select Customer,CustomerID,(Customer+(isnull('('+Code+')',''))) as Name,isnull(DiscountRate,0) DiscountRate,DistrictID, ").append("isnull(OrderDiscount,0) OrderDiscount,isnull(AllotDiscount,0) AllotDiscount,isnull(ReplenishDiscount,0) ReplenishDiscount ")
                    .append(" from customer where DepartmentID in (").append(userRight).append(") ");
            if (null != param && !param.isEmpty() && !"".equals(param)) {
                sb.append(" and CustomerID in (select CustomerID from Customer where Code like '%").append(param).append("%' or Customer like '%").append(param).append("%' or Tel like '%").append(param).append("%' )");
            }
            sb.append(" order by code asc ");
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
     * 根据用户编码模糊查询客户信息(获取客户)
     * 
     * @param req
     * @return
     */
    @RequestMapping(params = "getCustomerByUserNo")
    @ResponseBody
    public AjaxJson getCustomerByUserNo(HttpServletRequest req) {
        AjaxJson j = new AjaxJson();
        j.setAttributes(new HashMap<String, Object>());
        try {
            String param = oConvertUtils.getString(req.getParameter("param"));
            boolean flag = param.matches("^@.*@$");
            if (flag) {
                String userNo = param.substring(1, param.length() - 1);
                userId = commonDao.getDataForString(" select UserId from [user] where no = ? ", userNo);
                param = "";
            }
            int page = oConvertUtils.getInt(req.getParameter("currPage"));
            StringBuffer sb = new StringBuffer();
            sb.append(" select CustomerID,Code,Customer Name,HelpCode from Customer where StopFlag=0 and CustomerID in (select CustomerID from dbo.GetRightLst_Cust('" + userId + "')) ");
            if (null != param && !param.isEmpty() && !"".equals(param)) {
                sb.append(" and CustomerID in( select CustomerID from Customer where code like '%").append(param).append("%' or customer like '%").append(param).append("%' ) ");
            }
            sb.append(" order by 3 ");
            List list = commonDao.findForJdbc(sb.toString(), page, 15);
            if (list.size() == 0) {
                throw new BusinessException("用户姓名不存在");
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
     * 模糊查询部门信息(获取部门)
     * 
     * @param req
     * @return
     */
    @RequestMapping(params = "getDepartment")
    @ResponseBody
    public AjaxJson getDepartment(HttpServletRequest req) {
        AjaxJson j = new AjaxJson();
        j.setAttributes(new HashMap<String, Object>());
        Client client = ResourceUtil.getClientFromSession(req);
        Map<String, String> map = client.getMap();
        String userRight = map.get(client.getUserID());
        try {
            String param = oConvertUtils.getString(req.getParameter("param"));
            int page = oConvertUtils.getInt(req.getParameter("currPage"));
            StringBuffer sb = new StringBuffer();
            sb.append(" select Department  Name,DepartmentID from Department where DepartmentID in (").append(userRight).append(") ");
            if (null != param && !param.isEmpty() && !"".equals(param)) {
                sb.append(" and DepartmentID in ( select DepartmentID from Department where Code like '%").append(param).append("%' or Department like '%").append(param).append("%' ) ");
            }
            sb.append(" order by code asc ");
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
            String param = oConvertUtils.getString(req.getParameter("param"));
            int page = oConvertUtils.getInt(req.getParameter("currPage"));
            StringBuffer sb = new StringBuffer();
            sb.append(" select Department Name,DepartmentID,MustExistsGoodsFlag from Department where WarehouseFlag = '1' and DepartmentID in (").append(userRight).append(") ");
            if (null != param && !param.isEmpty() && !"".equals(param)) {
                sb.append(" and DepartmentID in ( select DepartmentID from Department where Code like '%").append(param).append("%' or Department like '%").append(param).append("%' ) ");
            }
            sb.append(" order by code asc ");
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
     * 根据调拨权限模糊查询仓库信息(获取转进仓库)
     * 
     * @param req
     * @return
     */
    @RequestMapping(params = "getWarehouseIn")
    @ResponseBody
    public AjaxJson getWarehouseIn(HttpServletRequest req) {
        AjaxJson j = new AjaxJson();
        j.setAttributes(new HashMap<String, Object>());
        Client client = ResourceUtil.getClientFromSession(req);
        try {
            String param = oConvertUtils.getString(req.getParameter("param"));
            int page = oConvertUtils.getInt(req.getParameter("currPage"));
            StringBuffer sb = new StringBuffer();
            sb.append(" select Department Name,DepartmentID from Department where WarehouseFlag = '1' and DepartmentID in (select departmentId from departmentRight where TransferRight = '1' and userId = '" + client.getUserID() + "' ) ");
            if (null != param && !param.isEmpty() && !"".equals(param)) {
                sb.append(" and DepartmentID in ( select DepartmentID from Department where Code like '%").append(param).append("%' or Department like '%").append(param).append("%' ) ");
            }
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
     * 模糊查询仓库信息(获取含含仓位的仓库) 用于仓位管理
     * 
     * @param req
     * @return
     */
    @RequestMapping(params = "getWarehouseHasStorage")
    @ResponseBody
    public AjaxJson getWarehouseHasStorage(HttpServletRequest req) {
        AjaxJson j = new AjaxJson();
        j.setAttributes(new HashMap<String, Object>());
        Client client = ResourceUtil.getClientFromSession(req);
        Map<String, String> map = client.getMap();
        String userRight = map.get(client.getUserID());
        try {
            String param = oConvertUtils.getString(req.getParameter("param"));
            int page = oConvertUtils.getInt(req.getParameter("currPage"));
            StringBuffer sb = new StringBuffer();
            sb.append(" select Department  Name,DepartmentID from Department where WarehouseFlag = '1' and Init <> '1' and DepartmentID in (").append(userRight).append(") ");
            if (null != param && !param.isEmpty() && !"".equals(param)) {
                sb.append(" and DepartmentID in ( select DepartmentID from Department where Code like '%").append(param).append("%' or Department like '%").append(param).append("%' ) ");
            }
            sb.append(" and DepartmentID in ( select distinct departmentID from storage )");
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
     * 模糊查询仓库信息(获取已经开始初始化的仓库) 用于仓位管理
     * 
     * @param req
     * @return
     */
    @RequestMapping(params = "getWarehouseHasStorageHasInit")
    @ResponseBody
    public AjaxJson getWarehouseHasStorageHasInit(HttpServletRequest req) {
        AjaxJson j = new AjaxJson();
        j.setAttributes(new HashMap<String, Object>());
        Client client = ResourceUtil.getClientFromSession(req);
        Map<String, String> map = client.getMap();
        String userRight = map.get(client.getUserID());
        try {
            String param = oConvertUtils.getString(req.getParameter("param"));
            int page = oConvertUtils.getInt(req.getParameter("currPage"));
            StringBuffer sb = new StringBuffer();
            sb.append(" select Department  Name,DepartmentID from Department where WarehouseFlag = '1' and Init = '1' and DepartmentID in (").append(userRight).append(") ");
            if (null != param && !param.isEmpty() && !"".equals(param)) {
                sb.append(" and DepartmentID in ( select DepartmentID from Department where Code like '%").append(param).append("%' or Department like '%").append(param).append("%' ) ");
            }
            sb.append(" and DepartmentID in ( select distinct departmentID from storage )");
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
     * 模糊查询货品编码(获取货品编号)
     * 
     * @param req
     * @return
     */
    @RequestMapping(params = "getGoods")
    @ResponseBody
    public AjaxJson getGoods(HttpServletRequest req) {
        AjaxJson j = new AjaxJson();
        j.setAttributes(new HashMap<String, Object>());
        try {
            String param = oConvertUtils.getString(req.getParameter("param"));
            int page = oConvertUtils.getInt(req.getParameter("currPage"));
            StringBuffer sb = new StringBuffer();
            sb.append(
                    " select GoodsID,name+'('+code+')' Name,name GoodsName,Code,g.PresentFlag,isnull(TradePrice,0) TradePrice,isnull(RetailSales,0) RetailSales,isnull(g.Discount,10) Discount,DiscountFlag,isnull(RetailSales1,0) RetailSales1,"
                            + "(select GoodsType from GoodsType where GoodsTypeID = g.GoodsTypeID) GoodsType,SubType,Age,Season,(select Serial from BrandSerial where BrandSerialID = g.BrandSerialID) BrandSerial,Style,Sex,Kind,Model,"
                            + "isnull(RetailSales2,0) RetailSales2,isnull(RetailSales3,0) RetailSales3,isnull(RetailSales4,0) RetailSales4,isnull(RetailSales5,0) RetailSales5,"
                            + "isnull(RetailSales6,0) RetailSales6,isnull(RetailSales7,0) RetailSales7,isnull(RetailSales8,0) RetailSales8,isnull(SalesPrice1,0) SalesPrice1,"
                            + "isnull(SalesPrice2,0) SalesPrice2,isnull(SalesPrice3,0) SalesPrice3,isnull(SalesPrice4,0) SalesPrice4,isnull(SalesPrice5,0) SalesPrice5, "
                            + "isnull(SalesPrice6,0) SalesPrice6,isnull(SalesPrice7,0) SalesPrice7,isnull(SalesPrice8,0) SalesPrice8,sizIndex=(select max(no) as maxsize from SizeGroupSize where sizeGroupId=g.GroupID) from  Goods g where g.code like '%").append(param).append("%' or g.Name like '%").append(param).append("%' or g.GoodsID like '%").append(param)
                    .append("%' order by len(code) asc ");
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
     * 模糊查询小票货品编码(获取货品编号)
     * 
     * @param req
     * @return
     */
    @RequestMapping(params = "getPosSalesGoods")
    @ResponseBody
    public AjaxJson getPosSalesGoods(HttpServletRequest req) {
        Client client = ResourceUtil.getClientFromSession(req);
        AjaxJson j = new AjaxJson();
        j.setAttributes(new HashMap<String, Object>());
        try {
            String param = oConvertUtils.getString(req.getParameter("param"));
            int page = oConvertUtils.getInt(req.getParameter("currPage"));
            String column = getTypeColumn(client.getDeptID(),null, "possales");
            StringBuffer sb = new StringBuffer(); //添加 返回最大的尺码编号
            sb.append( 
                    " select GoodsID,name+'('+code+')' Name,name GoodsName,Code,g.PresentFlag,isnull(TradePrice,0) TradePrice,isnull(RetailSales,0) RetailSales,isnull(g.Discount,10) Discount,DiscountFlag,isnull("+ column +",0) UnitPrice,"
                            + "(select GoodsType from GoodsType where GoodsTypeID = g.GoodsTypeID) GoodsType,SubType,Age,Season,(select Serial from BrandSerial where BrandSerialID = g.BrandSerialID) BrandSerial,Style,Sex,Kind,Model,"
                            + "isnull(RetailSales2,0) RetailSales2,isnull(RetailSales3,0) RetailSales3,isnull(RetailSales4,0) RetailSales4,isnull(RetailSales5,0) RetailSales5,"
                            + "isnull(RetailSales6,0) RetailSales6,isnull(RetailSales7,0) RetailSales7,isnull(RetailSales8,0) RetailSales8,isnull(SalesPrice1,0) SalesPrice1,"
                            + "isnull(SalesPrice2,0) SalesPrice2,isnull(SalesPrice3,0) SalesPrice3,isnull(SalesPrice4,0) SalesPrice4,isnull(SalesPrice5,0) SalesPrice5, "
                            + "isnull(SalesPrice6,0) SalesPrice6,isnull(SalesPrice7,0) SalesPrice7,isnull(SalesPrice8,0) SalesPrice8,sizIndex=(select max(no) as maxsize from SizeGroupSize where sizeGroupId=g.GroupID) from  Goods g where g.code like '%")
                            .append(param).append("%' or g.Name like '%").append(param).append("%' or g.GoodsID like '%").append(param)
                            .append("%' order by len(code) asc ");
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
     * 模糊查询货品为赠品的编码(获取货品编号)
     * 
     * @param req
     * @return
     */
    @RequestMapping(params = "getPresentGoods")
    @ResponseBody
    public AjaxJson getPresentGoods(HttpServletRequest req) {
        AjaxJson j = new AjaxJson();
        j.setAttributes(new HashMap<String, Object>());
        try {
            String param = oConvertUtils.getString(req.getParameter("param"));
            int page = oConvertUtils.getInt(req.getParameter("currPage"));
            StringBuffer sb = new StringBuffer();
            sb.append(" select GoodsID,name+'('+code+')' Name,name GoodsName,Code,isnull(TradePrice,0) TradePrice,isnull(RetailSales,0) RetailSales,isnull(g.Discount,10) Discount,DiscountFlag,isnull(RetailSales1,0) RetailSales1,")
                    .append("(select GoodsType from GoodsType where GoodsTypeID = g.GoodsTypeID) GoodsType,SubType,Age,Season,(select Serial from BrandSerial where BrandSerialID = g.BrandSerialID) BrandSerial,Style,Sex,Kind,Model,")
                    .append("isnull(RetailSales2,0) RetailSales2,isnull(RetailSales3,0) RetailSales3,isnull(RetailSales4,0) RetailSales4,isnull(RetailSales5,0) RetailSales5,")
                    .append("isnull(RetailSales6,0) RetailSales6,isnull(RetailSales7,0) RetailSales7,isnull(RetailSales8,0) RetailSales8,isnull(SalesPrice1,0) SalesPrice1,")
                    .append("isnull(SalesPrice2,0) SalesPrice2,isnull(SalesPrice3,0) SalesPrice3,isnull(SalesPrice4,0) SalesPrice4,isnull(SalesPrice5,0) SalesPrice5, ")
                    .append("isnull(SalesPrice6,0) SalesPrice6,isnull(SalesPrice7,0) SalesPrice7,isnull(SalesPrice8,0) SalesPrice8 from  Goods g where g.PresentFlag = 1 and g.goodsId in (select goodsId from Goods where code like '%").append(param).append("%' or Name like '%").append(param)
                    .append("%' or GoodsID like '%").append(param).append("%') order by len(code) asc ");
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
     * 模糊查询商品条码(获取商品条码)
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
            String param = oConvertUtils.getString(req.getParameter("param"));
            int page = oConvertUtils.getInt(req.getParameter("currPage"));
            StringBuffer sb = new StringBuffer();
            sb.append(" select Barcode Name from  Barcode b where b.Barcode <> '' and b.Barcode is not null and b.Barcode like '%").append(param).append("%' order by len(b.Barcode) asc ");
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
     * 根据货品编号,商品条码获取货品信息
     * 
     * @param req
     * @return
     */
    @RequestMapping(params = "getGoodsByCode")
    @ResponseBody
    public AjaxJson getGoodsByCode(HttpServletRequest req) {
        AjaxJson j = new AjaxJson();
        j.setAttributes(new HashMap<String, Object>());
        try {
            String code = oConvertUtils.getString(req.getParameter("goodsCode"));
            // 根据条码获取货品ID
            String goodsId = getGoodsId(code, commonDao);
            StringBuffer sb = new StringBuffer();
            sb.append(" select Name,isnull(TradePrice,0) TradePrice,isnull(RetailSales,0) RetailSales, Code from  Goods g where g.goodsId = ?  ");
            List list = commonDao.findForJdbc(sb.toString(), goodsId);
            if (list.size() < 1) {
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
     * 模糊查询货品类别(获取货品类别)
     * 
     * @param req
     * @return
     */
    @RequestMapping(params = "getGoodsType")
    @ResponseBody
    public AjaxJson getGoodsType(HttpServletRequest req) {
        AjaxJson j = new AjaxJson();
        j.setAttributes(new HashMap<String, Object>());
        try {
            String param = oConvertUtils.getString(req.getParameter("param"));
            int page = oConvertUtils.getInt(req.getParameter("currPage"));
            StringBuffer sb = new StringBuffer();
            sb.append(" select GoodsTypeID,GoodsType Name from GoodsType gt where gt.code like '%").append(param).append("%' or gt.goodsType like '%").append(param).append("%' order by len(GoodsType) asc ");
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
     * 模糊查询单据结算方式(获取单据结算方式)
     * 
     * @param req
     * @return
     */
    @RequestMapping(params = "getPaymentType")
    @ResponseBody
    public AjaxJson getPaymentType(HttpServletRequest req) {
        AjaxJson j = new AjaxJson();
        j.setAttributes(new HashMap<String, Object>());
        try {
            String param = oConvertUtils.getString(req.getParameter("param"));
            int page = oConvertUtils.getInt(req.getParameter("currPage"));
            StringBuffer sb = new StringBuffer();
            sb.append(" select PaymentTypeID,PaymentType Name from PaymentType gt where gt.type = '收款' and gt.PaymentType like '%").append(param).append("%' order by len(PaymentType) asc ");
            System.out.println("sql语句："+sb.toString());
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
     * 模糊查询货品子类别(获取货品子类别)
     * 
     * @param req
     * @return
     */
    @RequestMapping(params = "getGoodsSubType")
    @ResponseBody
    public AjaxJson getGoodsSubType(HttpServletRequest req) {
        AjaxJson j = new AjaxJson();
        j.setAttributes(new HashMap<String, Object>());
        try {
            String param = oConvertUtils.getString(req.getParameter("param"));
            int page = oConvertUtils.getInt(req.getParameter("currPage"));
            StringBuffer sb = new StringBuffer();
            sb.append(" select SubType Name from Goods g where SubType is not null and SubType <> '' ");
            if (null != param && !param.isEmpty() && !"".equals(param)) {
                sb.append(" and SubType like '%").append(param).append("%' ");
            }
            sb.append(" group by SubType order by len(SubType) asc ");
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
     * 模糊查询货品系列(获取货品系列)
     * 
     * @param req
     * @return
     */
    @RequestMapping(params = "getGoodsBrandSerial")
    @ResponseBody
    public AjaxJson getGoodsBrandSerial(HttpServletRequest req) {
        AjaxJson j = new AjaxJson();
        j.setAttributes(new HashMap<String, Object>());
        try {
            String param = oConvertUtils.getString(req.getParameter("param"));
            int page = oConvertUtils.getInt(req.getParameter("currPage"));
            StringBuffer sb = new StringBuffer();
            sb.append(" select BrandSerialID, Serial Name from BrandSerial bs order by len(Serial) asc ");
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
     * 模糊查询货品厂商(获取货品厂商)
     * 
     * @param req
     * @return
     */
    @RequestMapping(params = "getGoodsSupplier")
    @ResponseBody
    public AjaxJson getGoodsSupplier(HttpServletRequest req) {
        AjaxJson j = new AjaxJson();
        j.setAttributes(new HashMap<String, Object>());
        try {
            String param = oConvertUtils.getString(req.getParameter("param"));
            int page = oConvertUtils.getInt(req.getParameter("currPage"));
            StringBuffer sb = new StringBuffer();
            sb.append(" select SupplierID,Supplier Name,isnull(DiscountRate,0) DiscountRate from Supplier s where s.code like '%").append(param).append("%' or s.Supplier like '%").append(param).append("%' order by len(Supplier) asc ");
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
     * 模糊查询客户类别(获取客户类别)
     * 
     * @param req
     * @return
     */
    @RequestMapping(params = "getCustomerType")
    @ResponseBody
    public AjaxJson getCustomerType(HttpServletRequest req) {
        AjaxJson j = new AjaxJson();
        j.setAttributes(new HashMap<String, Object>());
        try {
            int page = oConvertUtils.getInt(req.getParameter("currPage"));
            StringBuffer sb = new StringBuffer();
            sb.append(" select CustomerTypeID,CustomerType Name from customerType order by len(CustomerType) asc ");
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
     * 模糊查询货品颜色(获取货品颜色)
     * 
     * @param req
     * @return
     */
    @RequestMapping(params = "getGoodsColor")
    @ResponseBody
    public AjaxJson getGoodsColor(HttpServletRequest req) {
        AjaxJson j = new AjaxJson();
        j.setAttributes(new HashMap<String, Object>());
        try {
            String param = oConvertUtils.getString(req.getParameter("param"));
            int page = oConvertUtils.getInt(req.getParameter("currPage"));
            StringBuffer sb = new StringBuffer();
            sb.append(" select ColorID,Color Name from Color c where c.No like '%").append(param).append("%' or c.Color like '%").append(param).append("%' order by len(Color) asc ");
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
     * 获取进仓单单据类别
     * 
     * @param req
     * @return
     */
    @RequestMapping(params = "getStockTypeIn")
    @ResponseBody
    public AjaxJson getStockTypeIn(HttpServletRequest req) {
        AjaxJson j = new AjaxJson();
        j.setAttributes(new HashMap<String, Object>());
        try {
            int page = oConvertUtils.getInt(req.getParameter("currPage"));
            String param = oConvertUtils.getString(req.getParameter("param"));
            boolean flag = param.matches("@[a-zA-Z0-9]{3}@");
            if (flag) {
                deptId = param.substring(1, param.length() - 1);
                param = "";
            }
            StringBuffer sb = new StringBuffer();
            sb.append(" select Type Name from stock s where direction='1' ");
            if (null != deptId && !deptId.isEmpty()) {
                sb.append(" and departmentId = '").append(deptId).append("' ");
            }
            sb.append(" group by Type order by len(Type) asc ");
            List list = commonDao.findForJdbc(sb.toString(), page, 15);
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("Name", "全部");
            list.add(0, map);
            j.setObj(list);
        } catch (Exception e) {
            j.setSuccess(false);
            j.setMsg(e.getMessage());
            SysLogger.error(e.getMessage(), e);
        }
        return j;
    }

    /**
     * 获取进仓单单据类别(转仓进仓类别除外) 仓位管理
     * 
     * @param req
     * @return
     */
    @RequestMapping(params = "getDocTypeIn")
    @ResponseBody
    public AjaxJson getDocTypeIn(HttpServletRequest req) {
        AjaxJson j = new AjaxJson();
        j.setAttributes(new HashMap<String, Object>());
        try {
            int page = oConvertUtils.getInt(req.getParameter("currPage"));
            String param = oConvertUtils.getString(req.getParameter("param"));
            boolean flag = param.matches("@[a-zA-Z0-9]{3}@");
            if (flag) {
                deptId = param.substring(1, param.length() - 1);
                param = "";
            }
            StringBuffer sb = new StringBuffer();
            sb.append(" select Type Name from stock s where direction='1' and AuditFlag = '1' and type not in ('转仓进仓','盘盈','调整','差异','返修','报溢')  ");
            if (null != deptId && !deptId.isEmpty()) {
                sb.append(" and departmentId = '").append(deptId).append("' ");
            }
            sb.append(" group by Type order by len(Type) asc ");
            List list = commonDao.findForJdbc(sb.toString(), page, 15);
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("Name", "全部");
            list.add(0, map);
            j.setObj(list);
        } catch (Exception e) {
            j.setSuccess(false);
            j.setMsg(e.getMessage());
            SysLogger.error(e.getMessage(), e);
        }
        return j;
    }

    /**
     * 获取出仓单单据类别
     * 
     * @param req
     * @return
     */
    @RequestMapping(params = "getStockTypeOut")
    @ResponseBody
    public AjaxJson getStockTypeOut(HttpServletRequest req) {
        AjaxJson j = new AjaxJson();
        j.setAttributes(new HashMap<String, Object>());
        try {
            int page = oConvertUtils.getInt(req.getParameter("currPage"));
            String param = oConvertUtils.getString(req.getParameter("param"));
            boolean flag = param.matches("@[a-zA-Z0-9]{3}@");
            if (flag) {
                deptId = param.substring(1, param.length() - 1);
                param = "";
            }
            StringBuffer sb = new StringBuffer();
            sb.append(" select Type Name from stock s where direction='-1' ");
            if (null != deptId && !deptId.isEmpty()) {
                sb.append(" and departmentId = '").append(deptId).append("' ");
            }
            sb.append(" group by Type order by len(Type) asc ");
            List list = commonDao.findForJdbc(sb.toString(), page, 15);
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("Name", "全部");
            list.add(0, map);
            j.setObj(list);
        } catch (Exception e) {
            j.setSuccess(false);
            j.setMsg(e.getMessage());
            SysLogger.error(e.getMessage(), e);
        }
        return j;
    }

    /**
     * 获取出仓单单据类别(转仓出仓类别除外) 仓位管理
     * 
     * @param req
     * @return
     */
    @RequestMapping(params = "getDocTypeOut")
    @ResponseBody
    public AjaxJson getDocTypeOut(HttpServletRequest req) {
        AjaxJson j = new AjaxJson();
        j.setAttributes(new HashMap<String, Object>());
        try {
            int page = oConvertUtils.getInt(req.getParameter("currPage"));
            String param = oConvertUtils.getString(req.getParameter("param"));
            boolean flag = param.matches("@[a-zA-Z0-9]{3}@");
            if (flag) {
                deptId = param.substring(1, param.length() - 1);
                param = "";
            }
            StringBuffer sb = new StringBuffer();
            sb.append(" select Type Name from stock s where direction='-1' and AuditFlag = '0' and type not in ('转仓出仓','盘亏','调整','赠品','差异','领用','返修','报损')  ");
            if (null != deptId && !deptId.isEmpty()) {
                sb.append(" and departmentId = '").append(deptId).append("' ");
            }
            sb.append(" group by Type order by len(Type) asc ");
            List list = commonDao.findForJdbc(sb.toString(), page, 15);
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("Name", "全部");
            list.add(0, map);
            j.setObj(list);
        } catch (Exception e) {
            j.setSuccess(false);
            j.setMsg(e.getMessage());
            SysLogger.error(e.getMessage(), e);
        }
        return j;
    }

    /**
     * 获取年份
     * 
     * @param req
     * @return
     */
    @RequestMapping(params = "getYear")
    @ResponseBody
    public AjaxJson getYear(HttpServletRequest req) {
        AjaxJson j = new AjaxJson();
        j.setAttributes(new HashMap<String, Object>());
        try {
            int page = oConvertUtils.getInt(req.getParameter("currPage"));
            StringBuffer sb = new StringBuffer();
            sb.append(" select g.Age Name from  goods g where g.Age is not null and g.Age <> '' group by g.Age order by g.Age asc ");
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
     * 模糊查询货品品牌信息(获取货品品牌信息)
     * 
     * @param req
     * @return
     */
    @RequestMapping(params = "getBrand")
    @ResponseBody
    public AjaxJson getBrand(HttpServletRequest req) {
        AjaxJson j = new AjaxJson();
        j.setAttributes(new HashMap<String, Object>());
        try {
            String param = oConvertUtils.getString(req.getParameter("param"));
            int page = oConvertUtils.getInt(req.getParameter("currPage"));
            StringBuffer sb = new StringBuffer();
            sb.append(" select BrandID,Brand Name from Brand b where b.code like '%").append(param).append("%' or b.brand like '%").append(param).append("%' order by len(Brand) asc ");
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
     * 通过品牌权限模糊查询品牌信息(通过品牌权限获取品牌)
     * 
     * @param req
     * @return
     */
    @RequestMapping(params = "getBrandByPower")
    @ResponseBody
    public AjaxJson getBrandByPower(HttpServletRequest req) {
        AjaxJson j = new AjaxJson();
        j.setAttributes(new HashMap<String, Object>());
        try {
            Client client = ResourceUtil.getClientFromSession(req);
            String param = oConvertUtils.getString(req.getParameter("param"));
            int page = oConvertUtils.getInt(req.getParameter("currPage"));
            StringBuffer sb = new StringBuffer();
            sb.append(" select b.BrandID,Brand Name from BrandRight br join Brand b on b.brandId = br.brandId where rightFlag = '1' and userId = '" + client.getUserID() + "' ");
            if (null != param && !param.isEmpty() && !"".equals(param)) {
                sb.append(" and b.code like '%").append(param).append("%' or b.brand like '%").append(param).append("%' order by len(Brand) asc ");
            }
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
     * 模糊查询销售订单单号
     * 
     * @param req
     * @return
     */
    @RequestMapping(params = "getSalesOrderNo")
    @ResponseBody
    public AjaxJson getSalesOrderNo(HttpServletRequest req) {
        Client client = ResourceUtil.getClientFromSession(req);
        Map<String, String> map = client.getMap();
        String userRight = map.get(client.getUserID());
        AjaxJson j = new AjaxJson();
        j.setAttributes(new HashMap<String, Object>());
        try {
            String param = oConvertUtils.getString(req.getParameter("param"));
            int page = oConvertUtils.getInt(req.getParameter("currPage"));
            StringBuffer sb = new StringBuffer();
            sb.append(" select No Name from SalesOrder where WarehouseID in (").append(userRight).append(") ");
            if (null != param && !param.isEmpty() && !"".equals(param)) {
                sb.append(" and No like '%").append(param).append("%' order by len(No) asc ");
            }
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
     * 模糊查询销售发货单单号[已完成装箱扫描]
     * 
     * @param req
     * @return
     */
    @RequestMapping(params = "getPackingBoxSalesNoOfComplete")
    @ResponseBody
    public AjaxJson getPackingBoxSalesNoOfComplete(HttpServletRequest req) {
        Client client = ResourceUtil.getClientFromSession(req);
        Map<String, String> map = client.getMap();
        String userRight = map.get(client.getUserID());
        AjaxJson j = new AjaxJson();
        j.setAttributes(new HashMap<String, Object>());
        try {
            String param = oConvertUtils.getString(req.getParameter("param"));
            int page = oConvertUtils.getInt(req.getParameter("currPage"));
            StringBuffer sb = new StringBuffer();
            sb.append(" select No Name from Sales where direction='1' and departmentId in (").append(userRight).append(") and " + " salesId in (select relationId from PackingBox where relationId is not null and relationId <> '') ");
            if (null != param && !param.isEmpty() && !"".equals(param)) {
                sb.append(" and No like '%").append(param).append("%' ");
            }
            sb.append(" order by No asc ");
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
     * 模糊查询销售发货单单号[装箱扫描]
     * 
     * @param req
     * @return
     */
    @RequestMapping(params = "getPackingBoxSalesNo")
    @ResponseBody
    public AjaxJson getPackingBoxSalesNo(HttpServletRequest req) {
        Client client = ResourceUtil.getClientFromSession(req);
        Map<String, String> map = client.getMap();
        String userRight = map.get(client.getUserID());
        AjaxJson j = new AjaxJson();
        j.setAttributes(new HashMap<String, Object>());
        try {
            String param = oConvertUtils.getString(req.getParameter("param"));
            int page = oConvertUtils.getInt(req.getParameter("currPage"));
            StringBuffer sb = new StringBuffer();
            sb.append(" select No Name from Sales where direction='1' and departmentId in (").append(userRight).append(") and " + " salesId not in (select relationId from PackingBox where relationId is not null and relationId <> '') ");
            if (null != param && !param.isEmpty() && !"".equals(param)) {
                sb.append(" and No like '%").append(param).append("%' ");
            }
            sb.append(" order by No asc ");
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
     * 模糊查询出仓单单号[装箱扫描]
     * 
     * @param req
     * @return
     */
    @RequestMapping(params = "getPackingBoxStockOutNo")
    @ResponseBody
    public AjaxJson getPackingBoxStockOutNo(HttpServletRequest req) {
        Client client = ResourceUtil.getClientFromSession(req);
        Map<String, String> map = client.getMap();
        String userRight = map.get(client.getUserID());
        AjaxJson j = new AjaxJson();
        j.setAttributes(new HashMap<String, Object>());
        try {
            String param = oConvertUtils.getString(req.getParameter("param"));
            int page = oConvertUtils.getInt(req.getParameter("currPage"));
            StringBuffer sb = new StringBuffer();
            sb.append(" select No Name from stock where direction='-1' and departmentId in (").append(userRight).append(") and " + " stockId not in (select relationId from PackingBox where relationId is not null and relationId <> '') ");
            if (null != param && !param.isEmpty() && !"".equals(param)) {
                sb.append(" and No like '%").append(param).append("%' ");
            }
            sb.append(" order by No asc ");
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
     * 模糊查询出仓单单号[已完成装箱扫描]
     * 
     * @param req
     * @return
     */
    @RequestMapping(params = "getPackingBoxStockOutNoOfComplete")
    @ResponseBody
    public AjaxJson getPackingBoxStockOutNoOfComplete(HttpServletRequest req) {
        Client client = ResourceUtil.getClientFromSession(req);
        Map<String, String> map = client.getMap();
        String userRight = map.get(client.getUserID());
        AjaxJson j = new AjaxJson();
        j.setAttributes(new HashMap<String, Object>());
        try {
            String param = oConvertUtils.getString(req.getParameter("param"));
            int page = oConvertUtils.getInt(req.getParameter("currPage"));
            StringBuffer sb = new StringBuffer();
            sb.append(" select No Name from stock where direction='-1' and departmentId in (").append(userRight).append(") and " + " stockId in (select relationId from PackingBox where relationId is not null and relationId <> '') ");
            if (null != param && !param.isEmpty() && !"".equals(param)) {
                sb.append(" and No like '%").append(param).append("%' ");
            }
            sb.append(" order by No asc ");
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
     * 模糊查询销售发货单单号
     * 
     * @param req
     * @return
     */
    @RequestMapping(params = "getSalesNo")
    @ResponseBody
    public AjaxJson getSalesNo(HttpServletRequest req) {
        Client client = ResourceUtil.getClientFromSession(req);
        Map<String, String> map = client.getMap();
        String userRight = map.get(client.getUserID());
        AjaxJson j = new AjaxJson();
        j.setAttributes(new HashMap<String, Object>());
        try {
            String param = oConvertUtils.getString(req.getParameter("param"));
            int page = oConvertUtils.getInt(req.getParameter("currPage"));
            StringBuffer sb = new StringBuffer();
            sb.append(" select No Name from Sales where direction='1' and departmentId in (").append(userRight).append(") ");
            if (null != param && !param.isEmpty() && !"".equals(param)) {
                sb.append(" and No like '%").append(param).append("%' ");
            }
            sb.append(" order by No asc ");
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
     * 模糊查询销售退货单单号
     * 
     * @param req
     * @return
     */
    @RequestMapping(params = "getSalesReturnNo")
    @ResponseBody
    public AjaxJson getSalesReturnNo(HttpServletRequest req) {
        Client client = ResourceUtil.getClientFromSession(req);
        Map<String, String> map = client.getMap();
        String userRight = map.get(client.getUserID());
        AjaxJson j = new AjaxJson();
        j.setAttributes(new HashMap<String, Object>());
        try {
            String param = oConvertUtils.getString(req.getParameter("param"));
            int page = oConvertUtils.getInt(req.getParameter("currPage"));
            StringBuffer sb = new StringBuffer();
            sb.append(" select No Name from Sales where direction='-1' and departmentId in (").append(userRight).append(") ");
            if (null != param && !param.isEmpty() && !"".equals(param)) {
                sb.append(" and No like '%").append(param).append("%' ");
            }
            sb.append(" order by No asc ");
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
     * 模糊查询采购收货单单号
     * 
     * @param req
     * @return
     */
    @RequestMapping(params = "getPurchaseNo")
    @ResponseBody
    public AjaxJson getPurchaseNo(HttpServletRequest req) {
        Client client = ResourceUtil.getClientFromSession(req);
        Map<String, String> map = client.getMap();
        String userRight = map.get(client.getUserID());
        AjaxJson j = new AjaxJson();
        j.setAttributes(new HashMap<String, Object>());
        try {
            String param = oConvertUtils.getString(req.getParameter("param"));
            int page = oConvertUtils.getInt(req.getParameter("currPage"));
            StringBuffer sb = new StringBuffer();
            sb.append(" select No Name from Purchase where direction='1' and departmentId in (").append(userRight).append(") ");
            if (null != param && !param.isEmpty() && !"".equals(param)) {
                sb.append(" and No like '%").append(param).append("%' ");
            }
            sb.append(" order by No asc ");
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
     * 模糊查询转仓单单号
     * 
     * @param req
     * @return
     */
    @RequestMapping(params = "getStockMoveNo")
    @ResponseBody
    public AjaxJson getStockMoveNo(HttpServletRequest req) {
        Client client = ResourceUtil.getClientFromSession(req);
        Map<String, String> map = client.getMap();
        String userRight = map.get(client.getUserID());
        AjaxJson j = new AjaxJson();
        j.setAttributes(new HashMap<String, Object>());
        try {
            String param = oConvertUtils.getString(req.getParameter("param"));
            int page = oConvertUtils.getInt(req.getParameter("currPage"));
            StringBuffer sb = new StringBuffer();
            sb.append(" select No Name from StockMove where departmentId in (").append(userRight).append(") ");
            if (null != param && !param.isEmpty() && !"".equals(param)) {
                sb.append(" and No like '%").append(param).append("%' ");
            }
            sb.append(" order by No asc ");
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
     * 模糊查询采购退货单单号
     * 
     * @param req
     * @return
     */
    @RequestMapping(params = "getPurchaseReturnNo")
    @ResponseBody
    public AjaxJson getPurchaseReturnNo(HttpServletRequest req) {
        Client client = ResourceUtil.getClientFromSession(req);
        Map<String, String> map = client.getMap();
        String userRight = map.get(client.getUserID());
        AjaxJson j = new AjaxJson();
        j.setAttributes(new HashMap<String, Object>());
        try {
            String param = oConvertUtils.getString(req.getParameter("param"));
            int page = oConvertUtils.getInt(req.getParameter("currPage"));
            StringBuffer sb = new StringBuffer();
            sb.append(" select No Name from Purchase where direction='-1' and departmentId in (").append(userRight).append(") ");
            if (null != param && !param.isEmpty() && !"".equals(param)) {
                sb.append(" and No like '%").append(param).append("%' ");
            }
            sb.append(" order by No asc ");
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
     * 模糊查询盘点单单号
     * 
     * @param req
     * @return
     */
    @RequestMapping(params = "getStocktakingNo")
    @ResponseBody
    public AjaxJson getStocktakingNo(HttpServletRequest req) {
        Client client = ResourceUtil.getClientFromSession(req);
        Map<String, String> map = client.getMap();
        String userRight = map.get(client.getUserID());
        AjaxJson j = new AjaxJson();
        j.setAttributes(new HashMap<String, Object>());
        try {
            String param = oConvertUtils.getString(req.getParameter("param"));
            int page = oConvertUtils.getInt(req.getParameter("currPage"));
            StringBuffer sb = new StringBuffer();
            sb.append(" select No Name from Stocktaking where departmentId in (").append(userRight).append(") ");
            if (null != param && !param.isEmpty() && !"".equals(param)) {
                sb.append(" and No like '%").append(param).append("%' ");
            }
            sb.append(" order by No asc ");
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
     * 获取季节
     * 
     * @param req
     * @return
     */
    @RequestMapping(params = "getSeason")
    @ResponseBody
    public AjaxJson getSeason(HttpServletRequest req) {
        AjaxJson j = new AjaxJson();
        j.setAttributes(new HashMap<String, Object>());
        try {
            int page = oConvertUtils.getInt(req.getParameter("currPage"));
            StringBuffer sb = new StringBuffer();
            sb.append(" select g.Season Name from  goods g where g.Season is not null and g.Season <> '' group by g.Season order by len(g.Season) asc ");
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
     * 模糊查询经手人(获取经手人)
     * 
     * @param req
     * @return
     */
    @RequestMapping(params = "getEmployee")
    @ResponseBody
    public AjaxJson getEmployee(HttpServletRequest req) {
        Client client = ResourceUtil.getClientFromSession(req);
        Map<String, String> map = client.getMap();
        String userRight = map.get(client.getUserID());
        AjaxJson j = new AjaxJson();
        j.setAttributes(new HashMap<String, Object>());
        try {
            String param = oConvertUtils.getString(req.getParameter("param"));
            int page = oConvertUtils.getInt(req.getParameter("currPage"));
            StringBuffer sb = new StringBuffer();
            sb.append(" select  Name,EmployeeID,departmentId BusinessDeptID, (select department from department where departmentId = e.departmentId) BusinessDeptName from Employee e where departmentId = '" + client.getDeptID() + "' ");
            if (null != param && !param.isEmpty() && !"".equals(param)) {
                sb.append(" and EmployeeID in( select EmployeeID from Employee where Code like '%").append(param).append("%' or Name like '%").append(param).append("%' ) order by len(Name) asc ");
            }
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
     * 根据部门获取部门下的仓位信息
     * 
     * @param req
     * @return
     */
    @RequestMapping(params = "getStorageByInner")
    @ResponseBody
    public AjaxJson getStorageByInner(HttpServletRequest req) {
        AjaxJson j = new AjaxJson();
        j.setAttributes(new HashMap<String, Object>());
        try {
            String param = oConvertUtils.getString(req.getParameter("param"));
            boolean flag = param.matches("@[a-zA-Z0-9]{3}@");
            if (flag) {
                deptId = param.substring(1, param.length() - 1);
                param = "";
            }
            int page = oConvertUtils.getInt(req.getParameter("currPage"));
            StringBuffer sb = new StringBuffer();
            sb.append(" select StorageID,Storage Name from Storage where 1=1 ");
            if (null != param && !param.isEmpty() && !"".equals(param)) {
                sb.append(" and StorageID in( select StorageID from Storage where Code like '%").append(param).append("%' or storage like '%").append(param).append("%' ) ");
            }
            if (null != deptId && !deptId.isEmpty()) {
                sb.append(" and departmentId = '").append(deptId).append("' ");
            }
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
     * 通过用户部门权限获取仓位
     * 
     * @param req
     * @return
     */
    @RequestMapping(params = "getStorage")
    @ResponseBody
    public AjaxJson getStorage(HttpServletRequest req) {
        Client client = ResourceUtil.getClientFromSession(req);
        Map<String, String> map = client.getMap();
        String userRight = map.get(client.getUserID());
        AjaxJson j = new AjaxJson();
        j.setAttributes(new HashMap<String, Object>());
        try {
            String param = oConvertUtils.getString(req.getParameter("param"));
            int page = oConvertUtils.getInt(req.getParameter("currPage"));
            StringBuffer sb = new StringBuffer();
            sb.append(" select StorageID,Storage Name from Storage where StorageID != '-1' and departmentId in (").append(userRight).append(") ");
            if (null != param && !param.isEmpty() && !"".equals(param)) {
                sb.append(" and StorageID in( select StorageID from Storage where Code like '%").append(param).append("%' or storage like '%").append(param).append("%' ) ");
            }
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
     * 模糊查询会员(获取会员)
     * 
     * @param req
     * @return
     */
    @RequestMapping(params = "getVip")
    @ResponseBody
    public AjaxJson getVip(HttpServletRequest req) {
        Client client = ResourceUtil.getClientFromSession(req);
        AjaxJson j = new AjaxJson();
        j.setAttributes(new HashMap<String, Object>());
        try {
            String param = oConvertUtils.getString(req.getParameter("param"));
            int page = oConvertUtils.getInt(req.getParameter("currPage"));
            StringBuffer sb = new StringBuffer();
            sb.append(
                    " select vip Name,isnull(vt.Discount,10) DiscountRate,isnull(vt.PointRateBirthday,0) BirthdayDiscount,Multiples PointRate,v.* from vip v join "
                            + " vipdiscount vt on v.viptypeId = vt.vipTypeID and v.DepartmentID = vt.DepartmentID where StopFlag ='0' and v.vip <> '' and v.DepartmentID = '").append(client.getDeptID()).append("' and v.vipid not in(select vipid from vip where EndDate  <= getdate() )  ");
            if (null != param && !param.isEmpty() && !"".equals(param)) {
                sb.append(" and Code like '%").append(param).append("%' or vip like '%").append(param).append("%' or mobilePhone like '%").append(param).append("%' order by len(vip) asc");
            }
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
     * 根据GoodsID(货品编码)检查区域是否重版
     * 
     * @param req
     * @return
     */
    @RequestMapping(params = "checkGoodsArea")
    @ResponseBody
    public AjaxJson checkGoodsArea(HttpServletRequest req) {
        AjaxJson j = new AjaxJson();
        j.setAttributes(new HashMap<String, Object>());
        try {
            boolean checkFlag = false;
            String goodsId = oConvertUtils.getString(req.getParameter("goodsId"));
            String districtId = oConvertUtils.getString(req.getParameter("districtId"));
            String customerId = oConvertUtils.getString(req.getParameter("customerId"));
            StringBuffer sb = new StringBuffer();
            sb.append(" select top 1 CustomerID,(select Customer from Customer where CustomerID = t.CustomerID) Customer,DistrictID," + "(select District from District where DistrictID = t.DistrictID) District,GoodsID,MadeByDate from ( select CustomerID,(select DistrictID from Customer "
                    + " where customerId = a.customerId) DistrictID,GoodsID,MadeByDate from SalesOrder a join SalesOrderDetailTemp b " + " on a.SalesOrderID = b.SalesOrderID group by CustomerID,GoodsID,MadeByDate union all select CustomerID,(select DistrictID from Customer where "
                    + " customerId = a.customerId) DistrictID,GoodsID,MadeByDate from Sales a join SalesDetailTemp b on a.SalesID = b.SalesID group by " + " CustomerID,GoodsID,MadeByDate ) as t where DistrictID <> '' and DistrictID is not null and goodsId = ? and DistrictID = ? "
                    + " group by CustomerID,DistrictID,GoodsID,MadeByDate order by MadeByDate ");
            List list = commonDao.findForJdbc(sb.toString(), goodsId, districtId);
            for (int i = 0; i < list.size(); i++) {
                Map<String, Object> map = (Map<String, Object>) list.get(i);
                String tcustomerId = (String) map.get("CustomerID");
                if (tcustomerId.equals(customerId)) {
                    list.remove(map);
                    i--;
                }
            }
            if (list.size() > 0) {
                // 存在地区重版
                checkFlag = true;
                // 获取存在重版的客户和地区
                sb = new StringBuffer();
                sb.append(" select CustomerID,(select Customer from Customer where CustomerID = t.CustomerID) Customer,DistrictID," + "(select District from District where DistrictID = t.DistrictID) District,GoodsID from ( select CustomerID,(select DistrictID from Customer "
                        + " where customerId = a.customerId) DistrictID,GoodsID,MadeByDate from SalesOrder a join SalesOrderDetailTemp b " + " on a.SalesOrderID = b.SalesOrderID group by CustomerID,GoodsID,MadeByDate union all select CustomerID,(select DistrictID from Customer where "
                        + " customerId = a.customerId) DistrictID,GoodsID,MadeByDate from Sales a join SalesDetailTemp b on a.SalesID = b.SalesID group by "
                        + " CustomerID,GoodsID,MadeByDate ) as t where DistrictID <> '' and DistrictID is not null and goodsId = ? and DistrictID = ? and CustomerID <> ? " + " group by CustomerID,DistrictID,GoodsID ");
                list = commonDao.findForJdbc(sb.toString(), goodsId, districtId, customerId);
            }
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("checkFlag", checkFlag);
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
     * 根据厂商货品编码检查区域是否重版
     * 
     * @param req
     * @return
     */
    @RequestMapping(params = "checkGoodsAreaBySupplierCode")
    @ResponseBody
    public AjaxJson checkGoodsAreaBySupplierCode(HttpServletRequest req) {
        AjaxJson j = new AjaxJson();
        j.setAttributes(new HashMap<String, Object>());
        try {
            boolean checkFlag = false;
            boolean isnull = false;
            List list = null;
            Map<String, Object> map = new HashMap<String, Object>();
            String goodsId = oConvertUtils.getString(req.getParameter("goodsId"));
            String districtId = oConvertUtils.getString(req.getParameter("districtId"));
            String customerId = oConvertUtils.getString(req.getParameter("customerId"));
            // 通过货品ID获取厂商货品编码
            String supplierCode = (String) commonDao.getData(" select SupplierCode from goods where goodsId = ? ", goodsId);
            // 厂商货品编码为空时,不检查货品区域
            if (supplierCode == null || "".equals(supplierCode) || "null".equalsIgnoreCase(supplierCode)) {
                list = new ArrayList();
                checkFlag = true;
                isnull = true;
            } else {
                StringBuffer sb = new StringBuffer();
                sb.append(" select top 1 CustomerID,(select Customer from Customer where CustomerID = t.CustomerID) Customer,DistrictID," + "(select District from District where DistrictID = t.DistrictID) District,SupplierCode,MadeByDate from ( select CustomerID,(select DistrictID from Customer "
                        + " where customerId = a.customerId) DistrictID,(select SupplierCode from  Goods where GoodsID = b.GoodsID) SupplierCode,MadeByDate from SalesOrder a join SalesOrderDetailTemp b "
                        + " on a.SalesOrderID = b.SalesOrderID group by CustomerID,GoodsID,MadeByDate union all select CustomerID,(select DistrictID from Customer where "
                        + " customerId = a.customerId) DistrictID,(select SupplierCode from  Goods where GoodsID = b.GoodsID) SupplierCode,MadeByDate from Sales a join SalesDetailTemp b on a.SalesID = b.SalesID group by "
                        + " CustomerID,GoodsID,MadeByDate ) as t where DistrictID <> '' and DistrictID is not null and SupplierCode is not null and SupplierCode <> '' and SupplierCode = ? and DistrictID = ? " + " group by CustomerID,DistrictID,SupplierCode,MadeByDate order by MadeByDate ");
                list = commonDao.findForJdbc(sb.toString(), supplierCode, districtId);
                for (int i = 0; i < list.size(); i++) {
                    Map<String, Object> tmap = (Map<String, Object>) list.get(i);
                    String tcustomerId = (String) tmap.get("CustomerID");
                    if (tcustomerId.equals(customerId)) {
                        list.remove(tmap);
                        i--;
                    }
                }
                if (list.size() > 0) {
                    // 存在地区重版
                    checkFlag = true;
                    // 获取存在重版的客户和地区
                    sb = new StringBuffer();
                    sb.append(" select CustomerID,(select Customer from Customer where CustomerID = t.CustomerID) Customer,DistrictID," + "(select District from District where DistrictID = t.DistrictID) District,SupplierCode from ( select CustomerID,(select DistrictID from Customer "
                            + " where customerId = a.customerId) DistrictID,(select SupplierCode from  Goods where GoodsID = b.GoodsID) SupplierCode,MadeByDate from SalesOrder a join SalesOrderDetailTemp b "
                            + " on a.SalesOrderID = b.SalesOrderID group by CustomerID,GoodsID,MadeByDate union all select CustomerID,(select DistrictID from Customer where "
                            + " customerId = a.customerId) DistrictID,(select SupplierCode from  Goods where GoodsID = b.GoodsID) SupplierCode,MadeByDate from Sales a join SalesDetailTemp b on a.SalesID = b.SalesID group by "
                            + " CustomerID,GoodsID,MadeByDate ) as t where DistrictID <> '' and DistrictID is not null and SupplierCode is not null and SupplierCode <> '' and SupplierCode = ? and DistrictID = ? and CustomerID <> ? " + " group by CustomerID,DistrictID,SupplierCode ");
                    list = commonDao.findForJdbc(sb.toString(), supplierCode, districtId, customerId);
                }
            }
            map.put("checkFlag", checkFlag);
            map.put("isnull", isnull);
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
     * 根据货品ID获取货品所有的颜色和尺码
     * 
     * @param req
     * @return
     */
    @RequestMapping(params = "getColorAndSize")
    @ResponseBody
    public AjaxJson getColorAndSize(HttpServletRequest req) {
        AjaxJson j = new AjaxJson();
        j.setAttributes(new HashMap<String, Object>());
        try {
            String goodsId = oConvertUtils.getString(req.getParameter("goodsId"));
            StringBuffer sb = new StringBuffer();
            // 颜色
            if (LoadUserCount.colorOption == 4) {
                sb.append(" select Color+'('+No+')' Name, ColorID,Color ColorName, No ColorCode from brandcolor c where  brandId = (select brandId from goods where goodsId = '" + goodsId + "') ");
            } else {
                sb.append(" select Color+'('+No+')' Name, ColorID,Color ColorName, No ColorCode from color c  where 1=1 ");
            }
            int exit = Integer.parseInt(String.valueOf(commonDao.getData(" select count(1) from goodsColor where goodsId = ? ", goodsId)));
            if (exit > 0) {// 关联货品颜色表
                sb.append(" and colorId in(select colorId from goodsColor where goodsId = '" + goodsId + "')");
            }
            sb.append(" order by No asc ");
            List colorList = commonDao.findForJdbc(sb.toString());
            // 尺码
            sb = new StringBuffer();
            sb.append(" select Size+'('+convert(varchar,No)+')' Name, SizeID,Size SizeName, No SizeCode from size c  where sizeId in (select sizeId from sizeGroupSize where sizeGroupId = (select GroupId from Goods where goodsId = '" + goodsId + "')) order by No asc ");
            List sizeList = commonDao.findForJdbc(sb.toString());
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("colorList", colorList);
            map.put("sizeList", sizeList);
            j.setObj(map);
        } catch (Exception e) {
            j.setSuccess(false);
            j.setMsg(e.getMessage());
            SysLogger.error(e.getMessage(), e);
        }
        return j;
    }

    /**
     * 根据货号选择颜色(手选货号,颜色,尺码录入数据)
     * 
     * @param req
     * @return
     */
    @RequestMapping(params = "getColorByGoodsCode")
    @ResponseBody
    public AjaxJson getColorByGoodsCode(HttpServletRequest req) {
        AjaxJson j = new AjaxJson();
        j.setAttributes(new HashMap<String, Object>());
        try {
            String param = oConvertUtils.getString(req.getParameter("param"));
            boolean flag = param.matches("^@.*@$");
            if (flag) {
                goodsId = param.substring(1, param.length() - 1);
                param = "";
            }
            int page = oConvertUtils.getInt(req.getParameter("currPage"));
            StringBuffer sb = new StringBuffer();
            if (LoadUserCount.colorOption == 4) {
                sb.append(" select Color+'('+No+')' Name, ColorID, No ColorCode,Color ColorName from brandcolor c where  brandId = (select brandId from goods where goodsId = '" + goodsId + "') ");
            } else {
                sb.append(" select Color+'('+No+')' Name, ColorID, No ColorCode,Color ColorName from color c  where 1=1 ");
            }
            if (null != param && !param.isEmpty() && !"".equals(param)) {
                sb.append(" and ColorID in( select ColorID from color where No like '%").append(param).append("%' or color like '%").append(param).append("%' ) ");
            }
            int exit = Integer.parseInt(String.valueOf(commonDao.getData(" select count(1) from goodsColor where goodsId = ? ", goodsId)));
            if (exit > 0) {// 关联货品颜色表
                sb.append(" and colorId in(select colorId from goodsColor where goodsId = '" + goodsId + "')");
            }
            sb.append(" order by No asc ");
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
     * 根据货号选择尺码(手选货号,颜色,尺码录入数据)
     * 
     * @param req
     * @return
     */
    @RequestMapping(params = "getSizeByGoodsCode")
    @ResponseBody
    public AjaxJson getSizeByGoodsCode(HttpServletRequest req) {
        AjaxJson j = new AjaxJson();
        j.setAttributes(new HashMap<String, Object>());
        try {
            String param = oConvertUtils.getString(req.getParameter("param"));
            boolean flag = param.matches("^@.*@$");
            if (flag) {
                goodsId = param.substring(1, param.length() - 1);
                param = "";
            }
            int page = oConvertUtils.getInt(req.getParameter("currPage"));   
            StringBuffer sb = new StringBuffer();   //2019-04-24  增加返回尺码组的 编码的编号 x_
           // sb.append(" select Size+'('+convert(varchar,No)+')' Name, SizeID, No SizeCode,Size SizeName from size c  where sizeId in (select sizeId from sizeGroupSize where sizeGroupId = (select GroupId from Goods where goodsId = '" + goodsId + "')) ");
           String groupid=commonDao.getDataForString("Select SizeGroupID From Goods a,GoodsType b Where a.GoodsTypeID=b.GoodsTypeID and GoodsID=?",goodsId);
            sb.append("Select 'x_'+cast(a.No as varchar(10)) x,b.Size+'('+convert(varchar,b.No)+')' Name,b.SizeID, b.No as SizeCode,b.Size SizeName From SizeGroupSize a,Size b Where a.SizeID=b.SizeID and a.SizeGroupID='"+groupid+"' ");
            if (null != param && !param.isEmpty() && !"".equals(param)) {
                sb.append(" and No like '%").append(param).append("%' or size like '%").append(param).append("%' ) ");
            }
            sb.append(" order by a.No asc ");
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
     * 根据货号,颜色,尺码的ID获取货品详细信息
     * 
     * @param req
     * @return
     */
    @RequestMapping(params = "addIteamByGoodsCode")
    @ResponseBody
    public AjaxJson addIteamByGoodsCode(HttpServletRequest req) {
        Client client = ResourceUtil.getClientFromSession(req);
        AjaxJson j = new AjaxJson();
        j.setAttributes(new HashMap<String, Object>());
        try {
            String barCode = oConvertUtils.getString(req.getParameter("Barcode"));
            String goodsId = oConvertUtils.getString(req.getParameter("GoodsID"));
            String colorId = oConvertUtils.getString(req.getParameter("ColorID"));
            String sizeId = oConvertUtils.getString(req.getParameter("SizeID"));
            String customerId = oConvertUtils.getString(req.getParameter("CustomerId"));
            String type = oConvertUtils.getString(req.getParameter("Type"));
            String column = null;
            List addItem = new ArrayList();
            if ("".equals(type) || type.isEmpty()) {
                type = null;
            }
            // 获取列名
            column = getTypeColumn(client.getDeptID(),customerId, type);
            StringBuffer sb = new StringBuffer();
            sb.append(" select ? as BarCode, g.Name GoodsName,c.Color,s.Size,g.Code GoodsCode, c.Color ColorName,c.No ColorCode,sizIndex=(select max(no) as maxsize from SizeGroupSize where sizeGroupId=g.GroupID),ss.SizeGroupID, s.Size SizeName,g.GoodsID,c.ColorID,s.SizeID,  ").append(" s.No SizeCode,ss.No IndexNo,isnull(").append(column)
                    .append(",0) UnitPrice, isnull(g.RetailSales,0) RetailSales from Goods g, ").append(" Color c,size s,GoodsType gt, SizeGroup sg,SizeGroupSize ss where s.SizeID = ?  ")
                    .append(" and c.colorId = ? and g.goodsId = ? and gt.GoodsTypeID = g.GoodsTypeID and sg.SizeGroupID = gt.SizeGroupID ").append(" and ss.SizeGroupID = sg.SizeGroupID and ss.SizeID = ? ");
            addItem = commonDao.findForJdbc(sb.toString(), barCode, sizeId, colorId, goodsId, sizeId);
            if (addItem.size() <= 0) {
                j.setObj(null);
                throw new BusinessException("未找到此条码[" + barCode + "]");
            }
            j.setObj(addItem.get(0));
        } catch (Exception e) {
            j.setSuccess(false);
            j.setMsg(e.getMessage());
            SysLogger.error(e.getMessage(), e);
        }
        return j;
    }

    /**
     * 通过解析条码获取货品详细信息(用于仓位管理中的条码解析)
     * 
     * @param req
     * @return
     */
    @RequestMapping(params = "getGoodsByBarcode")
    @ResponseBody
    public AjaxJson getGoodsByBarcode(HttpServletRequest req) {
        AjaxJson j = new AjaxJson();
        j.setAttributes(new HashMap<String, Object>());
        try {
            String barcode = oConvertUtils.getString(req.getParameter("barcode"));
            StringBuilder sql = new StringBuilder();
            // 判断货品颜色是否定义
            int exit = Integer.parseInt(String.valueOf(commonDao.getData(" select count(1) from goodsColor where goodsId = (select goodsId from BarCode where barcode = ?) ", barcode)));
            sql.append(" select g.GoodsID,g.code GoodsCode,g.name GoodsName,b.ColorID,Color,b.SizeID,Size,isnull(g.SupplierCode,'无厂商货品编码') SupplierCode from barcode b ").append(" left join goods g on b.goodsid = g.goodsid left join color c on c.colorId = b.colorId ");
            if (exit > 0) {// 使用货品颜色
                sql.append(" join goodsColor gc on gc.goodsId = b.goodsId and gc.colorId = b.colorId ");
            }
            sql.append(" left join size s on s.sizeid = b.sizeid where b.barcode = ? ");
            List list = commonDao.findForJdbc(sql.toString(), barcode);
            if (list.size() < 1) {
                List<Map<String, Object>> datas = BarcodeUtil.barcodeToGoods(barcode);
                if (datas != null) {
                    for (int i = 0; i < datas.size(); i++) {
                        Map map = datas.get(i);
                        String goodsCode = String.valueOf(map.get("goodsCode"));
                        String colorNo = String.valueOf(map.get("colorCode"));
                        String sizeNo = String.valueOf(map.get("sizeCode"));
                        // 获取货品,颜色,尺码的ID
                        String goodsId = String.valueOf(commonDao.getData(" select goodsId from goods g where g.code = '" + goodsCode + "' "));
                        String sizeId = String.valueOf(commonDao.getData(" select sizeId from size s where s.no = '" + sizeNo + "' "));
                        String colorId = null;
                        // 判断使用的颜色类型
                        if (LoadUserCount.colorOption == 4) {
                            colorId = String.valueOf(commonDao.getData(" select colorId from brandcolor c where c.no = '" + colorNo + "' and brandId = (select brandId from goods where goodsId = '" + goodsId + "') "));
                        } else {
                            colorId = String.valueOf(commonDao.getData(" select colorId from color c where c.no = '" + colorNo + "' "));
                        }
                        // 判断货品颜色是否定义
                        int exits = Integer.parseInt(String.valueOf(commonDao.getData(" select count(1) from goodsColor where goodsId = ? ", goodsId)));
                        StringBuffer sb = new StringBuffer();
                        sb.append(" select ? as BarCode, g.Name GoodsName,g.Code GoodsCode, g.GoodsID,c.ColorID,Color,s.SizeID,s.Size,isnull(g.SupplierCode,'无厂商货品编码') SupplierCode from Goods g, ").append(" Color c,size s,GoodsType gt,");
                        if (exits > 0) {// 关联货品颜色表
                            sb.append(" goodsColor gc, ");
                        }
                        sb.append("SizeGroup sg,SizeGroupSize ss where s.SizeID = ? and c.colorId = ? and g.goodsId = ? ").append(" and gt.GoodsTypeID = g.GoodsTypeID and sg.SizeGroupID = gt.SizeGroupID ").append(" and ss.SizeGroupID = sg.SizeGroupID and ss.SizeID = ? ");
                        if (exits > 0) {// 关联货品颜色表
                            sb.append(" and g.goodsId = gc.goodsId and c.colorId = gc.colorId ");
                        }
                        list = commonDao.findForJdbc(sb.toString(), barcode, sizeId, colorId, goodsId, sizeId);
                    }
                }
            }
            if (list.size() < 1) {
                throw new BusinessException("未找到此条码[" + barcode + "]");
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
     * 解析条码(通过条码获取货品明细信息)
     * 
     * @param req
     * @return
     */
    @RequestMapping(params = "analyticalBarcode")
    @ResponseBody
    public AjaxJson analyticalBarcode(HttpServletRequest req) {
        Client client = ResourceUtil.getClientFromSession(req);
        AjaxJson j = new AjaxJson();
        j.setAttributes(new HashMap<String, Object>());
        try {
            String barCode = oConvertUtils.getString(req.getParameter("BarCode"));
            String customerId = oConvertUtils.getString(req.getParameter("CustomerId"));
            String type = oConvertUtils.getString(req.getParameter("Type"));
            String column = null;
            List addItem = new ArrayList();
            if ("".equals(type) || type.isEmpty()) {
                type = null;
            }
            // 获取列名
            column = getTypeColumn(client.getDeptID(),customerId, type);
            // 判断条码表是否存在该条码
            int count = Integer.parseInt(String.valueOf(commonDao.getData("select count(1) from BarCode where barcode = ? ", barCode)));
            if (count > 0) {
                // 判断货品颜色是否定义
                int exit = Integer.parseInt(String.valueOf(commonDao.getData(" select count(1) from goodsColor where goodsId = (select goodsId from BarCode where barcode = ?) ", barCode)));
                StringBuffer sb = new StringBuffer();
              /*
                sb.append(" select bc.BarCode,g.Name GoodsName,g.PresentFlag,gt.Code GoodsTypeCode,g.DiscountFlag,c.Color,s.Size,bc.GoodsID,bc.ColorID,bc.SizeID,isnull(g.Discount,10) Discount  ")
                        .append(", g.Code GoodsCode, c.Color ColorName , c.No ColorCode, s.Size SizeName, s.No SizeCode,ss.No IndexNo ").append(",isnull(").append(column).append(",0) UnitPrice, isnull(g.RetailSales,0) RetailSales,ss.SizeGroupID from BarCode bc")
                        .append(" left join Goods g on bc.GoodsID = g.GoodsID ").append(" left join Color c on bc.ColorID = c.ColorID ").append(" left join Size s on bc.SizeID = s.SizeID ").append(" left join GoodsType gt on gt.GoodsTypeID = g.GoodsTypeID ")
                        .append(" left join SizeGroup sg on sg.SizeGroupID = gt.SizeGroupID ").append(" left join SizeGroupSize ss on ss.SizeGroupID = sg.SizeGroupID and ss.SizeID = bc.SizeID ");
               */
                sb.append(" select bc.BarCode,g.Name GoodsName,g.PresentFlag,gt.Code GoodsTypeCode,g.DiscountFlag,c.Color,s.Size,bc.GoodsID,bc.ColorID,bc.SizeID,isnull(g.Discount,10) Discount  ")
                .append(", g.Code GoodsCode, c.Color ColorName , c.No ColorCode, s.Size SizeName, s.No SizeCode,'x_'+cast(ss.No as varchar(10)) x ,sizIndex=(select max(no) as maxsize from SizeGroupSize where sizeGroupId=g.GroupID),ss.No IndexNo  ").append(",isnull(").append(column).append(",0) UnitPrice, isnull(g.RetailSales,0) RetailSales,ss.SizeGroupID from BarCode bc")
                .append(" left join Goods g on bc.GoodsID = g.GoodsID ").append(" left join Color c on bc.ColorID = c.ColorID ").append(" left join Size s on bc.SizeID = s.SizeID ").append(" left join GoodsType gt on gt.GoodsTypeID = g.GoodsTypeID ")
                .append(" left join SizeGroup sg on sg.SizeGroupID = gt.SizeGroupID ").append(" left join SizeGroupSize ss on ss.SizeGroupID = sg.SizeGroupID and ss.SizeID = bc.SizeID ");
                
                if (exit > 0) {// 使用货品颜色
                    sb.append(" join goodsColor gc on gc.goodsId = bc.goodsId and gc.colorId = bc.colorId ");
                }
                sb.append("  where BarCode = '").append(barCode).append("'");
                addItem = commonDao.findForJdbc(sb.toString());
            } else {
                // 使用自定义条码解析规则解析条码
                List<Map<String, Object>> datas = BarcodeUtil.barcodeToGoods(barCode);
                // 使用伏羲标准解析规则解析条码
                // if(datas == null || datas.size() == 0){
                // datas = analyticalStandardBarcode(barCode);
                // }
                if (datas != null && datas.size() > 0) {
                    for (int i = 0; i < datas.size(); i++) {
                        Map map = datas.get(i);
                        String goodsCode = String.valueOf(map.get("goodsCode"));
                        String colorNo = String.valueOf(map.get("colorCode"));
                        String sizeNo = String.valueOf(map.get("sizeCode"));
                        // 获取货品,颜色,尺码的ID
                        String goodsId = String.valueOf(commonDao.getData(" select goodsId from goods g where g.code = '" + goodsCode + "' "));
                        String sizeId = String.valueOf(commonDao.getData(" select sizeId from size s where s.no = '" + sizeNo + "' "));
                        String colorId = null;
                        // 判断使用的颜色类型
                        if (LoadUserCount.colorOption == 4) {
                            colorId = String.valueOf(commonDao.getData(" select colorId from brandcolor c where c.no = '" + colorNo + "' and brandId = (select brandId from goods where goodsId = '" + goodsId + "') "));
                        } else {
                            colorId = String.valueOf(commonDao.getData(" select colorId from color c where c.no = '" + colorNo + "' "));
                        }
                        // 判断货品颜色是否定义
                        int exit = Integer.parseInt(String.valueOf(commonDao.getData(" select count(1) from goodsColor where goodsId = ? ", goodsId)));
                        StringBuffer sb = new StringBuffer();
                        sb.append(" select ? as BarCode, g.Name GoodsName,c.Color,s.Size,g.PresentFlag,g.DiscountFlag,g.Code GoodsCode,gt.Code GoodsTypeCode,isnull(g.Discount,10) Discount, c.Color ColorName,c.No ColorCode,ss.SizeGroupID, s.Size SizeName,g.GoodsID,c.ColorID,s.SizeID,  ")
                                .append(" s.No SizeCode,'x_'+cast(ss.No as varchar(10)) x ,sizIndex=(select max(no) as maxsize from SizeGroupSize where sizeGroupId=g.GroupID),ss.No IndexNo,isnull(").append(column).append(",0) UnitPrice, isnull(g.RetailSales,0) RetailSales from Goods g, ").append(" Color c,size s,GoodsType gt,");
                        if (exit > 0) {// 关联货品颜色表
                            sb.append(" goodsColor gc, ");
                        }
                        sb.append(" SizeGroup sg,SizeGroupSize ss where s.SizeID = ?  ").append(" and c.colorId = ? and g.goodsId = ? and gt.GoodsTypeID = g.GoodsTypeID and sg.SizeGroupID = gt.SizeGroupID ").append(" and ss.SizeGroupID = sg.SizeGroupID and ss.SizeID = ? ");
                        if (exit > 0) {// 关联货品颜色表
                            sb.append(" and g.goodsId = gc.goodsId and c.colorId = gc.colorId ");
                        }
                        addItem = commonDao.findForJdbc(sb.toString(), barCode, sizeId, colorId, goodsId, sizeId);
                        if (addItem.size() > 0) {
                            break;
                        }
                    }
                }
            }
            if (addItem.size() <= 0) {
                // 均色均码识别
                int colorCount = commonDao.getDataToInt(" select count(1) from color ");
                int sizeCount = commonDao.getDataToInt(" select count(1) from size ");
                if (colorCount == 1 && sizeCount == 1) {
                    StringBuffer sb = new StringBuffer();
                    sb.append(" select ? BarCode,g.Name GoodsName,gt.Code GoodsTypeCode,g.PresentFlag,g.DiscountFlag,(select Color from Color) Color,(select Size from Size) Size,isnull(g.Discount,10) Discount,g.GoodsID,(select ColorID from Color) ColorID,(select SizeID from Size) SizeID,")
                            .append(" g.Code GoodsCode,(select Color from Color) ColorName,(select Size from Size) SizeName, (select No from Color) ColorCode, (select No from Size) SizeCode,'x'+Cast((select No from SizeGroup) as varchar(10)) x,sizIndex=(select max(no) as maxsize from SizeGroupSize where sizeGroupId=g.GroupID),(select No from SizeGroup) IndexNo,").append("isnull(").append(column)
                            .append(",0) UnitPrice,isnull(g.RetailSales,0) RetailSales,(select SizeGroupID from SizeGroup) SizeGroupID from goods g ").append(" left join GoodsType gt on gt.GoodsTypeID = g.GoodsTypeID where g.code = ? ");
                    addItem = commonDao.findForJdbc(sb.toString(), barCode, barCode);
                    if (addItem.size() <= 0) {
                        j.setObj(null);
                        throw new BusinessException("未找到此条码[" + barCode + "]");
                    }
                } else {
                    j.setObj(null);
                    throw new BusinessException("未找到此条码[" + barCode + "]");
                }
            }
            j.setObj(addItem.get(0));
        } catch (Exception e) {
            j.setSuccess(false);
            j.setMsg(e.getMessage());
            SysLogger.error(e.getMessage(), e);
        }
        return j;
    }

    /**
     * 解析条码(通过条码获取货品明细信息)  重写，不用原来的 2019-07-18  返回的内容不合适
     *   
     * @param req
     * @return
     */
    @RequestMapping(params = "analyticalBarcodeX")
    @ResponseBody
    public AjaxJson analyticalBarcodeX(HttpServletRequest req) {
        Client client = ResourceUtil.getClientFromSession(req);
        AjaxJson j = new AjaxJson();
        j.setAttributes(new HashMap<String, Object>());
        try {
            String barCode = oConvertUtils.getString(req.getParameter("BarCode"));
            String customerId = oConvertUtils.getString(req.getParameter("CustomerId"));
            String type = oConvertUtils.getString(req.getParameter("Type"));   //价格 与折扣  从前端 固定传 了，不要再判断了  发退货时，这个为  字段名
            String column = null;
        	String TypeStr ="PriceType";  
    		String DiscountRateStr="DiscountRate";
            List addItem = new ArrayList();
            String sql ="";
            Map<String,Object> temp =null;
            
            
            if ("".equals(type) || type.isEmpty()) {
                type = null;
            }else{
            	TypeStr =type;
            }
            
            // 获取列名  货品资料的，根据 单据类别 获取单价取值  字段名
           // column = getTypeColumn(client.getDeptID(),customerId, type);
            Map<String,Object> m1=getTypeColumnNew(client.getDeptID(),customerId, type);
            column =String.valueOf(m1.get("column")); //字段名
            if(!"".equals(customerId) && customerId !=null){
            DiscountRateStr  = String.valueOf(m1.get("DiscountRate"));  //字段名
            }
            // 判断条码表是否存在该条码
            int count = Integer.parseInt(String.valueOf(commonDao.getData("select count(1) from BarCode where barcode = ? ", barCode)));
            if (count > 0) {
                // 判断货品颜色是否定义
                int exit = Integer.parseInt(String.valueOf(commonDao.getData(" select count(1) from goodsColor where goodsId = (select goodsId from BarCode where barcode = ?) ", barCode)));
                StringBuffer sb = new StringBuffer();
                sb.append(" select bc.BarCode,g.Name ,g.PresentFlag,gt.Code GoodsTypeCode,g.DiscountFlag,c.Color,s.Size,bc.GoodsID,bc.ColorID,bc.SizeID,isnull(g.Discount,10) PosDiscount  ")
                        .append(", g.Code,  c.Color ,c.No ColorCode, s.Size SizeName, s.No SizeCode,ss.No IndexNo ").append(",isnull(").append(column).append(",0) UnitPrice, isnull(g.RetailSales,0) RetailSales,ss.SizeGroupID from BarCode bc")
                        .append(" left join Goods g on bc.GoodsID = g.GoodsID ").append(" left join Color c on bc.ColorID = c.ColorID ").append(" left join Size s on bc.SizeID = s.SizeID ").append(" left join GoodsType gt on gt.GoodsTypeID = g.GoodsTypeID ")
                        .append(" left join SizeGroup sg on sg.SizeGroupID = gt.SizeGroupID ").append(" left join SizeGroupSize ss on ss.SizeGroupID = sg.SizeGroupID and ss.SizeID = bc.SizeID ");
                if (exit > 0) {// 使用货品颜色
                    sb.append(" join goodsColor gc on gc.goodsId = bc.goodsId and gc.colorId = bc.colorId ");
                }
                sb.append("  where BarCode = '").append(barCode).append("'");    
                List<Map<String,Object>> ls = commonDao.findForJdbc(sb.toString());    //证明已经存在了
                temp =ls.get(0);
                
                //重新封装  货品颜色 
                if(!"".equals(customerId) && customerId !=null){
               sql="select CustomerID,Customer,"+TypeStr+" PriceType , "+DiscountRateStr+" DiscountRate  from Customer where CustomerID = ? ";
              // List<Map<String,Object>> custPriceType=	commonDao.findForJdbc(sql,customerId);
               List<Map<String,Object>> cust=	commonDao.findForJdbc(sql,customerId);
        	   
        	   System.out.println("价格类型："+String.valueOf(cust.get(0).get("PriceType")));
        	   System.out.println("折扣："+String.valueOf(cust.get(0).get("DiscountRate")));
        	   
        	   String PriceType="零售价";
        	   if(!"".equals(String.valueOf(cust.get(0).get("PriceType"))) && cust.get(0).get("PriceType") !=null && !"null".equals(String.valueOf(cust.get(0).get("PriceType")))){
        		   PriceType =String.valueOf(cust.get(0).get("PriceType"));  //零售价，批发价
        	   }
               //返回真实字段
        	   
        		  String PriceField ="RetailSales";
        		  sql ="select  dbo.GetCustPriceTypeOfFieldName('"+PriceType+"') PriceType";
        		  
        		  List<Map<String,Object>> custPriceType=	commonDao.findForJdbc(sql);
        		  
        		  System.out.println("价格类型字段："+String.valueOf(custPriceType.get(0).get("PriceType")));
        		  
        		  if(custPriceType.get(0).get("PriceType") !=null && !"null".equals(custPriceType.get(0).get("PriceType")) && !"".equals(String.valueOf(custPriceType.get(0).get("PriceType")))){
        			  PriceField =String.valueOf(custPriceType.get(0).get("PriceType"));
        		  }
        			String DiscountRate="0";
        			
        			if(cust.get(0).get("DiscountRate") !=null && !"".equals(String.valueOf(cust.get(0).get("DiscountRate"))) && !"null".equalsIgnoreCase(String.valueOf(cust.get(0).get("DiscountRate")))){
        				DiscountRate =String.valueOf(cust.get(0).get("DiscountRate"));
        			}
        			
        			sb =new StringBuffer();
        			sb.append("select GoodsID,Code,Name,GroupID,RetailSales,"+PriceField+" UnitPrice, Discount=0.0,DiscountRate="+DiscountRate+", Quantity=0,Amount=0  from Goods where GoodsID = '"+String.valueOf(temp.get("GoodsID"))+"'");
        			List<Map<String, Object>> lg =commonDao.findForJdbc(sb.toString());
        			for(int i=0;i< lg.size(); i++){
        				Map<String,Object> map= lg.get(i);
        				
        				sql="select a.GoodsID,a.ColorID,c.Color from GoodsColor a join Color c on a.ColorID=c.ColorID where a.GoodsID =? ";
        				List<Map<String, Object>> color=commonDao.findForJdbc(sql,String.valueOf(map.get("GoodsID")));
        				for(int j2 =0;j2<color.size();j2++){ //第个颜色包三个属性 一个颜色 一个map
        					
        					Map<String,Object> datamap=new LinkedHashMap<>();
        					
        					Map<String,Object> m2=color.get(j2);
        					datamap.put("GoodsID", String.valueOf(map.get("GoodsID")));
        					datamap.put("Code", String.valueOf(map.get("Code")));
        					datamap.put("Name", String.valueOf(map.get("Name")));
        					datamap.put("GroupID", String.valueOf(map.get("GroupID")));
        					datamap.put("RetailSales", new BigDecimal(String.valueOf(map.get("RetailSales"))).setScale(2,BigDecimal.ROUND_DOWN));
        					datamap.put("Discount", new BigDecimal(String.valueOf(map.get("Discount"))).setScale(2,BigDecimal.ROUND_DOWN));
        					if(!"".equals(String.valueOf(map.get("UnitPrice"))) && map.get("UnitPrice") !=null && !"null".equals(String.valueOf(map.get("UnitPrice"))))
        					{	
        					datamap.put("UnitPrice", new BigDecimal(String.valueOf(map.get("UnitPrice"))).setScale(2,BigDecimal.ROUND_DOWN));
        					}else{
        						datamap.put("UnitPrice","");
        					}
        					if(!"".equals(String.valueOf(map.get("DiscountRate"))) && map.get("DiscountRate") !=null && !"null".equals(String.valueOf(map.get("DiscountRate"))))
        					{
        					datamap.put("DiscountRate", new BigDecimal(String.valueOf(map.get("DiscountRate"))).setScale(2,BigDecimal.ROUND_DOWN));
        					if(!"".equals(String.valueOf(map.get("UnitPrice"))) && map.get("UnitPrice") !=null && !"null".equals(String.valueOf(map.get("UnitPrice")))){
        						if(new BigDecimal(String.valueOf(map.get("DiscountRate"))).compareTo(BigDecimal.ZERO) !=0 ){
        							System.out.println("进入此方法了a");
        							datamap.put("Amount",new BigDecimal(String.valueOf(map.get("UnitPrice"))).multiply(new BigDecimal(String.valueOf(map.get("DiscountRate")))).divide(new BigDecimal(10.0)).setScale(2,BigDecimal.ROUND_DOWN));	
        								
        						}else{
        							 System.out.println("进入此方法了b");
        							datamap.put("Amount",new BigDecimal(String.valueOf(map.get("UnitPrice"))).setScale(2,BigDecimal.ROUND_DOWN));
        						}
        						//数量默认为1 ，就单价 * 折扣，就可以了，不为1  就不一样
        					}
        					
        					
        					}else{
        						datamap.put("DiscountRate","");	
        					}
        					
        					 System.out.println("货号："+String.valueOf(datamap.get("Code"))+"\t"+"单价："
        					 +String.valueOf(datamap.get("UnitPrice"))+"\t"+"折扣："
        					 + String.valueOf(datamap.get("DiscountRate"))	 
        					 );
        					
        					if(String.valueOf(map.get("Quantity")).equals("0")){
        						datamap.put("Quantity", "");
        					}else{
        						datamap.put("Quantity", String.valueOf(map.get("Quantity")));
        					}
        					
        					
        					
        					if("".equals(String.valueOf(datamap.get("Amount"))) ||datamap.get("Amount")==null ){
        						datamap.put("Amount", "");
        					}
        					//else{
        					//	datamap.put("Amount",  new BigDecimal(String.valueOf(datamap.get("Amount"))).setScale(2,BigDecimal.ROUND_DOWN));
        					//}
        				
        					datamap.put("Quantity", 1);  //单据界面 扫，默认为1吧
        					
        					
        					
        					
        					datamap.put("ColorID", String.valueOf(m2.get("ColorID")));
        					datamap.put("Color", String.valueOf(m2.get("Color")));
        					datamap.put("img", "");
        					
        					List<Map<String, Object>> sizetitle =new ArrayList<>();
        					List<Map<String, Object>> sizeData =new ArrayList<>();
        					List<Map<String, Object>> right =new ArrayList<>();
        					//--------------颜色----------------
        					
        					sql="select * from SizeGroupSize where SizeGroupID = ?";
        					List<Map<String, Object>> sizels=commonDao.findForJdbc(sql,String.valueOf(map.get("GroupID")));
        					for(int k=0;k< sizels.size();k++){ 
        					
        						Map<String,Object> m3=sizels.get(k);
        					
        						Map<String,Object> m4=new LinkedHashMap<String,Object>();//sizeData 的map
        						Map<String,Object> st=new LinkedHashMap<String,Object>();
        						st.put("field", "x_"+String.valueOf(m3.get("No")));
        						st.put("title", String.valueOf(m3.get("Size")));
        						sizetitle.add(st);
        						//----------显示尺码列--------------
        						//--------数据---------
        						m4.put("GoodsID", String.valueOf(map.get("GoodsID")));
        						m4.put("ColorID", String.valueOf(datamap.get("ColorID")));
        						m4.put("Color", String.valueOf(datamap.get("Color")));
        						m4.put("x", "x_"+String.valueOf(m3.get("No")));
        						if(String.valueOf(temp.get("IndexNo")).equals(String.valueOf(m3.get("No"))) ) //尺码编号    因为在在单据界面 加，就默认数量为1 吧，后面加也再重新加数量修改
        					    {
        							m4.put("Quantity", 1);
        							 if(!"".equals(String.valueOf(datamap.get("Amount")))){
        							 m4.put("Amount", new BigDecimal(String.valueOf(datamap.get("Amount"))).setScale(2,BigDecimal.ROUND_DOWN));
        							 }else{
        								 m4.put("Amount", ""); 
        							 }
        							 }else{
        					    	m4.put("Quantity", "");
        					    	m4.put("Amount", "");
        					    }
        					
        						
        						if(!"".equals(String.valueOf(datamap.get("UnitPrice"))) && datamap.get("UnitPrice") !=null && !"null".equals(String.valueOf(datamap.get("UnitPrice"))))
        						{	
        						m4.put("UnitPrice", new BigDecimal(String.valueOf(datamap.get("UnitPrice"))).setScale(2,BigDecimal.ROUND_DOWN));
        						
        						}else{
        							m4.put("UnitPrice","");
        						}
        						if(!"".equals(String.valueOf(datamap.get("DiscountRate"))) && datamap.get("DiscountRate") !=null && !"null".equals(String.valueOf(datamap.get("DiscountRate"))))
        						{
        							m4.put("DiscountRate", new BigDecimal(String.valueOf(datamap.get("DiscountRate"))).setScale(2,BigDecimal.ROUND_DOWN));
        							
        						}else{
        							m4.put("DiscountRate","");	
        						}
        						
        						
        						
        						m4.put("SizeID", String.valueOf(m3.get("SizeID")));
        						m4.put("Size", String.valueOf(m3.get("Size")));
        						
        						sizeData.add(m4);
        					}
        					
        				//	for(int n=0;n<2;n++){
        						Map<String,Object> m5=new LinkedHashMap<String,Object>();
        						m5.put("text", "删除");
        					/*	m5.put("onPress", "() => {"+
        		                                  "  modal.toast({ "+
        		                                  "      message: '删除',"+
        		                                  "      duration: 0.3 "+
        		                                  "  });"+
        		                                "}"); */
        						
        						Map<String,Object> styleMap=new LinkedHashMap<String,Object>();
        						styleMap.put("backgroundColor", "#F4333C");
        						styleMap.put("color", "white");
        						m5.put("style", styleMap);
        						
        						
        						right.add(m5);
        				//	}
        					
        					datamap.put("sizetitle", sizetitle);
        					datamap.put("sizeData", sizeData);
        					datamap.put("right", right);
        					
        					addItem.add(datamap);
        				}
        			
        				
        			
        				
        			
        				
        				
        			}
                
                }
                
                // addItem
            } else {
                // 使用自定义条码解析规则解析条码
                List<Map<String, Object>> datas = BarcodeUtil.barcodeToGoods(barCode);
                // 使用伏羲标准解析规则解析条码
                // if(datas == null || datas.size() == 0){
                // datas = analyticalStandardBarcode(barCode);
                // }
                if (datas != null && datas.size() > 0) {
                    for (int i = 0; i < datas.size(); i++) {
                        Map map = datas.get(i);
                        String goodsCode = String.valueOf(map.get("goodsCode"));
                        String colorNo = String.valueOf(map.get("colorCode"));
                        String sizeNo = String.valueOf(map.get("sizeCode"));
                        // 获取货品,颜色,尺码的ID
                        String goodsId = String.valueOf(commonDao.getData(" select goodsId from goods g where g.code = '" + goodsCode + "' "));
                        String sizeId = String.valueOf(commonDao.getData(" select sizeId from size s where s.no = '" + sizeNo + "' "));
                        String colorId = null;
                        // 判断使用的颜色类型
                        if (LoadUserCount.colorOption == 4) {
                            colorId = String.valueOf(commonDao.getData(" select colorId from brandcolor c where c.no = '" + colorNo + "' and brandId = (select brandId from goods where goodsId = '" + goodsId + "') "));
                        } else {
                            colorId = String.valueOf(commonDao.getData(" select colorId from color c where c.no = '" + colorNo + "' "));
                        }
                        // 判断货品颜色是否定义
                        int exit = Integer.parseInt(String.valueOf(commonDao.getData(" select count(1) from goodsColor where goodsId = ? ", goodsId)));
                        StringBuffer sb = new StringBuffer();
                        sb.append(" select ? as BarCode, g.Name GoodsName,c.Color,s.Size,g.PresentFlag,g.DiscountFlag,g.Code GoodsCode,gt.Code GoodsTypeCode,isnull(g.Discount,10) Discount, c.Color ColorName,c.No ColorCode,ss.SizeGroupID, s.Size SizeName,g.GoodsID,c.ColorID,s.SizeID,  ")
                                .append(" s.No SizeCode,ss.No IndexNo,isnull(").append(column).append(",0) UnitPrice, isnull(g.RetailSales,0) RetailSales from Goods g, ").append(" Color c,size s,GoodsType gt,");
                        if (exit > 0) {// 关联货品颜色表
                            sb.append(" goodsColor gc, ");
                        }
                        sb.append(" SizeGroup sg,SizeGroupSize ss where s.SizeID = ?  ").append(" and c.colorId = ? and g.goodsId = ? and gt.GoodsTypeID = g.GoodsTypeID and sg.SizeGroupID = gt.SizeGroupID ").append(" and ss.SizeGroupID = sg.SizeGroupID and ss.SizeID = ? ");
                        if (exit > 0) {// 关联货品颜色表
                            sb.append(" and g.goodsId = gc.goodsId and c.colorId = gc.colorId ");
                        }
                      //  addItem = commonDao.findForJdbc(sb.toString(), barCode, sizeId, colorId, goodsId, sizeId);
                        
                        List<Map<String,Object>> ls =commonDao.findForJdbc(sb.toString(), barCode, sizeId, colorId, goodsId, sizeId);    //证明已经存在了
                        temp =ls.get(0);
                        
                        //重新封装  货品颜色 
                        if(!"".equals(customerId) && customerId !=null){
                       sql="select CustomerID,Customer,"+TypeStr+" PriceType , "+DiscountRateStr+" DiscountRate  from Customer where CustomerID = ? ";
                      // List<Map<String,Object>> custPriceType=	commonDao.findForJdbc(sql,customerId);
                       List<Map<String,Object>> cust=	commonDao.findForJdbc(sql,customerId);
                	   
                	   System.out.println("价格类型："+String.valueOf(cust.get(0).get("PriceType")));
                	   System.out.println("折扣："+String.valueOf(cust.get(0).get("DiscountRate")));
                	   
                	   String PriceType="零售价";
                	   if(!"".equals(String.valueOf(cust.get(0).get("PriceType"))) && cust.get(0).get("PriceType") !=null && !"null".equals(String.valueOf(cust.get(0).get("PriceType")))){
                		   PriceType =String.valueOf(cust.get(0).get("PriceType"));  //零售价，批发价
                	   }
                       //返回真实字段
                	   
                		  String PriceField ="RetailSales";
                		  sql ="select  dbo.GetCustPriceTypeOfFieldName('"+PriceType+"') PriceType";
                		  
                		  List<Map<String,Object>> custPriceType=	commonDao.findForJdbc(sql);
                		  
                		  System.out.println("价格类型字段："+String.valueOf(custPriceType.get(0).get("PriceType")));
                		  
                		  if(custPriceType.get(0).get("PriceType") !=null && !"null".equals(custPriceType.get(0).get("PriceType")) && !"".equals(String.valueOf(custPriceType.get(0).get("PriceType")))){
                			  PriceField =String.valueOf(custPriceType.get(0).get("PriceType"));
                		  }
                			String DiscountRate="0";
                			
                			if(cust.get(0).get("DiscountRate") !=null && !"".equals(String.valueOf(cust.get(0).get("DiscountRate"))) && !"null".equalsIgnoreCase(String.valueOf(cust.get(0).get("DiscountRate")))){
                				DiscountRate =String.valueOf(cust.get(0).get("DiscountRate"));
                			}
                			
                			sb =new StringBuffer();
                			sb.append("select GoodsID,Code,Name,GroupID,RetailSales,"+PriceField+" UnitPrice, Discount=0.0,DiscountRate="+DiscountRate+", Quantity=0,Amount=0  from Goods where GoodsID = '"+String.valueOf(temp.get("GoodsID"))+"'");
                			List<Map<String, Object>> lg =commonDao.findForJdbc(sb.toString());
                			for(int i2=0;i2< lg.size(); i2++){
                				Map<String,Object> map2= lg.get(i2);
                				
                				sql="select a.GoodsID,a.ColorID,c.Color from GoodsColor a join Color c on a.ColorID=c.ColorID where a.GoodsID =? ";
                				List<Map<String, Object>> color=commonDao.findForJdbc(sql,String.valueOf(map2.get("GoodsID")));
                				for(int j2 =0;j2<color.size();j2++){ //第个颜色包三个属性 一个颜色 一个map
                					
                					Map<String,Object> datamap=new LinkedHashMap<>();
                					
                					Map<String,Object> m2=color.get(j2);
                					datamap.put("GoodsID", String.valueOf(map2.get("GoodsID")));
                					datamap.put("Code", String.valueOf(map2.get("Code")));
                					datamap.put("Name", String.valueOf(map2.get("Name")));
                					datamap.put("GroupID", String.valueOf(map2.get("GroupID")));
                					datamap.put("RetailSales", new BigDecimal(String.valueOf(map2.get("RetailSales"))).setScale(2,BigDecimal.ROUND_DOWN));
                					datamap.put("Discount", new BigDecimal(String.valueOf(map2.get("Discount"))).setScale(2,BigDecimal.ROUND_DOWN));
                					if(!"".equals(String.valueOf(map2.get("UnitPrice"))) && map2.get("UnitPrice") !=null && !"null".equals(String.valueOf(map2.get("UnitPrice"))))
                					{	
                					datamap.put("UnitPrice", new BigDecimal(String.valueOf(map2.get("UnitPrice"))).setScale(2,BigDecimal.ROUND_DOWN));
                					}else{
                						datamap.put("UnitPrice","");
                					}
                					if(!"".equals(String.valueOf(map2.get("DiscountRate"))) && map2.get("DiscountRate") !=null && !"null".equals(String.valueOf(map2.get("DiscountRate"))))
                					{
                					datamap.put("DiscountRate", new BigDecimal(String.valueOf(map2.get("DiscountRate"))).setScale(2,BigDecimal.ROUND_DOWN));
                					if(!"".equals(String.valueOf(map2.get("UnitPrice"))) && map2.get("UnitPrice") !=null && !"null".equals(String.valueOf(map2.get("UnitPrice")))){
                						if(new BigDecimal(String.valueOf(map2.get("DiscountRate"))).compareTo(BigDecimal.ZERO) !=0 ){
                							System.out.println("进入此方法了a");
                							datamap.put("Amount",new BigDecimal(String.valueOf(map2.get("UnitPrice"))).multiply(new BigDecimal(String.valueOf(map2.get("DiscountRate")))).divide(new BigDecimal(10.0)).setScale(2,BigDecimal.ROUND_DOWN));	
                								
                						}else{
                							 System.out.println("进入此方法了b");
                							datamap.put("Amount",new BigDecimal(String.valueOf(map2.get("UnitPrice"))).setScale(2,BigDecimal.ROUND_DOWN));
                						}
                						//数量默认为1 ，就单价 * 折扣，就可以了，不为1  就不一样
                					}
                					
                					
                					}else{
                						datamap.put("DiscountRate","");	
                					}
                					
                					 System.out.println("货号："+String.valueOf(datamap.get("Code"))+"\t"+"单价："
                					 +String.valueOf(datamap.get("UnitPrice"))+"\t"+"折扣："
                					 + String.valueOf(datamap.get("DiscountRate"))	 
                					 );
                					
                					if(String.valueOf(map.get("Quantity")).equals("0")){
                						datamap.put("Quantity", "");
                					}else{
                						datamap.put("Quantity", String.valueOf(map.get("Quantity")));
                					}
                					
                					
                					
                					if(new BigDecimal(String.valueOf(datamap.get("Amount"))).compareTo(BigDecimal.ZERO) ==0){
                						datamap.put("Amount", "");
                					}
                					//else{
                					//	datamap.put("Amount",  new BigDecimal(String.valueOf(datamap.get("Amount"))).setScale(2,BigDecimal.ROUND_DOWN));
                					//}
                				
                					datamap.put("Quantity", 1);  //单据界面 扫，默认为1吧
                					
                					
                					
                					
                					datamap.put("ColorID", String.valueOf(m2.get("ColorID")));
                					datamap.put("Color", String.valueOf(m2.get("Color")));
                					datamap.put("img", "");
                					
                					List<Map<String, Object>> sizetitle =new ArrayList<>();
                					List<Map<String, Object>> sizeData =new ArrayList<>();
                					List<Map<String, Object>> right =new ArrayList<>();
                					//--------------颜色----------------
                					
                					sql="select * from SizeGroupSize where SizeGroupID = ?";
                					List<Map<String, Object>> sizels=commonDao.findForJdbc(sql,String.valueOf(map.get("GroupID")));
                					for(int k=0;k< sizels.size();k++){ 
                					
                						Map<String,Object> m3=sizels.get(k);
                					
                						Map<String,Object> m4=new LinkedHashMap<String,Object>();//sizeData 的map
                						Map<String,Object> st=new LinkedHashMap<String,Object>();
                						st.put("field", "x_"+String.valueOf(m3.get("No")));
                						st.put("title", String.valueOf(m3.get("Size")));
                						sizetitle.add(st);
                						//----------显示尺码列--------------
                						//--------数据---------
                						m4.put("GoodsID", String.valueOf(map.get("GoodsID")));
                						m4.put("ColorID", String.valueOf(datamap.get("ColorID")));
                						m4.put("Color", String.valueOf(datamap.get("Color")));
                						m4.put("x", "x_"+String.valueOf(m3.get("No")));
                						if(String.valueOf(temp.get("IndexNo")).equals(String.valueOf(m3.get("No")))) //尺码编号    因为在在单据界面 加，就默认数量为1 吧，后面加也再重新加数量修改
                					    {
                							m4.put("Quantity", 1);
                							m4.put("Amount", new BigDecimal(String.valueOf(datamap.get("Amount"))).setScale(2,BigDecimal.ROUND_DOWN));
                					    }else{
                					    	m4.put("Quantity", "");
                					    	m4.put("Amount", "");
                					    }
                					
                						
                						if(!"".equals(String.valueOf(datamap.get("UnitPrice"))) && datamap.get("UnitPrice") !=null && !"null".equals(String.valueOf(datamap.get("UnitPrice"))))
                						{	
                						m4.put("UnitPrice", new BigDecimal(String.valueOf(datamap.get("UnitPrice"))).setScale(2,BigDecimal.ROUND_DOWN));
                						
                						}else{
                							m4.put("UnitPrice","");
                						}
                						if(!"".equals(String.valueOf(datamap.get("DiscountRate"))) && datamap.get("DiscountRate") !=null && !"null".equals(String.valueOf(datamap.get("DiscountRate"))))
                						{
                							m4.put("DiscountRate", new BigDecimal(String.valueOf(datamap.get("DiscountRate"))).setScale(2,BigDecimal.ROUND_DOWN));
                							
                						}else{
                							m4.put("DiscountRate","");	
                						}
                						
                						
                						
                						m4.put("SizeID", String.valueOf(m3.get("SizeID")));
                						m4.put("Size", String.valueOf(m3.get("Size")));
                						
                						sizeData.add(m4);
                					}
                					
                					//for(int n=0;n<2;n++){
                						Map<String,Object> m5=new LinkedHashMap<String,Object>();
                						m5.put("text", "删除");
                						
                						
                    					/*	m5.put("onPress", "() => {"+
                    		                                  "  modal.toast({ "+
                    		                                  "      message: '删除',"+
                    		                                  "      duration: 0.3 "+
                    		                                  "  });"+
                    		                                "}"); */
                    						
                    						Map<String,Object> styleMap=new LinkedHashMap<String,Object>();
                    						styleMap.put("backgroundColor", "#F4333C");
                    						styleMap.put("color", "white");
                    						m5.put("style", styleMap);
                						
                						
                						right.add(m5);
                					//}
                					
                					datamap.put("sizetitle", sizetitle);
                					datamap.put("sizeData", sizeData);
                					datamap.put("right", right);
                					
                					addItem.add(datamap);
                				}
                			
                				
                			
                				
                			
                				
                				
                			}
                        
                        }
                        
                          
                        
                        if (addItem.size() > 0) {
                            break;
                        }
                    }
                }
            }
            if (addItem.size() <= 0) {
                // 均色均码识别
                int colorCount = commonDao.getDataToInt(" select count(1) from color ");
                int sizeCount = commonDao.getDataToInt(" select count(1) from size ");
                if (colorCount == 1 && sizeCount == 1) {
                    StringBuffer sb = new StringBuffer();
                    sb.append(" select ? BarCode,g.Name GoodsName,gt.Code GoodsTypeCode,g.PresentFlag,g.DiscountFlag,(select Color from Color) Color,(select Size from Size) Size,isnull(g.Discount,10) Discount,g.GoodsID,(select ColorID from Color) ColorID,(select SizeID from Size) SizeID,")
                            .append(" g.Code GoodsCode,(select Color from Color) ColorName,(select Size from Size) SizeName, (select No from Color) ColorCode, (select No from Size) SizeCode,(select No from SizeGroup) IndexNo,").append("isnull(").append(column)
                            .append(",0) UnitPrice,isnull(g.RetailSales,0) RetailSales,(select SizeGroupID from SizeGroup) SizeGroupID from goods g ").append(" left join GoodsType gt on gt.GoodsTypeID = g.GoodsTypeID where g.code = ? ");
                  //  addItem = commonDao.findForJdbc(sb.toString(), barCode, barCode);
                    
                    List<Map<String,Object>> ls = commonDao.findForJdbc(sb.toString(), barCode, barCode);    //证明已经存在了
                    temp =ls.get(0);
                    
                    //重新封装  货品颜色 
                    if(!"".equals(customerId) && customerId !=null){
                   sql="select CustomerID,Customer,"+TypeStr+" PriceType , "+DiscountRateStr+" DiscountRate  from Customer where CustomerID = ? ";
                  // List<Map<String,Object>> custPriceType=	commonDao.findForJdbc(sql,customerId);
                   List<Map<String,Object>> cust=	commonDao.findForJdbc(sql,customerId);
            	   
            	   System.out.println("价格类型："+String.valueOf(cust.get(0).get("PriceType")));
            	   System.out.println("折扣："+String.valueOf(cust.get(0).get("DiscountRate")));
            	   
            	   String PriceType="零售价";
            	   if(!"".equals(String.valueOf(cust.get(0).get("PriceType"))) && cust.get(0).get("PriceType") !=null && !"null".equals(String.valueOf(cust.get(0).get("PriceType")))){
            		   PriceType =String.valueOf(cust.get(0).get("PriceType"));  //零售价，批发价
            	   }
                   //返回真实字段
            	   
            		  String PriceField ="RetailSales";
            		  sql ="select  dbo.GetCustPriceTypeOfFieldName('"+PriceType+"') PriceType";
            		  
            		  List<Map<String,Object>> custPriceType=	commonDao.findForJdbc(sql);
            		  
            		  System.out.println("价格类型字段："+String.valueOf(custPriceType.get(0).get("PriceType")));
            		  
            		  if(custPriceType.get(0).get("PriceType") !=null && !"null".equals(custPriceType.get(0).get("PriceType")) && !"".equals(String.valueOf(custPriceType.get(0).get("PriceType")))){
            			  PriceField =String.valueOf(custPriceType.get(0).get("PriceType"));
            		  }
            			String DiscountRate="0";
            			
            			if(cust.get(0).get("DiscountRate") !=null && !"".equals(String.valueOf(cust.get(0).get("DiscountRate"))) && !"null".equalsIgnoreCase(String.valueOf(cust.get(0).get("DiscountRate")))){
            				DiscountRate =String.valueOf(cust.get(0).get("DiscountRate"));
            			}
            			
            			sb =new StringBuffer();
            			sb.append("select GoodsID,Code,Name,GroupID,RetailSales,"+PriceField+" UnitPrice, Discount=0.0,DiscountRate="+DiscountRate+", Quantity=0,Amount=0  from Goods where GoodsID = '"+String.valueOf(temp.get("GoodsID"))+"'");
            			List<Map<String, Object>> lg =commonDao.findForJdbc(sb.toString());
            			for(int i=0;i< lg.size(); i++){
            				Map<String,Object> map= lg.get(i);
            				
            				sql="select a.GoodsID,a.ColorID,c.Color from GoodsColor a join Color c on a.ColorID=c.ColorID where a.GoodsID =? ";
            				List<Map<String, Object>> color=commonDao.findForJdbc(sql,String.valueOf(map.get("GoodsID")));
            				for(int j2 =0;j2<color.size();j2++){ //第个颜色包三个属性 一个颜色 一个map
            					
            					Map<String,Object> datamap=new LinkedHashMap<>();
            					
            					Map<String,Object> m2=color.get(j2);
            					datamap.put("GoodsID", String.valueOf(map.get("GoodsID")));
            					datamap.put("Code", String.valueOf(map.get("Code")));
            					datamap.put("Name", String.valueOf(map.get("Name")));
            					datamap.put("GroupID", String.valueOf(map.get("GroupID")));
            					datamap.put("RetailSales", new BigDecimal(String.valueOf(map.get("RetailSales"))).setScale(2,BigDecimal.ROUND_DOWN));
            					datamap.put("Discount", new BigDecimal(String.valueOf(map.get("Discount"))).setScale(2,BigDecimal.ROUND_DOWN));
            					if(!"".equals(String.valueOf(map.get("UnitPrice"))) && map.get("UnitPrice") !=null && !"null".equals(String.valueOf(map.get("UnitPrice"))))
            					{	
            					datamap.put("UnitPrice", new BigDecimal(String.valueOf(map.get("UnitPrice"))).setScale(2,BigDecimal.ROUND_DOWN));
            					}else{
            						datamap.put("UnitPrice","");
            					}
            					if(!"".equals(String.valueOf(map.get("DiscountRate"))) && map.get("DiscountRate") !=null && !"null".equals(String.valueOf(map.get("DiscountRate"))))
            					{
            					datamap.put("DiscountRate", new BigDecimal(String.valueOf(map.get("DiscountRate"))).setScale(2,BigDecimal.ROUND_DOWN));
            					if(!"".equals(String.valueOf(map.get("UnitPrice"))) && map.get("UnitPrice") !=null && !"null".equals(String.valueOf(map.get("UnitPrice")))){
            						if(new BigDecimal(String.valueOf(map.get("DiscountRate"))).compareTo(BigDecimal.ZERO) !=0 ){
            							System.out.println("进入此方法了a");
            							datamap.put("Amount",new BigDecimal(String.valueOf(map.get("UnitPrice"))).multiply(new BigDecimal(String.valueOf(map.get("DiscountRate")))).divide(new BigDecimal(10.0)).setScale(2,BigDecimal.ROUND_DOWN));	
            								
            						}else{
            							 System.out.println("进入此方法了b");
            							datamap.put("Amount",new BigDecimal(String.valueOf(map.get("UnitPrice"))).setScale(2,BigDecimal.ROUND_DOWN));
            						}
            						//数量默认为1 ，就单价 * 折扣，就可以了，不为1  就不一样
            					}
            					
            					
            					}else{
            						datamap.put("DiscountRate","");	
            					}
            					
            					 System.out.println("货号："+String.valueOf(datamap.get("Code"))+"\t"+"单价："
            					 +String.valueOf(datamap.get("UnitPrice"))+"\t"+"折扣："
            					 + String.valueOf(datamap.get("DiscountRate"))	 
            					 );
            					
            					if(String.valueOf(map.get("Quantity")).equals("0")){
            						datamap.put("Quantity", "");
            					}else{
            						datamap.put("Quantity", String.valueOf(map.get("Quantity")));
            					}
            					
            					
            					
            					if(new BigDecimal(String.valueOf(datamap.get("Amount"))).compareTo(BigDecimal.ZERO) ==0){
            						datamap.put("Amount", "");
            					}
            					//else{
            					//	datamap.put("Amount",  new BigDecimal(String.valueOf(datamap.get("Amount"))).setScale(2,BigDecimal.ROUND_DOWN));
            					//}
            				
            					datamap.put("Quantity", 1);  //单据界面 扫，默认为1吧
            					
            					
            					
            					
            					datamap.put("ColorID", String.valueOf(m2.get("ColorID")));
            					datamap.put("Color", String.valueOf(m2.get("Color")));
            					datamap.put("img", "");
            					
            					List<Map<String, Object>> sizetitle =new ArrayList<>();
            					List<Map<String, Object>> sizeData =new ArrayList<>();
            					List<Map<String, Object>> right =new ArrayList<>();
            					//--------------颜色----------------
            					
            					sql="select * from SizeGroupSize where SizeGroupID = ?";
            					List<Map<String, Object>> sizels=commonDao.findForJdbc(sql,String.valueOf(map.get("GroupID")));
            					for(int k=0;k< sizels.size();k++){ 
            					
            						Map<String,Object> m3=sizels.get(k);
            					
            						Map<String,Object> m4=new LinkedHashMap<String,Object>();//sizeData 的map
            						Map<String,Object> st=new LinkedHashMap<String,Object>();
            						st.put("field", "x_"+String.valueOf(m3.get("No")));
            						st.put("title", String.valueOf(m3.get("Size")));
            						sizetitle.add(st);
            						//----------显示尺码列--------------
            						//--------数据---------
            						m4.put("GoodsID", String.valueOf(map.get("GoodsID")));
            						m4.put("ColorID", String.valueOf(datamap.get("ColorID")));
            						m4.put("Color", String.valueOf(datamap.get("Color")));
            						m4.put("x", "x_"+String.valueOf(m3.get("No")));
            						if(String.valueOf(temp.get("IndexNo")).equals(String.valueOf(m3.get("No")))) //尺码编号    因为在在单据界面 加，就默认数量为1 吧，后面加也再重新加数量修改
            					    {
            							m4.put("Quantity", 1);
            							m4.put("Amount", new BigDecimal(String.valueOf(datamap.get("Amount"))).setScale(2,BigDecimal.ROUND_DOWN));
            					    }else{
            					    	m4.put("Quantity", "");
            					    	m4.put("Amount", "");
            					    }
            					
            						
            						if(!"".equals(String.valueOf(datamap.get("UnitPrice"))) && datamap.get("UnitPrice") !=null && !"null".equals(String.valueOf(datamap.get("UnitPrice"))))
            						{	
            						m4.put("UnitPrice", new BigDecimal(String.valueOf(datamap.get("UnitPrice"))).setScale(2,BigDecimal.ROUND_DOWN));
            						
            						}else{
            							m4.put("UnitPrice","");
            						}
            						if(!"".equals(String.valueOf(datamap.get("DiscountRate"))) && datamap.get("DiscountRate") !=null && !"null".equals(String.valueOf(datamap.get("DiscountRate"))))
            						{
            							m4.put("DiscountRate", new BigDecimal(String.valueOf(datamap.get("DiscountRate"))).setScale(2,BigDecimal.ROUND_DOWN));
            							
            						}else{
            							m4.put("DiscountRate","");	
            						}
            						
            						
            						
            						m4.put("SizeID", String.valueOf(m3.get("SizeID")));
            						m4.put("Size", String.valueOf(m3.get("Size")));
            						
            						sizeData.add(m4);
            					}
            					
            				//	for(int n=0;n<2;n++){
            						Map<String,Object> m5=new LinkedHashMap<String,Object>();
            						m5.put("text", "删除");
            					
            						/*	m5.put("onPress", "() => {"+
	                                  "  modal.toast({ "+
	                                  "      message: '删除',"+
	                                  "      duration: 0.3 "+
	                                  "  });"+
	                                "}"); */
					
				     	  Map<String,Object> styleMap=new LinkedHashMap<String,Object>();
					     styleMap.put("backgroundColor", "#F4333C");
					     styleMap.put("color", "white");
					      m5.put("style", styleMap);
            						
            						right.add(m5);
            				//	}
            					
            					datamap.put("sizetitle", sizetitle);
            					datamap.put("sizeData", sizeData);
            					datamap.put("right", right);
            					
            					addItem.add(datamap);
            				}
            			
            				
            			
            				
            			
            				
            				
            			}
                    
                    }
                    
                    
                    
                    
                    
                    
                    
                    if (addItem.size() <= 0) {
                        j.setObj(null);
                        throw new BusinessException("未找到此条码[" + barCode + "]");
                    }
                } else {
                    j.setObj(null);
                    throw new BusinessException("未找到此条码[" + barCode + "]");
                }
            }
            j.setObj(addItem.get(0));
        } catch (Exception e) {
            j.setSuccess(false);
            j.setMsg(e.getMessage());
            SysLogger.error(e.getMessage(), e);
        }
        return j;
    }
    
    
    /**
     * 解析箱条码(通过箱条码获取货品明细信息)
     * 
     * @param req
     * @return
     */
    @RequestMapping(params = "getBoxCode")
    @ResponseBody
    public AjaxJson getBoxCode(HttpServletRequest req) {
        AjaxJson j = new AjaxJson();
        Client client = ResourceUtil.getClientFromSession(req);
        j.setAttributes(new HashMap<String, Object>());
        try {
            String GoodsBoxBarcode = oConvertUtils.getString(req.getParameter("BarCode"));
            String customerId = oConvertUtils.getString(req.getParameter("CustomerId"));
            String type = oConvertUtils.getString(req.getParameter("Type"));
            StringBuffer sb = new StringBuffer();
            // 获取列名
            String column = null;
            if ("".equals(type) || type.isEmpty()) {
                type = null;
            }
            // 获取列名
            column = getTypeColumn(client.getDeptID(),customerId, type);
            sb.append(" select a.*,g.code GoodsCode ,g.name GoodsName, ").append(" c.Color ,c.no ColorCode,sg.SizeGroupID,isnull(").append(column).append(",0) UnitPrice, isnull(g.RetailSales,0) RetailSales from GoodsBoxBarcode a join goods g on a.goodsid=g.goodsid  ")
                    .append(" join color c on a.colorid=c.colorid ").append("  join GoodsType gt on gt.GoodsTypeID = g.GoodsTypeID ").append("  join SizeGroup sg on sg.SizeGroupID =gt.SizeGroupID  ").append("  where GoodsBoxBarcode = '").append(GoodsBoxBarcode).append("'");
            List list = commonDao.findForJdbc(sb.toString());
            if (list.size() <= 0) {
                j.setObj(null);
                throw new BusinessException("未找到此箱条码[" + GoodsBoxBarcode + "]");
            }
            List retList = new ArrayList();
            for (int k = 0; k < list.size(); k++) {
                Map map = (Map) list.get(k);
                String SizeGroupID = (String) map.get("SizeGroupID");
                String sql = " select * from SizeGroupSize where SizeGroupID = '" + SizeGroupID + "'";
                List sizeList = commonDao.findForJdbc(sql.toString());
                Map sizeMap = new HashMap();
                for (Object o : sizeList) {
                    Map temp = (Map) o;
                    sizeMap.put(temp.get("No"), temp);
                }
                for (int i = 0; i < client.getMaxSize(); i++) {
                    Integer qty = (Integer) map.get("x_" + (i + 1));
                    if (qty == null || qty == 0) {
                        continue;
                    }
                    Map retMap = new HashMap();
                    retMap.put("GoodsCode", map.get("GoodsCode"));
                    retMap.put("GoodsID", map.get("GoodsID"));
                    retMap.put("ColorID", map.get("ColorID"));
                    retMap.put("ColorName", map.get("Color"));
                    retMap.put("GoodsName", map.get("GoodsName"));
                    retMap.put("ColorCode", map.get("ColorCode"));
                    Map temp = (Map) sizeMap.get(i + 1);
                    retMap.put("SizeID", temp.get("SizeID"));
                    retMap.put("SizeName", temp.get("Size"));
                    retMap.put("SizeCode", temp.get("No"));
                    retMap.put("IndexNo", i + 1);
                    retMap.put("Quantity", map.get("x_" + (i + 1)));
                    retMap.put("SizeGroupID", map.get("SizeGroupID"));
                    retMap.put("RetailSales", map.get("RetailSales"));
                    retMap.put("UnitPrice", map.get("UnitPrice"));
                    retMap.put("SizeStr", map.get("SizeStr"));
                    retList.add(retMap);
                }
            }

            j.setObj(retList);
        } catch (Exception e) {
            j.setSuccess(false);
            j.setMsg(e.getMessage());
            SysLogger.error(e.getMessage(), e);
        }
        return j;
    }

    /**
     * 根据客户(厂商)和类别,获取货品的价格类型
     * 
     * @param deptId 当前登录店铺(可选)
     * @param customerId 客户编码(可选)
     * @param type 价格类型(可选)
     * @return
     */
    private String getTypeColumn(String deptId, String customerId, String type) {
        String column = null;
        String data = null;
        StringBuffer sb = new StringBuffer();
        if (customerId != null && !"".equals(customerId) && !customerId.isEmpty()) {
            if (type != null) {
                sb.append("select ").append(type).append(" from customer where customerId = '").append(customerId).append("'");
            } else {
                sb.append("select PriceType from supplier where supplierId = '").append(customerId).append("'");
            }
            data = String.valueOf(commonDao.getData(sb.toString().trim()));
        } else if (type != null && type.equals("参考进价")) {
            data = "参考进价";
        } else if (type != null && type.equalsIgnoreCase("possales")) {
            data = String.valueOf(commonDao.getData(" select PriceType from Department where DepartmentID = '"+deptId+"' "));
        } else {
            data = "零售价";
        }
        if (null == data || "".equals(data) || "null".equals(data)) {
            return null;
        }
        if (data.contains("零售价")) {
            if ("零售价".equals(data)) {
                column = "RetailSales";
            } else if ("零售价2".equals(data)) {
                column = "RetailSales1";
            } else {
                column = "RetailSales" + (Integer.parseInt(data.substring(data.length() - 1)) - 1);
            }
        } else if (data.contains("批发价")) {
            if ("批发价".equals(data)) {
                column = "TradePrice";
            } else if ("批发价2".equals(data)) {
                column = "SalesPrice1";
            } else {
                column = "SalesPrice" + (Integer.parseInt(data.substring(data.length() - 1)) - 1);
            }
        } else if ("参考进价".equals(data)) {
            column = "PurchasePrice";
        }
        column = "g." + column;
        return column;
    }
    
    //type 当客户不为空后 ，Type 为字段名
    private Map<String,Object> getTypeColumnNew(String deptId, String customerId, String type) {
        String column = null;
        String data = null;
        String Discount="";
        String DiscountRate="0";
        StringBuffer sb = new StringBuffer();
        if (customerId != null && !"".equals(customerId) && !customerId.isEmpty()) {
            if (type != null) {
                sb.append("select ").append(type).append(" from customer where customerId = '").append(customerId).append("'");
            } else {
                sb.append("select PriceType from supplier where supplierId = '").append(customerId).append("'");
            }
            data = String.valueOf(commonDao.getData(sb.toString().trim()));
        } else if (type != null && type.equals("参考进价")) {
            data = "参考进价";
        } else if (type != null && type.equalsIgnoreCase("possales")) {
            data = String.valueOf(commonDao.getData(" select PriceType from Department where DepartmentID = '"+deptId+"' "));
        } else {
            data = "零售价";
        }
        if (null == data || "".equals(data) || "null".equals(data)) {
            return null;
        }
        if (data.contains("零售价")) {
            if ("零售价".equals(data)) {
                column = "RetailSales";
                if(customerId != null && !"".equals(customerId) && !customerId.isEmpty()){
                if("PriceType".equals(type)){
                	DiscountRate ="DiscountRate";
                }else if("OrderPriceType".equals(type)){
                	DiscountRate ="OrderDiscount";
                }else if("AllotPriceType".equals(type)){
                	DiscountRate ="AllotDiscount";
                }else if("ReplenishType".equals(type)){
                	DiscountRate ="ReplenishDiscount";
                }
                }
                
            } else if ("零售价2".equals(data)) {
                column = "RetailSales1";
                if(customerId != null && !"".equals(customerId) && !customerId.isEmpty()){
                    if("PriceType".equals(type)){
                    	DiscountRate ="DiscountRate";
                    }else if("OrderPriceType".equals(type)){
                    	DiscountRate ="OrderDiscount";
                    }else if("AllotPriceType".equals(type)){
                    	DiscountRate ="AllotDiscount";
                    }else if("ReplenishType".equals(type)){
                    	DiscountRate ="ReplenishDiscount";
                    }
                    }
            } else {
                column = "RetailSales" + (Integer.parseInt(data.substring(data.length() - 1)) - 1);
                if(customerId != null && !"".equals(customerId) && !customerId.isEmpty()){
                    if("PriceType".equals(type)){
                    	DiscountRate ="DiscountRate";
                    }else if("OrderPriceType".equals(type)){
                    	DiscountRate ="OrderDiscount";
                    }else if("AllotPriceType".equals(type)){
                    	DiscountRate ="AllotDiscount";
                    }else if("ReplenishType".equals(type)){
                    	DiscountRate ="ReplenishDiscount";
                    }
                    }
            }
        } else if (data.contains("批发价")) {
            if ("批发价".equals(data)) {
                column = "TradePrice";
                if(customerId != null && !"".equals(customerId) && !customerId.isEmpty()){
                    if("PriceType".equals(type)){
                    	DiscountRate ="DiscountRate";
                    }else if("OrderPriceType".equals(type)){
                    	DiscountRate ="OrderDiscount";
                    }else if("AllotPriceType".equals(type)){
                    	DiscountRate ="AllotDiscount";
                    }else if("ReplenishType".equals(type)){
                    	DiscountRate ="ReplenishDiscount";
                    }
                    }
                
            } else if ("批发价2".equals(data)) {
            	
                column = "SalesPrice1";
                
                if(customerId != null && !"".equals(customerId) && !customerId.isEmpty()){
                    if("PriceType".equals(type)){
                    	DiscountRate ="DiscountRate";
                    }else if("OrderPriceType".equals(type)){
                    	DiscountRate ="OrderDiscount";
                    }else if("AllotPriceType".equals(type)){
                    	DiscountRate ="AllotDiscount";
                    }else if("ReplenishType".equals(type)){
                    	DiscountRate ="ReplenishDiscount";
                    }
                    }
                
            } else {
                column = "SalesPrice" + (Integer.parseInt(data.substring(data.length() - 1)) - 1);
                if(customerId != null && !"".equals(customerId) && !customerId.isEmpty()){
                    if("PriceType".equals(type)){
                    	DiscountRate ="DiscountRate";
                    }else if("OrderPriceType".equals(type)){
                    	DiscountRate ="OrderDiscount";
                    }else if("AllotPriceType".equals(type)){
                    	DiscountRate ="AllotDiscount";
                    }else if("ReplenishType".equals(type)){
                    	DiscountRate ="ReplenishDiscount";
                    }
                    }
            }
        } else if ("参考进价".equals(data)) {
            column = "PurchasePrice";
        }
        column = "g." + column+"";
        
        Map<String,Object> map =new LinkedHashMap<>();
        map.put("column", column);
        map.put("DiscountRate", DiscountRate);
        
        return map;
    }
    /**
     * 根据货号或条码获取对应的货品ID
     * 
     * @param productId
     * @param commonDao
     * @return
     */
    protected String getGoodsId(String productId, CommonDao commonDao) {
        if (productId == null || "".equals(productId) || "null".equalsIgnoreCase(productId)) {
            return null;
        }
        int count = 0;
        String goodsId = null;
        count = Integer.parseInt(String.valueOf(commonDao.getData("select count(1) from barcode where barcode = ?", productId)));
        if (count > 0) {
            goodsId = String.valueOf(commonDao.getData("select goodsId from barcode where barcode = ?", productId));
        } else {
            goodsId = String.valueOf(commonDao.getData("select goodsId from goods where goodsId = ?", productId));
            if (null == goodsId || "".equals(goodsId) || "null".equalsIgnoreCase(goodsId)) {
                goodsId = String.valueOf(commonDao.getData("select goodsId from goods where code = ?", productId));
                if (null == goodsId || "".equals(goodsId) || "null".equalsIgnoreCase(goodsId)) {
                    List<Map<String, Object>> datas = BarcodeUtil.barcodeToGoods(productId);
                    if (datas != null && datas.size() > 0) {
                        for (int i = 0; i < datas.size(); i++) {
                            Map map = datas.get(i);
                            String goodsCode = String.valueOf(map.get("goodsCode"));
                            goodsId = String.valueOf(commonDao.getData(" select goodsId from goods g where g.code = '" + goodsCode + "' "));
                            if (null != goodsId && !"".equals(goodsId) && !"null".equalsIgnoreCase(goodsId)) {
                                break;
                            }
                        }
                    }
                }
                if (null == goodsId || "".equals(goodsId) || "null".equalsIgnoreCase(goodsId)) {
                    int len = 0;
                    int maxSizeLength = LoadUserCount.maxSizeLength;
                    int minSizeLength = LoadUserCount.minSizeLength;
                    int maxColorLength = LoadUserCount.maxColorLength;
                    int minColorLength = LoadUserCount.minColorLength;
                    // 尺码,颜色位数固定
                    if (BarcodeUtil.colorLength != null && BarcodeUtil.sizeLength != null) {
                        len = BarcodeUtil.colorLength + BarcodeUtil.sizeLength;
                        productId = productId.substring(0, productId.length() - len);
                        goodsId = String.valueOf(commonDao.getData("select goodsId from goods where code = ?", productId));
                        return goodsId;
                    } else {
                        if (BarcodeUtil.colorLength != null) {// 颜色位数固定
                            for (int i = minSizeLength; i <= maxSizeLength; i++) {
                                len = BarcodeUtil.colorLength + minSizeLength;
                                if (productId.length() > len) {
                                    productId = productId.substring(0, productId.length() - len);
                                    goodsId = String.valueOf(commonDao.getData("select goodsId from goods where code = ?", productId));
                                }
                                if (null != goodsId && !"".equals(goodsId) && !"null".equalsIgnoreCase(goodsId)) {
                                    return goodsId;
                                }
                            }
                        } else if (BarcodeUtil.sizeLength != null) {// 尺码位数固定
                            for (int i = minColorLength; i <= maxColorLength; i++) {
                                len = BarcodeUtil.sizeLength + minColorLength;
                                if (productId.length() > len) {
                                    productId = productId.substring(0, productId.length() - len);
                                    goodsId = String.valueOf(commonDao.getData("select goodsId from goods where code = ?", productId));
                                }
                                if (null != goodsId && !"".equals(goodsId) && !"null".equalsIgnoreCase(goodsId)) {
                                    return goodsId;
                                }
                            }
                        } else {// 尺码,颜色位数不固定
                            List<Map<String, Object>> datas = BarcodeUtil.barcodeToGoods(productId);
                            if (datas != null) {
                                for (int i = 0; i < datas.size(); i++) {
                                    Map map = datas.get(i);
                                    String goodsCode = String.valueOf(map.get("goodsCode"));
                                    goodsId = String.valueOf(commonDao.getData("select goodsId from goods where code = ?", goodsCode));
                                    if (null != goodsId && !"".equals(goodsId) && !"null".equalsIgnoreCase(goodsId)) {
                                        return goodsId;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return goodsId;
    }

    protected Map<String, String> simpleAnalyticalBarcode(String barcode, CommonDao commonDao) {
        Map<String, String> tmap = new HashMap<String, String>();
        String goodsId = null;
        String sizeId = null;
        String colorId = null;
        List list = commonDao.findForJdbc(" select GoodsID,ColorID,SizeID from barcode ", barcode);
        for (int i = 0; i < list.size(); i++) {
            Map<String, Object> temp = (Map<String, Object>) list.get(i);
            goodsId = (String) temp.get("GoodsID");
            colorId = (String) temp.get("ColorID");
            sizeId = (String) temp.get("SizeID");
        }
        if (list.size() == 0) {
            List<Map<String, Object>> datas = BarcodeUtil.barcodeToGoods(barcode);
            if (datas != null && datas.size() > 0) {
                for (int i = 0; i < datas.size(); i++) {
                    Map map = datas.get(i);
                    String goodsCode = String.valueOf(map.get("goodsCode"));
                    String colorNo = String.valueOf(map.get("colorCode"));
                    String sizeNo = String.valueOf(map.get("sizeCode"));
                    // 获取货品,颜色,尺码的ID
                    goodsId = String.valueOf(commonDao.getData(" select goodsId from goods g where g.code = '" + goodsCode + "' "));
                    sizeId = String.valueOf(commonDao.getData(" select sizeId from size s where s.no = '" + sizeNo + "' "));
                    colorId = null;
                    // 判断使用的颜色类型
                    if (LoadUserCount.colorOption == 4) {
                        colorId = String.valueOf(commonDao.getData(" select colorId from brandcolor c where c.no = '" + colorNo + "' and brandId = (select brandId from goods where goodsId = '" + goodsId + "') "));
                    } else {
                        colorId = String.valueOf(commonDao.getData(" select colorId from color c where c.no = '" + colorNo + "' "));
                    }
                }
            }
        }
        if (goodsId != null && "".equals(goodsId) && "null".equalsIgnoreCase(goodsId) && colorId != null && "".equals(colorId) && "null".equalsIgnoreCase(colorId) && sizeId != null && "".equals(sizeId) && "null".equalsIgnoreCase(sizeId)) {
            tmap.put("goodsId", goodsId);
            tmap.put("colorId", colorId);
            tmap.put("sizeId", sizeId);
        }
        return tmap;
    }

    // 按照伏羲标准的条码解析规则解析条码[货品编码+颜色编码+尺码编码]
    // protected List<Map<String, Object>> analyticalStandardBarcode(String
    // barcode){
    // List<Map<String, Object>> data = new ArrayList<Map<String,Object>>();
    // Map<String, Object> map = new HashMap<String, Object>();
    // String goodsNo = null;
    // String colorNo = null;
    // String sizeNo = null;
    // int maxSizeLength = LoadUserCount.maxSizeLength;
    // int minSizeLength = LoadUserCount.minSizeLength;
    // int maxColorLength = LoadUserCount.maxColorLength;
    // int minColorLength = LoadUserCount.minColorLength;
    // //颜色编码和尺码编码的位数固定
    // if(maxSizeLength == minSizeLength && maxColorLength == minColorLength){
    // sizeNo = barcode.substring(barcode.length()-maxSizeLength,
    // barcode.length());
    // colorNo =
    // barcode.substring(barcode.length()-maxSizeLength-maxColorLength,
    // barcode.length()-maxSizeLength);
    // goodsNo = barcode.substring(0,
    // barcode.length()-maxSizeLength-maxColorLength);
    // map.put("goodsCode", goodsNo);
    // map.put("colorCode", colorNo);
    // map.put("sizeCode", sizeNo);
    // data.add(map);
    // }else{
    // data = null;
    // }
    // return data;
    // }

    /**
     * 通过箱条码的配码信息获取货品对应的尺码信息(用于箱条码录入单据的重新纵向显示)
     * 
     * @param list
     * @param client
     * @param commonDao
     * @return
     */
    protected List getDetailTemp(List list, Client client, CommonDao commonDao) {
        List retList = new ArrayList();
        for (int k = 0; k < list.size(); k++) {
            Map map = (Map) list.get(k);
            String SizeGroupID = (String) map.get("SizeGroupID");
            String sql = " select * from SizeGroupSize where SizeGroupID = '" + SizeGroupID + "'";
            List sizeList = commonDao.findForJdbc(sql.toString());
            Map sizeMap = new HashMap();
            for (Object o : sizeList) {
                Map temp = (Map) o;
                sizeMap.put(temp.get("No"), temp);
            }
            for (int i = 0; i < client.getMaxSize(); i++) {
                Integer qty = (Integer) map.get("x_" + (i + 1));
                if (qty == null || qty == 0) {
                    continue;
                }
                Map retMap = new HashMap();
                retMap.put("GoodsName", map.get("GoodsName"));
                retMap.put("GoodsCode", map.get("GoodsCode"));
                retMap.put("GoodsID", map.get("GoodsID"));
                retMap.put("ColorID", map.get("ColorID"));
                retMap.put("Color", map.get("Color"));
                retMap.put("ColorCode", map.get("ColorCode"));
                Map temp = (Map) sizeMap.get(i + 1);
                retMap.put("SizeID", temp.get("SizeID"));
                retMap.put("Size", temp.get("Size"));
                retMap.put("SizeCode", temp.get("No"));
                retMap.put("BoxQty", map.get("BoxQty"));
                retMap.put("QuantitySum", map.get("QuantitySum"));
                retMap.put("IndexNo", i + 1);
                retMap.put("Quantity", (Integer) map.get("x_" + (i + 1)) * (Integer) map.get("BoxQty"));
                retMap.put("OneBoxQty", map.get("x_" + (i + 1)));
                retMap.put("SizeGroupID", map.get("SizeGroupID"));
                retMap.put("RetailSales", map.get("RetailSales"));
                retMap.put("UnitPrice", map.get("UnitPrice"));
                retMap.put("meno", map.get("meno"));
                retMap.put("DiscountPrice", map.get("DiscountPrice"));
                retMap.put("DiscountRate", map.get("DiscountRate"));
                retMap.put("SizeStr", map.get("SizeStr"));
                retMap.put("Barcode", "");
                retList.add(retMap);
            }
        }
        return retList;
    }
    
    
    /*
     * 
     * 根据 客户的定义的价类型与获取  goods的单价  销售发货单  多颜色与单颜色录入
     * 
     */
    
    @RequestMapping(params = "getSalesGoods")
    @ResponseBody
    public AjaxJson getSalesGoods(HttpServletRequest req) {
        AjaxJson j = new AjaxJson();
        j.setAttributes(new HashMap<String, Object>());
        try {
        	
            String param = oConvertUtils.getString(req.getParameter("param"));
            int page = oConvertUtils.getInt(req.getParameter("currPage"));
            String customerid=oConvertUtils.getString(req.getParameter("customerid"));
            
           String type=oConvertUtils.getString(req.getParameter("Type"));
            
            String column = getTypeColumn(null,customerid, type);
            
            StringBuffer sb = new StringBuffer();
            sb.append(
                    " select GoodsID,name+'('+code+')' Name,name GoodsName,Code,g.PresentFlag,isnull(TradePrice,0) TradePrice,isnull(RetailSales,0) RetailSales,isnull(g.Discount,10) Discount,DiscountFlag,isnull(RetailSales1,0) RetailSales1,isnull("+ column +",0) UnitPrice,"
                            + "(select GoodsType from GoodsType where GoodsTypeID = g.GoodsTypeID) GoodsType,SubType,Age,Season,(select Serial from BrandSerial where BrandSerialID = g.BrandSerialID) BrandSerial,Style,Sex,Kind,Model,"
                            + "isnull(RetailSales2,0) RetailSales2,isnull(RetailSales3,0) RetailSales3,isnull(RetailSales4,0) RetailSales4,isnull(RetailSales5,0) RetailSales5,"
                            + "isnull(RetailSales6,0) RetailSales6,isnull(RetailSales7,0) RetailSales7,isnull(RetailSales8,0) RetailSales8,isnull(SalesPrice1,0) SalesPrice1,"
                            + "isnull(SalesPrice2,0) SalesPrice2,isnull(SalesPrice3,0) SalesPrice3,isnull(SalesPrice4,0) SalesPrice4,isnull(SalesPrice5,0) SalesPrice5, "
                            + "isnull(SalesPrice6,0) SalesPrice6,isnull(SalesPrice7,0) SalesPrice7,isnull(SalesPrice8,0) SalesPrice8,sizIndex=(select max(no) as maxsize from SizeGroupSize where sizeGroupId=g.GroupID) from  Goods g ");
                           
                           if("".equals(param) || param !=null){
            		       sb.append("where g.code like '%").append(param).append("%' or g.Name like '%").append(param).append("%' or g.GoodsID like '%").append(param).append("%'");
                           }
                            
                    sb.append(" order by len(code) asc ");
            List list = commonDao.findForJdbc(sb.toString(), page, 15);
            j.setObj(list);
        } catch (Exception e) {
            j.setSuccess(false);
            j.setMsg(e.getMessage());
            SysLogger.error(e.getMessage(), e);
        }
        return j;
    }
    

}
