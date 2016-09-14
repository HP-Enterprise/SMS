package com.hp.sms.service;

import com.hp.sms.utils.SmsDataTool;
import com.hp.sms.utils.SmsSocketRedis;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Created by luj on 2015/11/6.
 */
@Service("cmppSender")
public class CmppSender implements SmsService{
    @Autowired
    SmsSocketRedis smsSocketRedis;
    @Autowired
    SmsDataTool smsDataTool;

    @Value("${com.hp.cmpp.disabled}")
    private boolean cmppDisabled;//自带cmpp客户端是否已经禁用

    @Autowired
    HttpSmsSender httpSmsSender;
    private Logger _logger= LoggerFactory.getLogger(CmppSender.class);
    @Override
    public int sendSms(String sim, String content) {
        String target=cmppDisabled?"cmpp http api":"cmpp client";
        _logger.info("handle txt sms to "+target+":" + sim + "|" + content);
        if(sim!=null){
            if(sim.length()==11){
                if(!cmppDisabled){//use local client
                smsSocketRedis.saveSetString(smsDataTool.smsout_preStr + smsDataTool.sms_txt_preStr + sim, content, -1);//消息推入redis
                return RESULT_SUCCESS;
                }else{// use http gateway
                    int result=httpSmsSender.Send(sim,content);
                    return result;
                }
            }
        }
        _logger.info("sms to cmpp error:sim:" + sim + "|");
        return RESULT_FAILURE;
    }

    @Override
    public int sendBinSms(String sim, byte[] content){
        _logger.info("handle bin sms to cmpp:" + sim + "|" + content.length+"(bytes)");
        if(content.length==0){
            return RESULT_FAILURE;
        }
        String hexStr=smsDataTool.bytes2hex(content);
        smsSocketRedis.saveSetString(smsDataTool.smsout_preStr+smsDataTool.sms_bin_preStr+sim,hexStr,-1);//消息hex推入redis
        return RESULT_SUCCESS;
    }


    public int sendBinSms(String sim,String hexStr){
        _logger.info("handle bin sms to cmpp:" + sim + "|" + hexStr);
        if(hexStr.length()==0){
            return RESULT_FAILURE;
        }
        smsSocketRedis.saveSetString(smsDataTool.smsout_preStr+smsDataTool.sms_bin_preStr+sim,hexStr,-1);//消息hex推入redis
        return RESULT_SUCCESS;
    }
}
