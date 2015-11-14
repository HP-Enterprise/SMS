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

import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * 发送消息
 */
public class OutputMessagerHandler extends ChannelInboundHandlerAdapter {

	private volatile ScheduledFuture<?> outputMsg;
	private SpInfo spInfo;
	private SharedInfo sharedInfo;
	private SmsSocketRedis smsSocketRedis;
	private SmsDataTool smsDataTool;
	private Logger _logger;

	public OutputMessagerHandler(SpInfo spInfo,SharedInfo sharedInfo, SmsSocketRedis s, SmsDataTool dt){
		this.spInfo=spInfo;
		this.sharedInfo=sharedInfo;
		this.smsSocketRedis =s;
		this.smsDataTool =dt;
		this._logger = LoggerFactory.getLogger(OutputMessagerHandler.class);
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg)
			throws Exception {
		ByteBuf m = (ByteBuf) msg;
		byte[] receiveData= smsDataTool.getBytesFromByteBuf(m);

		MsgHead message=new MsgHead(receiveData);
		// 握手成功，主动发送心跳消息
		if (message.getCommandId() == MsgCommand.CMPP_CONNECT_RESP) {
			if(sharedInfo.isConnected()==true){
				outputMsg = ctx.executor().scheduleAtFixedRate(
						new OutputMsgTask(ctx), 0, 100,
						TimeUnit.MILLISECONDS);
				_logger.info("------------------------------start OutputMsgTask");
			}else{
				_logger.info(">>>>>> not connected ");
			}
		}else if (message.getCommandId() == MsgCommand.CMPP_SUBMIT_RESP) {
			_logger.info("Client receive server submit resp message : ---> "
					+ message);
		}else
			ctx.fireChannelRead(msg);
	}

	private class OutputMsgTask implements Runnable {
		private final ChannelHandlerContext ctx;

		public OutputMsgTask(final ChannelHandlerContext ctx) {
			this.ctx = ctx;
		}

		@Override
		public void run() {
			String msgType="";
			String phone="";
			String msgContent="";
			//读取redis中所有的待发短信集合 采用key-set存储
			Set<String> setKey = smsSocketRedis.getKeysSet("smsoutput:*");
			if(setKey.size()>0){   _logger.info( setKey.size()+" sms wait to be handle "); }
			Iterator keys = setKey.iterator();
			while (keys.hasNext()){
				//遍历待发数据,处理
				String k=(String)keys.next();
				String[] datas=k.split(":");
				msgType=datas[1];
				phone=datas[2];//取出目标号码
				msgContent= smsSocketRedis.popSetOneString(k);
			}
			if(!phone.equals("")){
			MsgHead outMsg = buildTxtOutputMsg(msgType,phone, msgContent);
			String byteStr= smsDataTool.bytes2hex(outMsg.toByteArry());
			_logger.info("Client send submit message to server : ---> "
					+ byteStr);
			ctx.writeAndFlush(smsDataTool.getByteBuf(byteStr));
			}
		}
		private MsgHead buildTxtOutputMsg(String msgType,String phone,String msgContent) {
			//这部分的代码需要真实环境实测配置
			_logger.info(phone+">>>>>send msg:"+msgContent);
			String cusMsisdn=phone;
			MsgSubmit submit=new MsgSubmit();
			submit.setCommandId(MsgCommand.CMPP_SUBMIT);
			submit.setSequenceId(MsgUtils.getSequence());

			submit.setPkTotal((byte) 0x01);
			submit.setPkNumber((byte) 0x01);
			submit.setRegisteredDelivery((byte) 0x00);
			submit.setMsgLevel((byte) 0x01);

			submit.setFeeUserType((byte) 0x00);
			submit.setFeeTerminalId("1234567890");
			submit.setFeeTerminalType((byte) 0x00);
			submit.setTpPId((byte) 0x00);
			submit.setTpUdhi((byte) 0x00);
			if(msgType .equals("txt")){
				submit.setMsgFmt((byte) 0x08);//UCS2
				try{
					byte[] aaa=msgContent.getBytes("ISO-10646-UCS-2");
					submit.setMsgContent(aaa);
				}catch (UnsupportedEncodingException e){
					e.printStackTrace();
				}
			}else{
				submit.setMsgFmt((byte) 0x04);//二进制
				byte[] bbb = smsDataTool.getBytesFromByteBuf(smsDataTool.getByteBuf(msgContent));
				submit.setMsgContent(bbb);//二进制存储的是16进制hex字符串。转换层字节数组
			}
			submit.setMsgSrc(spInfo.getSpId());
			submit.setSrcId(spInfo.getSpCode());
			submit.setDestUsrTl((byte) 1);
			submit.setDestTerminalId(cusMsisdn);
			submit.setDestTerminalType((byte) 0);
			submit.setMsgLength((byte) submit.getMsgContent().length);
			submit.setTotalLength(12 + 8 + 1 + 1 + 1 + 1 + 10 + 1 + 32 + 1 + 1 + 1 + 1 + 6 + 2 + 6 + 17 + 17 + 21 + 1 + 32 + 1 + 1 + submit.getMsgContent() .length  + 20);
			submit.setLinkID("");
			return submit;
		}
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
			throws Exception {
		cause.printStackTrace();
		if (outputMsg != null) {
			outputMsg.cancel(true);
			outputMsg = null;
		}
		ctx.fireExceptionCaught(cause);
	}
}
