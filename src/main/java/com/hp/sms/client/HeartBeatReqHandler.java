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
import com.hp.sms.utils.MsgUtils;

import com.hp.sms.utils.SmsDataTool;
import com.hp.sms.utils.SmsSocketRedis;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * 心跳
 */
public class HeartBeatReqHandler extends ChannelInboundHandlerAdapter {

    private volatile ScheduledFuture<?> heartBeat;
	private SpInfo spInfo;
	private SharedInfo sharedInfo;
	private SmsSocketRedis smsSocketRedis;
	private SmsDataTool smsDataTool;
	private Logger _logger;

	public HeartBeatReqHandler(SpInfo spInfo,SharedInfo sharedInfo, SmsSocketRedis s, SmsDataTool dt){
		this.spInfo=spInfo;
		this.sharedInfo=sharedInfo;
		this.smsSocketRedis =s;
		this.smsDataTool =dt;
		this._logger = LoggerFactory.getLogger(HeartBeatReqHandler.class);
	}

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg)
	    throws Exception {
		ByteBuf m = (ByteBuf) msg;
		byte[] receiveData= smsDataTool.getBytesFromByteBuf(m);

		MsgHead message=new MsgHead(receiveData);
	// 握手成功，启动心跳发送任务
	if (message.getCommandId() == MsgCommand.CMPP_CONNECT_RESP) {
		if(sharedInfo.isConnected()==true){
	    	heartBeat = ctx.executor().scheduleAtFixedRate(
		    new HeartBeatTask(ctx), 0, 10000,
		    TimeUnit.MILLISECONDS);
			_logger.info("正在启动心跳任务");
		}else{
		_logger.info(">>>>>> not connected ");
		}
		ctx.fireChannelRead(msg);
	} else if (message.getCommandId() == MsgCommand.CMPP_ACTIVE_TEST_RESP) {
		//_logger.info("Client receive server heart beat message : ---> "
			//  + message);
		//ctx.fireChannelRead(msg);//真实运行不需要抛出心跳resp msg，目前为了便于检测收消息，暂时开启
		m.release();
	} else
	    ctx.fireChannelRead(msg);
    }

    private class HeartBeatTask implements Runnable {
	private final ChannelHandlerContext ctx;

	public HeartBeatTask(final ChannelHandlerContext ctx) {
	    this.ctx = ctx;
	}

	@Override
	public void run() {
		MsgHead heatBeat = buildHeatBeat();

		String byteStr= smsDataTool.bytes2hex(heatBeat.toByteArry());
		//_logger.info("Client send heart beat message to server : ---> "
		//		+ byteStr);
		ctx.writeAndFlush(smsDataTool.getByteBuf(byteStr));
	 	}

	private MsgHead buildHeatBeat() {
		MsgHead head=new MsgHead();
		head.setTotalLength(12);//消息总长度，级总字节数:4+4+4(消息头)
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
