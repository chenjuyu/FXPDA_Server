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
		request.getRequestDispatcher("/common.do?print").forward(request, response);
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
    	var headstr = "<html><head></head><body>"; 
        var footstr = "</body>";    
        var printData = document.getElementById("print_div").innerHTML;  
        //var oldstr = document.body.innerHTML;    
        var data = headstr+printData+footstr; 
        LODOP.ADD_PRINT_HTM("8mm","6mm","100%","92%",data);
        LODOP.SET_PRINT_PAGESIZE(1, "9.5in", "5.5in","CreateCustomPage");
        LODOP.SET_PRINT_MODE("POS_BASEON_PAPER",1);
        LODOP.SET_PRINT_MODE("FULL_WIDTH_FOR_OVERFLOW",true);
        LODOP.SET_PRINT_MODE("NOCLEAR_AFTER_PRINT",true);//不清空打印内容	
		LODOP.SET_PREVIEW_WINDOW(0,1,0,0,0,"");//打印前弹出选择打印机的对话框	
		LODOP.SET_PRINT_MODE("AUTO_CLOSE_PREWINDOW",1);//打印后自动关闭预览窗口
		LODOP.PREVIEW();
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
    </object>  
<body>
	<div id="print_div">
		<c:forEach begin="0" end="${fn:length(datas.details)}" step="15"  varStatus="status">
		<c:if test="${status.first == true}">
		<!--标题-->
		<div class="title" align="center">
			<h2>${datas.shop}</h2>
			<strong>${datas.docType}</strong>
		</div>
		<div class="datas">
			<table class="t_s" align="center">
				<tr>
					<td>单号:&nbsp;&nbsp;${datas.no}</td>

					<td>类别:&nbsp;&nbsp;${datas.type}</td>

					<td>日期:&nbsp;&nbsp;<fmt:formatDate value="${datas.date}" pattern="yyyy-MM-dd" /></td>

					<td>
						<c:if test="${datas.tableName eq 'Purchase'}">厂商名称:</c:if>
						<c:if test="${datas.tableName ne 'Purchase'}">客户名称:</c:if>
					&nbsp;&nbsp;${datas.customer}
					</td>

					<td>发货部门:&nbsp;&nbsp;${datas.department}</td>
				</tr>
				<tr>
					<td>应收金额合计:&nbsp;&nbsp;<fmt:formatNumber type="number" value="${datas.amountSum}" pattern="0.00" maxFractionDigits="2"/></td>
					<td></td>
					<td></td>
					<td></td>
					<td align="right"><font><span>第${status.count}页</span>/<span>共${datas.page}页</span></font></td>
				</tr>
			</table>
		</div>
		</c:if>
		<c:if test="${status.first == false}">
			<div class="datas" style="page-break-before:avoid; margin-top: 20px;">
				<table class="t_s" align="center">
					<tr>
						<td></td>
						<td></td>
						<td></td>
						<td></td>
						<td align="right"><font><span>第${status.count}页</span>/<span>共${datas.page}页</span></font></td>
					</tr>
				</table>
			</div>
		</c:if>
		<div class="detail">
			<table class="t_d" border="1" cellspacing="0" cellpadding="0">
				<tr>
					<th>货品编码</th>
					<th>货品名称</th>
					<th>品牌</th>
					<th>颜色</th>
					<c:forEach begin="0" end="${fn:length(datas.sizeTitle)}" var="x" items="${datas.sizeTitle}">
						<th align="center" style="padding-left: 10px;padding-right: 10px;"> ${x.Size} </th>
					</c:forEach>
					<th>数量</th>
					<th>单价</th>
					<th>金额</th>
				</tr>
				<c:if test="${status.first == true}">
					<% i++; %>
					<c:forEach begin="${status.index}" end="${status.index+15}" items="${datas.details}" var="d">
					<tr>
						<td>${d.code}</td>
						<td>${d.name}</td>
						<td>${d.brand}</td>
						<td>${d.color}</td>
						<c:forEach begin="1" end="${datas.maxSize}" var="nc">
						<c:set var="xn" value="x_${nc}"/>
						<c:set var="xValue" value="${d[xn]}"/>
							<td class="right"> ${xValue} </td>
						</c:forEach>
						<td class="right">${d.quantity}</td>
						<td class="right"><fmt:formatNumber type="number" value="${d.price}" pattern="0.00" maxFractionDigits="2"/></td>
						<td class="right"><fmt:formatNumber type="number" value="${d.amount}" pattern="0.00" maxFractionDigits="2"/></td>
					</tr>
					</c:forEach>
				</c:if>
				<c:if test="${status.first == false && status.last == false}">
					<% i++; if(i > 2){j++;} request.setAttribute("j",j);%>
					<c:forEach begin="${status.index+j}" end="${status.index+15+j}" items="${datas.details}" var="d">
						<tr>
							<td>${d.code}</td>
							<td>${d.name}</td>
							<td>${d.brand}</td>
							<td>${d.color}</td>
							<c:forEach begin="1" end="${datas.maxSize}" var="nc">
							<c:set var="xn" value="x_${nc}"/>
							<c:set var="xValue" value="${d[xn]}"/>
							<td class="right"> ${xValue} </td>
							</c:forEach>
							<td class="right">${d.quantity}</td>
							<td class="right"><fmt:formatNumber type="number" value="${d.price}" pattern="0.00" maxFractionDigits="2"/></td>
							<td class="right"><fmt:formatNumber type="number" value="${d.amount}" pattern="0.00" maxFractionDigits="2"/></td>
						</tr>
					</c:forEach>
				</c:if>
				<c:if test="${status.last == true}">
					<% i++; if(i > 2){j = Integer.parseInt(String.valueOf(request.getAttribute("j"))); j++;} request.setAttribute("j",j);%>
					<c:if test="${(fn:length(datas.details)-1) > 15}">
						<c:forEach begin="${status.index+j}" end="${fn:length(datas.details)}" items="${datas.details}" var="d">
							<tr>
								<td>${d.code}</td>
								<td>${d.name}</td>
								<td>${d.brand}</td>
								<td>${d.color}</td>
								<c:forEach begin="1" end="${datas.maxSize}" var="nc">
								<c:set var="xn" value="x_${nc}"/>
								<c:set var="xValue" value="${d[xn]}"/>
								<td class="right"> ${xValue} </td>
								</c:forEach>
								<td class="right">${d.quantity}</td>
								<td class="right"><fmt:formatNumber type="number" value="${d.price}" pattern="0.00" maxFractionDigits="2"/></td>
								<td class="right"><fmt:formatNumber type="number" value="${d.amount}" pattern="0.00" maxFractionDigits="2"/></td>
							</tr>
						</c:forEach>
					</c:if>
					<tr>
						<td>合计</td>
						<td></td>
						<td></td>
						<td></td>
						<c:forEach begin="1" end="${datas.maxSize}" var="y">
						<td></td>
						</c:forEach>
						<td class="right">${datas.quantitySum}</td>
						<td></td>
						<td class="right"><fmt:formatNumber type="number" value="${datas.amountSum}" pattern="0.00" maxFractionDigits="2"/></td>
					</tr>
				</c:if>
			</table>
			<c:if test="${status.first == false && status.last == false}">
				<p style="margin-bottom: 80px;"></p>
			</c:if>
		</div>
		<c:if test="${status.last}">
			<div class="bottom" id="print_bottom">
				<table class="t_b" align="center">
					<tr>
						<td class="manager">主管:&nbsp;&nbsp;</td>
	
						<td>审核:&nbsp;&nbsp;${datas.audit}</td>
	
						<td>制表:&nbsp;&nbsp;${datas.userName}</td>
	
						<td>打印日期:&nbsp;&nbsp;<fmt:formatDate value="<%=new Date() %>" pattern="yyyy-MM-dd" /></td>
						
					</tr>
				</table>
			</div>
			<div class="page_foot" style="page-break-before:avoid;">
				<div class="pf_title">【伏羲软件】</div>
				<table class="t_pf">
					<tr>
						<td>地址:&nbsp;&nbsp;${datas.address}</td>
					</tr>
					<tr>
						<td>座机/Phone:&nbsp;&nbsp;${datas.phone}
							&nbsp;&nbsp;&nbsp;&nbsp;手机/Mobile:&nbsp;&nbsp;${datas.mobile}</td>
					</tr>
					<tr>
						<td>${datas.bankTypeOne}:&nbsp;&nbsp;${datas.bankOneNo}
							&nbsp;&nbsp;&nbsp;&nbsp;户名:&nbsp;&nbsp;${datas.bankCardOneName}</td>
					</tr>
					<tr>
						<td>${datas.bankTypeTwo}:&nbsp;&nbsp;${datas.bankTwoNo}
							&nbsp;&nbsp;&nbsp;&nbsp;户名:&nbsp;&nbsp;${datas.bankCardTwoName}</td>
					</tr>
				</table>
			</div>
		</c:if>
		</c:forEach>
			<style type="text/css">
				*{margin:0 auto;padding:0;background: transparent;font-size:14px;}
				#print_div{width:100%;background: transparent;}
				td{word-break:keep-all}
				table{width:98%;}
				.manager{padding-right:20px;}
				.context{text-align:left;}
				.title{margin-top:0px;margin-bottom: 5px;}
				.t_b{margin-bottom: 5px;}
				.title strong{font-size:15px;}
				.right{text-align:right;padding-left: 15px;padding-right: 15px;}
				.page_foot{margin-left:0px;font-size:13px;}
				.pf_title{margin-left:10%}
				.t_pf,.pf_title{font-size:13px;}
				.t_d{border:solid #000; border-width:1px 0px 0px 1px; margin-bottom: 5px;}
				.t_d th,.t_d td{border:solid #000; border-width:0px 1px 1px 0px; padding:2px 0px;}
				.bottom{margin-top: -5px;}
			</style>
	</div>
</body>


</html>