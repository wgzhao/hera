package com.dfire.controller;

import com.dfire.common.entity.HeraJob;
import com.dfire.common.entity.model.JsonResponse;
import com.dfire.common.service.HeraDataManageService;
import com.dfire.common.service.HeraJobService;
import com.dfire.common.graph.DirectionGraph;
import com.dfire.common.graph.Edge;
import com.dfire.common.graph.GraphNode;
import com.dfire.common.graph.TabRelation;
import com.dfire.util.ReadHdfsFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@Controller
@RequestMapping("/dag")
public class dagController extends BaseHeraController {
    private static final Pattern inputPattern = Pattern.compile(" (from|join) +[a-zA-Z0-9_]+[\\\\.][a-zA-Z0-9_]+");
    private static final Pattern outputPattern = Pattern.compile(" (overwrite|into) +(table) +[a-zA-Z0-9_]+[\\\\.][a-zA-Z0-9_]+");

    @Autowired
    @Qualifier("HeraDataManageService")
    private HeraDataManageService heraDataManageService;

    @Autowired
    @Qualifier("heraJobMemoryService")
    private HeraJobService heraJobService;

    @RequestMapping(value = "/hdfsCat", method = RequestMethod.GET)
    @ResponseBody
    public String hdfsCat(String script) {
        if (script.contains("download[hdfs://")) {
            String path = script.split("download\\[hdfs://")[1].split(" ")[0];
            return ReadHdfsFile.hdfsCat(path).trim();
        }
        return "读取hdfs文件异常，联系管理员!";
    }

    @RequestMapping(value = "/getTarTableName", method = RequestMethod.POST)
    @ResponseBody
    public String getSrcTableName(Integer jobId) {
        HeraJob job = heraJobService.findById(jobId);
        String script = job.getScript();
        String tarTab = "";
        if (script.contains("download[hdfs:")) {
            String path = script.split("download\\[hdfs://")[1].split(" ")[0];
            String[] sqlArray = ReadHdfsFile.hdfsCat(path).trim().split(";");
            for (String s : sqlArray) {
                return getTarTable(s);
            }
        } else {
            String[] sqlArray = script.trim().split(";");
            for (String s : sqlArray) {
                return getTarTable(s);
            }
        }
        return tarTab;
    }

    @RequestMapping(value = "/tableRelation", method = RequestMethod.POST)
    // filterSqlJob
    // tableRelation
    @ResponseBody
    public JsonResponse tableRelation(String tabName, Integer type) {
        List<HeraJob> sqlJobs = heraJobService.findSqlJob();
        List<TabRelation> tabRelations = new ArrayList<>(sqlJobs.size() * 6);
        for (HeraJob sqlJob : sqlJobs) {
            String script = sqlJob.getScript();
            Integer id = sqlJob.getId();
            //去掉注释
            StringBuilder sqls = new StringBuilder();
            for (String line : script.split("\n")) {
                line = line.trim().replaceAll("--.*$", " ").trim();
                sqls.append(" ").append(line);
            }
            for (String sql : sqls.toString().split(";")) {
                sql = sql.trim().replaceAll("^(set|alter|drop|refresh|invalidate) {1,}.+", "").replace("${pt_day}", "current_date()").replaceAll("\\s{1,}", " ").trim();
                if (!sql.isEmpty()) {
                    Matcher outputMatcher = outputPattern.matcher(sql);
                    Matcher inputMatcher = inputPattern.matcher(sql);
                    while (outputMatcher.find()) {
                        String outputTable = outputMatcher.group().replaceAll(" (overwrite|into) +(table) +", "");
                        while (inputMatcher.find()) {
                            String inputTable = inputMatcher.group().replaceAll(" (from|join) +", "");
                            tabRelations.add(new TabRelation("1", outputTable, inputTable));
                        }
                    }
                }
            }
        }
        DirectionGraph<Integer> directionGraph = buildJobGraph(tabRelations);//建立表关系之间的关系
        Map<String, Object> graph = buildCurrJobGraph(tabName.trim(), directionGraph, type);//创建graph
        if (graph == null) {
            return new JsonResponse(false, "当前表不存在");
        }
        return new JsonResponse(true, "成功", graph);
    }

