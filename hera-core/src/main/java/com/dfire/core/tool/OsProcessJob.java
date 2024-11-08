package com.dfire.core.tool;

import com.dfire.common.config.HeraGlobalEnvironment;
import com.dfire.core.event.LogHelpUtil;
import com.dfire.common.logs.ErrorLog;
import com.dfire.protocol.RpcWorkInfo;
import com.dfire.protocol.RpcWorkInfo.OSInfo;
import com.dfire.protocol.RpcWorkInfo.ProcessMonitor;
import lombok.Getter;
import org.apache.commons.lang.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.dfire.common.enums.OperatorSystemEnum.LINUX;

/**
 * @author xiaosuda
 * @date 2018/11/12
 */
public class OsProcessJob
        extends RunShell
{

    @Getter private OSInfo osInfo;
    @Getter private List<ProcessMonitor> processMonitors;

    public OsProcessJob()
    {
        super();
    }

    @Override
    public int run()
    {
        String command = "echo";
        super.setCommand(command);
        if (HeraGlobalEnvironment.getSystemEnum() == LINUX) {
            return runLinux();
        }
        return -1;
    }

    private Float getMemPercent(String used, float total)
    {
        return getMb(used) / total * 100;
    }

    private Float getMb(String used)
    {
        float res = 0.0f;
        String GB = "G";
        String KB = "K";
        String MB = "M";
        if (used.contains(KB)) {
            res = parseFloat(used) / 1024;
        }
        else if (used.contains(MB)) {
            res = parseFloat(used);
        }
        else if (used.contains(GB)) {
            res = parseFloat(used) * 1024;
        }
        return res;
    }

    /**
     * used 数字必须连续
     *
     * @param used
     * @return
     */

    private float parseFloat(String used)
    {
        try {
            String numRegex = "[^.&&\\D]";
            return Float.parseFloat(used.replaceAll(numRegex, ""));
        }
        catch (Exception e) {
            return 0f;
        }
    }

    private Integer runLinux()
    {
        Integer exitCode = -1;
        try {
            exitCode = super.run();
            if (exitCode == 0) {
                String result = super.getResult();
                if (result != null) {
                    String[] lines = result.split("\n");
                    float user = 0.0f, system = 0.0f, cpu = 0.0f,
                            swap = 0.0f, swapTotal = 0.0f, swapUsed = 0.0f, swapCached = 0.0f, swapFree = 0.0f,
                            mem = 0.0f, memTotal = 0.0f, memFree = 0.0f, memBuffers = 0.0f;
                    processMonitors = new ArrayList<>();
                    for (String line : lines) {
                        String[] words = line.trim().split("\\s+");
                        if (words.length > 0) {
                            String first = words[0];
                            if (StringUtils.isBlank(first)) {
                                continue;
                            }
                            if (first.contains("Cpu")) {
                                if ("Cpu(s):".equals(first)) {
                                    user = Float.parseFloat(words[1].replace("%us,", ""));
                                    system = Float.parseFloat(words[2].replace("%sy,", ""));
                                    cpu = Float.parseFloat(words[4].replace("%id,", ""));
                                }
                                else if ("%Cpu(s):".equals(first)) {
                                    user = Float.parseFloat(words[1]);
                                    system = Float.parseFloat(words[3]);
                                    try {
                                        cpu = Float.parseFloat(words[7]);
                                    }
                                    catch (Exception e) {
                                        cpu = Float.parseFloat(words[6].replace("ni,", ""));
                                    }
                                }
                            }
                            else if ("Mem:".equals(first)) {
                                memTotal = parseKb(words[1]);
                                memFree = parseKb(words[5]);
                                memBuffers = parseKb(words[7]);
                            }
                            else if ("Swap:".equals(first)) {
                                swapTotal = parseKb(words[1]);
                                swapUsed = parseKb(words[3]);
                                swapFree = parseKb(words[5]);
                                swapCached = parseKb(words[7]);
                            }
                            else if ("KiB".equals(first)) {
                                if ("Mem".equals(words[1])) {
                                    memTotal = parseKb(words[3]);
                                    memFree = parseKb(words[5]);
                                    memBuffers = parseKb(words[9]);
                                }
                                else if ("Swap".equals(words[1])) {
                                    swapTotal = parseKb(words[2]);
                                    swapFree = parseKb(words[4]);
                                    swapCached = parseKb(words[8]);
                                }
                            }
                            else if (StringUtils.isNumeric(first)) {
                                try {
                                    if (processMonitors.size() > 30) {
                                        continue;
                                    }
                                    processMonitors.add(ProcessMonitor.newBuilder()
                                            .setPid(words[0])
                                            .setUser(words[1])
                                            .setViri(words[4])
                                            .setRes(words[5])
                                            .setCpu(words[8])
                                            .setMem(words[9])
                                            .setTime(words[10])
                                            .setCommand(words[11])
                                            .build());
                                }
                                catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }

                    mem = 1.0f - ((memFree + memBuffers + swapCached) / memTotal);
                    swap = 1.0f - ((swapFree) / swapTotal);
                    processMonitors.sort((o1, o2) -> {
                        int comp;
                        if ((comp = o1.getMem().compareTo(o2.getCommand())) == 0) {
                            return -o1.getCpu().compareTo(o2.getCpu());
                        }
                        return -comp;
                    });
                    osInfo = OSInfo.newBuilder()
                            .setUser(user)
                            .setSystem(system)
                            .setCpu(cpu)
                            .setSwap(swap * 100f)
                            .setMem(mem * 100f)
                            .build();
                }
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return exitCode;
    }

    private float parseKb(String str)
    {
        float res;
        try {
            res = Float.parseFloat(str.replace("k", ""));
        }
        catch (Exception e) {
            res = 0.0f;
        }
        return res;
    }
}
