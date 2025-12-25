package com.msb.strategy.mq;

import com.msb.common.constant.RabbitMQConstant;
import com.msb.common.exception.StrategyException;
import com.msb.common.model.StandardSubmit;
import com.msb.strategy.filter.StrategyFilterContext;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;


/**
 * 监听接口模块推送过来的消息
 */
@Component
@Slf4j
public class PreSendListener {

    //整个策略模块的校验
    @Autowired
    private StrategyFilterContext strategyFilterContext;

    @RabbitListener(queues = RabbitMQConstant.SMS_PRE_SEND)
    public void listen(StandardSubmit standardSubmit, Message message, Channel channel) throws IOException {
        log.info("【策略模块-接收消息】 接收到接口模块发送的消息 submit={}",standardSubmit);
        //处理业务......
        try {
            strategyFilterContext.strategy(standardSubmit);
            log.info("【策略模块-消费完毕】 手动ack");
            channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
        } catch (StrategyException e) {
            log.info("【策略模块-消费失败】校验未通过, msg={}",e.getMessage());
            //避免try里面的ack没有执行
            channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
        }
    }
}
