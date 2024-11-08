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
    <link href="${request.contextPath}/plugins/codemirror/theme/solarized.css" rel="stylesheet">
    <link href="${request.contextPath}/adminlte/plugins/bootstrap-fileinput/fileinput.min.css" rel="stylesheet">
    <link href="${request.contextPath}/adminlte/plugins/bootstrap-table/bootstrap-table.min.css" rel="stylesheet">
    <link rel="stylesheet" href="${request.contextPath}/plugins/bootstrap-select/bootstrap-select.min.css">

    <link rel="stylesheet" href="${request.contextPath}/css/scheduleCenter.css">


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
        <input type="hidden" id="syFlag" name="syFlag" value="${syFlag}">
        <section class="content">
            <div class="row">
                <div class="col-md-3 col-sm-3 col-lg-3 colStyle" style="border: none" id="treeCon">
                    <div class="height-self left-bar" style="overflow: auto;">
                        <div class="box-header left-bar-head">
                            <ul class="nav nav-tabs" role="tablist">
                                <li role="presentation" class="active" style="background-color: #fff"><a href="#"
                                                                                                         role="tab"
                                                                                                         id="myScheBtn">我的调度任务</a>
                                </li>
                                <li role="presentation" style="background-color: #fff"><a href="#" role="tab"
                                                                                          id="allScheBtn">全部调度任务</a>
                                </li>
                                <#--<li role="presentation" style="background-color: #fff"><a href="#" role="tab"
                                                                                          id="wwweee">功能按钮</a>
                                </li>-->
                            </ul>
                            <div class="box-tools">
                                <button type="button" class="btn btn-box-tool" id="hideTreeBtn"><i
                                            class="fa fa-minus"></i>
                                </button>
                            </div>
                        </div>
                        <div class="box-body" style="height: 100%;padding-bottom: 10px;">
                            <div>
                                <input type="text" class="form-control" id="keyWords" placeholder="请输入关键词(空格分割,回车搜索)">
                                <p id="searchInfo" style="display: none">查找中，请稍候...</p>
                                <div class="scroll-box">
                                    <ul id="jobTree" class="ztree"></ul>
                                    <ul id="allTree" class="ztree"></ul>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>

                <div class="col-md-8 col-sm-8 col-lg-8 colStyle height-self"
                     style="overflow: auto;background: transparent;border: none;display: none" id="showAllModal">
                    <div class="my-box" style="margin-top: 0">
                        <div class="box box-body text-center">
                            <div id="allTable">
                            </div>
                        </div>
                    </div>
                </div>


                <div class="col-md-8 col-sm-8 col-lg-8 colStyle height-self"
                     style="overflow: auto;" id="infoCon">

                    <div class="my-box" style="margin-top: 0">

                        <div id="groupMessage" class="box-body text-center" style="display: none">

                            <label class="info-title">基本信息</label>
                            <form class="form-horizontal form-group-sm">

                                <div class="row">
                                    <div class="col-sm-12">
                                        <div class="form-group">
                                            <label class="control-label input-sm col-sm-1">组id:</label>
                                            <div class="col-sm-3">
                                                <input class="form-control" type="text" name="id" readonly>
                                            </div>
                                            <label class="control-label input-sm col-sm-1">名称:</label>
                                            <div class="col-sm-3">
                                                <input class="form-control" type="text" name="name"
                                                       id="out-info-title-name" readonly>
                                            </div>
                                            <label class="control-label input-sm col-sm-1">所有人:</label>
                                            <div class="col-sm-3">
                                                <#--<label class="form-control-static" name="owner">类型</label>-->
                                                <input class="form-control" type="text" name="owner" readonly>
                                            </div>
                                        </div>
                                        <div class="form-group">
                                            <label class="control-label input-sm col-sm-1">描述:</label>
                                            <div class="col-sm-3">
                                                <#--<label class="form-control-static" name="description">导数据</label>-->
                                                <input class="form-control" type="text" name="description" readonly>
                                            </div>
                                            <label class="control-label input-sm col-sm-1">关注人员:</label>
                                            <div class="col-sm-3">
                                                <#--<label class="form-control-static" name="focusUser"></label>-->
                                                <input class="form-control" type="text" name="focusUser" readonly>
                                            </div>
                                            <label class="control-label input-sm col-sm-1">管理员:</label>
                                            <div class="col-sm-3">
                                                <#--<label class="form-control-static" name="uidS"></label>-->
                                                <input class="form-control" type="text" name="uidS" readonly>
                                            </div>
                                        </div>

                                    </div>
                                </div>


                            </form>

                        </div>

                        <div id="jobMessage" class="box-body text-center" style="display: none">

                            <#-- <div class='dp-model-alert'> </div>-->


                            <label class="info-title">基本信息</label>

                            <form class="form-group-sm form-horizontal">

                                <div class="row">
                                    <div class="col-lg-4 col-md-4 col-sm-4">
                                        <div class="form-group">
                                            <label class="control-label input-sm col-sm-3">任务id:</label>
                                            <div class="col-sm-8">
                                                <input class="form-control" type="text" name="id"
                                                       id="schedule-center-id-readonly" readonly>
                                            </div>
                                        </div>
                                        <div class="form-group">
                                            <label class="control-label input-sm col-sm-3">名称:</label>
                                            <div class="col-sm-8">
                                                <input class="form-control" type="text" name="name" readonly>
                                            </div>
                                        </div>
                                        <div class="form-group">
                                            <label class="control-label input-sm col-sm-3">任务类型:</label>
                                            <div class="col-sm-8">
                                                <input class="form-control" type="text" name="runType" readonly>
                                            </div>
                                        </div>
                                        <div class="form-group">
                                            <label class="control-label input-sm col-sm-3"> 自动调度:</label>
                                            <div class="col-sm-8">
                                                <input class="form-control" type="text" name="auto" readonly>
                                            </div>
                                        </div>
                                        <div class="form-group">
                                            <label class="control-label input-sm col-sm-3">任务优先级:</label>
                                            <div class="col-sm-8">
                                                <input class="form-control" type="text" name="runPriorityLevel"
                                                       readonly>
                                            </div>
                                        </div>
                                        <div class="form-group">
                                            <label class="control-label input-sm col-sm-3"><label class="tip">*</label>描述:</label>
                                            <div class="col-sm-8">
                                                <input class="form-control" type="text" name="description" readonly>
                                            </div>
                                        </div>
                                    </div>
                                    <div class="col-lg-4 col-md-4 col-sm-4">
                                        <div class="form-group">
                                            <label class="control-label input-sm col-sm-3">调度类型:</label>
                                            <div class="col-sm-8">
                                                <input class="form-control" type="text" name="scheduleType" readonly>
                                            </div>
                                        </div>
                                        <div class="form-group" id="dependencies">
                                            <label class="control-label input-sm col-sm-3">依赖任务:</label>
                                            <div class="col-sm-8">
                                                <input class="form-control" type="text" name="dependencies" readonly>
                                            </div>
                                        </div>
                                        <div class="form-group" id="heraDependencyCycle">
                                            <label class="control-label input-sm col-sm-3">依赖周期:</label>
                                            <div class="col-sm-8">
                                                <input class="form-control" type="text" name="heraDependencyCycle"
                                                       readonly>
                                            </div>
                                        </div>
                                        <div class="form-group" id="cronExpression">
                                            <label class="control-label input-sm col-sm-3">定时表达式:</label>
                                            <div class="col-sm-8">
                                                <input class="form-control" type="text" name="cronExpression" readonly>
                                            </div>
                                        </div>
                                        <div class="form-group">
                                            <label class="control-label input-sm col-sm-3">重试次数:</label>
                                            <div class="col-sm-8">
                                                <input class="form-control" type="text" name="rollBackTimes" readonly>
                                            </div>
                                        </div>
                                        <div class="form-group">
                                            <label class="control-label input-sm col-sm-3">重试间隔:</label>
                                            <div class="col-sm-8">
                                                <input class="form-control" type="text" name="rollBackWaitTime"
                                                       readonly>
                                            </div>
                                        </div>
                                        <div class="form-group">
                                            <label class="control-label input-sm col-sm-3">预计时长:</label>
                                            <div class="col-sm-8">
                                                <input class="form-control" type="text" name="" readonly>
                                            </div>
                                        </div>

                                    </div>
                                    <div class="col-lg-4 col-md-4 col-sm-4">
                                        <div class="form-group">
                                            <label class="control-label input-sm col-sm-3">所有人:</label>
                                            <div class="col-sm-8">
                                                <input class="form-control" type="text" name="owner" readonly>
                                            </div>
                                        </div>
                                        <div class="form-group">
                                            <label class="control-label input-sm col-sm-3">关注人员:</label>
                                            <div class="col-sm-8">
                                                <input class="form-control" type="text" name="focusUser" readonly>
                                            </div>
                                        </div>
                                        <div class="form-group ">
                                            <label class="control-label input-sm col-sm-3">管理员:</label>
                                            <div class="col-sm-8">
                                                <input class="form-control" type="text" name="uidS" readonly>
                                            </div>
                                        </div>
                                        <div class="form-group">
                                            <label class="control-label input-sm col-sm-3">重复执行:</label>
                                            <div class="col-sm-8">
                                                <input class="form-control" type="text" name="repeatRun" readonly>
                                            </div>
                                        </div>
                                        <div class="form-group ">
                                            <label class="control-label input-sm col-sm-3">机器组:</label>
                                            <div class="col-sm-8">
                                                <input class="form-control" type="text" name="hostGroupName" readonly>
                                            </div>
                                        </div>

                                        <div class="form-group ">
                                            <label class="control-label input-sm col-sm-3">区域:</label>
                                            <div class="col-sm-8">
                                                <input class="form-control" type="text" name="area" readonly>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </form>

                        </div>

                        <div id="groupMessageEdit" class="box-body" style="display: none;">
                            <form class="form-horizontal form-group-sm" role="form">
                                <form class="form-horizontal">
                                    <div class="row">
                                        <div class="col-lg-8 col-md-8 col-sm-8">
                                            <div class="form-group">
                                                <label class="control-label col-sm-4 col-lg-4 col-md-4">名称:</label>
                                                <div class="col-sm-8 col-lg-8 col-md-8 ">
                                                    <input class="form-control" type="text" name="name">
                                                </div>
                                            </div>
                                            <div class="form-group">
                                                <label class="control-label col-sm-4 col-lg-4 col-md-4">描述:</label>
                                                <div class="col-sm-8 col-lg-8 col-md-8 ">
                                                    <input class="form-control" type="text" name="description">
                                                </div>
                                            </div>

                                        </div>
                                    </div>
                                </form>

                            </form>
                        </div>
                        <div id="jobMessageEdit" class="box-body" style="display: none;">
                            <form class="form-horizontal form-group-sm" role="form" id="jobMsgEditForm">

                                <div class="row">
                                    <div class="col-sm-6 col-md-6 col-lg-6">

                                        <div class="form-group">
                                            <label class="control-label col-sm-4 col-lg-4 col-md-4">名称:</label>
                                            <div class="col-sm-8 col-lg-8 col-md-8 ">
                                                <input class="form-control" type="text" name="name">

                                            </div>
                                        </div>
                                        <div class="form-group">
                                            <label class="control-label col-sm-4 col-lg-4 col-md-4">重试次数:</label>
                                            <div class="col-sm-8 col-lg-8 col-md-8 ">
                                                <select class="form-control" name="rollBackTimes">
                                                    <option value="0" selected="selected">0</option>
                                                    <option value="1">1</option>
                                                    <option value="2">2</option>
                                                    <option value="3">3</option>
                                                    <option value="4">4</option>
                                                    <option value="5">5</option>
                                                </select>

                                            </div>
                                        </div>
                                        <div class="form-group">
                                            <label class="control-label col-sm-4 col-lg-4 col-md-4">重试间隔(分):</label>
                                            <div class="col-sm-8 col-lg-8 col-md-8 ">
                                                <select class="form-control" name="rollBackWaitTime">
                                                    <option value="1" selected="selected">1</option>
                                                    <option value="3">3</option>
                                                    <option value="5">5</option>
                                                    <option value="10">10</option>
                                                    <option value="10">15</option>
                                                    <option value="30">30</option>
                                                    <option value="60">60</option>
                                                    <option value="120">120</option>
                                                </select>
                                            </div>
                                        </div>
                                        <div class="form-group">
                                            <label class="control-label col-sm-4 col-lg-4 col-md-4">任务类型:</label>
                                            <div class="col-sm-8 col-lg-8 col-md-8 ">
                                                <select class="form-control" name="runType">
                                                    <option value="Shell" selected="selected">Shell</option>
                                                    <option value="Hive">Hive</option>
                                                    <option value="Spark">Spark</option>
                                                    <option value="Impala">Impala</option>
                                                </select>
                                            </div>
                                        </div>
                                        <div class="form-group">
                                            <label class="control-label col-sm-4 col-lg-4 col-md-4">任务优先级:</label>
                                            <div class="col-sm-8 col-lg-8 col-md-8 ">
                                                <select class="form-control" name="runPriorityLevel">
                                                    <option value="3">high</option>
                                                    <option value="2">medium</option>
                                                    <option value="1" selected="selected">low</option>
                                                </select>
                                            </div>
                                        </div>
                                        <div class="form-group">
                                            <label class="control-label col-sm-4 col-lg-4 col-md-4">描述:</label>
                                            <div class="col-sm-8 col-lg-8 col-md-8 ">
                                                <input class="form-control" type="text" name="description">

                                            </div>
                                        </div>
                                    </div>
                                    <div class="col-sm-6 col-md-6 col-lg-6">

                                        <div class="form-group">
                                            <label class="control-label col-sm-4 col-lg-4 col-md-4">调度类型:</label>
                                            <div class="col-sm-8 col-lg-8 col-md-8 ">
                                                <select class="form-control" name="scheduleType">
                                                    <option value="0">定时调度</option>
                                                    <option value="1">依赖调度</option>
                                                </select>
                                            </div>
                                        </div>
                                        <div class="form-group">
                                            <label class="control-label col-sm-4 col-lg-4 col-md-4">定时表达式:</label>
                                            <div class="col-sm-8 col-lg-8 col-md-8 ">
                                                <input class="form-control" type="text" name="cronExpression"
                                                       id="timeChange">

                                            </div>
                                        </div>
                                        <div class="form-group">
                                            <label class="control-label col-sm-4 col-lg-4 col-md-4">依赖任务:</label>
                                            <div class="col-sm-8 col-lg-8 col-md-8 ">
                                                <input class="form-control" type="text" id="dependJob"
                                                       name="dependencies">

                                            </div>
                                        </div>
                                        <div class="form-group">
                                            <label class="control-label col-sm-4 col-lg-4 col-md-4">依赖周期:</label>
                                            <div class="col-sm-8 col-lg-8 col-md-8 ">
                                                <input class="form-control" type="text" name="heraDependencyCycle">

                                            </div>
                                        </div>
                                        <div class="form-group">
                                            <label class="control-label col-sm-4 col-lg-4 col-md-4">机器组:</label>
                                            <div class="col-sm-8 col-lg-8 col-md-8 ">
                                                <select class="form-control" name="hostGroupId">
                                                </select>
                                            </div>
                                        </div>

                                        <#--<div class="form-group">
                                            <label class="control-label col-sm-4 col-lg-4 col-md-4">预计时长(分):</label>
                                            <div class="col-sm-8 col-lg-8 col-md-8 ">
                                                <input class="form-control" type="text" name="jobName">

                                            </div>
                                        </div>-->
                                        <div class="form-group">
                                            <label class="control-label col-sm-4 col-lg-4 col-md-4">预计时长(分):</label>
                                            <div class="col-sm-8 col-lg-8 col-md-8 ">
                                                <input class="form-control" type="text" name="jobDurationTime">

                                            </div>
                                        </div>
                                        <div class="form-group">
                                            <label class="control-label col-sm-4 col-lg-4 col-md-4">区域:</label>
                                            <div class="col-sm-8 col-lg-8 col-md-8 ">
                                                <select name="areaId" class="selectpicker form-control"
                                                        data-live-search="true" multiple data-done-button="true">

                                                </select>
                                            </div>
                                        </div>

                                        <div class="form-group">
                                            <label class="control-label col-sm-4 col-lg-4 col-md-4">重复执行:</label>
                                            <div class="col-sm-8 col-lg-8 col-md-8 ">
                                                <select name="repeatRun" class="form-control">
                                                    <option value="1" selected>是</option>
                                                    <option value="0">否</option>
                                                </select>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </form>
                        </div>
                    </div>
                    <div id="config" class="my-box" style="display: none">
                        <div class="box-body">
                            <div class="form-group">
                                <label class="info-title">配置项信息</label>
                                <textarea class="form-control"
                                ></textarea>
                            </div>
                        </div>
                    </div>
                    <div id="script" class="my-box" style="display: none">
                        <div class="box-body" id="scriptdivs">
                            <div class="form-group">
                                <label class="info-title" style="display: inline">脚本</label>
                                <select class="pull-right center-block" onchange="selectTheme()" id="themeSelect">
                                    <option value="default">default</option>
                                    <option value="solarized">solarized</option>
                                </select>
                            </div>
                            <div class="form-group">
                                  <textarea id="editor" name="editor"></br>
                                  </textarea>
                            </div>
                        </div>
                    </div>

                    <div id="inheritConfig" class="my-box" style="display: none">
                        <div class="box-body">
                            <div class="form-group">
                                <label class="info-title">继承的配置项信息</label>
                                <textarea class="form-control" style="resize: none"
                                ></textarea>
                            </div>
                        </div>
                        <label id="showScriptSelf" class="info-title" style="display: inline"> </label>

                    </div>
                </div>
                <div class="col-md-1 col-lg-1 col-sm-1 colStyle">
                    <div id="groupOperate" style="display: none;" class="btn-con">
                        <div class="box-body">
                            <div>
                                <ul class="list-unstyled">
                                    <li>
                                        <button class="btn btn-xs  btn-primary btn-block" type="button" id="showAllBtn">
                                            任务总览
                                        </button>
                                    </li>
                                    <br>
                                    <li>
                                        <button class="btn btn-xs  btn-primary btn-block" type="button"
                                                name="showRunning">正在运行
                                        </button>
                                    </li>
                                    <br>
                                    <li>
                                        <button class="btn btn-xs  btn-primary btn-block" type="button"
                                                name="showFaild">失败记录
                                        </button>
                                    </li>
                                    <br>
                                    <li>
                                        <button class="btn  btn-xs btn-primary btn-block" type="button" name="addGroup">
                                            添加组
                                        </button>
                                    </li>
                                    <br>
                                    <li>
                                        <button class="btn  btn-xs btn-primary btn-block" type="button" name="edit">编辑
                                        </button>
                                    </li>
                                    <br>
                                    <li>
                                        <button class="btn  btn-xs btn-primary btn-block" type="button" name="addJob">
                                            添加任务
                                        </button>
                                    </li>
                                    <br>
                                    <li>
                                        <button class="btn  btn-xs btn-primary btn-block" type="button" name="delete">
                                            删除
                                        </button>
                                    </li>
                                    <br>
                                    <li>
                                        <button class="btn  btn-xs btn-primary btn-block" type="button" name="addAdmin">
                                            配置管理员
                                        </button>

                                    </li>
                                    <#--     <br>
                                         <li>
                                             <button class="btn  btn-xs btn-primary btn-block" type="button">关注组下任务</button>
                                         </li>-->
                                </ul>
                            </div>
                        </div>

                    </div>

                    <div id="jobOperate" class="btn-con" style="display: none">
                        <div class="box-body" style="white-space:nowrap;">
                            <ul class="list-unstyled">
                                <li>
                                    <button class="btn btn-xs btn-primary btn-block" type="button" name="runningLog">
                                        运行日志
                                    </button>
                                </li>
                                <br>

                                <li>
                                    <button class="btn btn-xs btn-primary btn-block" type="button" name="version">版本生成
                                    </button>
                                </li>
                                <br>
                                <li>
                                    <button class="btn  btn-xs btn-primary btn-block" type="button" name="jobDag"
                                            data-toggle="modal">依赖图
                                    </button>
                                </li>
                                <br>
                                <li>
                                    <button class="btn  btn-xs btn-primary btn-block" type="button" name="lineageDag"
                                            data-toggle="modal">血缘图
                                    </button>
                                </li>
                                <br>
                                <li>
                                    <button class="btn  btn-xs btn-primary btn-block" type="button" name="edit">编辑
                                    </button>
                                </li>
                                <br>
                                <li>
                                    <button id="manual" class="btn  btn-xs btn-primary btn-block" type="button"
                                            data-toggle="modal">
                                        手动执行
                                    </button>
                                </li>
                                <br>
                                <li>
                                    <button id="manualRecovery" class="btn  btn-xs btn-primary btn-block" type="button">
                                        手动恢复
                                    </button>
                                </li>
                                <br>
                                <li>
                                    <button id="downRecovery" class="btn  btn-xs btn-primary btn-block" type="button">
                                        恢复下游
                                    </button>
                                </li>

                                <#--  <br>
                                  <li>
                                      <button id="manualForceRecovery" class="btn  btn-xs btn-primary btn-block"
                                              type="button">
                                          强制恢复
                                      </button>
                                  </li>-->


                                <br>
                                <li>
                                    <button class="btn  btn-xs btn-primary btn-block" type="button" name="switch">
                                        开启/关闭
                                    </button>
                                </li>
                                <br>
                                <li>
                                    <button class="btn  btn-xs btn-primary btn-block" type="button" name="invalid">
                                        失效
                                    </button>
                                </li>
                                <br>
                                <li>
                                    <button class="btn  btn-xs btn-primary btn-block" type="button" name="delete">删除
                                    </button>

                                </li>
                                <br>
                                <li>
                                    <button class="btn  btn-xs btn-primary btn-block" type="button" name="addAdmin">
                                        配置管理员
                                    </button>

                                </li>
                                <br>
                                <li>
                                    <button class="btn  btn-xs btn-primary btn-block" type="button" name="monitor">
                                        关注该任务
                                    </button>
                                </li>

                                <br>

                                <li>
                                    <button class="btn  btn-xs btn-primary btn-block" type="button" name="AddMonitor">
                                        配置关注者
                                    </button>
                                </li>

                                <br>

                                <li>
                                    <button class="btn  btn-xs btn-primary btn-block" id="scriptPreview" type="button"
                                            name="scriptPreview">
                                        脚本预览
                                    </button>
                                </li>

                            </ul>
                        </div>
                    </div>

                    <div id="editOperator" class="btn-con" style="display: none">
                        <div class="box-body">
                            <ul class="list-unstyled">
                                <li>
                                    <button class="btn  btn-xs btn-primary btn-block" type="button" name="back">返回
                                    </button>
                                </li>
                                <br>
                                <li>
                                    <button class="btn  btn-xs btn-primary btn-block" type="button" name="upload">
                                        上传资源文件
                                    </button>
                                </li>
                                <br>
                                <li>
                                    <button class="btn  btn-xs btn-primary btn-block" type="button"
                                            name="editUploadFile">
                                        编辑资源文件
                                    </button>
                                </li>
                                <br>
                                <li>
                                    <button class="btn btn-xs  btn-primary btn-block" type="button" name="save">保存
                                    </button>
                                </li>
                                <br>
                            </ul>
                        </div>
                    </div>

                    <div id="overviewOperator" class="btn-con" style="display: none">
                        <div class="box-body">
                            <ul class="list-unstyled">
                                <li>
                                    <button class="btn  btn-xs btn-primary btn-block" type="button" name="back">返回
                                    </button>
                                </li>
                                <br>
                                <li>
                                    <button class="btn  btn-xs btn-primary btn-block" type="button" name="showRunning">
                                        正在运行
                                    </button>
                                </li>
                                <br>
                                <li>
                                    <button class="btn btn-xs  btn-primary btn-block" type="button" name="showFaild">
                                        失败记录
                                    </button>
                                </li>
                            </ul>
                        </div>
                    </div>
                </div>
            </div>
        </section>
    </div>

