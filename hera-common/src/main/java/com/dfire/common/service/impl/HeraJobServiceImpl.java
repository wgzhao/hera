package com.dfire.common.service.impl;

import com.alibaba.druid.support.json.JSONUtils;
import com.alibaba.fastjson2.JSONObject;
import com.dfire.common.constants.Constants;
import com.dfire.common.entity.HeraAction;
import com.dfire.common.entity.HeraGroup;
import com.dfire.common.entity.HeraJob;
import com.dfire.common.entity.HeraJobHistory;
import com.dfire.common.entity.HeraUser;
import com.dfire.common.entity.model.JsonResponse;
import com.dfire.common.entity.vo.HeraJobTreeNodeVo;
import com.dfire.common.entity.vo.HeraJobVo;
import com.dfire.common.kv.JobDownDenpends;
import com.dfire.common.mapper.HeraGroupMapper;
import com.dfire.common.mapper.HeraJobActionMapper;
import com.dfire.common.mapper.HeraJobMapper;
import com.dfire.common.service.HeraJobHistoryService;
import com.dfire.common.service.HeraJobService;
import com.dfire.common.util.DagLoopUtil;
import com.dfire.common.util.StringUtil;
import com.dfire.common.graph.DirectionGraph;
import com.dfire.common.graph.Edge;
import com.dfire.common.graph.GraphNode;
import com.dfire.common.graph.JobRelation;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author xiaosuda
 * @date 2018/11/7
 */
