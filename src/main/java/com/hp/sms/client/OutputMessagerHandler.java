/*
 * Copyright 2013-2018 Lilinfeng.
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 *      http://www.apache.org/licenses/LICENSE-2.0
 *  
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.hp.sms.client;

import com.hp.sms.domain.MsgCommand;
import com.hp.sms.domain.MsgHead;
import com.hp.sms.domain.SharedInfo;
import com.hp.sms.domain.SpInfo;
import com.hp.sms.util.MsgUtils;
import com.hp.sms.utils.DataTool;
import com.hp.sms.utils.SocketRedis;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * @author Lilinfeng
 * @date 2014年3月15日
 * @version 1.0
 */
public class OutputMessagerHandler extends ChannelInboundHandlerAdapter {

    private volatile ScheduledFuture<?> heartBeat;
	private SpInfo spInfo;
	private SharedInfo sharedInfo;
	private SocketRedis socketRedis;
	private DataTool dataTool;
	private Logger _logger;

	public OutputMessagerHandler(SpInfo spInfo, SharedInfo sharedInfo, SocketRedis s, DataTool dt){
		this.spInfo=spInfo;
		this.sharedInfo=sharedInfo;
		this.socketRedis=s;
		this.dataTool=dt;
		this._logger = LoggerFactory.getLogger(OutputMessagerHandler.class);
	}

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg)
	    throws Exception {
		ByteBuf m = (ByteBuf) msg;
		byte[] receiveData=dataTool.getBytesFromByteBuf(m);
		String receiveDataHexString=dataTool.bytes2hex(receiveData);
		_logger.info("Receive date from " + ctx.channel().remoteAddress() + ">>>:" + receiveDataHexString);
		// 握手成功，主动发送心跳消息
		if(sharedInfo.isConnected()==true){
			heartBeat = ctx.executor().scheduleAtFixedRate(
				new HeartBeatTask(ctx), 0, 10000,
				TimeUnit.MILLISECONDS);
			}else{
				_logger.info(">>>>>> not connected ");
			}
		ctx.fireChannelRead(msg);
	}


	private class HeartBeatTask implements Runnable {
	private final ChannelHandlerContext ctx;
	public HeartBeatTask(final ChannelHandlerContext ctx) {
	    this.ctx = ctx;
	}
	@Override
	public void run() {
		MsgHead heatBeat = buildOutputMsg();
		String byteStr=dataTool.bytes2hex(heatBeat.toByteArry());
		_logger.info("Client send submit messsage to server : ---> "
				+ byteStr);
		ctx.writeAndFlush(dataTool.getByteBuf(byteStr));
	 	}
	private MsgHead buildOutputMsg() {
		MsgHead head=new MsgHead();
		head.setTotalLength(12);//消息总长度，级总字节数:4+4+4(消息头)+6+16+1+4(消息主体)
		head.setCommandId(MsgCommand.CMPP_ACTIVE_TEST);//标识创建连接
		head.setSequenceId(MsgUtils.getSequence());//序列，由我们指定
	    return head;
	}
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
	    throws Exception {
	cause.printStackTrace();
	if (heartBeat != null) {
	    heartBeat.cancel(true);
	    heartBeat = null;
	}
	ctx.fireExceptionCaught(cause);
    }
}
