package com.hp.sms.service;

import com.hp.sms.utils.SmsDataTool;
import com.hp.sms.utils.SmsSocketRedis;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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

    private Logger _logger= LoggerFactory.getLogger(CmppSender.class);
    @Override
    public int sendSms(String sim, String content) {
        _logger.info("handle txt sms to cmpp:" + sim + "|" + content);
        smsSocketRedis.saveSetString(smsDataTool.smsout_preStr+smsDataTool.sms_txt_preStr+sim,content,-1);//消息推入redis
        return RESULT_SUCCESS;
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
