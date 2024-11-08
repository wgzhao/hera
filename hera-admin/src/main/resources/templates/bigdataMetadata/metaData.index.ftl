<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/html" xmlns="http://www.w3.org/1999/html">
<head>
    <title>任务调度平台
    </title>
    <#import "/common/common.macro.ftl" as netCommon>
    <@netCommon.commonStyle />
    <link href="${request.contextPath}/adminlte/plugins/bootstrap-table/bootstrap-table.min.css" rel="stylesheet">
</head>

<style>
    .my-easy-pie {
        text-align: center;
        margin-top: 50px;
        font-size: 20px;
        color: #666;
    }

    .btn-default {
        background-color: #fff !important;
        color: #444;
        border-color: #ddd;
    }

    .box-header {
        padding: 12px;
    }

    .table-hover > tbody > tr:hover {
        background-color: #fff;
    }

    /* #taskStatus {
         width: auto;
         display: inline;
     }*/

    #user {
        width: auto;
        display: inline;
    }

    /* #metaDataForm > label:first-child {
         width: 20% !important;
     }

     #metaDataForm > input:first-child {
         width: 50% !important;;
     }*/
    .layui-form-item > label:first-child {
        width: 20% !important;
    }

    .layui-form-item > div > input:first-child {
        width: 80% !important;
    }

    .layui-form-item > div > textarea:first-child {
        width: 80% !important;
    }

    .layui-form > div:last-child {

        margin-top: 50px;
        margin-left: 150px;
    }

</style>

<body class="hold-transition skin-black sidebar-mini">
<div class="wrapper">
    <!-- header -->
    <@netCommon.commonHeader />
    <!-- left -->
    <@netCommon.commonLeft "developCenter" />

    <div class="content-wrapper">
        <!-- Content Header (Page header) -->

        <!-- Main content -->
        <section class="content container-fluid">

            <!--------------------------
              | Your Page Content Here |
              -------------------------->
            <div class="row">
                <div class="col-lg-12">
                    <div class="box box-info">
                        <div class="box-header with-border">
                            <h3 class="box-title">业务元数据</h3>
                            <div class="box-tools pull-right">
                                <div class="layui-input-inline">
                                    <input type="text" class="form-control" name="queryData" id="queryData"
                                           placeholder="请输入查询类容">
                                </div>
                                <div class="layui-input-inline">
                                    <input type="text" class="form-control" name="dateTime" id="dateTime"
                                           placeholder="请选择日期" readonly="true">
                                </div>
                                <div class="layui-input-inline">
                                    <button type="button" id="metaBt" class="layui-btn layui-btn-normal">新增</button>
                                </div>

                                <button type="button" class="btn btn-box-tool" data-widget="collapse"><i
                                            class="fa fa-minus"></i>
                                </button>
                                <button type="button" class="btn btn-box-tool" data-widget="remove"><i
                                            class="fa fa-times"></i></button>
                            </div>
                        </div>
                        <!-- /.box-header -->
                        <div class="box-body table-responsive">
                            <div id="processMonitor">
                                <table id="metaDataTable" lay-filter="metaDataTable"></table>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <div id="metaDataForm" hidden="hidden">
                <div style="height: 40px"></div>
                <form class="layui-form">
                    <div class="layui-form-item">
                        <label class="layui-form-label">规则名称</label>
                        <div class="layui-input-block">
                            <input type="text" name="ruleName" required lay-verify="required" placeholder="请输入内容"
                                   autocomplete="off" class="layui-input">
                        </div>
                    </div>
                    <div class="layui-form-item">
                        <label class="layui-form-label">统计规则</label>
                        <div class="layui-input-block">
                            <input type="text" name="ruleStat" required lay-verify="required" placeholder="请输入内容"
                                   autocomplete="off" class="layui-input">
                        </div>
                    </div>


                    <div class="layui-form-item">
                        <label class="layui-form-label">业务归属</label>
                        <div class="layui-input-block">
                            <input type="text" name="bizDesc" required lay-verify="required" placeholder="请输入内容"
                                   autocomplete="off" class="layui-input">
                        </div>
                    </div>
                    <div class="layui-form-item">
                        <label class="layui-form-label">业务描述</label>
                        <div class="layui-input-block">
                            <input type="text" name="bizAff" required lay-verify="required" placeholder="请输入内容"
                                   autocomplete="off" class="layui-input">
                        </div>
                    </div>
                    <div class="layui-form-item">
                        <label class="layui-form-label">规则状态</label>
                        <div class="layui-input-block">
                            <input type="radio" name="status" value=1 title="有效" checked>
                            <input type="radio" name="status" value=0 title="无效">
                        </div>
                    </div>

                    <div class="layui-form-item">
                        <label class="layui-form-label">规则详情</label>
                        <div class="layui-input-block">
                            <#--<input type="text" name="ruleSql" required lay-verify="required" placeholder="请输入规则详情" autocomplete="off" class="layui-input">-->
                            <textarea name="ruleSql" placeholder="请输入内容" class="layui-textarea"></textarea>
                        </div>
                    </div>


                    <#-- <div class="layui-form-item layui-form-text">
                         <label class="layui-form-label">文本域</label>
                         <div class="layui-input-block">
                             <textarea name="desc" placeholder="请输入内容" class="layui-textarea"></textarea>
                         </div>
                     </div>-->
                    <div class="layui-form-item">
                        <div class="layui-input-block">
                            <button class="layui-btn" lay-submit lay-filter="metaDataSub">立即提交</button>
                            <button type="reset" class="layui-btn layui-btn-primary">重置</button>
                        </div>
                    </div>
                </form>
            </div>

            <div id="metaSql" hidden="hidden">
                <div style="height: 40px"></div>
                <form class="layui-form">
                    <div class="layui-form-item">
                        <label class="layui-form-label">规则详情</label>
                        <div class="layui-input-block">
                            <#--<input type="text" name="ruleSql" required lay-verify="required" placeholder="请输入规则详情" autocomplete="off" class="layui-input">-->
                            <textarea name="ruleSql" id="ruleSqlText" placeholder="请输入内容"
                                      class="layui-textarea"></textarea>
                        </div>
                    </div>

                    <div class="layui-form-item">
                        <div class="layui-input-block">
                            <button class="layui-btn" lay-submit lay-filter="metaDataSub">立即提交</button>
                            <button type="reset" class="layui-btn layui-btn-primary">重置</button>
                        </div>
                    </div>
                </form>
            </div>
            <!-- /.box-body -->
    </div>
    </section>
</div>

<#--content-wrapper-->
</div>
<@netCommon.commonScript />

<script src="${request.contextPath}/js/metaData.js"></script>
<script src="${request.contextPath}/adminlte/plugins/bootstrap-table/bootstrap-table.min.js"></script>
<script src="${request.contextPath}/adminlte/plugins/bootstrap-table/bootstrap-table-zh-CN.min.js"></script>
</body>

</html>


