package com.hp.sms.service;
import org.springframework.stereotype.Service;
/**
 * 短信网关业务层接口.
 */
@Service
public interface SmsService {
    /**
     *
     * @param sim 目标短信号码
     * @param content 消息内容
     * @return 结果 1成功 0失败
     */

     public int sendSms(String sim, String content);

}
