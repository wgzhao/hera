package com.dfire.common.entity;


import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/**
 * @ClassName HeraNginxInfo
 * @Description TODO
 * @Author lenovo
 * @Date 2020/2/24 14:25
 **/
public class HeraNginxInfo {

   /* id
            active_connections
    reading
            writing
    waiting
            create_time*/

    private Integer activeConnections;
    private Integer reading;
    private Integer writing;
    private Integer waiting;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    public Integer getActiveConnections() {
        return activeConnections;
    }

    public void setActiveConnections(Integer activeConnections) {
        this.activeConnections = activeConnections;
    }

    public Integer getReading() {
        return reading;
    }

    public void setReading(Integer reading) {
        this.reading = reading;
    }

    public Integer getWriting() {
        return writing;
    }

    public void setWriting(Integer writing) {
        this.writing = writing;
    }

    public Integer getWaiting() {
        return waiting;
    }

    public void setWaiting(Integer waiting) {
        this.waiting = waiting;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }
}
