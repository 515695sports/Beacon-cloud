package com.msb.strategy.filter.impl;

import com.msb.common.constant.CacheConstant;
import com.msb.common.constant.RabbitMQConstant;
import com.msb.common.enums.ExceptionEnums;
import com.msb.common.exception.StrategyException;
import com.msb.common.model.StandardSubmit;
import com.msb.strategy.client.BeaconCacheClient;
import com.msb.strategy.filter.StrategyFilter;
import com.msb.strategy.util.ChannelTransferUtil;
import com.msb.strategy.util.ErrorSendMessageUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * 路由策略：选择合适的运营商通道
 */
@Service(value = "route")
@Slf4j
public class RouteStrategyFilter implements StrategyFilter {

    @Autowired
    private BeaconCacheClient beaconCacheClient;
    @Autowired
    private ErrorSendMessageUtil errorSendMessageUtil;
    //在这里导入用于动态构建队列
    @Autowired
    private AmqpAdmin amqpAdmin;
    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Override
    public void strategy(StandardSubmit submit) {
        log.info("【策略模块-路由策略】 校验ing......");
        //1.拿到客户id
        Long clientId = submit.getClientId();

        //2.基于redis获取当前对象绑定的所有channel
        Set<Map> clientChannels = beaconCacheClient.smemberMap(CacheConstant.CLIENT_CHANNEL + clientId);

        //3.把获取到的客户通道信息基于权重排序
        TreeSet<Map> clientWeightChannels = new TreeSet<>(new Comparator<Map>() {
            @Override
            public int compare(Map o1, Map o2) {
                int o1Weight = Integer.parseInt(o1.get("clientChannelWeight")+"");
                int o2Weight = Integer.parseInt(o2.get("clientChannelWeight")+"");
                return o2Weight-o1Weight; //倒序
            }
        });
        clientWeightChannels.addAll(clientChannels);

        boolean ok=false;
        Map channel=null;
        Map chosenChannel=null;
        //4.基于排好序的通道,选择权重更高的
        for (Map clientWeightChannel : clientWeightChannels) {
            if((int)(clientWeightChannel.get("isAvailable"))!=0){
                //当前绑定关系不可用,选择权重更低一点的通道
                continue;
            }
            //5.若客户和通道的绑定可用,直接基于redis查询具体通道信息
            channel = beaconCacheClient.hGetAll(CacheConstant.CHANNEL + clientWeightChannel.get("channelId"));
            if((int)(channel.get("isAvailable"))!=0){
                //当前通道不可用,选择权重更低一点的通道
                continue;
            }
            //6.如果通道信息查询后依然可用，其次运营商可以匹配
            //获取通道类型
            Integer channelType = (Integer) channel.get("channelType");
            if(channelType!=0 && submit.getOperatorId()!=channelType){
                //通道不是全网通，而且和当前手机号运营商不匹配
                continue;
            }
            //7.如果后期涉及通道转换，这里留一个口子
            Map transferChannel = ChannelTransferUtil.transfer(submit, channel);

            //找到了可以使用的通道
            ok=true;
            chosenChannel=clientWeightChannel;
            break;
        }

        if(!ok){ //没找到可用通道
            log.info("【策略模块-路由策略】 没有可用通道");
            submit.setErrorMsg(ExceptionEnums.NO_CHANNEL.getMsg());
            errorSendMessageUtil.sendWriteLog(submit);
            errorSendMessageUtil.sendPushReport(submit);
            throw new StrategyException(ExceptionEnums.NO_CHANNEL);
        }

        //8.选择通道成功
        log.info("【策略模块-路由策略】 通道选择成功");
        //8.1基于选择的通道封装submit的信息
        submit.setChannelId(Long.parseLong(channel.get("id")+""));
        //channelNumber:账户接入号 + clientChannelNumber:下发拓展号 = srcNumber
        submit.setSrcNumber(channel.get("channelNumber") + "" +chosenChannel.get("clientChannelNumber"));

        //9.声明队列名称(一个通道对应一个队列)并构建队列
        String queueName= null;
        try {
            queueName = RabbitMQConstant.SMS_GATEWAY+submit.getChannelId();
            amqpAdmin.declareQueue(QueueBuilder.durable(queueName).build());
        } catch (Exception e) {
            log.info("【策略模块-路由策略】 声明通道对应队列以及发送消息时出现了问题");
            submit.setErrorMsg(e.getMessage());
            errorSendMessageUtil.sendWriteLog(submit);
            errorSendMessageUtil.sendPushReport(submit);
            throw new StrategyException(e.getMessage(),ExceptionEnums.UNKNOWN_ERROR.getCode());
        }

        //10.发送消息到声明好的队列中
        rabbitTemplate.convertAndSend(queueName,submit);
    }
}