</div>
<div class="modal fade" id="myModal" tabindex="-1" role="dialog" aria-labelledby="addConfig" aria-hidden="true">
    <div class="modal-dialog" style="height:100px;">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-hidden="true">
                    &times;
                </button>
                <h4 class="modal-title" id="title">选择Job版本</h4>
            </div>
            <div class="modal-body">
                <div class="input-group form-inline">
                    <label class="input-group-addon control-label form-inline" for="jobVersion">选择Job版本</label>
                    <select id="selectJobVersion" class="form-control">
                    </select>
                </div>
                <br>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-default" data-dismiss="modal">取消</button>
                <button type="button" class="btn btn-info add-btn">执行</button>
            </div>
        </div>
    </div>
</div>


<div class="modal fade" id="myModalForDownRecovery" tabindex="-1" role="dialog" aria-labelledby="MyModelForDown"
     aria-hidden="true">
    <div class="modal-dialog" style="height:100px;">
        <div class="modal-content modal-lg">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-hidden="true">
                    &times;
                </button>
                <h4 class="modal-title" id="title">任务恢复</h4>
            </div>
            <div class="modal-body">
                <div class="input-group form-inline">
                    <label class="input-group-addon control-label form-inline" for="jobVersion">Job版本</label>
                    <select id="selectJobVersionForDownRecovery" class="form-control"></select>

                    <label class="input-group-addon control-label form-inline" for="isReRun">是否回刷历史任务</label>
                    <select id="isReRun" class="form-control">
                        <option value=false selected="selected">否</option>
                        <option value=true>是</option>
                    </select>

                    <label class="input-group-addon control-label form-inline">开始日期</label>
                    <input class="form_datetime form-control" id="startDay" size="12" type="text" readonly
                           placeholder="请选择日期"/>

                    <label class="input-group-addon control-label form-inline">结束日期</label>
                    <input class="form_datetime form-control" id="endDay" size="12" type="text" readonly
                           placeholder="请选择日期"/>

                </div>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-default" data-dismiss="modal">取消</button>
                <button type="button" id="downRecoveryShowDag" class="btn btn-default">刷新依赖</button>
                <button type="button" id="downRecoveryRun" class="btn btn-info add-btn">执行</button>
            </div>

            <#--            <div class="modal-dialog fade modal-lg" &lt;#&ndash;style="display:none;height:0px"&ndash;&gt; id="jobDagModalForDownRecovery">-->
            <div class="modal-lg" <#--style="display:none;height:0px"--> id="jobDagModalForDownRecovery">
                <div class="modal-content">
                    <div class="modal-body">
                        <div class="row" style="margin: 0;">
                            <svg style="border: 3px solid dimgrey;height:700" class="col-lg-10" id="svgFD">
                                <g id="gFD"/>
                            </svg>
                            <textarea class="label-primary col-lg-2 col-sm-2 col-md-2" style="height: 400px;"
                                      id="jobDetailFD"
                                      readonly>任务信息</textarea>
                        </div>
                    </div>
                </div>
            </div>
            <#-- </div>-->


        </div>
    </div>
