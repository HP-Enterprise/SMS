package com.hp.sms.domain;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Created by luj on 2015/11/10.
 */
@Component
public class SpInfo {
    //短信网关IP
    @Value("${com.hp.cmpp.ismgIp}")
    private String ismgIp;
    //短信网关端口，根据使用的CMPP协议不同而不同，如cmpp3.0长链接的端口为7892
    @Value("${com.hp.cmpp.ismgPort}")
    private int  ismgPort;
    //访问短信网关需要的密码
    @Value("${com.hp.cmpp.sharedSecret}")
    private String spSharedSecret;
    //由短信网关分配的SPID
    @Value("${com.hp.cmpp.spId}")
    private String spId;
       //由短信网关分配的SPCODE,即用户接受到的短信显示的主叫号码
    @Value("${com.hp.cmpp.spCode}")
    private String spCode;
    //SOCKET超时链接时间，可根据需求自由修改，建议6000，单位为毫秒
    @Value("${com.hp.cmpp.timeOut}")
    private int timeOut;
    //SOCKET链接失败重试次数，及短信发送失败重新发送的次数
    @Value("${com.hp.cmpp.connectCount}")
    private int connectCount;

    public String getIsmgIp() {
        return ismgIp;
    }

    public void setIsmgIp(String ismgIp) {
        this.ismgIp = ismgIp;
    }

    public int getIsmgPort() {
        return ismgPort;
    }

    public void setIsmgPort(int ismgPort) {
        this.ismgPort = ismgPort;
    }

    public String getSpSharedSecret() {
        return spSharedSecret;
    }

    public void setSpSharedSecret(String spSharedSecret) {
        this.spSharedSecret = spSharedSecret;
    }

    public String getSpId() {
        return spId;
    }

    public void setSpId(String spId) {
        this.spId = spId;
    }

    public String getSpCode() {
        return spCode;
    }

    public void setSpCode(String spCode) {
        this.spCode = spCode;
    }

    public int getTimeOut() {
        return timeOut;
    }

    public void setTimeOut(int timeOut) {
        this.timeOut = timeOut;
    }

    public int getConnectCount() {
        return connectCount;
    }

    public void setConnectCount(int connectCount) {
        this.connectCount = connectCount;
    }
}
