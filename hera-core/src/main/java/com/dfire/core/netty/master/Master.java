package com.dfire.core.netty.master;


import com.dfire.common.constants.Constants;
import com.dfire.common.constants.LogConstant;
import com.dfire.common.entity.HeraAction;
import com.dfire.common.entity.HeraJob;
import com.dfire.common.entity.HeraJobHistory;
import com.dfire.common.entity.HeraUser;
import com.dfire.common.entity.vo.HeraActionVo;
import com.dfire.common.entity.vo.HeraDebugHistoryVo;
import com.dfire.common.entity.vo.HeraJobHistoryVo;
import com.dfire.common.enums.JobScheduleTypeEnum;
import com.dfire.common.enums.StatusEnum;
import com.dfire.common.enums.TriggerTypeEnum;
import com.dfire.common.kv.Tuple;
import com.dfire.common.logs.DebugLog;
import com.dfire.common.logs.ErrorLog;
import com.dfire.common.logs.HeraLog;
import com.dfire.common.logs.ScanLog;
import com.dfire.common.logs.ScheduleLog;
import com.dfire.common.logs.SocketLog;
import com.dfire.common.logs.TaskLog;
import com.dfire.common.util.*;
import com.dfire.common.config.HeraGlobalEnvironment;
import com.dfire.core.HeraException;
import com.dfire.core.event.*;
import com.dfire.core.event.LogHelpUtil;
import com.dfire.core.event.base.ApplicationEvent;
import com.dfire.core.event.base.Events;
import com.dfire.core.event.handler.AbstractHandler;
import com.dfire.core.event.handler.JobHandler;
import com.dfire.core.event.listenter.*;
import com.dfire.core.message.HeartBeatInfo;
import com.dfire.core.netty.master.constant.MasterConstant;
import com.dfire.core.netty.master.response.MasterExecuteJob;
import com.dfire.core.queue.JobElement;
import com.dfire.core.route.loadbalance.LoadBalance;
import com.dfire.core.route.loadbalance.LoadBalanceFactory;
import com.dfire.core.util.CronParse;
import com.dfire.monitor.service.JobFailAlarm;
import com.dfire.protocol.JobExecuteKind;
import com.dfire.protocol.ResponseStatus;
import com.dfire.protocol.RpcResponse;
import io.netty.channel.Channel;
import lombok.Getter;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.springframework.beans.BeanUtils;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.mail.MessagingException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.*;

import static com.dfire.protocol.JobExecuteKind.ExecuteKind.ScheduleKind;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 16:24 2018/1/12
 * @desc hera核心任务调度器
 */
@Component
@Order(1)
public class Master {

    private MasterContext masterContext;
    @Getter
    private ConcurrentHashMap<Long, HeraAction> heraActionMap;
    private ThreadPoolExecutor executeJobPool;

    private volatile boolean isGenerateActioning = false;
    private LoadBalance loadBalance;

    private Channel lastWork;

    /**
     * 提取出job下游的的任务
     *
     * @param dependMap          依赖关系
     * @param selfOrImpactJobMap 存储数据
     * @param jobId              job
     */
    private static void getJobImpact(HashMap<String, HashSet<HeraJob>> dependMap, HashMap<Integer, HeraJob> selfOrImpactJobMap, Integer jobId) {
        HashSet<HeraJob> heraJobs = dependMap.get(jobId.toString());
        if (heraJobs == null || heraJobs.size() == 0) {
            return;
        }
        for (HeraJob heraJob : heraJobs) {
            selfOrImpactJobMap.put(heraJob.getId(), heraJob);
            getJobImpact(dependMap, selfOrImpactJobMap, heraJob.getId());
        }
    }

    public void init(MasterContext masterContext) {
        this.masterContext = masterContext;
        loadBalance = LoadBalanceFactory.getLoadBalance();
        executeJobPool = new ThreadPoolExecutor(HeraGlobalEnvironment.getMaxParallelNum(), HeraGlobalEnvironment.getMaxParallelNum(), 10L, TimeUnit.MINUTES,
                new LinkedBlockingQueue<>(Integer.MAX_VALUE), new NamedThreadFactory("master-execute-job-thread"), new ThreadPoolExecutor.AbortPolicy());
        executeJobPool.allowCoreThreadTimeOut(true);
        if (HeraGlobalEnvironment.getEnv().equalsIgnoreCase(Constants.PRE_ENV)) {
            masterContext.getDispatcher().addDispatcherListener(new HeraStopScheduleJobListener());
        }
        executeJobPool.execute(() -> {
            HeraLog.info("-----------------------------init action,time: {}-----------------------------", System.currentTimeMillis());
            masterContext.getDispatcher().addDispatcherListener(new HeraAddJobListener(this, masterContext));
            masterContext.getDispatcher().addDispatcherListener(new HeraJobFailListener());
            masterContext.getDispatcher().addDispatcherListener(new HeraDebugListener(masterContext));
            masterContext.getDispatcher().addDispatcherListener(new HeraJobSuccessListener(masterContext));
            ///select * from hera_action where id >= ?(202206270000000000写注释的时间20220629)
            List<HeraAction> allJobList = masterContext.getHeraJobActionService().getAfterAction(getBeforeDayAction());
            HeraLog.info("-----------------------------action size:{}, time {}-----------------------------", allJobList.size(), System.currentTimeMillis());
            heraActionMap = new ConcurrentHashMap<>(allJobList.size());
            allJobList.forEach(heraAction -> {
                masterContext.getDispatcher().
                        addJobHandler(new JobHandler(heraAction.getId().toString(), this, masterContext));
                heraActionMap.put(heraAction.getId(), heraAction);
            });
            HeraLog.info("-----------------------------add actions to handler success, time:{}-----------------------------", System.currentTimeMillis());
            masterContext.getDispatcher().forwardEvent(Events.Initialize);
            HeraLog.info("-----------------------------dispatcher actions success, time{}-----------------------------", System.currentTimeMillis());
        });
        masterContext.refreshHostGroupCache();
        HeraLog.info("refresh hostGroup cache");
        // 1.生成版本
        batchActionCheck();
        // 2.扫描任务
        waitingQueueCheck();
        // 3.心跳检查
        heartCheck();
        // 4.漏跑检测
        lostJobCheck();
    }

