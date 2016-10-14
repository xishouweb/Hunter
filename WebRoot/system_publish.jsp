<%@page import="java.util.concurrent.Executors"%>
<%@page import="java.util.concurrent.ScheduledExecutorService"%>
<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<%@page import="net.navagraha.hunter.tool.JoinPushTool"%>
<%@page import="javax.websocket.Session"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%
	String path = request.getContextPath();
	String basePath = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort();
	Map<String,Session> map=JoinPushTool.getConnections();
%>

<!DOCTYPE HTML>
<html>
<head>
<meta charset="utf-8">
<meta name="renderer" content="webkit|ie-comp|ie-stand">
<meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1">
<meta name="viewport"
	content="width=device-width,initial-scale=1,minimum-scale=1.0,maximum-scale=1.0,user-scalable=no" />
<meta http-equiv="Cache-Control" content="no-siteapp" />
<LINK rel="Bookmark" href="/favicon.ico">
<LINK rel="Shortcut Icon" href="/favicon.ico" />
<!--[if lt IE 9]>
<script type="text/javascript" src="lib/html5.js"></script>
<script type="text/javascript" src="lib/respond.min.js"></script>
<script type="text/javascript" src="lib/PIE_IE678.js"></script>
<![endif]-->
<link rel="stylesheet" type="text/css"
	href="<%=basePath%>/MyAlipay_MD5/css/H-ui.min.css" />
<link rel="stylesheet" type="text/css"
	href="<%=basePath%>/MyAlipay_MD5/css/H-ui.admin.css" />
<link rel="stylesheet" type="text/css"
	href="<%=basePath%>/MyAlipay_MD5/css/skin.css" id="skin" />
<link rel="stylesheet" type="text/css"
	href="<%=basePath%>/MyAlipay_MD5/css/style.css" />
<link
	href="<%=basePath%>/MyAlipay_MD5/lib/Hui-iconfont/1.0.1/iconfont.css"
	rel="stylesheet" type="text/css" />
<link rel="stylesheet" type="text/css"
	href="<%=basePath%>/MyAlipay_MD5/lib/icheck/icheck.css" />
