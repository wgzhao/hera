layui.use("layer", function () {

    airBalloon('div.air-balloon');

    $('input').iCheck({
        checkboxClass: 'icheckbox_square-blue',
        radioClass: 'iradio_square-blue',
        increaseArea: '20%' // optional
    });

    jQuery.validator.addMethod("phone", function (value, element) {
        var length = value.length;
        var mobile = /^1[3|5|8]{1}[0-9]{9}$/;
        return this.optional(element) || (length == 11 && mobile.test(value));
    }, "请正确填写您的手机号码");
    var registerForm = $('#registerForm');
    registerForm.validate({
        rules: {
            name: {
                required: true,
                minlength: 3,
                maxlength: 18
            },
            password: {
                required: true,
                minlength: 5,
                maxlength: 18
            },
            confirmPassword: {
                required: true,
                equalTo: '#password'
            },
            email: {
                required: true,
                email: true
            },
            phone: {
                required: true,
                phone: true
            },
            description: {
                required: true,
                minlength: 10,
                maxlength: 200
            }
        },
        messages: {
            confirmPassword: {
                equalTo: "两次输入的密码必须一致"
            }
        },
        submitHandler: function () {
            $.post($("#baseURl").attr("href") + "/register", {
                name: $('#name').val(),
                password: hex_md5($('#password').val()),
                phone: $('#phone').val(),
                email: $('#email').val(),
                description: $('#description').val()
            }, function (data) {
                if (data.success === true) {
                    layer.open({
                        title: '系统提示',
                        content: data.message,
                        icon: '1'
                    });
                    registerForm[0].reset();
                } else {
                    layer.open({
                        title: '系统提示',
                        content: (data.message || "注册失败"),
                        icon: '2'
                    });
                }
            });
        }
    });

    $("#loginForm").validate({
        errorElement: 'span',
        errorClass: 'help-block',
        focusInvalid: true,
        rules: {
            userName: {
                required: true,
                maxlength: 18
            },
            password: {
                required: true,
                minlength: 4,
                maxlength: 18
            }
        },
        messages: {
            userName: {
                required: "请输入登录账号.",
                maxlength: "登录账号不应超过18位"
            },
            password: {
                required: "请输入登录密码.",
                minlength: "登录密码不应低于4位",
                maxlength: "登录密码不应超过18位"
            }
        },
        highlight: function (element) {
            $(element).closest('.form-group').addClass('has-error');
        },
        success: function (label) {
            label.closest('.form-group').removeClass('has-error');
            label.remove();
        },
        errorPlacement: function (error, element) {
            element.parent('div').append(error);
        },
        submitHandler: function () {
            $.post($("#baseURl").attr("href") + "/loginCheck", $("#loginForm").serialize(), function (data) {
                layer.msg(data.message);
                if (data.success === true) {
                    window.location.href = base_url + "/home";
                    ;
                }
            });
        }
    });
});


/*
 @function 热气球移动
 */
function airBalloon(balloon) {
    var viewSize = [], viewWidth = 0, viewHeight = 0;
    resize();
    $(balloon).each(function () {
        $(this).css({top: rand(40, viewHeight * 0.5), left: rand(10, viewWidth - $(this).width())});
        fly(this);
    });
    $(window).resize(function () {
        resize()
        $(balloon).each(function () {
            $(this).stop().animate({
                top: rand(40, viewHeight * 0.5),
                left: rand(10, viewWidth - $(this).width())
            }, 1000, function () {
                fly(this);
            });
        });
    });

    function resize() {
        viewSize = getViewSize();
        viewWidth = $(document).width();
        viewHeight = viewSize[1];
    }

    function fly(obj) {
        var $obj = $(obj);
        var currentTop = parseInt($obj.css('top'));
        var currentLeft = parseInt($obj.css('left'));
        var targetLeft = rand(10, viewWidth - $obj.width());
        var targetTop = rand(40, viewHeight / 2);
        /*求两点之间的距离*/
        var removing = Math.sqrt(Math.pow(targetLeft - currentLeft, 2) + Math.pow(targetTop - currentTop, 2));
        /*每秒移动24px ，计算所需要的时间，从而保持 气球的速度恒定*/
        var moveTime = removing / 24;
        $obj.animate({top: targetTop, left: targetLeft}, moveTime * 1000, function () {
            setTimeout(function () {
                fly(obj);
            }, rand(1000, 3000));
        });
    }
};