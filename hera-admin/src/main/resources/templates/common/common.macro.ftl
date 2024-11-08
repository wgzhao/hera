<#macro commonStyle>
<#-- favicon -->
    <link rel="shortcut icon" type="image/ico" href="${request.contextPath}/images/favicon.png"/>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <!-- Tell the browser to be responsive to screen width -->
    <meta content="width=device-width, initial-scale=1, maximum-scale=1, user-scalable=no" name="viewport">
    <link href="${request.contextPath}/adminlte/bootstrap/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="${request.contextPath}/plugins/jsmind/style/jsmind.css"/>
    <link rel="stylesheet" href="${request.contextPath}/adminlte/plugins/font-awesome-4.5.0/css/font-awesome.min.css">
    <link rel="stylesheet" href="${request.contextPath}/adminlte/dist/css/AdminLTE.min.css">
    <link rel="stylesheet" href="${request.contextPath}/adminlte/dist/css/skins/_all-skins.min.css">
    <link rel="stylesheet" href="${request.contextPath}/plugins/ionicons-2.0.1/css/ionicons.min.css">
    <link rel="stylesheet" href="${request.contextPath}/plugins/layui/css/layui.css">
    <link rel="stylesheet" href="${request.contextPath}/css/common.css">

    <!-- HTML5 Shim and Respond.js IE8 support of HTML5 elements and media queries -->
    <!-- WARNING: Respond.js doesn't work if you view the page via file:// -->
    <!--[if lt IE 9]>

    <![endif]-->
    <!-- pace -->
</#macro>

<#macro commonScript>
    <script src="${request.contextPath}/adminlte/plugins/jQuery/jquery-2.2.3.min.js"></script>
    <script src="${request.contextPath}/adminlte/bootstrap/js/bootstrap.min.js"></script>
    <script src="${request.contextPath}/plugins/layui/layui.js"></script>
    <script src="${request.contextPath}/plugins/html5shiv/html5shiv.min.js"></script>
    <script src="${request.contextPath}/plugins/respond/respond.min.js"></script>

    <script src="${request.contextPath}/adminlte/dist/js/app.min.js"></script>

    <script src="${request.contextPath}/js/common.js"></script>

    <script>var base_url = '${request.contextPath}';</script>

    <script>var screenHeight = document.body.clientHeight</script>
    <script>
        $.get(base_url + '/isAdmin', function (data) {
            if (data.data === true) {
                $('#sysManager').css("display", "block");
            } else {
                $('#sysManager').css("display", "none");
            }
        });
    </script>

<#--<script src="${request.contextPath}/js/datalinkInfo.js"></script>-->

</#macro>

<#macro commonHeader>
    <header class="main-header">
        <a href="${request.contextPath}/home" class="logo">
            <span class="logo-mini"><b>${company}</b></span>
            <span class="logo-lg"><b>任务调度平台</b></span>
        </a>
        <nav class="navbar navbar-static-top" role="navigation">
            <a href="#" class="sidebar-toggle" data-toggle="offcanvas" role="button"><span
                        class="sr-only">切换导航</span></a>
            <div class="navbar-custom-menu">
                <ul class="nav navbar-nav">

                    <li class="dropdown user user-menu">
                        <a id="getCurrentUser" class="dropdown-toggle" data-toggle="dropdown" aria-expanded="false">
                        </a>
                    </li>
                    <#-- <li><a  role="tab-register" data-toggle="tab" id="changePassword" >修改密码</a></li>-->
                    <li class="dropdown user user-menu">
                        <a id="logoutBtn" class="dropdown-toggle" data-toggle="dropdown" aria-expanded="false">
                            <span class="hidden-xs">注销</span>
                        </a>
                    </li>
                </ul>
            </div>
        </nav>
    </header>
</#macro>



