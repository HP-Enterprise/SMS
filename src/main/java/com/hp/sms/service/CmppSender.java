package com.hp.sms.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Created by luj on 2015/11/6.
 */
@Service("cmppSender")
public class CmppSender implements SmsService{
    @Value("${com.hp.cmpp.ip}")
    private String _cmppIP;

    private Logger _logger= LoggerFactory.getLogger(CmppSender.class);
    @Override
    public int sendSms(String sim, String content) {
        _logger.info(_cmppIP);
        return 0;
    }
}
