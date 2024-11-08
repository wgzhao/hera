let nodes, edges, g, headNode, currIndex = 0, len, inner, initialScale = 0.75, zoom, nodeIndex = {}, graphType,
    codeMirror, themeSelect = $('#themeSelect'), zTree;
;

layui.use(['table', 'laydate'], function () {
        let metadataMonitor = $('#dataDictionary');
        metadataMonitor.parent().addClass('menu-open');
        metadataMonitor.parent().parent().addClass('menu-open');
        metadataMonitor.addClass('active');
        $('#bigdataMetadata').addClass('active');

        let table = layui.table, laydate = layui.laydate;
        let focusTree;
        let selected, parentNode;
        let tableName2, tableSchema, tableIns;
        let heraDictTableName = "dwd_herafunc_hive_dictionary_df"
        let formData;//form表单数据
        let setting = {
            view: {
                fontCss: getFontCss
            },
            // check: {
            //     enable: true
            // },
            callback: {
                beforeClick: zTreeBeforeClick,
                onClick: leftClick,
            }
        };

        /**
         *  触发点击动作之前的函数调用
         * @param treeId
         * @param treeNode
         * @param clickFlag
         */
        function zTreeBeforeClick(treeId, treeNode, clickFlag) {
            if (judgeObjectIsNull(focusTree)) {
                selected = focusTree.getSelectedNodes()[0];
                if (selected && selected.level == 1) {
                    changeNodeColor(selected, "rgba(0, 0, 0, 0.65)")
                    //$("#" + selected.tId + "_span").css("color", 'rgba(0, 0, 0, 0.65)');
                }
            }

            return true
        }

        function getFontCss(treeId, treeNode) {
            // if (judgeObjectIsNull(tableName2) && tableName2 == treeNode.name) {
            //     return {
            //         color: "#37a64d",
            //         "font-weight": "bold"
            //     };
            // }
            return {
                color: "rgba(0, 0, 0, 0.65)",
                "font-weight": "normal"
            };
        }

        /**
         * 改变ztree节点颜色
         * @param treeNode
         * @param color
         */
        function changeNodeColor(treeNode, color) {
            $("#" + treeNode.tId + "_span").css("color", color);
        }

        // function switchPage(a) {
        //
        //     if (a == 0) {//展现页面
        //         $("#tableMessage").css("display", "")
        //         $("#tableField").css("display", "")
        //         $("#editTm").css("display", "none")
        //     } else {//编辑页面
        //         $("#tableMessage").css("display", "none")
        //         $("#tableField").css("display", "none")
        //         $("#editTm").css("display", "")
        //     }
        // }

        /**
         * 根据group选择字典表
         */
        function selectGroup(group_name) {
            if (group_name == "dwd_herafunc_hive_dictionary_df") {
                $('#hiveTable').parent().addClass('active');
                $('#middleTable').parent().removeClass('active');
                $('#officialTable').parent().removeClass('active');
            } else if (group_name == "dwd_herafunc_middlebase_dictionary_df") {
                $('#hiveTable').parent().removeClass('active');
                $('#middleTable').parent().addClass('active');
                $('#officialTable').parent().removeClass('active');
            } else if (group_name == "dwd_herafunc_officialbase_dictionary_df") {
                $('#hiveTable').parent().removeClass('active');
                $('#middleTable').parent().removeClass('active');
                $('#officialTable').parent().addClass('active');
            } else {
                $('#hiveTable').parent().addClass('active');
                $('#middleTable').parent().removeClass('active');
                $('#officialTable').parent().removeClass('active');
            }
        }

        function changeZtreeDiv(heraDictTableName) {
            if (heraDictTableName == "dwd_herafunc_hive_dictionary_df") {
                $("#hiveDiv").css("display", "")
                $("#middleDiv").css("display", "none")
                $("#officialDiv").css("display", "none")
            } else if (heraDictTableName == "dwd_herafunc_middlebase_dictionary_df") {
                $("#hiveDiv").css("display", "none")
                $("#middleDiv").css("display", "")
                $("#officialDiv").css("display", "none")
            } else if (heraDictTableName == "dwd_herafunc_officialbase_dictionary_df") {
                $("#hiveDiv").css("display", "none")
                $("#middleDiv").css("display", "none")
                $("#officialDiv").css("display", "")
            }
        }

        $('#hiveTable').click(function (e) {
            e.stopPropagation();
            heraDictTableName = "dwd_herafunc_hive_dictionary_df"
            localStorage.setItem("dataDict_group", heraDictTableName)
            changeTimeTitle()
            // changeZtreeDiv(heraDictTableName)
            initSelect()
        });

        $('#middleTable').click(function (e) {
            e.stopPropagation();
            heraDictTableName = "dwd_herafunc_middlebase_dictionary_df"
            localStorage.setItem("dataDict_group", heraDictTableName)
            changeTimeTitle()
            // changeZtreeDiv(heraDictTableName)
            initSelect()
        });

        $('#officialTable').click(function (e) {
            e.stopPropagation();
            heraDictTableName = "dwd_herafunc_officialbase_dictionary_df"
            localStorage.setItem("dataDict_group", heraDictTableName)
            // changeZtreeDiv(heraDictTableName)
            changeTimeTitle()
            initSelect()
        });


        /**
         * 表字段详情展示
         */
        function tableFormat() {
            if (heraDictTableName == "dwd_herafunc_hive_dictionary_df") {
                // if (judgeObjectIsNull(tableIns)) {
                //     tableIns.reload()
                // }
                tableIns = table.render({
                    elem: '#tableForHive'
                    , height: "full"
                    , url: base_url + '/bigdataMetadata/selectDictTableNew'
                    , where: {
                        databaseName: tableSchema,
                        tableName: tableName2,
                        tableVariable: heraDictTableName
                    }
                    // , page: {
                    //     curr: 1
                    //     , limits: [10,20,30,50,100]
                    // }
                    , cols: [[ //表头
                        {field: 'columnRank', title: '序列', align: 'center', width: 80}
                        , {field: 'columnName', title: '字段名称', align: 'center', width: 200}
                        , {field: 'columnType', title: '字段类型', align: 'center', width: 200}
                        , {
                            field: 'columnComment', title: '字段注释', align: 'center', templet: function (d) {
                                if (d.columnComment == null) {
                                    return ""
                                } else {
                                    return d.columnComment
                                }
                            }
                        }
                        , {
                            field: 'isPartition', title: '是否分区', align: 'center', width: 80, templet: function (d) {
                                if (d.isPartition == 1) {
                                    return "是"
                                } else if (d.isPartition == 0) {
                                    return "否"
                                } else {
                                    return d.isPartition
                                }
                            }
                        }
                    ]]
                });
            } else if (heraDictTableName == "dwd_herafunc_middlebase_dictionary_df") {
                // if (judgeObjectIsNull(tableIns)) {
                //     tableIns.reload()
                // }
                tableIns = table.render({
                    elem: '#tableForHive'
                    , height: "full"
                    , url: base_url + '/bigdataMetadata/selectDictTableNew'
                    , where: {
                        databaseName: tableSchema,
                        tableName: tableName2,
                        tableVariable: heraDictTableName
                    }
                    // , page: {
                    //     curr: 1
                    //     , limits: [10,20,30,50,100]
                    // }
                    , cols: [[ //表头
                        {field: 'columnRank', title: '序列', align: 'center', width: 80}
                        , {field: 'columnName', title: '字段名称', align: 'center', width: 150}
                        , {field: 'columnType', title: '字段类型', align: 'center', width: 150}
                        , {
                            field: 'columnComment', title: '字段注释', align: 'center', templet: function (d) {
                                if (d.columnComment == null) {
                                    return ""
                                } else {
                                    return d.columnComment
                                }
                            }
                        }
                        , {
                            field: 'isKey', title: '是否主键', align: 'center', width: 80, templet: function (d) {
                                if (d.isKey == 1) {
                                    return "是"
                                } else if (d.isKey == 0) {
                                    return "否"
                                } else {
                                    return d.isKey
                                }
                            }
                        }
                        , {
                            field: 'columnDefault', title: '默认值', align: 'center', width: 200
                        }
                        , {
                            field: 'isNullable', title: '是否可为空', align: 'center', width: 80, templet: function (d) {
                                if (d.isNullable == 1) {
                                    return "是"
                                } else if (d.isNullable == 0) {
                                    return "否"
                                } else {
                                    return d.isNullable
                                }
                            }
                        }
                    ]]
                });
            } else if (heraDictTableName == "dwd_herafunc_officialbase_dictionary_df") {
                // if (judgeObjectIsNull(tableIns)) {
                //     tableIns.reload()
                // }
                tableIns = table.render({
                    elem: '#tableForHive'
                    , height: "full"
                    , url: base_url + '/bigdataMetadata/selectDictTableNew'
                    , where: {
                        databaseName: tableSchema,
                        tableName: tableName2,
                        tableVariable: heraDictTableName
                    }
                    // , page: {
                    //     curr: 1
                    //     , limits: [10,20,30,50,100]
                    // }
                    , cols: [[ //表头
                        {field: 'columnRank', title: '序列', align: 'center', width: 80}
                        , {field: 'columnName', title: '字段名称', align: 'center', width: 150}
                        , {field: 'columnType', title: '字段类型', align: 'center', width: 150}
                        , {
                            field: 'columnComment', title: '字段注释', align: 'center', templet: function (d) {
                                if (d.columnComment == null) {
                                    return ""
                                } else {
                                    return d.columnComment
                                }
                            }
                        }
                        , {
                            field: 'isKey', title: '是否主键', align: 'center', width: 80, templet: function (d) {
                                if (d.isKey == 1) {
                                    return "是"
                                } else if (d.isKey == 0) {
                                    return "否"
                                } else {
                                    return d.isKey
                                }
                            }
                        }
                        , {
                            field: 'columnDefault', title: '默认值', align: 'center', width: 200
                        }
                        , {
                            field: 'isNullable', title: '是否可为空', align: 'center', width: 80, templet: function (d) {
                                if (d.isNullable == 1) {
                                    return "是"
                                } else if (d.isNullable == 0) {
                                    return "否"
                                } else {
                                    return d.isNullable
                                }
                            }
                        }
                    ]]
                });
            }
        }

        /**
         * ztree点击事件
         */
        function leftClick() {
            selected = focusTree.getSelectedNodes()[0];
            if (selected) {
                tableName2 = selected.name;
                let level = selected.level;
                if (level == 1) { //table节点
                    //改变选中节点颜色
                    changeNodeColor(selected, "rgb(204,0,170)")
                    setCurrentId(selected.name)
                    //switchPage(0)
                    parentNode = selected.getParentNode();
                    tableSchema = parentNode.name;
                    //表数据描述
                    formDataVal(tableName2, tableSchema)
                    //表字段详情展示
                    tableFormat()
                }
            }
        }

        /**
         *form表单改变
         */
        function changeTimeTitle() {
            if (heraDictTableName == "dwd_herafunc_hive_dictionary_df") {
                $("#labela").css("display", "")
                $("#labelb").css("display", "none")
            } else {
                $("#labela").css("display", "none")
                $("#labelb").css("display", "")
            }
        }

        /**
         * 把当前选中的节点存入localStorage
         * 页面刷新后，会根据"defaultId"设置当前选中的节点
         * 避免页面刷新丢失
         * @param id    节点ID
         */
        function setCurrentId(id) {
            localStorage.setItem("dataDict_node", id);
            // localStorage.setItem("dataDict_group", heraDictTableName)
        }

        $('#keyWords').on('keydown', function (e) {
            if (e.keyCode == '13') {
                if ($('#keyWords').val().length > 0) {
                    searchNodeLazy($.trim($(this).val()), focusTree, "keyWords", false);
                } else {
                    //需处理
                    location.reload(false);
                }
            }
        });
        let timeoutId;

        /**
         * ztree 查询功能
         * @param key 查询字段
         * @param tree tree
         * @param keyId  kId
         * @param first first
         */
        function searchNodeLazy(key, tree, keyId, first) {
            //开始搜索重置check状态
            tree.checkAllNodes(false);
            tree.expandAll(false);
            let searchInfo = $('#searchInfo');
            let deSearchInfo = $('#deSearchInfo');
            let isDepen = tree === $.fn.zTree.getZTreeObj('allTree');
            if (key == null || key === "" || key === undefined) {
                return;
            }
            if (isDepen) {
                searchInfo.show();
                searchInfo.text('查找中，请稍候...');
            } else {
                deSearchInfo.show();
                deSearchInfo.text('查找中，请稍候...');
            }

            if (timeoutId) {
                clearTimeout(timeoutId);
            }
            timeoutId = setTimeout(function () {
                searchKey(key); //lazy load ztreeFilter function
                $('#' + keyId).focus();
            }, 50);

            function searchKey(key) {

                function filter(node) {
                    if (node.level == 1 && node.isHidden) {
                        tree.showNode(node)
                    }
                    return (node.level == 1 &&
                        ((node.name.indexOf(key) > -1)
                            || (node.ds.indexOf(key) > -1)));
                }

                function filter2(node) {
                    return (node.level == 1 &&
                        ((node.name.indexOf(key) === -1)
                            && (node.ds.indexOf(key) === -1)));
                }

                if (key !== null && key !== "" && key !== undefined) {
                    let nodesByFilter = tree.getNodesByFilter(filter);
                    let nodesByFilter2 = tree.getNodesByFilter(filter2);
                    if (nodesByFilter.length > 0) {
                        if (isDepen) {
                            searchInfo.hide();
                        } else {
                            deSearchInfo.hide();
                        }
                        tree.refresh();
                    } else {
                        if (isDepen) {
                            searchInfo.text('未找到该节点');
                        } else {
                            deSearchInfo.text('未找到该节点');
                        }
                        layer.msg("如果是新加节点，请刷新网页后再搜索一次哟");
                    }
                    for (var i = 0, l = nodesByFilter.length; i < l; i++) {
                        tree.checkNode(nodesByFilter[i], true);
                        tree.updateNode(nodesByFilter[i])
                        tree.expandNode(nodesByFilter[i].getParentNode(), true);
                        if (i === 0) {
                            tree.selectNode(nodesByFilter[i]);
                            leftClick();
                        }
                    }

                    for (var i = 0, l = nodesByFilter2.length; i < l; i++) {
                        tree.hideNode(nodesByFilter2[i])
                    }
                }
            }
        }

        /**
         * 对表描述信息初始化值
         * @param tableName2 表名称
         * @param tableSchema 库名称
         */
        function formDataVal(tableName2, tableSchema) {
            formData = getDataByGet(base_url + "/bigdataMetadata/selectDictTableNew", "tableName=" + tableName2 + "&databaseName=" + tableSchema + "&tableVariable=" + heraDictTableName);
            if (formData.data.length == 0) {
                $("#tableForm")[0].reset()
            }
            let databaseName = formData.data[0].databaseName;
            let tableName = formData.data[0].tableName;
            let tableCreateTime = formData.data[0].tableCreateTime;
            let lastDdlTime = formData.data[0].lastDdlTime;
            let updateTime = formData.data[0].updateTime;
            let tableOwner = formData.data[0].tableOwner;
            let tableComment = formData.data[0].tableComment;
            $("[name='databaseName']").val(databaseName == null ? "" : databaseName);
            $("[name='tableName']").val(tableName == null ? "" : tableName);
            $("[name='tableCreateTime']").val(tableCreateTime == null ? "" : tableCreateTime);
            if (heraDictTableName == "dwd_herafunc_hive_dictionary_df") {
                $("[name='lastDdlTime']").val(lastDdlTime == null ? "" : lastDdlTime);
            } else {
                $("[name='lastDdlTime']").val(updateTime == null ? "" : updateTime);
            }
            $("[name='tableOwner']").val(tableOwner == null ? "" : tableOwner);
            $("[name='tableComment']").val(tableComment == null ? "" : tableComment);
        }

        function parseJson(obj) {
            let res = "";
            for (let x in obj) {
                res = res + x + "=" + obj[x] + "\n";
            }
            return res;
        }

        function parseToParam(obj) {
            let res = "";
            for (let x in obj) {
                res = res + x + "=" + obj[x] + "&";
            }
            return res;
        }


        let zNodes;
        let firstAllTreeInit = true;

        function initSelect() {
            let name = localStorage.getItem("dataDict_node");
            let group_name = localStorage.getItem("dataDict_group");
            // alert("=============================gp:"+group_name)
            //tab选择
            if (judgeObjectIsNull(group_name)) {
                heraDictTableName = group_name
                selectGroup(group_name)
                changeTimeTitle()
            } else {
                changeTimeTitle()
                selectGroup(heraDictTableName)
            }
            // alert("=========initSelect:"+heraDictTableName)
            zNodes = getDataByGet(base_url + "/bigdataMetadata/initForDictNew?tableVariable=" + heraDictTableName);
            //判断是否是第一次
            //  if (firstAllTreeInit) {
            firstAllTreeInit = false;
            $.fn.zTree.init($("#allTree"), setting, zNodes);
            //  }
            focusTree = $.fn.zTree.getZTreeObj("allTree");
            if (judgeObjectIsNull(name)) {
                let nodeByParam = focusTree.getNodeByParam("name", name, null);
                if (judgeObjectIsNull(nodeByParam)) {
                    focusTree.selectNode(nodeByParam)
                    focusTree.expandNode(nodeByParam.getParentNode(), true);
                    leftClick()
                } else {
                    //TODO 找不到相关表,对form和table进行重置
                    $("#tableForm")[0].reset()
                    tableFormat()
                }
            } else {
                //TODO 初始化form和table进行重置
                $("#tableForm")[0].reset()
                tableFormat()
            }
        }

        /**
         * 判断对象是否为空
         * @param one_object
         * @returns {boolean}
         */
        function judgeObjectIsNull(one_object) {
            if (one_object !== null && one_object !== "" && one_object !== undefined) {
                return true
            } else {
                return false
            }
        }

        /**
         * 页面初始化
         */
        $(document).ready(function () {
            // changeZtreeDiv(heraDictTableName)

            //selectGroup(heraDictTableName)
            initSelect()
        });
    }
);