<#macro commonLeft pageName >
    <!-- Left side column. contains the logo and sidebar -->
    <aside class="main-sidebar height-self">
        <!-- sidebar: style can be found in sidebar.less -->
        <section class="sidebar">
            <ul class="sidebar-menu tree">
                <li class="treeview menu-closed" id="home">
                    <a href="${request.contextPath}/home">
                        <i class="fa fa-dashboard"></i> <span>首页</span>
                    </a>
                </li>
                <#--<li class="nav-click" id="machineInfoMenu" style="display: none"><a-->
                <#--href="${request.contextPath}/machineInfo"><i class="fa fa-book"></i> <span>机器组监控</span></a>-->
                <#--</li>-->

                <li class="treeview menu-closed my-tree" id="MintorCenter" style="display: block">
                    <a href="${request.contextPath}/machineInfo">
                        <#--<a>-->
                        <i class="fa fa-folder"></i> <span id="dddd">监控中心</span>
                        <span class="pull-right-container">
                          <i class="fa fa-angle-left pull-right"></i>
                        </span>
                    </a>
                    <ul class="treeview-menu">
                        <li class="nav-click" id="machineInfoMenu">
                            <a href="${request.contextPath}/machineInfo">
                                <i class="fa fa-book"></i> <span>机器组监控</span></a>
                        </li>
                        <#--<li class="nav-click" id="datalinkMintor">
                        &lt;#&ndash;<a href="${request.contextPath}/datalinkMintor">&ndash;&gt;
                            <a>
                                <i class="fa fa-book"></i> 实时任务监控</a>
                        </li>-->
                        <li class="nav-click" id="offlineTaskMonitoring">
                            <a href="${request.contextPath}/offlineTaskMonitoring">
                                <i class="fa fa-book"></i> 离线任务监控</a>
                        </li>
                        <li class="nav-click" id="maxwellInfo">
                            <a href="${request.contextPath}/maxwellInfo">
                                <i class="fa fa-book"></i> 实时服务监控</a>
                        </li>
                    </ul>
                </li>


                <li class="treeview menu-closed my-tree" id="sysManager" style="display: none">
                    <a href="#">
                        <#--<a>-->
                        <i class="fa fa-folder"></i> <span>系统管理</span>
                        <span class="pull-right-container">
                          <i class="fa fa-angle-left pull-right"></i>
                        </span>
                    </a>
                    <ul class="treeview-menu">
                        <li id="userManage"><a href="${request.contextPath}/userManage"><i
                                        class="fa fa-circle-o"></i>
                                用户管理</a></li>
                        <li id="hostGroupManage"><a href="${request.contextPath}/hostGroupManage"><i
                                        class="fa fa-circle-o"></i> 机器组管理</a>
                        </li>
                        <li id="workManage"><a href="${request.contextPath}/workManage"><i
                                        class="fa fa-circle-o"></i> worker管理</a>
                        </li>
                    </ul>
                </li>

                <li class=" treeview menu-closed my-tree" id="jobManage">
                    <a href="${request.contextPath}/jobDetail">
                        <i class="fa fa-folder"></i> <span>任务管理</span>
                        <span class="pull-right-container">
                          <i class="fa fa-angle-left pull-right"></i>
                        </span>
                    </a>
                    <ul class="treeview-menu">
                        <#--<li class="" id="jobDetailMenu"><a href="${request.contextPath}/jobDetail"><i-->
                        <#--class="fa fa-circle-o"></i>开启任务详情</a>-->
                        <#--</li>-->

                        <li class="" id="StopJobDetailMenu"><a href="${request.contextPath}/stopJobDetail?syFlag=0"><i
                                        class="fa fa-circle-o"></i>任务详情</a>
                        </li>

                        <li class="" id="jobDag"><a href="${request.contextPath}/jobDag"><i
                                        class="fa fa-circle-o"></i>任务依赖图</a></li>
                    </ul>
                </li>

                <li class=" treeview menu-closed my-tree" id="consanguinityManage">
                    <a href="#">
                        <i class="fa fa-folder"></i> <span>血缘分析</span>
                        <span class="pull-right-container">
                          <i class="fa fa-angle-left pull-right"></i>
                        </span>
                    </a>
                    <ul class="treeview-menu">
                        <li class="" id="tableDeps"><a href="${request.contextPath}/tableDeps"><i
                                        class="fa fa-circle-o"></i>表级血缘图</a></li>
                        <li class="" id="columnDeps"><a href="${request.contextPath}/columnDeps"><i
                                        class="fa fa-circle-o"></i>字段级血缘图</a></li>
                        <li class="" id="dataswitch"><a href="${request.contextPath}/dataswitch"><i
                                        class="fa fa-circle-o"></i>数据转换</a></li>
                    </ul>
                </li>

                <li class=" treeview menu-closed my-tree" id="bigdataMetadata">
                    <a href="#">
                        <i class="fa fa-folder"></i> <span>数据管理</span>
                        <span class="pull-right-container">
                          <i class="fa fa-angle-left pull-right"></i>
                        </span>
                    </a>
                    <ul class="treeview-menu">
                        <li class="" id="metaData">
                            <a href="${request.contextPath}/bigdataMetadata/metaData"><i
                                        class="fa fa-circle-o"></i>元数据</a>
                        </li>

                        <li class="" id="dataDictionary">
                            <a href="${request.contextPath}/bigdataMetadata/dataDictionary"><i
                                        class="fa fa-circle-o"></i>数据字典</a>
                        </li>

                        <li class="" id="dataDiscoveryNew">
                            <a href="${request.contextPath}/bigdataMetadata/dataDiscovery"><i
                                        class="fa fa-circle-o"></i>数据发现</a>
                        </li>


                        <li class="" id="sqoopTaskDetail">
                            <a href="${request.contextPath}/sqoopTaskCenter/sqoopTaskDetail">
                                <i class="fa fa-circle-o"></i>数据监控</a>
                        </li>

                    </ul>
                </li>


                <li class="nav-click" id="developManage"><a
                            href="${request.contextPath}/developCenter"><i class="fa fa-book"></i> <span>开发中心</span></a>
                </li>
                <li class="nav-click" id="scheduleManage"><a
                            href="${request.contextPath}/scheduleCenter?syFlag=0"><i class="fa fa-edit"></i>
                        <span>调度中心</span></a>
                </li>

                <#--<li class="nav-click" id="scheduleManage"><a
                        href="${request.contextPath}/dataxScheduleCenter"><i class="fa fa-edit"></i> <span>dataX调度中心</span></a>
                </li>-->
                <#--<li class="nav-click" id="advice"><a-->
                <#--href="${request.contextPath}/adviceController"><i class="fa fa-bug"></i> <span>建议&留言</span></a>-->
                <#--</li>-->


            </ul>
        </section>
        <!-- /.sidebar -->
    </aside>
</#macro>

<#macro commonControl >
    <!-- Control Sidebar -->
    <aside class="control-sidebar control-sidebar-dark">
        <!-- Create the tabs -->
        <ul class="nav nav-tabs nav-justified control-sidebar-tabs">
            <li class="active"><a href="#control-sidebar-home-tab" data-toggle="tab"><i class="fa fa-home"></i></a></li>
            <li><a href="#control-sidebar-settings-tab" data-toggle="tab"><i class="fa fa-gears"></i></a></li>
        </ul>
        <!-- Tab panes -->
    </aside>
    <!-- /.control-sidebar -->
    <!-- Add the sidebar's background. This div must be placed immediately after the control sidebar -->
    <div class="control-sidebar-bg"></div>
</#macro>

<#macro commonFooter >
    <footer class="main-footer">
        Powered by <b>hera</b> 2.3.1
        <div class="pull-right hidden-xs">
            <strong>Copyright &copy; 2018-${.now?string('yyyy')} &nbsp;
            </strong><!-- All rights reserved. -->
        </div>
    </footer>
</#macro>
