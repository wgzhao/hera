package com.dfire.common.entity.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/**
 * @author lenovo
 * @description
 * @date 2020/5/19 19:25
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class HeraMetaDataVo {
    /*uid	int
    rule_name	varchar
    rule_stat	varchar
    rule_sql	varchar
    biz_desc	varchar
    biz_aff	varchar
    status	int
    creator	varchar
    mender	varchar
    create_time	datetime
    update_time	datetime*/

    private Integer uid;
    private String ruleName;
    private String ruleStat;
    private String ruleSql;
    private String bizDesc;
    private String bizAff;
    private Integer status;
    private String creator;
    private String mender;
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;
    private String dt;
    private String queryData;
    private Integer page;
    private Integer limit;
    private String field;
    private String order;
    private String startTime;
    private String endTime;

}
