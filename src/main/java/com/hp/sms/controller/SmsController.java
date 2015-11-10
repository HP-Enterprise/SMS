package com.hp.sms.controller;
import com.hp.sms.service.SmsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
/**
 * Created by luj on 2015/11/10.
 */
/**
 * 短信接口
 */
@RestController
public class SmsController {
    @Autowired
    @Qualifier("cmppSender")
    SmsService cmppSms;

    @RequestMapping(value="api/sms/{phone}/msg/{message}",method = RequestMethod.GET)//暂时GET方法便于测试
    public String send(@PathVariable("phone") String phone,
                    @PathVariable("message") String message, String token,HttpServletRequest request){
        cmppSms.sendSms(phone,message);
        return "send SMS success!";
    }

}