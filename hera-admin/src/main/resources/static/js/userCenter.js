layui.use(['table', 'laytpl', 'form'], function () {

});

/*window.onload = function () {
    PWDgetCurrentUser()
}*/
PWDgetCurrentUser()

function PWDgetCurrentUser() {
    let url = base_url + "/scheduleCenter/getCurrentUser";
    let parameter;
    $.get(url, parameter, function (data) {
        //alert("账号 ："+data)
        /* $("#CPWDoldPWD").val("")*/
        $("#CPWDZH").attr("readOnly", "false");
        $("#CPWDZH").val(data)
        $("#CPWDZH").attr("readOnly", "true");
    });
}


$('#CPWDSure').click(function (e) {

    if ($("#CPWDoldPWD").val().length == 0) {
        alert("请输入原始密码")
        return;
    }
    if ($("#CPWDnewPWD").val().length == 0) {
        alert("请输入新密码");
        return;
    }
    if ($("#CPWDsureNPWD").val().length == 0) {
        alert("请重新输入新密码");
        return;
    }
    if ($("#CPWDnewPWD").val().length < 4 || $("#CPWDsureNPWD").val().length < 4) {
        alert("密码长度不能少于4位");
        return;
    }

    if ($("#CPWDnewPWD").val().length > 18 || $("#CPWDsureNPWD").val().length > 18) {
        alert("密码长度不能超过18位");
        return;
    }
    if ($("#CPWDnewPWD").val() !== $("#CPWDsureNPWD").val()) {
        alert("新密码两次输入不一致");
        return;
    }


    let url = base_url + "/scheduleCenter/changPWD";
    let parameter = "user=" + $("#CPWDZH").val() + "&pwd=" + $("#CPWDoldPWD").val()
        + "&newP1=" + $("#CPWDnewPWD").val() + "&newP2=" + $("#CPWDsureNPWD").val();
    $.get(url, parameter, function (data) {
        alert(data)
        if (data == "密码修改成功") {
            window.location.href = base_url + "/login";
        }
    });
})


$('#CPWDQK').click(function (e) {
    $("#CPWDoldPWD").val("");
    $("#CPWDnewPWD").val("");
    $("#CPWDsureNPWD").val("");
})


$('#CPWDoldPWD').click(function (e) {

})

$('#CPWDnewPWD').click(function (e) {

})

$('#CPWDsureNPWD').click(function (e) {

})