package com.wzy.thirdParty.common;

import common.constant.RabbitMqConstant;
import common.constant.RedisConstant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * 向mq延时队列中发送订单消息
 * @author YukeSeko
 */
@Slf4j
@Component
public class RabbitOrderPaySuccessUtils{


    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    private String finalId = null;

    /**
     * 向mq发送订单消息
     * @param orderSn
     */
    public void sendOrderPaySuccess(String orderSn){
        finalId = orderSn;
        redisTemplate.opsForValue().set(RedisConstant.ORDER_PAY_SUCCESS_INFO+orderSn,orderSn);
        String finalMessageId = UUID.randomUUID().toString();
        rabbitTemplate.convertAndSend(RabbitMqConstant.order_exchange,"order.pay.success",orderSn, message -> {
            MessageProperties messageProperties = message.getMessageProperties();
            //生成全局唯一id
            messageProperties.setMessageId(finalMessageId);
            messageProperties.setContentEncoding("utf-8");
            return message;
        });
    }
}
