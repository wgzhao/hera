layui.use(['table', 'laytpl', 'form', 'laydate'], function () {
    $('#GZGtagPush').addClass('active');
    $('#GZGtagPush').parent().addClass('menu-open');
    $('#GZGtagPush').parent().parent().addClass('menu-open');
    $('#bigdataMetadata').addClass('active');
    $('#tagPushManager').addClass('active');
    var laydate = layui.laydate;
    // let params;
//直接嵌套显示
//常规用法
    laydate.render({
        elem: '#runday'
        , min: -6
        , max: 0
        , value: getDate()
        , done: function () {
            updateTagPushTable()
        }
    });

    updateTagPushTable()

});


function params(params) {
    var temp = {
        tagTypeId: $('#tagManagerTagType').val(),
        isValid: $('#tagManagerIsValid').val()
    };
    return temp;
}

function updateTagPushTable() {


    //var tagType=$('#tagManagerTagType').val();

    $('#tagPushTable').bootstrapTable('destroy')
    var TableInit = function () {

        var oTableInit = new Object();
        oTableInit.init = function () {
            var table = $('#tagPushTable');
            table.bootstrapTable({
                url: base_url + '/GZGtagManageController/getAllTagInfo',
                method: 'get',
                queryParams: params,
                pagination: true,
                cache: false,
                clickToSelect: true,
                toolTip: "",
                striped: false,
                showRefresh: true,           //是否显示刷新按钮
                showPaginationSwitch: false,  //是否显示选择分页数按钮
                pageNumber: 1,              //初始化加载第一页，默认第一页
                pageSize: 15,                //每页的记录行数（*）
                pageList: [10, 15, 20, 40, 60, 80],
                search: true,
                uniqueId: 'tagId',
                sidePagination: "client",
                searchAlign: 'left',
                buttonsAlign: 'left',
                clickToSelect: true, //是否启用点击选中行
                showToggle: false,
                cardView: false,                    //是否显示详细视图
                detailView: false,                   //是否显示父子表
                fixedColumns: true,//是否固定列
                fixedNumber: 3,//固定多少列，从左边开始数

                onClickRow: function (row) {
                    //onDblClickRow: function (row) {

                    /* alert(row['sqlId'] + "   ok");
                     alert(row['tagId'] + "   ok");*/
                    //    initMachineNull();
                    flag = 0;
                    initMachine(row['sqlId'], row['tagId']);

                    // $('#ffff').modal('show');

                    layer.open({
                        anim: 2,
                        title: '标签任务修改',
                        type: 1,
                        area: ['60%', '97%'],
                        content: $('#TagDataForm'),
                        closeBtn: 1
                    });


                },

                //   rowStyle :rowStyle,
                /*
                    onClickCell:function(field, value, row, $element){
                                    alert(JSON.stringify(value));
                     },
                */

                columns: [
                    {
                        field: '',
                        title: '序号',
                        formatter: function (val, row, index) {
                            return index + 1;
                        },
                        width: "3%"
                    },

                    {
                        field: 'valid',
                        title: '状态',
                        sortable: true,
                        width: "5%",
                        formatter: function (val) {
                            if (val == '0') {
                                return '<a class="layui-btn layui-btn-xs" style="width: 100%;background-color:#FF0000;color: white;" >' + '下线' + '</a>';
                            } else {
                                return '<a class="layui-btn layui-btn-xs" style="width: 100%;background-color:#3CB371;color: white;" >' + '上线' + '</a>';
                                //  return  val ;
                            }
                        },
                    },

                    /*{
                        field: 'status',
                        title: '状态',
                        sortable: true
                    },*/
                    {
                        field: 'tagTypeId',
                        title: '标签分类id',
                        sortable: true,
                        width: "7%"
                    },
                    {
                        field: 'tagType',
                        title: '标签分类',
                        sortable: true,
                        width: "7%"
                    },
                    {
                        field: 'sqlId',
                        title: 'Sql Id',
                        /*  formatter: function (val) {
                              return '<a href = "#">' + val + '</a>';
                          },*/
                        width: "14%",
                        sortable: true
                    },

                    {
                        field: 'tagId',
                        title: '标签id',
                        formatter: function (val) {
                            return '<a href = "#">' + val + '</a>';
                        },
                        width: "12%",
                        sortable: true
                    },
                    {
                        field: 'tagName',
                        title: '标签名称',
                        formatter: function (val) {
                            return '<a href = "#">' + val + '</a>';
                        },
                        width: "18%",
                        sortable: true
                    },
                    {
                        field: 'instruction',
                        title: '计算口径',
                        width: "25%",
                        // sortable: true
                    },
                    {
                        field: 'operator',
                        title: '操作人',
                        width: "6%",
                        sortable: true
                    },
                    {
                        field: 'updateTime',
                        title: '更新时间',
                        formatter: function (val) {
                            if (val !== null && val.length >= 19) {
                                return val.substring(0, 19);
                            } else {
                                return val;
                            }
                        },
                        width: "10%",
                        sortable: true,
                    }
                ],
                // data:info.data
            });
        }
        // oTableInit= new Object();
        return oTableInit;
    }
    var oTable = new TableInit();
    oTable.init();
    $('#tagPushTable').bootstrapTable('hideLoading');
    // }
}

