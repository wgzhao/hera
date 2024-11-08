package com.dfire.common.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @ClassName HeraDataDiscovery
 * @Description TODO
 * @Author lenovo
 * @Date 2019/8/21 14:02
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
public class HeraDataDiscoveryNew {
    private String databaseName;
    private String tableName;
    private String tableComment;
    private String changeType;
    private String changeDetail;
    private Integer isMiddleBase;
    private Integer isHiveBase;
    private String hpStatDate;
    private String tableVar;
    private String order;
    private String field;
    private Integer page;
    private Integer limit;
    private String dt;
    private String queryData;
    private String startTime;
    private String endTime;


}
