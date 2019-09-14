package com.fuxi.web.controller;

import java.util.LinkedHashMap;
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

@Controller
@RequestMapping("/color")
public class ColorController extends BaseController {
	   private Logger log = Logger.getLogger(ColorController.class);

	    @Autowired
	    private CommonDao commonDao;
	    
	    
	    
	    
	  
	    @RequestMapping(params="coloradd")
	    @ResponseBody
	    public AjaxJson coloradd(HttpServletRequest req){
	    	Client client = ResourceUtil.getClientFromSession(req);
	    	AjaxJson j=new AjaxJson();
	    	
	    	try {
	    		String No = oConvertUtils.getString(req.getParameter("No"));
	        	String Color = oConvertUtils.getString(req.getParameter("Color"));
	        	String GoodsID =oConvertUtils.getString(req.getParameter("GoodsID"));
	        	String ColorID="";
	          Object o=	commonDao.getData("select No from Color where No='"+No+"'");
	          
	          if(o !=null && !"".equals(String.valueOf(o))){
	        	j.setMsg("颜色编码已存在，新建失败");  
	        	j.setObj(ColorID);
	          }else{
	        	         // 生成ID
	              ColorID  = commonDao.getNewIDValue(1);
	              
	              if (ColorID == null) {
	            	  j.setMsg("生成主键失败");
	                  throw new BusinessException("生成主键失败");
	                 
	              }
	             
	        	 int count= commonDao.executeSql("Insert into Color(ColorID,No,Color) values(?,?,?)", ColorID,No,Color); 

	        	 if(count>0){
	        		 j.setMsg("新建成功");
	        	 if(!"".equals(GoodsID) && !"undefined".equals(GoodsID) && GoodsID !=null){
	        		                  //如果是修改货品资料不是新增是 可以写到货品颜色中
	        		 Object c=	commonDao.getData("select ColorID from GoodsColor where GoodsID ='"+GoodsID+"' and ColorID='"+ColorID+"'");
	        		
	        		 if(c ==null || "".equals(String.valueOf(c))){
	        			 commonDao.executeSql("Insert into GoodsColor(GoodsID,ColorID) values(?,?)",GoodsID,ColorID); 
	        		 }
	        		 
	        		 
	        	 }	 	        		 
	        		 j.setObj(ColorID);
	        	 }else{
	        		 j.setMsg("新建失败");
	        		 j.setObj("");
	        	 }
	        	 
	          }		
	    	}catch(Exception e){
	        	   
	        	   j.setSuccess(false);
	               j.setMsg(e.getMessage());
	               SysLogger.error(e.getMessage(), e);   
	           }
	           
	           return j;
	    }
	    
	    @RequestMapping(params="colorEdit")
	    @ResponseBody
	    public AjaxJson colorEdit(HttpServletRequest req){
	    	Client client = ResourceUtil.getClientFromSession(req);
	    	AjaxJson j=new AjaxJson();
	    	
	    	try {
	    		String No = oConvertUtils.getString(req.getParameter("No"));
	        	String Color = oConvertUtils.getString(req.getParameter("Color"));
	        	String ColorID=oConvertUtils.getString(req.getParameter("ColorID"));;
	          Object o=	commonDao.getData("select No from Color where No="+No+"");
	          
	          if(o !=null && !"".equals(String.valueOf(o))){
	        	j.setMsg("颜色编码已存在，修改失败");  
	        	j.setObj(ColorID);
	          }else{
	        	
	             
	        	commonDao.executeSql("update Color set No = ? ,Color=? where ColorID =? ", No,Color,ColorID); 
	        	
	        	Map<String,Object> map =new LinkedHashMap<>();
	        	map.put("ColorID",ColorID);
	        	map.put("No",No);
	        	map.put("Color",Color);
	        	 j.setMsg("修改成功");
        		 j.setObj(map);  
	        	 
	        	 
	          }		
	    	}catch(Exception e){
	        	   
	        	   j.setSuccess(false);
	               j.setMsg(e.getMessage());
	               SysLogger.error(e.getMessage(), e);   
	           }
	           
	           return j;
	    }
	    
}
