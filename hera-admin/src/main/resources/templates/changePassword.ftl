<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/html" xmlns="http://www.w3.org/1999/html">
<head>
    <title>任务调度平台</title>
    <#import "/common/common.macro.ftl" as netCommon>
    <@netCommon.commonStyle />
    <link rel="stylesheet" href="${request.contextPath}/css/userManage.css">
</head>

<style type="text/css">

</style>

<body class="hold-transition skin-black sidebar-mini">
<div class="wrapper">
    <!-- header -->
    <@netCommon.commonHeader />
    <!-- left -->
    <@netCommon.commonLeft "developCenter" />

    <div class="content-wrapper">
        <section class="content">
            <div class="box">
                <div class="box-header">
                    <h5 class="big-title">密码修改</h5>
                </div>
            </div>
            <br><br> <br>
            <div class="box-body">
                <#--                <div class="form-group" >-->
                <label class="control-label col-sm-1 col-lg-1 col-md-2" style="margin-left: 350px">账号:</label>
                <div class="col-sm-1 col-lg-3 col-md-2 ">
                    <input class="form-control" type="text" id="CPWDZH">
                </div>
                <br> <br> <br>
                <label class="control-label col-sm-1 col-lg-1 col-md-1" style="margin-left: 350px;">旧密码:</label>
                <tr>
                    <td>
                        <div class="col-sm-1 col-lg-3 col-md-2 ">
                            <input class="form-control" type="text" autocomplete="new-password" id="CPWDoldPWD"
                                   placeholder="输入密码..."

                            >
                        </div>
                    </td>
                </tr>


                <br> <br> <br>
                <label class="control-label col-sm-1 col-lg-1 col-md-1" style="margin-left: 350px">新密码:</label>
                <div class="col-sm-1 col-lg-3 col-md-2 ">
                    <input class="form-control" type="text" autocomplete="off" id="CPWDnewPWD" placeholder="输入新密码..."
                    >
                </div>

                <br> <br> <br>

                <label class="control-label col-sm-1 col-lg-1 col-md-1" style="margin-left: 350px">确认密码:</label>
                <div class="col-sm-1 col-lg-3 col-md-2 ">
                    <input class="form-control" type="text" id="CPWDsureNPWD" placeholder="重新输入新密码...">

                </div>

            </div>
            <br><br>


            <#-- </div>-->
            <#--    <div class="box-tools pull-right" style="margin-top: 20px;margin-right: 20px">-->
            <#--    <div class="modal-footer" style="text-align:left">-->
            <div <#--class="modal-footer"--> style="margin-left:570px">
                <button type="button" class="btn btn-primary"
                        id="CPWDQK">重新输入
                </button>
                &emsp;&emsp;&emsp;&emsp; &emsp;
                <button type="button" class="btn btn-primary"
                        id="CPWDSure">确认修改
                </button>

            </div>

        </section>


    </div>

    <@netCommon.commonScript />
    <script src="${request.contextPath}/js/userCenter.js"></script>
</body>

</html>


