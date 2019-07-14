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
		request.getRequestDispatcher("/common.do?packingBoxDetailPrintHasPrice").forward(request, response);
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
	<!-- 打印的内容 -->
	<div id="print_div">
		<!--标题-->
		<div class="title" align="center">
			<strong>${datas.docType}</strong>
		</div>
		<div class="datas">
			<table class="t_s" align="center">
				<tr>
					<td width="33.3%">单号:&nbsp;${datas.no}</td>

					<td width="33.3%">客户:&nbsp;${datas.customer}</td>
					
					<td width="33.3%">箱号:&nbsp;${datas.boxNo}</td>

				</tr>
			</table>
		</div>
		<div class="detail">
			<table class="t_d" border="1" cellspacing="0" cellpadding="0">
				<tr>
					<th>箱号</th>
					<th>型号</th>
					<th>商品名称</th>
					<th>数量</th>
					<th>零售价</th>
				</tr>
				<c:forEach begin="0" end="${fn:length(datas.details)}" items="${datas.details}" var="d">
					<tr>
						<td class="center">${d.boxNo}</td>
						<td class="center">${d.goodsCode}</td>
						<td class="center">${d.goodsName}</td>
						<td class="center">${d.quantity}</td>
						<td class="right"><span class="_span"><fmt:formatNumber type="number" value="${d.retailSales}" pattern="0" maxFractionDigits="0"/></span></td>
					</tr>
				</c:forEach>
					<tr>
						<td class="center">合计/${fn:length(datas.details)}</td>
						<td></td>
						<td></td>
						<td class="center">${datas.quantitySum}</td>
						<td></td>
					</tr>
			</table>
		</div>
		<div class="bottom" id="print_bottom">
			<table class="t_b" align="center">
				<tr>
					<td>装箱人:&nbsp;</td>
					
					<td class="center">仓库主管:&nbsp;</td>

					<td align="right">
						<c:if test="${datas.tableName eq 'Sales'}">销售单号:</c:if>
						<c:if test="${datas.tableName ne 'Sales'}">出仓单号:</c:if>
						&nbsp;${datas.relationNo}
					</td>
					
				</tr>
				<tr>
					<td colspan="3">备注:&nbsp;${datas.memo}</td>
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
			.title strong{font-size:26px; margin-top: 0px;}
			.right{text-align:right;padding-left: 15px;padding-right: 15px;}
			.center{text-align: center;}
			._span{margin-right: 8px;}
			.pf_title{margin-left:10%}
			.t_pf,.pf_title{font-size:13px;}
			.t_d{border:solid #000; border-width:1px 0px 0px 1px; margin-bottom: 5px;}
			.t_d th,.t_d td{border:solid #000; border-width:0px 1px 1px 0px; padding:2px 0px;}
		</style>
	</div>
</body>


</html>