layui.use(['table', 'laytpl', 'form', 'laydate'], function () {

    let maxwellInfo = $('#maxwellInfo');
    maxwellInfo.parent().addClass('menu-open');
    maxwellInfo.parent().parent().addClass('menu-open');
    maxwellInfo.addClass('active');
    $('#MintorCenter').addClass('active');
    let table = layui.table, laytpl = layui.laytpl, form = layui.form, laydate = layui.laydate;

    let dateTime, queryData;


    layui.use(['laydate'], function () {
        let laydate = layui.laydate
        laydate.render({
            elem: '#timePoint'
            , min: -6
            , max: 0
            , done: function (value, date) {
                getNginxLineInfo(value);
            }
        });
    })


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

    let dateStr = getDate();
    //初始化时间插件时间(当天时间)
    $("#timePoint").val(dateStr);

    function lineRow(name, data) {
        this.name = name;
        this.type = 'line';
        this.smooth = 0.3
        this.data = data;
        this.markPoint = {
            data: [
                {type: 'max', name: '最大值'},
                {type: 'min', name: '最小值'}
            ]
        };
    }

    function boom(date) {
        this.startValue = date
        this.start = 80
        this.end = 100
    }

    function initOption() {
        option.series = [];
        option.dataZoom = [];
        option.title = {};
        option.legend = {};
        option.xAxis = {};
        option.tooltip = {
            trigger: 'axis',
            axisPointer: {            // 坐标轴指示器，坐标轴触发有效
                type: 'shadow'        // 默认为直线，可选为：'line' | 'shadow'
            }
        };
    }

    var option = {
        title: {
            show: true
        },
        tooltip: {
            trigger: 'axis',
            axisPointer: {            // 坐标轴指示器，坐标轴触发有效
                type: 'shadow'        // 默认为直线，可选为：'line' | 'shadow'
            }
        },
        legend: {
            data: [],
            selected: {}
        },
        grid: {
            x: 100,
            x2: 100,
        },
        toolbox: {
            show: false,
            feature: {
                mark: {show: true},
                dataView: {show: true, readOnly: false},
                magicType: {show: true, type: ['line', 'bar', 'stack', 'tiled']},
                restore: {show: true},
                saveAsImage: {show: true}
            }
        },
        calculable: true,
        xAxis: [],
        yAxis: [],
        dataZoom: [],
        series: []
    };


    function initNginxLine(data) {
        /**
         　　* @Description: TODO 初始化nginx并发折线图配置
         　　* @param
         　　* @return
         　　* @throws
         　　* @author lenovo
         　　* @date 2020/2/25 14:25
         　　*/
        initOption();
        option.xAxis.boundaryGap = false;
        option.xAxis.type = "category";
        option.legend.data = ['并发数'];

        option.yAxis = {
            type: 'value'
        };

        var xData = new Array();
        var activeNums = new Array();   //并发数

        data.forEach(function (one, index) {
            xData[index] = one.createTime;
            activeNums[index] = one.activeConnections;
        })
        option.xAxis.data = xData;
        option.series[0] = new lineRow('并发数', activeNums);
        if (data.length != 0) {
            option.dataZoom[0] = new boom(dateStr)
        } else {
            delete option.dataZoom;
        }
        var myChart = echarts.init(document.getElementById('nginxLine'));
        myChart.setOption(option, true)
    }


    function getNginxLineInfo(date) {
        /**
         　　* @Description: TODO  调取后台接口查询nginx并发历史数据
         　　* @param
         　　* @return
         　　* @throws
         　　* @author lenovo
         　　* @date 2020/2/25 14:23
         　　*/
        if (date != undefined && date != '' && date != null) {
            dateStr = date
        } else {
            dateStr = getDate()
        }
        jQuery.ajax({
            url: base_url + "/offlineTaskMonitoring/selectHeraNginxInfoList",
            type: "get",
            data: "createTime=" + dateStr,
            success: function (data) {
                if (data.success == false) {
                    layer.msg(data.message);
                    console.log(data.message)
                    return;
                }
                initNginxLine(data.data);
            }
        });
    }

    getNginxLineInfo(dateStr)


    function getTableInfoMaxwell() {
        /**
         　　* @Description: TODO 初始化table数据
         　　* @param
         　　* @return
         　　* @throws
         　　* @author lenovo
         　　* @date 2019/8/21 15:11
         　　*/
        table.render({
            elem: '#maxwellPage'
            , height: "full"
            , url: base_url + '/selectMaxWellMonitorInfo'
            , page: {
                curr: 1
                , limits: [10]
            }
            , cols: [[ //表头
                {field: 'id', title: '序列', fixed: 'left', align: 'center', type: 'numbers'}
                , {field: 'server_id', title: 'maxwell任务ID', align: 'center'}
                , {field: 'client_id', title: 'maxwell任务名称', align: 'center'}
                , {field: 'messages_succeede', title: '成功发送条数', align: 'center'}
                , {field: 'messages_failed', title: '发送失败条数', align: 'center'}
                , {field: 'row_count', title: '已处理binlog条数', align: 'center'}
                , {field: 'publish_time', title: '消息发送时间(秒/条)', align: 'center'}
                , {field: 'binlog_file', title: 'binlog文件名称', align: 'center'}
                , {field: 'binlog_position', title: 'binlog偏移量', align: 'center'}
            ]]
        });
    }

    function getTableNginxPage() {
        /**
         　　* @Description: TODO 初始化table数据
         　　* @param
         　　* @return
         　　* @throws
         　　* @author lenovo
         　　* @date 2019/8/21 15:11
         　　*/
        table.render({
            elem: '#nginxPage'
            , height: "full"
            , url: base_url + '/selectNginxStatus'
            , page: {
                curr: 1
                , limits: [10]
            }
            , cols: [[ //表头
                {field: 'id', title: '序列', fixed: 'left', align: 'center', type: 'numbers'}
                , {field: 'activeConnections', title: '活动连接数', align: 'center'}
                , {field: 'reading', title: '读取客户端连接数', align: 'center'}
                , {field: 'writing', title: '响应到客户端连接数', align: 'center'}
                , {field: 'waiting', title: '驻留连接数', align: 'center'}
            ]]
        });
    }

    //getTableInfoMaxwell();
    getTableNginxPage();

});


