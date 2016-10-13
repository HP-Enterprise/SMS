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
import com.hp.sms.utils.MsgUtils;
import com.hp.sms.utils.SmsDataTool;
import com.hp.sms.utils.SmsSocketRedis;
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
	private SmsSocketRedis smsSocketRedis;
	private SmsDataTool smsDataTool;
	private Logger _logger;

	public InputMessageHandler(SpInfo spInfo, SharedInfo sharedInfo, SmsSocketRedis s, SmsDataTool dt){
		this.spInfo=spInfo;
		this.sharedInfo=sharedInfo;
		this.smsSocketRedis =s;
		this.smsDataTool =dt;
		this._logger = LoggerFactory.getLogger(InputMessageHandler.class);
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg)
			throws Exception {
		//_logger.info("check deliver msg...");
		ByteBuf m = (ByteBuf) msg;
		byte[] receiveData= smsDataTool.getBytesFromByteBuf(m);
		String receiveDataHexString= smsDataTool.bytes2hex(receiveData);
		//_logger.info("Receive date from " + ctx.channel().remoteAddress() + ">>>:" + receiveDataHexString);
		MsgHead message=new MsgHead(receiveData);
		// 收到消息
		if (message.getCommandId() == MsgCommand.CMPP_DELIVER) {

			MsgDeliver msgDeliver=new MsgDeliver(receiveData);
			_logger.info("Client receive server deliver message : ---> "
					+ msgDeliver.getSrc_terminal_Id().trim() + "|" + msgDeliver.getMsg_Fmt() + "|" + msgDeliver.getMsg_Content());
			if(msgDeliver.getMsg_Fmt()==(byte)0x08){//二进制
				smsSocketRedis.saveSetString(smsDataTool.smsin_preStr+smsDataTool.sms_bin_preStr+msgDeliver.getSrc_terminal_Id().trim(),msgDeliver.getMsg_Content(),-1);//消息hex推入redis
			}else{//文本
				smsSocketRedis.saveSetString(smsDataTool.smsin_preStr+smsDataTool.sms_txt_preStr+msgDeliver.getSrc_terminal_Id().trim(),msgDeliver.getMsg_Content(),-1);//消息hex推入redis
			}

			MsgDeliverResp msgDeliverResp=buildMsgDeliverResp();
			msgDeliverResp.setMsg_Id(msgDeliver.getMsg_Id());
			msgDeliverResp.setResult(0);//返回短信接收结果
			String byteStr= smsDataTool.bytes2hex(msgDeliverResp.toByteArry());
			_logger.info("Client send Deliver Resp message to server : ---> "
					+ byteStr);
			ctx.writeAndFlush(smsDataTool.getByteBuf(byteStr));
			//ctx.fireChannelRead(msg);
			m.release();
		}else{
			m.release();
		}
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
