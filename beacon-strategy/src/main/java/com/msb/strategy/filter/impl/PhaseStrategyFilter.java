package com.msb.strategy.filter.impl;

import com.msb.common.constant.CacheConstant;
import com.msb.common.constant.RabbitMQConstant;
import com.msb.common.enums.MobileOperatorEnum;
import com.msb.common.model.StandardSubmit;
import com.msb.common.util.OperatorUtil;
import com.msb.strategy.client.BeaconCacheClient;
import com.msb.strategy.filter.StrategyFilter;
import com.msb.strategy.util.MobileOperatorUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * 号段补全：获取手机号运营商以及对应归属地
 */
@Service(value = "phase")
@Slf4j
public class PhaseStrategyFilter implements StrategyFilter {

    @Autowired
    private BeaconCacheClient beaconCacheClient;
    @Autowired
    private MobileOperatorUtil mobileOperatorUtil;
    @Autowired
    private RabbitTemplate rabbitTemplate;

    //切分手机号前7位
    private final int MOBILE_START=0;
    private final int MOBILE_END=7;
    //分隔符
    private final String SEPARATE=",";
    //校验的长度
    private final int LENGTH=2;
    //未知归属地
    private final String UNKNOW="未知 未知,未知";

    @Override
    public void strategy(StandardSubmit submit) {
        log.info("【策略模块-号段补齐】 校验ing......");
        //1.根据手机号查询对应区域及运营商
        String mobile = submit.getMobile().substring(MOBILE_START, MOBILE_END);
        String mobileInfo = beaconCacheClient.get(CacheConstant.PHASE + mobile);

        getMobileInfo: if(StringUtils.isEmpty(mobileInfo)){
            //2.查询不到，调用第三方接口查询手机号归属地 【这里测试删除了redis的1300521】
            mobileInfo = mobileOperatorUtil.getMobileInfoBy360(mobile);
            if(!StringUtils.isEmpty(mobileInfo)){
                //3.调用第三方工具查到了信息,发送消息到MQ，并同步到Mysql和redis
                //这里只向rabbitmq发送了手机号
                rabbitTemplate.convertAndSend(RabbitMQConstant.MOBILE_AREA_OPERATOR,submit.getMobile());
                break getMobileInfo;
            }
            mobileInfo=UNKNOW;
            //TODO 通知UNKNOW
        }

        //4.封装submit
        String[] areaAndOperator = mobileInfo.split(SEPARATE);
        if(areaAndOperator.length==LENGTH){
            submit.setArea(areaAndOperator[0]);
            submit.setOperatorId(OperatorUtil.getOperatorIdByOperatorName(areaAndOperator[1]));
        }

    }
}
