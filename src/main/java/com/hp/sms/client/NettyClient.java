package com.hp.sms.client;

/**
 * Created by luj on 2015/11/9.
 */

import com.hp.sms.domain.SharedInfo;
import com.hp.sms.domain.SpInfo;
import com.hp.sms.utils.DataTool;
import com.hp.sms.utils.SocketRedis;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * CMPP客户端 负责连接CMPP服务端
 */
public class NettyClient {
    private SpInfo spInfo;
    private SharedInfo sharedInfo;
    private SocketRedis socketRedis;
    private DataTool dataTool;
    private Logger _logger;

    public NettyClient(SpInfo spInfo,SharedInfo sharedInfo,SocketRedis s,DataTool dt){
        this.spInfo=spInfo;
        this.sharedInfo=sharedInfo;
        this.socketRedis=s;
        this.dataTool=dt;
        this._logger = LoggerFactory.getLogger(NettyClient.class);
    }

    private ScheduledExecutorService executor = Executors
            .newScheduledThreadPool(1);
    EventLoopGroup group = new NioEventLoopGroup();

    public void connect(int port, String host) throws Exception {
        // 配置客户端NIO线程组
        try {
            Bootstrap b = new Bootstrap();
            b.group(group).channel(NioSocketChannel.class)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch)
                                throws Exception {
                            ch.pipeline().addLast(new LengthFieldBasedFrameDecoder(1024,0,4,-4,0));
                            ch.pipeline().addLast("LoginAuthHandler",
                                    new LoginAuthReqHandler(spInfo,sharedInfo,socketRedis,dataTool));
                            ch.pipeline().addLast("HeartBeatHandler",
                                    new HeartBeatReqHandler(spInfo,sharedInfo,socketRedis,dataTool));
                            ch.pipeline().addLast("OutputMessageHandler",
                                    new OutputMessagerHandler(spInfo,sharedInfo,socketRedis,dataTool));
                            ch.pipeline().addLast("InputMessageHandler",
                                    new InputMessageHandler(spInfo,sharedInfo,socketRedis,dataTool));

                        }
                    });
            // 发起异步连接操作
            ChannelFuture future = b.connect(
                    new InetSocketAddress(host, port)).sync();
            future.channel().closeFuture().sync();
        } finally {
            // 所有资源释放完成之后，清空资源，再次发起重连操作
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        TimeUnit.SECONDS.sleep(1);
                        try {
                            connect(port,host);// 发起重连操作
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }


}

