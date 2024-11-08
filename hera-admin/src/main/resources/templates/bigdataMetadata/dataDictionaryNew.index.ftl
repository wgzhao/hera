<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/html" xmlns="http://www.w3.org/1999/html" xmlns="http://www.w3.org/1999/html"
      xmlns="http://www.w3.org/1999/html">
<head>
    <title>任务调度平台</title>
    <#import "/common/common.macro.ftl" as netCommon>
    <@netCommon.commonStyle />
    <link href="${request.contextPath}/plugins/ztree/css/metroStyle/metroStyle.css" rel="stylesheet">
    <link href="${request.contextPath}/plugins/codemirror/lib/codemirror.css" rel="stylesheet">
    <link href="${request.contextPath}/plugins/codemirror/addon/hint/show-hint.css" rel="stylesheet">
    <link href="${request.contextPath}/plugins/codemirror/theme/eclipse.css" rel="stylesheet">
    <link href="${request.contextPath}/plugins/codemirror/theme/lucario.css" rel="stylesheet">
    <link href="${request.contextPath}/plugins/codemirror/theme/3024-day.css" rel="stylesheet">
    <link href="${request.contextPath}/plugins/codemirror/theme/3024-night.css" rel="stylesheet">
    <link href="${request.contextPath}/plugins/codemirror/theme/ambiance.css" rel="stylesheet">
    <link href="${request.contextPath}/plugins/codemirror/theme/base16-dark.css" rel="stylesheet">
    <link href="${request.contextPath}/plugins/codemirror/theme/base16-light.css" rel="stylesheet">
    <link href="${request.contextPath}/plugins/codemirror/theme/bespin.css" rel="stylesheet">
    <link href="${request.contextPath}/plugins/codemirror/theme/blackboard.css" rel="stylesheet">
    <link href="${request.contextPath}/plugins/codemirror/theme/colorforth.css" rel="stylesheet">
    <link href="${request.contextPath}/plugins/codemirror/theme/dracula.css" rel="stylesheet">
    <link href="${request.contextPath}/plugins/codemirror/theme/duotone-dark.css" rel="stylesheet">
    <link href="${request.contextPath}/plugins/codemirror/theme/duotone-light.css" rel="stylesheet">
    <link href="${request.contextPath}/plugins/codemirror/theme/erlang-dark.css" rel="stylesheet">
    <link href="${request.contextPath}/plugins/codemirror/theme/gruvbox-dark.css" rel="stylesheet">
    <link href="${request.contextPath}/plugins/codemirror/theme/mbo.css" rel="stylesheet">
    <link href="${request.contextPath}/plugins/codemirror/theme/material.css" rel="stylesheet">
    <link href="${request.contextPath}/plugins/codemirror/theme/solarized.css" rel="stylesheet">
    <link href="${request.contextPath}/plugins/codemirror/theme/base16-light.css" rel="stylesheet">
    <link href="${request.contextPath}/adminlte/plugins/bootstrap-fileinput/fileinput.min.css" rel="stylesheet">
    <link href="${request.contextPath}/adminlte/plugins/bootstrap-table/bootstrap-table.min.css" rel="stylesheet">
    <link rel="stylesheet" href="${request.contextPath}/plugins/bootstrap-select/bootstrap-select.min.css">
    <link rel="stylesheet" href="${request.contextPath}/css/scheduleCenter.css">
    <style>
        body .layer_bg .layui-layer-content {
            background-color: #eaf3fd;
        }

        /*-----------table换行*/
        .layui-table-cell {
            font-size: 14px;
            padding: 0 5px;
            height: auto;
            overflow: visible;
            text-overflow: inherit;
            white-space: normal;
            word-break: break-all;
        }
    </style>

    <#--自己添加-->
    <#--<link href="${request.contextPath}/css/developCenter.css" rel="stylesheet">-->
</head>


