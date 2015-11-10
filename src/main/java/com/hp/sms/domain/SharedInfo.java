package com.hp.sms.domain;

/**
 * Created by luj on 2015/11/10.
 */
public class SharedInfo {
    //是否鉴权通过
    private boolean connected;

    public SharedInfo(){
        connected=false;
    }

    public boolean isConnected() {
        return connected;
    }

    public void setConnected(boolean connected) {
        this.connected = connected;
    }
}
