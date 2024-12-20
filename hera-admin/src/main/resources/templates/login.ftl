<!DOCTYPE html>
<html>
<head lang="en">
    <title>任务调度平台</title>
    <base href="${request.contextPath}" id="baseURl">
    <#import "/common/common.macro.ftl" as netCommon>
    <@netCommon.commonStyle />
    <link rel="stylesheet" href="${request.contextPath}/adminlte/plugins/iCheck/square/green.css">

    <!-- 页面logo设置 start-->
    <link rel="icon" type="image/png" href="${request.contextPath}/images/favicon.ico">
    <!-- 页面logo设置 end -->
    <!-- 页面样式设置，使用bootstrap前端框架 start-->
    <link rel="stylesheet" href="${request.contextPath}/css/login.css"/>
    <!-- 页面样式设置，使用bootstrap前端框架 end-->
    <!-- 引入JQuery库 start -->
    <style>
        .error {
            color: red;
        }
    </style>
</head>


<body>


<div class="login box box-primary">
    <#--<div class="box png">-->
    <div><h3 align="center" class="title">任务调度平台</h3></div>
    <div class="input">
        <div class="log">
            <ul class="nav nav-tabs" role="tablist" id="menu-tab">
                <li class="active"><a href="#tab-login" role="tab" data-toggle="tab">登录</a></li>
                <li><a href="#tab-middle" role="tab-register" data-toggle="tab">注册</a></li>
                <#--  <li><a href="#change-tab-middle" role="tab-register" data-toggle="tab">修改</a></li>-->
            </ul>

            <div class="tab-content">

                <div class="tab-pane active" id="tab-login">
                    <form id="loginForm" method="post">
                        <div class="name" style="margin: 20px 10px">
                            <label>用户名</label><input type="text" class="text" name="userName" placeholder="用户名"/>
                        </div>
                        <div class="pwd" style="margin: 20px 10px">
                            <label>密　码</label><input type="password" class="text" name="password" placeholder="密码"/>
                        </div>
                        <#--   <div>
                               <label class="col-sm-offset-7 col-sm-5" style="font-size: smaller;color: #8a8a8a;font-weight: 400">修改密码</label>
                           </div>-->
                        <button type="submit" class="btn btn-primary btn-block btn-flat" style="border-radius: 4px">
                            登录
                        </button>
                    </form>
                </div>

                <div class="tab-pane" id="tab-middle">
                    <p class="login-box-msg text-center">提醒：hera账号与hive账号必须相同</p>

                    <form action="" type="post" id="registerForm">
                        <fieldset>
                            <div class="form-group">
                                <label for="name">账号</label>
                                <input type="text" class="form-control" name="name" id="name">
                            </div>
                            <div class="form-group">
                                <label for="name">密码</label>
                                <input type="password" class="form-control" name="password"
                                       id="password">
                            </div>
                            <div class="form-group">
                                <label for="name">确认密码</label>
                                <input type="password" class="form-control" name="confirmPassword"
                                       id="confirmPassword">
                            </div>
                            <div class="form-group">
                                <label for="name">邮箱</label>
                                <input type="text" class="form-control" name="email" id="email">
                            </div>
                            <div class="form-group">
                                <label for="name">手机</label>
                                <input type="text" class="form-control" name="phone" id="phone">
                            </div>
                            <div class="form-group">
                                <label for="name">账号描述</label>
                                <input type="text" class="form-control" name="description"
                                       id="description">
                            </div>
                            <input type="reset" class="btn btn-default pull-left" value="重置">
                            <input type="submit" class="btn btn-primary pull-right" value="注册">
                        </fieldset>
                    </form>

                </div>

                <div class="tab-pane" id="change-tab-middle">
                    <#-- <p class="login-box-msg text-center">提醒：hera账号与hive账号必须相同</p>-->
                    <form action="" type="post" id="changeForm">
                        <fieldset>
                            <br/>
                            <div class="form-group">
                                <label for="name">账号</label>
                                <input type="text" class="form-control" name="name" id="name">
                            </div>
                            <div class="form-group">
                                <label for="name">旧密码</label>
                                <input type="password" class="form-control" name="oldPassword"
                                       id="oldPassword">
                            </div>

                            <div class="form-group">
                                <label for="name">新密码</label>
                                <input type="password" class="form-control" name="password"
                                       id="password">
                            </div>
                            <div class="form-group">
                                <label for="name">确认新密码</label>
                                <input type="password" class="form-control" name="confirmPassword"
                                       id="confirmPassword">
                            </div>
                            <#--<div class="form-group">
                                <label for="name">邮箱</label>
                                <input type="text" class="form-control" name="email" id="email">
                            </div>
                            <div class="form-group">
                                <label for="name">手机</label>
                                <input type="text" class="form-control" name="phone" id="phone">
                            </div>
                            <div class="form-group">
                                <label for="name">账号描述</label>
                                <input type="text" class="form-control" name="description"
                                       id="description">
                            </div>-->
                            <input type="reset" class="btn btn-default pull-left" value="重置">
                            <input type="submit" class="btn btn-primary pull-right" value="确认修改">
                        </fieldset>
                    </form>

                </div>

            </div>

        </div>
    </div>
    <#--</div>-->

</div>

<!-- /.login-box -->

<@netCommon.commonScript />
<script src="${request.contextPath}/plugins/jquery/jquery.validate.min.js"></script>
<script src="${request.contextPath}/plugins/jquery/jquery.metadata.js"></script>
<script src="${request.contextPath}/adminlte/plugins/iCheck/icheck.min.js"></script>
<script src="${request.contextPath}/plugins/jquery/messages_zh.js"></script>
<script src="${request.contextPath}/plugins/jquery/md5.js"></script>
<script src="${request.contextPath}/js/login.js"></script>
<script src="${request.contextPath}/js/fun.base.js"></script>
</body>
</html>
