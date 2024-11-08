package com.dfire.common.service.impl;

import com.dfire.common.constants.Constants;
import com.dfire.common.entity.HeraJob;
import com.dfire.common.entity.HeraJobHistory;
import com.dfire.common.mapper.HeraJobMapper;
import com.dfire.common.service.HeraDataManageService;
import com.dfire.common.service.HeraJobHistoryService;
import com.dfire.common.graph.DirectionGraph;
import com.dfire.common.graph.Edge;
import com.dfire.common.graph.GraphNode;
import com.dfire.common.graph.JobRelation;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author xiaosuda
 * @date 2018/11/7
 */
@Service("HeraDataManageService")
public class HeraDataManageServiceImpl implements HeraDataManageService {

    @Autowired
    protected HeraJobMapper heraJobMapper;

    @Autowired
    private HeraJobHistoryService heraJobHistoryService;

    @Override
    public Map<String, Object> findCurrentTableGraph(int jobId, Integer type) {
        Map<String, GraphNode> historyMap = buildHistoryMap();

        HeraJob nodeJob = findById(jobId);
        if (nodeJob == null) {
            return null;
        }
        //查找标志
        // System.err.println("nodeJob : " + nodeJob);

        GraphNode graphNode1 = historyMap.get(nodeJob.getId() + "");

        //System.err.println("graphNode1 :"+graphNode1);
        String remark = "";
        if (graphNode1 != null) {
            remark = (String) graphNode1.getRemark();
        }

        GraphNode<Integer> graphNode = new GraphNode<>(nodeJob.getAuto(), nodeJob.getId(),
                "任务ID：" + jobId + "\n任务名称:" + nodeJob.getName() + "\n" + remark);

        //查找标志
        System.err.println("——————————————");
        System.err.println("historyMap : " + historyMap);
        System.err.println("graphNode : " + graphNode);
        System.err.println("getDirectionGraph() : " + getDirectionGraph());

        System.err.println("type : " + type);
        System.err.println("——————————————");

        return buildCurrJobGraph(historyMap, graphNode, getDirectionGraph(), type);
    }


    @Override
    public HeraJob findById(int id) {
        return heraJobMapper.findById(id);
    }


    /**
     * 建立今日任务执行 Map映射 便于获取
     *
     * @return Map
     */
    private Map<String, GraphNode> buildHistoryMap() {

        List<HeraJobHistory> actionHistories = heraJobHistoryService.findTodayJobHistory();
        Map<String, GraphNode> map = new HashMap<>(actionHistories.size());
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        for (HeraJobHistory actionHistory : actionHistories) {
            String start = "none", end = "none", status, jobId, duration, upDependencies, actionId;
            actionId = actionHistory.getActionId() == null ? "none" : actionHistory.getActionId();
            status = actionHistory.getStatus() == null ? "none" : actionHistory.getStatus();
            jobId = actionHistory.getJobId() + "";
            duration = "none";
            if (actionHistory.getStartTime() != null) {
                start = sdf.format(actionHistory.getStartTime());
                if (actionHistory.getEndTime() != null) {
                    duration = (actionHistory.getEndTime().getTime() - actionHistory.getStartTime().getTime()) / 1000 + "s";
                    end = sdf.format(actionHistory.getEndTime());
                }
            }
            //if(StringUtils.isEmpty(jobId)){
            upDependencies = StringUtils.isEmpty(heraJobMapper.findUpDependenciesById(Integer.parseInt(jobId)))
                    ? "none" : heraJobMapper.findUpDependenciesById(Integer.parseInt(jobId));
            //}
            List<String> list = new ArrayList<>(1);
            list.add("none");
            List<String> downDependencies = heraJobMapper.findDownDependenciesById(Integer.parseInt(jobId)).isEmpty()
                    ? list : heraJobMapper.findDownDependenciesById(Integer.parseInt(jobId));
            //downDependencies.toString().substring(0).substring(downDependencies.toString().substring(0).length()-1);
            GraphNode node = new GraphNode<>(Integer.parseInt(jobId),
                    "任务状态：" + status + "\n" +
                            "actionId：" + actionId + "\n" +
                            "执行时间：" + start + "\n" +
                            "结束时间：" + end + "\n" +
                            "耗时：" + duration + "\n" +
                            "上游依赖：" + upDependencies + "\n" +
                            "下游依赖：" + StringUtils.strip(downDependencies.toString(), "[]") + "\n"
            );
            map.put(actionHistory.getJobId() + "", node);
        }
        //System.err.println("map :"+map);
        return map;
    }