<body class="hold-transition skin-black sidebar-mini">
<div class="wrapper">
    <!-- header -->
    <@netCommon.commonHeader />
    <!-- left -->
    <@netCommon.commonLeft "developCenter" />

    <div class="content-wrapper">

        <section class="content">
            <div class="row">
                <div class="col-md-3 col-sm-3 col-lg-3 colStyle" style="border: none" id="treeCon">
                    <div class="height-self left-bar" style="overflow: hidden;">
                        <div class="box-header left-bar-head">
                            <ul class="nav nav-tabs" role="tablist">
                                <li role="presentation" class="" style="background-color: #fff"><a href="#" role="tab"
                                                                                                   id="hiveTable">hive字典</a>
                                </li>
                                <li role="presentation" style="background-color: #fff"><a href="#" role="tab"
                                                                                          id="middleTable">中间字典</a>
                                </li>
                                <li role="presentation" style="background-color: #fff"><a href="#" role="tab"
                                                                                          id="officialTable">业务字典</a>
                                </li>
                            </ul>
                        </div>
                        <div class="box-body" style="height: 100%;padding-bottom: 10px;">
                            <div>
                                <input type="text" class="form-control" id="keyWords" placeholder="请输入关键词(空格分割,回车搜索)">
                                <p id="searchInfo" style="display: none">查找中，请稍候...</p>
                                <div>
                                    <ul id="allTree" class="ztree"></ul>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>

                <div class="col-md-9 col-sm-9 col-lg-9 colStyle height-self"
                     style="overflow:auto;overflow-x:hidden" id="showAllModal">
                    <div class="my-box" style="margin-top: 0">
                        <div id="tableMessage" class="box-body text-center">
                            <label class="info-title">数据描述</label>
                            <form class="form-horizontal form-group-sm" id="tableForm">
                                <div class="row">
                                    <div class="col-sm-12">
                                        <div class="form-group">
                                            <label class="control-label input-sm col-sm-1">数据库名称:</label>
                                            <div class="col-sm-5">
                                                <input class="form-control" type="text" name="databaseName" readonly>
                                            </div>
                                            <label class="control-label input-sm col-sm-1">表名称:</label>
                                            <div class="col-sm-5">
                                                <input class="form-control" type="text" name="tableName" readonly>
                                            </div>
                                        </div>

                                        <div class="form-group">
                                            <label class="control-label input-sm col-sm-1">创建时间:</label>
                                            <div class="col-sm-5">
                                                <input class="form-control" type="text" name="tableCreateTime" readonly>
                                            </div>

                                            <label id="labelb" class="control-label input-sm col-sm-1"
                                                   style="display: none">最后更新时间:</label>
                                            <label id="labela" class="control-label input-sm col-sm-1" style="">表定义最近更新时间:</label>
                                            <div class="col-sm-5">
                                                <input class="form-control" type="text" name="lastDdlTime" readonly>
                                            </div>
                                        </div>

                                        <div class="form-group">
                                            <label class="control-label input-sm col-sm-1">创建人:</label>
                                            <div class="col-sm-5">
                                                <input class="form-control" type="text" name="tableOwner" readonly>
                                            </div>
                                            <label class="control-label input-sm col-sm-1">业务描述:</label>
                                            <div class="col-sm-5">
                                                <textarea class="form-control" type="text" name="tableComment"
                                                          readonly></textarea>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </form>
                        </div>
                        <div id="tableFieldHive" class="box box-body text-center">
                            <label class="info-title">字段展示</label>
                            <div id="hiveDiv">
                                <table id="tableForHive" lay-filter="tableForHive"></table>
                            </div>
                            <#--                            <div id="middleDiv" style="display: none">-->
                            <#--                                <table id="tableForMiddle" lay-filter="tableForMiddle"></table>-->
                            <#--                            </div>-->
                            <#--                            <div id="officialDiv" style="display:none;">-->
                            <#--                                <table id="tableForOfficial" lay-filter="tableForOfficial"></table>-->
                            <#--                            </div>-->
                        </div>
                    </div>
                </div>
            </div>
        </section>
    </div>

</div>

<#--定时表达式模态框-->
<!-- /.modal -->


<@netCommon.commonScript />

<script src="${request.contextPath}/plugins/ztree/js/jquery.ztree.core.min.js"></script>
<script src="${request.contextPath}/plugins/ztree/js/jquery.ztree.exedit.min.js"></script>
<script src="${request.contextPath}/plugins/ztree/js/jquery.ztree.excheck.min.js"></script>
<script src="${request.contextPath}/plugins/ztree/js/jquery.ztree.exhide.min.js"></script>
<script src="${request.contextPath}/plugins/codemirror/lib/codemirror.js"></script>
<script src="${request.contextPath}/plugins/codemirror/mode/shell/shell.js"></script>
<script src="${request.contextPath}/plugins/codemirror/addon/hint/anyword-hint.js"></script>
<script src="${request.contextPath}/plugins/codemirror/addon/hint/show-hint.js"></script>
<script src="${request.contextPath}/plugins/codemirror/addon/hint/sql-hint.js"></script>
<script src="${request.contextPath}/plugins/codemirror/addon/hint/active-line.js"></script>
<script src="${request.contextPath}/plugins/codemirror/mode/python/python.js"></script>
<script src="${request.contextPath}/plugins/codemirror/mode/sql/sql.js"></script>
<script src="${request.contextPath}/adminlte/plugins/bootstrap-fileinput/fileinput.min.js"></script>
<script src="${request.contextPath}/adminlte/plugins/bootstrap-fileinput/zh.min.js"></script>

<script src="${request.contextPath}/adminlte/plugins/bootstrap-table/bootstrap-table.min.js"></script>
<script src="${request.contextPath}/adminlte/plugins/bootstrap-table/bootstrap-table-zh-CN.min.js"></script>
<script src="${request.contextPath}/plugins/d3/dagre-d3.js"></script>
<script src="${request.contextPath}/plugins/d3/d3.v3.min.js"></script>
<#--<script src="${request.contextPath}/plugins/bootstrap-select/bootstrap-select.min.js"></script>-->
<script src="${request.contextPath}/js/taskGraph.js?v=2"></script>
<script src="${request.contextPath}/js/dic/dataDictionaryNew.js"></script>
<script src="${request.contextPath}/js/common.js"></script>
</body>

</html>


