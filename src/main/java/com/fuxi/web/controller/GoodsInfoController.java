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
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fuxi.core.common.dao.impl.CommonDao;
import com.fuxi.core.common.model.json.AjaxJson;
import com.fuxi.core.common.service.GoodsInfoService;
import com.fuxi.system.util.Client;
import com.fuxi.system.util.ResourceUtil;
import com.fuxi.system.util.SysLogger;
import com.fuxi.system.util.oConvertUtils;

/**
 * Title: GoodsInfoController Description: 货品资料逻辑控制器
 * 
 * @author LYJ
 * 
 */
@Controller
@RequestMapping("/goodsInfo")
public class GoodsInfoController extends BaseController {

    private Logger log = Logger.getLogger(GoodsInfoController.class);

    @Autowired
    private CommonDao commonDao;
    @Autowired
    private GoodsInfoService goodsInfoService;
    
    
    @RequestMapping(params = "glist")
    @ResponseBody
    public AjaxJson glist(HttpServletRequest req){
    	AjaxJson j=new AjaxJson();
    	try{
    	String Code=oConvertUtils.getString(req.getParameter("Code"));
    	int page =Integer.parseInt(oConvertUtils.getString(req.getParameter("page")));
    	List<Map<String,Object>> ls=goodsInfoService.goodslist(Code,page,15);
    	if(ls.size()>0){
    		j.setSuccess(true);
    		j.setObj(ls);
    	}else{
    		j.setSuccess(true);
    		j.setMsg("暂无数据");
    	}
    	}catch(Exception e){
    		j.setSuccess(false);
            j.setMsg(e.getMessage());
            e.printStackTrace();
    	}
    	
    return j;
    }
    //单个货品的详情，包含货品颜色
    @RequestMapping(params = "goodsDetail")
    @ResponseBody
    public AjaxJson goodsDetail(HttpServletRequest req){
    	Client client = ResourceUtil.getClientFromSession(req);
    	AjaxJson j=new AjaxJson();
    	try{
    		String GoodsID =oConvertUtils.getString(req.getParameter("GoodsID"));
    		//String SupplierID=oConvertUtils.getString(req.getParameter("SupplierID"));
    		Map<String, Object> map=goodsInfoService.goodsColor(GoodsID, null, "采购", null,client);
    		j.setAttributes(map);
    		j.setSuccess(true);
    	}catch(Exception e){
    		j.setSuccess(false);
    		j.setMsg(e.getMessage());
    		e.printStackTrace();
    	}
    	return j;
    }
    
    @RequestMapping(params = "goodslist")
    @ResponseBody
    public AjaxJson goodslist(HttpServletRequest req){
    	
    	   Client client = ResourceUtil.getClientFromSession(req);
           AjaxJson j = new AjaxJson();
           j.setAttributes(new HashMap<String, Object>());
           try {
        	   
        	   String Code = oConvertUtils.getString(req.getParameter("Code"));
        	   String Type = oConvertUtils.getString(req.getParameter("Type"));
        	   String CustomerID=oConvertUtils.getString(req.getParameter("CustomerID"));
        	   String SupplierID=oConvertUtils.getString(req.getParameter("SupplierID"));
        	   int page = Integer.parseInt(oConvertUtils.getString(req.getParameter("page")));
               //此是返回货品的所有颜色的，不是只有货品 一个货品可能有多条记录
              List<Map<String,Object>> ls=goodsInfoService.goodslist(Code,Type, CustomerID,SupplierID, page, 15);	 
              System.out.println("ls总条数："+ls.size());
             // List<Map<String,Object>> goodslist=new ArrayList<>();
              if(ls.size()>0){
            	for(int i=0;i<ls.size();i++){
            		Map<String,Object> map=ls.get(i);
            		//Map<String,Object> m1=getMap(goodslist,map);
            		
            	/*	if(m1==null){
            			Map<String,Object> m2=new LinkedHashMap<>();	
            			m2.put("GoodsID", String.valueOf(map.get("GoodsID")));
            			m2.put("Code", String.valueOf(map.get("Code")));
            			m2.put("Name", String.valueOf(map.get("Name")));
            			m2.put("GroupID", String.valueOf(map.get("GroupID")));
            			m2.put("RetailSales", String.valueOf(map.get("RetailSales")));
            			if(map.get("RetailSales") !=null && !"".equals(String.valueOf(map.get("RetailSales"))) && !"null".equals(String.valueOf(map.get("RetailSales")))){
            				m2.put("RetailSales", new BigDecimal(String.valueOf(map.get("RetailSales"))).setScale(2,BigDecimal.ROUND_DOWN));
            			}else{
            				m2.put("RetailSales","");
            			}
            			
            			m2.put("Quantity", String.valueOf(map.get("Quantity")));
            			m2.put("Amount", String.valueOf(map.get("Amount")));
            			
            			goodslist.add(m2);	
            		} */
            	}	 
               Map<String,Object> m=new LinkedHashMap<>();	
        		  m.put("goodslist", ls);
        		 // m.put("goodslist", goodslist);
        		  j.setAttributes(m);
        		  j.setSuccess(true);
        		  j.setMsg("返回成功");
        	  }else{
        		  j.setSuccess(true);
        		  j.setMsg("暂无数据");
        	  }
        	     
           }catch(Exception e){
        	   
        	   j.setSuccess(false);
               j.setMsg(e.getMessage());
               SysLogger.error(e.getMessage(), e);   
           }
           
           return j;
    	
  }
    
