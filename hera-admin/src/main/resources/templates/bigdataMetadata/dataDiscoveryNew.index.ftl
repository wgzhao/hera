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
                            <div class="box-tools ">

                                <div>
                                    <div class="layui-input-inline">
                                        <input type="text" class="form-control" name="queryData" id="queryData"
                                               placeholder="请输入查询类容">
                                    </div>
                                    <div class="layui-input-inline">
                                        <input type="text" class="form-control" name="dateTime" id="dateTime"
                                               placeholder="请选择日期" readonly="true">
                                    </div>
                                </div>


                                <#-- <div class="layui-input-inline" style="margin-right:1px">
                                     <button type="button" class="btn btn-box-tool" data-widget="collapse" style="margin-right:1px"><i
                                                 class="fa fa-minus"></i>
                                     </button>
                                 </div>

                                 <div class="layui-input-inline" style="margin-left:100px">
                                     <button type="button" class="btn btn-box-tool" data-widget="remove" style="margin-right:1px"><i
                                                 class="fa fa-times"></i></button>
                                 </div>-->
                            </div>
                            <div>
                                <div class="layui-input-inline" style="margin-left:0px">
                                    <ul class="nav nav-tabs" role="tablist">
                                        <li role="presentation" class="" style="background-color: #fff">
                                            <a href="#" role="tab" id="tabOfficial">正式业务库</a>
                                        </li>
                                        <li role="presentation" style="background-color: #fff">
                                            <a href="#" role="tab" id="tabMiddle">阿里云中间库</a>
                                        </li>
                                    </ul>
                                </div>
                            </div>
                        </div>
                        <!-- /.box-header -->
                        <div class="box-body table-responsive" style="">
                            <div id="processMonitor">
                                <table id="dataDiscovery" lay-filter="dataDiscovery"></table>
                            </div>
                        </div>
                        <!-- /.box-body -->
                    </div>
                </div>
            </div>
        </section>
    </div>

    <#--content-wrapper-->
</div>
<@netCommon.commonScript />
<script src="${request.contextPath}/js/dic/dataDiscoveryNew.js"></script>
<script src="${request.contextPath}/adminlte/plugins/bootstrap-table/bootstrap-table.min.js"></script>
<script src="${request.contextPath}/adminlte/plugins/bootstrap-table/bootstrap-table-zh-CN.min.js"></script>
</body>

</html>


