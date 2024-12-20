package com.dfire.core.util;

import com.dfire.common.constants.RunningJobKeyConstant;
import com.dfire.common.entity.HeraDebugHistory;
import com.dfire.common.entity.model.HeraJobBean;
import com.dfire.common.entity.vo.HeraJobHistoryVo;
import com.dfire.common.entity.vo.HeraProfileVo;
import com.dfire.common.enums.JobRunTypeEnum;
import com.dfire.common.util.BeanConvertUtils;
import com.dfire.common.util.HierarchyProperties;
import com.dfire.common.util.RenderHierarchyProperties;
import com.dfire.core.job.*;
import com.dfire.core.netty.worker.WorkContext;
import com.dfire.common.logs.HeraLog;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 上午12:13 2018/4/26
 * @desc 任务执行体job的创建工具，创建任务依据任务配置创建任务前置处理，核心执行体，后置处理，以及脚本中的变量替换
 */
public class JobUtils {

    public static final Pattern pattern = Pattern.compile("download\\[(hdfs)://.+]");

    public static Job createDebugJob(JobContext jobContext, HeraDebugHistory heraDebugHistory,
                                     String workDir, WorkContext workContext) {
        jobContext.setDebugHistory(BeanConvertUtils.convert(heraDebugHistory));
        jobContext.setWorkDir(workDir);

        HierarchyProperties hierarchyProperties = new HierarchyProperties(new HashMap<>());
        String script = heraDebugHistory.getScript();
        List<Map<String, String>> resources = new ArrayList<>();
        script = resolveScriptResource(resources, script, workContext);
        jobContext.setResources(resources);
        hierarchyProperties.setProperty(RunningJobKeyConstant.JOB_SCRIPT, script);

        String owner = workContext.getHeraFileService().findById(heraDebugHistory.getFileId()).getOwner();
        HeraProfileVo heraProfile = workContext.getHeraProfileService().findByOwner(owner);
        if (heraProfile != null && heraProfile.getHadoopConf() != null) {
            for (String key : heraProfile.getHadoopConf().keySet()) {
                hierarchyProperties.setProperty(key, heraProfile.getHadoopConf().get(key));
            }
        }

        jobContext.setProperties(new RenderHierarchyProperties(hierarchyProperties));
        hierarchyProperties.setProperty("hadoop.mappred.job.hera_id", "hera_debug_" + heraDebugHistory.getId());

        List<Job> pres = new ArrayList<>(1);
        pres.add(new DownLoadJob(jobContext));
        Job core = null;
        // convert to switch logic
        switch (heraDebugHistory.getRunType().toUpperCase()) {
            case "SHELL":
                jobContext.putData(RunningJobKeyConstant.JOB_RUN_TYPE, JobRunTypeEnum.Shell.toString());
                core = new HadoopShellJob(jobContext);
                break;
            case "HIVE":
                jobContext.putData(RunningJobKeyConstant.JOB_RUN_TYPE, JobRunTypeEnum.Hive.toString());
                core = new HiveJob(jobContext);
                break;
            case "IMPALA":
                jobContext.putData(RunningJobKeyConstant.JOB_RUN_TYPE, JobRunTypeEnum.Impala.toString());
                core = new ImpalaJob(jobContext);
                break;
            default:
                HeraLog.error("unsupported run type: " + heraDebugHistory.getRunType());
        }
        return new ProcessJobContainer(jobContext, pres, new ArrayList<>(), core);
    }

