package com.msb.api.config;

import com.msb.common.constant.RabbitMQConstant;
import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 构建队列和交换机
 */
@Configuration
public class RabbitMQConfig {

    /**
     * 接口模块发消息到策略模块的队列
     * @return
     */
    @Bean
    public Queue preSendQueue(){
        return QueueBuilder.durable(RabbitMQConstant.SMS_PRE_SEND).build();
    }
}
