nodeIndex = {}
//文本提示对象
layui.use('layer', function () {
    var layer = layui.layer;
});
$(document).ready(function () {
        // keypath();
        $('#tableDeps').addClass('active');
        $('#tableDeps').parent().addClass('menu-open');
        $('#tableDeps').parent().parent().addClass('menu-open');
        $('#consanguinityManage').addClass('active');
    }
);
function lineageExpandNextNode(nodeNum) {
    while (nodeNum > 0) {
        if (currIndex < len) {
            let edge = edges[currIndex];
            if (addEdgeToTableGraph(edge)) {
                nodeNum--;
            }
            currIndex++;
        } else {
            layer.msg("已经全部展示完毕！");
            break;
        }
    }
    redraw();
}
function lineageInitDate(data) {
    edges = data.data.edges;
    headNode = data.data.headNode;
    len = edges.length;
    currIndex = 0;
    /* g = new dagreD3.graphlib.Graph().setGraph({});*/
    g = new dagreD3.graphlib.Graph().setGraph({rankdir: "LR"});//横向展示
    g.setNode(headNode.nodeName, {label: headNode.nodeName, style: "fill: #bd16ff" + ";" + headNode.remark})
    let nodeName;
    for (let i = 0; i < len; i++) {
        nodeName = edges[i].nodeA.nodeName;
        if (nodeIndex[nodeName] == null || nodeIndex[nodeName] == undefined || nodeIndex[nodeName] == 0) {
            nodeIndex[nodeName] = i + 1;
        }
    }
}
function keypath2(type) {
    //alert("keypath2(type)" + type)
    if (!$("#lineageItem")[0].value.length > 0) {
        layer.msg("请输入表名");
        return;
    }
    layer.msg("开始查询...");
    $('#lineageExpandAll').removeClass('active').addClass('disabled');
    $('#syxgb').removeClass('active').addClass('disabled');
    $('#xyxgb').removeClass('active').addClass('disabled');

    graphType = type;
    let node = $("#lineageItem")[0].value;
    //alert("node :" + node);
    //let node = "1";
    if (node == "") {
        return;
    }

    let url = base_url + "/dag/tableRelation";
    let data = {tabName: node, type: type};
    //alert("type  " + type)
    let success = function (data) {

        // Create a new directed com.dfire.graph
        if (data.success == false) {
            alert("不存在该任务节点");
            return;
        }

        //initDate(data);
        lineageInitDate(data);
        // Set up the edges
        // svg = d3.select("svg");
        svg1 = d3.select("#svg1");
        inner = svg1.select("g");

        // Set up zoom support
        zoom = d3.behavior.zoom().on("zoom", function () {
            inner.attr("transform", "translate(" + d3.event.translate + ")" +
                "scale(" + d3.event.scale + ")");
        });
        svg1.call(zoom);

        redraw();

        //expandNextNode(1);

        zoom
            .translate([35, 50])
            // .translate([($('#svg1').width() - g.graph().width * initialScale) / 2, ($('#svg1').width() - g.graph().height * initialScale) / 2])
            /* .translate([($('svg').width() - g.graph().width * initialScale) / 2, 20])*/
            //.scale(initialScale)
            .scale(1.5)
            .event(svg1);
        //svg.attr('height', g.com.dfire.graph().height * initialScale + 40);

        $('#lineageExpandAll').removeClass('disabled').addClass('active');
        $('#syxgb').removeClass('disabled').addClass('active');
        $('#xyxgb').removeClass('disabled').addClass('active');

        //expandNextNode(len);
        lineageExpandNextNode(len)
    }

    jQuery.ajax({
        type: 'POST',
        url: url,
        data: data,
        success: success
        //dataType: 'json'
    });
}