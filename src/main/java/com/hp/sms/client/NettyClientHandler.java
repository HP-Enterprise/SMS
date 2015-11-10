/*
 * Copyright 2012 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package com.hp.sms.client;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.util.logging.Logger;


public class NettyClientHandler extends ChannelInboundHandlerAdapter {

    private static final Logger logger = Logger
	    .getLogger(NettyClientHandler.class.getName());

    /**
     * Creates a client-side handler.
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) {
    //发送CONNECT 消息，连接建立后启动心跳线程 消息检测发送线程
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg)
	    throws Exception {
	String body = (String) msg;
	logger.info(">>>>>"+body);
    }
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
	// 释放资源
	logger.warning("Unexpected exception from downstream : "
		+ cause.getMessage());
	ctx.close();
    }
}
