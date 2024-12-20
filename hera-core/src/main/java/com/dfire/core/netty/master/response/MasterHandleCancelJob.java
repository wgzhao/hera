package com.dfire.core.netty.master.response;

import com.dfire.common.config.HeraGlobalEnvironment;
import com.dfire.core.event.LogHelpUtil;
import com.dfire.core.exception.RemotingException;
import com.dfire.core.netty.HeraChannel;
import com.dfire.core.netty.listener.MasterResponseListener;
import com.dfire.core.netty.master.MasterContext;
import com.dfire.core.netty.util.AtomicIncrease;
import com.dfire.common.logs.ErrorLog;
import com.dfire.common.logs.SocketLog;
import com.dfire.protocol.*;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 下午3:42 2018/5/11
 * @desc master接收到worker端取消任务执行请求时，处理逻辑
 */
public class MasterHandleCancelJob {

    public Future<RpcResponse.Response> cancel(final MasterContext context, HeraChannel channel, JobExecuteKind.ExecuteKind kind, String jobId) {
        RpcCancelMessage.CancelMessage cancelMessage = RpcCancelMessage.CancelMessage.newBuilder()
                .setEk(kind)
                .setId(jobId)
                .build();
        final RpcRequest.Request request = RpcRequest.Request.newBuilder()
                .setRid(AtomicIncrease.getAndIncrement())
                .setOperate(RpcOperate.Operate.Cancel)
                .setBody(cancelMessage.toByteString())
                .build();
        RpcSocketMessage.SocketMessage socketMessage = RpcSocketMessage.SocketMessage.newBuilder()
                .setKind(RpcSocketMessage.SocketMessage.Kind.REQUEST)
                .setBody(request.toByteString())
                .build();
        final CountDownLatch latch = new CountDownLatch(1);
        MasterResponseListener responseListener = new MasterResponseListener(request, false, latch, null);
        context.getHandler().addListener(responseListener);
        Future<RpcResponse.Response> future = context.getThreadPool().submit(() -> {
            latch.await(HeraGlobalEnvironment.getRequestTimeout(), TimeUnit.SECONDS);
            if (!responseListener.getReceiveResult()) {
                ErrorLog.error("取消任务信号消失，三小时未收到work返回：{}", jobId);
            }
            context.getHandler().removeListener(responseListener);
            return responseListener.getResponse();
        });
        try {
            SocketLog.info("send cancel job success {}", request.getRid());
            channel.writeAndFlush(socketMessage);
        } catch (RemotingException e) {
            ErrorLog.error(LogHelpUtil.getSpecificTrace(e));
            e.printStackTrace();
            ErrorLog.error("send cancel job exception {}", request.getRid());
        }
        return future;
    }
}
