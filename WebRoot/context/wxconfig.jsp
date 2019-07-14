	<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
		<c:if test="${!empty  signMap.appid}" >
			<script type="text/javascript">
			wx.config({
		    debug: false, // 开启调试模式,调用的所有api的返回值会在客户端alert出来，若要查看传入的参数，可以在pc端打开，参数信息会通过log打出，仅在pc端时才会打印。
		    appId: '${signMap.appid}', // 必填，公众号的唯一标识
		    timestamp:${signMap.timestamp}, // 必填，生成签名的时间戳
		    nonceStr: '${signMap.nonceStr}', // 必填，生成签名的随机串
		    signature: '${signMap.signature}',// 必填，签名，见附录1
		    jsApiList: ['scanQRCode'] // 必填，需要使用的JS接口列表，所有JS接口列表见附录2
				});
		</script>
		</c:if>
		
		<script type="text/javascript" >
			$(function(){
				<c:if test="${empty  signMap.appid}" >
				$(".icon-scan").click(function(){
					layer.msg("扫描功能只能在微信中使用");
					return false;
				});
				</c:if>
			});
		</script>