package com.dfire.util;

import com.dfire.bean.NginxStatus;
import com.dfire.common.util.HttpRequest;

import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName NginxStatusUntil
 * @Description TODO
 * @Author lenovo
 * @Date 2019/12/25 14:13
 **/
public class NginxStatusUntil {
    public static List<NginxStatus> getNginxStatus(String hosts) {
        ArrayList<NginxStatus> nginxStatuses = new ArrayList<>();
        String[] hostArray = hosts.split(",");
        for (String s : hostArray) {
            String nginxUrl = String.format("http://%s/nginxstatus", s.trim());
            String line = HttpRequest.sendGet(nginxUrl);
            String regEx = "\\D+";
            String[] split = line.split(regEx);
            NginxStatus nginxStatus = new NginxStatus(split[1].trim(), split[5].trim(), split[6].trim(), split[7].trim());
            nginxStatuses.add(nginxStatus);
        }
        return nginxStatuses;
    }
}
