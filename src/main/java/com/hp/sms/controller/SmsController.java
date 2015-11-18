package com.hp.sms.controller;

import com.hp.sms.domain.Sms;
import com.hp.sms.service.SmsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.RequestContext;

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

    /**
     * * HTTP POST /api/sms/message<br>
     * post方式发送短信消息
     * @param sms Sms短信信息
     * @param request 请求对象
     * @return  如果成功返回成功信息，如果失败返回失败信息
     */
    @RequestMapping(value="/api/sms/message",method = RequestMethod.POST)
    public String sendSms(@RequestBody Sms sms , HttpServletRequest request) {
        RequestContext requestContext = new RequestContext(request);
        if (sms == null){
            return "error: null";
        }else {
//            System.out.println(sms.getPhone()+"|"+sms.getMessage());
            cmppSms.sendSms(sms.getPhone(), sms.getMessage());
            return "send SMS success!";
        }
    }

}