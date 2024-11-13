package com.dfire.controller;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.dfire.common.constants.Constants;
import com.dfire.common.entity.*;
import com.dfire.common.entity.model.JsonResponse;
import com.dfire.common.entity.model.TablePageForm;
import com.dfire.common.entity.model.TableResponse;
import com.dfire.common.entity.vo.*;
import com.dfire.common.enums.JobScheduleTypeEnum;
import com.dfire.common.enums.StatusEnum;
import com.dfire.common.enums.TriggerTypeEnum;
import com.dfire.common.service.*;
import com.dfire.common.util.ActionUtil;
import com.dfire.common.util.BeanConvertUtils;
import com.dfire.common.util.NamedThreadFactory;
import com.dfire.common.util.StringUtil;
import com.dfire.common.vo.GroupTaskVo;
import com.dfire.common.config.HeraEnvForTest;
import com.dfire.common.config.HeraGlobalEnvironment;
import com.dfire.config.UnCheckLogin;
import com.dfire.core.netty.worker.WorkClient;
import com.dfire.common.logs.ErrorLog;
import com.dfire.common.logs.MonitorLog;
import com.dfire.common.logs.WorkerLog;
import com.dfire.protocol.JobExecuteKind;
import com.dfire.util.ReadHdfsFile;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.quartz.CronExpression;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.WebAsyncTask;

import javax.mail.MessagingException;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

import static com.dfire.common.util.StringUtil.EncoderByMd5;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 16:50 2018/1/13
 * @desc 调度中心视图管理器
 */
