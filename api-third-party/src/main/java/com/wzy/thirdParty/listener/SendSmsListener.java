package com.wzy.thirdParty.listener;

import com.rabbitmq.client.Channel;
import com.tencentcloudapi.sms.v20210111.models.SendSmsResponse;
import com.tencentcloudapi.sms.v20210111.models.SendStatus;
import com.wzy.thirdParty.Tencent.SendSmsUtils;
import common.constant.RabbitMqConstant;
import common.to.SmsTo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * @author YukeSeko
 * 发送短信验证码
 */
@Slf4j
@Component
public class SendSmsListener  {

    @Autowired
    private SendSmsUtils sendSmsUtils;

    @Autowired
    private RedisTemplate redisTemplate;


    /**
     * 监听普通队列 - 实际发送短信
     * 出现异常，使用消息重传。
     * @param sms
     * @param message
     * @param channel
     * @throws IOException
     */
    @RabbitListener(queues = "api-sms-queue")
    public void listener(SmsTo sms, Message message, Channel channel) throws IOException {
        String messageId = message.getMessageProperties().getMessageId();
        int retryCount = (int) redisTemplate.opsForHash().get(RabbitMqConstant.SMS_HASH_PREFIX+messageId, "retryCount");
        if (retryCount >= 3){
            //投递次数大于三次，放入死信队列
            log.error("重试次数大于三次");
            channel.basicReject(message.getMessageProperties().getDeliveryTag(),false);
            redisTemplate.delete(RabbitMqConstant.SMS_HASH_PREFIX + messageId);
            return;
        }
        try{
            String mobile = sms.getMobile();
            String code = sms.getCode();
            if (null == mobile || null == code){
                throw new RuntimeException("请求参数错误");
            }
            //发送验证码
            SendSmsResponse response = null;
            response = sendSmsUtils.sendSmsResponse(mobile.toString(), code);
            SendStatus[] sendStatusSet = response.getSendStatusSet();
            SendStatus sendStatus = sendStatusSet[0];
            String statusCode = sendStatus.getCode();
            String res = sendStatus.getMessage();
            if(!"OK".equals(statusCode) || "send success".equals(res)){
                throw new RuntimeException("发送验证码失败");
            }
            //手动确认消息
            channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
            log.info("发送短信成功--{}",sms);
            //发送成功后，从redis中删除该缓存
            Boolean delete = redisTemplate.delete(RabbitMqConstant.SMS_HASH_PREFIX + messageId);
        }catch (Exception e){
            //进行重试，重试次数加1
            redisTemplate.opsForHash().put(RabbitMqConstant.SMS_HASH_PREFIX+messageId,"retryCount",retryCount+1);
            channel.basicReject(message.getMessageProperties().getDeliveryTag(),true);
        }
    }

    /**
     * 监听死信队列 - 记录发送短信失败后的日志
     * （可以记录日志，入库，人工干预处理）
     * @param sms
     * @param message
     * @param channel
     * @throws IOException
     */
    @RabbitListener(queues = "sms.delay.queue")
    public void delayListener(SmsTo sms, Message message, Channel channel) throws IOException {
        try{
            log.error("监听到死信队列消息==>{}",sms);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
        }catch (Exception e){
            channel.basicReject(message.getMessageProperties().getDeliveryTag(),true);
        }
    }
}
