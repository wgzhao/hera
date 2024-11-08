package com.dfire.monitor.domain;

import lombok.Data;


/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 下午3:42 2018/8/15
 * @desc
 */
@Data
public class JobHistoryVo {

    private String runTime;

    private String jobId;

    private String actionId;

    private String startTime;

    private String endTime;

    private String executeHost;

    private String status;

    private String operator;

    private String description;

    private String dependencies;

    private String type;

    private String jobName;

    private Integer times;

    @Override
    public String toString() {
        return "JobHistoryVo{" +
                "runTime='" + runTime + '\'' +
                ", jobId='" + jobId + '\'' +
                ", actionId='" + actionId + '\'' +
                ", startTime='" + startTime + '\'' +
                ", endTime='" + endTime + '\'' +
                ", executeHost='" + executeHost + '\'' +
                ", status='" + status + '\'' +
                ", operator='" + operator + '\'' +
                ", description='" + description + '\'' +
                ", dependencies='" + dependencies + '\'' +
                ", type='" + type + '\'' +
                ", jobName='" + jobName + '\'' +
                ", times=" + times +
                '}';
    }
}