   public Map<String,Object> getMap(List<Map<String,Object>> ls,Map<String,Object> goodsMap){
	   
	   for(int i=0;i<ls.size();i++){
		   Map<String,Object>   map=ls.get(i); 
		if(String.valueOf(map.get("GoodsID")) == String.valueOf(goodsMap.get("GoodsID"))){
			
			 return map; 
		} 
	   }
	   
	   return null; 
   }
    
    
    /**
     * 获取货品资料明细方法
     * 
     * @param req
     * @return
     */
    @RequestMapping(params = "getGoodsInfo")
    @ResponseBody
    public AjaxJson getGoodsInfo(HttpServletRequest req) {
        Client client = ResourceUtil.getClientFromSession(req);
        AjaxJson j = new AjaxJson();
        j.setAttributes(new HashMap<String, Object>());
        try {
            Map<String, Object> map = new HashMap<String, Object>();
            String goodsId = oConvertUtils.getString(req.getParameter("goodsId"));
            StringBuffer sb = new StringBuffer();
            sb.append(" select GoodsID,g.name GoodsName,g.code GoodsCode,g.GoodsTypeID,GoodsType,isnull(SubType,'') SubType, ")
                    .append(" g.BrandID,isnull(Brand,'') Brand,g.BrandSerialID,isnull(Serial,'') Serial,isnull(Kind,'') Kind,isnull(g.Age,'') Age,isnull(g.Season,'') Season,g.SupplierID,isnull(Supplier,'') Supplier,isnull(g.SupplierCode,'') SupplierCode,isnull(PurchasePrice,'') PurchasePrice,isnull(TradePrice,'') TradePrice, ")
                    .append(" isnull(RetailSales,'') RetailSales,isnull(SalesPrice1,'') SalesPrice1,isnull(SalesPrice2,'') SalesPrice2,isnull(SalesPrice3,'') SalesPrice3,isnull(RetailSales1,'') RetailSales1,isnull(RetailSales2,'') RetailSales2,isnull(RetailSales3,'') RetailSales3 ")
                    .append(" from  Goods g left join Brand b on g.brandId = b.brandId left join GoodsType gt on g.goodsTypeId  = gt.goodsTypeId ").append(" left join BrandSerial bs on g.BrandSerialID = bs.BrandSerialID left join Supplier s on g.SupplierID = s.SupplierID where goodsId = ? ");
            List list = commonDao.findForJdbc(sb.toString(), goodsId);
            List data = commonDao.findForJdbc(" select c.Color,c.ColorID from goodsColor gc join Color c on gc.colorId = c.colorId where goodsID = ? ", goodsId);
            map.put("list", list);
            map.put("data", data);
            j.setAttributes(map);
        } catch (Exception e) {
            j.setSuccess(false);
            j.setMsg(e.getMessage());
            SysLogger.error(e.getMessage(), e);
        }
        return j;
    }

