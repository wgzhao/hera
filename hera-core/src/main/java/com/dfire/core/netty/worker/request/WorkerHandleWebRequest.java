package com.dfire.core.netty.worker.request;

import com.dfire.common.config.HeraGlobalEnvironment;
import com.dfire.core.event.LogHelpUtil;
import com.dfire.core.exception.RemotingException;
import com.dfire.core.netty.listener.WorkResponseListener;
import com.dfire.core.netty.util.AtomicIncrease;
import com.dfire.core.netty.worker.WorkContext;
import com.dfire.common.logs.ErrorLog;
import com.dfire.common.logs.SocketLog;
import com.dfire.protocol.JobExecuteKind.ExecuteKind;
import com.dfire.protocol.RpcSocketMessage.SocketMessage;
import com.dfire.protocol.RpcWebOperate.WebOperate;
import com.dfire.protocol.RpcWebRequest.WebRequest;
import com.dfire.protocol.RpcWebResponse.WebResponse;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * @author xiaosuda
 * @date 2018/11/9
 */
public class WorkerHandleWebRequest {

    public static Future<WebResponse> handleWebExecute(final WorkContext workContext, ExecuteKind kind, String id) {
        return buildMessage(WebRequest.newBuilder()
                .setRid(AtomicIncrease.getAndIncrement())
                .setOperate(WebOperate.ExecuteJob)
                .setEk(kind)
                .setId(id)
                .build(), workContext, "[执行]-任务超出3小时未得到master消息返回:" + id);
    }

    public static Future<WebResponse> handleWebAction(final WorkContext workContext, ExecuteKind kind, String id) {

        //  System.err.println(workContext + "__\n" + kind + "__\n" + id);

        return buildMessage(WebRequest.newBuilder()
                .setRid(AtomicIncrease.getAndIncrement())
                .setOperate(WebOperate.GenerateAction)
                .setEk(kind)
                .setId(id)
                .build(), workContext, "[更新]-action超出3小时未得到master消息返回:" + id);
    }

    public static Future<WebResponse> handleCancel(final WorkContext workContext, ExecuteKind kind, String id) {
        return buildMessage(WebRequest.newBuilder()
                .setRid(AtomicIncrease.getAndIncrement())
                .setOperate(WebOperate.CancelJob)
                .setEk(kind)
                .setId(id)
                .build(), workContext, "[取消]-任务超出3小时未得到master消息返回：" + id);
    }

    public static Future<WebResponse> handleCancelForTime(final WorkContext workContext, ExecuteKind kind, String id) {
        return buildMessage(WebRequest.newBuilder()
                .setRid(AtomicIncrease.getAndIncrement())
                //.setOperate(WebOperate.CancelForTime)
                .setEk(kind)
                .setId(id)
                .build(), workContext, "[取消]-任务超出3小时未得到master消息返回：" + id);
    }

    public static Future<WebResponse> handleUpdate(final WorkContext workContext, String jobId) {
        return buildMessage(WebRequest.newBuilder()
                .setRid(AtomicIncrease.getAndIncrement())
                .setOperate(WebOperate.UpdateJob)
                .setEk(ExecuteKind.ManualKind)
                .setId(jobId)
                .build(), workContext, "[更新]-job超出3小时未得到master消息返回：" + jobId);
    }

    public static Future<WebResponse> getJobQueueInfoFromMaster(WorkContext workContext) {
        return buildMessage(WebRequest.newBuilder()
                .setRid(AtomicIncrease.getAndIncrement())
                .setOperate(WebOperate.GetAllHeartBeatInfo)
                .build(), workContext, "三个小时未获得master任务队列的获取信息");
    }


    public static Future<WebResponse> getAllWorkInfoFromMaster(WorkContext workContext) {
        return buildMessage(WebRequest.newBuilder()
                .setRid(AtomicIncrease.getAndIncrement())
                .setOperate(WebOperate.GetAllWorkInfo)
                .build(), workContext, "三个小时未获得master所有work信息");
    }

    private static Future<WebResponse> buildMessage(WebRequest request, WorkContext workContext, String errorMsg) {
        CountDownLatch latch = new CountDownLatch(1);

//        System.err.println("request :" + JSONObject.toJSONString(request));
//        System.err.println("latch " + latch.toString());
//        System.err.println("errorMsg " + errorMsg);
//        WorkResponseListener responseListener = new WorkResponseListener(request, false, latch, null);
//        System.err.println("responseListener " + JSONObject.toJSONString(responseListener));


        WorkResponseListener responseListener = new WorkResponseListener(request, false, latch, null);

        workContext.getHandler().addListener(responseListener);
        Future<WebResponse> future = workContext.getWorkWebThreadPool().submit(() -> {
            latch.await(HeraGlobalEnvironment.getRequestTimeout(), TimeUnit.SECONDS);
            if (!responseListener.getReceiveResult()) {
                ErrorLog.error(errorMsg);
            }
            workContext.getHandler().removeListener(responseListener);
            return responseListener.getWebResponse();
        });
        try {
            workContext.getServerChannel().writeAndFlush(SocketMessage.newBuilder()
                    .setKind(SocketMessage.Kind.WEB_REQUEST)
                    .setBody(request.toByteString())
                    .build());
            SocketLog.info("1.WorkerHandleWebRequest: send web request to master requestId ={}", request.getRid());
        } catch (RemotingException e) {
            ErrorLog.error(LogHelpUtil.getSpecificTrace(e));
            e.printStackTrace();
            workContext.getHandler().removeListener(responseListener);
            ErrorLog.error("1.WorkerHandleWebRequest: send web request to master exception requestId ={}", request.getRid());
        }
        return future;
    }
}
