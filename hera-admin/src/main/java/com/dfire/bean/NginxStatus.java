package com.dfire.bean;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @ClassName NginxStatus
 * @Description TODO
 * @Author lenovo
 * @Date 2019/12/25 14:27
 **/
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NginxStatus {
    private String activeConnections;
    private String reading;
    private String writing;
    private String waiting;
}
