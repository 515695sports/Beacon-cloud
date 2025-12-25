package com.msb.strategy.filter.impl;

import com.msb.common.constant.CacheConstant;
import com.msb.common.enums.ExceptionEnums;
import com.msb.common.exception.StrategyException;
import com.msb.common.model.StandardSubmit;
import com.msb.strategy.client.BeaconCacheClient;
import com.msb.strategy.filter.StrategyFilter;
import com.msb.strategy.util.ClientBalanceUtil;
import com.msb.strategy.util.ErrorSendMessageUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 策略模块的扣费操作
 */
@Service(value = "fee")
@Slf4j
public class FeeStrategyFilter implements StrategyFilter {

    @Autowired
    private BeaconCacheClient beaconCacheClient;
    @Autowired
    private ErrorSendMessageUtil errorSendMessageUtil;

    private final String BALANCE="balance";

    @Override
    public void strategy(StandardSubmit submit) {
        log.info("【策略模块-预扣费校验】 校验ing......");
        //1.获取submit中封装的金额
        Long fee = submit.getFee();
        Long clientId = submit.getClientId();

        //2.调用redis的decr扣减具体金额
        Long amount = beaconCacheClient.hIncrBy(CacheConstant.CLIENT_BALANCE + clientId, BALANCE, -fee);

        //3.获取当前客户的欠费金额限制（外部方法调用即可）
        Long amountLimit = ClientBalanceUtil.getClientAmountLimit(submit.getClientId());

        //4.判断扣减后的金额是否超过欠费限制
        if(amount<amountLimit){
            log.info("【策略模块-预扣费校验】 扣费后超出欠费余额的限制，无法发出短信");
            //5.如果超过了，把扣除的费用加回去。并做后续处理
            beaconCacheClient.hIncrBy(CacheConstant.CLIENT_BALANCE+clientId,BALANCE,fee);
            submit.setErrorMsg(ExceptionEnums.BALANCE_NOT_ENOUGH.getMsg());
            errorSendMessageUtil.sendWriteLog(submit);
            errorSendMessageUtil.sendPushReport(submit);
            throw new StrategyException(ExceptionEnums.BALANCE_NOT_ENOUGH);
        }
        log.info("【策略模块-预扣费校验】 扣费成功");
    }
}
