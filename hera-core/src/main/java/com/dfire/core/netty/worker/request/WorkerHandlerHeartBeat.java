package com.dfire.core.netty.worker.request;

import com.dfire.core.event.LogHelpUtil;
import com.dfire.core.exception.RemotingException;
import com.dfire.core.netty.util.AtomicIncrease;
import com.dfire.core.netty.worker.WorkContext;
import com.dfire.core.tool.CpuLoadPerCoreJob;
import com.dfire.core.tool.MemUseRateJob;
import com.dfire.common.logs.ErrorLog;
import com.dfire.protocol.RpcHeartBeatMessage;
import com.dfire.protocol.RpcOperate;
import com.dfire.protocol.RpcRequest;
import com.dfire.protocol.RpcSocketMessage;

/**
 * @author xiaosuda
 * @date 2018/4/12
 */
public class WorkerHandlerHeartBeat {


    ///主要send方法
    public boolean send(WorkContext context) {
        try {
            ///获取内存信息
            MemUseRateJob memUseRateJob = new MemUseRateJob(1);
            memUseRateJob.readMemUsed();
            ///获取负载信息
            CpuLoadPerCoreJob loadPerCoreJob = new CpuLoadPerCoreJob();
            loadPerCoreJob.run();
            ///构建成数据包
            RpcHeartBeatMessage.HeartBeatMessage hbm = RpcHeartBeatMessage.HeartBeatMessage.newBuilder()
                    .setHost(WorkContext.host)
                    .setMemTotal(memUseRateJob.getMemTotal())
                    .setMemRate(memUseRateJob.getRate())
                    .setCpuLoadPerCore(loadPerCoreJob.getLoadPerCore())
                    .setTimestamp(System.currentTimeMillis())
                    .addAllDebugRunnings(context.getDebugRunning().keySet())
                    .addAllManualRunnings(context.getManualRunning().keySet())
                    .addAllRunnings(context.getRunning().keySet())
                    .setCores(WorkContext.cpuCoreNum)
                    .build();
            ///发送数据包
            context.getServerChannel().writeAndFlush(RpcSocketMessage.SocketMessage.newBuilder().
                    setKind(RpcSocketMessage.SocketMessage.Kind.REQUEST).
                    setBody(RpcRequest.Request.newBuilder().
                            setRid(AtomicIncrease.getAndIncrement()).
                            setOperate(RpcOperate.Operate.HeartBeat).
                            setBody(hbm.toByteString()).
                            build().toByteString()).
                    build());
        } catch (RemotingException e) {
            ErrorLog.error(LogHelpUtil.getSpecificTrace(e));
            e.printStackTrace();
            return false;
        }
        return true;
    }

}
