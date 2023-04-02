package com.wzy.thirdParty.config;

import common.constant.RabbitMqConstant;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ配置
 * @author YukeSeko
 */
@Configuration
public class RabbitMqConfig {

    /**
     * 普通队列，订单支付成功队列
     * @return
     */
    @Bean
    public Queue orderPaySuccess(){
        return new Queue(RabbitMqConstant.order_pay_success, true, false, false);
    }

    /**
     * 交换机和队列绑定
     * @return
     */
    @Bean
    public Binding orderPaySuccessBinding(){
        return new Binding(RabbitMqConstant.order_pay_success, Binding.DestinationType.QUEUE,RabbitMqConstant.order_exchange,"order.pay.success",null);
    }
}
