var msgHeight;

function successMsg(data) {
    if (window.screen.width <= 767) {
        msgHeight = 100;
    } else {
        msgHeight = 50;
    }
    if (data.success == true) {
        success(data.message);
        $("#table").bootstrapTable("refresh");
    } else {
        failure(data.message);
    }

}

function success(msg) {
    $('#alertSuccess').css({
        "width": 700,
        "right": ($(window).width() - 700) / 2,
        "display": "block"
    });
    $('#alertSuccess #successText').text(msg);
    $('#alertSuccess').animate({
        top: msgHeight
    }, 2000);
    $('#alertSuccess').animate({
        top: 0
    }, 2000, "linear", function () {
        $('#alertSuccess').css("display", "none");
    });
}

function failure(msg) {

    $('#alertFailure').css({
        "width": 500,
        "right": ($(window).width() - 500) / 2,
        "display": "block"
    });
    $('#alertFailure #failureText').text(msg);
    $('#alertFailure').animate({
        top: msgHeight
    }, 2000);
    $('#alertFailure').animate({
        top: 0
    }, 2000, "linear", function () {
        $('#alertFailure').css("display", "none");
    });
}

function dealCode(data) {
    if (data.code == 401) {
        location.href = "/";
    } else {
        alert("错误代码：" + data.code);
    }
}

function formDataLoad(domId, obj) {
    $("#" + domId)[0].reset();
    for (var property in obj) {
        if (obj.hasOwnProperty(property) == true) {
            if ($("#" + domId + " [name='" + property + "']").size() > 0) {
                $("#" + domId + " [name='" + property + "']").each(function () {
                    var dom = this;
                    if ($(dom).attr("type") == "radio") {
                        $(dom).filter("[value='" + obj[property] + "']").attr("checked", true);
                    } else if ($(dom).attr("type") == "checkbox") {
                        obj[property] == true ? $(dom).attr("checked", "checked") : $(dom).attr("checked", "checked").removeAttr("checked");
                    } else if ($(dom).attr("type") == "text" || $(dom).prop("tagName") == "SELECT" || $(dom).attr("type") == "hidden" || $(dom).attr("type") == "textarea") {
                        if (obj[property] == null || obj[property] == undefined) {
                            $("dom option:first").attr("selected", true)
                        } else {
                            $(dom).val(obj[property]);
                        }
                    } else if ($(dom).prop("tagName") == "TEXTAREA") {
                        $(dom).val(obj[property]);
                    } else {
                        $(dom).val(obj[property]);
                        $(dom).text(obj[property]);
                    }
                });
            }
        }
    }
}


/**
 * 判断value是否已经存在arr数据中
 *
 * @param arr
 * @param value
 * @returns {boolean}
 */
function isInArray(arr, value) {
    var b = false;
    if (arr == null) {
        return b;
    }
    for (var i = 0; i < arr.length; i++) {
        if (arr[i]['id'] == value['id']) {
            b = true;
            break;
        }
    }
    return b;
}

/**
 * 以post方式请求数据
 *
 * @param url
 * @returns {*}
 */

function getDataByPost(url) {
    var dataStore;
    $.ajax({
        type: "post",
        url: url,
        async: false,
        success: function (data) {
            dataStore = data;
        }
    });
    return dataStore;
}

/**
 * 以get方式获取数据
 * @param url
 * @param parameter
 * @returns {*}
 */
function getDataByGet(url, parameter) {
    var result = '';
    $.ajax({
        url: url,
        type: "get",
        async: false,
        data: parameter,
        success: function (data) {
            result = data;
        }
    });
    return result;
}

/**
 * 格式化日期
 * @param timestamp
 * @returns {string}
 */