<!--[if IE 6]>
<script type="text/javascript" src="http://lib.h-ui.net/DD_belatedPNG_0.0.8a-min.js" ></script>
<script>DD_belatedPNG.fix('*');</script>
<![endif]-->
<!--/meta 作为公共模版分离出去-->
<title>消息推送</title>
</head>
<body>
	<nav class="breadcrumb">
		<i class="Hui-iconfont">&#xe67f;</i> 首页 <span class="c-gray en">&gt;</span>
		系统管理 <span class="c-gray en">&gt;</span> 消息推送 <a
			class="btn btn-success radius r mr-20"
			style="line-height:1.6em;margin-top:3px"
			href="javascript:location.replace(location.href);" title="刷新"><i
			class="Hui-iconfont">&#xe68f;</i></a>
	</nav>
	<br />
	<div class="page-container">
		<form class="form form-horizontal" id="form-article-add">
			<div id="tab-system" class="HuiTab">

				<div class="tabBar cl">
					<span onclick="change01()">局部推送</span><span onclick="change02()">全局推送</span>
				</div>

				<br />

				<div class="cl pd-5 bg-1 bk-gray mt-20"
					style="margin-top: 2px;margin-left: 1%;margin-right: 1%">
					<span class="l"><span class="btn btn-success radius"
						id="month"><i class="Hui-iconfont">&#xe6e0;</i> 结果图表</span>&nbsp;&nbsp;&nbsp;&nbsp;
						<button class="btn btn-primary radius r mr-20" type="button"
							onclick="message_start()">
							<i class="Hui-iconfont">&#xe603;</i> &nbsp;推送
						</button> </span>
				</div>

				<br />

				<div class="tabCon">
					<div class="mt-10" style="margin-left: 1.5%;margin-right: 1.5%;">
						<table
							class="table table-border table-bordered table-bg table-hover table-sort">
							<thead>
								<tr class="text-c">
									<th width="25"><input type="checkbox" name="" value=""></th>
									<th width="50">会话ID</th>
									<th width="75">手机号</th>
									<th width="75">用户类型</th>
									<th width="75">连接状态</th>
									<th width="75">是否可信任</th>
									<th width="80">最大可发送字节</th>
									<th width="80">最大可发送字符</th>
									<th width="80">WebSocket版本</th>
								</tr>
							</thead>
							<tbody id="tbody">
								<%
									for(String key:map.keySet()){
								%>
								<tr class="text-c">
									<td>
										<%
											if(key.equals("01010000000"))
																																																																																																																		  out.print("<input type='checkbox' value='' name='' disable='true'>");
																																																																																																											else
																																																																																																																		  out.print("<input type='checkbox' value='' name='' >");
										%>
									</td>
									<td><%=map.get(key).getId()%></td>
									<td><%=key%></td>
									<%
										if(key.equals("01010000000"))
																																																																																																																																																						out.print("<td>管理员</td>");
																																																																																																																																																					else
																																																																																																																																																						out.print("<td>普通用户</td>");
																																																																																																																																																					if(map.get(key).isOpen()==true)
																																																																																																																																																						out.print("<td class='td-status'><span class='label label-success radius'>已连接</span></td>");
																																																																																																																																																					else
																																																																																																																																																						out.print("<td class='td-status'><span class='label label-false radius'>未连接</span></td>");
																																																																																																																																																					if(map.get(key).isSecure()==true)
																																																																																																																																																						out.print("<td class='td-status'><span class='label label-success radius'>可信任</span></td>");
																																																																																																																																																					else
																																																																																																																																																						out.print("<td class='td-status'><span class='label label-warning radius'>不可信任</span></td>");
									%>
									<td><%=map.get(key).getMaxBinaryMessageBufferSize()/8%></td>
									<td><%=map.get(key).getMaxTextMessageBufferSize()/8%>（100个汉字）</td>
									<td>WS <%=map.get(key).getProtocolVersion()%></td>
								</tr>
								<%
									}
								%>
							</tbody>
						</table>
					</div>
				</div>

				<div class="tabCon">
					<div class="row cl">
						<label class="form-label col-xs-4 col-sm-2">已推送：</label>
						<div class="formControls col-xs-8 col-sm-6">
							<div class="textarea" name="console" id="console"
								style="height: 320px;"></div>
						</div>
					</div>
				</div>
		</form>
	</div>

	<!--_footer 作为公共模版分离出去-->
	<script type="text/javascript"
		src="<%=basePath%>/MyAlipay_MD5/lib/jquery/1.9.1/jquery.min.js"></script>
	<script type="text/javascript"
		src="<%=basePath%>/MyAlipay_MD5/lib/layer/1.9.3/layer.js"></script>
	<script type="text/javascript"
		src="<%=basePath%>/MyAlipay_MD5/lib/layer/1.9.3/extend/layer.ext.js"></script>
	<script type="text/javascript"
		src="<%=basePath%>/MyAlipay_MD5/lib/icheck/jquery.icheck.min.js"></script>
	<script type="text/javascript"
		src="<%=basePath%>/MyAlipay_MD5/lib/jquery.validation/1.14.0/jquery.validate.min.js"></script>
	<script type="text/javascript"
		src="<%=basePath%>/MyAlipay_MD5/lib/jquery.validation/1.14.0/validate-methods.js"></script>
	<script type="text/javascript"
		src="<%=basePath%>/MyAlipay_MD5/lib/jquery.validation/1.14.0/messages_zh.min.js"></script>
	<script type="text/javascript"
		src="<%=basePath%>/MyAlipay_MD5/js/H-ui.js"></script>
	<script type="text/javascript"
		src="<%=basePath%>/MyAlipay_MD5/js/H-ui.admin.js"></script>
	<script type="text/javascript"
		src="<%=basePath%>/MyAlipay_MD5/lib/datatables/1.10.0/jquery.dataTables.min.js"></script>
	<!--/_footer /作为公共模版分离出去-->

	<!--请在下方写此页面业务相关的脚本-->
	<script type="text/javascript">
		/*消息-推送*/
		var local_message;
		var error_message;

		function message_start() {

			layer.prompt({
				title : '请输入局部推送内容:',
				formType : 2
			}, function(text, index) {
				local_message = text;

				if (local_message != null) {
					layer.confirm('			确认要推送此消息吗？', {
						btn : [ '确认', '取消' ]
					}, function(index) {
						if (Chat.sendMessage())
							layer.msg('已推送!', {
								icon : 6,
								time : 1000
							});
						else {
							layer.msg(error_message, {
								icon : 5,
								time : 1000
							});
						}
					});
				}
				layer.close(index);
			});
		}

		var flag = 1;//默认局部
		/*局部推送*/
		function change01() {
			flag = 1;
		}

		function change02() {
			flag = 2;//全局
		}

		/*websocekt:Chat.initialize();*/

		var Chat = {};

		Chat.socket = null;

		Chat.connect = (function(host) {
			if ('WebSocket' in window) {
				Chat.socket = new WebSocket(host);
			} else if ('MozWebSocket' in window) {
				Chat.socket = new MozWebSocket(host);
			} else {
				Console.log('Error: 当前浏览器不支持消息推送！');
				return;
			}

			Chat.socket.onopen = function() {
				Console.log('Info: 已连接到推送服务器！');
			};

			Chat.socket.onclose = function() {
				Console.log('Info: 已断开推送服务器！');
			};

			Chat.socket.onmessage = function(message) {
				Console.log('Info: ' + message.data.substring(3));//截取为了去掉后台添加的标记“sys”
			};
		});

		Chat.initialize = function() {
			if (window.location.protocol == 'http:') {
				Chat
						.connect('ws://192.168.0.1:8080/Hunter/websocket/01010000000/x.x.x');
			} else {
				Chat
						.connect('wws://192.168.0.1:8080/Hunter/websocket/01010000000/x.x.x');//www.7hunter.cn
			}
		};

		Chat.sendMessage = (function() {
			if (flag == 1) {//局部推送
				var tbody = document.getElementById("tbody");
				if (tbody.rows.length < 1) {
					error_message = "ERROR,当前无用户可进行推送";
					return false;
				}
				var row;
				var cell;
				var chk;
				var num = 0;
				var detail = "";
				//倒着迭代可以少做一些处理
				for (var i = tbody.rows.length - 1; i > -1; i--) {
					row = tbody.rows[i];//迭代当前行
					cell = row.cells[0];//复选框所在的单元格
					chk = cell.getElementsByTagName("input")[0];//获取复选框
					if (chk != undefined) {
						if (chk.checked) {//如果选中 

							if (detail == "")
								detail += row.cells[2].innerHTML;
							else
								detail += "/" + row.cells[2].innerHTML;
							num++;
						}
					} else {
						error_message = "ERROR,当前无用户可选择";
						return false;
					}
				}
				if (num <= 0) {
					error_message = "ERROR,当前选择用户为空，无法推送";
					return false;
				}
				detail += "/" + local_message;
				Chat.socket.send(detail);
				return true;
			} else {//全局推送
				Chat.socket.send(local_message);
				return true;
			}
		});

		var Console = {};

		Console.log = (function(message) {
			var console = document.getElementById('console');
			if (console.childNodes.length >= 10)//多余10个移除第一个
				console.removeChild(console.firstChild);
			var p = document.createElement('p');
			p.style.wordWrap = 'break-word';
			p.innerHTML = message;
			console.appendChild(p);
			while (console.childNodes.length > 25) {
				console.removeChild(console.firstChild);
			}
			console.scrollTop = console.scrollHeight;
		});

		Chat.initialize();
		/* 请在上方写此页面业务相关的脚本*/

		/* 请在上方写此页面业务相关的脚本*/

		$('.table-sort').dataTable({
			"aaSorting" : [ [ 1, "asc" ] ],//默认第几个排序
			"bStateSave" : true,//状态保存
			"aoColumnDefs" : [
			//{"bVisible": false, "aTargets": [ 3 ]} //控制列的隐藏显示
			{
				"orderable" : false,
				"aTargets" : [ 4, 5, 8 ]
			} // 制定列不参与排序
			]
		});

		function accAdd(arg1, arg2) {
			var r1, r2, m;
			try {
				r1 = arg1.toString().split(".")[1].length;
			} catch (e) {
				r1 = 0;
			}
			try {
				r2 = arg2.toString().split(".")[1].length;
			} catch (e) {
				r2 = 0;
			}
			m = Math.pow(10, Math.max(r1, r2));
			return (arg1 * m + arg2 * m) / m;
		}

		$(function() {
			$('.skin-minimal input').iCheck({
				checkboxClass : 'icheckbox-blue',
				radioClass : 'iradio-blue',
				increaseArea : '20%'
			});
			$.Huitab("#tab-system .tabBar span", "#tab-system .tabCon",
					"current", "click", "0");
		});
	</script>
</body>
</html>