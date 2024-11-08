package com.dfire.monitor.service.impl;

import com.dfire.common.constants.Constants;
import com.dfire.common.entity.HeraJob;
import com.dfire.common.entity.HeraJobMonitor;
import com.dfire.common.entity.HeraUser;
import com.dfire.common.service.EmailService;
import com.dfire.common.service.HeraJobMonitorService;
import com.dfire.common.service.HeraJobService;
import com.dfire.common.service.HeraUserService;
import com.dfire.common.util.ActionUtil;
import com.dfire.common.util.WeChatUtil;
import com.dfire.common.config.HeraEnvForTest;
import com.dfire.common.config.HeraGlobalEnvironment;
import com.dfire.common.logs.ErrorLog;
import com.dfire.common.logs.ScheduleLog;
import com.dfire.monitor.config.Alarm;
import com.dfire.monitor.service.JobFailAlarm;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import javax.mail.MessagingException;

/**
 * @author xiaosuda
 * @date 2019/2/25
 */
@Alarm
public class EmailJobFailAlarm implements JobFailAlarm {

    @Autowired
    @Qualifier("heraJobMemoryService")
    private HeraJobService heraJobService;

    @Autowired
    private HeraJobMonitorService heraJobMonitorService;

    @Autowired
    private HeraUserService heraUserService;

    @Autowired
    private EmailService emailService;

    @Autowired
    HeraEnvForTest heraEnvForTest;

    @Override
    public void alarm(String actionId, int runCount, int rollCount) {

        System.out.println("alarm  " + actionId + "----" + runCount + "----" + rollCount);

        Integer jobId = ActionUtil.getJobId(actionId);

        if (jobId == null) {
            System.out.println("return 了");
            return;
        }

        System.out.println("没 return ");

        HeraJob heraJob = heraJobService.findById(jobId);

        //System.out.println(JSONObject.toJSONString(heraJob));
        //非开启任务不处理  最好能把这些抽取出去 提供接口实现
        if (heraJob.getAuto() != 1 && !Constants.PUB_ENV.equals(HeraGlobalEnvironment.getEnv())) {
            return;
        }
        StringBuilder address = new StringBuilder();
        try {

            String text = heraEnvForTest.getEnvFlag() + "hera调度" + heraJob.getId() + "号任务失败-" + heraJob.getDescription();
            String desp = "任务Id :" + heraJob.getId()
                    + "\n\n Job版本 : " + actionId
                    + "\n\n 任务名称 : " + heraJob.getName()
                    + "\n\n 相关描述 : " + heraJob.getDescription();
            HeraJobMonitor monitor = heraJobMonitorService.findByJobId(heraJob.getId());
            if (monitor == null && Constants.PUB_ENV.equals(HeraGlobalEnvironment.getEnv())) {
                ScheduleLog.info("任务无监控人，发送给owner：{}", heraJob.getId());
                HeraUser user = heraUserService.findByName(heraJob.getOwner());
                address.append(user.getEmail().trim());

                try {
                    if (runCount - 1 == rollCount) {
                        WeChatUtil.sendWeChatMessage(user, text, desp);
                    }
                } catch (Exception e) {

                }


            } else if (monitor != null) {
                String ids = monitor.getUserIds();
                String[] id = ids.split(Constants.COMMA);
                for (String anId : id) {
                    if (StringUtils.isBlank(anId)) {
                        continue;
                    }
                    HeraUser user = heraUserService.findById(Integer.parseInt(anId));
                    if (user != null && user.getEmail() != null) {
                        address.append(user.getEmail()).append(Constants.SEMICOLON);
                    }
                    try {
                        if (runCount - 1 == rollCount && user != null && StringUtils.isNotBlank(user.getScKey())) {
                            WeChatUtil.sendWeChatMessage(user, text, desp);
                        }
                    } catch (Exception e) {

                    }
                }
            }
            emailService.sendEmail(heraEnvForTest.getEnvFlag() + "hera任务(id=" + heraJob.getId() + ")失败-" + heraJob.getDescription(),
                    "任务Id        :" + heraJob.getId()
                            + "<br/>Job版本      :" + actionId
                            + "<br/>任务名称     :" + heraJob.getName()
                            + "<br/>相关描述     :" + heraJob.getDescription(),
                    // + "<br/>重复执行:" + heraJob.getRepeatRun()
                    //+",<br/>脚本信息:"+heraJob.getScript(),
                    //+ "<br/><br/>________详细信息________<br/>" + heraJob.toString1()
                    address.toString());
        } catch (MessagingException e) {
            e.printStackTrace();
            ErrorLog.error("发送邮件失败");
        }
    }

    @Override
    public void successAlarm(String actionId, int n) {
        Integer jobId = ActionUtil.getJobId(actionId);

        if (jobId == null) {
            return;
        }
        HeraJob heraJob = heraJobService.findById(jobId);

        //System.out.println(JSONObject.toJSONString(heraJob));
        //非开启任务不处理  最好能把这些抽取出去 提供接口实现
        if (heraJob.getAuto() != 1 && !Constants.PUB_ENV.equals(HeraGlobalEnvironment.getEnv())) {
            return;
        }
        StringBuilder address = new StringBuilder();
        try {
            String text = heraEnvForTest.getEnvFlag() + "hera调度" + heraJob.getId() + "号任务重试成功-" + heraJob.getDescription();
            String desp = "任务Id :" + heraJob.getId()
                    + "\n\n Job版本 : " + actionId
                    + "\n\n 任务名称 : " + heraJob.getName()
                    + "\n\n 相关描述 : " + heraJob.getDescription();

            HeraJobMonitor monitor = heraJobMonitorService.findByJobId(heraJob.getId());
            if (monitor == null && Constants.PUB_ENV.equals(HeraGlobalEnvironment.getEnv())) {
                ScheduleLog.info("任务无监控人，发送给owner：{}", heraJob.getId());
                HeraUser user = heraUserService.findByName(heraJob.getOwner());
                address.append(user.getEmail().trim());
                //WeiChatUtil.sendWeiChatMessage(user, text, desp);
            } else if (monitor != null) {
                String ids = monitor.getUserIds();
                String[] id = ids.split(Constants.COMMA);
                for (String anId : id) {
                    if (StringUtils.isBlank(anId)) {
                        continue;
                    }
                    HeraUser user = heraUserService.findById(Integer.parseInt(anId));
                    if (user != null && user.getEmail() != null) {
                        address.append(user.getEmail()).append(Constants.SEMICOLON);
                        //WeiChatUtil.sendWeiChatMessage(user, text, desp);
                    }
                }
            }
            emailService.sendEmail(heraEnvForTest.getEnvFlag() + "hera任务(id=" + heraJob.getId() + ")重试成功-" + heraJob.getDescription(),
                    "任务Id : " + heraJob.getId() + ",在第" + n + "次重试后，执行成功"
                            + "<br/>具体信息："
                            + "<br/>&emsp;&emsp;Job版本  :  " + actionId
                            + "<br/>&emsp;&emsp;任务名称  :  " + heraJob.getName()
                            + "<br/>&emsp;&emsp;相关描述  :  " + heraJob.getDescription(),
                    address.toString());
        } catch (MessagingException e) {
            e.printStackTrace();
            ErrorLog.error("发送邮件失败");
        }
    }
}