</div>


<div class="modal fade" id="scriptPreviewModal" tabindex="-1" role="dialog" aria-labelledby="addConfig"
     aria-hidden="true">
    <div class="modal-dialog" style="/*height:260px;*/width: 50%;">
        <div class="modal-content">

            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-hidden="true">
                    &times;
                </button>
                <br/>
                <h4 class="modal-title" id="title">&nbsp;脚本预览</h4>
            </div>

            <div class="modal-body">
                <textarea class="form-control" rows="32" id="scriptPreviewModalContent" wrap="off"
                          style="overflow-y: scroll;overflow-x: scroll;width: 100%;resize: vertical;word-break: break-all;"
                ></textarea>
                <br>
            </div>


            <div class="modal-footer">
                <button type="button" class="btn btn-default" data-dismiss="modal">返回</button>
                <#--<button type="button" class="btn btn-info add-btn">执行</button>-->
            </div>

        </div>
    </div>
</div>
</div>


<div class="modal fade" id="addJobModal" tabindex="-1" role="dialog" aria-labelledby="addJob"
     aria-hidden="true">
    <div class="modal-dialog" style="height:100px;">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-hidden="true">
                    &times;
                </button>
                <h4 class="modal-title">添加任务</h4>
            </div>
            <div class="modal-body">

                <div class="form-horizontal">
                    <div class="row">
                        <div class="col-sm-8 col-md-8 col-lg-8">
                            <div class="form-group">
                                <label class="control-label col-sm-4 col-lg-4 col-md-4">任务名称</label>
                                <div class="col-sm-8 col-lg-8 col-md-8 ">
                                    <input class="form-control" type="text" name="jobName">
                                </div>
                            </div>
                            <div class="form-group">
                                <label class="control-label col-sm-4 col-lg-4 col-md-4">任务类型</label>
                                <div class="col-sm-8 col-lg-8 col-md-8 ">
                                    <select class="form-control" name="jobType">
                                        <option value="shell" selected>shell脚本</option>
                                        <option value="hive">hive脚本</option>
                                        <option value="spark">spark脚本</option>
                                        <option value="impala">impala脚本</option>
                                        <#--没有权限控制，暂时就不开放了<option value="spark2">spark2脚本</option>-->
                                    </select>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
                <br>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-default" data-dismiss="modal">取消</button>
                <button type="button" class="btn btn-info add-btn" name="addBtn">添加</button>
            </div>
        </div>
    </div>