    public String getTarTable(String sql1) {

        sql1 = sql1.toLowerCase();

        String tarReg = "[\\s]((overwrite)|(into))[\\s]+(table)[\\s]+[a-zA-Z0-9_]+[\\.][a-zA-Z0-9_]+";
        Pattern tarPatten = Pattern.compile(tarReg);//编译正则表达式
        Matcher tarMatcher = tarPatten.matcher(sql1);// 指定要匹配的字符串

        while (tarMatcher.find()) { //此处find（）每次被调用后，会偏移到下一个匹配
            return (tarMatcher.group().replace("insert ", " ").replace("overwrite ", " ").replace(" table ", "").replace("into", " ").trim());//获取当前匹配的值
        }

        return null;
    }

    public DirectionGraph<Integer> buildJobGraph(List<TabRelation> jobRelations) {

        DirectionGraph<Integer> directionGraph = new DirectionGraph<>();

        for (TabRelation tabRelation : jobRelations) {
            GraphNode<Integer> graphNodeBegin = new GraphNode(1, tabRelation.getName(), "");
            GraphNode<Integer> graphNodeEnd = new GraphNode(1, tabRelation.getPname(), "");
            directionGraph.addNode(graphNodeBegin);
            directionGraph.addNode(graphNodeEnd);
            directionGraph.addEdge(graphNodeBegin, graphNodeEnd);
        }
        return directionGraph;
    }

    private Map<String, Object> buildCurrJobGraph(String tabName, DirectionGraph<Integer> graph, Integer type) {
        GraphNode<Integer> node = new GraphNode(type, tabName, "");

        String start = "start_node";
        GraphNode headNode = new GraphNode<>(0, start);

        Map<String, Object> res = new HashMap<>(2);
        List<Edge> edgeList = new ArrayList<>();
        Queue<GraphNode<Integer>> nodeQueue = new LinkedList<>();

        res.put("headNode", headNode);
        nodeQueue.add(node);
        edgeList.add(new Edge(headNode, node));
        ArrayList<Integer> graphNodes;
        Map<Integer, GraphNode<Integer>> indexMap = graph.getIndexMap();
        GraphNode graphNode;
        Integer index;
        int j = 0;
        while (!nodeQueue.isEmpty()) {
            j++;
            if (j > 10000) {
                break;
            }
            node = nodeQueue.remove();
            index = graph.getNodeIndex(node);
            if (index == null) {
                break;
            }
            if (type == 0) {
                graphNodes = graph.getSrcEdge()[index];
            } else if ((type == 1)) {
                graphNodes = graph.getTarEdge()[index];
            } else {
                graphNodes = graph.getTarEdge()[index];
            }

            if (graphNodes == null) {
                continue;
            }
            int i = 0;
            for (Integer integer : graphNodes) {
                graphNode = indexMap.get(integer);
                GraphNode graphNode1 = null;
                if (graphNode1 == null) {
                    graphNode1 = new GraphNode<>(graphNode.getAuto(), graphNode.getNodeName(), "");
                } else {
                    graphNode1 = new GraphNode<>(graphNode.getAuto(), graphNode.getNodeName(), "");
                }
                // node.getNodeName() == graphNode1.getNodeName() 循环依赖
                if (edgeList.contains(new Edge(node, graphNode1))) {
                    continue;
                }
                i++;
                if (i > 100000000) {
                    break;
                }
                edgeList.add(new Edge(node, graphNode1));
                nodeQueue.add(graphNode1);
            }
        }
        res.put("edges", edgeList);
        return res;
    }
}



