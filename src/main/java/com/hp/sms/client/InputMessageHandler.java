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

import com.hp.sms.domain.*;
import com.hp.sms.util.MsgUtils;
import com.hp.sms.utils.DataTool;
import com.hp.sms.utils.SocketRedis;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 接收消息
 */
public class InputMessageHandler extends ChannelInboundHandlerAdapter {

	private SpInfo spInfo;
	private SharedInfo sharedInfo;
	private SocketRedis socketRedis;
	private DataTool dataTool;
	private Logger _logger;

	public InputMessageHandler(SpInfo spInfo, SharedInfo sharedInfo, SocketRedis s, DataTool dt){
		this.spInfo=spInfo;
		this.sharedInfo=sharedInfo;
		this.socketRedis=s;
		this.dataTool=dt;
		this._logger = LoggerFactory.getLogger(InputMessageHandler.class);
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg)
			throws Exception {
		_logger.info("check deliver msg...");
		ByteBuf m = (ByteBuf) msg;
		byte[] receiveData=dataTool.getBytesFromByteBuf(m);
		String receiveDataHexString=dataTool.bytes2hex(receiveData);
		//_logger.info("Receive date from " + ctx.channel().remoteAddress() + ">>>:" + receiveDataHexString);
		MsgHead message=new MsgHead(receiveData);
		// 收到消息
		if (message.getCommandId() == MsgCommand.CMPP_DELIVER) {

			MsgDeliver msgDeliver=new MsgDeliver(receiveData);
			_logger.info("Client receive server deliver message : ---> "
					+ msgDeliver.getMsg_Content());
			MsgDeliverResp msgDeliverResp=buildMsgDeliverResp();
			msgDeliverResp.setMsg_Id(msgDeliver.getMsg_Id());
			msgDeliverResp.setResult(0);//返回短信接收结果
			String byteStr=dataTool.bytes2hex(msgDeliverResp.toByteArry());
			_logger.info("Client send Deliver Resp message to server : ---> "
					+ byteStr);
			ctx.writeAndFlush(dataTool.getByteBuf(byteStr));
			ctx.fireChannelRead(msg);
		}else
			ctx.fireChannelRead(msg);
		}

	private MsgDeliverResp buildMsgDeliverResp() {
		MsgDeliverResp msgDeliverResp=new MsgDeliverResp();
		msgDeliverResp.setTotalLength(12+8+4);//消息总长度，级总字节数:4+4+4(消息头)+6+16+1+4(消息主体)
		msgDeliverResp.setCommandId(MsgCommand.CMPP_DELIVER_RESP);//标识创建连接
		msgDeliverResp.setSequenceId(MsgUtils.getSequence());//序列，由我们指定
		return msgDeliverResp;
	}
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
			throws Exception {
		cause.printStackTrace();
		ctx.fireExceptionCaught(cause);
	}
}
