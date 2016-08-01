package com.hp.sms.service;
import org.springframework.stereotype.Service;
/**
 * 短信网关业务层接口.
 */
@Service
public interface SmsService {
    public static int RESULT_SUCCESS=1;
    public static int RESULT_FAILURE=0;
    /**
     * 发送文本短信，支持中英文
     * @param sim 目标短信号码
     * @param content 消息内容
     * @return 结果 1成功 0失败
     */

     public int sendSms(String sim, String content);

    /**
     * 发送二进制短信
     * @param sim 目标短信号码
     * @param content 消息内容（字节数组）
     * @return 结果 1成功 0失败
     */
    public int sendBinSms(String sim, byte[] content);

}
