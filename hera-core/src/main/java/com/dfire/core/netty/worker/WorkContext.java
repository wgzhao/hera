package com.dfire.core.netty.worker;

import com.dfire.common.service.*;
import com.dfire.common.util.NamedThreadFactory;
import com.dfire.common.config.HeraGlobalEnvironment;
import com.dfire.core.event.LogHelpUtil;
import com.dfire.core.job.Job;
import com.dfire.core.netty.HeraChannel;
import com.dfire.core.tool.RunShell;
import com.dfire.core.util.NetUtils;
import com.dfire.common.logs.ErrorLog;
import com.dfire.common.logs.HeraLog;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.*;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 11:30 2018/1/10
 * @desc
 */
@Data
@NoArgsConstructor
@Component
public class WorkContext {

    private static final String loadStr = "cat /proc/cpuinfo |grep processor | wc -l";
    public static String host;
    public static Integer cpuCoreNum;

    static {
        host = NetUtils.getLocalAddress();

        HeraLog.info("-----------------------------当前机器的IP为:{}-----------------------------", host);
        if (HeraGlobalEnvironment.isLinuxSystem()) {
            RunShell shell = new RunShell(loadStr);
            Integer exitCode = shell.run();
            if (exitCode == 0) {
                try {
                    cpuCoreNum = Integer.parseInt(shell.getResult());
                } catch (IOException e) {
                    ErrorLog.error(LogHelpUtil.getSpecificTrace(e));
                    e.printStackTrace();
                    cpuCoreNum = 4;
                }
            }
        } else {
            cpuCoreNum = 4;
        }

    }

    public String serverHost;
    @Autowired
    private HeraDebugHistoryService heraDebugHistoryService;
    @Autowired
    private HeraJobHistoryService heraJobHistoryService;
    @Autowired
    @Qualifier("heraGroupMemoryService")
    private HeraGroupService heraGroupService;
    @Autowired
    private HeraJobActionService heraJobActionService;
    @Autowired
    @Qualifier("heraFileMemoryService")
    private HeraFileService heraFileService;
    @Autowired
    private HeraProfileService heraProfileService;
    @Autowired
    @Qualifier("heraJobMemoryService")
    private HeraJobService heraJobService;
    private HeraChannel serverChannel;
    private Map<String, Job> running = new ConcurrentHashMap<>();
    private Map<String, Job> manualRunning = new ConcurrentHashMap<>();
    private Map<String, Job> debugRunning = new ConcurrentHashMap<>();
    private WorkHandler handler;
    private WorkClient workClient;
    /**
     * 处理web 异步请求
     */
    private ExecutorService workWebThreadPool = new ThreadPoolExecutor(
            0,
            Integer.MAX_VALUE,
            1L, TimeUnit.MINUTES,
            new SynchronousQueue<>(),
            new NamedThreadFactory("worker-web"),
            new ThreadPoolExecutor.AbortPolicy()
    );

    /**
     * 执行任务
     */
    private ExecutorService workExecuteThreadPool = new ThreadPoolExecutor(
            0,
            Integer.MAX_VALUE,
            1L, TimeUnit.MINUTES,
            new SynchronousQueue<>(),
            new NamedThreadFactory("worker-execute"),
            new ThreadPoolExecutor.AbortPolicy()
    );


}
