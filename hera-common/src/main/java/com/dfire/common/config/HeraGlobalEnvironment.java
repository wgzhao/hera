package com.dfire.common.config;

import com.dfire.common.enums.OperatorSystemEnum;
import com.dfire.common.logs.HeraLog;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * @author xiaosuda
 * @date 2018/4/16
 */
@Component
public class HeraGlobalEnvironment {

    @Getter
    public static String excludeFile;

    public static int defaultWorkerGroup;

    public static Integer preemptionMasterGroup;
    /**
     * 用户环境变量
     */
    public static Map<String, String> userEnvMap = new HashMap<>();
    @Getter
    private static long requestTimeout = 300 * 1000L;
    @Getter
    private static long channelTimeout = 1000L;
    @Getter
    private static int jobCacheDay;
    @Getter
    private static String loadBalance;
    @Getter
    private static String env;
    @Getter
    private static int warmUpCheck;
    @Getter
    private static Float maxMemRate;
    @Getter
    private static Float maxCpuLoadPerCore;
    @Getter
    private static Float perTaskUseMem;
    @Getter
    private static Float systemMemUsed;
    @Getter
    private static Integer scanRate;
    @Getter
    private static Integer connectPort;
    @Getter
    private static String workDir;
    @Getter
    private static Integer maxParallelNum;
    @Getter
    private static Integer heartBeat;
    @Getter
    private static String admin;
    @Getter
    private static Integer taskTimeout;
    @Getter
    private static String hdfsUploadPath;
    /**
     * 判断是否是linux 环境，有些命令不一样
     */
    private static boolean linuxSystem = false;
    @Getter
    private static OperatorSystemEnum systemEnum;

    static {
        String os = System.getProperties().getProperty("os.name");
        if (os != null) {
            if (os.toLowerCase().startsWith("win")) {
                systemEnum = OperatorSystemEnum.WIN;
            } else if (os.toLowerCase().startsWith("mac")) {
                systemEnum = OperatorSystemEnum.MAC;
            } else {
                systemEnum = OperatorSystemEnum.LINUX;
                linuxSystem = true;
            }
        }
        // 全局配置，支持中文不乱
        userEnvMap.putAll(System.getenv());
        userEnvMap.put("LANG", "zh_CN.UTF-8");
    }

    public static boolean isLinuxSystem() {
        return linuxSystem;
    }

    @Value("${hera.excludeFile")
    public void setExcludeFile(String excludeFile) {
        HeraGlobalEnvironment.excludeFile = excludeFile;
    }

    @Value("${hera.defaultWorkerGroup}")
    public void setDefaultWorkerGroup(int defaultWorkerGroup) {
        HeraGlobalEnvironment.defaultWorkerGroup = defaultWorkerGroup;
    }

    @Value("${hera.preemptionMasterGroup}")
    public void setPreemptionMasterGroup(Integer preemptionMasterGroup) {
        HeraGlobalEnvironment.preemptionMasterGroup = preemptionMasterGroup;
    }

    @Value("${hera.env}")
    public void setEnv(String env) {
        HeraGlobalEnvironment.env = env;
    }

    @Value("${hera.maxMemRate}")
    public void setMaxMemRate(Float maxMemRate) {
        HeraGlobalEnvironment.maxMemRate = maxMemRate;
    }

    @Value("${hera.maxCpuLoadPerCore}")
    public void setCpuLoadPerCore(Float maxCpuLoadPerCore) {
        HeraGlobalEnvironment.maxCpuLoadPerCore = maxCpuLoadPerCore;
    }

    @Value("${hera.hdfsUploadPath}")
    public void setHdfsUploadPath(String hdfsUploadPath) {
        HeraGlobalEnvironment.hdfsUploadPath = hdfsUploadPath;
    }

    @Value("${hera.scanRate}")
    public void setScanRate(Integer scanRate) {
        HeraGlobalEnvironment.scanRate = scanRate;
    }

    @Value("${hera.connectPort}")
    public void setConnectPort(Integer connectPort) {
        HeraGlobalEnvironment.connectPort = connectPort;
    }

    @Value("${hera.workDir}")
    public void setWorkDir(String workDir) {
        File file = new File(workDir);
        if (!file.exists()) {
            HeraGlobalEnvironment.workDir = System.getProperty("user.dir");
            HeraLog.warn("配置的工作路径" + workDir + "不存在，将使用默认路径:" + HeraGlobalEnvironment.workDir);
        } else {
            HeraGlobalEnvironment.workDir = workDir;
        }
    }

    @Value("${hera.maxParallelNum}")
    public void setMaxParallelNum(Integer maxParallelNum) {
        HeraGlobalEnvironment.maxParallelNum = maxParallelNum;
    }

    @Value("${hera.jobCacheDay}")
    public void setJobCacheDay(int jobCacheDay) {
        HeraGlobalEnvironment.jobCacheDay = jobCacheDay;
    }

    @Value("${hera.admin}")
    public void setAdmin(String admin) {
        HeraGlobalEnvironment.admin = admin;
    }

    @Value("${hera.taskTimeout}")
    public void setTaskTimeout(Integer taskTimeout) {
        HeraGlobalEnvironment.taskTimeout = taskTimeout;
    }

    @Value("${hera.heartBeat}")
    public void setHeartBeat(Integer heartBeat) {
        HeraGlobalEnvironment.heartBeat = heartBeat;
    }

    @Value("${hera.perTaskUseMem}")
    public void setPerTaskUseMem(Float perTaskUseMem) {
        HeraGlobalEnvironment.perTaskUseMem = perTaskUseMem;
    }

    @Value("${hera.systemMemUsed}")
    public void setSystemMemUsed(Float systemMemUsed) {
        HeraGlobalEnvironment.systemMemUsed = systemMemUsed;
    }

    @Value("${hera.maxCpuLoadPerCore}")
    public void setMaxCpuLoadPerCore(Float maxCpuLoadPerCore) {
        HeraGlobalEnvironment.maxCpuLoadPerCore = maxCpuLoadPerCore;
    }

    @Value("${hera.loadBalance}")
    public void setLoadBalance(String loadBalance) {
        HeraGlobalEnvironment.loadBalance = loadBalance;
    }

    @Value("${hera.warmUpCheck}")
    public void setWarmUpCheck(int warmUpCheck) {
        HeraGlobalEnvironment.warmUpCheck = warmUpCheck;
    }

    @Value("${hera.requestTimeout}")
    public void setTimeout(Long requestTimeout) {
        HeraGlobalEnvironment.requestTimeout = requestTimeout;
    }

    @Value("${hera.channelTimeout}")
    public void setChannelTimeout(Long channelTimeout) {
        HeraGlobalEnvironment.channelTimeout = channelTimeout;
    }

    }
