package com.msb.strategy.filter.impl;

import com.msb.common.constant.CacheConstant;
import com.msb.common.model.StandardSubmit;
import com.msb.strategy.client.BeaconCacheClient;
import com.msb.strategy.filter.StrategyFilter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * 携号转网策略
 */
@Service(value = "transfer")
@Slf4j
public class TansferStrategyFilter implements StrategyFilter {

    @Autowired
    private BeaconCacheClient beaconCacheClient;

    //发生了携号转网
    private final Boolean TRANSFER=true;

    @Override
    public void strategy(StandardSubmit submit) {
        log.info("【策略模块-携号转网策略】 校验ing......");
        //1.获取用户手机号
        String mobile = submit.getMobile();

        //2.基于redis查询携号转网信息
        String value = beaconCacheClient.get(CacheConstant.TRANSFER + mobile);

        //3.如果存在携号转网，设置重置operatorId
        if(!StringUtils.isEmpty(value)){
            //携号转网了
            submit.setOperatorId(Integer.valueOf(value));
            //设置isTransfer为true
            submit.setIsTransfer(TRANSFER);
            log.info("【策略模块-携号转网策略】 当前手机号发生了携号转网");
            return;
        }
        log.info("【策略模块-携号转网策略】 当前手机号没有携号转网");
    }
}
