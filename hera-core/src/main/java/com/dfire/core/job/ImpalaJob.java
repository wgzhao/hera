package com.dfire.core.job;

import com.dfire.common.constants.RunningJobKeyConstant;
import com.dfire.core.event.LogHelpUtil;
import com.dfire.common.logs.ErrorLog;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;


public class ImpalaJob extends ProcessJob {


    public ImpalaJob(JobContext jobContext) {
        super(jobContext);
        jobContext.getProperties().setProperty(RunningJobKeyConstant.JOB_RUN_TYPE, "ImpalaJob");
    }

    @Override
    public int run() throws Exception {
        return runInner();
    }

    private Integer runInner() throws Exception {
        String script = getProperties().getLocalProperty(RunningJobKeyConstant.JOB_SCRIPT);
        File file = new File(jobContext.getWorkDir() + File.separator + System.currentTimeMillis() + ".impala");
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                ErrorLog.error(LogHelpUtil.getSpecificTrace(e));
                ErrorLog.error("创建.impala失败");
            }
        }

        try (OutputStreamWriter writer = new OutputStreamWriter(Files.newOutputStream(file.toPath()),
                Charset.forName(jobContext.getProperties().getProperty("hera.fs.encode", "utf-8")))) {
            writer.write(script.replaceAll("^--.*", "--"));
        }
        catch (Exception e) {
            ErrorLog.error(LogHelpUtil.getSpecificTrace(e));
            if (jobContext.getHeraJobHistory() != null) {
                jobContext.getHeraJobHistory().getLog().appendHeraException(e);
            }
            else {
                jobContext.getDebugHistory().getLog().appendHeraException(e);
            }
        }

        getProperties().setProperty(RunningJobKeyConstant.RUN_HIVE_PATH, file.getAbsolutePath());
        return super.run();
    }

    @Override
    public List<String> getCommandList() {
        String hiveFilePath = getProperty(RunningJobKeyConstant.RUN_HIVE_PATH, "");
        List<String> list = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        String shellPrefix = getJobPrefix();
        boolean isDocToUnix = checkDosToUnix(hiveFilePath);

        if (isDocToUnix) {
            list.add("dos2unix " + hiveFilePath);
            log("dos2unix file" + hiveFilePath);
        }

        sb.append(" -f ").append(hiveFilePath).append(" >").append(jobContext.getWorkDir()).append("/run.log").append(" 2>&1");

        if (StringUtils.isNotBlank(shellPrefix)) {
            String tmpFilePath = jobContext.getWorkDir() + File.separator + "tmp.sh";
            File tmpFile = new File(tmpFilePath);
            OutputStreamWriter tmpWriter = null;
            if (!tmpFile.exists()) {
                try {
                    tmpFile.createNewFile();
                    tmpWriter = new OutputStreamWriter(Files.newOutputStream(tmpFile.toPath()),
                            Charset.forName(jobContext.getProperties().getProperty("hera.fs.encode", "utf-8")));
                    tmpWriter.write("impala-shell " + sb);
                } catch (Exception e) {
                    ErrorLog.error(LogHelpUtil.getSpecificTrace(e));
                    jobContext.getHeraJobHistory().getLog().appendHeraException(e);
                } finally {
                    if (tmpWriter != null) {
                        try {
                            tmpWriter.close();
                        } catch (IOException e) {
                            ErrorLog.error(LogHelpUtil.getSpecificTrace(e));
                            e.printStackTrace();
                        }
                    }
                }
                list.add("chmod -R 777 " + jobContext.getWorkDir());
                list.add(shellPrefix + " sh " + tmpFilePath);
            } else {
                list.add("chmod -R 777 " + jobContext.getWorkDir());
                list.add(shellPrefix + " impala-shell " + sb);
            }

        } else {
            list.add("impala-shell" + sb);
        }
        return list;
    }
}
