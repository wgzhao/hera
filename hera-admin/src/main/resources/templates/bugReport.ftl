<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/html" xmlns="http://www.w3.org/1999/html">
<head>
    <title>任务调度中心</title>
    <#import "/common/common.macro.ftl" as netCommon>
    <@netCommon.commonStyle />
    <link rel="stylesheet" href="${request.contextPath}/plugins/layui/css/layui.css">
    <link rel="stylesheet" href="${request.contextPath}/css/bugReport.css">
</head>


<body class="hold-transition skin-black sidebar-mini">
<div class="wrapper" style="height: 100%;">
    <!-- header -->
    <@netCommon.commonHeader />
    <!-- left -->
    <@netCommon.commonLeft "adviceController" />

    <div class="content-wrapper">
        <!-- 留言板 -->
        <div class="content">
            <div>
                <textarea id="input" type="text" placeholder="随便说说吧...按回车发布"></textarea>
                <div id="colorPicker"/>
            </div>
            <div id="container">
                <#list allMsg as msg>
                    <div class="item" style="background: ${msg.color}">
                        <h2> ${msg.createTime}</h2>
                        <p style="width:90%;height:90%;font-family: microsoft yahei"> ${msg.msg}</p>
                        <h2 class="pull-right"> 来自: ${msg.address} </h2>
                    </div>
                </#list>

            </div>
        </div>
    </div>
</div>

<@netCommon.commonScript />
<script src="${request.contextPath}/plugins/layui/layui.js"></script>
<script src="http://pv.sohu.com/cityjson?ie=utf-8"></script>
<script src="${request.contextPath}/js/bugReport.js"></script>
</body>

</html>


