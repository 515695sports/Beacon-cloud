package com.msb.strategy.filter.impl;

import com.msb.common.constant.CacheConstant;
import com.msb.common.constant.SmsConstant;
import com.msb.common.enums.ExceptionEnums;
import com.msb.common.exception.StrategyException;
import com.msb.common.model.StandardSubmit;
import com.msb.strategy.client.BeaconCacheClient;
import com.msb.strategy.filter.StrategyFilter;
import com.msb.strategy.util.ErrorSendMessageUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

/**
 * 只有验证码类短信会做限流
 * 同一客户针对同一手机号的验证码内容，60s只能发送一条短信
 */
@Service(value = "limitOneMinute")
@Slf4j
public class LimitOneMinuteStrategyFilter implements StrategyFilter {

    @Autowired
    private BeaconCacheClient beaconCacheClient;
    @Autowired
    private ErrorSendMessageUtil errorSendMessageUtil;

    //东八区
    private final String UTC="+8";
    //1 min的毫秒值
    private final long ONE_MINUTE=60000-1;


    @Override
    public void strategy(StandardSubmit submit) {
        //营销类短信才限流
        if(submit.getState()!= SmsConstant.CODE_TYPE){
            return;
        }

        log.info("【策略模块-60s限流策略】 校验ing......");
        //1.基于submit获取短信发送时间
        LocalDateTime sendTime = submit.getSendTime();

        //2.基于LocalDateTime获取时间毫秒值
        long sendTimeMilli = sendTime.toInstant(ZoneOffset.of(UTC)).toEpochMilli();

        //3.基于submit获取客户标识以及手机号信息
        Long clientId = submit.getClientId();
        String mobile = submit.getMobile();

        //4.先把当前获取的信息插入到redis的Zset结构中
        String key=CacheConstant.LIMIT_MINUTES+clientId+CacheConstant.SEPARATE+mobile;
        boolean addOk = beaconCacheClient.zaddLong(key, sendTimeMilli, sendTimeMilli);

        //5.如果插入失败，直接告辞，存在并发情况，60s不能发送两条
        if(!addOk){
            log.info("【策略模块-60s限流策略】插入失败！满足60s限流规则，无法发送");
            //TODO 失败后处理
            submit.setErrorMsg(ExceptionEnums.ONE_MINUTE_LIMIT.getMsg()+",mobile="+mobile);
            errorSendMessageUtil.sendWriteLog(submit);
            errorSendMessageUtil.sendPushReport(submit);
            throw new StrategyException(ExceptionEnums.ONE_MINUTE_LIMIT);
        }

        //6.基于zRangeByScoreCount查询1min内是否只有当前插入的短信信息
        long start = sendTimeMilli - ONE_MINUTE;
        int count = beaconCacheClient.zRangeByScoreCount(key, start, sendTimeMilli);

        //7.如果>=2条，达到了60s一条的限流要求，直接告辞
        if(count>1){
            //一分钟之前发送过短信，限流规则生效
            log.info("【策略模块-60s限流策略】查询数据！满足60s限流规则，无法发送");
            //TODO 失败后处理
            //删除插入的这条数据
            beaconCacheClient.zRemove(key,sendTimeMilli+"");
            submit.setErrorMsg(ExceptionEnums.ONE_MINUTE_LIMIT.getMsg()+",mobile="+mobile);
            errorSendMessageUtil.sendWriteLog(submit);
            errorSendMessageUtil.sendPushReport(submit);
            throw new StrategyException(ExceptionEnums.ONE_MINUTE_LIMIT);
        }

        log.info("【策略模块-60s限流策略】60s限流规则通过，可以发送");
    }
}