    /**
     * 保存货品资料明细方法
     * 
     * @param req
     * @return
     */
    @RequestMapping(params = "saveGoodsInfo")
    @ResponseBody
    public AjaxJson saveGoodsInfo(HttpServletRequest req) {
        Client client = ResourceUtil.getClientFromSession(req);
        AjaxJson j = new AjaxJson();
        j.setAttributes(new HashMap<String, Object>());
        try {
            String goodsCode = processingStringData(oConvertUtils.getString(req.getParameter("goodsCode")));
            String goodsName = processingStringData(oConvertUtils.getString(req.getParameter("goodsName")));
            String goodsTypeId = processingStringData(oConvertUtils.getString(req.getParameter("goodsTypeId")));
            String goodsSubType = processingStringData(oConvertUtils.getString(req.getParameter("goodsSubType")));
            String brandId = processingStringData(oConvertUtils.getString(req.getParameter("brandId")));
            String brandSerialId = processingStringData(oConvertUtils.getString(req.getParameter("brandSerialId")));
            String kind = processingStringData(oConvertUtils.getString(req.getParameter("kind")));
            String age = processingStringData(oConvertUtils.getString(req.getParameter("age")));
            String season = processingStringData(oConvertUtils.getString(req.getParameter("season")));
            String supplierId = processingStringData(oConvertUtils.getString(req.getParameter("supplierId")));
            String supplierCode = processingStringData(oConvertUtils.getString(req.getParameter("supplierCode")));
            String purchasePrice = processingNumberData(oConvertUtils.getString(req.getParameter("purchasePrice")));
            String tradePrice = processingNumberData(oConvertUtils.getString(req.getParameter("tradePrice")));
            String retailSales = processingNumberData(oConvertUtils.getString(req.getParameter("retailSales")));
            String retailSales1 = processingNumberData(oConvertUtils.getString(req.getParameter("retailSales1")));
            String retailSales2 = processingNumberData(oConvertUtils.getString(req.getParameter("retailSales2")));
            String retailSales3 = processingNumberData(oConvertUtils.getString(req.getParameter("retailSales3")));
            String salesPrice1 = processingNumberData(oConvertUtils.getString(req.getParameter("salesPrice1")));
            String salesPrice2 = processingNumberData(oConvertUtils.getString(req.getParameter("salesPrice2")));
            String salesPrice3 = processingNumberData(oConvertUtils.getString(req.getParameter("salesPrice3")));
            String colorId1 = processingStringData(oConvertUtils.getString(req.getParameter("colorId1")));
            String colorId2 = processingStringData(oConvertUtils.getString(req.getParameter("colorId2")));
            String colorId3 = processingStringData(oConvertUtils.getString(req.getParameter("colorId3")));
            String colorId4 = processingStringData(oConvertUtils.getString(req.getParameter("colorId4")));
            String colorId5 = processingStringData(oConvertUtils.getString(req.getParameter("colorId5")));
            String colorId6 = processingStringData(oConvertUtils.getString(req.getParameter("colorId6")));
            String colorId7 = processingStringData(oConvertUtils.getString(req.getParameter("colorId7")));
            String colorId8 = processingStringData(oConvertUtils.getString(req.getParameter("colorId8")));
            String goodsId =
                    goodsInfoService.saveGoodsInfoMsg(goodsCode, goodsName, goodsTypeId, goodsSubType, brandId, brandSerialId, kind, age, season, supplierId, supplierCode, purchasePrice, tradePrice, retailSales, retailSales1, retailSales2, retailSales3, salesPrice1, salesPrice2, salesPrice3,
                            colorId1, colorId2, colorId3, colorId4, colorId5, colorId6, colorId7, colorId8, client);
            j.setObj(goodsId);
        } catch (Exception e) {
            j.setSuccess(false);
            j.setMsg(e.getMessage());
            SysLogger.error(e.getMessage(), e);
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
        }
        return j;
    }

