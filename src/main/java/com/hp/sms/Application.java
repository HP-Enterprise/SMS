package com.hp.sms;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.beans.factory.annotation.*;
import org.springframework.context.annotation.ComponentScan;

import java.lang.System;

@SpringBootApplication
@ComponentScan(basePackages = {"com.hp.sms"} )
public class Application implements CommandLineRunner {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    public void run(String... args) throws Exception{
        System.out.println("com.hp.sms.Application.run...");
    }

    @Autowired
    private SimpleSMS _sms;
}