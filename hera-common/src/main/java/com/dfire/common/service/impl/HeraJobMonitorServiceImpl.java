package com.dfire.common.service.impl;

import com.dfire.common.entity.HeraJobMonitor;
import com.dfire.common.mapper.HeraJobMonitorMapper;
import com.dfire.common.service.HeraJobMonitorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @author xiaosuda
 * @date 2018/8/1
 */
@Service
public class HeraJobMonitorServiceImpl implements HeraJobMonitorService {

    @Autowired
    private HeraJobMonitorMapper heraJobMonitorMapper;

    @Override
    public boolean addMonitor(String userId, Integer jobId) {
        HeraJobMonitor res = heraJobMonitorMapper.findByJobId(jobId);
        HeraJobMonitor monitor = new HeraJobMonitor();
        monitor.setUserIds(userId.endsWith(",") ? userId : userId + ",");
        monitor.setJobId(jobId);
        //插入
        if (res == null) {
            Integer insert = heraJobMonitorMapper.insert(monitor);
            return insert != null && insert > 0;
        } else { //更新
            Integer update = heraJobMonitorMapper.insertUser(monitor);
            return update != null && update > 0;
        }
    }

    @Override
    public boolean removeMonitor(String userId, Integer jobId) {
        HeraJobMonitor monitor = new HeraJobMonitor();

        HeraJobMonitor jobMonitor = heraJobMonitorMapper.findByJobId(jobId);
        String userIds = jobMonitor.getUserIds();
        ArrayList<String> list = new ArrayList<>();
        if (userIds != null && !userIds.isEmpty()) {
            for (String id : userIds.split(",")) {
                if (!id.isEmpty()) list.add(id);
            }
        }
        //删除关注者
        list.remove(userId);
        String result = "";
        if (!list.isEmpty()) {
            result = String.join(",", list) + ",";
        }

        monitor.setUserIds(result);
        monitor.setJobId(jobId);

        Integer res = heraJobMonitorMapper.deleteMonitor(monitor);
        return res != null && res > 0;
    }

    @Override
    public HeraJobMonitor findByJobId(Integer jobId) {
        return heraJobMonitorMapper.findByJobId(jobId);
    }


    //自己添加
    @Override
    public boolean addMonitors(String userIds, Integer jobId) {
        HeraJobMonitor res = heraJobMonitorMapper.findByJobId(jobId);
        HeraJobMonitor monitor = new HeraJobMonitor();
        monitor.setUserIds(userIds.endsWith(",") ? userIds : userIds + ",");
        monitor.setJobId(jobId);
        //插入
        if (res == null) {
            Integer insert = heraJobMonitorMapper.insert(monitor);
            return insert != null && insert > 0;
        } else { //更新
            Integer update = heraJobMonitorMapper.insertUsers(userIds, jobId);
            return update != null && update > 0;
        }
    }

    @Override
    public boolean removeAllMonitor(Integer jobId) {
        return heraJobMonitorMapper.removeAllMonitor(jobId);
    }

    @Override
    public Integer removeMonitorByUserId(Integer userId) {
        HeraJobMonitor monitor = new HeraJobMonitor();

        List<HeraJobMonitor> jobMonitors = heraJobMonitorMapper.findByUserId(userId);
        int count = 0;
        for (HeraJobMonitor jobMonitor : jobMonitors) {
            String userIds = jobMonitor.getUserIds();
            Integer jobId = jobMonitor.getJobId();
            ArrayList<String> list = new ArrayList<>();
            if (userIds != null && !userIds.isEmpty()) {
                for (String id : userIds.split(",")) {
                    if (!id.isEmpty()) list.add(id);
                }
            }
            //删除关注者
            list.remove(userId.toString());
            String result = "";
            if (!list.isEmpty()) {
                result = String.join(",", list) + ",";
            }

            monitor.setUserIds(result);
            monitor.setJobId(jobId);
            Integer res = heraJobMonitorMapper.deleteMonitor(monitor);
            if (res != null && res > 0) {
                System.out.println(monitor.getJobId() + ",取关成功!");
                count++;
            }
        }
        return count;
    }


}