var rowStyle = function (row, index) {
    var classes = ['success', 'info', 'bg-blue', 'bg-red', 'no'];
    if (index % 2 === 0) {//偶数行
        return {classes: classes[4]};
    } else {//奇数行
        return {classes: classes[0]};
    }
};


function initMachine(sqlId, tagId) {

    $("#delete_tag2").show();

    $.ajax({
        // url: base_url + '/homePage/getAllWorkInfo',
        url: base_url + '/GZGtagManageController/getOldValue',
        type: 'post',
        data: {
            sqlId: sqlId,
            tagId: tagId
        },
        success: function (data) {

            var tagTypeId = data.data['tagTypeId']
            var tagId = data.data['tagId']
            var tagName = data.data['tagName']
            var sqlComment = data.data['sqlComment']
            var queryType = data.data['queryType']
            var instruction = data.data['instruction']
            var sqlId = data.data['sqlId']
            var valid = data.data['valid']

            // alert("1   valid : " + valid + "  |tagTypeId : " + tagTypeId + " |queryType : " + queryType)

            // $('#queryType').attr('readonly', false);

            $("#queryType").val(queryType);
            $('#isValid').val(valid);
            $('#tagTypeId').val(tagTypeId);

            //alert("2  valid : " + $('#isValid').val() + "  |tagTypeId : " + $('#tagTypeId').val()+ " |queryType : " + $('#queryType').val())


            $('#tagName').val(tagName);
            $('#sqlId').val(sqlId);
            $('#tagId').val(tagId);
            $('#sqlComment').val(sqlComment);
            $('#instruction').val(instruction);


            $('#sqlId').attr('readonly', true);


        }
    })
}

function initMachineNull() {

    $('#sqlId').attr('readonly', false);

    $('#isValid').val("");
    $('#tagTypeId').val("");
    $('#tagName').val("");
    $('#sqlId').val("");
    $('#tagId').val("");
    $('#sqlComment').val("");
    $('#queryType').val("");
    $('#instruction').val("");

}

function getDate() {
    /**
     　　* @Description: TODO 获取时间(yyyy-MM-dd)
     　　* @param
     　　* @return
     　　* @throws
     　　* @author lenovo
     　　* @date 2019/8/21 16:05
     　　*/
    var now = new Date();
    var year = now.getFullYear(); //得到年份
    var month = now.getMonth();//得到月份
    var date = now.getDate();//得到日期
    month = month + 1;
    if (month < 10) month = "0" + month;
    if (date < 10) date = "0" + date;
    var time = "";
    time = year + "-" + month + "-" + date;
    return time;
};

let flag = 0;

$("#newTagButton_1").click(function () {
    flag = 1;
    initMachineNull()
    $('#ffff').modal('show');
});

$("#newTagButton").click(function () {

    var permission = false;
    $.ajax({
        url: base_url + '/GZGtagManageController/checkPermission',
        type: 'post',
        data: {user: $("#getCurrentUser").text()},
        async: false,
        success: function (data) {
            if (data.success == true) {
                // layer.msg("有权");
                permission = true;
            }
        }
    });

    //  alert(permission);
    if (!permission) {
        layer.msg("抱歉,你没有修改权限 </br>如果需要更新标签,请联系管理员 ");
        return false;
    }

    //隐藏删除选项
    $("#delete_tag2").hide();

    flag = 1;
    initMachineNull()
    layer.open({
        anim: 2,
        title: '业务规则新增',
        type: 1,
        //   area: ['700px', '600px'],
        area: ['60%', '97%'],
        content: $('#TagDataForm'),
        closeBtn: 1
    });
});


function checkSqlId() {

    flag = 0
    //alert($('#sqlId').val())
    var sqlId = $('#sqlId').val()
    let data = {sqlId: sqlId}
    $.ajax({
        url: base_url + '/GZGtagManageController/checkSqlId',
        type: 'post',
        data: data,
        success: function (data) {

            if (data.success == true) {
                layer.msg("该sql id存在,显示已有sql语句");
                $('#sqlComment').val(data.data)
            } else {
                $('#sqlComment').val("")
            }
        }

    })
};

function isEmpty(obj) {
    if (typeof obj == "undefined" || obj == null || obj.trim() == "") {
        return true;
    } else {
        return false;
    }
}

String.prototype.trim = function () {
    return this.replace(/(^\s*)|(\s*$)/g, '');
}

