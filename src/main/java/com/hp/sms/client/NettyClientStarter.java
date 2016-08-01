package com.hp.sms.client;

import com.hp.sms.domain.SharedInfo;
import com.hp.sms.domain.SpInfo;
import com.hp.sms.utils.SmsDataTool;
import com.hp.sms.utils.SmsSocketRedis;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by jackl on 2016/8/1.
 */
public class NettyClientStarter extends Thread{

    private SpInfo spInfo;
    private SharedInfo sharedInfo;
    private SmsSocketRedis smsSocketRedis;
    private SmsDataTool smsDataTool;
    private Logger _logger;
    private String _cmpp_ip;
    private int _cmpp_port;

    public NettyClientStarter(SpInfo spInfo,SharedInfo sharedInfo, SmsSocketRedis s, SmsDataTool dt,String cmppip,int cmppport){
        this.spInfo=spInfo;
        this.sharedInfo=sharedInfo;
        this.smsSocketRedis =s;
        this.smsDataTool =dt;
        this._logger = LoggerFactory.getLogger(NettyClientStarter.class);
        this._cmpp_ip=cmppip;
        this._cmpp_port=cmppport;
    }
    public  void run()
    {
        _logger.info("start NettyClient...");
        try {
            new NettyClient(spInfo, sharedInfo, smsSocketRedis, smsDataTool).connect(_cmpp_port, _cmpp_ip);
        }catch (Exception e){e.printStackTrace();}
    }
}
