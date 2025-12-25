package com.msb.strategy.filter.impl;

import com.msb.common.constant.CacheConstant;
import com.msb.common.enums.ExceptionEnums;
import com.msb.common.exception.StrategyException;
import com.msb.common.model.StandardSubmit;
import com.msb.strategy.client.BeaconCacheClient;
import com.msb.strategy.filter.StrategyFilter;
import com.msb.strategy.util.ErrorSendMessageUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 全局级别黑名单校验
 */
@Service(value = "blackGlobal")
@Slf4j
public class BlackGlobalStrategyFilter implements StrategyFilter {

    @Autowired
    private BeaconCacheClient beaconCacheClient;
    @Autowired
    private ErrorSendMessageUtil errorSendMessageUtil;

    //黑名单的默认value
    private final String TRUE="1";

    @Override
    public void strategy(StandardSubmit submit) {
        log.info("【策略模块-全局级别黑名单校验】 校验ing......");
        //1.获取发送短信的手机号
        String mobile = submit.getMobile();

        //2.调用redis查询
        String value = beaconCacheClient.get(CacheConstant.BLACK + mobile);

        //3.查询结果为"1"代表黑名单
        if(TRUE.equals(value)){
            log.info("【策略模块-全局级别黑名单校验】 当前手机号在全局黑名单内,mobile={}",mobile);
            //封装errorMsg
            submit.setErrorMsg(ExceptionEnums.BLACK_GLOBAL.getMsg()+",mobile= "+mobile);
            //发送消息到写日志队列
            errorSendMessageUtil.sendWriteLog(submit);
            //发送消息到状态报告推送队列
            errorSendMessageUtil.sendPushReport(submit);
            //抛出异常
            throw new StrategyException(ExceptionEnums.BLACK_GLOBAL);
        }

        //4.不是"1"正常结束
        log.info("【策略模块-全局级别黑名单校验】 全局黑名单校验通过");
    }
}
