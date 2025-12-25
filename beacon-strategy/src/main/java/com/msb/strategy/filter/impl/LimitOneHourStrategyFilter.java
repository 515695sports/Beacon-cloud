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

import java.time.LocalDateTime;
import java.time.ZoneOffset;

/**
 * 只有验证码类短信会做限流
 * 同一客户针对同一手机号的验证码内容，1h只能发送3条短信
 */
@Service(value = "limitOneHour")
@Slf4j
public class LimitOneHourStrategyFilter implements StrategyFilter {

    @Autowired
    private BeaconCacheClient beaconCacheClient;
    @Autowired
    private ErrorSendMessageUtil errorSendMessageUtil;

    //东八区
    private final String UTC="+8";
    //1 h的毫秒值
    private final long ONE_HOUR=3600*1000-1;
    private final int RETRY_COUNT=2;
    private final int LIMIT_HOUR=3;


    @Override
    public void strategy(StandardSubmit submit) {
        //营销类短信才限流
        if(submit.getState()!= SmsConstant.CODE_TYPE){
            return;
        }
        log.info("【策略模块-1h限流策略】 校验ing......");
        //1.基于submit获取短信发送时间
        LocalDateTime sendTime = submit.getSendTime();

        //2.基于LocalDateTime获取时间毫秒值
        long sendTimeMilli = sendTime.toInstant(ZoneOffset.of(UTC)).toEpochMilli();
        submit.setOneHourLimitMilli(sendTimeMilli);

        //3.基于submit获取客户标识以及手机号信息
        Long clientId = submit.getClientId();
        String mobile = submit.getMobile();

        //4.先把当前获取的信息插入到redis的Zset结构中
        String key=CacheConstant.LIMIT_HOURS+clientId+CacheConstant.SEPARATE+mobile;

        //5.如果插入失败，需要重新将毫秒值做改变，尝试重新插入
        int retry=0;
        while (!beaconCacheClient.zaddLong(key,submit.getOneHourLimitMilli(),submit.getOneHourLimitMilli())){
            //发送失败,尝试重试
            if(retry==RETRY_COUNT) break;
            retry++;
            //插入失败是由于member不允许重复，既然重复了，就把时间向后移动,移动到当前系统时间
            submit.setOneHourLimitMilli(System.currentTimeMillis());

        }
        //如果retry为2，代表已经重试两次都没成功
        if(retry==RETRY_COUNT){
            log.info("【策略模块-1h限流策略】插入失败！满足1h限流规则，无法发送");
            //TODO 失败后处理
            submit.setErrorMsg(ExceptionEnums.ONE_HOUR_LIMIT.getMsg()+",mobile="+mobile);
            errorSendMessageUtil.sendWriteLog(submit);
            errorSendMessageUtil.sendPushReport(submit);
            throw new StrategyException(ExceptionEnums.ONE_HOUR_LIMIT);
        }

        //没有重试2次，3次之内把数据成功插入,基于zRangeScore范围查询
        long start = submit.getOneHourLimitMilli() - ONE_HOUR;
        int count = beaconCacheClient.zRangeByScoreCount(key, start, submit.getOneHourLimitMilli());

        if(count>LIMIT_HOUR){
            log.info("【策略模块-1h限流策略】查询数据！满足1h限流规则，无法发送");
            //TODO 失败后处理
            //删除插入的这个数据
            beaconCacheClient.zRemove(key,submit.getOneHourLimitMilli()+"");
            submit.setErrorMsg(ExceptionEnums.ONE_HOUR_LIMIT.getMsg()+",mobile="+mobile);
            errorSendMessageUtil.sendWriteLog(submit);
            errorSendMessageUtil.sendPushReport(submit);
            throw new StrategyException(ExceptionEnums.ONE_HOUR_LIMIT);
        }

        log.info("【策略模块-1h限流策略】1h限流规则通过，可以发送");
    }
}
