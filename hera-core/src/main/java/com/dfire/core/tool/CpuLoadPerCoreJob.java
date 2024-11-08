package com.dfire.core.tool;

import com.dfire.common.config.HeraGlobalEnvironment;
import com.dfire.core.event.LogHelpUtil;
import com.dfire.core.netty.worker.WorkContext;
import com.dfire.common.logs.ErrorLog;
import lombok.Getter;

import java.io.IOException;

/**
 * @author xiaosuda
 * @date 2018/8/6
 */
public class CpuLoadPerCoreJob extends RunShell {

    private final String keys = "load average:";
    private final Integer keysLen = keys.length();
    @Getter private float loadPerCore = 1f;

    public CpuLoadPerCoreJob() {
        super("uptime");
    }

    @Override
    public int run() {
        if (!HeraGlobalEnvironment.isLinuxSystem()) {
            return -1;
        }
        int exitCode = super.run();
        if (exitCode == 0) {
            try {
                String result = super.getResult();
                loadPerCore = getCpuLoad(result) / WorkContext.cpuCoreNum;
            } catch (IOException e) {
                ErrorLog.error(LogHelpUtil.getSpecificTrace(e));
                e.printStackTrace();
            }
        }
        return exitCode;
    }


    private Float getCpuLoad(String result) {
        String loadStr = result.substring(result.indexOf(keys) + keysLen);
        loadStr = loadStr.replace(",", " ").trim();
        String[] split = loadStr.split(" ");
        return Float.parseFloat(split[0]);
    }
}