    private DirectionGraph<Integer> getDirectionGraph() {
        return this.buildJobGraph(this.getJobRelations());
    }

    /**
     * 定时调用的任务图
     *
     * @param jobRelations 任务之间的关系
     * @return DirectionGraph
     */

    public DirectionGraph<Integer> buildJobGraph(List<JobRelation> jobRelations) {

        DirectionGraph<Integer> directionGraph = new DirectionGraph<>();

        for (JobRelation jobRelation : jobRelations) {


            GraphNode<Integer> graphNodeBegin = new GraphNode<>(jobRelation.getAuto(), jobRelation.getId(), "任务ID：" + jobRelation.getId() +
                    "\n任务名称:" + jobRelation.getName() + "\n");

            GraphNode<Integer> graphNodeEnd = new GraphNode<>(jobRelation.getPAuto(), jobRelation.getPid(), "任务ID：" + jobRelation.getPid()
                    + "\n任务名称:" + jobRelation.getPname() + "\n");

            directionGraph.addNode(graphNodeBegin);
            directionGraph.addNode(graphNodeEnd);
            directionGraph.addEdge(graphNodeBegin, graphNodeEnd);
        }

        System.err.println("11111------directionGraph :" + directionGraph);
        return directionGraph;
    }


    @Override
    public List<JobRelation> getJobRelations() {
        List<HeraJob> list = this.getAllJobDependencies();
        List<JobRelation> res = new ArrayList<>(list.size() * 3);
        Map<Integer, String> map = new HashMap<>(list.size());
        Map<Integer, Integer> parentAutoMap = new HashMap<>(list.size());
        for (HeraJob job : list) {
            map.put(job.getId(), job.getName());
            parentAutoMap.put(job.getId(), job.getAuto());
        }
        Integer p, id;
        String dependencies;
        for (HeraJob job : list) {
            id = job.getId();
            dependencies = job.getDependencies();
            if (StringUtils.isBlank(dependencies)) {
                continue;
            }
            String[] parents = dependencies.split(Constants.COMMA);
            for (String parent : parents) {
                p = Integer.parseInt(parent);
                if (map.get(p) == null) {
                    continue;
                }
                JobRelation jr = new JobRelation();
                jr.setAuto(job.getAuto());
                jr.setId(id);
                jr.setName(map.get(id));
                jr.setPid(p);
                jr.setPname(map.get(p));
                jr.setPAuto(parentAutoMap.get(p));
                res.add(jr);
            }
        }
        System.err.println("res : xxxxxxxxx");
        System.err.println(res);
        System.err.println("xxxxxxxxxx");
        return res;
    }


    @Override
    public List<HeraJob> getAllJobDependencies() {
        return heraJobMapper.getAllJobRelations();
    }


    private Map<String, Object> buildCurrJobGraph(Map<String, GraphNode> historyMap, GraphNode<Integer> node, DirectionGraph<Integer> graph, Integer type) {
        String start = "start_node";
        Map<String, Object> res = new HashMap<>(2);
        List<Edge> edgeList = new ArrayList<>();
        Queue<GraphNode<Integer>> nodeQueue = new LinkedList<>();
        GraphNode headNode = new GraphNode<>(0, start);
        res.put("headNode", headNode);
        nodeQueue.add(node);
        edgeList.add(new Edge(headNode, node));
        ArrayList<Integer> graphNodes;
        Map<Integer, GraphNode<Integer>> indexMap = graph.getIndexMap();
        GraphNode graphNode;
        Integer index;
        while (!nodeQueue.isEmpty()) {
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

            System.err.println("graphNodes :" + graphNodes);

            for (Integer integer : graphNodes) {
                graphNode = indexMap.get(integer);
                GraphNode graphNode1 = historyMap.get(graphNode.getNodeName() + "");
                if (graphNode1 == null) {
                    graphNode1 = new GraphNode<>(graphNode.getAuto(), graphNode.getNodeName(), "" + graphNode.getRemark());
                } else {
                    graphNode1 = new GraphNode<>(graphNode.getAuto(), graphNode.getNodeName(), "" + graphNode.getRemark() + graphNode1.getRemark());
                }
                edgeList.add(new Edge(node, graphNode1));
                nodeQueue.add(graphNode1);
            }
        }
        res.put("edges", edgeList);
        return res;
    }


}