</div>
<div class="modal fade" id="addGroupModal" tabindex="-1" role="dialog" aria-labelledby="addGroupModal"
     aria-hidden="true">
    <div class="modal-dialog" style="height:100px;">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-hidden="true">
                    &times;
                </button>
                <h4 class="modal-title">添加组</h4>
            </div>
            <div class="modal-body">

                <div class="form-horizontal">
                    <div class="row">
                        <div class="col-sm-8 col-md-8 col-lg-8">
                            <div class="form-group">
                                <label class="control-label col-sm-4 col-lg-4 col-md-4">目录名称</label>
                                <div class="col-sm-8 col-lg-8 col-md-8 ">
                                    <input class="form-control" type="text" name="groupName">
                                </div>
                            </div>
                            <div class="form-group">
                                <label class="control-label col-sm-4 col-lg-4 col-md-4">目录类型</label>
                                <div class="col-sm-8 col-lg-8 col-md-8 ">
                                    <select class="form-control" name="groupType">
                                        <option value="0" selected>大目录</option>
                                        <option value="1">小目录</option>
                                    </select>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
                <br>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-default" data-dismiss="modal">取消</button>
                <button type="button" class="btn btn-info add-btn" name="addBtn">添加</button>
            </div>
        </div>
    </div>
</div>

