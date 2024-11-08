package com.dfire.task;

import com.dfire.bean.ClusterMetrics;
import com.dfire.bean.NginxStatus;
import com.dfire.common.constants.Constants;
import com.dfire.common.entity.*;
import com.dfire.common.service.*;
import com.dfire.common.util.HeraDateTool;
import com.dfire.common.util.WeChatUtil;
import com.dfire.common.config.HeraEnvForTest;
import com.dfire.core.util.NetUtils;
import com.dfire.common.logs.DebugLog;
import com.dfire.util.HtmlUnitCommon;
import com.dfire.common.util.HttpRequest;
import com.dfire.util.NginxStatusUntil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Component
@Configuration // 1.主要用于标记配置类，兼备Component的效果。
@EnableScheduling // 2.开启定时任务
public class StaticScheduleTask {
  private static final String CANCEL_URL = "http://localhost:8090/hera/scheduleCenter/cancelJobForOvertime";
  private static final String RECOVERY_URL = "http://localhost:8090/hera/scheduleCenter/manualForOvertime";
  private static String localIp = null;

  static {
    try {
      localIp = NetUtils.getLocalAddress();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Autowired
  HeraYarnInfoUseService heraYarnInfoUseService;

  @Autowired
  HeraNginxInfoService heraNginxInfoService;

  @Autowired
  HeraJobHistoryService heraJobHistoryService;

  @Autowired
  EmailService emailService;

  @Autowired
  HeraWarnNumControlService heraWarnNumControlService;
  @Autowired
  HeraEnvForTest heraEnvForTest;
  @Autowired
  private HeraJobMonitorService heraJobMonitorService;
  @Autowired
  private HeraUserService heraUserService;
  @Value("${real_server.hosts}")
  private String hosts;

  // 3.添加定时任务
  @Scheduled(cron = "0 0/1 * * * ?")
  // 或直接指定时间间隔，例如：
  // @Scheduled(fixedRate=5000)
  private void configureTasks() {
    if (null != localIp && getIP().equals(localIp)) {
      SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
      String format = sdf.format(new Date());
      ClusterMetrics clusterMetrics = HtmlUnitCommon.selectClusterMetrics();
      String allocatedMB = clusterMetrics.getAllocatedMB();
      String totalMB = clusterMetrics.getTotalMB();
      double mb = Double.parseDouble(allocatedMB) / Double.parseDouble(totalMB);
      String mbStr = String.format("%.2f", mb);
      String allocatedVirtualCores = clusterMetrics.getAllocatedVirtualCores();
      String totalVirtualCores = clusterMetrics.getTotalVirtualCores();
      double vc = Double.parseDouble(allocatedVirtualCores) / Double.parseDouble(totalVirtualCores);
      String vcStr = String.format("%.2f", vc);
      HeraYarnInfoUse heraYarnInfoUse = new HeraYarnInfoUse();
      heraYarnInfoUse.setCpuUse(vcStr);
      heraYarnInfoUse.setMemUse(mbStr);
      heraYarnInfoUse.setTimePoint(format);
      heraYarnInfoUseService.insertHeraYarnInfoUse(heraYarnInfoUse);
    }
  }

  @Scheduled(cron = "0 0/1 * * * ?")
  private void nginxInfo() {
    if (null != localIp && getIP().equals(localIp)) {
      try {
        List<NginxStatus> nginxStatus = NginxStatusUntil.getNginxStatus(hosts);
        HeraNginxInfo heraNginxInfo = new HeraNginxInfo();
        int activeConnections = nginxStatus.stream().mapToInt(status -> Integer.parseInt(status.getActiveConnections()))
            .sum();
        int reading = nginxStatus.stream().mapToInt(status -> Integer.parseInt(status.getReading())).sum();
        int writing = nginxStatus.stream().mapToInt(status -> Integer.parseInt(status.getWriting())).sum();
        int waiting = nginxStatus.stream().mapToInt(status -> Integer.parseInt(status.getWaiting())).sum();
        heraNginxInfo.setActiveConnections(activeConnections);
        heraNginxInfo.setReading(reading);
        heraNginxInfo.setWriting(writing);
        heraNginxInfo.setWaiting(waiting);
        heraNginxInfoService.insertHeraNginxInfo(heraNginxInfo);
      } catch (NumberFormatException e) {
        e.printStackTrace();
      }
    }
  }

  // @Scheduled(cron = "0 0/10 * * * ?")
  private void earlyWarningOverTimeJob() {
    try {
      if (null != localIp && getIP().equals(localIp)) {
        List<HeraJobHistoryForTime> runningList = heraJobHistoryService.findByStatus("running");

        for (HeraJobHistoryForTime run : runningList) {
          if (run.getTriggerType() != null &&
              (run.getTriggerType() == 1 || run.getTriggerType() == 3) &&
              run.getStartTime() != null) {
            Date startTime = run.getStartTime();
            long currentRunTime = new HeraDateTool().getTime() - new HeraDateTool(startTime).getTime();
            Long jobId = run.getJobId();
            List<HeraJobHistory> successList = heraJobHistoryService.findByStatusJobId("success", jobId);
            // successList.size()==0 自动执行0次 超过20分钟出发预警
            String text = heraEnvForTest.getEnvFlag() + "hera调度" + run.getJobId() + "号任务超时-" + run.getDescription();
            HeraWarnNumControl build = HeraWarnNumControl.builder().jobId(run.getJobId()).warnType(1).build();
            HeraWarnNumControl one = heraWarnNumControlService.getOne(build);
            if (CollectionUtils.isEmpty(successList)) {
              if (currentRunTime > 60 * 60) { // 新任务运行时间> 60分算超时
                // 前置判断
                if (sendJudge(one)) {
                  DebugLog.info("currentRunTime:" + currentRunTime + "---------------开始发送任务超时预警");
                  // emailService.sendEmail(text, "", run.getEmail());
                  // WeiChatUtil.sendWeiChatMessage(run.getScKey(), text, "");
                  sendMessage(run, text, one);
                }
              }
            }
            if (CollectionUtils.isNotEmpty(successList)) {
              long allTime = 0;
              for (HeraJobHistory success : successList) {
                Date sucStartTime = success.getStartTime();
                Date sucEndTime = success.getEndTime();
                long runTime = new HeraDateTool(sucEndTime).getTime() - new HeraDateTool(sucStartTime).getTime();
                // 求和
                allTime += runTime;
              }

              long avgTime = allTime / successList.size();

              // 平均时长小于60秒
              if (sendJudge(one)) {
                if (avgTime <= 60 && currentRunTime > 60 * 10) { // 平均时间<=1分钟,运行时间>10分钟算超时
                  DebugLog
                      .info("avgTime:" + avgTime + "|currentRunTime:" + currentRunTime + "---------------开始发送任务超时预警");
                  // emailService.sendEmail(text, "", run.getEmail());
                  // WeiChatUtil.sendWeiChatMessage(run.getScKey(), text, "");
                  // 发送预警消息
                  sendMessage(run, text, one);
                  // kill和恢复任务
                  cancelAndRecovery(run.getId(), run.getJobId() + "", run.getActionId());
                } else if (avgTime > 60 && avgTime <= 60 * 10 && currentRunTime > 60 * 60) { // 平均时间10分钟之内,运行时间>60分算超时
                  DebugLog
                      .info("avgTime:" + avgTime + "|currentRunTime:" + currentRunTime + "---------------开始发送任务超时预警");
                  // emailService.sendEmail(text, "", run.getEmail());
                  // WeiChatUtil.sendWeiChatMessage(run.getScKey(), text, "");
                  // 发送预警消息
                  sendMessage(run, text, one);
                  // kill和恢复任务
                  cancelAndRecovery(run.getId(), run.getJobId() + "", run.getActionId());
                } else if (avgTime > 60 * 10 && currentRunTime > avgTime * 5) { // 平均时间>10分钟,运行时间是平均时间5倍算超时
                  DebugLog
                      .info("avgTime:" + avgTime + "|currentRunTime:" + currentRunTime + "---------------开始发送任务超时预警");
                  // emailService.sendEmail(text, "", run.getEmail());
                  // WeiChatUtil.sendWeiChatMessage(run.getScKey(), text, "");
                  // 发送预警消息
                  sendMessage(run, text, one);
                  // kill和恢复任务
                  cancelAndRecovery(run.getId(), run.getJobId() + "", run.getActionId());
                } else if (currentRunTime > 60 * 60 * 5) { // 运行时间超过5小时算超时
                  // 发送预警消息
                  sendMessage(run, text, one);
                }
              }
            }
          }
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * @return ip
   */
  public String getIP() {
    return "127.0.0.1";
  }

  /**
   * 判断是否可以发送邮件
   *
   * @param one 参数详情
   * @return true/false
   */
  public boolean sendJudge(HeraWarnNumControl one) {
    if (one == null) {
      return true;
    }
    Date sendTime = one.getSendTime();
    Date now = new Date();
    long l = (now.getTime() - sendTime.getTime()) / (1000 * 60 * 60);
    // 一小时至多发送一次
    return l > 0;
  }

  /**
   * 发送邮件警报,关注人员都会发送邮件
   *
   * @param run  jobId
   * @param text 信息标题
   * @throws Exception 呃呃呃
   */
  public void sendMessage(HeraJobHistoryForTime run, String text, HeraWarnNumControl one)
      throws Exception {

    int job_id = Integer.parseInt(run.getJobId() + "");
    HeraJobMonitor monitor = heraJobMonitorService.findByJobId(job_id);

    String ids = monitor.getUserIds();
    String[] id = ids.split(Constants.COMMA);
    for (String anId : id) {
      if (StringUtils.isBlank(anId)) {
        continue;
      }
      HeraUser user = heraUserService.findById(Integer.parseInt(anId));
      emailService.sendEmail(text, run.getActionId(), user.getEmail());
      WeChatUtil.sendWeChatMessage(user.getScKey(), text, run.getActionId());
    }
    // 更新发送时间
    if (one == null) {
      one = HeraWarnNumControl.builder()
          .jobId(run.getJobId())
          .warnType(1)
          .sendTime(new Date())
          .insertTime(new Date())
          .build();
    } else {
      one.setSendTime(new Date());
    }
    heraWarnNumControlService.insert(one);
  }

  /**
   * @param historyId rowid
   * @param jobId     任务id
   * @param actionId  版本id
   */
  public void cancelAndRecovery(String historyId, String jobId, String actionId)
      throws Exception {
    // 先执行取消任务
    DebugLog.info("actionId=" + actionId + "-----------------开始执行超时任务删除");
    String result = HttpRequest.sendGet(CANCEL_URL, "historyId=" + historyId + "&jobId=" + jobId);

    // TODO: 2020/10/16 无论是否取消任务成功都执行手动恢复
    if (result.equals("取消任务成功")) {
      Thread.sleep(5000);
      DebugLog.info("actionId=" + actionId + "-----------------超时取消任务成功");
      // 执行手动恢复
      HttpRequest.sendGet(RECOVERY_URL, "actionId=" + actionId + "&triggerType=" + 2);
    } else {
      DebugLog.info("actionId=" + actionId + "-----------------超时取消任务(取消动作)超时----result=" + result);
      Thread.sleep(5000);
      // 执行手动恢复
      HttpRequest.sendGet(RECOVERY_URL, "actionId=" + actionId + "&triggerType=" + 2);
    }
  }
}
