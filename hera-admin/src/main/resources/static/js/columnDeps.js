//获取显示器的高度
var height = window.screen.height;
console.log("显示器高度:" + height);
//设置mind容器的高度，使其自适应
$("#jsmind_container").attr("style", "border: 3px solid dimgrey;height: " + height * 0.80 + "px")
$("#columnDepsDetail").attr("style", "border: 3px solid dimgrey;height: " + height * 0.80 + "px")

var tips;
$(document).ready(function () {
    $('#columnDeps').addClass('active');
    $('#columnDeps').parent().addClass('menu-open');
    $('#columnDeps').parent().parent().addClass('menu-open');
    $('#consanguinityManage').addClass('active');

    //获取表的来源 指定表名(必须有) 字段名(可有可无)
    $('#getColumnDeps').on('click', function () {
        var tableName = $("#tableName").val().trim();
        var field = $("#field").val().trim();
        if (tableName === "") {
            layer.msg("必须有表名")
        } else {
            var json;
            if (field === "") {
                json = {tableName: tableName};
            } else {
                json = {tableName: tableName, field: field};
            }
            $.ajax({
                url: base_url + "/columnDeps/getSource",
                type: "get",
                data: json,
                success: function (reselt) {
                    var msg = reselt.message;
                    if (msg === "success") {
                        //鼠标滚动缩放
                        $(".jsmind-inner").on("mousewheel", function () {
                            var attr = $(".jsmind-inner").attr("class");
                            var zoom = parseInt(this.style.zoom, 10) || 100;
                            zoom += event.wheelDelta / 50;
                            if (zoom > 0) this.style.zoom = zoom + '%';
                            return false;
                        })
                        var data = reselt.data;
                        //展示思维导图
                        createDepsView(data, 2);
                    } else {
                        layer.msg(msg)
                    }
                }
            })
        }
    })
})

//创建视图容器
var options = {
    container: 'jsmind_container',//[必选] 容器的ID
    theme: 'primary',             // 主题
    view: {
        engine: 'svg',        // 思维导图各节点之间线条的绘制引擎
        hmargin: 100,         // 思维导图距容器外框的最小水平距离
        vmargin: 50,          // 思维导图距容器外框的最小垂直距离
        line_width: 2,        // 思维导图线条的粗细
        line_color: '#555',   // 思维导图线条的颜色
        draggable: true       // 当容器不能完全容纳思维导图时，是否允许拖动画布代替鼠标滚动
    },
    layout: {
        hspace: 30,          // 节点之间的水平间距
        vspace: 20,          // 节点之间的垂直间距
        pspace: 13           // 节点收缩/展开控制器的尺寸
    }
};
var jm = new jsMind(options);

//构建并展示视图的方法
function createDepsView(data, num) {
    var mind = {
        "meta": {
            "name": "online",
            "author": "songzhou@gushi.com",
            "version": "1.0"
        },
        "format": "node_array",
        "data": data
    };
    if (num === 1) {
        jm.enable_edit()
    }
    if (num === 2) {
        jm.disable_edit();
    }
    jm.show(mind);
}

//文本提示对象
layui.use('layer', function () {
    var layer = layui.layer;
});

//显示节点信息
function doExtend(property) {
    var attr = $(property).attr("value").trim();
    var table = attr.substr(0, attr.lastIndexOf("."));
    var field = attr.substr(attr.lastIndexOf(".") + 1, attr.length);
    $.ajax({
        url: base_url + "/columnDeps/getFieldInfo",
        type: "get",
        data: {
            tableName: table,
            field: field
        },
        success: function (reselt) {
            var msg = reselt.message;
            if (msg === "success") {
                var tableComment = reselt.data.tableComment.trim();
                var fieldType = reselt.data.fieldType.trim();
                var fieldComment = reselt.data.fieldComment.trim();
                tableComment = tableComment === "" ? "-" : "\n" + tableComment;
                fieldType = fieldType === "" ? "-" : fieldType;
                fieldComment = fieldComment === "" ? "-" : "\n" + fieldComment;
                var text = "血缘信息:\n表名称:\n" + table +
                    "\n\n表注释:" + tableComment +
                    "\n\n字段名称:" + field +
                    "\n\n字段类型:" + fieldType +
                    "\n\n字段注释:" + fieldComment
                $("#columnDepsDetail").text(text)
            } else {
                layer.msg(msg)
            }
        }
    })
}

//双击搜索字段血缘
function getColumnByNode(property) {
    var value = $(property).attr("value");
    var table = value.substr(0, value.lastIndexOf("."));
    var field = value.substr(value.lastIndexOf(".") + 1, value.length);
    $.ajax({
        url: base_url + "/columnDeps/getTarget",
        type: "get",
        data: {
            tableName: table,
            field: field
        },
        success: function (reselt) {
            var msg = reselt.message;
            if (msg === "success") {
                var data = reselt.data;
                createDepsView(data, 1);
            } else {
                layer.msg(msg)
            }
        }
    })
}