<div class="modal fade" id="jobLog" tabindex="-1" role="dialog" aria-labelledby="jobLog" aria-hidden="true">
    <div class="modal-dialog" style="width: 90%">
        <div class="modal-content">
            <div class="modal-header">
                <h4 class="modal-title" id="myModalLabel">信息日志</h4>
                <#--<div id="log_content">11</div>-->
            </div>

            <div class="modal-body">
                <table class="table " id="runningLogDetailTable"></table>
            </div>


            <div class="modal-footer">
                <button type="button" class="btn btn-default" data-dismiss="modal">返回</button>
                <button type="button" class="btn btn-info add-btn" name="refreshLog">刷新</button>
            </div>
        </div>
    </div>
</div>

<div class="modal" id="uploadFile" tabindex="-1" role="dialog" aria-labelledby="title">
    <div class="modal-dialog" style="width: 600px">
        <div class="modal-content">

            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal">&times;</button>
                <div class="modal-title"><h4>上传文件</h4></div>
            </div>
            <div class="modal-body">
                <div id="responseResult" class="modal-title"></div>
            </div>

            <div class="modal-footer">
                <input multiple id="fileForm" name="fileForm" type="file" class="file-loading"
                >
                <br>
                <button class="btn btn-primary" id="overwriteScript">覆盖脚本</button>
                <button class="btn btn-primary" id="closeUploadModal">关闭</button>
            </div>
        </div>
    </div>
