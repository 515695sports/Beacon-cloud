package com.msb.strategy.config;

import com.msb.common.constant.RabbitMQConstant;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 构建队列和交换机
 */
@Configuration
public class RabbitMQConfig {

    /**
     * 策略模块发消息到后台管理模块的队列
     * @return
     */
    @Bean
    public Queue mobileAreaOperatorQueue(){
        return QueueBuilder.durable(RabbitMQConstant.MOBILE_AREA_OPERATOR).build();
    }

    /**
     * 写日志到elasticsearch的队列
     * @return
     */
    @Bean
    public Queue writeLogQueue(){
        return QueueBuilder.durable(RabbitMQConstant.SMS_WRITE_LOG).build();
    }

    /**
     * 推送状态报告的队列
     * @return
     */
    @Bean
    public Queue pubshReportQueue(){
        return QueueBuilder.durable(RabbitMQConstant.SMS_PUSH_REPORT).build();
    }

}
