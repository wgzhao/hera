layui.use(['table', 'laytpl', 'form', 'laydate'], function () {

    let commonTab = $('#dataDiscoveryNew');
    commonTab.parent().addClass('menu-open');
    commonTab.parent().parent().addClass('menu-open');
    commonTab.addClass('active');
    $('#bigdataMetadata').addClass('active');
    let table = layui.table, laytpl = layui.laytpl, form = layui.form, laydate = layui.laydate;

    let dateTime, queryData
    let selectValStr
    let tableVar = "dwd_herafunc_officialbase_discovery_df"
    //初次页面时间加载标志
    let dateFlag = true


    /**
     * 获取时间
     * @param AddDayCount
     * @returns {string}
     * @constructor
     */
    function getAddDateStr(AddDayCount) {
        var dd = new Date();
        dd.setDate(dd.getDate() + AddDayCount);//获取AddDayCount天后的日期
        var y = dd.getFullYear();
        var m = dd.getMonth() + 1;//获取当前月份的日期
        var d = dd.getDate();
        if (m < 10) m = "0" + m;
        if (d < 10) d = "0" + d;
        return y + "-" + m + "-" + d;
    }

    /**
     * 得到当天初始化时间
     * @returns {string}
     */
    function getDate() {
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
    }


    function judgeFieldStatus(v) {
        if (v !== "" && v !== undefined && v !== null) {
            return true;
        } else {
            return false;
        }
    };

    /**
     *  根据tabVar值,改变tab状态
     * @param tabId
     */
    function changeTabColor() {
        if (tableVar == "dwd_herafunc_officialbase_discovery_df") {
            $('#tabOfficial').parent().addClass('active');
            $('#tabMiddle').parent().removeClass('active');
        } else if (tableVar == "dwd_herafunc_middlebase_discovery_df") {
            $('#tabOfficial').parent().removeClass('active');
            $('#tabMiddle').parent().addClass('active');
        }
    }

    $("#tabOfficial").click(function (e) {
        tableVar = "dwd_herafunc_officialbase_discovery_df"
        changeTabColor()
        selectTableInfo()

    })

    $("#tabMiddle").click(function (e) {
        tableVar = "dwd_herafunc_middlebase_discovery_df"
        changeTabColor()
        selectTableInfo()
    })


    /**
     * 选择表
     */
    function selectTableInfo() {
        if (tableVar == "dwd_herafunc_officialbase_discovery_df") {
            table.render({
                elem: '#dataDiscovery',
                height: "full",
                url: base_url + '/bigdataMetadata/selectDataDiscoveryNew',
                where: {
                    tableVar: tableVar,
                    dt: dateTime,
                    queryData: queryData,
                    isMiddleBase: selectValStr
                },
                page: {
                    curr: 1,
                    limits: [10, 50]
                },
                cols: [
                    [ //表头
                        //{field: 'id', title: '序列', fixed: 'left', align: 'center', type: 'numbers'}
                        {field: 'dt', title: '数据发现日期', align: 'center', width: 120, sort: true},
                        {field: 'databaseName', title: '数据库名称', align: 'center', width: 160},
                        {field: 'tableName', title: '表名称', align: 'center', width: 200},
                        {field: 'tableComment', title: '表注释', align: 'center', width: 150},
                        {field: 'changeType', title: '变更类型', align: 'center', width: 150},
                        {
                            field: 'changeDetail', title: '变更详情', align: 'center', templet: function (d) {
                                if (d.changeDetail == null || d.changeDetail == "null" || d.changeDetail == "") {
                                    return ""
                                } else {
                                    return '<div style = "text-align:left">' + d.changeDetail + '</div>'
                                }
                            }
                        },
                        {
                            field: 'isMiddleBase', title: '是否接入中间库', align: 'center', templet: function (d) {
                                if (d.isMiddleBase == 1) {
                                    return "是"
                                } else {
                                    return "否"
                                }
                            },
                            width: 130,
                            sort: true
                        },
                        {
                            field: 'isHiveBase', title: '是否接入hive库', align: 'center', templet: function (d) {
                                if (d.isHiveBase == 1) {
                                    return "是"
                                } else {
                                    return "否"
                                }
                            },
                            width: 130,
                            sort: true
                        }
                    ]
                ]
            });
        } else if (tableVar = "dwd_herafunc_middlebase_discovery_df") {
            table.render({
                elem: '#dataDiscovery',
                height: "full",
                url: base_url + '/bigdataMetadata/selectDataDiscoveryNew',
                where: {
                    tableVar: tableVar,
                    dt: dateTime,
                    queryData: queryData,
                    isHiveBase: selectValStr
                },
                page: {
                    curr: 1,
                    limits: [10, 50]
                },
                cols: [
                    [ //表头
                        //{field: 'id', title: '序列', fixed: 'left', align: 'center', type: 'numbers'}
                        {field: 'dt', title: '数据发现日期', align: 'center', width: 120, sort: true},
                        {field: 'databaseName', title: '数据库名称', align: 'center', width: 160},
                        {field: 'tableName', title: '表名称', align: 'center', width: 200},
                        {field: 'tableComment', title: '表注释', align: 'center', width: 150},
                        {field: 'changeType', title: '变更类型', align: 'center', width: 150},
                        {
                            field: 'changeDetail', title: '变更详情', align: 'center', templet: function (d) {
                                if (d.changeDetail == null || d.changeDetail == "null" || d.changeDetail == "") {
                                    return ""
                                } else {
                                    return '<div style = "text-align:left">' + d.changeDetail + '</div>'
                                }
                            }
                        },
                        {
                            field: 'isHiveBase', title: '是否接入hive库', align: 'center', templet: function (d) {
                                if (d.isHiveBase == 1) {
                                    return "是"
                                } else {
                                    return "否"
                                }
                            },
                            width: 130,
                            sort: true
                        }
                    ]
                ]
            });
        }
    }


    /**
     * 初始化时间
     */
    function initDate() {
        if (dateFlag) {
            dateTime = getAddDateStr(-2) + " - " + getAddDateStr(0)
            $("#dateTime").val(dateTime)
            dateFlag = false
        }
    }

    /**
     *  初始化table数据逻辑方法
     * @param dateVal 时间
     */
    function getTableInfo() {
        initDate()
        queryData = $("#queryData").val();
        selectVal = $("#selectStatus").val();
        selectTableInfo()
    }


    /**
     * 初始化时间插件
     */
    laydate.render({
        elem: '#dateTime'
        , range: true
        , done: function (value, date) {
            console.log("dateVal==========================" + value)
            dateTime = value
            getTableInfo();
        }
    });

    table.on('sort(dataDiscovery)', function (obj) { //注：tool是工具条事件名，test是table原始容器的属性 lay-filter="对应的值"
        console.log(obj.field); //当前排序的字段名
        console.log(obj.type); //当前排序类型：desc（降序）、asc（升序）、null（空对象，默认排序）
        console.log(this); //当前排序的 th 对象
        table.reload('dataDiscovery', {
            initSort: obj //记录初始排序，如果不设的话，将无法标记 表头的排序状态。 layui 2.1.1 新增参数
            , where: { //请求参数（注意：这里面的参数可任意定义，并非下面固定的格式）
                field: obj.field //排序字段   在接口作为参数字段  field order
                , order: obj.type //排序方式   在接口作为参数字段  field order
            }
        });
    });


    function setHeadWidth() {
        let width = $("#processMonitor").width();
        alert("========================" + width)
        $(".box-tools.pull-right").css("width", width)
        alert("======================================" + $(".box-tools.pull-right").width())
    }

    $(function () {
        //初始化
        changeTabColor()
        getTableInfo();
        document.onkeydown = function (e) {
            var ev = document.all ? window.event : e;
            if (ev.keyCode == 13) {
                getTableInfo();
            }
        }
        //监测下拉选择变动
        $("#selectStatus").change(function () {
            selectValStr = $("#selectStatus").val()
            if (selectValStr == "") {
                selectValStr = null
            }
            selectTableInfo()
        })
    });
});


