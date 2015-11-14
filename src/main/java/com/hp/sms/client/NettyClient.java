package com.hp.sms.client;

/**
 * Created by luj on 2015/11/9.
 */

import com.hp.sms.domain.SharedInfo;
import com.hp.sms.domain.SpInfo;
import com.hp.sms.utils.SmsDataTool;
import com.hp.sms.utils.SmsSocketRedis;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.timeout.ReadTimeoutHandler;
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
    private SmsSocketRedis smsSocketRedis;
    private SmsDataTool smsDataTool;
    private Logger _logger;

    public NettyClient(SpInfo spInfo,SharedInfo sharedInfo, SmsSocketRedis s, SmsDataTool dt){
        this.spInfo=spInfo;
        this.sharedInfo=sharedInfo;
        this.smsSocketRedis =s;
        this.smsDataTool =dt;
        this._logger = LoggerFactory.getLogger(NettyClient.class);
    }

    private ScheduledExecutorService executor = Executors
            .newScheduledThreadPool(1);
    EventLoopGroup group = new NioEventLoopGroup();

    public void connect(int port, String host) throws Exception {
        // 配置客户端NIO线程组
        _logger.info("try connect to server @"+host+":"+port);
        try {
            Bootstrap b = new Bootstrap();
            b.group(group).channel(NioSocketChannel.class)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch)
                                throws Exception {
                            ch.pipeline().addLast(new LengthFieldBasedFrameDecoder(1024,0,4,-4,0));
                            ch.pipeline().addLast("readTimeoutHandler",
                                    new ReadTimeoutHandler(30));//TimeOut=N*T
                            ch.pipeline().addLast("LoginAuthHandler",
                                    new LoginAuthReqHandler(spInfo,sharedInfo, smsSocketRedis, smsDataTool));
                            ch.pipeline().addLast("HeartBeatHandler",
                                    new HeartBeatReqHandler(spInfo,sharedInfo, smsSocketRedis, smsDataTool));
                            ch.pipeline().addLast("OutputMessageHandler",
                                    new OutputMessagerHandler(spInfo,sharedInfo, smsSocketRedis, smsDataTool));
                            ch.pipeline().addLast("InputMessageHandler",
                                    new InputMessageHandler(spInfo,sharedInfo, smsSocketRedis, smsDataTool));

                        }
                    });
            // 发起异步连接操作
            ChannelFuture future = b.connect(
                    new InetSocketAddress(host, port)).sync();
            future.channel().closeFuture().sync();
        } finally {
            sharedInfo.setConnected(false);//标识重新连接后需要做login
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

