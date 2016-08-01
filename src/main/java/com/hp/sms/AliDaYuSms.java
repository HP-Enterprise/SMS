package com.hp.sms;


import com.taobao.api.DefaultTaobaoClient;
import com.taobao.api.TaobaoClient;
import com.taobao.api.request.AlibabaAliqinFcSmsNumSendRequest;
import com.taobao.api.response.AlibabaAliqinFcSmsNumSendResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

/**
 * Created by wh on 2016/2/15.
 */

@Service
public class AliDaYuSms implements ISms {


    @Value("${com.hp.sms.url}")
    private String url;
    @Value("${com.hp.sms.appkey}")
    private String appkey;
    @Value("${com.hp.sms.secret}")
    private String secret;
    @Value("${com.hp.sms.signName}")
    private String signName;  //短信签名
    @Value("${com.hp.sms.smsTemplateCode}")
    private String smsTemplateCode; //短信模板编号


    /**
     * 阿里大鱼发送短信
     * @param sim 手机号码
     * @param content 验证码
     * @return 0 发送成功  1 发送失败
     */
    @Override
    public int Send(String sim, String content) {
        TaobaoClient client = new DefaultTaobaoClient(url, appkey, secret);
        AlibabaAliqinFcSmsNumSendRequest req = new AlibabaAliqinFcSmsNumSendRequest();
        req.setSmsType("normal");
        req.setSmsFreeSignName(signName);
        req.setSmsParamString("{\"code\":\""+content+"\",\"product\":\"\"}");
        req.setRecNum(sim);
        req.setSmsTemplateCode(smsTemplateCode);
        try {
            AlibabaAliqinFcSmsNumSendResponse rsp = client.execute(req);
            System.out.println(rsp.getBody());
            return 0;
        } catch (Exception e) {
            return 1;
        }
    }
}
