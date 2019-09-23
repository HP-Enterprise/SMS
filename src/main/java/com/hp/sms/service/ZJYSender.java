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
@Service("zjySender")
public class ZJYSender implements SmsService{

    @Autowired
    HttpSmsSender httpSmsSender;
    private Logger _logger= LoggerFactory.getLogger(ZJYSender.class);
    @Override
    public int sendSms(String sim, String content) {
        if(sim!=null){
            if(sim.length()==11){
                //所有短信服务均走http请求
//                int result=httpSmsSender.sendZJY(sim, content, "");
                //中聚元新短信接口
                //int result=httpSmsSender.sendAPI(sim, content);
                int result=httpSmsSender.sendMsg(sim, content);
                return result;
            }
        }
        _logger.info("sms to zjy:sim:" + sim + "|");
        return RESULT_FAILURE;
    }

    @Override
    public int sendBinSms(String sim, byte[] content){
        return RESULT_FAILURE;
    }
}
