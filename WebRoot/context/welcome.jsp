<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<%
String path = request.getContextPath();
String basePath = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+path+"/";
%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
  <head>
  	<base href="<%=basePath%>">
    <title>PDA服务端欢迎界面</title>
	<meta http-equiv="pragma" content="no-cache">
	<meta http-equiv="cache-control" content="no-cache">
	<meta http-equiv="expires" content="0">    
	<meta http-equiv="keywords" content="keyword1,keyword2,keyword3">
	<meta http-equiv="description" content="This is my page">
	<!-- css样式 -->
	<style type="text/css">
		/* 背景效果 */
		*{margin:0 auto; padding:0;}
		@media all and (-webkit-min-device-pixel-ratio:0) and (min-resolution: .001dpcm) { 
		    .head{
		        background-image: -webkit-linear-gradient(left, #fe5d4b, #E6D205 25%, #fe5d4b 50%, #E6D205 75%, #fe5d4b);
		        -webkit-text-fill-color: transparent;
		        -webkit-background-clip: text;
		        -webkit-background-size: 200% 100%;
		        -webkit-animation: masked-animation 4s infinite linear;
		    }
		}
		@-webkit-keyframes masked-animation {
		    0%  { background-position: 0 0;}
		    100% { background-position: -100% 0;}
		}
		.slideshow {position: absolute;width: 100vw;height: 100vh;overflow: hidden;}
		.slideshow-image {position: absolute;width: 100%;height: 100%;background: no-repeat 50% 50%;background-size: cover;-webkit-animation-name: kenburns;animation-name: kenburns;-webkit-animation-timing-function: linear;animation-timing-function: linear;-webkit-animation-iteration-count: infinite;animation-iteration-count: infinite;-webkit-animation-duration: 16s;animation-duration: 16s;opacity: 1;-webkit-transform: scale(1.2);transform: scale(1.2);}
		.slideshow-image:nth-child(1) {-webkit-animation-name: kenburns-1;animation-name: kenburns-1;z-index: 3;}
		.slideshow-image:nth-child(2) {-webkit-animation-name: kenburns-2;animation-name: kenburns-2;z-index: 2;}
		.slideshow-image:nth-child(3) {-webkit-animation-name: kenburns-3;animation-name: kenburns-3;z-index: 1;}
		.slideshow-image:nth-child(4) {-webkit-animation-name: kenburns-4;animation-name: kenburns-4;z-index: 0;}
		@-webkit-keyframes kenburns-1 {
		  0% {opacity: 1;-webkit-transform: scale(1.2);transform: scale(1.2);}
		  1.5625% {opacity: 1;}
		  23.4375% {opacity: 1;}
		  26.5625% {opacity: 0;-webkit-transform: scale(1);transform: scale(1);}
		  100% {opacity: 0;-webkit-transform: scale(1.2);transform: scale(1.2);}
		  98.4375% {opacity: 0;-webkit-transform: scale(1.21176);transform: scale(1.21176);}
		  100% {opacity: 1;}
		}
		@keyframes kenburns-1 {
		  0% {opacity: 1;-webkit-transform: scale(1.2);transform: scale(1.2);}
		  1.5625% {opacity: 1;}
		  23.4375% {opacity: 1;}
		  26.5625% {opacity: 0;-webkit-transform: scale(1);transform: scale(1);}
		  100% {opacity: 0;-webkit-transform: scale(1.2);transform: scale(1.2);}
		  98.4375% {opacity: 0;-webkit-transform: scale(1.21176);transform: scale(1.21176);}
		  100% {opacity: 1;}
		}
		@-webkit-keyframes kenburns-2 {
		  23.4375% {opacity: 1;-webkit-transform: scale(1.2);transform: scale(1.2);}
		  26.5625% {opacity: 1;}
		  48.4375% {opacity: 1;}
		  51.5625% {opacity: 0;-webkit-transform: scale(1);transform: scale(1);}
		  100% {opacity: 0;-webkit-transform: scale(1.2); transform: scale(1.2);}
		}
		@keyframes kenburns-2 {
		  23.4375% {opacity: 1;-webkit-transform: scale(1.2);transform: scale(1.2);}
		  26.5625% {opacity: 1;}
		  48.4375% {opacity: 1;}
		  51.5625% {opacity: 0;-webkit-transform: scale(1);transform: scale(1);}
		  100% {opacity: 0;-webkit-transform: scale(1.2); transform: scale(1.2);
		  }
		}
		@-webkit-keyframes kenburns-3 {
		  48.4375% {opacity: 1;-webkit-transform: scale(1.2); transform: scale(1.2);}
		  51.5625% {opacity: 1;}
		  73.4375% {opacity: 1;}
		  76.5625% {opacity: 0;-webkit-transform: scale(1); transform: scale(1);}
		  100% {opacity: 0;-webkit-transform: scale(1.2); transform: scale(1.2);}
		}
		@keyframes kenburns-3 {
		  48.4375% {opacity: 1;-webkit-transform: scale(1.2);transform: scale(1.2);}
		  51.5625% {opacity: 1;}
		  73.4375% {opacity: 1;}
		  76.5625% {opacity: 0;-webkit-transform: scale(1);transform: scale(1);}
		  100% {opacity: 0;-webkit-transform: scale(1.2); transform: scale(1.2);}
		}
		@-webkit-keyframes kenburns-4 {
		  73.4375% {opacity: 1;-webkit-transform: scale(1.2);transform: scale(1.2);}
		  76.5625% {opacity: 1;}
		  98.4375% {opacity: 1;}
		  100% {opacity: 0;-webkit-transform: scale(1); transform: scale(1);}
		}
		@keyframes kenburns-4 {
		  73.4375% {opacity: 1;-webkit-transform: scale(1.2);transform: scale(1.2);}
		  76.5625% {opacity: 1;}
		  98.4375% {opacity: 1;}
		  100% {opacity: 0;-webkit-transform: scale(1); transform: scale(1);}
		}
		
		/* div样式 */
		a{text-decoration: none;}
		.head{height: 128px;width: 100%; color:#002685; font:normal bold 88px "楷体"; line-height:88px; text-align:center;position:absolute; margin:120 auto; z-index: 10;}
		.head:HOVER{cursor: default;}
		.content{ overflow:hidden; width:700px; background-position: 50% 50%; background-repeat:no-repeat;float:left;position:absolute;z-index: 10; right:342px;top:224px;}
		.content .tea{height:120px;width:480px; background:#E8473E; margin-top:60px;  font:normal bold 60px "楷体"; line-height:120px; text-align:center;border-radius:8px 60px;}
		.content .stu{height:120px;width:480px; background:#4CB848; margin-top:30px; font:normal bold 60px "楷体"; line-height:120px; text-align:center;border-radius:8px 60px;}
		.content .tea a{height:120px;width:480px;display:inline-block;color:white;}
		.content .stu a{height:120px;width:480px;display:inline-block;color:#531A8B;}
	</style>

  </head>
  <body>
    <div class="head">欢迎使用伏羲智能终端</div>
    <!-- 轮换背景 -->
    <div class="slideshow">
	<div class="slideshow-image" style="background-image: url('images/1.jpg')"></div>
	<div class="slideshow-image" style="background-image: url('images/2.jpg')"></div>
	<div class="slideshow-image" style="background-image: url('images/3.jpg')"></div>
	<div class="slideshow-image" style="background-image: url('images/4.jpg')"></div>
	</div>
  </body>
</html>
