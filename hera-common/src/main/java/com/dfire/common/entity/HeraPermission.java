package com.dfire.common.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 17:34 2018/1/11
 * @desc
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HeraPermission {


    private int id;

    private String type;

    private Long targetId;

    private String uid;

    private Date gmtCreate;

    private Date gmtModified;

    /*@Override
    public String toString() {
        return "HeraPermission{" +
                "id=" + id +
                ", type='" + type + '\'' +
                ", targetId=" + targetId +
                ", uid='" + uid + '\'' +
                ", gmtCreate=" + gmtCreate +
                ", gmtModified=" + gmtModified +
                '}';
    }*/
}
