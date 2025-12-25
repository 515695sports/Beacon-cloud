package com.msb.strategy.util;

import com.msb.common.constant.CacheConstant;
import com.msb.common.constant.RabbitMQConstant;
import com.msb.common.constant.SmsConstant;
import com.msb.common.enums.ExceptionEnums;
import com.msb.common.model.StandardReport;
import com.msb.common.model.StandardSubmit;
import com.msb.strategy.client.BeaconCacheClient;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 校验未通过时，实现两个供功能：
 * 1. 把消息传递到写日志队列
 * 2. 封装回调信息，把消息传递到回调队列
 */
@Component
public class ErrorSendMessageUtil {

    @Autowired
    private RabbitTemplate rabbitTemplate;
    @Autowired
    private BeaconCacheClient beaconCacheClient;

    //把消息传递到写日志队列
    public void sendWriteLog(StandardSubmit submit) {
        submit.setReportState(SmsConstant.REPORT_FAIL);
        // ====================发送写日志【sms_write_log】================================
        rabbitTemplate.convertAndSend(RabbitMQConstant.SMS_WRITE_LOG, submit);
    }

    //把消息传递到回调队列
    public void sendPushReport(StandardSubmit submit) {
        Integer isCallback = beaconCacheClient.hgetInteger(CacheConstant.CLIENT_BUSINESS + submit.getApikey(), "isCallback");
        if(isCallback==1){ //需要回调，再查询callbackUrl
            String callbackUrl = beaconCacheClient.hget(CacheConstant.CLIENT_BUSINESS + submit.getApikey(), "callbackUrl");
            if(!StringUtils.isEmpty(callbackUrl)){
                //客户需要状态报告推送且回调地址不为空，再开始封装StandardReport对象
                StandardReport report = new StandardReport();
                BeanUtils.copyProperties(submit,report);
                report.setIsCallback(isCallback);
                report.setCallbackUrl(callbackUrl);
                //发送消息到【推送报告】的队列
                rabbitTemplate.convertAndSend(RabbitMQConstant.SMS_PUSH_REPORT,report);
            }
        }
    }
}