    public static Job createScheduleJob(JobContext jobContext, HeraJobBean jobBean,
                                        HeraJobHistoryVo history, String workDir, WorkContext workContext) {
        jobContext.setHeraJobHistory(history);
        jobContext.setWorkDir(workDir);
        jobContext.getProperties().setProperty("hera.encode", "utf-8");
        HierarchyProperties hierarchyProperties = jobBean.getHierarchyProperties();

        Map<String, String> configs = history.getProperties();
        if (configs != null && !configs.isEmpty()) {
            history.getLog().appendHera("this job has configs");
            for (String key : configs.keySet()) {
                hierarchyProperties.setProperty(key, configs.get(key));
                history.getLog().appendHera(key + "=" + configs.get(key));
            }
        }

        //   history.getLog().appendHera("\n 具体脚本 : "+jobBean.getHeraJob().getScript()+"\n");

        jobContext.setProperties(new RenderHierarchyProperties(hierarchyProperties));


        List<Map<String, String>> resource = jobBean.getHierarchyResources();


        String script = jobBean.getHeraJob().getScript();


        if (jobBean.getHeraJob().getRunType().equals(JobRunTypeEnum.Shell.toString())
                || jobBean.getHeraJob().getRunType().equals(JobRunTypeEnum.Hive.toString())) {
            script = resolveScriptResource(resource, script, workContext);
        }
        jobContext.setResources(resource);
        script = replace(jobContext.getProperties().getAllProperties(), script);
        script = RenderHierarchyProperties.render(script, history.getActionId().substring(0, 12));
        hierarchyProperties.setProperty(RunningJobKeyConstant.JOB_SCRIPT, script);

        List<Job> pres = new ArrayList<>();
        pres.add(new DownLoadJob(jobContext));
        List<Job> posts = new ArrayList<>();
        Job core = null;
        JobRunTypeEnum runType = JobRunTypeEnum.parser(jobBean.getHeraJob().getRunType());
        if (runType == JobRunTypeEnum.Shell) {
            core = new HadoopShellJob(jobContext);
        } else if (runType == JobRunTypeEnum.Hive) {
            core = new HiveJob(jobContext);
        } else if (runType == JobRunTypeEnum.Impala) {
            core = new ImpalaJob(jobContext);
        }
        return new ProcessJobContainer(jobContext, pres, posts, core);

    }


    public static String replace(Map<String, String> allProperties, String script) {
        if (script == null) {
            return null;
        }
        Map<String, String> varMap = new HashMap<>(allProperties.size());
        for (Map.Entry<String, String> entry : allProperties.entrySet()) {
            varMap.put("${" + entry.getKey() + "}", entry.getValue());

          /*  System.err.println();
            System.err.println( entry.getKey()+"___"+entry.getValue());
            System.err.println();*/

        }
        String old;
        for (Map.Entry<String, String> entry : varMap.entrySet()) {
            do {
                old = script;
                script = script.replace(entry.getKey(), entry.getValue());
            } while (!old.equals(script));
        }
        return script;
    }

    /**
     * 解析脚本中download开头的脚本，解析后存储在jobContext的resources中，在生成ProcessJobContainer时，根据属性生成preProcess,postProcess
     *
     * @param resources   资源下载
     * @param script      脚本内容
     * @param workContext 工作路径
     * @return
     */
    public static String resolveScriptResource(List<Map<String, String>> resources, String script, WorkContext workContext) {
        Matcher matcher = pattern.matcher(script);
        while (matcher.find()) {
            String group = matcher.group();
            group = group.substring(group.indexOf("[") + 1, group.indexOf("]"));
            String[] url = StringUtils.split(group, " ");
            String uri = null;
            String name = null;
            if (url.length == 1) {
                uri = url[0];
                HeraLog.warn("can not found download name,will use default name,{}", group);
            } else if (url.length == 2) {
                uri = url[0];
                name = url[1];
            }
            Map<String, String> map = new HashMap<>(2);
            boolean exist = false;
            for (Map<String, String> env : resources) {
                if (env.get("name").equals(name)) {
                    exist = true;
                    break;
                }
            }
            //TODO 脚本中不存在 就不用下载了
            if (!exist) {
                map.put("uri", uri);
                map.put("name", name);
                resources.add(map);
            }
        }
        script = matcher.replaceAll("");
        return script;
    }


}