function getLocalTime(timestamp) {
    if (timestamp == null) {
        return "";
    }
    var date = new Date(timestamp);
    var newDate = date.toLocaleDateString().replace(/\//g, "-") + " " + date.toTimeString().substr(0, 8);
    return newDate;
}


function uploadFile() {

    /* alert("$(\"#uploadFile\"):"+$("#uploadFile").val);*/

    $("#uploadFile").modal('show');

    $("#fileForm").fileinput({
        uploadUrl: base_url + "/uploadResource/upload",
        maxFileCount: 1,
        enctype: 'multipart/form-data',
        language: 'zh',
        allowedFileExtensions: ['py', 'jar', 'sql', 'hive', 'sh', 'js', 'txt', 'png', 'jpg', 'gif'],
        msgFilesTooMany: "选择上传的文件数量({n}) 超过允许的最大数值{m}！"
    }).on("fileuploaded", function (event, data) {
        var response = data.response;
        /*   alert("response:"+response.val)*/
        var message = response.message;
        console.log(data)
        var msg = "<b>" + "hadoop文件使用路径: " + message + "</b>";
        var na1 = message.substr(message.lastIndexOf("/") + 1);
        var name = na1.substr(0, na1.lastIndexOf("-"));
        name = na1.substr(0, name.lastIndexOf("-"));
        var suff = na1.substr(na1.lastIndexOf("."));
        var script = "";
        /*  alert("suff:"+suff);*/
        /*  alert("suff.equalsIgnoreCase(\".sh\"):"+(suff==".sh"));*/
        if (suff == ".sh") {
            script = "download[hdfs:///" + message.substring(message.indexOf("/hera") + 1) + " " + name + suff + "]" + "</br>"
                + "export JAVA_HOME=/usr/java/jdk1.8.0_144" + "</br>"
                + "sh " + name + suff + "";
        } else if (suff == ".sql") {
            script = "download[hdfs:///" + message.substring(message.indexOf("/hera") + 1) + " " + name + suff + "]" + "</br>"
                + "hive -f " + name + suff + ""
            /*   +"&nbsp;"+"|"+" "+"|";*/
        } else if (suff == ".hive") {
            script = "download[hdfs:///" + message.substring(message.indexOf("/hera") + 1) + " " + name + suff + "]" + "</br>"
                + "hive -f " + name + suff + "";
        } else if (suff == ".py") {
            script = "download[hdfs:///" + message.substring(message.indexOf("/hera") + 1) + " " + name + suff + "]" + "</br>"
                + "python3 " + name + suff + "";
        } else {
            script = "download[hdfs:///" + message.substring(message.indexOf("/hera") + 1) + " " + name + suff + "]" + "</br>"
                + "需自行指定 " + name + suff + "";
        }
        /*脚本填充*/
        /*var st8 = script.replace("</br>","\n").replace("</br>","\n").replace("</br>","\n");
         codeMirror.setValue(st8)*/

        if (response.success == false) {
            /*   $("#responseResult").html(msg);*/
            $("#responseResult").html(script);
        }
        if (response.success == true) {
            $("#responseResult").html(script);
            /*  $("#responseResult").html(msg);*/
        }
    }).on('filepredelete', function () {
            $("#responseResult").html("");
        }
    );
}


function closeUploadFile() {
    $("#closeUploadModal").click(function () {

        $("#uploadFile").modal('hide');

    });
}

window.onload = function () {
    getCurrentUser()
}

function getCurrentUser() {
    // alert("getCurrentUser ... start ...")
    let url = base_url + "/scheduleCenter/getCurrentUser";
    let parameter;
    $.get(url, parameter, function (data) {
        //    layer.msg(data);
        $("#getCurrentUser").text(data)
    });
    //  alert("getCurrentUser ... over ...")
}


$("#logoutBtn").click(function () {
    var url = base_url;
    clearAllCookie();
    window.location.href = url;
});

function clearAllCookie() {
    var keys = document.cookie.match(/[^ =;]+(?=\=)/g);
    if (keys) {
        for (var i = 0; i < keys.length; i++) {
            document.cookie = keys[i] + '=0;expires=' + new Date(0).toUTCString();
        }
    }
}

$('.my-tree').click(function (e) {
    $(this).addClass('menu-open');
})

$('#datalinkMintor').click(function () {
    //alert("666");
    $('#datalinkMintor').addClass('active');
    $('#datalinkMintor').parent().addClass('menu-open');
    $('#datalinkMintor').parent().parent().addClass('menu-open');
    $("#MintorCenter").addClass('active')
    window.location.href = "/hera/datalinkMintor";
})


$('#getCurrentUser').click(function () {

    if (!confirm("是否进入密码修改界面?")) {
        return;
    } else {
        //alert("进入密码修改界面")
        window.location.href = "/hera/changePassword";
    }
})











