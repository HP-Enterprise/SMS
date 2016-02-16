package com.hp.sms;

import org.springframework.stereotype.Component;

/**
 * Created by wh on 2016/2/15.
 */

public interface ISms {

    int Send(String sim, String content);
}
