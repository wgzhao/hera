layui.use(['table', 'laytpl', 'form', 'laydate'], function () {

    let metaData = $('#metaData');
    metaData.parent().addClass('menu-open');
    metaData.parent().parent().addClass('menu-open');
    metaData.addClass('active');
    $('#bigdataMetadata').addClass('active');
    let table = layui.table, laytpl = layui.laytpl, form = layui.form, laydate = layui.laydate;
    let dateTime, queryData;


    function judgeFieldStatus(v) {
        if (v !== "" && v !== undefined && v !== null) {
            return true;
        } else {
            return false;
        }
    };

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
    let dateFlag = true


    $("#metaBt").click(function () {
        layer.open({
            anim: 2,
            title: '业务规则新增',
            type: 1,
            area: ['700px', '600px'],
            content: $('#metaDataForm'),
            closeBtn: 1
        });
    });

    function getTableInfo(dateVal) {
        /**
         　　* @Description: TODO 初始化table数据
         　　* @param
         　　* @return
         　　* @throws
         　　* @author lenovo
         　　* @date 2019/8/21 15:11
         　　*/
        if (dateVal != undefined) {
            dateTime = dateVal
        }
        /*if (dateFlag) {
            dateTime = getDate()
            $("#dateTime").val(getDate())
            dateFlag = false
        }*/
        queryData = $("#queryData").val();
        table.render({
            elem: '#metaDataTable'
            , height: "full"
            , url: base_url + '/bigdataMetadata/selectMetaData'
            , where: {
                dt: dateTime
                , queryData: queryData
            }
            , page: {
                curr: 1
                , limits: [10, 50, 100]
            }
            , cols: [[ //表头
                {field: 'id', title: '序列', fixed: 'left', align: 'center', type: 'numbers'}
                , {field: 'ruleName', title: '规则名称', align: 'center', edit: 'text'}
                , {field: 'ruleStat', title: '统计规则', align: 'center', edit: 'text'}
                , {
                    field: 'ruleSql', title: '规则详情', align: 'center', edit: 'text'
                }
                , {field: 'bizDesc', title: '业务描述', align: 'center', edit: 'text'}
                , {field: 'bizAff', title: '业务归属', align: 'center', edit: 'text', sort: true}
                , {
                    field: 'status', title: '规则状态', align: 'center', templet: function (d) {
                        if (d.status == 1) {
                            return "有效"
                        } else {
                            return "无效"
                        }
                    }, edit: 'text'
                }
                , {field: 'creator', title: '创建者', align: 'center'}
                , {field: 'mender', title: '维护者', align: 'center'}
                , {field: 'createTime', title: '创建时间', align: 'center'}
                , {field: 'updateTime', title: '修改时间', align: 'center'}
            ]]

        });
    }


    /**
     *
     */

    table.on('edit(metaDataTable)', function (obj) {
        layer.open({
            skin: 'wyd-class',
            content: '更新提示',
            btn: ['确认更新', '取消更新'],
            yes: function (index, layero) {
                let jsonData = JSON.stringify(obj.data);
                $.ajax({
                    url: base_url + "/bigdataMetadata/updateMetaDataTable",
                    data: jsonData,
                    dataType: 'json',
                    contentType: "application/json",
                    type: "post",
                    success: function (data) {
                        if (data.success == true) {
                            layer.msg(data.message)
                        } else {
                            layer.msg(data.message)
                        }
                    }
                });
                layer.close(index); //如果设定了yes回调，需进行手工关闭
            },
            no: function (index, layero) {
                layer.close(index)
            }
        });
    });


    //监听提交
    form.on('submit(metaDataSub)', function (data) {
        let jsonData = JSON.stringify(data.field);
        $.ajax({
            url: base_url + "/bigdataMetadata/insertHeraMetaDataTable",
            data: jsonData,
            dataType: 'json',
            contentType: "application/json",
            type: "post",
            success: function (data) {
                if (data.success == true) {
                    layer.closeAll()
                    getTableInfo()
                    layer.msg(data.message)
                } else {
                    layer.msg(data.message)
                }
            }
        });
        return false;
    });

    table.on('sort(metaDataTable)', function (obj) { //注：tool是工具条事件名，test是table原始容器的属性 lay-filter="对应的值"
        console.log(obj.field); //当前排序的字段名
        console.log(obj.type); //当前排序类型：desc（降序）、asc（升序）、null（空对象，默认排序）
        console.log(this); //当前排序的 th 对象
        table.reload('metaDataTable', {
            initSort: obj //记录初始排序，如果不设的话，将无法标记表头的排序状态。 layui 2.1.1 新增参数
            , where: { //请求参数（注意：这里面的参数可任意定义，并非下面固定的格式）
                field: obj.field //排序字段   在接口作为参数字段  field order
                , order: obj.type //排序方式   在接口作为参数字段  field order
            }
        });
    });
    getTableInfo();
    laydate.render({
        /**
         　　* @Description: TODO 初始化时间插件
         　　* @param
         　　* @return
         　　* @throws
         　　* @author lenovo
         　　* @date 2019/8/21 15:11
         　　*/
        elem: '#dateTime'
        , range: true
        , done: function (value, date) {
            console.log("----------------dateValue" + value)
            getTableInfo(value);
        }
    });

    /*table.on('tool(metaDataTable)', function (obj) {
        var rowData = obj.data //获得当前行数据
        var layEvent = obj.event; //获得 lay-event 对应的值
        $("#ruleSqlText").text(rowData.ruleSql)
        if (layEvent === 'elipsis') {
            layer.open({
                title: '业务规则新增',
                type: 1,
                area: ['700px', '600px'],
                content: $('#metaSql'),
                closeBtn: 1
            });
        }
    });*/

    $(function () {
        document.onkeydown = function (e) {
            var ev = document.all ? window.event : e;
            if (ev.keyCode == 13) {
                getTableInfo();
            }
        }
    });
});


