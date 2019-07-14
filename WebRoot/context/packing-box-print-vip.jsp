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
		request.getRequestDispatcher("/common.do?packingBoxPrintVip").forward(request, response);
		return;
	}
	int i = 0;int j = 1; request.setAttribute("j",j);
%>
<script language="javascript" src="${pageContext.request.contextPath}/plug-in/jquery/LodopFuncs.js"></script> 
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
    	var iprinter = document.getElementById('printer').value;
    	var headstr = "<html><head></head><body>"; 
        var footstr = "</body>";    
        var printData = document.getElementById("print_div").innerHTML;  
        //var oldstr = document.body.innerHTML;    
        var data = headstr+printData+footstr;
        LODOP.ADD_PRINT_HTM("20mm","6mm","RightMargin:6mm","BottomMargin:10mm",data);
        LODOP.SET_PRINT_PAGESIZE(1, "137.5mm", "216mm","CreateCustomPage");
        LODOP.SET_PRINT_MODE("POS_BASEON_PAPER",1);
        LODOP.SET_PRINT_MODE("FULL_WIDTH_FOR_OVERFLOW",true);
        LODOP.SET_PRINT_MODE("NOCLEAR_AFTER_PRINT",true);//不清空打印内容	
// 		LODOP.SET_PREVIEW_WINDOW(0,1,0,0,0,"");//打印前弹出选择打印机的对话框	
// 		LODOP.SET_PRINT_MODE("AUTO_CLOSE_PREWINDOW",1);//打印后自动关闭预览窗口
		if(iprinter != undefined && iprinter.length > 0){
			LODOP.SET_PRINTER_INDEXA(iprinter);
		}
		LODOP.PRINT();
    }
}

//界面加载完毕后的操作
window.onload=function(){
	window.setTimeout('print()',1000);
}

</script>
</head>
	<object  id="LODOP_OB" classid="clsid:2105C259-1E0C-4534-8141-A753534CB4CA" width=0 height=0>  
           <embed id="LODOP_EM" type="application/x-print-lodop" width=0 height=0 ></embed>
           <param name="Color" value="#FFFFFF">
    </object>  
<body>
	<!-- 默认打印机的名称 -->
	<input id="printer" value="${datas.printer}" type="hidden"/>
	<!-- 打印内容 -->
	<div id="print_div">
		<!--标题-->
		<div class="title" align="center">
			<strong>${datas.docType}</strong>
		</div>
		<!-- 仓库位置 -->
		<div class="warehouse" align="right">
			${datas.warehouse}
		</div>
		<div class="detail">
			<table class="t_d" border="1" cellspacing="0" cellpadding="0">
				<tr>
					<th>序号</th>
					<th>箱号</th>
					<th>货号</th>
					<th>条形码</th>
					<th>规格</th>
					<th>数量</th>
				</tr>
				<c:forEach begin="0" end="${fn:length(datas.dataList)}" items="${datas.dataList}" var="d" varStatus="status">
					<tr>
						<td class="center">${status.count}</td>
						<td class="center">${d.boxNo}</td>
						<td class="center">${d.goodsCode}</td>
						<td class="center">${d.barcode}</td>
						<td class="center">${d.size}</td>
						<td class="center">${d.quantity}</td>
					</tr>
				</c:forEach>
					<tr>
						<td colspan="5" class="center">汇总/${datas.boxCount}箱</td>
						<td class="center">${datas.quantitySum}</td>
					</tr>
			</table>
		</div>
		<style type="text/css">
			*{margin:0 auto;padding:0;background: transparent;font-size:16px;font-family: "宋体"}
			#print_div{width:100%;background: transparent;}
			td{word-break:keep-all}
			table{width:98%;}
			.title{margin-top:5px;margin-bottom: 5px;}
			.t_b{margin-bottom: 5px;}
			.title strong{font-size:26px;}
			.center{text-align:center;padding-left: 15px;padding-right: 15px;}
			.pf_title{margin-left:10%}
			.t_pf,.pf_title{font-size:13px;}
			.t_d{border:solid #000; border-width:1px 0px 0px 1px; margin-bottom: 5px;}
			.t_d th,.t_d td{border:solid #000; border-width:0px 1px 1px 0px; padding:2px 0px;}
			.warehouse{padding-right: 30px;}
		</style>
	</div>
</body>


</html>