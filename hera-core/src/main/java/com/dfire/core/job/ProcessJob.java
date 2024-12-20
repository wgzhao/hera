package com.dfire.core.job;

import com.alibaba.fastjson2.JSONObject;
import com.dfire.common.constants.Constants;
import com.dfire.common.util.HierarchyProperties;
import com.dfire.common.config.HeraGlobalEnvironment;
import com.dfire.core.event.LogHelpUtil;
import com.dfire.common.logs.ErrorLog;
import com.dfire.common.logs.HeraLog;
import com.dfire.common.logs.TaskLog;
import com.dfire.core.exception.HeraCaughtExceptionHandler;

import java.io.*;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 11:01 2018/3/23
 * @desc 通过操作系统创建进程Process的Job任务
 */
public abstract class ProcessJob extends AbstractJob implements Job {

    protected final Map<String, String> envMap;
    protected volatile Process process;
    private int exitCode;


    public ProcessJob(JobContext jobContext) {
        super(jobContext);
        envMap = HeraGlobalEnvironment.userEnvMap;
    }

    /**
     * @param command
     * @return
     * @desc 对hera中的操作系统命令进行拆分成字符串数组，方便给ProcessBuilder传命令参数，
     * 如："free -m | grep buffers/cache"，成为：{“free”，“-m”，“|”，“grep”，“buffers/cache”}
     */
    public static String[] partitionCommandLine(String command) {
        List<String> commands = new ArrayList<>();
        StringBuilder builder = new StringBuilder(command.length());
        int index = 0;
        boolean isApostrophe = false;
        boolean isQuote = false;
        while (index < command.length()) {
            char c = command.charAt(index);
            switch (c) {
                case ' ':
                    if (!isQuote && !isApostrophe) {
                        String arg = builder.toString();
                        builder = new StringBuilder(command.length() - index);
                        if (!arg.isEmpty()) {
                            commands.add(arg);
                        }
                    } else {
                        builder.append(c);
                    }
                    break;
                case '\'':
                    if (!isQuote) {
                        isApostrophe = !isApostrophe;
                    } else {
                        builder.append(c);
                    }
                    break;
                case '"':
                    if (!isApostrophe) {
                        isQuote = !isQuote;
                    } else {
                        builder.append(c);
                    }
                    break;
                default:
                    builder.append(c);
            }
            index++;
        }
        if (builder.length() > 0) {
            String arg = builder.toString();
            commands.add(arg);
        }
        TaskLog.info("5.2 ProcessJob :组装后的命令为：{}", JSONObject.toJSONString(commands));
        return commands.toArray(new String[commands.size()]);
    }

    /**
     * 组装脚本执行命令
     *
     * @return
     */
    public abstract List<String> getCommandList();

    @Override
    public int run() throws Exception {
        exitCode = Constants.DEFAULT_EXIT_CODE;
        jobContext.getProperties().getAllProperties().keySet().stream()
                .filter(key -> jobContext.getProperties().getProperty(key) != null && (key.startsWith("secret.")))
                .forEach(k -> envMap.put(k, jobContext.getProperties().getProperty(k)));
        List<String> commands = getCommandList();

        for (String command : commands) {
            String[] splitCommand = partitionCommandLine(command);
            ProcessBuilder builder = new ProcessBuilder(splitCommand);
            builder.directory(new File(jobContext.getWorkDir()));
            builder.environment().putAll(envMap);
            try {
                process = builder.start();
            } catch (IOException e) {
                ErrorLog.error(LogHelpUtil.getSpecificTrace(e));
                e.printStackTrace();
            }
            String threadName;
            if (jobContext.getHeraJobHistory() != null && jobContext.getHeraJobHistory().getJobId() != null) {
                threadName = "actionId=" + jobContext.getHeraJobHistory().getJobId();
            } else if (jobContext.getDebugHistory() != null && jobContext.getDebugHistory().getId() != null) {
                threadName = "debugId=" + jobContext.getDebugHistory().getId();
            } else {
                threadName = "not-normal-job";
            }
            CountDownLatch latch = new CountDownLatch(2);
            Thread inputThread = new StreamThread(process.getInputStream(), threadName, latch);
            Thread outputThread = new StreamThread(process.getErrorStream(), threadName, latch);
            inputThread.setUncaughtExceptionHandler(new HeraCaughtExceptionHandler());
            outputThread.setUncaughtExceptionHandler(new HeraCaughtExceptionHandler());
            inputThread.start();
            outputThread.start();
            try {
                exitCode = process.waitFor();
                latch.await();
            } catch (InterruptedException e) {
                ErrorLog.error(LogHelpUtil.getSpecificTrace(e));
                exitCode = Constants.INTERRUPTED_EXIT_CODE;
                log(e);
            } finally {
                process = null;
            }
        }
        return exitCode;
    }

    @Override
    public void cancel() {
        try {
            new CancelHadoopJob(jobContext).run();
        } catch (Exception e) {
            ErrorLog.error(LogHelpUtil.getSpecificTrace(e));
            log(e);
        }
        //强制kill 进程
        if (process != null) {
            log("WARN Attempting to kill the process ");
            try {
                process.destroy();
                int pid = getProcessId();
                String st = "sudo sh -c \"cd; pstree " + pid + " -p | grep -o '([0-9]*)' | awk -F'[()]' '{print \\$2}' | xargs kill -9\"";
                String[] commands = {"sudo", "sh", "-c", st};
                ProcessBuilder processBuilder = new ProcessBuilder(commands);
                try {
                    process = processBuilder.start();
                    log("kill process tree success");
                } catch (Exception e) {
                    ErrorLog.error(LogHelpUtil.getSpecificTrace(e));
                    log(e);
                }
            } catch (Exception e) {
                ErrorLog.error(LogHelpUtil.getSpecificTrace(e));
                log(e);
            } finally {
                process = null;
            }
        }
    }

    private int getProcessId() {
        int processId = 0;
        try {
            Field f = process.getClass().getDeclaredField("pid");
            f.setAccessible(true);
            processId = f.getInt(process);
        } catch (Throwable e) {
            ErrorLog.error(LogHelpUtil.getSpecificTrace(e));
            log(e.getMessage());
        }
        return processId;
    }

    @Override
    protected String getProperty(String key, String defaultValue) {
        String value = jobContext.getProperties().getProperty(key);
        if (value == null) {
            value = defaultValue;
        }
        return value;
    }

    @Override
    public HierarchyProperties getProperties() {
        return jobContext.getProperties();
    }

    @Override
    public JobContext getJobContext() {
        return jobContext;
    }

    /**
     * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
     * @time: Created in 11:01 2018/3/26
     * @desc job输出流日志接收线程
     */
    public class StreamThread extends Thread {
        private final InputStream inputStream;
        private final String threadName;
        private final CountDownLatch latch;

        private StreamThread(InputStream inputStream, String threadName, CountDownLatch latch) {
            this.inputStream = inputStream;
            this.threadName = threadName;
            this.latch = latch;
        }

        @Override
        public void run() {
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
                String line;
                while ((line = reader.readLine()) != null) {
                    logConsole(line);
                }
            } catch (Exception e) {
                ErrorLog.error(LogHelpUtil.getSpecificTrace(e));
                exitCode = Constants.LOG_EXIT_CODE;
                HeraLog.error("接受日志异常:{}", e);
                log(threadName + ": 接收日志出错，退出日志接收");
            } finally {
                latch.countDown();
            }
        }
    }

}