</div>


<div class="modal" id="editUploadFile" tabindex="-1" role="dialog"
     aria-labelledby="title"
     style="border-left: dashed;border-right: dashed">

    <div class="modal-dialog" style="width: 1000px">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal">×</button>
                <div class="modal-title">
                    <h4>编辑资源文件</h4>
                    </br>
                    </br>
                    <div class="col-md-3">
                        <div class="form-group">  <#--cols="10"-->
                            <textarea class="form-control" rows="20" id="edit-script-content" wrap="hard"
                                      style="overflow-y: scroll;overflow-x: scroll;width: 950px;resize: vertical; "></textarea>
                            <input type=button class="btn btn-default" value=脚本编辑区>
                        </div>
                    </div>
                </div>
            </div>
            <div class="modal-body" style="text-align:right">
            </div>
            <div class="modal-body" style="text-align:left">
                <div id="test-run-btn1-result" class="modal-title"></div>
            </div>
            <div class="modal-footer" style="text-align:right">
                <button type="button" class="btn btn-primary"
                        id="create-script-btn1">生成脚本
                </button>

                <button type="button" class="btn btn-primary"
                        id="overwrite-script-btn1">覆盖脚本
                </button>

                <button type="button" class="btn btn-primary"
                        data-dismiss="modal">关闭
                </button>
                <#-- <button type="button" class="btn btn-primary">
                     提交更改
                 </button>-->
            </div>
        </div>
    </div>
