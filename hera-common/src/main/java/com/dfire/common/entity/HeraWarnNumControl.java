package com.dfire.common.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * @author lenovo
 * @description
 * @date 2020/8/3 14:28
 */

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HeraWarnNumControl {
//    id	bigint
//    job_id	bigint
//    warn_type	tinyint
//    send_time	timestamp
//    insert_time	timestamp

    private Long jobId;
    private Integer warnType;
    private Date sendTime;
    private Date insertTime;

    public static void main(String[] args) {
    }

}
