package com.dfire.core.queue;

import lombok.Builder;
import lombok.Data;


/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 10:54 2018/1/4
 * @desc 任务队列中job的实体
 */
@Data
@Builder
public class JobElement {

    /**
     * 版本号id
     */
    private String jobId;

    private int hostGroupId;

    private Integer priorityLevel;
    /**
     * 内存中的创建时间
     */
    private Long gmtCreated;
    /**
     * 内存中的修改时间
     */
    private Long gmtModified;

    private Long triggerTime;


    public boolean equals(JobElement jobElement) {
        return jobElement.getJobId().equals(jobId);
    }

    @Override
    public String toString() {
        return "JobElement{" +
                "jobId='" + jobId + '\'' +
                ", hostGroupId=" + hostGroupId +
                '}';
    }
}
