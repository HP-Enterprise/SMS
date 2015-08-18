package com.hp.sms;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

/**
 * 简单短信网关
 */
@Service
public class SimpleSMS {

    @Value("${com.hp.sms.ip}")
    private String _smsIP;

    public int Send(String sim, String content){
        System.out.println(this._smsIP);
        throw new NotImplementedException();
    }
}
