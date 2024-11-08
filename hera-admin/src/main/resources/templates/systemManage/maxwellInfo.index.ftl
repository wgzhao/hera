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
            <#--<div class="row">
                <div class="col-lg-12">
                    <div class="box box-info">
                        <div class="box-header with-border">
                            <h3 class="box-title">maxwell信息详情</h3>
                            <div class="box-tools pull-right">
                                <button type="button" class="btn btn-box-tool" data-widget="collapse"><i
                                        class="fa fa-minus"></i>
                                </button>
                                <button type="button" class="btn btn-box-tool" data-widget="remove"><i
                                        class="fa fa-times"></i></button>
                            </div>
                        </div>
                        <!-- /.box-header &ndash;&gt;
                        <div class="box-body table-responsive">
                            <div id="processMonitor">
                                <table id="maxwellPage" lay-filter="maxwellPage"></table>
                            </div>
                        </div>
                        <!-- /.box-body &ndash;&gt;
                    </div>
                </div>
            </div>-->

            <div class="row">
                <div class="col-lg-12">
                    <div class="box box-info">
                        <div class="box-header with-border">
                            <h3 class="box-title">nginx实时信息详情</h3>
                            <div class="box-tools pull-right">
                                <#-- <div class="layui-input-inline">
                                     <input type="text" class="form-control" id="timePoint" placeholder="请选择日期" readonly="true">
                                 </div>-->
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
                                <table id="nginxPage" lay-filter="nginxPage"></table>
                            </div>
                        </div>

                        <#--<div id="nginxLine" class="box-body" style="height: 500px;width: 100%;"></div>-->
                        <!-- /.box-body -->
                    </div>

                    <div class="box box-info">
                        <div class="box-header with-border">
                            <h3 class="box-title">nginx历史信息详情</h3>
                            <div class="box-tools pull-right">
                                <div class="layui-input-inline">
                                    <input type="text" class="form-control" id="timePoint" placeholder="请选择日期"
                                           readonly="true">
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
                                <div id="nginxLine" class="box-body" style="height: 500px;width: 100%;"></div>
                            </div>
                        </div>

                        <#--<div id="nginxLine" class="box-body" style="height: 500px;width: 100%;"></div>-->
                        <!-- /.box-body -->
                    </div>
                </div>
            </div>
        </section>
    </div>

    <#--content-wrapper-->
</div>
<@netCommon.commonScript />

<script src="${request.contextPath}/js/maxwellInfo.js"></script>
<script src="${request.contextPath}/plugins/easyPie/jquery.easypiechart.min.js"></script>
<script src="${request.contextPath}/plugins/echarts/echarts.min.js"></script>
<script src="${request.contextPath}/plugins/echarts/PercentPie.js"></script>
<script src="${request.contextPath}/plugins/echarts/macarons.js"></script>
<script src="${request.contextPath}/plugins/echarts/shine.js"></script>
<script src="${request.contextPath}/adminlte/plugins/bootstrap-table/bootstrap-table.min.js"></script>
<script src="${request.contextPath}/adminlte/plugins/bootstrap-table/bootstrap-table-zh-CN.min.js"></script>
</body>

</html>


