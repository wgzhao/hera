package com.dfire.core.route.loadbalance.impl;

import com.dfire.common.entity.vo.HeraHostGroupVo;
import com.dfire.core.netty.master.MasterContext;
import com.dfire.core.netty.master.MasterWorkHolder;
import com.dfire.core.route.loadbalance.AbstractLoadBalance;
import com.dfire.common.logs.ScheduleLog;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 随机算法
 *
 * @author xiaosdua
 */
public class RandomLoadBalance extends AbstractLoadBalance {

    public static final String NAME = "random";

    @Override
    protected MasterWorkHolder doSelect(HeraHostGroupVo hostGroup, MasterContext masterContext) {
        List<String> hosts = hostGroup.getHosts();
        int size = hosts.size();
        Set<Integer> checkedIndices = new HashSet<>(size);
        for (int i = 0; i < size; i++) {
            int selectedIndex;
            do {
                selectedIndex = ThreadLocalRandom.current().nextInt(size);
            } while (checkedIndices.contains(selectedIndex));
            checkedIndices.add(selectedIndex);

            String selectedHost = hosts.get(selectedIndex).trim();
            for (MasterWorkHolder workHolder : masterContext.getWorkMap().values()) {
                if (workHolder.getHeartBeatInfo().getHost().equals(selectedHost) && check(workHolder)) {
                    ScheduleLog.warn("Selected work holder: {}", workHolder.getChannel().getRemoteAddress());
                    return workHolder;
                }
            }
        }
        return null;
    }
}
