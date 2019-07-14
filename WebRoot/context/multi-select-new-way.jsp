<%@page import="java.util.Date"%>
<%@page import="java.util.Map"%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jstl/fmt_rt" prefix="fmt"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>

<%
    String path = request.getContextPath();
			String basePath = request.getScheme() + "://"
					+ request.getServerName() + ":" + request.getServerPort()
					+ path + "/";
%>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<base href="<%=basePath%>">
<title>多颜色尺码录入</title>
<meta name="viewport" content="initial-scale=1.0, maximum-scale=1.0, minimum-scale=1.0, user-scalable=no, width=device-width">
<meta name="apple-mobile-web-app-capable" content="yes">
<meta name="apple-touch-fullscreen" content="yes">
<meta name="apple-mobile-web-app-status-bar-style" content="black">
<meta name="format-detection" content="telephone=no">
<meta name="format-detection" content="address=no">
<!-- CSS -->
<style>
* {
	margin: 0px;
	padding: 0px;
	font-size: 10vw;
}

.content {
	width: 100%;
	overflow: scroll;
}

.main {
	width: 100%;
	height: 50px;
	overflow: hidden;
	position: fixed;
}

.main-top {
	width: 100%;
	overflow: hidden;
	background: white;
	clear: both;
	padding: 10px 5px;
}

.main-top-left {
	width: 30%;
	float: left;
	text-indent: 0.2em;
}

.main-top-rigth {
	width: 45%;
	float: right;
	align-items:center; display: -webkit-flex;
}

.sku table {
	width: 100%;
	margin-top: 50px;
	text-align: center;
}

.sku td {
	width: 500px;
	padding: 3px 0;
}

.sku th{
	padding: 0 3px;
}

.sku .color-column{
	padding: 0;
}

.sku table,.sku td,.sku th {
	border-collapse: collapse;
	border: 1px solid #E3E3E3;
}

.sku table input {
	width: 50%;
	border: none;
	text-align: center;
	outline: none;
	float: left;
}

.sku table .stock {
	width: 50%;
	text-align: center;
	float: right;
	background: #F0F0F4
}

/*按钮样式*/
.mui-switch {
	width: 52px;
	height: 31px;
	position: relative;
	border: 1px solid #dfdfdf;
	background-color: #fdfdfd;
	box-shadow: #dfdfdf 0 0 0 0 inset;
	border-radius: 20px;
	border-top-left-radius: 20px;
	border-top-right-radius: 20px;
	border-bottom-left-radius: 20px;
	border-bottom-right-radius: 20px;
	background-clip: content-box;
	display: inline-block;
	-webkit-appearance: none;
	user-select: none;
	outline: none;
}

.mui-switch:before {
	content: '';
	width: 29px;
	height: 29px;
	position: absolute;
	top: 0px;
	left: 0;
	border-radius: 20px;
	border-top-left-radius: 20px;
	border-top-right-radius: 20px;
	border-bottom-left-radius: 20px;
	border-bottom-right-radius: 20px;
	background-color: #fff;
	box-shadow: 0 1px 3px rgba(0, 0, 0, 0.4);
}

.mui-switch:checked {
	border-color: #F19415;
	box-shadow: #F19415 0 0 0 16px inset;
	background-color: #F19415;
}

.mui-switch:checked:before {
	left: 21px;
}

.mui-switch.mui-switch-animbg {
	transition: background-color ease 0.4s;
}

.mui-switch.mui-switch-animbg:before {
	transition: left 0.3s;
}

.mui-switch.mui-switch-animbg:checked {
	box-shadow: #dfdfdf 0 0 0 0 inset;
	background-color: #F19415;
	transition: border-color 0.4s, background-color ease 0.4s;
}

.mui-switch.mui-switch-animbg:checked:before {
	transition: left 0.3s;
}
</style>
<!-- JS -->
<script type="text/javascript" src="${pageContext.request.contextPath}/plug-in/jquery/jquery-1.8.3.min.js"></script>
<script type="text/javascript">
	//界面加载完毕后的操作
	$(function() {
		
		var colorListLength = $("#colorListLength").val();
		if(colorListLength < 1){
			fail(-1);
		}
		var sizeListLength = $("#sizeListLength").val();
		if(sizeListLength < 1){
			fail(-2);
		}
		
		var flag = $("#show-stock").prop("checked");
		if (flag) {
			$(".stock").css("display", "block");
			$(".quantity").css("width", "50%");
		} else {
			$(".stock").css("display", "none");
			$(".quantity").css("width", "100%");
		}

		//事件
		$("#show-stock").click(function() {
			var flag = $("#show-stock").prop("checked");
			if (flag) {
				$(".stock").css("display", "block");
				$(".quantity").css("width", "50%");
			} else {
				$(".stock").css("display", "none");
				$(".quantity").css("width", "100%");
			}
		});
		

		$(".quantity").on('input propertychange', function() {
			var quantitySum = 0;
			var objs = $(".quantity");
			for ( var i = 0; i < objs.length; i++) {
				var count = objs[i].value;
				if(count != null && count != undefined && count != ''){
					quantitySum += parseInt(count);
				}
			}
			$("#quantitySum").text(quantitySum);
		});

	});
	
	//保存数据
	function save(){
		var objs = $(".quantity");
		var result = "";
		for ( var i = 0; i < objs.length; i++) {
			var quantity = objs[i].value;
			if(quantity != null && quantity != undefined && quantity != ''){
				var name = objs[i].name;
				name = name + "_" + quantity;
				if(i == objs.length-1){
					result += name;
	        	}else{
	        		result += name + ",";
	        	}
			}
		}
		
		alert("查看值输出"+result)
		window.androidShare.jsMethod(result);
	}
	
	function fail(result){
		window.androidShare.jsMethod(result);
	}
	
</script>
</head>
<body>
	<div class="content">
		<div class="main">
			<div class="main-top">
				<div class="main-top-left">
					数量: <span style="color: red;" id="quantitySum">0</span>
				</div>
				<div class="main-top-rigth">
					显示存库&nbsp;&nbsp;&nbsp;&nbsp;<label><input class="mui-switch mui-switch-animbg" id="show-stock" type="checkbox"></label>
				</div>
			</div>
		</div>
		<div class="sku">
			<table>
				<tr class="color-column">
					<th></th>
					<c:forEach items="${colorList}" var="c">
						<th>${c.Color}</th>
					</c:forEach>
				</tr>
				<c:forEach items="${sizeList}" var="s">
					<tr>
						<th>${s.Size}</th>
						<c:forEach items="${s.colors}" var="t">
							<td>
								<input name="${s.SizeID}_${s.No}_${s.Size}_${t.ColorID}_${t.No}_${t.Color}" class="quantity" oninput = "value=value.replace(/[^\d]/g,'')">
								<div class="stock">${t.stock}</div>
							</td>
						</c:forEach>
					</tr>
				</c:forEach>
			</table>
		</div>
		<input name="colorListLength" id="colorListLength" type="hidden" value="${fn:length(colorList)}">
		<input name="sizeListLength" id="sizeListLength" type="hidden" value="${fn:length(sizeList)}">
	</div>
</body>
</html>