$("#confirmUpdate").click(function () {

    $("#delete_tag2").show();

    var permission = false;
    $.ajax({
        url: base_url + '/GZGtagManageController/checkPermission',
        type: 'post',
        data: {user: $("#getCurrentUser").text()},
        async: false,
        success: function (data) {
            if (data.success == true) {
                // layer.msg("有权");
                permission = true;
            }
        }
    });

    //  alert(permission);
    if (!permission) {
        layer.msg("抱歉,你没有修改权限 </br>如果需要更新标签,请联系管理员 ");
        return false;
    }

    if (
        isEmpty($('#isValid').val())
        || isEmpty($('#tagTypeId').val())
        || isEmpty($('#tagName').val())
        || isEmpty($('#tagId').val())
        || isEmpty($('#instruction').val())
        || isEmpty($('#queryType').val())
        || isEmpty($('#sqlId').val())
        || isEmpty($('#sqlComment').val())
    ) {
        layer.msg("填写不完整");
        return false;
    } else {
        var str = "请联系管理员";
        let data = {sqlId: $('#sqlId').val()};
        $.ajax({
            url: base_url + '/GZGtagManageController/checkSqlId',
            type: 'post',
            data: data,
            async: false,
            success: function (data) {
                // alert(JSON.stringify(data))

                if (data.success == true) {
                    // layer.msg("该sql id存在,显示已有sql语句");
                    str = "重要操作,请慎重 !!! <br> 该sql id存在,确认修改: sqlId为" + $('#sqlId').val() + "的标签吗?<br>";
                } else {
                    str = "该sql id 不存在,确认新增: sqlId为" + $('#sqlId').val() + "的标签吗?<br>";
                }
            }
        });

        layer.confirm(str,
            {
                icon: 0,
                skin: 'msg-class',
                btn: ['确定', '取消'],
                anim: 0
            },
            function (index, layero) {

                //     $('#ffff').modal('show');

                var tdm = new Object()
                tdm.tagTypeId = $("#tagTypeId").val()
                tdm.tagName = $("#tagName").val()
                tdm.tagId = $("#tagId").val()
                tdm.instruction = $("#instruction").val()
                tdm.queryType = $("#queryType").val()
                tdm.sqlId = $("#sqlId").val()
                tdm.sqlComment = $("#sqlComment").val()
                tdm.operator = $("#getCurrentUser").text();
                tdm.valid = $("#isValid").val();
                tdm.updateTime = flag + "";
                // alert(JSON.stringify(tdm))

                /* $.ajax({
                     url: base_url + "/GZGtagManageController/repalceDimMan",
                     data: {
                         tdm: JSON.stringify(tdm),
                         //tdm: tdm,
                     },
                     dataType: "json",
                     contentType: "application/json",
                     async: true,
                     type: "post",
                     success: function (data) {
                         console.log(data)
                         if (data.success == false) {
                             alert(data.message);
                         }
                     }
                 })*/

                $.ajax({
                    url: base_url + "/GZGtagManageController/repalceDimMan",
                    data: JSON.stringify(tdm),
                    type: "post",
                    dataType: 'json',
                    contentType: "application/json;charset=utf-8",
                    success: function (data) {
                        if (data.success == false) {
                            layer.msg(data.message)
                        } else {
                            layer.msg(data.message + "  " + data.data)
                            return false;
                        }
                    }
                });
                //$('#TagDataForm').hide();
                //alert("1")
                layer.close(index)
                //updateTagPushTable()
            }, function (index) {
                //alert("2")
                //  return false;
                layer.close(index)
            });
    }
    flag = 0;

});


function checkSqlComment() {

    //alert($('#sqlId').val())
    var sqlComment = $('#sqlComment').val().toLowerCase();
    if (isEmpty(sqlComment)) {
        layer.msg("sql语句不能为空 ! ")
        return false;
    } else {
        var index1 = 0;
        if (sqlComment.indexOf("from") < 1) {
            index1 = sqlComment.length;
        } else {
            index1 = sqlComment.indexOf("from");
        }
        var sql = sqlComment.substring(0, index1);
        if (sql.indexOf("tag_id") == -1 || sql.indexOf("user_ids") == -1) {
            alert("sql语句的查询结果必须包含 tag_id 和 user_ids !")
        } else {
            // layer.msg(" sql语句校验正常 ")
        }
    }


};

function addSqlId() {

    $('#sqlId').attr('readonly', false);

    if (flag == 1) {
        //    alert("val  " + $("#tagTypeId").val())
        $('#sqlId').val("fz_" + $("#tagTypeId").val() + "_" + new Date().format("yyyyMMddhhmmss"));
    } else {
        return false;
    }


}

Date.prototype.format = function (fmt) {
    var o = {
        "M+": this.getMonth() + 1, //月份
        "d+": this.getDate(), //日
        "h+": this.getHours(), //小时
        "m+": this.getMinutes(), //分
        "s+": this.getSeconds(), //秒
        "q+": Math.floor((this.getMonth() + 3) / 3), //季度
        "S": this.getMilliseconds() //毫秒
    };
    if (/(y+)/.test(fmt)) fmt = fmt.replace(RegExp.$1, (this.getFullYear() + "").substr(4 - RegExp.$1.length));
    for (var k in o)
        if (new RegExp("(" + k + ")").test(fmt)) fmt = fmt.replace(RegExp.$1, (RegExp.$1.length == 1) ? (o[k]) : (("00" + o[k]).substr(("" + o[k]).length)));
    return fmt;
}


function sleep(delay) {
    for (var t = Date.now(); Date.now() - t <= delay;) ;
}