package com.dfire.core.netty.master;

import com.dfire.core.event.LogHelpUtil;
import com.dfire.common.logs.ErrorLog;
import com.dfire.common.logs.HeraLog;
import com.dfire.protocol.RpcSocketMessage;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;
import io.netty.handler.timeout.IdleStateHandler;

import java.util.concurrent.TimeUnit;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 10:34 2018/1/10
 * @desc
 */
public class MasterServer {

    private final ServerBootstrap serverBootstrap;
    private final EventLoopGroup bossGroup;
    private final EventLoopGroup workGroup;

    /**
     * ProtobufVarint32LengthFieldPrepender:对protobuf协议的的消息头上加上一个长度为32的整形字段,用于标志这个消息的长度。
     * ProtobufVarint32FrameDecoder:针对protobuf协议的ProtobufVarint32LengthFieldPrepender()所加的长度属性的解码器
     *
     * @param handler
     */
    public MasterServer(final ChannelHandler handler) {
        serverBootstrap = new ServerBootstrap();
        //服务端接受客户端的连接， Reactor线程组
        bossGroup = new NioEventLoopGroup(1);
        //SocketChannel的网络读写
        workGroup = new NioEventLoopGroup(1);

        //一个Netty服务端启动时，通常会有两个NioEventLoopGroup：
        // bossGroup  监听线程组，主要是监听客户端请求，
        // workGroup  工作线程组，主要是处理与客户端的数据通讯。
        serverBootstrap.group(bossGroup, workGroup)
                .channel(NioServerSocketChannel.class)
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline().addLast("frameDecoder", new ProtobufVarint32FrameDecoder())
                                .addLast("decoder", new ProtobufDecoder(RpcSocketMessage.SocketMessage.getDefaultInstance()))
                                .addLast("frameEncoder", new ProtobufVarint32LengthFieldPrepender())
                                .addLast("encoder", new ProtobufEncoder())
                                //IdleStateHandler心跳机制主要是用来检测远端是否存活，
                                //如果不存活或活跃则对空闲Socket连接进行处理避免资源的浪费。
                                .addLast(new IdleStateHandler(0, 0, 10, TimeUnit.SECONDS))
                                .addLast("handler", handler);
                    }
                });
    }

    public synchronized boolean start(int port) {
        ChannelFuture channelFuture = null;
        try {
            channelFuture = serverBootstrap.bind(port).sync();
        } catch (InterruptedException e) {
            ErrorLog.error(LogHelpUtil.getSpecificTrace(e));
            e.printStackTrace();
        }
        if (channelFuture != null && channelFuture.isSuccess()) {
            HeraLog.info("start master server success");
        } else {
            ErrorLog.error("start master server success");
        }
        return true;
    }

    public synchronized boolean shutdown() {
        bossGroup.shutdownGracefully();
        workGroup.shutdownGracefully();
        HeraLog.info("stop master server gracefully");
        return true;
    }


}