</div>
</div>

<div class="modal" id="selectDepend" tabindex="-1" role="dialog" aria-labelledby="title">
    <div class="modal-dialog" style="width: 600px">
        <div class="modal-content">

            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal"></button>
                <div class="modal-title"><h4>选择任务依赖任务</h4></div>
            </div>
            <div class="modal-body">
                <input type="text" class="form-control" id="dependKeyWords" placeholder="请输入关键词">
                <p id="deSearchInfo" style="display: none">查找中，请稍候...</p>
                <ul id="dependTree" class="ztree"></ul>
            </div>

            <div class="modal-footer">
                <button class="btn btn-primary" id="chooseDepend">确定</button>
            </div>
        </div>
    </div>
</div>
<div class="modal" id="addAdminModal" tabindex="-1" role="dialog" aria-labelledby="title">
    <div class="modal-dialog" style="width: 600px">
        <div class="modal-content">

            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal"></button>
                <div class="modal-title"><h4>配置管理员</h4></div>
            </div>
            <div class="modal-body">
                <select id="userList" class="selectpicker form-control" multiple data-done-button="true">

                </select>
            </div>

            <div class="modal-footer">
                <button class="btn btn-primary" name="submit" data-dismiss="modal">确定</button>
            </div>
        </div>
    </div>
</div>

<div class="modal" id="AddMonitorModal" tabindex="-1" role="dialog" aria-labelledby="title">
    <div class="modal-dialog" style="width: 600px">
        <div class="modal-content">

            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal"></button>
                <div class="modal-title"><h4>配置关注人员</h4></div>
            </div>
            <div class="modal-body">
                <select id="userListMonitor" class="selectpicker form-control" multiple data-done-button="true">

                </select>
            </div>

            <div class="modal-footer">
                <button class="btn btn-primary" name="submit" data-dismiss="modal">确定</button>
            </div>
        </div>
    </div>
</div>

<#--定时表达式模态框-->
<div class="modal fade" tabindex="-1" role="dialog" id="timeModal">
    <div class="modal-dialog" role="document">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span>
                </button>
                <h4 class="modal-title">构造定时表达式</h4>
            </div>
            <div class="modal-body">
                <form class="form-horizontal">
                    <div class="form-group">
                        <label for="inputMin" class="col-sm-2 control-label">分</label>
                        <div class="col-sm-8">
                            <input type="text" class="form-control" id="inputMin" placeholder="分">
                        </div>
                    </div>
                    <div class="form-group">
                        <label for="inputHour" class="col-sm-2 control-label">时</label>
                        <div class="col-sm-8">
                            <input type="text" class="form-control" id="inputHour" placeholder="时">
                        </div>
                    </div>
                    <div class="form-group">
                        <label for="inputDay" class="col-sm-2 control-label">天</label>
                        <div class="col-sm-8">
                            <input type="text" class="form-control" id="inputDay" placeholder="天">
                        </div>
                    </div>
                    <div class="form-group">
                        <label for="inputMonth" class="col-sm-2 control-label">月</label>
                        <div class="col-sm-8">
                            <input type="text" class="form-control" id="inputMonth" placeholder="月">
                        </div>
                    </div>
                    <div class="form-group">
                        <label for="inputWeek" class="col-sm-2 control-label">周</label>
                        <div class="col-sm-8">
                            <input type="text" class="form-control" id="inputWeek" placeholder="周">
                        </div>
                    </div>
                </form>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-primary" id="saveTimeBtn">确认</button>
            </div>
        </div><!-- /.modal-content -->
    </div><!-- /.modal-dialog -->