    private long getBeforeDayAction() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, -HeraGlobalEnvironment.getJobCacheDay());
        return Long.parseLong(ActionUtil.getActionVersionByDate(calendar.getTime()));
    }

    /**
     * 版本定时生成
     */
    private void batchActionCheck() {
        //启动的时候生成版本
        masterContext.masterSchedule.schedule(() -> {
            try {
                generateBatchAction();
                clearInvalidAction();
            } catch (Exception e) {
                ErrorLog.error(LogHelpUtil.getSpecificTrace(e));
                e.printStackTrace();
            }
        }, 30, TimeUnit.SECONDS);

        //只在整点生成版本
        //可以修改period参数修改间隔时间
        masterContext.masterSchedule.scheduleAtFixedRate(() -> {
            try {
                generateBatchAction();
                if (DateTime.now().getHourOfDay() == MasterConstant.MORNING_TIME) {
                    clearInvalidAction();
                }
            } catch (Exception e) {
                ErrorLog.error(LogHelpUtil.getSpecificTrace(e));
                e.printStackTrace();
            }
        }, 60 - new DateTime().getMinuteOfHour(), 60, TimeUnit.MINUTES);
    }

    /**
     * 漏泡检测，清理schedule线程，30分钟调度一次
     * 信号丢失检测
     * job开始检测15分钟之前的漏跑任务
     */
    private void lostJobCheck() {
        int minute = new DateTime().getMinuteOfHour();
        masterContext.masterSchedule.scheduleAtFixedRate(() -> {
            ScheduleLog.info("refresh host group success, start roll back");
            masterContext.refreshHostGroupCache();
            String currDate = ActionUtil.getCurrActionVersion();
            Dispatcher dispatcher = masterContext.getDispatcher();
            if (dispatcher != null) {
                Map<Long, HeraAction> actionMapNew = heraActionMap;
                if (actionMapNew != null && actionMapNew.size() > 0) {
                    List<Long> actionIdList = new ArrayList<>();
                    Long tmp = Long.parseLong(currDate) - MasterConstant.PRE_CHECK_MIN;
                    for (Long actionId : actionMapNew.keySet()) {
                        if (actionId < tmp) {
                            rollBackLostJob(actionId, actionMapNew, actionIdList);
                            checkLostSingle(actionId, actionMapNew);
                        }
                    }
                    ScheduleLog.info("roll back action count:" + actionIdList.size());
                }
                ScheduleLog.info("clear job scheduler ok");
            }
        }, minute <= 30 ? 40 - minute : 70 - minute, 30, TimeUnit.MINUTES);

    }

    /**
     * 漏跑检测
     *
     * @param actionId     版本id
     * @param actionMapNew actionMap集合
     * @param actionIdList 重跑的actionId
     */
    private void rollBackLostJob(Long actionId, Map<Long, HeraAction> actionMapNew, List<Long> actionIdList) {
        HeraAction lostJob = actionMapNew.get(actionId);
        boolean isCheck = lostJob != null
                && lostJob.getAuto() == 1
                && lostJob.getStatus() == null;
        if (isCheck) {
            String dependencies = lostJob.getDependencies();
            if (StringUtils.isNotBlank(dependencies)) {
                List<String> jobDependList = Arrays.asList(dependencies.split(Constants.COMMA));
                boolean isAllComplete = false;
                HeraAction heraAction;

                if (jobDependList.size() > 0) {
                    for (String jobDepend : jobDependList) {
                        heraAction = actionMapNew.get(Long.parseLong(jobDepend));
                        if (heraAction != null) {
                            if (!(isAllComplete = StatusEnum.SUCCESS.toString().equals(heraAction.getStatus()))) {
                                break;
                            }
                        }
                    }
                }

//                if (isAllComplete) {
//                    addRollBackJob(actionIdList, actionId);
//                }
                // TODO: 2020/10/12 漏跑任务过滤:过滤掉冲突任务
                if (isAllComplete) {
                    HeraJobHistory heraJobHistory = masterContext.getHeraJobHistoryService().findStatusByActionId(actionId);
                    if (heraJobHistory == null || StringUtils.isBlank(heraJobHistory.getStatus())) {
                        addRollBackJob(actionIdList, actionId);
                    }
                }
            } else { //独立任务情况
//                addRollBackJob(actionIdList, actionId);
                // TODO: 2020/10/12 漏跑任务过滤:过滤掉冲突任务
                HeraJobHistory heraJobHistory = masterContext.getHeraJobHistoryService().findStatusByActionId(actionId);
                if (heraJobHistory == null || StringUtils.isBlank(heraJobHistory.getStatus())) {
                    addRollBackJob(actionIdList, actionId);
                }
            }
        }
    }

    /**
     * 信号丢失处理
     *
     * @param actionId     hera_action 表信息id /版本id
     * @param actionMapNew hera_action 内存信息 /内存保存的今天版本信息
     */
    private void checkLostSingle(Long actionId, Map<Long, HeraAction> actionMapNew) {
        try {
            HeraAction checkJob = actionMapNew.get(actionId);
            if (checkJob == null) {
                return;
            }
            if (StatusEnum.RUNNING.toString().equals(checkJob.getStatus())) {
                HeraJobHistory actionHistory = masterContext.getHeraJobHistoryService().findById(checkJob.getHistoryId());
                if (actionHistory == null) {
                    return;
                }
                if (actionHistory.getStatus() != null && !actionHistory.getStatus().equals(StatusEnum.RUNNING.toString())) {
                    masterContext.getMasterSchedule().schedule(() -> {
                        HeraAction newAction = masterContext.getHeraJobActionService().findById(String.valueOf(actionId));
                        if (StatusEnum.RUNNING.toString().equals(newAction.getStatus())) {
                            ErrorLog.error("任务信号丢失actionId:{},historyId:{}", actionId, newAction.getHistoryId());
                            Integer jobId = ActionUtil.getJobId(String.valueOf(actionId));
                            boolean scheduleType = actionHistory.getTriggerType().equals(TriggerTypeEnum.SCHEDULE.getId())
                                    || actionHistory.getTriggerType().equals(TriggerTypeEnum.MANUAL_RECOVER.getId());
                            //TODO 可以选择重跑 or 广播 + 设置状态 这里偷懒 直接重跑
                            masterContext.getWorkMap().values().forEach(workHolder -> {
                                if (scheduleType) {
                                    workHolder.getRunning().remove(jobId);
                                } else {
                                    workHolder.getManningRunning().remove(jobId);
                                }
                            });
                            startNewJob(actionHistory, "任务信号丢失重试");
                        }
                    }, 1, TimeUnit.MINUTES);

                }
            }
        } catch (Exception e) {
            ErrorLog.error(LogHelpUtil.getSpecificTrace(e));
            e.printStackTrace();
        }

    }

    private void addRollBackJob(List<Long> actionIdList, Long actionId) {
        String actionStr = String.valueOf(actionId);
        if (!actionIdList.contains(actionId) &&
                !checkJobExists(HeraJobHistoryVo
                        .builder()
                        .actionId(actionStr)
                        .triggerType(TriggerTypeEnum.SCHEDULE)
                        .jobId((ActionUtil.getJobId(actionStr)))
                        .build(), true)) {
            masterContext.getDispatcher().forwardEvent(new HeraJobLostEvent(Events.UpdateJob, actionStr));
            actionIdList.add(actionId);
            ScheduleLog.info("roll back lost actionId :" + actionId);
        }
    }

    /**
     * 扫描任务等待队列，可获得worker的任务将执行
     * 对于没有可运行机器的时，manual,debug任务重新offer到原队列
     */
    private void waitingQueueCheck() {

        masterContext.masterSchedule.schedule(new Runnable() {
            // scan频率递增的步长
            private final Integer DELAY_TIME = 100;
            // 最大scan频率
            private final Integer MAX_DELAY_TIME = 10 * 1000;

            private Integer nextTime = HeraGlobalEnvironment.getScanRate();

            @Override
            public void run() {
                try {
                    if (scan()) {
                        nextTime = HeraGlobalEnvironment.getScanRate();
                    } else {
                        nextTime = (nextTime + DELAY_TIME) > MAX_DELAY_TIME ? MAX_DELAY_TIME : nextTime + DELAY_TIME;
                    }
                } catch (Exception e) {
                    ErrorLog.error(LogHelpUtil.getSpecificTrace(e));
                    ScanLog.error("scan waiting queueTask exception", e);
                } finally {
                    masterContext.masterSchedule.schedule(this, nextTime, TimeUnit.MILLISECONDS);
                }
            }
        }, HeraGlobalEnvironment.getScanRate(), TimeUnit.MILLISECONDS);
    }

    /**
     * 定时检测work心跳是否超时
     */
    private void heartCheck() {
        masterContext.masterSchedule.scheduleAtFixedRate(() -> {
            Date now = new Date();
            Map<Channel, MasterWorkHolder> workMap = masterContext.getWorkMap();
            List<Channel> removeChannel = new ArrayList<>(workMap.size());
            for (Channel channel : workMap.keySet()) {
                MasterWorkHolder workHolder = workMap.get(channel);
                if (workHolder.getHeartBeatInfo() == null) {
                    continue;
                }
                Long workTime = workHolder.getHeartBeatInfo().getTimestamp();
                if (workTime == null || now.getTime() - workTime > 1000 * 60L) {
                    workHolder.getChannel().close();
                    removeChannel.add(channel);
                }
            }
            removeChannel.forEach(workMap::remove);
        }, 0, 1, TimeUnit.MINUTES);
    }

    public boolean generateSingleAction(Integer jobId) {
        ScheduleLog.info("单个任务版本生成：{}", jobId);
        return generateAction(true, jobId);
    }

    public boolean generateBatchAction() {
        ScheduleLog.info("全量任务版本生成");
        long begin = System.currentTimeMillis();
        boolean flag = generateAction(false, null);
        ScheduleLog.info("生成版本时间:" + (System.currentTimeMillis() - begin) + " ms");
        return flag;
    }

    /**
     * 版本生成的方法
     *
     * @param isSingle 是否是单任务版本生成
     * @param jobId    进行单任务版本生成时的任务id
     * @return
     */
    private boolean generateAction(boolean isSingle, Integer jobId) {
        try {
            //判断是否在进行版本生成
            if (isGenerateActioning) {
                return true;
            }
            DateTime dateTime = new DateTime();
            Date now = dateTime.toDate();
            int executeHour = dateTime.getHourOfDay();
            // 0 || (7,23] 版本生成
            boolean execute = executeHour == 0 || (executeHour > ActionUtil.ACTION_CREATE_MIN_HOUR && executeHour <= ActionUtil.ACTION_CREATE_MAX_HOUR);
            // 判断是否可以进行版本生成 满足时间 或者 满足单任务版本生成
            if (execute || isSingle) {
                String currString = ActionUtil.getCurrHourVersion();
                if (executeHour == ActionUtil.ACTION_CREATE_MAX_HOUR) {
                    Tuple<String, Date> nextDayString = ActionUtil.getNextDayString();
                    //例如：今天 2018.07.17 23:50  currString = 201807180000000000 now = 2018.07.18 23:50
                    currString = nextDayString.getSource();
                    now = nextDayString.getTarget();
                }
                Long nowAction = Long.parseLong(currString);
                ConcurrentHashMap<Long, HeraAction> actionMap = new ConcurrentHashMap<>(heraActionMap.size());
                //记录任务
                List<HeraJob> jobList = new ArrayList<>();
                if (!isSingle) { //批量生成
                    isGenerateActioning = true;
                    //select * from hera_job
                    jobList = masterContext.getHeraJobService().getAll();
                } else { //单个任务生成版本
                    //提取出job的本身和其下游的的任务
                    HashMap<Integer, HeraJob> selfOrImpactJob = getSelfOrImpactJob(jobId);
                    jobList.addAll(selfOrImpactJob.values());
                    //现在内存中的所有版本数据 这里做一下暂存
                    actionMap = heraActionMap;
                    List<Long> shouldRemove = new ArrayList<>();
                    for (Long actionId : actionMap.keySet()) {
                        for (Integer id : selfOrImpactJob.keySet()) {
                            if (StringUtil.actionIdToJobId(String.valueOf(actionId), String.valueOf(id))) {
                                //获取要删除的版本号
                                shouldRemove.add(actionId);
                            }
                        }
                    }
                    //这一步是为了 actionMap 和 handlers 数据是一致的
                    shouldRemove.forEach(actionMap::remove);
                    //赋值请看93行 存储的也是版本数据
                    List<AbstractHandler> handlers = new ArrayList<>(masterContext.getDispatcher().getJobHandlers());
                    //移除所有依赖于这个任务的版本号
                    if (handlers != null && handlers.size() > 0) {
                        for (AbstractHandler handler : handlers) {
                            JobHandler jobHandler = (JobHandler) handler;
                            for (Integer id : selfOrImpactJob.keySet()) {
                                try {
                                    if (StringUtil.actionIdToJobId(jobHandler.getActionId(), String.valueOf(id))) {
                                        //广播要删除的版本号数据
                                        masterContext.getQuartzSchedulerService().deleteJob(jobHandler.getActionId());
                                        //删除内存中的版本号
                                        masterContext.getDispatcher().removeJobHandler(jobHandler);
                                    }
                                } catch (Exception e) {
                                    ErrorLog.error(LogHelpUtil.getSpecificTrace(e));
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                }
                //当前日期 yyyy-MM-dd
                String cronDate = ActionUtil.getActionVersionPrefix(now);
                //处理过的任务的版本信息
                Map<Integer, List<HeraAction>> idMap = new HashMap<>(jobList.size());
                Map<Integer, HeraJob> jobMap = new HashMap<>(jobList.size());
                //处理生成 定时任务的版本号(idMap)，不删除原有的版本号，并将版本号存储到内存中(actionMap)，依赖任务存储到jobMap
                generateScheduleJobAction(jobList, cronDate, actionMap, nowAction, idMap, jobMap);
                //依赖任务依次向上递归寻找上游的任务的版本号，并将新生成的版本号存储到内存中(actionMap(有版本号的任务)和idMap(处理过的任务))
                for (Map.Entry<Integer, HeraJob> entry : jobMap.entrySet()) {
                    generateDependJobAction(jobMap, entry.getValue(), actionMap, nowAction, idMap, isSingle);
                }
                //将版本号数据存入内存(全局的)
                if (executeHour < ActionUtil.ACTION_CREATE_MAX_HOUR) {
                    heraActionMap = actionMap;
                }

                Dispatcher dispatcher = masterContext.getDispatcher();

                if (dispatcher != null) {
                    if (actionMap.size() > 0) {
                        for (Long id : actionMap.keySet()) {
                            //和heraActionMap类似
                            dispatcher.addJobHandler(new JobHandler(id.toString(), masterContext.getMaster(), masterContext));
                            //新加入的版本发送事件通知
                            if (id >= Long.parseLong(currString)) {
                                dispatcher.forwardEvent(new HeraJobMaintenanceEvent(Events.UpdateActions, id.toString()));
                            }
                        }
                    }
                }

                ScheduleLog.info("[单个任务:{}，任务id:{}]generate action success", isSingle, jobId);
                return true;
            }
        } catch (Exception e) {
            ErrorLog.error(LogHelpUtil.getSpecificTrace(e));
            e.printStackTrace();
        } finally {
            isGenerateActioning = false;
        }
        return false;
    }

    /**
     * 提取出job的本身和其下游的的任务
     *
     * @param jobId 提取的任务
     */
    private HashMap<Integer, HeraJob> getSelfOrImpactJob(Integer jobId) {
        HashMap<Integer, HeraJob> selfOrImpactJobMap = new HashMap<>();
        List<HeraJob> jobAllList = masterContext.getHeraJobService().getAll();
        HashMap<String, HashSet<HeraJob>> dependMap = new HashMap<>();
        for (HeraJob heraJob : jobAllList) {
            String dependencies = heraJob.getDependencies();
            if (StringUtils.isNotBlank(dependencies)) {
                String[] depends = dependencies.split(Constants.COMMA);
                for (String depend : depends) {
                    HashSet<HeraJob> heraJobs = dependMap.getOrDefault(depend, new HashSet<>());
                    heraJobs.add(heraJob);
                    dependMap.put(depend, heraJobs);
                }
            }
            if (heraJob.getId() == jobId) {
                //本身
                selfOrImpactJobMap.put(jobId, heraJob);
            }
        }
        //下游的任务
        getJobImpact(dependMap, selfOrImpactJobMap, jobId);
        return selfOrImpactJobMap;
    }

    /**
     * 版本生成方法
     *
     * @param jobList   要进行版本生成的任务 hera_job数据
     * @param cronDate  CURRENT_DATE
     * @param actionMap 任务版本号数据
     * @param nowAction yyyyMMddHH00000000
     * @param idMap     存储定时任务生成的版本号
     * @param jobMap    存储依赖任务
     */
    public void generateScheduleJobAction(List<HeraJob> jobList, String cronDate, Map<Long, HeraAction> actionMap, Long nowAction, Map<Integer, List<HeraAction>> idMap, Map<Integer, HeraJob> jobMap) {
        //存储定时任务生成的版本号
        List<HeraAction> insertActionList = new ArrayList<>();
        for (HeraJob heraJob : jobList) {
            ///判断任务类型，定时或依赖 没有状态不处理
            if (heraJob.getScheduleType() != null) {
                if (heraJob.getScheduleType() == 1) { //依赖任务
                    jobMap.put(heraJob.getId(), heraJob);
                } else if (heraJob.getScheduleType() == 0) { //定时任务
                    String cron = heraJob.getCronExpression();
                    //存储解析出的定时表达式
                    List<String> list = new ArrayList<>();
                    //判断定时表达式是否存可用
                    if (StringUtils.isNotBlank(cron)) {
                        //cron表达式解析
                        boolean isCronExp = CronParse.Parser(cron, cronDate, list);
                        if (!isCronExp) {
                            ErrorLog.error("cron parse error,jobId={},cron = {}", heraJob.getId(), cron);
                            continue;
                        }
                        //对应任务在 hera_action 中的 action 集合
                        List<HeraAction> heraAction = createHeraAction(list, heraJob);
                        //存储定时任务生成的版本号
                        idMap.put(heraJob.getId(), heraAction);
                        insertActionList.addAll(heraAction);
                    }
                } else {
                    ErrorLog.error("任务{}未知的调度类型{}", heraJob.getId(), heraJob.getScheduleType());
                }
            }
        }
        //存储新生成的版本号并将新的版本号放入内存中
        batchInsertList(insertActionList, actionMap, nowAction);
    }


    /**
     * 批量插入
     *
     * @param insertActionList 要插入/更新的hera_action 集合
     */
    private void batchInsertList(List<HeraAction> insertActionList, Map<Long, HeraAction> actionMap, Long nowAction) {
        //总的条数
        int maxSize = insertActionList.size();
        //每批次的条数
        int batchNum = 200;
        //停止标识
        int step = batchNum > maxSize ? maxSize : batchNum;
        if (maxSize != 0) {
            for (int i = 0; i < maxSize; i = i + batchNum) {
                List<HeraAction> insertList;
                if ((step + batchNum) > maxSize) {
                    insertList = insertActionList.subList(i, step);
                    step = maxSize;
                } else {
                    insertList = insertActionList.subList(i, step);
                    step = step + batchNum;
                }
                //批量插入数据库
                masterContext.getHeraJobActionService().batchInsert(insertList, nowAction);
                //将生成的版本号放入内存中
                for (HeraAction action : insertList) {
                    actionMap.put(action.getId(), action);
                }
            }
        }
    }


    /**
     * 生成action
     *
     * @param list    表格cronTab 表达式，对应多了时间点的版本集合
     * @param heraJob hera_job 表对象
     * @return 更新后的action 信息，保存到内存
     */
    private List<HeraAction> createHeraAction(List<String> list, HeraJob heraJob) {
        List<HeraAction> heraActionList = new ArrayList<>();
        for (String str : list) {
            String actionDate = HeraDateTool.StringToDateStr(str, ActionUtil.DEFAULT_FORMAT, ActionUtil.ACTION_MIN);
            String actionCron = HeraDateTool.StringToDateStr(str, ActionUtil.DEFAULT_FORMAT, ActionUtil.ACTION_CRON) + " ?";
            HeraAction heraAction = new HeraAction();
            BeanUtils.copyProperties(heraJob, heraAction);
            Long actionId = Long.parseLong(actionDate) * 1000000 + Long.parseLong(String.valueOf(heraJob.getId()));
            heraAction.setId(actionId);
            heraAction.setCronExpression(actionCron);
            heraAction.setGmtCreate(new Date());
            heraAction.setJobId(heraJob.getId());
            heraAction.setHistoryId(heraJob.getHistoryId());
            heraAction.setAuto(heraJob.getAuto());
            heraAction.setGmtModified(new Date());
            heraAction.setJobDependencies(null);
            heraAction.setDependencies(null);
            heraAction.setReadyDependency(null);
            heraAction.setHostGroupId(heraJob.getHostGroupId());
            heraActionList.add(heraAction);
        }
        return heraActionList;
    }


    private void clearInvalidAction() {
        ScheduleLog.warn("开始进行版本清理");
        Dispatcher dispatcher = masterContext.getDispatcher();
        Long currDate = ActionUtil.getLongCurrActionVersion();
        Long nextDay = ActionUtil.getLongNextDayActionVersion();
        Long preCheckTime = currDate - MasterConstant.PRE_CHECK_MIN;

        Map<Long, HeraAction> actionMapNew = heraActionMap;
        //移除未生成的调度
        List<AbstractHandler> handlers = dispatcher.getJobHandlers();
        List<JobHandler> shouldRemove = new ArrayList<>();
        String dayAction = String.valueOf(getBeforeDayAction());
        if (handlers != null && handlers.size() > 0) {
            handlers.forEach(handler -> {
                JobHandler jobHandler = (JobHandler) handler;
                String actionId = jobHandler.getActionId();
                Long aid = Long.parseLong(actionId);
                if (Long.parseLong(actionId) < preCheckTime) {
                    masterContext.getQuartzSchedulerService().deleteJob(actionId);
                } else if (aid >= currDate && aid < nextDay) {
                    if (!actionMapNew.containsKey(aid)) {
                        masterContext.getQuartzSchedulerService().deleteJob(actionId);
                        masterContext.getHeraJobActionService().delete(actionId);
                        shouldRemove.add(jobHandler);
                    }
                }
                //移除非缓存时间内的版本的订阅者
                if (actionId.compareTo(dayAction) < 0) {
                    shouldRemove.add(jobHandler);
                }
            });
        }
        //移除 过期 失效的handler
        shouldRemove.forEach(dispatcher::removeJobHandler);
        ScheduleLog.warn("版本清理完成");
    }


    /**
     * 递归生成任务依赖action
     *
     * @param jobMap    存储依赖任务
     * @param heraJob   当前任务的信息
     * @param actionMap 内存中的版本号
     * @param nowAction yyyyMMddHH00000000
     * @param idMap     处理过的版本号
     */
    private void generateDependJobAction(Map<Integer, HeraJob> jobMap, HeraJob heraJob, Map<Long, HeraAction> actionMap, Long nowAction, Map<Integer, List<HeraAction>> idMap, Boolean isSingle) {
        //判断任务是否已经新生成过版本号,是就跳过这个任务
        if (heraJob == null || idMap.containsKey(heraJob.getId())) {
            return;
        }
        //任务的上游
        String jobDependencies = heraJob.getDependencies();
        //依赖不为空
        if (StringUtils.isNotBlank(jobDependencies)) {
            Map<String, List<HeraAction>> dependenciesMap = new HashMap<>(1024);
            //将依赖的上游拆分 ","
            String[] dependencies = jobDependencies.split(Constants.COMMA);
            //存储版本号较少 || 执行时间较晚的任务
            String actionMinDeps = "";
            //判断上游的任务是否有版本号
            boolean noAction = false;
            //得出 actionMinDeps
            for (String dependentId : dependencies) {
                Integer dpId = Integer.parseInt(dependentId);
                //如果是未生成版本号的就任务进行递归
                if (!idMap.containsKey(dpId)) {
                    generateDependJobAction(jobMap, jobMap.get(dpId), actionMap, nowAction, idMap, isSingle);
                }
                //上游任务的版本号数据
                List<HeraAction> dpActions = idMap.get(dpId);
                //类似idMap 存储上游任务的版本号
                dependenciesMap.put(dependentId, dpActions);
                List<HeraAction> dpHeraActions = new ArrayList<>();
                if (dpActions == null || dpActions.size() == 0) {
                    if (!isSingle) {
                        ErrorLog.warn("{}今天找不到版本，无法为任务{}生成版本", dependentId, heraJob.getId());
                        noAction = true;
                        break;
                    } else {
                        for (Map.Entry<Long, HeraAction> heraActionEntry : actionMap.entrySet()) {
                            if (StringUtil.actionIdToJobId(String.valueOf(heraActionEntry.getKey()), dependentId)) {
                                dpHeraActions.add(heraActionEntry.getValue());
                            }
                        }
                        //类似idMap 存储上游任务的版本号
                        dependenciesMap.put(dependentId, dpHeraActions);
                    }
                }
                //缓存版本号,为寻找最晚的上游任务做准备
                if (StringUtils.isBlank(actionMinDeps)) {
                    actionMinDeps = dependentId;
                }
                if (dependenciesMap.get(actionMinDeps).size() > dependenciesMap.get(dependentId).size()) {
                    //取版本号数量较少的
                    actionMinDeps = dependentId;
                } else if (dependenciesMap.get(dependentId).size() > 0 && dependenciesMap.get(actionMinDeps).size() == dependenciesMap.get(dependentId).size() && dependenciesMap.get(actionMinDeps).get(0).getId() < dependenciesMap.get(dependentId).get(0).getId()) {
                    //版本号个数相同取版本号值较大的 时间较晚
                    actionMinDeps = dependentId;
                }
            }
            if (noAction) {
                //存储处理过的任务 不生成版本号
                idMap.put(heraJob.getId(), null);
            } else {
                //依赖的上游任务的版本号
                List<HeraAction> actionMinList = dependenciesMap.get(actionMinDeps);
                if (actionMinList != null && actionMinList.size() > 0) {
                    //将要存储的任务的版本号
                    List<HeraAction> insertList = new ArrayList<>();
                    for (HeraAction action : actionMinList) {
                        //基准版本
                        StringBuilder actionDependencies = new StringBuilder(action.getId().toString());
                        Long longActionId = Long.parseLong(actionDependencies.toString());

                        for (String dependency : dependencies) {
                            if (!dependency.equals(actionMinDeps)) {
                                List<HeraAction> otherAction = dependenciesMap.get(dependency);
                                if (otherAction == null || otherAction.size() == 0) {
                                    continue;
                                }
                                //找到一个离基准版本时间最近的action，添加为该任务的版本依赖
                                String otherActionId = otherAction.get(0).getId().toString();
                                for (HeraAction o : otherAction) {
                                    if (Math.abs(o.getId() - longActionId) < Math.abs(Long.parseLong(otherActionId) - longActionId)) {
                                        otherActionId = o.getId().toString();
                                    }
                                }
                                actionDependencies.append(",");
                                actionDependencies.append(Long.parseLong(otherActionId) / 1000000 * 1000000 + Long.parseLong(dependency));
                            }
                        }
                        HeraAction actionNew = new HeraAction();
                        BeanUtils.copyProperties(heraJob, actionNew);
                        Long actionId = longActionId / 1000000 * 1000000 + Long.parseLong(String.valueOf(heraJob.getId()));
                        actionNew.setId(actionId);
                        actionNew.setGmtCreate(new Date());
                        actionNew.setDependencies(actionDependencies.toString());
                        actionNew.setJobDependencies(heraJob.getDependencies());
                        actionNew.setJobId(heraJob.getId());
                        actionNew.setAuto(heraJob.getAuto());
                        actionNew.setHostGroupId(heraJob.getHostGroupId());
                        masterContext.getHeraJobActionService().insert(actionNew, nowAction);
                        //新生成的版本号存储到内存
                        actionMap.put(actionNew.getId(), actionNew);
                        insertList.add(actionNew);
                    }
                    //存储任务新生成的版本号
                    idMap.put(heraJob.getId(), insertList);
                }
            }
        }
    }


    /**
     * 扫描任务等待队列，取出任务去执行
     */
    public boolean scan() throws InterruptedException {
        boolean hasTask = false;
        if (!masterContext.getScheduleQueue().isEmpty()) {
            JobElement jobElement = masterContext.getScheduleQueue().take();
            if (jobElement != null) {
                MasterWorkHolder selectWork = getRunnableWork(jobElement);
                if (selectWork == null) {
                    masterContext.getScheduleQueue().put(jobElement);
                    ScheduleLog.warn("can not get work to execute Schedule job in master,job is:{}", jobElement.toString());
                } else {
                    runScheduleJob(selectWork, jobElement.getJobId());
                    hasTask = true;
                }
            }
        }

        if (!masterContext.getManualQueue().isEmpty()) {
            ///拿出任务
            JobElement jobElement = masterContext.getManualQueue().take();
            if (jobElement != null) {
                MasterWorkHolder selectWork = getRunnableWork(jobElement);
                if (selectWork == null) {
                    masterContext.getManualQueue().put(jobElement);
                    ScheduleLog.warn("can not get work to execute ManualQueue job in master,job is:{}", jobElement.toString());
                } else {
                    ///当有机器的时候，直接给子机分配任务（向work发送任务）
                    runManualJob(selectWork, jobElement.getJobId());
                    hasTask = true;

                }
            }
        }

        if (!masterContext.getDebugQueue().isEmpty()) {
            JobElement jobElement = masterContext.getDebugQueue().take();
            if (jobElement != null) {
                MasterWorkHolder selectWork = getRunnableWork(jobElement);
                if (selectWork == null) {
                    masterContext.getDebugQueue().put(jobElement);
                    ScheduleLog.warn("can not get work to execute DebugQueue job in master,job is:{}", jobElement.toString());
                } else {
                    runDebugJob(selectWork, jobElement.getJobId());
                    hasTask = true;
                }
            }

        }
        return hasTask;

    }

    public void printThreadPoolLog() {
        String sb = "当前线程池信息" + "[ActiveCount: " + executeJobPool.getActiveCount() + "," +
                "CompletedTaskCount：" + executeJobPool.getCompletedTaskCount() + "," +
                "PoolSize:" + executeJobPool.getPoolSize() + "," +
                "LargestPoolSize:" + executeJobPool.getLargestPoolSize() + "," +
                "TaskCount:" + executeJobPool.getTaskCount() + "]";
        ScheduleLog.warn(sb);
    }

    /**
     * 手动执行任务调度器执行逻辑，向master的channel写manual任务执行请求
     *
     * @param selectWork selectWork 所选机器
     * @param actionId   actionId
     */
    private void runManualJob(MasterWorkHolder selectWork, String actionId) {
        final MasterWorkHolder workHolder = selectWork;
        SocketLog.info("start run manual job, actionId = {}", actionId);

        this.executeJobPool.execute(() -> {
            HeraAction heraAction = masterContext.getHeraJobActionService().findById(actionId);
            HeraJobHistory history = masterContext.getHeraJobHistoryService().findById(heraAction.getHistoryId());
            HeraJobHistoryVo historyVo = BeanConvertUtils.convert(history);
            historyVo.getLog().append(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + " 开始运行");
            historyVo.getLog().append("\n---------------------------------------------------------------------------------------具体的脚本---------------------------------------------------------------------------------------\n" +
                    heraAction.getScript()
                    + "\n----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------");
            heraAction.setStatus(StatusEnum.RUNNING.toString());
            historyVo.setStatusEnum(StatusEnum.RUNNING);
            HeraAction cacheAction = heraActionMap.get(Long.parseLong(actionId));
            if (cacheAction != null) {
                cacheAction.setStatus(StatusEnum.RUNNING.toString());
                cacheAction.setHistoryId(heraAction.getHistoryId());
            }
            masterContext.getHeraJobHistoryService().updateHeraJobHistoryLogAndStatus(BeanConvertUtils.convert(historyVo));

            Exception exception = null;
            RpcResponse.Response response = null;
            Future<RpcResponse.Response> future = null;
            try {
                future = new MasterExecuteJob().executeJob(masterContext, workHolder,
                        JobExecuteKind.ExecuteKind.ManualKind, actionId);
                response = future.get();
            } catch (Exception e) {
                ErrorLog.error(LogHelpUtil.getSpecificTrace(e));
                exception = e;
                if (future != null) {
                    future.cancel(true);
                }
                ErrorLog.error("manual job run error {}", e);
            }
            boolean success = response != null && response.getStatusEnum() != null && response.getStatusEnum() == ResponseStatus.Status.OK;
            if (response != null) {
                ScheduleLog.info("actionId 执行结果" + actionId + "---->" + response.getStatusEnum());
            }
            ApplicationEvent event;
            if (!success) {
                if (exception != null) {
                    HeraException heraException = new HeraException(exception);
                    ErrorLog.error("manual actionId = {} error, {}", history.getActionId(), heraException.getMessage());
                }
                ScheduleLog.info("actionId = {} manual execute failed", history.getActionId());
                heraAction.setStatus(StatusEnum.FAILED.toString());
                HeraJobHistory jobHistory = masterContext.getHeraJobHistoryService().findById(history.getId());
                if (LogConstant.CANCEL_JOB_LOG.equals(jobHistory.getIllustrate())) {
                    event = null;
                } else {
                    HeraJobHistoryVo jobHistoryVo = BeanConvertUtils.convert(jobHistory);
                    event = new HeraJobFailedEvent(history.getActionId(), jobHistoryVo.getTriggerType(), jobHistoryVo);
                }
            } else {
                heraAction.setStatus(StatusEnum.SUCCESS.toString());
                event = new HeraJobSuccessEvent(history.getActionId(), historyVo.getTriggerType(), history.getId());
            }
            cacheAction = heraActionMap.get(Long.parseLong(actionId));
            if (cacheAction != null) {
                cacheAction.setStatus(heraAction.getStatus());
            }
            heraAction.setStatisticEndTime(new Date());
            masterContext.getHeraJobActionService().update(heraAction);
            if (event != null) {
                masterContext.getDispatcher().forwardEvent(event);
            }
        });
    }

    /**
     * 调度任务执行前，先获取任务的执行重试时间间隔和重试次数
     *
     * @param workHolder 所选机器
     * @param actionId   actionId
     */
    private void runScheduleJob(MasterWorkHolder workHolder, String actionId) {
        this.executeJobPool.execute(() -> {
            int runCount = 0;
            int retryCount = 0;
            int retryWaitTime = 1;
            HeraActionVo heraActionVo = masterContext.getHeraJobActionService().findHeraActionVo(actionId).getSource();
            Map<String, String> properties = heraActionVo.getConfigs();
            if (properties != null && properties.size() > 0) {
                retryCount = Integer.parseInt(properties.get("roll.back.times") == null ? "0" : properties.get("roll.back.times"));
                retryWaitTime = Integer.parseInt(properties.get("roll.back.wait.time") == null ? "0" : properties.get("roll.back.wait.time"));
            }
            runScheduleJobContext(workHolder, actionId, runCount, retryCount, retryWaitTime);
        });
    }

    /**
     * 自动调度任务开始执行入口，向master端的channel写请求任务执行请求
     *
     * @param workHolder    workHolder
     * @param actionId      actionId
     * @param runCount      runCount
     * @param retryCount    retryCount
     * @param retryWaitTime retryWaitTime
     */
    private void runScheduleJobContext(MasterWorkHolder workHolder, String actionId, int runCount, int retryCount, int retryWaitTime) {

        DebugLog.info("重试次数：{},重试时间：{},actionId:{}", retryCount, retryWaitTime, actionId);
        runCount++;
        boolean isCancelJob = false;
        if (runCount > 1) {
            DebugLog.info("任务重试，睡眠：{}分钟,第{}次重试", retryWaitTime, runCount - 1);
            try {
                TimeUnit.MINUTES.sleep(retryWaitTime);
            } catch (InterruptedException e) {
                ErrorLog.error(LogHelpUtil.getSpecificTrace(e));
                e.printStackTrace();
            }
        }
        HeraJobHistoryVo heraJobHistoryVo;
        HeraJobHistory heraJobHistory;
        TriggerTypeEnum triggerType;
        HeraAction heraAction;
        if (runCount == 1) {
            heraAction = masterContext.getHeraJobActionService().findById(actionId);
            heraJobHistory = masterContext.getHeraJobHistoryService().
                    findById(heraAction.getHistoryId());
            heraJobHistoryVo = BeanConvertUtils.convert(heraJobHistory);
            triggerType = heraJobHistoryVo.getTriggerType();
            heraJobHistoryVo.getLog().append(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + " 开始运行");
            heraJobHistoryVo.getLog().append("\n---------------------------------------------------------------------------------------具体的脚本---------------------------------------------------------------------------------------\n" +
                    heraAction.getScript()
                    + "\n----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------");
        } else {
            heraAction = masterContext.getHeraJobActionService().findById(actionId);
            heraJobHistory = HeraJobHistory.builder()
                    .illustrate(LogConstant.FAIL_JOB_RETRY)
                    .triggerType(TriggerTypeEnum.SCHEDULE.getId())
                    .jobId(heraAction.getJobId())
                    .actionId(String.valueOf(heraAction.getId()))
                    .operator(heraAction.getOwner())
                    .hostGroupId(heraAction.getHostGroupId())
                    .build();
            masterContext.getHeraJobHistoryService().insert(heraJobHistory);
            heraAction.setHistoryId(heraJobHistory.getId());
            heraAction.setStatus(StatusEnum.RUNNING.toString());
            masterContext.getHeraJobActionService().update(heraAction);
            heraJobHistoryVo = BeanConvertUtils.convert(heraJobHistory);
            heraJobHistoryVo.getLog().append(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + " 第" + (runCount - 1) + "次重试运行\n");
            DebugLog.info(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + " 第" + (runCount - 1) + "次重试运行\n");
            triggerType = heraJobHistoryVo.getTriggerType();
        }
        HeraAction cacheAction = heraActionMap.get(Long.parseLong(actionId));
        if (cacheAction != null) {
            cacheAction.setStatus(StatusEnum.RUNNING.toString());
            cacheAction.setHistoryId(heraJobHistory.getId());
        }
        heraJobHistoryVo.setStatusEnum(StatusEnum.RUNNING);
        masterContext.getHeraJobHistoryService().updateHeraJobHistoryLogAndStatus(BeanConvertUtils.convert(heraJobHistoryVo));
        RpcResponse.Response response = null;
        Future<RpcResponse.Response> future = null;
        try {
            future = new MasterExecuteJob().executeJob(masterContext, workHolder,
                    ScheduleKind, actionId);
            response = future.get(HeraGlobalEnvironment.getTaskTimeout(), TimeUnit.HOURS);
        } catch (Exception e) {
            ErrorLog.error(LogHelpUtil.getSpecificTrace(e));
            ErrorLog.error("schedule job run error :" + actionId, e);
            if (future != null) {
                future.cancel(true);
            }
            heraAction.setStatus(StatusEnum.FAILED.toString());
            heraJobHistoryVo.setStatusEnum(StatusEnum.FAILED);
            masterContext.getHeraJobHistoryService().updateHeraJobHistoryStatus(BeanConvertUtils.convert(heraJobHistoryVo));
        }
        boolean success = response != null && response.getStatusEnum() == ResponseStatus.Status.OK;
        ScheduleLog.info("job_id 执行结果" + actionId + "---->" + (response == null ? "空指针" : response.getStatusEnum().toString()));
        if (!success) {
            heraAction.setStatus(StatusEnum.FAILED.toString());
            HeraJobHistory history = masterContext.getHeraJobHistoryService().findById(heraJobHistoryVo.getId());
            HeraJobHistoryVo jobHistory = BeanConvertUtils.convert(history);
            HeraJobFailedEvent event = new HeraJobFailedEvent(actionId, triggerType, jobHistory);
            event.setRollBackTime(retryWaitTime);
            event.setRunCount(runCount);
            event.setRollBackTime(retryCount);
            if (Constants.CANCEL_JOB_MESSAGE.equals(jobHistory.getIllustrate()) || StatusEnum.WAIT.toString().equals(history.getStatus())) {
                isCancelJob = true;
                ScheduleLog.info("任务取消或者暂停，取消重试:{}", jobHistory.getActionId());
            } else {
                try {
                    ErrorLog.error("任务失败了。。。{}", jobHistory.getActionId());
                    //      ErrorLog.error("任务失败了。。。{}"+event);
                    //       ErrorLog.error("任务失败了_1。。。{}", event.toString());
                } catch (Exception e) {
                    // System.out.println("918");
                }
                masterContext.getDispatcher().forwardEvent(event);
            }
        } else {
            //如果是依赖任务 置空依赖
            if (JobScheduleTypeEnum.Dependent.getType().equals(heraAction.getScheduleType())) {
                heraAction.setReadyDependency("{}");
            }
            heraAction.setStatus(StatusEnum.SUCCESS.toString());
            HeraJobSuccessEvent successEvent = new HeraJobSuccessEvent(actionId, triggerType, heraJobHistory.getId());
            masterContext.getDispatcher().forwardEvent(successEvent);
            //sendMail("runCount?0  hera任务（id=" + Integer.parseInt(actionId.substring(12)) + "）重试成功", "job版本（" + actionId + "） 第" + (runCount - 1) + "次重试运行执行成功");

            if (runCount > 1) {
                DebugLog.info("Master\t" + (runCount - 1) + "次重试运行执行成功");
                //TODO 重点代码
                //sendMail("runCount>0  hera任务（id=" + Integer.parseInt(actionId.substring(12)) + "）重试成功", "job版本（" + actionId + "） 第" + (runCount - 1) + "次重试运行执行成功");
                List<JobFailAlarm> alarms = com.dfire.monitor.config.ServiceLoader.getAlarms();
                for (JobFailAlarm failAlarm : alarms) {
                    failAlarm.successAlarm(actionId, runCount - 1);
                }
            }


            // JobFailAlarm ;
            // EmailServiceImpl emailService =new EmailServiceImpl();
            // emailService.sendEmail("","","");
            // EmailJobFailAlarm emailJobFailAlarm =new EmailJobFailAlarm();
            // System.out.println("Master 里面925行 ");
            // emailJobFailAlarm.retrySuccessAlarm3("Master 里面926行");
            // EmailJobFailAlarm


        }
        cacheAction = heraActionMap.get(Long.parseLong(actionId));
        if (cacheAction != null) {
            cacheAction.setStatus(heraAction.getStatus());
        }
        heraAction.setStatisticEndTime(new Date());
        masterContext.getHeraJobActionService().update(heraAction);
        if (runCount < (retryCount + 1) && !success && !isCancelJob) {
            if (checkJobExists(heraJobHistoryVo, true)) {
                DebugLog.info("--------------------------任务在队列中，取消重试--------------------------");
            } else {
                DebugLog.info("--------------------------失败任务，准备重试--------------------------");
                //TODO 重要代码
                runScheduleJobContext(workHolder, actionId, runCount, retryCount, retryWaitTime);
            }
        }
    }

    /**
     * 开发中心脚本执行逻辑
     *
     * @param selectWork 所选机器
     * @param debugId    debugId
     */
    private void runDebugJob(MasterWorkHolder selectWork, String debugId) {
        final MasterWorkHolder workHolder = selectWork;
        this.executeJobPool.execute(() -> {
            HeraDebugHistoryVo history = masterContext.getHeraDebugHistoryService().findById(Integer.parseInt(debugId));
            history.getLog().append(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + " 开始运行");
            DebugLog.info(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + " 开始运行");
            masterContext.getHeraDebugHistoryService().update(BeanConvertUtils.convert(history));
            Exception exception = null;
            RpcResponse.Response response = null;
            Future<RpcResponse.Response> future = null;
            try {
                future = new MasterExecuteJob().executeJob(masterContext, workHolder, JobExecuteKind.ExecuteKind.DebugKind, debugId);
                response = future.get(HeraGlobalEnvironment.getTaskTimeout(), TimeUnit.HOURS);
            } catch (Exception e) {
                ErrorLog.error(LogHelpUtil.getSpecificTrace(e));
                exception = e;
                if (future != null) {
                    future.cancel(true);
                }
                DebugLog.error(String.format("debugId:%s run failed", debugId), e);
            }
            boolean success = response != null && response.getStatusEnum() == ResponseStatus.Status.OK;
            if (!success) {
                exception = new HeraException(String.format("fileId:%s run failed ", history.getFileId()), exception);
                TaskLog.info("8.Master: debug job error");
                history = masterContext.getHeraDebugHistoryService().findById(Integer.parseInt(debugId));
                HeraDebugFailEvent failEvent = HeraDebugFailEvent.builder()
                        .debugHistory(BeanConvertUtils.convert(history))
                        .throwable(exception)
                        .fileId(history.getFileId())
                        .build();
                masterContext.getDispatcher().forwardEvent(failEvent);
            } else {
                TaskLog.info("7.Master: debug success");
                HeraDebugSuccessEvent successEvent = HeraDebugSuccessEvent.builder()
                        .fileId(history.getFileId())
                        .history(BeanConvertUtils.convert(history))
                        .build();
                masterContext.getDispatcher().forwardEvent(successEvent);
            }
        });
    }

    /**
     * 获取hostGroupId中可以分发任务的worker
     *
     * @param jobElement job 部分信息
     * @return
     */
    private MasterWorkHolder getRunnableWork(JobElement jobElement) {
        MasterWorkHolder selectWork = loadBalance.select(jobElement, masterContext);
        if (selectWork == null) {
            return null;
        }
        Channel channel = selectWork.getChannel().getChannel();
        HeartBeatInfo beatInfo = selectWork.getHeartBeatInfo();
        // 如果最近两次选择的work一致  需要等待机器最新状态发来之后(睡眠)再进行任务分发
        if (HeraGlobalEnvironment.getWarmUpCheck() > 0 && lastWork != null && channel == lastWork && (beatInfo.getCpuLoadPerCore() > 0.6F || beatInfo.getMemRate() > 0.7F)) {
            ScheduleLog.info("达到预热条件，睡眠" + HeraGlobalEnvironment.getWarmUpCheck() + "秒");
            try {
                TimeUnit.SECONDS.sleep(HeraGlobalEnvironment.getWarmUpCheck());
            } catch (InterruptedException e) {
                ErrorLog.error(LogHelpUtil.getSpecificTrace(e));
                e.printStackTrace();
            }
            lastWork = null;
            return null;
        }
        lastWork = channel;
        return selectWork;
    }

    public void debug(HeraDebugHistoryVo debugHistory) {
        JobElement element = JobElement.builder()
                .jobId(debugHistory.getId())
                .hostGroupId(debugHistory.getHostGroupId())
                .build();
        debugHistory.setStatus(StatusEnum.RUNNING);
        debugHistory.setStartTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
        debugHistory.getLog().append(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + " 进入任务队列");
        masterContext.getHeraDebugHistoryService().update(BeanConvertUtils.convert(debugHistory));
        try {
            masterContext.getDebugQueue().put(element);
        } catch (InterruptedException e) {
            ErrorLog.error(LogHelpUtil.getSpecificTrace(e));
            ErrorLog.error("添加开发中心执行任务失败:" + element.getJobId(), e);
        }
    }

    /**
     * 手动执行任务或者手动恢复任务的时候，先进行任务是否在执行的判断，
     * 没有在运行进入队列等待，已经在运行的任务不入队列，避免重复执行
     *
     * @param heraJobHistory heraJobHistory表信息
     */
    public void run(HeraJobHistoryVo heraJobHistory) {

        //System.err.println("run :"+heraJobHistory);

        String actionId = heraJobHistory.getActionId();

        //System.err.println("actionId :"+actionId);

        int priorityLevel = 3;
        HeraAction heraAction = masterContext.getHeraJobActionService().findById(actionId);
        Map<String, String> configs = StringUtil.convertStringToMap(heraAction.getConfigs());
        String priorityLevelValue = configs.get("run.priority.level");
        if (priorityLevelValue != null) {
            priorityLevel = Integer.parseInt(priorityLevelValue);
        }
        JobElement element = JobElement.builder()
                .jobId(heraJobHistory.getActionId())
                .hostGroupId(heraJobHistory.getHostGroupId())
                .priorityLevel(priorityLevel)
                .build();
        heraJobHistory.setStatusEnum(StatusEnum.RUNNING);
        //重复job检测
        if (checkJobExists(heraJobHistory, false)) {
            return;
        }

        //先在数据库中set一些执行任务所需的必须值 然后再加入任务队列
        heraAction.setLastResult(heraAction.getStatus());
        heraAction.setStatus(StatusEnum.RUNNING.toString());
        heraAction.setHistoryId(heraJobHistory.getId());
        heraAction.setHost(heraJobHistory.getExecuteHost());
        heraAction.setStatisticStartTime(new Date());
        heraAction.setStatisticEndTime(null);
        masterContext.getHeraJobActionService().update(heraAction);
        heraJobHistory.getLog().append(ActionUtil.getTodayString() + "进入任务队列");
        masterContext.getHeraJobHistoryService().update(BeanConvertUtils.convert(heraJobHistory));
        try {
            if (heraJobHistory.getTriggerType() == TriggerTypeEnum.MANUAL) {
                masterContext.getManualQueue().put(element);
            } else {
                masterContext.getScheduleQueue().put(element);
            }
        } catch (InterruptedException e) {
            ErrorLog.error(LogHelpUtil.getSpecificTrace(e));
            ErrorLog.error("添加任务" + element.getJobId() + "失败", e);
        }
    }


    private boolean checkJobExists(HeraJobHistoryVo heraJobHistory, boolean checkOnly) {
        // 允许重复的话 不检测
        if (masterContext.getHeraJobService().isRepeat(heraJobHistory.getJobId())) {
            return false;
        }
        String actionId = heraJobHistory.getActionId();
        Integer jobId = heraJobHistory.getJobId();
        ///自动 或 手动恢复(恢复下游和手动恢复)
        if (heraJobHistory.getTriggerType() == TriggerTypeEnum.MANUAL_RECOVER || heraJobHistory.getTriggerType() == TriggerTypeEnum.SCHEDULE) {
            // check调度器等待队列是否有此任务在排队
            for (JobElement jobElement : masterContext.getScheduleQueue()) {
                if (ActionUtil.jobEquals(jobElement.getJobId(), actionId)) {
                    if (!checkOnly) {
                        heraJobHistory.getLog().append(LogConstant.CHECK_QUEUE_LOG);
                        heraJobHistory.setStartTime(new Date());
                        heraJobHistory.setEndTime(new Date());
                        heraJobHistory.setStatusEnum(StatusEnum.FAILED);
                        masterContext.getHeraJobHistoryService().update(BeanConvertUtils.convert(heraJobHistory));
                        //发送重复执行预警
//                        DebugLog.info("---------------开始发送任务重复执行警报");
//                        HeraUser scKeyById = masterContext.getHeraJobService().findScKeyById(jobId);
//                        WeiChatUtil.sendWeiChatMessage(scKeyById.getScKey(), text, "");
                    }

                    TaskLog.warn("调度队列已存在该任务，添加失败 {}", actionId);
                    return true;
                }
            }
            // check所有的worker中是否有此任务的id在执行，如果有，不进入队列等待
            for (MasterWorkHolder workHolder : masterContext.getWorkMap().values()) {
                if (workHolder.getRunning().contains(jobId)) {
                    if (!checkOnly) {
                        heraJobHistory.getLog().append(LogConstant.CHECK_QUEUE_LOG + "执行worker ip " + workHolder.getChannel().getLocalAddress());
                        heraJobHistory.setStartTime(new Date());
                        heraJobHistory.setEndTime(new Date());
                        heraJobHistory.setStatusEnum(StatusEnum.FAILED);
                        masterContext.getHeraJobHistoryService().update(BeanConvertUtils.convert(heraJobHistory));
                        //发送重复执行预警
//                        DebugLog.info("---------------开始发送任务重复执行警报");
//                        HeraUser scKeyById = masterContext.getHeraJobService().findScKeyById(jobId);
//                        WeiChatUtil.sendWeiChatMessage(scKeyById.getScKey(), text, "");

                    }
                    TaskLog.warn("该任务正在执行，添加失败 {}", actionId);
                    return true;
                }
            }

        } else if (heraJobHistory.getTriggerType() == TriggerTypeEnum.MANUAL) {///手动队列

            for (JobElement jobElement : masterContext.getManualQueue()) {
                if (ActionUtil.jobEquals(jobElement.getJobId(), actionId)) {
                    if (!checkOnly) {
                        heraJobHistory.getLog().append(LogConstant.CHECK_MANUAL_QUEUE_LOG);
                        heraJobHistory.setStartTime(new Date());
                        heraJobHistory.setEndTime(new Date());
                        heraJobHistory.setStatusEnum(StatusEnum.FAILED);
                        masterContext.getHeraJobHistoryService().update(BeanConvertUtils.convert(heraJobHistory));
                    }
                    TaskLog.warn("手动任务队列已存在该任务，添加失败 {}", actionId);
                    return true;
                }
            }

            for (MasterWorkHolder workHolder : masterContext.getWorkMap().values()) {
                if (workHolder.getManningRunning().contains(jobId)) {
                    if (!checkOnly) {
                        heraJobHistory.getLog().append(LogConstant.CHECK_MANUAL_QUEUE_LOG + "执行worker ip " + workHolder.getChannel().getLocalAddress());
                        heraJobHistory.setStartTime(new Date());
                        heraJobHistory.setEndTime(new Date());
                        heraJobHistory.setStatusEnum(StatusEnum.FAILED);
                        masterContext.getHeraJobHistoryService().update(BeanConvertUtils.convert(heraJobHistory));
                    }
                    TaskLog.warn("该任务正在执行，添加失败 {}", actionId);
                    return true;
                }
            }
        }
        return false;

    }

    /**
     * work断开的处理
     *
     * @param channel channel
     */
    public void workerDisconnectProcess(Channel channel) {
        String ip = getIpFromChannel(channel);
        ErrorLog.error("work:{}断线", ip);
        HeraUser admin = masterContext.getHeraUserService().findByName(HeraGlobalEnvironment.getAdmin());

        if (admin != null) {
            try {
                masterContext.getEmailService().sendEmail("警告:work断线了", "worker ip = " + ip.substring(1), admin.getEmail());
                //WeiChatUtil.sendWeiChatMessage(admin, "警告-work断线了", "ip是" + ip.substring(1));
            } catch (MessagingException e) {
                ErrorLog.error(LogHelpUtil.getSpecificTrace(e));
                e.printStackTrace();
            }
        }
        MasterWorkHolder workHolder = masterContext.getWorkMap().get(channel);
        masterContext.getWorkMap().remove(channel);
        if (workHolder != null) {
            List<String> scheduleTask = workHolder.getHeartBeatInfo().getRunning();

            if (scheduleTask == null || scheduleTask.size() == 0) {
                return;
            }
            //十分钟后开始检查 work是否重连成功
            masterContext.masterSchedule.schedule(() -> {
                try {
                    Channel newChannel = null;
                    HeraAction heraAction;
                    HeraJobHistory heraJobHistory;
                    //遍历新的心跳信息 匹配断线ip是否重新连接
                    Set<Channel> channels = masterContext.getWorkMap().keySet();
                    for (Channel cha : channels) {
                        if (getIpFromChannel(cha).equals(ip)) {
                            newChannel = cha;
                            break;
                        }
                    }

                    if (newChannel != null) {
                        SocketLog.warn("work重连成功:{}", newChannel.remoteAddress());
                        // 判断任务状态 无论是否成功，全部重新广播一遍

                        for (String action : scheduleTask) {
                            heraAction = masterContext.getHeraJobActionService().findById(action);
                            //检测action表是否已经更新 如果更新 证明work的成功信号发送给了master已经广播
                            if (StatusEnum.SUCCESS.toString().equals(heraAction.getStatus())) {
                                SocketLog.warn("任务{}已经执行完成并发信号给master，无需重试", action);
                                continue;
                            }
                            heraJobHistory = masterContext.getHeraJobHistoryService().findById(heraAction.getHistoryId());
                            //如果work已经运行成功但是成功信号没有发送给master master做一次广播
                            if (StatusEnum.SUCCESS.toString().equals(heraJobHistory.getStatus())) {
                                HeraJobSuccessEvent successEvent = new HeraJobSuccessEvent(action, TriggerTypeEnum.parser(heraJobHistory.getTriggerType())
                                        , heraJobHistory.getId());
                                heraAction.setStatus(heraJobHistory.getStatus());
                                masterContext.getHeraJobActionService().updateStatus(heraAction);
                                SocketLog.warn("任务{}已经执行完成但是信号未发送给master,手动广播成功事件", action);
                                //成功时间广播
                                masterContext.getDispatcher().forwardEvent(successEvent);
                            } else if (StatusEnum.FAILED.toString().equals(heraJobHistory.getStatus())) {

                                SocketLog.warn("任务{}执行失败，但是丢失重试次数，重新调度", action);
                                //丢失重试次数信息   master直接重试
                                heraJobHistory.setIllustrate("work断线，丢失任务重试次数，重新执行该任务");
                                startNewJob(heraJobHistory, LogConstant.RETRY_JOB);
                            } else if (StatusEnum.RUNNING.toString().equals(heraJobHistory.getStatus())) {
                                //如果仍然在运行中，那么检测新的心跳信息 判断work是断线重连 or 重启
                                HeartBeatInfo newBeatInfo = masterContext.getWorkMap().get(newChannel).getHeartBeatInfo();
                                if (newBeatInfo == null) {
                                    TimeUnit.SECONDS.sleep(HeraGlobalEnvironment.getHeartBeat() * 2);

                                    newBeatInfo = masterContext.getWorkMap().get(newChannel).getHeartBeatInfo();
                                }
                                if (newBeatInfo != null) {
                                    List<String> newRunning = newBeatInfo.getRunning();
                                    //如果work新的心跳信息 包含该任务的信息 work继续执行即可
                                    if (newRunning.contains(action)) {
                                        SocketLog.warn("任务{}还在运行中，并且work重连后心跳信息存在，等待work执行完成", action);
                                        continue;
                                    }
                                }
                                heraJobHistory.setIllustrate("work心跳该任务信息为空，重新执行该任务");
                                SocketLog.warn("任务{}还在运行中，但是work已经无该任务的相关信息，重新调度该任务", action);
                                //不包含该任务信息，重新调度
                                startNewJob(heraJobHistory, LogConstant.RETRY_JOB);
                            }
                        }
                    } else {
                        for (String action : scheduleTask) {
                            heraAction = masterContext.getHeraJobActionService().findById(action);
                            heraJobHistory = masterContext.getHeraJobHistoryService().findById(heraAction.getHistoryId());
                            heraJobHistory.setIllustrate("work断线超出十分钟，重新执行该任务");
                            SocketLog.warn("work断线并且未重连，重新调度任务{}", action);
                            startNewJob(heraJobHistory, LogConstant.RETRY_JOB);
                        }
                    }
                } catch (InterruptedException e) {
                    ErrorLog.error(LogHelpUtil.getSpecificTrace(e));
                    ErrorLog.error("work断线任务检测异常{}", e);
                }
            }, 10, TimeUnit.MINUTES);

            String content = "不幸的消息，work宕机了:" + channel.remoteAddress() + "<br>" +
                    "自动调度队列任务：" + workHolder.getHeartBeatInfo().getRunning() + "<br>" +
                    "手动队列任务：" + workHolder.getHeartBeatInfo().getManualRunning() + "<br>" +
                    "开发中心队列任务：" + workHolder.getHeartBeatInfo().getDebugRunning() + "<br>";
            ErrorLog.error(content);
        }
    }

    private void startNewJob(HeraJobHistory heraJobHistory, String illustrate) {
        heraJobHistory.setStatus(StatusEnum.FAILED.toString());
        masterContext.getHeraJobHistoryService().update(heraJobHistory);
        HeraJobHistory newHistory = HeraJobHistory.builder().
                actionId(heraJobHistory.getActionId()).
                illustrate(illustrate).
                jobId(heraJobHistory.getJobId()).
                triggerType(heraJobHistory.getTriggerType()).
                operator(heraJobHistory.getOperator()).
                hostGroupId(heraJobHistory.getHostGroupId()).
                log(heraJobHistory.getIllustrate()).build();
        masterContext.getHeraJobHistoryService().insert(newHistory);
        this.run(BeanConvertUtils.convert(newHistory));
    }

    private String getIpFromChannel(Channel channel) {
        return channel.remoteAddress().toString().split(":")[0];
    }


}
