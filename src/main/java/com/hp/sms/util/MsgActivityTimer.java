package com.hp.sms.util;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;

/**
 * 接口调用
 *
 */
@Component

public class MsgActivityTimer {
	/**
	 * 短信接口长链接，定时进行链路检查
	 */
	protected void executeInternal()throws JobExecutionException {
			System.out.println("×××××××××××××开始链路检查××××××××××××××");
			int count=0;
			boolean result=MsgContainer.activityTestISMG();
			while(!result){
				count++;
				result=MsgContainer.activityTestISMG();
				if(count>=(MsgConfig.getConnectCount()-1)){//如果再次链路检查次数超过两次则终止连接
					break;
				}
			}
			System.out.println("×××××××××××××链路检查结束××××××××××××××");
	}
}