    /**
     * 修改货品资料明细方法
     * 
     * @param req
     * @return
     */
    @RequestMapping(params = "updateGoodsInfo")
    @ResponseBody
    public AjaxJson updateGoodsInfo(HttpServletRequest req) {
        Client client = ResourceUtil.getClientFromSession(req);
        AjaxJson j = new AjaxJson();
        j.setAttributes(new HashMap<String, Object>());
        try {
            String goodsId = processingStringData(oConvertUtils.getString(req.getParameter("goodsId")));
            String goodsSubType = processingStringData(oConvertUtils.getString(req.getParameter("goodsSubType")));
            String brandId = processingStringData(oConvertUtils.getString(req.getParameter("brandId")));
            String brandSerialId = processingStringData(oConvertUtils.getString(req.getParameter("brandSerialId")));
            String kind = processingStringData(oConvertUtils.getString(req.getParameter("kind")));
            String age = processingStringData(oConvertUtils.getString(req.getParameter("age")));
            String season = processingStringData(oConvertUtils.getString(req.getParameter("season")));
            String supplierId = processingStringData(oConvertUtils.getString(req.getParameter("supplierId")));
            String supplierCode = processingStringData(oConvertUtils.getString(req.getParameter("supplierCode")));
            String purchasePrice = processingNumberData(oConvertUtils.getString(req.getParameter("purchasePrice")));
            String tradePrice = processingNumberData(oConvertUtils.getString(req.getParameter("tradePrice")));
            String retailSales = processingNumberData(oConvertUtils.getString(req.getParameter("retailSales")));
            String retailSales1 = processingNumberData(oConvertUtils.getString(req.getParameter("retailSales1")));
            String retailSales2 = processingNumberData(oConvertUtils.getString(req.getParameter("retailSales2")));
            String retailSales3 = processingNumberData(oConvertUtils.getString(req.getParameter("retailSales3")));
            String salesPrice1 = processingNumberData(oConvertUtils.getString(req.getParameter("salesPrice1")));
            String salesPrice2 = processingNumberData(oConvertUtils.getString(req.getParameter("salesPrice2")));
            String salesPrice3 = processingNumberData(oConvertUtils.getString(req.getParameter("salesPrice3")));
            String colorId1 = processingStringData(oConvertUtils.getString(req.getParameter("colorId1")));
            String colorId2 = processingStringData(oConvertUtils.getString(req.getParameter("colorId2")));
            String colorId3 = processingStringData(oConvertUtils.getString(req.getParameter("colorId3")));
            String colorId4 = processingStringData(oConvertUtils.getString(req.getParameter("colorId4")));
            String colorId5 = processingStringData(oConvertUtils.getString(req.getParameter("colorId5")));
            String colorId6 = processingStringData(oConvertUtils.getString(req.getParameter("colorId6")));
            String colorId7 = processingStringData(oConvertUtils.getString(req.getParameter("colorId7")));
            String colorId8 = processingStringData(oConvertUtils.getString(req.getParameter("colorId8")));
            int count =
                    goodsInfoService.updateGoodsInfoMsg(goodsId, goodsSubType, brandId, brandSerialId, kind, age, season, supplierId, supplierCode, purchasePrice, tradePrice, retailSales, retailSales1, retailSales2, retailSales3, salesPrice1, salesPrice2, salesPrice3, colorId1, colorId2, colorId3,
                            colorId4, colorId5, colorId6, colorId7, colorId8, client);
            j.setObj(count);
        } catch (Exception e) {
            j.setSuccess(false);
            j.setMsg(e.getMessage());
            SysLogger.error(e.getMessage(), e);
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
        }
        return j;
    }
    /*
     * 删除货品
     * */
    @RequestMapping(params = "deleteGoodsInfo")
    @ResponseBody
  public AjaxJson  deleteGoodsInfo(HttpServletRequest req){
    	  Client client = ResourceUtil.getClientFromSession(req);
          AjaxJson j = new AjaxJson();
          j.setAttributes(new HashMap<String, Object>());
          try{
        	  String goodsId = processingStringData(oConvertUtils.getString(req.getParameter("GoodsID")));
        	  String msg= goodsInfoService.delgoods(goodsId);
        	  j.setSuccess(true);
        	  j.setMsg(msg);
          }catch(Exception e){
        	  j.setSuccess(false);
        	  j.setMsg(e.getMessage());
        	  SysLogger.error(e.getMessage(), e);
          }
          return j;
    	
    }
    @RequestMapping(params = "audit")
    @ResponseBody
  public AjaxJson  audit(HttpServletRequest req){
 	  Client client = ResourceUtil.getClientFromSession(req);
      AjaxJson j = new AjaxJson();
      j.setAttributes(new HashMap<String, Object>());
      try{
    	  String goodsId = processingStringData(oConvertUtils.getString(req.getParameter("GoodsID")));
    	  int GoodsAuditFlag =Integer.parseInt(processingStringData(oConvertUtils.getString(req.getParameter("AuditFlag"))));
    	  String msg= goodsInfoService.audit(goodsId, GoodsAuditFlag, client);
    	  j.setSuccess(true);
    	  j.setMsg(msg);
      }catch(Exception e){
    	  j.setSuccess(false);
    	  j.setMsg(e.getMessage());
    	  SysLogger.error(e.getMessage(), e);
      }
      return j;
	  
  }

    /**
     * 格式化空字符串方法
     * 
     * @param data
     * @return
     */
    private String processingStringData(String data) {
        if (null == data || data.isEmpty() || "null".equalsIgnoreCase(data)) {
            return null;
        }
        return data;
    }

    /**
     * 格式化空数据类型方法
     * 
     * @param data
     * @return
     */
    private String processingNumberData(String data) {
        if (null == data || data.isEmpty() || "null".equalsIgnoreCase(data) || "0".equals(data) || "0.0".equals(data)) {
            return null;
        }
        return data;
    }

}
