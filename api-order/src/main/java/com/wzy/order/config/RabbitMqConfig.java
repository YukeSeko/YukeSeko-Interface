package com.wzy.order.config;

import common.constant.RabbitMqConstant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * @author YukeSeko
 * RabbitMQ配置
 */
@Slf4j
@Configuration
public class RabbitMqConfig {

    /**
     * 普通队列
     * @return
     */
    @Bean
    public Queue orderQueue(){
        Map<String, Object> arguments = new HashMap<>();
        //声明死信队列和交换机
        arguments.put("x-dead-letter-exchange", RabbitMqConstant.order_exchange);
        arguments.put("x-dead-letter-routing-key", "order.release");
        arguments.put("x-message-ttl", 30*60000); // 订单过期时间为30分钟
        return new Queue(RabbitMqConstant.order_queue,true,false,false ,arguments);
    }

    /**
     * 死信队列：消息重试三次后放入死信队列
     * @return
     */
    @Bean
    public Queue deadLetter(){
        return new Queue(RabbitMqConstant.order_delay_queue, true, false, false);
    }

    /**
     * 交换机
     * @return
     */
    @Bean
    public Exchange orderExchange() {
        return new TopicExchange(RabbitMqConstant.order_exchange, true, false);
    }


    /**
     * 交换机和普通队列绑定
     * @return
     */
    @Bean
    public Binding orderBinding(){
        return new Binding(RabbitMqConstant.order_queue, Binding.DestinationType.QUEUE,RabbitMqConstant.order_exchange,"order-send",null);
    }

    /**
     * 交换机和死信队列绑定
     * @return
     */
    @Bean
    public Binding orderDelayBinding(){
        return new Binding(RabbitMqConstant.order_delay_queue, Binding.DestinationType.QUEUE,RabbitMqConstant.order_exchange,"order.release",null);
    }


}
