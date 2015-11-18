package com.hp.sms.domain;

/**
 * Created by zxZhang on 2015/11/18.
 */

public class Sms {
    private String phone;
    private String message;

    public Sms() {
    }

    public Sms(String message, String phone) {
        this.message = message;
        this.phone = phone;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
