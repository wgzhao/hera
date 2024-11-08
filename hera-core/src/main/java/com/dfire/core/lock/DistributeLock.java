package com.dfire.core.lock;

import com.dfire.common.entity.HeraLock;
import com.dfire.common.service.HeraHostRelationService;
import com.dfire.common.service.HeraLockService;
import com.dfire.common.config.HeraGlobalEnvironment;
import com.dfire.core.event.LogHelpUtil;
import com.dfire.core.netty.worker.WorkClient;
import com.dfire.core.netty.worker.WorkContext;
import com.dfire.core.schedule.HeraSchedule;
import com.dfire.common.logs.ErrorLog;
import com.dfire.common.logs.HeraLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 20:47 2018/1/10
 * @desc 基于数据库实现的分布式锁方案，后面优化成基于redis实现分布式锁
 */
@Component
public class DistributeLock {


    private final long timeout = 1000 * 60 * 5L;
    private final String ON_LINE = "online";
    @Autowired
    private HeraHostRelationService hostGroupService;
    @Autowired
    private HeraLockService heraLockService;
    @Autowired
    private WorkClient workClient;
    @Autowired
    private HeraSchedule heraSchedule;


    /**
     * 被@PostConstruct修饰的方法会在服务器加载Servlet的时候运行，并且只会被服务器执行一次。
     * PostConstruct在构造函数之后执行，init（）方法之前执行。
     * PreDestroy（）方法在destroy（）方法执行之后执行
     * 链接：https://www.jianshu.com/p/98cf7d8b9ec3
     */
    @PostConstruct
    public void init() {
        workClient.workSchedule.scheduleAtFixedRate(() -> {
            try {
                checkLock();
            } catch (Exception e) {
                ErrorLog.error(LogHelpUtil.getSpecificTrace(e));
                e.printStackTrace();
            }
        }, 10, 60, TimeUnit.SECONDS);
    }

    public void checkLock() {
        ///获取master的信息 存储在hera_lock中
        HeraLock heraLock = heraLockService.findBySubgroup(ON_LINE);
        ///如果没有master
        if (heraLock == null) {
            Date date = new Date();
            heraLock = HeraLock.builder()
                    .id(1)
                    .host(WorkContext.host)///机器对应ip
                    .serverUpdate(date)///master的心跳更新时间
                    .subgroup(ON_LINE)
                    .gmtCreate(date)///master的创建时间
                    .gmtModified(date)///master的更新时间
                    .build();
            ///设置master信息
            Integer lock = heraLockService.insert(heraLock);
            ///master信息设置不成功结束本次定时任务,等待下一次定时任务触发
            if (lock == null || lock <= 0) {
                return;
            }
        }

        if (WorkContext.host.equals(heraLock.getHost().trim())) {///节点是master节点
            ///更新 心跳更新时间 -> hera_lock 数据更新
            heraLock.setServerUpdate(new Date());
            heraLockService.update(heraLock);
            HeraLog.info("hold lock and update time");
            ///是master节点调度器开始执行
            heraSchedule.startup();
        } else {///节点不是master节点
            long currentTime = System.currentTimeMillis();
            long lockTime = heraLock.getServerUpdate().getTime();
            ///计算master节点上一次心跳更新时间距离现在的时间
            long interval = currentTime - lockTime;
            ///判断master是否断线 并 判断节点是否拥有抢占master节点的权限,超过5分钟未更新心跳更新时间判定为master断线
            if (interval > timeout && isPreemptionHost()) {
                Date date = new Date();
                ///抢占master
                Integer lock = heraLockService.changeLock(WorkContext.host, date, date, heraLock.getHost());
                if (lock != null && lock > 0) {
                    ErrorLog.error("master 发生切换,{} 抢占成功", WorkContext.host);
                    heraSchedule.startup();
                    heraLock.setHost(WorkContext.host);
                    //TODO  接入master切换通知
                } else {
                    HeraLog.info("master抢占失败，由其它worker抢占成功");
                }
            } else {
                //非主节点，调度器不执行
                heraSchedule.shutdown();
            }
        }
        workClient.init();
        try {
            workClient.connect(heraLock.getHost().trim());
        } catch (Exception e) {
            ErrorLog.error(LogHelpUtil.getSpecificTrace(e));
            e.printStackTrace();
        }
    }

    /**
     * 检测该ip是否具有抢占master的权限
     *
     * @return 是/否
     */
    private boolean isPreemptionHost() {
        ///获取拥有抢占master权限的机器
        List<String> preemptionHostList = hostGroupService.findPreemptionGroup(HeraGlobalEnvironment.preemptionMasterGroup);
        if (preemptionHostList.contains(WorkContext.host)) {
            return true;
        } else {
            HeraLog.info(WorkContext.host + " is not in master group " + preemptionHostList);
            return false;
        }
    }
}