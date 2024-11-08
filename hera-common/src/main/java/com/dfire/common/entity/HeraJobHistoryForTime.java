package com.dfire.common.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 17:31 2018/1/11
 * @desc
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class HeraJobHistoryForTime {
    private String properties;
    private String runTime;  // run_time
    private String id;  // id,
    private String actionId; // action_id,
    private Long jobId; // job_id,
    private Date startTime; // start_time,
    private Date endTime;// end_time,
    private String executeHost;// execute_host,
    private String operator; // operator,
    private String status;// status,
    private Integer triggerType;// trigger_type,
    private String illustrate; // illustrate,
    private Date statisticEndTime;
    private String log;
    private String timezone;
    private String cycle;
    private int hostGroupId;// host_group_id
    private String scKey;
    private String email;
    private String description;
    private String owner;
}