@RestController
@RequestMapping("/scheduleCenter")
public class ScheduleCenterController
        extends BaseHeraController
{

    private final String JOB = "job";
    private final String GROUP = "group";
    private final String ERROR_MSG = "抱歉，您没有权限进行此操作";
    @Autowired
    @Qualifier("heraJobMemoryService")
    private HeraJobService heraJobService;
    @Autowired
    private HeraJobActionService heraJobActionService;
    @Autowired
    @Qualifier("heraGroupMemoryService")
    private HeraGroupService heraGroupService;
    @Autowired
    private HeraJobHistoryService heraJobHistoryService;
    @Autowired
    private HeraJobMonitorService heraJobMonitorService;
    @Autowired
    private HeraUserService heraUserService;
    @Autowired
    private HeraPermissionService heraPermissionService;
    @Autowired
    private WorkClient workClient;
    @Autowired
    private HeraHostGroupService heraHostGroupService;
    @Autowired
    private HeraAreaService heraAreaService;
    private final ThreadPoolExecutor poolExecutor;

    {
        poolExecutor = new ThreadPoolExecutor(
                1, Runtime.getRuntime().availableProcessors() * 4, 60L, TimeUnit.SECONDS, new LinkedBlockingQueue<>(), new NamedThreadFactory("updateJobThread"), new ThreadPoolExecutor.AbortPolicy());
        poolExecutor.allowCoreThreadTimeOut(true);
    }

    @RequestMapping()
    public String login(@RequestParam("syFlag") int syFlag, ModelMap map)
    {
        map.addAttribute("syFlag", syFlag);
        return "scheduleCenter/scheduleCenter.index";
    }

    @PostMapping(value = "/init")
    public Map<String, List<HeraJobTreeNodeVo>> initJobTree()
    {
        return heraJobService.buildJobTree(getOwner());
    }

    @GetMapping(value = "/getJobMessage")
    public HeraJobVo getJobMessage(Integer jobId)
    {
        HeraJob job = heraJobService.findById(jobId);
        // System.err.println(job);

        HeraJobVo heraJobVo = BeanConvertUtils.convert(job);
        heraJobVo.setInheritConfig(getInheritConfig(job.getGroupId()));
        HeraJobMonitor monitor = heraJobMonitorService.findByJobId(jobId);
        StringBuilder focusUsers = new StringBuilder("[ ");
        if (monitor != null && StringUtils.isNotBlank(monitor.getUserIds())) {
            String ownerId = getOwnerId();
            String[] ids = monitor.getUserIds().split(Constants.COMMA);
            Arrays.stream(ids).forEach(id -> {
                if (ownerId.equals(id)) {
                    heraJobVo.setFocus(true);
                }
                if (StringUtils.isNotEmpty(id) && !id.equalsIgnoreCase("null")) {
                    //System.err.println("getJobMessage__id :"+id);
                    HeraUser heraUser = heraUserService.findById(Integer.valueOf(id));
                    focusUsers.append(heraUser.getName() + " ");
                }
            });
        }

        HeraHostGroup hostGroup = heraHostGroupService.findById(job.getHostGroupId());
        focusUsers.append("]");
        if (hostGroup != null) {
            heraJobVo.setHostGroupName(hostGroup.getName());
        }
        heraJobVo.setUIdS(getuIds(jobId));
        heraJobVo.setFocusUser(focusUsers.toString());

        return heraJobVo;
    }

    /**
     * 组下搜索任务
     *
     * @param groupId groupId
     * @param type 0：all 所有任务 1:running 运行中的任务
     * @param pageForm layui table分页参数
     * @return 结果
     */
    @GetMapping(value = "/getGroupTask")
    public TableResponse<List<GroupTaskVo>> getGroupTask(@RequestParam String groupId, @RequestParam int type, @RequestParam TablePageForm pageForm)
    {

        List<HeraGroup> group = heraGroupService.findDownStreamGroup(getGroupId(groupId));

        Set<Integer> groupSet = group.stream().map(HeraGroup::getId).collect(Collectors.toSet());
        List<HeraJob> jobList = heraJobService.getAll();
        Set<Integer> jobIdSet = jobList.stream().filter(job -> groupSet.contains(job.getGroupId())).map(HeraJob::getId).collect(Collectors.toSet());

        Calendar calendar = Calendar.getInstance();
        String startDate = ActionUtil.getFormatterDate("yyyyMMdd", calendar.getTime());
        calendar.add(Calendar.DAY_OF_MONTH, +1);
        String endDate = ActionUtil.getFormatterDate("yyyyMMdd", calendar.getTime());
        List<GroupTaskVo> taskVos = heraJobActionService.findByJobIds(new ArrayList<>(jobIdSet), startDate, endDate, pageForm, type);
        return new TableResponse<>(pageForm.getCount(), 0, taskVos);
    }

    @GetMapping(value = "/getGroupMessage/{groupId}")
    public HeraGroupVo getGroupMessage(@PathVariable String groupId)
    {
        int id = getGroupId(groupId);
        HeraGroup group = heraGroupService.findById(id);
        HeraGroupVo groupVo = BeanConvertUtils.convert(group);
        groupVo.setInheritConfig(getInheritConfig(groupVo.getParent()));
        groupVo.setUIdS(getuIds(id));
        return groupVo;
    }

    @PostMapping(value = "/updatePermissionSelf")
    @Transactional(rollbackFor = Exception.class)
    public JsonResponse updatePermissionSelf(@RequestBody Map<String, Object> requests)
    {
        String id = (String) requests.get("id");
        boolean type = (boolean) requests.get("type");
        String names = (String) requests.get("uIdS");
        int newId = getGroupId(id);
        //System.err.println("0");
        if (!hasPermission(newId, type ? GROUP : JOB)) {
            return new JsonResponse(false, ERROR_MSG);
        }
        //System.err.println("1");
        Integer integer = null;
        //System.err.println("1.0");
        integer = heraPermissionService.deleteByTargetId(newId);
        if (integer == null) {
            return new JsonResponse(false, "修改失败");
        }
        HeraPermission heraPermission = new HeraPermission();
        if (names != null) {
            String typeStr = type ? "group" : "job";
            Date date = new Date();
            Long targetId = Long.parseLong(String.valueOf(newId));
            String uId = names;
            heraPermission.setType(typeStr);
            heraPermission.setGmtModified(date);
            heraPermission.setGmtCreate(date);
            heraPermission.setTargetId(targetId);
            heraPermission.setUid(uId);
        }
        int res = heraPermissionService.insert(heraPermission);
        if (res < 1) {
            //System.err.println("修改失败");
            MonitorLog.info("任务id={}【自动添加管理员】失败 管理员:{}", id, getOwner());
            return new JsonResponse(false, "修改失败");
        }
        //System.err.println("修改成功");
        MonitorLog.info("任务id={}【自动添加管理员】成功 管理员:{}", id, getOwner());
        return new JsonResponse(true, "修改成功");
    }

    @PostMapping(value = "/updatePermission")
    @Transactional(rollbackFor = Exception.class)
    public JsonResponse updatePermission(@RequestBody Map<String, Object> requests)
    {
        String id = (String) requests.get("id");
        boolean type = (boolean) requests.get("type");
        String names = (String) requests.get("uIdS");
        int newId = getGroupId(id);
        if (!hasPermission(newId, type ? GROUP : JOB)) {
            return new JsonResponse(false, ERROR_MSG);
        }
        // System.err.println(names);
        JSONArray uIdS = JSONArray.parseArray(names);
        Integer integer = heraPermissionService.deleteByTargetId(newId);
        if (integer == null) {
            return new JsonResponse(false, "修改失败");
        }
        if (uIdS != null && !uIdS.isEmpty()) {
            String typeStr = type ? "group" : "job";
            Date date = new Date();
            Long targetId = Long.parseLong(String.valueOf(newId));
            List<HeraPermission> permissions = new ArrayList<>(uIdS.size());
            for (Object uId : uIdS) {
                HeraPermission heraPermission = new HeraPermission();
                heraPermission.setType(typeStr);
                heraPermission.setGmtModified(date);
                heraPermission.setGmtCreate(date);
                heraPermission.setTargetId(targetId);
                heraPermission.setUid((String) uId);
                permissions.add(heraPermission);
            }
            Integer res = heraPermissionService.insertList(permissions);
            if (res == null || res != uIdS.size()) {
                return new JsonResponse(false, "修改失败");
            }
        }

        return new JsonResponse(true, "修改成功");
    }

    @GetMapping(value = "/getJobOperator")
    public JsonResponse getJobOperator(@RequestParam String jobId, @RequestParam boolean type)
    {
        int groupId = getGroupId(jobId);
        if (!hasPermission(groupId, type ? GROUP : JOB)) {
            return new JsonResponse(false, ERROR_MSG);
        }
        //select * from hera_permission where target_id =#{targetId}
        List<HeraPermission> permissions = heraPermissionService.findByTargetId(groupId);
        //select name from hera_user
        List<HeraUser> all = heraUserService.findAllName();

        if (all == null || permissions == null) {
            return new JsonResponse(false, "发生错误，请联系管理员");
        }
        if (jobId.contains("group")) {
            Map<String, Object> res = new HashMap<>(2);
            res.put("allUser", all);
            res.put("admin", permissions);
            return new JsonResponse(true, "查询成功", res);
        }
        else {
            String jobMonitorInfo = getJobMonitorInfo(Integer.parseInt(jobId));
            Map<String, Object> res = new HashMap<>(2);
            res.put("allUser", all);
            res.put("admin", permissions);
            res.put("monitor", jobMonitorInfo);
            return new JsonResponse(true, "查询成功", res);
        }
    }

    /**
     * 手动执行任务/手动恢复任务triggerType=2
     *
     * @return
     */
    @PostMapping(value = "/manualForOvertime")
    @UnCheckLogin
    public WebAsyncTask<JsonResponse> manualForOvertime(@RequestBody Map<String, Object> requests)
    {
        String owner = (String) requests.get("owner");
        String actionId = (String) requests.getOrDefault("actionId", null);
        int triggerType = (int) requests.getOrDefault("triggerType", 0);
        TriggerTypeEnum triggerTypeEnum;
        HeraAction heraAction = heraJobActionService.findById(actionId);
        HeraJob heraJob = heraJobService.findById(heraAction.getJobId());
        String configs;
        configs = heraJob.getConfigs();
        if (triggerType == 2) {
            triggerTypeEnum = TriggerTypeEnum.MANUAL_RECOVER;
            try {
                configs = configs.split("\"roll.back.times\":\"[\\d]+[\"]")[0] + "\"roll.back.times\":\"0\"" + configs.split("\"roll.back.times\":\"[\\d]+[\"]")[1];
            }
            catch (Exception e) {
                configs = heraJob.getConfigs();
                e.printStackTrace();
            }
        }
        else if (triggerType == 3) {
            triggerTypeEnum = TriggerTypeEnum.MANUAL;
            configs = heraJob.getConfigs();
        }
        else {
            triggerTypeEnum = TriggerTypeEnum.MANUAL;
            configs = heraJob.getConfigs();
        }
        //System.err.println("2  configs : "+configs);
        HeraJobHistory actionHistory = HeraJobHistory.builder().build();
        actionHistory.setJobId(heraAction.getJobId());
        actionHistory.setActionId(heraAction.getId().toString());
        actionHistory.setTriggerType(triggerTypeEnum.getId());
        actionHistory.setOperator(heraJob.getOwner());
        actionHistory.setIllustrate(owner);
        actionHistory.setStatus(StatusEnum.RUNNING.toString());
        actionHistory.setStatisticEndTime(heraAction.getStatisticEndTime());
        actionHistory.setHostGroupId(heraAction.getHostGroupId());
        actionHistory.setProperties(configs);

        //System.err.println("actionHistory : " + actionHistory);
        heraJobHistoryService.insert(actionHistory);

        heraAction.setScript(heraJob.getScript());
        heraAction.setHistoryId(actionHistory.getId());
        heraAction.setConfigs(configs);
        heraAction.setAuto(heraJob.getAuto());
        heraAction.setHostGroupId(heraJob.getHostGroupId());

        heraJobActionService.update(heraAction);

        WebAsyncTask<JsonResponse> webAsyncTask = new WebAsyncTask<>(HeraGlobalEnvironment.getRequestTimeout(), () -> {
            try {
                //System.err.println("m_" + JobExecuteKind.ExecuteKind.ManualKind + "  _   " + actionHistory.getId());
                workClient.executeJobFromWeb(JobExecuteKind.ExecuteKind.ManualKind, actionHistory.getId());
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            return new JsonResponse(true, actionId);
        });
        webAsyncTask.onTimeout(() -> new JsonResponse(false, "执行任务操作请求中，请稍后"));
        return webAsyncTask;
    }

    //当前正在重刷历史任务的集合
    static List<String> runningJobs = Collections.synchronizedList(new ArrayList<String>());

    /**
     * 恢复当前任务的历史数据，不跑下游任务.
     *
     * @return
     */
    @PostMapping(value = "/manualHistory")
    @UnCheckLogin
    public JsonResponse manualHistory(@RequestBody Map<String, String> requests )
            throws Exception
    {
        String jobId = requests.get("jobId");
        String startDay = requests.get("startDay");
        String endDay = requests.get("endDay");
        String actionId = requests.get("actionId");
        int triggerType = Integer.parseInt(requests.get("triggerType"));
        //0 检查是否有任务在跑
        if (runningJobs.contains(jobId)) {
            return new JsonResponse(false, "该任务已经提交！");
        }

        String user = getOwner();
        //1 检查日期
        if (startDay.compareTo(endDay) > 0) {
            return new JsonResponse(false, "结束时间必须大于等于开始时间！");
        }
        else if (startDay.compareTo(endDay) < 0) {
            runningJobs.add(jobId);
        }

        //2 跑第一天
        WebAsyncTask<JsonResponse> asyncTask = execute(requests);
        JsonResponse res = (JsonResponse) asyncTask.getCallable().call();
        if (startDay.equals(endDay)) {
            return res;
        }

        //3 开启另一个线程进行处理后面的日期
        if (res.isSuccess()) {
            List<String> dateList = getDateList(startDay, endDay);
            dateList.remove(startDay);

            new Thread(new Runnable()
            {
                @SneakyThrows
                @Override
                public void run()
                {
                    try {
                        String lastDay = startDay;
                        for (String runDay : dateList) {
                            //查询任务执行状态
                            String jobStatus = judgeStatus(actionId);
                            if ("failed".equals(jobStatus)) {
                                emailService.sendEmail(jobId + " 任务重刷历史数据失败", jobId + "任务在刷" + lastDay + "这天数据时失败，请检查失败原因！", heraUserService.findByName(user).getEmail());
                                //发送邮件
                                return;
                            }

                            lastDay = runDay;

                            //执行任务
                            WebAsyncTask<JsonResponse> asyncTask = execute(requests);
                            JsonResponse res = (JsonResponse) asyncTask.getCallable().call();
                            if (!res.isSuccess()) {
                                emailService.sendEmail(jobId + " 任务重刷历史数据失败", jobId + "任务在" + runDay + "这天提交时失败，失败原因：" + res.getMessage(), heraUserService.findByName(user).getEmail());
                                return;
                            }
                        }
                        emailService.sendEmail(jobId + " 任务重刷历史数据完成", jobId + "重刷历史数据完成：" + startDay + "-" + endDay, heraUserService.findByName(user).getEmail());
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                    finally {
                        runningJobs.remove(jobId);
                    }
                }
            }).start();
        }

        //返回第一天执行结果
        return res;
    }

    //判断任务状态
    public String judgeStatus(String actionId)
            throws InterruptedException
    {
        while (true) {
            Thread.sleep(30 * 1000L);
            List<HeraJobHistory> jobStatus = heraJobHistoryService.getJobStatus(Long.parseLong(actionId));
            String status = jobStatus.get(0).getStatus();
            System.out.println("任务的状态是： " + status);
            if ("failed".equals(status) || "success".equals(status)) {
                return status;
            }
        }
    }

    /**
     * 获取日期范围
     *
     * @param startTime 开始日期    yyyy-MM-dd格式
     * @param endTime 结束日期    yyyy-MM-dd格式
     * @return 日期集合
     * @throws ParseException
     */
    public static List<String> getDateList(String startTime, String endTime)
            throws ParseException
    {
        ArrayList<String> dates = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Calendar cal = Calendar.getInstance();
        Date startDay = sdf.parse(startTime);
        Date endDay = sdf.parse(endTime);

        Date currentDay = startDay;
        while (currentDay.getTime() <= endDay.getTime()) {
            String currentDayString = sdf.format(currentDay);
            dates.add(currentDayString);

            cal.setTime(currentDay);
            cal.add(Calendar.DATE, 1);
            currentDay = cal.getTime();
        }

        return dates;
    }

    /**
     * 手动执行任务/手动恢复任务triggerType=2
     *
     * params:
     * String actionId,
     * Integer triggerType,
     * String owner,
     * String runDay
     * @return
     */
    @PostMapping(value = "/manual")
    @UnCheckLogin
    public WebAsyncTask<JsonResponse> execute(@RequestBody Map<String, String> requests)
    {
        String actionId = requests.get("actionId");
        int triggerType = Integer.parseInt(requests.get("triggerType"));
        String owner = requests.getOrDefault("owner", null);
        String runDay = requests.getOrDefault("runDay", null);
        System.out.println("runDay:" + runDay);
        if (owner == null && !hasPermission(Integer.parseInt(actionId.substring(actionId.length() - 4)), JOB)) {
            return new WebAsyncTask<>(() -> new JsonResponse(false, ERROR_MSG));
        }
        TriggerTypeEnum triggerTypeEnum;
        HeraAction heraAction = heraJobActionService.findById(actionId);
        HeraJob heraJob = heraJobService.findById(heraAction.getJobId());

        if (owner == null) {
            owner = super.getOwner();
        }
        if (owner == null) {
            throw new IllegalArgumentException("任务执行人为空");
        }
        String configs;
        //configs = heraJob.getConfigs();
        configs = heraAction.getConfigs();
        if (triggerType == 2 && runDay != null) { //实现重刷历史任务
            Map<String, String> map1 = StringUtil.convertStringToMap(configs);
            map1.put("pt_day", "'" + runDay + "'");
            configs = JSONObject.toJSONString(map1);
        }

        if (triggerType == 2) {
            triggerTypeEnum = TriggerTypeEnum.MANUAL_RECOVER;
            try {
                configs = configs.split("\"roll.back.times\":\"[\\d]+[\"]")[0] + "\"roll.back.times\":\"0\"" + configs.split("\"roll.back.times\":\"[\\d]+[\"]")[1];
            }
            catch (Exception e) {
                configs = heraAction.getConfigs();
                // configs = heraJob.getConfigs();
                e.printStackTrace();
            }
        }
        else if (triggerType == 3) {
            triggerTypeEnum = TriggerTypeEnum.MANUAL;
            configs = heraAction.getConfigs();
        }
        else {
            triggerTypeEnum = TriggerTypeEnum.MANUAL;
            configs = heraAction.getConfigs();
            //如果configs配置中有pt_day，将它删除掉，防止因为恢复下游操作写进去的pt_day影响手动执行任务，造成数据错误。
            Map<String, String> map1 = StringUtil.convertStringToMap(configs);
            map1.remove("pt_day");
            configs = JSONObject.toJSONString(map1);
        }
        //System.err.println("2  configs : "+configs);
        HeraJobHistory actionHistory = HeraJobHistory.builder().build();
        actionHistory.setJobId(heraAction.getJobId());
        actionHistory.setActionId(heraAction.getId().toString());
        actionHistory.setTriggerType(triggerTypeEnum.getId());
        actionHistory.setOperator(heraJob.getOwner());
        actionHistory.setIllustrate(owner);
        actionHistory.setStatus(StatusEnum.RUNNING.toString());
        actionHistory.setStatisticEndTime(heraAction.getStatisticEndTime());
        actionHistory.setHostGroupId(heraAction.getHostGroupId());
        actionHistory.setProperties(configs);

        //System.err.println("actionHistory : " + actionHistory);
        heraJobHistoryService.insert(actionHistory);

        heraAction.setScript(heraJob.getScript());
        heraAction.setHistoryId(actionHistory.getId());
        heraAction.setConfigs(configs);
        heraAction.setAuto(heraJob.getAuto());
        heraAction.setHostGroupId(heraJob.getHostGroupId());

        heraJobActionService.update(heraAction);

        WebAsyncTask<JsonResponse> webAsyncTask = new WebAsyncTask<>(HeraGlobalEnvironment.getRequestTimeout(), () -> {
            try {
                workClient.executeJobFromWeb(JobExecuteKind.ExecuteKind.ManualKind, actionHistory.getId());
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            return new JsonResponse(true, actionId);
        });
        webAsyncTask.onTimeout(() -> new JsonResponse(false, "执行任务操作请求中，请稍后"));
        return webAsyncTask;
    }

    @RequestMapping(value = "/manualForReRun", method = RequestMethod.GET)
    @UnCheckLogin
    public WebAsyncTask<JsonResponse> manualForReRun(String actionId, Integer triggerType, @RequestParam(required = false) String owner)
    {

        if (owner == null && !hasPermission(Integer.parseInt(actionId.substring(actionId.length() - 4)), JOB)) {
            return new WebAsyncTask<>(() -> new JsonResponse(false, ERROR_MSG));
        }
        TriggerTypeEnum triggerTypeEnum;
        HeraAction heraAction = heraJobActionService.findById(actionId);
        HeraJob heraJob = heraJobService.findById(heraAction.getJobId());

        if (owner == null) {
            owner = super.getOwner();
        }
        if (owner == null) {
            throw new IllegalArgumentException("任务执行人为空");
        }
        String configs;
        configs = heraAction.getConfigs();

        if (triggerType == 2) {
            triggerTypeEnum = TriggerTypeEnum.MANUAL_RECOVER;
            try {
                configs = configs.split("\"roll.back.times\":\"[\\d]+[\"]")[0] + "\"roll.back.times\":\"0\"" + configs.split("\"roll.back.times\":\"[\\d]+[\"]")[1];
            }
            catch (Exception e) {
                configs = heraAction.getConfigs();
                e.printStackTrace();
            }
        }
        else if (triggerType == 3) {
            triggerTypeEnum = TriggerTypeEnum.MANUAL;
            configs = heraAction.getConfigs();
        }
        else {
            triggerTypeEnum = TriggerTypeEnum.MANUAL;
            configs = heraAction.getConfigs();
        }
        HeraJobHistory actionHistory = HeraJobHistory.builder().build();
        actionHistory.setJobId(heraAction.getJobId());
        actionHistory.setActionId(heraAction.getId().toString());
        actionHistory.setTriggerType(triggerTypeEnum.getId());
        actionHistory.setOperator(heraJob.getOwner());
        actionHistory.setIllustrate(owner);
        actionHistory.setStatus(StatusEnum.RUNNING.toString());
        actionHistory.setStatisticEndTime(heraAction.getStatisticEndTime());
        actionHistory.setHostGroupId(heraAction.getHostGroupId());
        actionHistory.setProperties(configs);

        heraJobHistoryService.insert(actionHistory);

        heraAction.setHistoryId(actionHistory.getId());
        heraAction.setConfigs(configs);
        heraAction.setAuto(heraJob.getAuto());
        heraAction.setHostGroupId(heraJob.getHostGroupId());

        heraJobActionService.update(heraAction);

        WebAsyncTask<JsonResponse> webAsyncTask = new WebAsyncTask<>(HeraGlobalEnvironment.getRequestTimeout(), () -> {
            try {
                workClient.executeJobFromWeb(JobExecuteKind.ExecuteKind.ManualKind, actionHistory.getId());
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            return new JsonResponse(true, actionId);
        });
        webAsyncTask.onTimeout(() -> new JsonResponse(false, "执行任务操作请求中，请稍后"));
        return webAsyncTask;
    }

    @GetMapping(value = "/forceRunGetStatusById/{taskId}")
    public String forceRunGetStatusById(@PathVariable String taskId)
    {
        return heraJobActionService.getStatus(taskId);
    }

    static HashSet<String> taskSet = new HashSet<>();

    public static String[] reverse(String[] a)
    {
        String[] b = a;
        for (int start = 0, end = b.length - 1; start < end; start++, end--) {
            String temp = b[start];
            b[start] = b[end];
            b[end] = temp;
        }
        return b;
    }

    /*  static int RetryTaskRank = 0;*/
    static LinkedHashMap<String, String> taskMap = new LinkedHashMap<>();

    /**/
    public boolean judgeFirstTask(String id)
    {
        //String id="201909191400000007";
        String upJobs = heraJobActionService.getUpDependsId(id);
        //System.err.println(id+"找到头了");
        return !StringUtils.isNotEmpty(upJobs);
    }

    @GetMapping(value = "/getJobVersion/{jobId}")
    public List<HeraActionVo> getJobVersion(@PathVariable String jobId)
    {
        List<HeraActionVo> list = new ArrayList<>();
        List<String> idList = heraJobActionService.getActionVersionByJobId(Long.parseLong(jobId));
        String[] arr;
        String vId;
        String vStatus;
        for (String id : idList) {
            //System.err.println("id :"+id);
            arr = id.split("\\+");
            try {
                vId = arr[0];
                vStatus = arr[1];
            }
            catch (Exception e) {
                vId = id;
                vStatus = "";
            }
            list.add(HeraActionVo.builder().id(vId).status(vStatus).build());
        }
        return list;
    }

    @PostMapping(value = "/updateJobMessage")
    public JsonResponse updateJobMessage(@RequestBody HeraJobVo heraJobVo)
    {
        //System.err.println("/updateJobMessage heraJobVo :"+heraJobVo);

        if (!hasPermission(heraJobVo.getId(), JOB)) {
            return new JsonResponse(false, ERROR_MSG);
        }
        if (StringUtils.isBlank(heraJobVo.getDescription())) {
            return new JsonResponse(false, "描述不能为空");
        }
        try {
            new CronExpression(heraJobVo.getCronExpression());
        }
        catch (ParseException e) {
            return new JsonResponse(false, "定时表达式不准确，请核实后再保存");
        }

        HeraHostGroup hostGroup = heraHostGroupService.findById(heraJobVo.getHostGroupId());
        if (hostGroup == null) {
            return new JsonResponse(false, "机器组不存在，请选择一个机器组");
        }

        if (StringUtils.isBlank(heraJobVo.getAreaId())) {
            return new JsonResponse(false, "至少选择一个任务所在区域");
        }

        //如果是依赖任务
        if (heraJobVo.getScheduleType() == 1) {
            String dependencies = heraJobVo.getDependencies();
            if (StringUtils.isNotBlank(dependencies)) {
                String[] jobs = dependencies.split(Constants.COMMA);
                HeraJob heraJob;
                boolean jobAuto = true;
                StringBuilder sb = null;
                for (String job : jobs) {
                    heraJob = heraJobService.findById(Integer.parseInt(job));
                    if (heraJob == null) {
                        return new JsonResponse(false, "任务:" + job + "为空");
                    }
                    if (heraJob.getAuto() != 1) {
                        if (jobAuto) {
                            jobAuto = false;
                            sb = new StringBuilder();
                            sb.append(job);
                        }
                        else {
                            sb.append(",").append(job);
                        }
                    }
                }
                if (!jobAuto) {
                    return new JsonResponse(false, "不允许依赖关闭状态的任务:" + sb);
                }
            }
            else {
                return new JsonResponse(false, "请勾选你要依赖的任务");
            }
        }
        else if (heraJobVo.getScheduleType() == 0) {
            heraJobVo.setDependencies("");
        }
        else {
            return new JsonResponse(false, "无法识别的调度类型");
        }
        return heraJobService.checkAndUpdate(BeanConvertUtils.convertToHeraJob(heraJobVo));
    }

    @PostMapping(value = "/updateGroupMessage/{groupId}")
    public JsonResponse updateGroupMessage(@PathVariable String groupId, @RequestBody HeraGroupVo groupVo)
    {
        groupVo.setId(getGroupId(groupId));
        if (!hasPermission(groupVo.getId(), GROUP)) {
            return new JsonResponse(false, ERROR_MSG);
        }
        HeraGroup heraGroup = BeanConvertUtils.convert(groupVo);
        boolean res = heraGroupService.update(heraGroup) > 0;
        return new JsonResponse(res, res ? "更新成功" : "系统异常,请联系管理员");
    }

    @DeleteMapping(value = "/deleteJob/{id}")
    public JsonResponse deleteJob(@PathVariable String id, @RequestParam boolean isGroup)
    {
        int xId = getGroupId(id);
        if (!hasPermission(xId, isGroup ? GROUP : JOB)) {
            return new JsonResponse(false, ERROR_MSG);
        }
        boolean res;
        String check = checkDependencies(xId, isGroup);
        if (StringUtils.isNotBlank(check)) {
            return new JsonResponse(false, check);
        }

        if (isGroup) {
            res = heraGroupService.delete(xId) > 0;
            MonitorLog.info("{}【删除】组{}成功", getOwner(), xId);
            return new JsonResponse(res, res ? "删除成功" : "系统异常,请联系管理员");
        }
        res = heraJobService.delete(xId) > 0;
        //删除任务相关的关注者数据.
        heraJobMonitorService.removeAllMonitor(xId);
        MonitorLog.info("{}【删除】任务{}成功", getOwner(), xId);
        updateJobToMaster(res, xId);
        return new JsonResponse(res, res ? "删除成功" : "系统异常,请联系管理员");
    }

    @PostMapping(value = "/addJob")
    public JsonResponse addJob(@RequestParam String parentId, @RequestBody HeraJob heraJob)
    {
        //System.err.println(heraJob);
        heraJob.setGroupId(getGroupId(parentId));
        if (!hasPermission(heraJob.getGroupId(), GROUP)) {
            return new JsonResponse(false, ERROR_MSG);
        }
        heraJob.setHostGroupId(HeraGlobalEnvironment.defaultWorkerGroup);
        heraJob.setOwner(getOwner());
        heraJob.setScheduleType(JobScheduleTypeEnum.Independent.getType());
        int insert = heraJobService.insert(heraJob);
        if (insert > 0) {
            MonitorLog.info("{}【添加】任务{}成功", heraJob.getOwner(), heraJob.getId());
            //System.err.println(String.valueOf(heraJob.getId()));
            return new JsonResponse(true, String.valueOf(heraJob.getId()));
        }
        else {
            return new JsonResponse(false, "新增失败");
        }
    }

    @PostMapping(value = "/addMonitor/{id}")
    public JsonResponse updateMonitor(@PathVariable int id)
    {
        boolean res = heraJobMonitorService.addMonitor(getOwnerId(), id);
        if (res) {
            MonitorLog.info("{}【关注】任务{}成功", getOwner(), id);
            return new JsonResponse(true, "关注成功");
        }
        else {
            return new JsonResponse(false, "系统异常，请联系管理员");
        }
    }

    @DeleteMapping(value = "/delMonitor/{id}")
    public JsonResponse deleteMonitor(@PathVariable int id)
    {
        boolean res = heraJobMonitorService.removeMonitor(getOwnerId(), id);
        if (res) {
            MonitorLog.info("{}【取关】任务{}成功", getOwner(), id);
            return new JsonResponse(true, "取关成功");
        }
        else {
            return new JsonResponse(false, "系统异常，请联系管理员");
        }
    }

    @PostMapping(value = "/addGroup")
    public JsonResponse addJob(@RequestParam String parentId, @RequestBody HeraGroup heraGroup)
    {
        heraGroup.setParent(getGroupId(parentId));
        if (!hasPermission(heraGroup.getParent(), GROUP)) {
            return new JsonResponse(false, ERROR_MSG);
        }

        Date date = new Date();
        heraGroup.setGmtModified(date);
        heraGroup.setGmtCreate(date);
        heraGroup.setOwner(getOwner());
        heraGroup.setExisted(1);

        int insert = heraGroupService.insert(heraGroup);
        if (insert > 0) {
            MonitorLog.info("{}【添加】组{}成功", getOwner(), heraGroup.getId());
            return new JsonResponse(true, String.valueOf(heraGroup.getId()));
        }
        else {
            return new JsonResponse(false, String.valueOf(-1));
        }
    }

    @RequestMapping(value = "/updateSwitch", method = RequestMethod.POST)
    @ResponseBody
    public JsonResponse updateSwitch(Integer id, Integer status)
    {
        if (!hasPermission(id, JOB)) {
            return new JsonResponse(false, ERROR_MSG);
        }

        HeraJob heraJob = heraJobService.findById(id);

        if (status.equals(heraJob.getAuto())) {
            return new JsonResponse(true, "操作成功");
        }
        //关闭动作 上游关闭时需要判断下游是否有开启任务，如果有，则不允许关闭
        if (status != 1) {
            String errorMsg;
            if ((errorMsg = getJobFromAuto(heraJobService.findDownStreamJob(id), 1)) != null) {
                return new JsonResponse(false, id + "下游存在开启状态任务:" + errorMsg);
            }
        }
        else { //开启动作 如果有上游任务，上游任务不能为关闭状态
            String errorMsg;
            if ((errorMsg = getJobFromAuto(heraJobService.findUpStreamJob(id), 0)) != null) {
                return new JsonResponse(false, id + "上游存在关闭状态任务:" + errorMsg);
            }
        }
        boolean result = heraJobService.changeSwitch(id, status);

        if (result) {
            MonitorLog.info("{}【切换】任务{}状态{}成功", id, status == 1 ? Constants.OPEN_STATUS : status == 0 ? "关闭" : "失效");
        }
        if (status == 1) {
            updateJobToMaster(result, id);
            return new JsonResponse(result, result ? "开启成功" : "开启失败");
        }
        else if (status == 0) {
            return new JsonResponse(result, result ? "关闭成功" : "关闭失败");
        }
        else {
            return new JsonResponse(result, result ? "成功设置为失效状态" : "设置状态失败");
        }
    }

    private String getJobFromAuto(List<HeraJob> streamJob, Integer auto)
    {
        boolean has = false;
        StringBuilder filterJob = null;
        for (HeraJob job : streamJob) {
            if (job.getAuto().equals(auto)) {
                if (!has) {
                    has = true;
                    filterJob = new StringBuilder();
                    filterJob.append(job.getId());
                }
                else {
                    filterJob.append(",").append(job.getId());
                }
            }
        }
        if (has) {
            return filterJob.toString();
        }
        return null;
    }

    @RequestMapping(value = "/generateVersion", method = RequestMethod.POST)
    @ResponseBody
    public WebAsyncTask<String> generateVersion(String jobId)
    {
        // System.err.println("jobId :"+jobId);
        if (!hasPermission(Integer.parseInt(jobId), JOB)) {
            return new WebAsyncTask<>(() -> ERROR_MSG);
        }

        WebAsyncTask<String> asyncTask = new WebAsyncTask<>(HeraGlobalEnvironment.getRequestTimeout(), () ->
                workClient.generateActionFromWeb(JobExecuteKind.ExecuteKind.ManualKind, jobId));
        asyncTask.onTimeout(() -> "版本生成时间较长，请耐心等待下");
        return asyncTask;
    }

    @RequestMapping(value = "/generateAllVersion", method = RequestMethod.GET)
    @ResponseBody
    public WebAsyncTask<String> generateAllVersion()
    {
       /* if (!isAdmin(getOwner())) {
            return new WebAsyncTask<>(() -> ERROR_MSG);
        }*/
        WebAsyncTask<String> asyncTask = new WebAsyncTask<>(HeraGlobalEnvironment.getRequestTimeout(), () ->
                workClient.generateActionFromWeb(JobExecuteKind.ExecuteKind.ManualKind, Constants.ALL_JOB_ID));
        asyncTask.onTimeout(() -> "全量版本生成时间较长，请耐心等待下");
        return asyncTask;
    }

    /**
     * 获取任务历史版本
     *
     * @param pageHelper
     * @return
     */
    @RequestMapping(value = "/getJobHistory", method = RequestMethod.GET)
    @ResponseBody
    public Map<String, Object> getJobHistory(PageHelper pageHelper)
    {
        /*查看信息日志详情
        System.out.println(heraJobHistoryService.findLogByPage(pageHelper));
        */
        return heraJobHistoryService.findLogByPage(pageHelper);
    }

    @RequestMapping(value = "/getHostGroupIds", method = RequestMethod.GET)
    @ResponseBody
    public List<HeraHostGroup> getHostGroupIds()
    {
        return heraHostGroupService.getAll();
    }

    /**
     * 取消正在执行的任务
     *
     * @param jobId
     * @param historyId
     * @return
     */
    @RequestMapping(value = "/cancelJob", method = RequestMethod.GET)
    @ResponseBody
    public WebAsyncTask<String> cancelJob(String historyId, String jobId)
    {
        if (!hasPermission(Integer.parseInt(jobId), JOB)) {
            return new WebAsyncTask<>(() -> ERROR_MSG);
        }

        HeraJobHistory history = heraJobHistoryService.findById(historyId);
        JobExecuteKind.ExecuteKind kind;
        if (TriggerTypeEnum.parser(history.getTriggerType()) == TriggerTypeEnum.MANUAL) {
            kind = JobExecuteKind.ExecuteKind.ManualKind;
        }
        else {
            kind = JobExecuteKind.ExecuteKind.ScheduleKind;
        }

        WebAsyncTask<String> webAsyncTask = new WebAsyncTask<>(HeraGlobalEnvironment.getRequestTimeout(), () ->
                workClient.cancelJobFromWeb(kind, historyId));
        webAsyncTask.onTimeout(() -> "任务取消执行中，请耐心等待");
        return webAsyncTask;
    }

    /**
     * 取消正在执行的任务
     *
     * @param jobId
     * @param historyId
     * @return
     */
    @RequestMapping(value = "/cancelJobForOvertime", method = RequestMethod.GET)
    @ResponseBody
    @UnCheckLogin
    public WebAsyncTask<String> cancelJobForOvertime(String historyId, String jobId)
    {

        HeraJobHistory history = heraJobHistoryService.findById(historyId);
        JobExecuteKind.ExecuteKind kind;
        if (TriggerTypeEnum.parser(history.getTriggerType()) == TriggerTypeEnum.MANUAL) {
            kind = JobExecuteKind.ExecuteKind.ManualKind;
        }
        else {
            kind = JobExecuteKind.ExecuteKind.ScheduleKind;
        }

        WebAsyncTask<String> webAsyncTask = new WebAsyncTask<>(HeraGlobalEnvironment.getRequestTimeout(), () ->
                workClient.cancelJobFromWeb(kind, historyId));
        webAsyncTask.onTimeout(() -> "任务取消执行中，请耐心等待");
        return webAsyncTask;
    }

    @RequestMapping(value = "/cancelJobForTime", method = RequestMethod.GET)
    @ResponseBody
    @UnCheckLogin
    public WebAsyncTask<String> cancelJobForTime(String historyId)
    {
        HeraJobHistory history = heraJobHistoryService.findById(historyId);
        JobExecuteKind.ExecuteKind kind;
        if (TriggerTypeEnum.parser(history.getTriggerType()) == TriggerTypeEnum.MANUAL) {
            kind = JobExecuteKind.ExecuteKind.ManualKind;
        }
        else {
            kind = JobExecuteKind.ExecuteKind.ScheduleKind;
        }

        WebAsyncTask<String> webAsyncTask = new WebAsyncTask<>(HeraGlobalEnvironment.getRequestTimeout(), () ->
                //workClient.cancelJobFromWeb(kind, historyId));
                workClient.cancelJobFromWebForTime(kind, historyId));
        webAsyncTask.onTimeout(() -> "任务取消执行中，请耐心等待");
        return webAsyncTask;
    }

    @RequestMapping(value = "getLog", method = RequestMethod.GET)
    @ResponseBody
    public HeraJobHistory getJobLog(Integer id)
    {
        return heraJobHistoryService.findLogById(id);
    }

    @PostMapping(value = "/execute")
    @UnCheckLogin
    public WebAsyncTask<JsonResponse> zeusExecute(@RequestBody Map<String, String> requests)
    {
        int id = Integer.parseInt(requests.get("id"));
        String owner = requests.get("owner");
        List<HeraAction> actions = heraJobActionService.findByJobId(String.valueOf(id));
        if (actions == null) {
            return new WebAsyncTask<>(() -> new JsonResponse(false, "action为空"));
        }
        requests.put("actionId", actions.get(actions.size() - 1).getId().toString());
        requests.put("triggerType", "2");
        requests.put("owner", owner);
        requests.put("runDay", null);
        return execute(requests);
    }

    private void updateJobToMaster(boolean result, Integer id)
    {
        if (result) {
            poolExecutor.execute(() -> {
                try {
                    workClient.updateJobFromWeb(String.valueOf(id));
                }
                catch (ExecutionException | InterruptedException e) {
                    e.printStackTrace();
                }
            });
        }
    }

    private Map<String, String> getInheritConfig(Integer groupId)
    {
        HeraGroup group = heraGroupService.findConfigById(groupId);
        Map<String, String> configMap = new TreeMap<>();
        while (group != null && groupId != null && groupId != 0) {
            Map<String, String> map = StringUtil.convertStringToMap(group.getConfigs());
            // 多重继承相同变量，以第一个的为准
            for (Map.Entry<String, String> entry : map.entrySet()) {
                String key = entry.getKey();
                if (!configMap.containsKey(key)) {
                    configMap.put(key, entry.getValue());
                }
            }
            groupId = group.getParent();
            group = heraGroupService.findConfigById(groupId);
        }
        return configMap;
    }

    private boolean hasPermission(Integer id, String type)
    {
        String owner = getOwner();
        if (owner == null || id == null || type == null) {
            return false;
        }
        if (isAdmin(owner)) {
            return true;
        }
        if (JOB.equals(type)) {
            HeraJob job = heraJobService.findById(id);
            if (!(job != null && owner.equals(job.getOwner()))) {
                HeraPermission permission = heraPermissionService.findByCond(id, owner);
                if (permission == null) {
                    permission = heraPermissionService.findByCond(job.getGroupId(), owner);
                    return permission != null;
                }
            }
        }
        else if (GROUP.equals(type)) {
            HeraGroup group = heraGroupService.findById(id);
            if (!(group != null && owner.equals(group.getOwner()))) {
                HeraPermission permission = heraPermissionService.findByCond(id, owner);
                return permission != null;
            }
        }

        return true;
    }

    private boolean isAdmin(String owner)
    {
        return HeraGlobalEnvironment.getAdmin().equals(owner);
    }

    private String getuIds(Integer id)
    {
        List<HeraPermission> permissions = heraPermissionService.findByTargetId(id);
        StringBuilder uids = new StringBuilder("[ ");
        if (permissions != null && !permissions.isEmpty()) {
            permissions.forEach(x -> uids.append(x.getUid()).append(" "));
        }
        uids.append("]");

        return uids.toString();
    }

    private String checkDependencies(Integer id, boolean isGroup)
    {
        List<HeraJob> allJobs = heraJobService.getAllJobDependencies();
        if (isGroup) {

            HeraGroup heraGroup = heraGroupService.findById(id);
            if (heraGroup == null) {
                return "组不存在";
            }
            else if (heraGroup.getDirectory() == 1) {
                //如果是小目录
                List<HeraJob> jobList = heraJobService.findByPid(id);
                StringBuilder openJob = new StringBuilder("无法删除存在任务的目录:[ ");
                for (HeraJob job : jobList) {
                    openJob.append(job.getId()).append(" ");
                }
                openJob.append("]");
                if (!jobList.isEmpty()) {
                    return openJob.toString();
                }
                return null;
            }
            else {
                //如果是大目录
                List<HeraGroup> parent = heraGroupService.findByParent(id);

                if (parent == null || parent.isEmpty()) {
                    return null;
                }
                StringBuilder openGroup = new StringBuilder("无法删除存在目录的目录:[ ");
                for (HeraGroup group : parent) {
                    if (group.getExisted() == 1) {
                        openGroup.append(group.getId()).append(" ");
                    }
                }
                openGroup.append("]");
                return openGroup.toString();
            }
        }
        else {
            HeraJob job = heraJobService.findById(id);
            if (job.getAuto() == 1) {
                return "无法删除正在开启的任务";
            }
            boolean canDelete = true;
            boolean isFirst = true;
            String deleteJob = String.valueOf(job.getId());
            StringBuilder dependenceJob = new StringBuilder("任务依赖: ");
            String[] dependenceJobs;
            for (HeraJob allJob : allJobs) {
                if (StringUtils.isNotBlank(allJob.getDependencies())) {
                    dependenceJobs = allJob.getDependencies().split(",");
                    for (String jobId : dependenceJobs) {
                        if (jobId.equals(deleteJob)) {
                            if (canDelete) {
                                canDelete = false;
                            }
                            if (isFirst) {
                                isFirst = false;
                                dependenceJob.append("[").append(job.getId()).append(" -> ").append(allJob.getId()).append(" ");
                            }
                            else {
                                dependenceJob.append(allJob.getId()).append(" ");
                            }
                            break;
                        }
                    }
                }
            }
            dependenceJob.append("]").append("\n");
            if (!canDelete) {
                return dependenceJob.toString();
            }
            return null;
        }
    }

    /**
     * 一键开启/关闭/失效 某job 的上游/下游的所有任务
     *
     * @param jobId jobId
     * @param type 0:上游  1:下游
     * @param auto 0:关闭  1:开启  2:失效
     * @return
     */
    @PostMapping(value = "/switchAll")
    public JsonResponse getJobImpact(@RequestParam int jobId, @RequestParam int type, @RequestParam int auto)
    {
        List<Integer> jobList = heraJobService.findJobImpact(jobId, type);
        if (jobList == null) {
            return new JsonResponse(false, "当前任务不存在");
        }
        int size = jobList.size();
        JsonResponse response;
        if ((type == 0 && auto == 1) || (type == 1 && auto != 1)) {
            for (int i = size - 1; i >= 0; i--) {
                response = this.updateSwitch(jobList.get(i), auto);
                if (!response.isSuccess()) {
                    return response;
                }
            }
        }
        else if ((type == 1 && auto == 1) || (type == 0 && auto != 1)) {
            for (int i = 0; i < size; i++) {
                response = this.updateSwitch(jobList.get(i), auto);
                if (!response.isSuccess()) {
                    return response;
                }
            }
        }
        else {
            return new JsonResponse(false, "未知的type:" + type);
        }
        return new JsonResponse(true, "全部处理成功", jobList);
    }

    @PostMapping(value = "/getJobImpactOrProgress")
    public JsonResponse getJobImpactOrProgress(@RequestParam int jobId, @RequestParam  int type)
    {
        Map<String, Object> graph = heraJobService.findCurrentJobGraph(jobId, type);

        if (graph == null) {
            return new JsonResponse(false, "当前任务不存在");
        }
        return new JsonResponse(true, "成功", graph);
    }

    @GetMapping(value = "/getAllArea")
    public JsonResponse getAllArea()
    {
        List<HeraArea> heraAreas = heraAreaService.findAll();
        if (heraAreas == null) {
            return new JsonResponse(false, "查询异常");
        }
        return new JsonResponse(true, "成功", heraAreas);
    }

    @GetMapping(value = "/check/{id}")
    public JsonResponse check(@PathVariable(required = false) String id)
    {
        if (id == null) {
            return new JsonResponse(true, "查询成功", false);
        }
        if (id.startsWith(Constants.GROUP_PREFIX)) {
            return new JsonResponse(true, "查询成功", hasPermission(getGroupId(id), GROUP));
        }
        else {
            return new JsonResponse(true, "查询成功", hasPermission(Integer.parseInt(id), JOB));
        }
    }

    @PostMapping(value = "/moveNode")
    public JsonResponse moveNode(@RequestParam String id, @RequestParam String parent, @RequestParam String lastParent)
    {
        Integer newParent = getGroupId(parent);
        Integer newId;
        if (id.startsWith(GROUP)) {
            newId = getGroupId(id);
            if (!hasPermission(newId, GROUP)) {
                return new JsonResponse(false, "无权限");
            }
            boolean result = heraGroupService.changeParent(newId, newParent);
            MonitorLog.info("组{}:发生移动 {}  --->  {}", newId, lastParent, newParent);
            return new JsonResponse(result, result ? "处理成功" : "移动失败");
        }
        else {
            newId = Integer.parseInt(id);
            if (!hasPermission(newId, JOB)) {
                return new JsonResponse(false, "无权限");
            }
            boolean result = heraJobService.changeParent(newId, newParent);
            MonitorLog.info("任务{}:发生移动{}  --->  {}", newId, lastParent, newParent);
            return new JsonResponse(result, result ? "处理成功" : "移动失败");
        }
    }

    private int getGroupId(String group)
    {
        String groupNum = group;
        if (group.startsWith(Constants.GROUP_PREFIX)) {
            groupNum = group.split("_")[1];
        }
        int res;
        try {
            res = Integer.parseInt(groupNum);
        }
        catch (Exception e) {
            throw new IllegalArgumentException("无法识别的groupId：" + group);
        }

        return res;
    }

    @GetMapping(value = "/getCurrentUser")
    public String getCurrentUser()
    {
        String owner = getOwner();
        /*System.err.println("owner : "+owner);*/
        MonitorLog.info("当前用户 ：{}", owner);
        return owner;
    }

    @PostMapping(value = "/findByTargetIdSelf")
    @Transactional(rollbackFor = Exception.class)
    public boolean findByTargetIdSelf(@RequestParam("id") String id)
    {
        boolean flag = true;
        String owner = getOwner().trim();
        //System.err.println("owner : "+owner);
        Integer newId = Integer.parseInt(id);
        List<HeraPermission> all = heraPermissionService.findByTargetId(newId);
        for (HeraPermission h : all) {
            if (owner.equalsIgnoreCase(h.getUid().trim())) {
                flag = false;
                break;
            }
        }
        if (owner.equalsIgnoreCase("zhangbinb") || owner.equalsIgnoreCase(HeraGlobalEnvironment.getAdmin())) {
            flag = false;
        }
        return flag;
    }

    public String getJobMonitorInfo(Integer jobId)
    {
        HeraJobMonitor monitor = heraJobMonitorService.findByJobId(jobId);
        StringBuilder focusUsers = new StringBuilder();
        if (monitor != null && StringUtils.isNotBlank(monitor.getUserIds())) {
            String[] ids = monitor.getUserIds().split(Constants.COMMA);
            Arrays.stream(ids).forEach(id -> {
                if (StringUtils.isNotEmpty(id) && !id.equalsIgnoreCase("null")) {
                    //System.err.println("getJobMessage__id :"+id);
                    HeraUser heraUser = heraUserService.findById(Integer.valueOf(id));
                    focusUsers.append(heraUser.getName()).append(",");
                }
            });
        }
        return focusUsers.toString();
    }

    @PostMapping(value = "/addMonitorByOwner")
    public JsonResponse addMonitorByOwner(@RequestParam String names, @RequestParam int id)
    {
        names = StringUtils.strip(names, "[]");
        String ids = getHeraJobIdByName(names);

        if (StringUtils.isNotEmpty(names) && !names.equalsIgnoreCase("null")) {
            boolean res = heraJobMonitorService.addMonitors(ids, id);
            if (res) {
                return new JsonResponse(true, "关注成功");
            }
            else {
                return new JsonResponse(false, "系统异常，请联系管理员");
            }
        }
        else {
            boolean res1 = heraJobMonitorService.removeAllMonitor(id);
            if (res1) {
                MonitorLog.info("任务{}取消所有关注者", id);
                return new JsonResponse(true, "任务{" + id + "}取消所有关注者成功");
            }
            else {
                return new JsonResponse(false, "任务{" + id + "}取消所有关注者失败");
            }
        }
    }

    public String getHeraJobIdByName(String users)
    {

        //System.err.println("users :"+users);
        StringBuilder uu = new StringBuilder();
        String[] user = users.split(",");
        for (String u : user) {
            //System.err.println("u :"+u);
            uu.append(heraUserService.getHeraJobIdByName(u.replace("\"", ""))).append(",");
        }
        //uu.append(",");
        return uu.toString();
    }

    @GetMapping(value = "/getStopHeraJobInfo")
    public int getStopHeraJobInfo()
    {
        return heraJobService.getStopHeraJobInfo();
    }

    // 获取任务运行的通常时间
    @GetMapping(value = "/getTaskCommonTime/{id}")
    public String getTaskCommonTime(@PathVariable String id)
    {
        return heraJobActionService.getTaskCommonTime(id);
    }

    // 获取任务运行的通常时间
    @GetMapping(value = "/hdfsCat")
    public String hdfsCat(@RequestParam("script") String script)
    {
        try {
            if (script.contains("download[hdfs://")) {
                String path = script.split("download\\[hdfs://")[1].split(" ")[0];
                if (path.contains(".jar")) {
                    path = script.split("download\\[hdfs://")[2].split(" ")[0];
                    if (path.contains(".jar")) {
                        path = script.split("download\\[hdfs://")[3].split(" ")[0];
                        return ReadHdfsFile.hdfsCat(path).trim();
                    }
                    return ReadHdfsFile.hdfsCat(path).trim();
                }
                return ReadHdfsFile.hdfsCat(path).trim();
            }
            return "读取hdfs文件异常，联系管理员!";
        }
        catch (Exception e) {
            return "读取hdfs文件异常，联系管理员!";
        }
    }

    @PostMapping(value = "/changePassowrd")
    public String checkPWD(@RequestBody Map<String, String>  requests)
    {
        String user = requests.get("user");
        String pwd = requests.get("pwd");
        String newP1 = requests.get("newP1");
        String newP2 = requests.get("newP2");
        String pwdMd5 = heraJobService.getPWDbyUserName(user).trim();
        String pwd5 = EncoderByMd5(pwd).trim();

        if (!pwdMd5.equals(pwd5)) {
            return "原始密码错误";
        }

        if (!newP1.equals(newP2)) {
            return "新密码两次输入不一致";
        }

        String P1 = EncoderByMd5(newP1);
        if (P1.equals(pwdMd5)) {
            return "新密码和原始密码一致";
        }

        Integer integer = heraJobService.updatePwdByUser(user, P1);

        return "密码修改成功";
    }

    @GetMapping(value = "/getTodayEarliestActionId/{id}")
    public JsonResponse getTodayEarliestActionId(@PathVariable String jobId)
    {
        MonitorLog.info("{}正在构建版本依赖图", jobId);

        try {

            HeraAction todayEarliestActionId = heraJobService.getTodayEarliestActionId(jobId);

            Long actionId = todayEarliestActionId.getId();

            if (actionId != null) {
                return new JsonResponse(true, "查询成功", actionId + "");
            }
            else {
                return new JsonResponse(false, "查询成功", actionId + "");
            }
        }
        catch (Exception e) {
            return new JsonResponse(false, "查询成功", "该任务版本下游无版本依赖，请直接手动恢复");
        }
    }

    private final HashMap<String, List<String>> jobMap = new HashMap<>();

    @Autowired
    private EmailService emailService;
    @Autowired
    HeraEnvForTest heraEnvForTest;

    /**
     * 恢复下游,支持区间恢复
     *
     * @param jobId 恢复的任务
     * @param isReRun 是否回刷历史数据
     * @param startDay 恢复开始的时间
     * @param endDay 恢复结束的时间
     * @param actionId 版本号
     * @return 是否可以开始任务, 下游版本号是否有运行以及修改成功
     */
    @GetMapping(value = "/downRecoveryRun")
    public JsonResponse downRecoveryRun(@RequestParam String jobId, @RequestParam boolean isReRun, @RequestParam String startDay, @RequestParam String endDay, @RequestParam String actionId, @RequestParam int triggerType)
    {
        List<String> ranges = jobMap.getOrDefault(jobId, new ArrayList<>());
        ///是否恢复中
        if (!ranges.isEmpty()) {
            return new JsonResponse(false, "重复操作,job_id=" + jobId + " 恢复下游已提交！", "");
        }
        else {///没有恢复数据,新增恢复数据
        ///获取处理的区间
            getDownRecoveryRange(startDay, endDay, ranges);
            ///检验时间,判断是否可执行
            if (!ranges.isEmpty()) {
                String start = ranges.get(0);
                ranges.remove(start);
                ///修改第一天的状态
                JsonResponse jsonResponse = heraJobService.changeDownActionId(jobId, isReRun, start, true);
                if (jsonResponse.isSuccess() && ranges.size() > 0) {
                    MonitorLog.info("恢复下游数据,启动定时任务");
                    jobMap.put(jobId, ranges);
                    Timer timer = new Timer();
                    String finalOwner = super.getOwner();
                    timer.schedule(new TimerTask()
                    {
                        @Override
                        public void run()
                        {
                            if (ranges.isEmpty()) {
                                JsonResponse jsonResponse1 = heraJobService.changeDownActionId(jobId, isReRun, "runDay", false);
                                if (jsonResponse1.isSuccess() && jsonResponse1.getMessage().equals("全部日期执行完成")) {
                                    stopTimerAndRemoveJobMap(timer, jobMap, jobId);
                                    MonitorLog.info(startDay + "至" + endDay + "下游数据恢复完成(id=" + jobId + "),取消定时任务,并清除JobMap");
                                    try {
                                        emailService.sendEmail(heraEnvForTest.getEnvFlag() + "hera任务(id=" + jobId + ")恢复下游成功!",
                                                startDay + "至" + endDay + "下游数据恢复完成,已取消定时执行队列", heraUserService.findByName(finalOwner).getEmail());
                                    }
                                    catch (MessagingException e) {
                                        e.printStackTrace();
                                        ErrorLog.warn(heraEnvForTest.getEnvFlag() + "hera任务(id=" + jobId + ") " + startDay + "至" + endDay + "下游数据恢复完成,已取消定时执行队列,邮件发送失败!");
                                    }
                                }
                                else {
                                    String message = jsonResponse1.getMessage();
                                    String data = jsonResponse1.getData().toString();
                                    if (message.equals("Last failure")) {
                                        stopTimerAndRemoveJobMap(timer, jobMap, jobId);
                                        WorkerLog.warn(left1Day(endDay, -1) + "下游数据恢复时任务失败,请根据任务id仔细核对依赖图进行重新恢复(id=" + data + ")");
                                        try {
                                            emailService.sendEmail(heraEnvForTest.getEnvFlag() + "hera任务(id=" + data + ")恢复下游失败!",
                                                    left1Day(endDay, -1) + "下游数据恢复时任务失败,请根据任务id仔细核对依赖图进行重新恢复,已取消定时执行队列", heraUserService.findByName(finalOwner).getEmail());
                                        }
                                        catch (MessagingException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                    else if (message.equals("修改任务状态异常.")) {
                                        stopTimerAndRemoveJobMap(timer, jobMap, jobId);
                                        WorkerLog.warn(endDay + "下游数据恢复失败,下游依赖修改异常(id=" + jobId + ")");
                                        try {
                                            emailService.sendEmail(heraEnvForTest.getEnvFlag() + "hera任务(id=" + jobId + ")恢复下游失败!",
                                                    endDay + "下游数据恢复失败,下游依赖修改异常,已取消定时执行队列", heraUserService.findByName(finalOwner).getEmail());
                                        }
                                        catch (MessagingException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                    else {
                                        WorkerLog.warn(message);
                                    }
                                }
                            }
                            else {
                                String runDay = ranges.get(0);
                                //如果当前时间大于23点，则停止刷数据并发送通知邮件.
                                SimpleDateFormat dateFormat = new SimpleDateFormat("HH");
                                String hourStr = dateFormat.format(new Date());
                                int hour = Integer.parseInt(hourStr);
                                if (hour >= 23) {
                                    //取消定时任务并发送邮件
                                    stopTimerAndRemoveJobMap(timer, jobMap, jobId);
                                    WorkerLog.warn("当前时间大于23点，停止" + jobId + "恢复下游");
                                    try {
                                        emailService.sendEmail("停止" + jobId + "恢复下游", "当前时间大于23点，已停止" + jobId + "恢复下游,即将要刷" + runDay + "这天的数据", heraUserService.findByName(finalOwner).getEmail());
                                    }
                                    catch (MessagingException e) {
                                        WorkerLog.warn("发送大于23点停止恢复下游邮件失败！");
                                        e.printStackTrace();
                                    }
                                    return;
                                }

                                WorkerLog.warn("回刷的日期:" + runDay);
                                JsonResponse jsonResponse1 = heraJobService.changeDownActionId(jobId, isReRun, runDay, false);
                                if (jsonResponse1.isSuccess()) {
                                    try {
                                        WebAsyncTask<JsonResponse> webAsyncTask = manualForReRun(actionId, triggerType, finalOwner);
                                        JsonResponse call = (JsonResponse) webAsyncTask.getCallable().call();
                                        if (call.isSuccess()) {
                                            //提交任务成功;
                                            ranges.remove(runDay);
                                            MonitorLog.info("删除区间内日期:" + runDay);
                                        }
                                        else {
                                            stopTimerAndRemoveJobMap(timer, jobMap, jobId);
                                            WorkerLog.warn(runDay + "下游数据恢复失败,提交执行失败(id=" + jobId + ")");
                                            emailService.sendEmail(heraEnvForTest.getEnvFlag() + "hera任务(id=" + jobId + ")恢复下游失败!",
                                                    runDay + "下游数据恢复失败,提交执行失败,已取消定时执行队列", heraUserService.findByName(finalOwner).getEmail());
                                        }
                                    }
                                    catch (Exception e) {
                                        try {
                                            stopTimerAndRemoveJobMap(timer, jobMap, jobId);
                                            ErrorLog.warn(runDay + "数据恢复失败,任务提交执行异常(id=" + jobId + ")");
                                            emailService.sendEmail(heraEnvForTest.getEnvFlag() + "hera任务(id=" + jobId + ")恢复下游异常!",
                                                    runDay + "数据恢复失败,任务提交执行异常", heraUserService.findByName(finalOwner).getEmail());
                                        }
                                        catch (MessagingException ex) {
                                            ex.printStackTrace();
                                            ErrorLog.warn(heraEnvForTest.getEnvFlag() + "hera任务(id=" + jobId + ")恢复下游异常,邮件发送失败!");
                                        }
                                    }
                                }
                                else {
                                    String message = jsonResponse1.getMessage();
                                    String data = jsonResponse1.getData().toString();
                                    if (message.equals("Last failure")) {
                                        stopTimerAndRemoveJobMap(timer, jobMap, jobId);
                                        WorkerLog.warn(left1Day(runDay, -1) + "下游数据恢复时任务失败,请根据任务id仔细核对依赖图进行重新恢复(id=" + data + ")");
                                        try {
                                            emailService.sendEmail(heraEnvForTest.getEnvFlag() + "hera任务(id=" + data + ")恢复下游失败!",
                                                    left1Day(runDay, -1) + "下游数据恢复时任务失败,请根据任务id仔细核对依赖图进行重新恢复,已取消定时执行队列", heraUserService.findByName(finalOwner).getEmail());
                                        }
                                        catch (MessagingException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                    else if (message.equals("修改任务状态异常.")) {
                                        stopTimerAndRemoveJobMap(timer, jobMap, jobId);
                                        WorkerLog.warn(runDay + "下游数据恢复失败,下游依赖修改异常(id=" + jobId + ")");
                                        try {
                                            emailService.sendEmail(heraEnvForTest.getEnvFlag() + "hera任务(id=" + jobId + ")恢复下游失败!",
                                                    runDay + "下游数据恢复失败,下游依赖修改异常,已取消定时执行队列", heraUserService.findByName(finalOwner).getEmail());
                                        }
                                        catch (MessagingException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                    else {
                                        WorkerLog.warn(message);
                                    }
                                }
                            }
                        }
                    }, 1000 * 30, 1000 * 30);
                }
                ///第一天的数据借助ajax执行
                return jsonResponse;
            }
            else {
                return new JsonResponse(false, "请检查你的执行区间", "");
            }
        }
    }

    /**
     * 清空所有的状态 解除定时任务
     *
     * @param timer
     * @param jobMap
     * @param jobId
     */
    public void stopTimerAndRemoveJobMap(Timer timer, HashMap<String, List<String>> jobMap, String jobId)
    {
        WorkerLog.warn("定时任务取消,任务id：" + jobId);
        jobMap.remove(jobId);
        timer.cancel();
    }

    /**
     * 获取时间区间
     *
     * @param startDay 开始的日期
     * @param endDay 结束的日期
     * @param list 升序排序的日期字符串
     */
    public static void getDownRecoveryRange(String startDay, String endDay, List<String> list)
    {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            ///获取后一天的时间
            String currDay = sdf.format(new Date());
            Date nextDay = new Date((sdf.parse(currDay).getTime() / (1000 * 60 * 60 * 24) + 1) * (1000 * 60 * 60 * 24));
            Date start = sdf.parse(startDay);
            Date end = sdf.parse(endDay);
            if (end.before(nextDay) && start.getTime() <= end.getTime()) {
                int diffDays = (int) ((start.getTime() - end.getTime()) / (1000 * 60 * 60 * 24));
                while (diffDays <= 0) {
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(end);
                    calendar.add(Calendar.DATE, diffDays++);
                    String runDay = sdf.format(calendar.getTime());
                    list.add(runDay);
                }
            }
        }
        catch (ParseException e) {
            e.printStackTrace();
        }
    }

    /**
     * 求前num天的日期
     *
     * @param begin
     * @param num
     * @return
     */
    public static String left1Day(String begin, Integer num)
    {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date date = null;
        try {
            date = sdf.parse(begin);
        }
        catch (ParseException e) {
            e.printStackTrace();
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DATE, num);
        return sdf.format(calendar.getTime());
    }

    /**
     * 恢复下游任务
     *
     * @param jobId,actionId
     * @return
     */
    @PostMapping(value = "/recoverImpactOrProgressForDownRecovery")
    public JsonResponse recoverImpactOrProgressForDownRecovery(@RequestParam String jobId, @RequestParam String actionId, @RequestParam int type)
    {

        Map<String, Object> graph = heraJobService.findCurrentJobGraphForDownRecovery(jobId, actionId, type);

        MonitorLog.info("{}构建版本依赖图完成,节点个数:{}.", jobId, graph.size());

        if (graph == null) {
            return new JsonResponse(false, "当前任务不存在");
        }
        return new JsonResponse(true, "成功", graph);
    }
}
