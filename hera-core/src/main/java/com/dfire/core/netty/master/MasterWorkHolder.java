package com.dfire.core.netty.master;

import com.dfire.core.message.HeartBeatInfo;
import com.dfire.core.netty.HeraChannel;
import com.dfire.protocol.RpcWorkInfo.WorkInfo;
import lombok.Data;

import java.util.HashSet;
import java.util.Set;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 16:13 2018/1/12
 * @desc
 */

@Data
public class MasterWorkHolder {

    private HeraChannel channel;

    /**
     * 存放的jobId
     */
    private Set<Integer> running = new HashSet<>();
    /**
     * 存放的jobId
     */
    private Set<Integer> manningRunning = new HashSet<>();
    /**
     * 存放的debugId
     */
    private Set<Integer> debugRunning = new HashSet<>();


    /**
     * 存放的ReRunId
     */
    private Set<Integer> reRunRunning = new HashSet<>();

    private HeartBeatInfo heartBeatInfo;

    private volatile WorkInfo workInfo;

    public MasterWorkHolder(HeraChannel channel) {
        this.channel = channel;
    }

}