@Service("heraJobService")
public class HeraJobServiceImpl
        implements HeraJobService
{

    private static final Map<Integer, String> upDepsMap = new HashMap<>();
    private static final Map<Integer, List<String>> downDepsMap = new HashMap<>();

    @Autowired
    protected HeraJobMapper heraJobMapper;

    @Autowired
    protected HeraJobActionMapper heraJobActionMapper;

//    @Autowired
//    @Qualifier("heraGroupMemoryService")
//    private HeraGroupService groupService;

    @Autowired
    private HeraGroupMapper heraGroupMapper;

    @Autowired
    private HeraJobHistoryService heraJobHistoryService;

    @Override
    public int insert(HeraJob heraJob)
    {
        Date date = new Date();
        heraJob.setGmtCreate(date);
        heraJob.setGmtModified(date);
        heraJob.setAuto(0);
        return heraJobMapper.insert(heraJob);
    }

    @Override
    public int delete(int id)
    {
        return heraJobMapper.delete(id);
    }

    @Override
    public int update(HeraJob heraJob)
    {
        return heraJobMapper.update(heraJob);
    }

    @Override
    public List<HeraJob> getAll()
    {
        return heraJobMapper.getAll();
    }

    @Override
    public HeraUser findScKeyById(int jobId)
    {
        return heraJobMapper.findScKeyById(jobId);
    }

    @Override
    public HeraJob findById(int id)
    {
        return heraJobMapper.findById(id);
    }

    @Override
    public List<HeraJob> findByIds(List<Integer> list)
    {
        return heraJobMapper.findByIds(list);
    }

    @Override
    public List<HeraJob> findByPid(int groupId)
    {
        return heraJobMapper.findByPid(groupId);
    }

    @Override
    public Map<String, List<HeraJobTreeNodeVo>> buildJobTree(String owner)
    {
        Map<String, List<HeraJobTreeNodeVo>> treeMap = new HashMap<>(2);
        List<HeraGroup> groups = heraGroupMapper.getAll();

      /*  for (HeraGroup s:groups) {
            System.err.println(s);//文件夹
        }
*/
        List<HeraJob> jobs = this.getAll();
       /* for (HeraJob h:jobs) {
            System.err.println(h);
        }*/

        Map<String, HeraJobTreeNodeVo> groupMap = new HashMap<>(groups.size());
        List<HeraJobTreeNodeVo> myGroupList = new ArrayList<>();
        // 建立所有任务的树
        List<HeraJobTreeNodeVo> allNodes = groups.stream()
                .filter(group -> group.getExisted() == 1)
                .map(g -> {
                    //System.err.println("g : "+g);
                    HeraJobTreeNodeVo groupNodeVo = HeraJobTreeNodeVo.builder()
                            .id(Constants.GROUP_PREFIX + g.getId())
                            .parent(Constants.GROUP_PREFIX + g.getParent())
                            .directory(g.getDirectory())
                            .isParent(true)
                            .jobId(g.getId())
                            .jobName(g.getName())
                            .jobDescription(g.getDescription())
                            .owner(g.getOwner())
                            .name(g.getName() + Constants.LEFT_BRACKET + g.getId() + Constants.RIGHT_BRACKET)
                            .build();
                    if (owner.equals(g.getOwner())) {
                        myGroupList.add(groupNodeVo);
                    }
                    groupMap.put(groupNodeVo.getId(), groupNodeVo);
                    return groupNodeVo;
                })
                .collect(Collectors.toList());
        Set<HeraJobTreeNodeVo> myGroupSet = new HashSet<>();
        //建立我的任务的树
        List<HeraJobTreeNodeVo> myNodeVos = new ArrayList<>();
        jobs.forEach(job -> {
            //System.err.println("job : "+job.toString1());
            HeraJobTreeNodeVo build = HeraJobTreeNodeVo.builder()
                    .id(String.valueOf(job.getId()))
                    .parent(Constants.GROUP_PREFIX + job.getGroupId())
                    .isParent(false)
                    .jobId(job.getId())
                    .jobDescription(job.getDescription())
                    .owner(job.getOwner())
                    .jobName(job.getName())
                    .name(job.getName() + Constants.LEFT_BRACKET + job.getId() + Constants.RIGHT_BRACKET)
                    .dependence(job.getDependencies())
                    .build();
            allNodes.add(build);
            if (owner.equals(job.getOwner().trim())) {
                getPathGroup(myGroupSet, build.getParent(), groupMap);
                myNodeVos.add(build);
            }
        });
        myGroupList.forEach(treeNode -> getPathGroup(myGroupSet, treeNode.getId(), groupMap));
        myNodeVos.addAll(myGroupSet);
        //根据名称排序
        allNodes.sort(Comparator.comparing(HeraJobTreeNodeVo::getName));
        myNodeVos.sort(Comparator.comparing(HeraJobTreeNodeVo::getName));
        treeMap.put("myJob", myNodeVos);
        treeMap.put("allJob", allNodes);
        return treeMap;
    }

    /**
     * 递归获得父目录
     *
     * @param myGroupSet 结果集
     * @param group 当前group
     * @param allGroupMap 所有组map
     */
    private void getPathGroup(Set<HeraJobTreeNodeVo> myGroupSet, String group, Map<String, HeraJobTreeNodeVo> allGroupMap)
    {
        HeraJobTreeNodeVo groupNode = allGroupMap.get(group);
        if (groupNode == null || myGroupSet.contains(groupNode)) {
            return;
        }
        myGroupSet.add(groupNode);
        getPathGroup(myGroupSet, groupNode.getParent(), allGroupMap);
    }

    @Override
    public boolean changeSwitch(int id, int status)
    {
        int res = heraJobMapper.updateSwitch(id, status);
        return res > 0;
    }

    @Override
    public JsonResponse checkAndUpdate(HeraJob heraJob)
    {

        if (StringUtils.isNotBlank(heraJob.getDependencies())) {
            HeraJob job = this.findById(heraJob.getId());

            if (!heraJob.getDependencies().equals(job.getDependencies())) {
                List<HeraJob> relation = this.getAllJobDependencies();

                DagLoopUtil dagLoopUtil = new DagLoopUtil(heraJobMapper.selectMaxId());
                relation.forEach(x -> {
                    String dependencies;
                    if (x.getId() == heraJob.getId()) {
                        dependencies = heraJob.getDependencies();
                    }
                    else {
                        dependencies = x.getDependencies();
                    }
                    if (StringUtils.isNotBlank(dependencies)) {
                        String[] split = dependencies.split(",");
                        for (String s : split) {
                            dagLoopUtil.addEdge(x.getId(), Integer.parseInt(s));
                        }
                    }
                });

                if (dagLoopUtil.isLoop()) {
                    return new JsonResponse(false, "出现环形依赖，请检测依赖关系:" + dagLoopUtil.getLoop());
                }
            }
        }

        Integer line = this.update(heraJob);
        if (line == null || line == 0) {
            return new JsonResponse(false, "更新失败，请联系管理员");
        }
        return new JsonResponse(true, "更新成功");
    }

    @Override
    public Map<String, Object> findCurrentJobGraph(int jobId, int type)
    {

        Map<String, GraphNode> historyMap = buildHistoryMap();

        HeraJob nodeJob = findById(jobId);
        if (nodeJob == null) {
            return null;
        }
        //查找标志
        //System.err.println("nodeJob : " + nodeJob);
        GraphNode graphNode1 = historyMap.get(nodeJob.getId() + "");
        //System.err.println("graphNode1 :"+graphNode1);

        String remark = "";
        if (graphNode1 != null) {
            remark = (String) graphNode1.getRemark();
        }

        GraphNode<Integer> graphNode = new GraphNode<>(nodeJob.getAuto(), nodeJob.getId(),
                "任务ID：" + jobId + "\n任务名称:" + nodeJob.getName() + "\n" + remark);

        return buildCurrJobGraph(historyMap, graphNode, getDirectionGraph(), type);
    }

    @Override
    public Map<String, Object> findCurrentJobGraphForDownRecovery(String jobId, String actionId, int type)
    {

        //    System.out.println("HeraJobServiceImpl findCurrentJobGraphForDownRecovery ...")
        Map<String, GraphNode> historyMap = buildHistoryMapForDownRecovery(jobId);

        HeraJob nodeJob = findById(Integer.parseInt(jobId));
        if (nodeJob == null) {
            return null;
        }
        //查找标志
        //System.err.println("nodeJob : " + nodeJob);
        GraphNode graphNode1 = historyMap.get(nodeJob.getId() + "");
        //System.err.println("graphNode1 :"+graphNode1);

        String remark = "";
        if (graphNode1 != null) {
            remark = (String) graphNode1.getRemark();
        }

        GraphNode<Integer> graphNode = new GraphNode<>(nodeJob.getAuto(), nodeJob.getId(),
                "任务ID：" + jobId + "\n任务名称:" + nodeJob.getName() + "\n" + remark);

        return buildCurrJobGraph(historyMap, graphNode, getDirectionGraph(), type);
    }

    @Override
    public List<Integer> findJobImpact(int jobId, int type)
    {
        Set<Integer> check = new HashSet<>();
        List<Integer> res = new ArrayList<>();
        check.add(jobId);
        res.add(jobId);
        DirectionGraph<Integer> graph = getDirectionGraph();

        Queue<GraphNode<Integer>> nodeQueue = new LinkedList<>();
        GraphNode<Integer> node = new GraphNode<>(jobId, "");
        nodeQueue.add(node);
        Integer index;
        ArrayList<Integer> graphNodes;
        Map<Integer, GraphNode<Integer>> indexMap = graph.getIndexMap();
        GraphNode<Integer> graphNode;
        while (!nodeQueue.isEmpty()) {
            node = nodeQueue.remove();
            index = graph.getNodeIndex(node);
            if (index == null) {
                break;
            }
            if (type == 0) {
                graphNodes = graph.getSrcEdge()[index];
            }
            else {
                graphNodes = graph.getTarEdge()[index];
            }
            if (graphNodes == null) {
                continue;
            }
            for (Integer integer : graphNodes) {
                graphNode = indexMap.get(integer);
                if (!check.contains(graphNode.getNodeName())) {
                    check.add(graphNode.getNodeName());
                    res.add(graphNode.getNodeName());
                    nodeQueue.add(graphNode);
                }
            }
        }
        return res;
    }

    @Override
    public List<JobRelation> getJobRelations()
    {
        List<HeraJob> list = this.getAllJobDependencies();//select `name`,id,dependencies,auto from hera_job
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
        return res;
    }

    @Override
    public List<HeraJob> findDownStreamJob(int jobId)
    {
        return this.getStreamTask(jobId, true);
    }

    @Override
    public List<HeraJob> findUpStreamJob(int jobId)
    {
        return this.getStreamTask(jobId, false);
    }

    @Override
    public List<HeraJob> getAllJobDependencies()
    {
        return heraJobMapper.getAllJobRelations();
    }

    @Override
    public boolean changeParent(int newId, int parentId)
    {
        Integer update = heraJobMapper.changeParent(newId, parentId);
        return update != null && update > 0;
    }

    @Override
    public boolean isRepeat(int jobId)
    {
        Integer repeat = heraJobMapper.findRepeat(jobId);
        return repeat != null && repeat > 0;
    }

    /**
     * 建立今日任务执行 Map映射 便于获取
     *
     * @return Map
     */
    private Map<String, GraphNode> buildHistoryMap()
    {

        if (upDepsMap.isEmpty() || downDepsMap.isEmpty()) {
            loadDeps();
        }

        List<HeraJobHistory> actionHistories = heraJobHistoryService.findTodayJobHistory();
        Map<String, GraphNode> map = new HashMap<>(actionHistories.size());
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        for (HeraJobHistory actionHistory : actionHistories) {
            String start = "none", end = "none", status, jobId, duration, upDependencies, actionId;
            status = actionHistory.getStatus() == null ? "none" : actionHistory.getStatus();
            actionId = actionHistory.getActionId() == null ? "none" : actionHistory.getActionId();
            jobId = actionHistory.getJobId() + "";
            duration = "none";
            if (actionHistory.getStartTime() != null) {
                start = sdf.format(actionHistory.getStartTime());
                if (actionHistory.getEndTime() != null) {
                    duration = (actionHistory.getEndTime().getTime() - actionHistory.getStartTime().getTime()) / 1000 + "s";
                    end = sdf.format(actionHistory.getEndTime());
                }
            }
            String upDeps = upDepsMap.get(Integer.parseInt(jobId));
//            String upDeps = heraJobMapper.findUpDependenciesById(Integer.parseInt(jobId));
            //if(StringUtils.isEmpty(jobId)){
            upDependencies = StringUtils.isEmpty(upDeps) ? "none" : upDeps;
            //}
            List<String> list = new ArrayList<>(1);
            list.add("none");
            List<String> downDeps = downDepsMap.get(Integer.parseInt(jobId));
//            List<String> downDeps = heraJobMapper.findDownDependenciesById(Integer.parseInt(jobId));
            List<String> downDependencies = downDeps.isEmpty() ? list : downDeps;

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

    /**
     * 针对指定版本的任务依赖图
     *
     * @return
     */
    private Map<String, GraphNode> buildHistoryMapForDownRecovery(String jobIdA)
    {

        List<HeraJobHistory> actionHistories = findDownActionId(jobIdA);

        Map<String, GraphNode> map = new HashMap<>(actionHistories.size());
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        for (HeraJobHistory actionHistory : actionHistories) {
            String start = "none", end = "none", status, jobId, duration, upDependencies, actionId;
            status = actionHistory.getStatus() == null ? "none" : actionHistory.getStatus();
            actionId = actionHistory.getActionId() == null ? "none" : actionHistory.getActionId();
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

    /**
     * 恢复下游--改变下游版本id
     *
     * @param jobId
     * @return
     */
    public JsonResponse changeDownActionId(String jobId, boolean isReRun, String runDay, boolean isfirstDay)
    {

        try {
            ///获取两天最早的版本信息 (在2022-07-04获取2022-07-03的第一个版本号)
            HeraAction todayEarliestActionId = heraJobMapper.getTodayEarliestActionId(jobId);
            HashSet<String> hs = new HashSet<>();
            ///获取版本号
            String actionId = todayEarliestActionId.getId() + "";

            ///获取当日的所有依赖任务的版本
            List<JobDownDenpends> todayAllActionId = heraJobMapper.getTodayAllActionId();
            ///获取依赖于恢复任务的下游 存入 HashSet
            for (int j = 0; j < todayAllActionId.size(); j++) {
                String dependencies = todayAllActionId.get(j).getDependencies();
                String id = todayAllActionId.get(j).getId();
                try {
                    if (dependencies.contains(actionId)) {
                        hs.add(id);
                        ///获取下游的下游 (递归获取所有下游)
                        findAllDownAction(todayAllActionId, id, hs);
                    }
                }
                catch (Exception e) {
                    // e.printStackTrace();
                }
            }
            hs.add(actionId);

            ///获取于该任务最早版本号相关的所有任务的 hera_action 所有信息
            List<HeraAction> heraActions = heraJobMapper.batchHeraJobSelect(new ArrayList<>(hs));

            int nodeHistoryId = 0;
            for (HeraAction heraAction : heraActions) {
                if (heraAction.getId().toString().equals(actionId)) {
                    nodeHistoryId = Integer.parseInt(heraAction.getHistoryId());
                }
            }

            ///判断是否有任务在执行
            for (HeraAction heraAction : heraActions) {
                if (heraAction != null && heraAction.getStatus() != null && heraAction.getStatus().trim().equalsIgnoreCase("running")) {
                    return new JsonResponse(false, "该依赖图中有任务(job_id=" + heraAction.getJobId() + ")正在进行，请稍后重试。",
                            new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(new Date()));
                }
                if (!isfirstDay) {
                    if (!heraAction.getStatus().trim().equalsIgnoreCase("success")) {
                        if (nodeHistoryId <= Integer.parseInt(heraAction.getHistoryId()) && heraAction.getStatus().trim().equalsIgnoreCase("failed")) {
                            return new JsonResponse(false, "Last failure", heraAction.getJobId());
                        }
                        return new JsonResponse(false, "未执行,jobId=" + heraAction.getJobId(), "");
                    }
                    else if (nodeHistoryId > Integer.parseInt(heraAction.getHistoryId())) {
                        return new JsonResponse(false, "未提交,jobId=" + heraAction.getJobId(), "");
                    }
                }
            }

            if (runDay.equals("runDay")) {
                ///全部执行完成
                return new JsonResponse(true, "全部日期执行完成", "全部日期执行完成");
            }
            else {
                for (HeraAction heraAction : heraActions) {
                    String dependencies = heraAction.getDependencies();
                    Map<String, String> map = new HashMap<>();
                    if (dependencies != null) {
                        String[] split = dependencies.split(",");
                        for (String s : split) {
                            map.put(s, System.currentTimeMillis() + "");
                        }
                    }
                    for (String h : hs) {
                        map.remove(h);
                    }
                    String string = JSONUtils.toJSONString(map);
                    heraAction.setReadyDependency(string);

                    if (isReRun) {
                        try {
                            String configs = heraAction.getConfigs();
                            Map<String, String> map1 = StringUtil.convertStringToMap(configs);
                            map1.put("pt_day", "'" + runDay + "'");
                            configs = JSONObject.toJSONString(map1);
                            heraAction.setConfigs(configs);
                        }
                        catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    heraAction.setStatus("failed");
                }

                int maxSize = heraActions.size();
                int batch = 500;
                int count = maxSize / batch;

                if (maxSize <= batch) {
                    heraJobMapper.batchUpdate(heraActions);
                }
                else {
                    for (int j = 0; j < count; j++) {
                        List<HeraAction> heraActions1 = heraActions.subList(j * batch, (j + 1) * batch);
                        heraJobMapper.batchUpdate(heraActions1);
                    }
                    if (maxSize > count * batch) {
                        heraJobMapper.batchUpdate(heraActions.subList((count * batch), maxSize));
                    }
                }

                return new JsonResponse(true, "修改任务状态成功.", "修改任务状态成功.");
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            return new JsonResponse(false, "修改任务状态异常.", "修改任务状态异常.");
        }
    }

    /**
     * 恢复下游--查找下游版本id
     *
     * @param jobId
     * @return
     */
    public List<HeraJobHistory> findDownActionId(String jobId)
    {

        //   List<HeraJobHistory> actionHistories=new ArrayList<>();

        try {
            HeraAction todayEarliestActionId = heraJobMapper.getTodayEarliestActionId(jobId);
            String actionId = todayEarliestActionId.getId() + "";
            HashSet<String> hs = new HashSet<>();
            List<JobDownDenpends> todayAllActionId = heraJobMapper.getTodayAllActionId();
            for (int j = 0; j < todayAllActionId.size(); j++) {
                String dependencies = todayAllActionId.get(j).getDependencies();
                String id = todayAllActionId.get(j).getId();
                try {
                    if (dependencies.contains(actionId)) {
                        hs.add(id);
                        findAllDownAction(todayAllActionId, id, hs);
                    }
                }
                catch (Exception e) {
                    // e.printStackTrace();
                }
            }
            ArrayList<String> heraActions = new ArrayList<String>(hs);
            return heraJobMapper.batchSelect(heraActions);
        }
        catch (Exception e) {
            ArrayList<String> list = new ArrayList<String>();
            list.add(jobId);
            return heraJobMapper.batchSelect(list);
        }
    }

    /**
     * 恢复下游--查找下一层依赖
     *
     * @param actionId
     * @param hs
     */
    public void findAllDownAction(List<JobDownDenpends> todayAllActionId, String actionId, HashSet<String> hs)
    {

        try {
            for (int j = 0; j < todayAllActionId.size(); j++) {
                String dependencies = todayAllActionId.get(j).getDependencies();
                String id = todayAllActionId.get(j).getId();
                try {
                    if (dependencies.contains(actionId)) {
                        hs.add(todayAllActionId.get(j).getId());
                        findAllDownAction(todayAllActionId, id, hs);
                    }
                }
                catch (Exception e) {
                    // e.printStackTrace();
                }
            }
        }
        catch (Exception e) {

        }
    }

    /* public void findAllDownAction(List<String> downJobIds, HashSet hs) {

        if (downJobIds == null || downJobIds.size() == 0) {
            return;
        }
        for (int i = 0; i < downJobIds.size(); i++) {
            List<String> downJobIds1 = heraJobMapper.getDownJobIds(downJobIds.get(i).trim());
            hs.add(downJobIds.get(i));
            findAllDownAction(downJobIds1, hs);
        }
    }*/

    /**
     * 恢复下游--将下游任务的依赖修改-具体任务具体动作
     */
  /*  public void changeDownActionId(String actionId, HashSet hs) {

        // System.err.println(actionId);
        HeraAction heraAction = heraJobMapper.getHeraAction(actionId);

        String dependencies = heraAction.getDependencies();
        Map map = new HashedMap();
        if (dependencies != null) {
            String[] split = dependencies.split(",");
            for (int i = 0; i < split.length; i++) {
                map.put(split[i], System.currentTimeMillis() + "");
            }
        }

        Iterator it = hs.iterator();
        while (it.hasNext()) {
            map.remove(it.next());
        }
        String string = JSONUtils.toJSONString(map);
        heraAction.setReadyDependency(string);
        heraAction.setStatus("failed");
        // heraAction.setStartTime(new Date());
        //   System.err.println("8888888  "+heraAction.getId()+""+heraAction.getReadyDependency()+heraAction.getStatus());
        //    heraJobActionMapper.update(heraAction);

        List<String> downJobIds = heraJobMapper.getDownJobIds(actionId);

        if (downJobIds != null && downJobIds.size() > 0) {
            for (int i = 0; i < downJobIds.size(); i++) {
                changeDownActionId(downJobIds.get(i), hs);
            }
        }


    }*/
    private DirectionGraph<Integer> getDirectionGraph()
    {
        return this.buildJobGraph(this.getJobRelations());
    }

    /**
     * 获得上下游的任务
     *
     * @param jobId 任务id
     * @param down 是否为下游
     * @return
     */

    private List<HeraJob> getStreamTask(Integer jobId, boolean down)
    {
        GraphNode<Integer> head = new GraphNode<>();
        head.setNodeName(jobId);
        DirectionGraph<Integer> graph = this.getDirectionGraph();
        Integer headIndex = graph.getNodeIndex(head);
        Queue<Integer> nodeQueue = new LinkedList<>();
        if (headIndex != null) {
            nodeQueue.add(headIndex);
        }
        ArrayList<Integer> graphNodes;
        Map<Integer, GraphNode<Integer>> indexMap = graph.getIndexMap();
        List<Integer> jobList = new ArrayList<>();
        while (!nodeQueue.isEmpty()) {
            headIndex = nodeQueue.remove();
            if (down) {
                graphNodes = graph.getTarEdge()[headIndex];
            }
            else {
                graphNodes = graph.getSrcEdge()[headIndex];
            }
            if (graphNodes == null || graphNodes.size() == 0) {
                continue;
            }

            for (Integer graphNode : graphNodes) {
                nodeQueue.add(graphNode);
                jobList.add(indexMap.get(graphNode).getNodeName());
            }
        }

        List<HeraJob> res = new ArrayList<>();
        for (Integer id : jobList) {
            res.add(this.findById(id));
        }
        return res;
    }

    /**
     * @param historyMap 宙斯任务历史运行任务map
     * @param node 当前头节点
     * @param graph 所有任务的关系图
     * @param type 展示类型  0:任务进度分析   1：影响分析
     */
    private Map<String, Object> buildCurrJobGraph(Map<String, GraphNode> historyMap, GraphNode<Integer> node, DirectionGraph<Integer> graph, Integer type)
    {

        String start = "start_node";
        Map<String, Object> res = new HashMap<>(2);
        List<Edge> edgeList = new ArrayList<>();
        Queue<GraphNode<Integer>> nodeQueue = new LinkedList<>();
        GraphNode headNode = new GraphNode<>(0, start);
        res.put("headNode", headNode);

        //System.err.println("1218______"+node);
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
            }
            else if ((type == 1)) {
                graphNodes = graph.getTarEdge()[index];
            }
            else {
                graphNodes = graph.getTarEdge()[index];
            }
            if (graphNodes == null) {
                continue;
            }

            //System.err.println("graphNodes :" + graphNodes);

            for (Integer integer : graphNodes) {
                graphNode = indexMap.get(integer);
                GraphNode graphNode1 = historyMap.get(graphNode.getNodeName() + "");
                if (graphNode1 == null) {
                    graphNode1 = new GraphNode<>(graphNode.getAuto(), graphNode.getNodeName(), "" + graphNode.getRemark());
                }
                else {
                    graphNode1 = new GraphNode<>(graphNode.getAuto(), graphNode.getNodeName(), "" + graphNode.getRemark() + graphNode1.getRemark());
                }
                edgeList.add(new Edge(node, graphNode1));
                nodeQueue.add(graphNode1);
            }
        }
        res.put("edges", edgeList);
        return res;
    }

    /**
     * 定时调用的任务图
     *
     * @param jobRelations 任务之间的关系
     * @return DirectionGraph
     */

    public DirectionGraph<Integer> buildJobGraph(List<JobRelation> jobRelations)
    {

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
        return directionGraph;
    }

    /**
     * 查找任务关闭数
     *
     * @return
     */
    @Override
    public int getStopHeraJobInfo()
    {
        return  heraJobMapper.getStopHeraJobInfo();
    }

    @Override
    public int selectJobCountByUserName(HeraJobVo heraJobVo)
    {
        return heraJobMapper.selectJobCountByUserName(heraJobVo);
    }

    @Override
    public int selectManJobCountByUserName(HeraJobVo heraJobVo)
    {
        return heraJobMapper.selectManJobCountByUserName(heraJobVo);
    }

    @Override
    public int selectfailedCountByUserName(HeraJobVo heraJobVo)
    {
        return heraJobMapper.selectFailedCountByUserName(heraJobVo);
    }

    @Override
    public int selectJobStartCount()
    {
        return heraJobMapper.selectJobStartCount();
    }

    @Override
    public String getPWDbyUserName(String user)
    {
        return heraJobMapper.getPWDbyUserName(user);
    }

    @Override
    public int updatePwdByUser(String user, String newP1)
    {
        return heraJobMapper.updatePwdByUser(user, newP1);
    }

    @Override
    public List<HeraJob> findSqlJob()
    {
        return heraJobMapper.findSqlJob();
    }

    /**
     * 自助取数关闭任务
     *
     * @param heraJob
     * @return
     */
    @Override
    public int updateForZZQS(HeraJob heraJob)
    {
        return heraJobMapper.updateForZZQS(heraJob);
    }

    /**
     * 自助取数关闭任务
     *
     * @param heraJob
     * @return
     */
    @Override
    public int deleteForZZQS(HeraJob heraJob)
    {
        return heraJobMapper.deleteForZZQS(heraJob);
    }

    public HeraAction getTodayEarliestActionId(String jobId)
    {
        return heraJobMapper.getTodayEarliestActionId(jobId);
    }

    @Override
    public int updateOwnerToAdmin(String admin, String owner)
    {
        return heraJobMapper.updateOwnerToAdmin(admin, owner);
    }

    @Override
    public List<JobDownDenpends> getTodayAllActionId()
    {
        return heraJobMapper.getTodayAllActionId();
    }

    @Override
    public List<HeraAction> batchHeraJobSelect(ArrayList<String> hs)
    {
        return heraJobMapper.batchHeraJobSelect(new ArrayList<>(hs));
    }

    @Override
    public int batchUpdate(List<HeraAction> heraActions)
    {
        return heraJobMapper.batchUpdate(heraActions);
    }

    //定时加载依赖数据
    @Scheduled(cron = "0 */30 * * * ?")
    public void loadDeps()
    {
        System.out.println("加载数据");
        List<HeraJob> heraJobs = heraJobMapper.getAll();
        //清空原来的
        upDepsMap.clear();
        downDepsMap.clear();
        //重新加载
        heraJobs.forEach(heraJob -> {
            int jobId = heraJob.getId();
            String dependencies = heraJob.getDependencies();
            upDepsMap.put(jobId, dependencies);
            ArrayList<String> downDeps = new ArrayList<>();

            heraJobs.forEach(job -> {
                String deps = job.getDependencies() == null ? "" : job.getDependencies();
                List<String> data = Arrays.asList(deps.split(","));
                if (data.contains(String.valueOf(jobId))) {
                    downDeps.add(String.valueOf(job.getId()));
                }
            });
            downDepsMap.put(jobId, downDeps);
        });
        System.out.println("加载结束。。。。");
    }
}
