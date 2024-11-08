package com.dfire.core.netty.worker.request;

import com.dfire.core.event.LogHelpUtil;
import com.dfire.core.exception.RemotingException;
import com.dfire.core.netty.HeraChannel;
import com.dfire.core.tool.OsProcessJob;
import com.dfire.common.logs.ErrorLog;
import com.dfire.protocol.RpcOperate;
import com.dfire.protocol.RpcRequest.Request;
import com.dfire.protocol.RpcSocketMessage.SocketMessage;
import com.dfire.protocol.RpcWorkInfo.WorkInfo;

/**
 * @author xiaosuda
 * @date 2018/11/20
 */
public class WorkHandlerRequest {

    public void getWorkInfo(HeraChannel channel) {
        OsProcessJob processJob = new OsProcessJob();
        int exitCode = processJob.run();
        if (exitCode == 0) {
            try {
                channel.writeAndFlush(
                        SocketMessage.newBuilder()
                                .setKind(SocketMessage.Kind.REQUEST)
                                .setBody(Request.newBuilder()
                                        .setBody(WorkInfo.newBuilder()
                                                .setOSInfo(processJob.getOsInfo())
                                                .addAllProcessMonitor(processJob.getProcessMonitors())
                                                .build().toByteString())
                                        .setOperate(RpcOperate.Operate.SetWorkInfo)
                                        .build()
                                        .toByteString())
                                .build());
            } catch (RemotingException e) {
                ErrorLog.error(LogHelpUtil.getSpecificTrace(e));
                e.printStackTrace();
            }
        }

    }
}
