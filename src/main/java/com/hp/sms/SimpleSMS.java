package com.hp.sms;

import com.hp.sms.service.ZJYSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

/**
 * 简单短信网关
 */
@Service
public class SimpleSMS implements ISms {
//    @Autowired
//    @Qualifier("cmppSender")
//    SmsService cmppSms;

    @Autowired
    @Qualifier("zjySender")
    ZJYSender zjySender;

    private Logger _logger=LoggerFactory.getLogger(SimpleSMS.class);


    public int Send(String sim, String content){
        _logger.info("send sms:"+sim+"|"+content);
        if (sim == null || content==null){
            return 0;
        }else {
            if(sim.length()!=11){
                return 0;
            }
//            cmppSms.sendSms(sim, content);
            //更改为中聚元短信接口
            zjySender.sendSms(sim, content);
            return 1;
        }
    }

}
