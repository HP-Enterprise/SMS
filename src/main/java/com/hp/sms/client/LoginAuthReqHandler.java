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

import com.hp.sms.utils.MsgUtils;
import com.hp.sms.utils.SmsDataTool;
import com.hp.sms.utils.SmsSocketRedis;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import  com.hp.sms.domain.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



/**
 * 发起鉴权校验
 */
public class LoginAuthReqHandler extends ChannelInboundHandlerAdapter {
	private SpInfo spInfo;
	private SharedInfo sharedInfo;
	private SmsSocketRedis smsSocketRedis;
	private SmsDataTool smsDataTool;
	private Logger _logger;

	public LoginAuthReqHandler(SpInfo spInfo,SharedInfo sharedInfo, SmsSocketRedis s, SmsDataTool dt){
		this.spInfo=spInfo;
		this.sharedInfo=sharedInfo;
		this.smsSocketRedis =s;
		this.smsDataTool =dt;
		this._logger = LoggerFactory.getLogger(LoginAuthReqHandler.class);
	}
    /**
     * Calls {@link ChannelHandlerContext#fireChannelActive()} to forward to the
     * next {@link ChannelHandler} in the {@link ChannelPipeline}.
     * 
     * Sub-classes may override this method to change behavior.
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
		_logger.info(">>>>>>>>>>>>>>>>LoginAuthReqHandler channelActive");
		if(sharedInfo.isConnected()==false){
			_logger.info(">>>>>>>>>>>>>>>>do Connect");
			MsgHead h=buildLoginReq();
			String byteStr= smsDataTool.bytes2hex(h.toByteArry());
			_logger.info(byteStr);
			ctx.writeAndFlush(smsDataTool.getByteBuf(byteStr));
		}else{
			_logger.info(">>>>>>>>>>>>>>>>not need to do Connect");
		}

    }

    /**
     * Calls {@link ChannelHandlerContext#fireChannelRead(Object)} to forward to
     * the next {@link ChannelHandler} in the {@link ChannelPipeline}.
     * 
     * Sub-classes may override this method to change behavior.
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg)
		    throws Exception {

		ByteBuf m = (ByteBuf) msg;
		byte[] receiveData= smsDataTool.getBytesFromByteBuf(m);
		String receiveDataHexString= smsDataTool.bytes2hex(receiveData);
		//_logger.info("Receive date from " + ctx.channel().remoteAddress() + ">>>:" + receiveDataHexString);
		//
		MsgHead message=new MsgHead(receiveData);

	// 如果是握手应答消息，需要判断是否认证成功
	if (message.getCommandId() == MsgCommand.CMPP_CONNECT_RESP
		) {
		_logger.info(">>>>>>>>>>>>>>>>CMPP_CONNECT_RESP");
		MsgConnectResp connectResp=new MsgConnectResp(receiveData);
	    int loginResult = (connectResp.getStatus());
	    if (loginResult !=  0) {
		// 握手失败，关闭连接
		ctx.close();
	    } else {
			sharedInfo.setConnected(true);
			_logger.info("Login is ok: " + sharedInfo.isConnected());
		ctx.fireChannelRead(msg);
	    }
	} else
	    ctx.fireChannelRead(msg);
    }

    private MsgHead buildLoginReq() {
		MsgConnect connect=new MsgConnect();
		connect.setTotalLength(12 + 6 + 16 + 1 + 4);//消息总长度，级总字节数:4+4+4(消息头)+6+16+1+4(消息主体)
		connect.setCommandId(MsgCommand.CMPP_CONNECT);//标识创建连接
		connect.setSequenceId(MsgUtils.getSequence());//序列，由我们指定
		connect.setSourceAddr(spInfo.getSpId());//我们的企业代码
		connect.setAuthenticatorSource(MsgUtils.getAuthenticatorSource(spInfo.getSpId(),spInfo.getSpSharedSecret()));//md5(企业代码+密匙+时间戳)
		connect.setTimestamp(Integer.parseInt(MsgUtils.getTimestamp()));//时间戳(MMDDHHMMSS)
		connect.setVersion((byte)0x30);//版本号 高4bit为3，低4位为0
	return connect;
    }

    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
	    throws Exception {
	ctx.fireExceptionCaught(cause);
    }
}
