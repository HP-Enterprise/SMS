package com.hp.sms;

import com.hp.sms.client.NettyClient;
import com.hp.sms.domain.SharedInfo;
import com.hp.sms.domain.SpInfo;
import com.hp.sms.service.SimpleSMS;
import com.hp.sms.utils.SmsDataTool;
import com.hp.sms.utils.SmsSocketRedis;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.beans.factory.annotation.*;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"com.hp.sms"} )
public class Application implements CommandLineRunner {
    @Autowired
    private SimpleSMS _sms;

    @Value("${com.hp.cmpp.ismgIp}")
    private String _cmpp_ip;

    @Value("${com.hp.cmpp.ismgPort}")
    private int _cmpp_port;

    @Value("${com.hp.cmpp.disabled}")
    private boolean _disabled;
    @Autowired
    SmsDataTool smsDataTool;
    public static SharedInfo sharedInfo=new SharedInfo();

    @Autowired
    SmsSocketRedis smsSocketRedis;

    @Autowired
    SpInfo spInfo;
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    public void run(String... args) throws Exception{
        Logger logger = LoggerFactory.getLogger(Application.class);
        logger.info("Application is running...");

        if(!_disabled){
            logger.info("Client try connect to cmpp @" + _cmpp_ip + ":" + _cmpp_port);
            new NettyClient(spInfo,sharedInfo, smsSocketRedis, smsDataTool).connect(_cmpp_port, _cmpp_ip);
            logger.info("Client try connect to cmpp @" + _cmpp_ip + ":" + _cmpp_port);
        }


    }



}