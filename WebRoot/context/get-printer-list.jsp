<%@page import="java.util.Date"%>
<%@page import="java.util.Map"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jstl/fmt_rt" prefix="fmt"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%> 
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%> 
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<%
	Map map = (Map)request.getAttribute("datas");
	if(null == map){
		request.getRequestDispatcher("/common.do?getPrinterList").forward(request, response);
		return;
	}
%>
<script src='http://${datas.printIp}:${datas.printPort}/print/CLodopfuncs.js'></script>
<!--声明当前页面的编码集:charset=gbk,gb2312(中文编码), utf-8(国际编码)-->
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<!--当前页面的三要素-->
<title>打印</title>
<script>
//定义全局变量  
var LODOP;
//打印  
function print() {
    if(!LODOP){  
        //getLodop的任务是判断浏览器的类型并决定采用哪个对象实例，并检测控件是否安装、是否最新版本、并引导安装或升级   
    	LODOP=getLodop();
    }  
    if(LODOP){ 
    	CLODOP.Create_Printer_List(document.getElementById('print-select'));
    	var obj = document.getElementById('print-select');
    	var prints = getPrints(obj);
    	var printers = '';
        for(var i=0; i<prints.length; i++) {
        	if(i == prints.length-1){
        		printers += prints[i].innerHTML;
        	}else{
        		printers += prints[i].innerHTML+",";
        	}
        }    
        window.androidShare.jsMethod(printers);
    }
}

function getPrints(obj) {   //获取option元素
    var arr1 = obj.childNodes;
    var arr = [];
    for(var i=0; i<arr1.length; i++) {
        if(arr1[i].nodeName == 'OPTION') {
            arr.push(arr1[i]);            
        }
    }
    return arr;        
}

</script>
</head>
	<object  id="LODOP_OB" classid="clsid:2105C259-1E0C-4534-8141-A753534CB4CA" width=0 height=0>  
           <embed id="LODOP_EM" type="application/x-print-lodop" width=0 height=0 ></embed>  
    </object>  
<body onload="print()">
	<div id="print_div">
		<select id="print-select">打印机:</select>
	</div>
</body>


</html>