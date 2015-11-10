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

import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
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
	private SocketRedis socketRedis;
	private DataTool dataTool;
	private Logger _logger;

	public OutputMessagerHandler(SpInfo spInfo,SharedInfo sharedInfo,SocketRedis s,DataTool dt){
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
			String phone="";
			String msgContent="";
			//读取redis中所有的待发短信集合 采用key-set存储
			Set<String> setKey = socketRedis.getKeysSet("smsoutput:*");
			if(setKey.size()>0){   _logger.info( setKey.size()+" sms wait to be handle "); }
			Iterator keys = setKey.iterator();
			while (keys.hasNext()){
				//遍历待发数据,处理
				String k=(String)keys.next();
				phone=k.replace("smsoutput:", "");//取出目标号码
				msgContent= socketRedis.popSetOneString(k);
			}
			if(!phone.equals("")){
			MsgHead outMsg = buildOutputMsg(phone,msgContent);
			String byteStr=dataTool.bytes2hex(outMsg.toByteArry());
			_logger.info("Client send submit message to server : ---> "
			+ byteStr);
			ctx.writeAndFlush(dataTool.getByteBuf(byteStr));
			}
		}

		private MsgHead buildOutputMsg(String phone,String msgContent) {
			//这部分的代码需要真实环境实测配置
			_logger.info(phone+">>>>>send msg:"+msgContent);
			String cusMsisdn=phone;
			MsgSubmit submit=new MsgSubmit();
			submit.setTotalLength(12 + 8 + 1 + 1 + 1 + 1 + 10 + 1 + 32 + 1 + 1 + 1 + 1 + 6 + 2 + 6 + 17 + 17 + 21 + 1 + 32 + 1 + 1 + msgContent.length() * 2 + 20);
			submit.setCommandId(MsgCommand.CMPP_SUBMIT);
			submit.setSequenceId(MsgUtils.getSequence());
			//submit.setMsgId(1l);/////////////////////////////
			submit.setPkTotal((byte) 0x01);
			submit.setPkNumber((byte) 0x01);
			submit.setRegisteredDelivery((byte) 0x00);
			submit.setMsgLevel((byte) 0x01);
			//submit.setServiceId("");//////////////////////
			submit.setFeeUserType((byte) 0x00);
			submit.setFeeTerminalId("1234567890");
			submit.setFeeTerminalType((byte) 0x00);
			submit.setTpPId((byte) 0x00);
			submit.setTpUdhi((byte) 0x00);
			submit.setMsgFmt((byte) 0x0f);
			submit.setMsgSrc(spInfo.getSpId());
			/*submit.setFeeType("03");/////////////////////////////////
			submit.setFeeCode("123456");/////////////////////////////
			submit.setValIdTime("1511101449311010");////////////////////////////////
			submit.setAtTime("1511101459311010");//////////////////////////////////*/
			submit.setSrcId(spInfo.getSpCode());
			submit.setDestUsrTl((byte) 1);
			submit.setDestTerminalId(cusMsisdn);
			submit.setDestTerminalType((byte) 0);
			submit.setMsgLength((byte) (msgContent.length()));
			try{
				submit.setMsgContent(msgContent.getBytes("UnicodeBigUnmarked"));
			}catch (UnsupportedEncodingException e){
				e.printStackTrace();
			}
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