</div><!-- /.modal -->

<div class="response response-sch box box-success" id="responseCon">
    <p id="response"></p>
</div>

<div class="modal fade" tabindex="-1" role="dialog" id="jobDagModal">
    <div class="modal-dialog modal-lg" role="document" id="jobDagModalCon">
        <div class="modal-content">
            <div class="modal-header">
                <h3 class="box-title">任务链路图</h3>
                <div id="biggerBtn">
                    <i class="fa fa-plus"></i>
                </div>
            </div>
            <div class="modal-body">

                <form class="form-inline">

                    <div class="form-group">
                        <label for="itemw">任务ID:</label>
                        <input id="item" class="input-sm" style="width:80px; border: 1px solid #ccc;"/>
                        <input class="btn btn-primary" type="button" value="上游任务链" onclick="keypath1(0)"/>
                        <input class="btn btn-primary" type="button" value="下游任务链" onclick="keypath1(1)"/>
                    </div>
                    <div class="form-group">
                        <#--控制回车跳转bug,临时解决方案-->
                        <input id="item3333" class="input-sm" style="width:80px; border: 1px solid #ccc;"
                               hidden="hidden"/>
                        <input class="btn btn-primary disabled" type="button" id="expandAllFD" value="展示全部">
                    </div>

                </form>

                </br>

                <div class="row" style="margin: 0;">
                    <svg style="border: 3px solid dimgrey;height:700" class="col-lg-10" id="svg">
                        <g/>
                    </svg>
                    <textarea class="label-primary col-lg-2 col-sm-2 col-md-2" style="height: 400px;" id="jobDetail"
                              readonly>任务信息</textarea>
                    <#--<textarea class="label-primary col-lg-2 col-sm-2 col-md-2" style="height: 300px;" id="ColorMeaning"
                              readonly>颜色示意图</textarea>-->
                </div>
            </div>
        </div>
    </div>
</div>


<div class="modal fade" tabindex="-1" role="dialog" id="lineageDagModal">
    <div class="modal-dialog modal-lg" role="document" id="lineageDagModalCon">
        <div class="modal-content">
            <div class="modal-header">
                <h3 class="box-title">数据表血缘图</h3>
                <div id="biggerBtn" name="lineageBiggerBtn">
                    <i class="fa fa-plus"></i>
                </div>
            </div>
            <div class="modal-body">

                <form class="form-inline">

                    <div class="form-group">
                        <label for="itemw">表名:</label>
                        <input id="lineageItem" class="input-sm" style="width:260px; border: 1px solid #ccc;"
                               placeholder="输入库名.表名"/>
                        <#--  <input class="btn btn-primary" type="button" value="上游相关表" onclick="lineageKeypath1(0)"/>-->
                        <input class="btn btn-primary" type="button" id="syxgb" value="上游相关表" onclick="keypath2(0)"/>
                        <input class="btn btn-primary" type="button" id="xyxgb" value="下游相关表" onclick="keypath2(1)"/>
                    </div>
                    <div class="form-group">
                        <#--控制回车跳转bug,临时解决方案-->
                        <input id="item3333" class="input-sm" style="width:80px; border: 1px solid #ccc;"
                               hidden="hidden"/>
                        <input class="btn btn-primary disabled" type="button" id="lineageExpandAll" value="刷新">
                    </div>

                </form>

                </br>

                <div class="row" style="margin: 0;">
                    <svg style="border: 3px solid dimgrey;height:740;width: 100%" class="col-lg-10" id="svg1">
                        <g/>
                    </svg>
                    <#-- <textarea class="label-primary col-lg-2 col-sm-2 col-md-2" style="height: 400px;" id="jobDetail"
                               readonly>任务信息</textarea>-->
                    <#--<textarea class="label-primary col-lg-2 col-sm-2 col-md-2" style="height: 300px;" id="ColorMeaning"
                              readonly>颜色示意图</textarea>-->
                </div>
            </div>
        </div>
    </div>
</div>


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
<script src="${request.contextPath}/plugins/bootstrap-select/bootstrap-select.min.js"></script>
<script src="${request.contextPath}/js/taskGraph.js?v=2"></script>
<script src="${request.contextPath}/js/scheduleCenter.js"></script>
<script src="${request.contextPath}/js/common.js"></script>

<script src="${request.contextPath}/adminlte/bootstrap/js/bootstrap-datetimepicker.min.js"></script>
<script src="${request.contextPath}/adminlte/bootstrap/js/bootstrap-datetimepicker.zh-CN.js"></script>

</body>

<script type="text/javascript">
    $(".form_datetime").datetimepicker({
        format: "yyyy-mm-dd",
        autoclose: true,
        todayBtn: true,
        todayHighlight: true,
        language: 'zh-CN',//中文，需要引用zh-CN.js包
        startView: 2,//月视图
        minView: 2,//日期时间选择器所能够提供的最精确的时间选择视图
    });
</script>

<script>
    $(document).ready(function () {
        var time = new Date();
        var day = ("0" + time.getDate()).slice(-2);
        var month = ("0" + (time.getMonth() + 1)).slice(-2);
        var today = time.getFullYear() + "-" + (month) + "-" + (day);
        $(".form_datetime").val(today);
    })
</script>


</html>
