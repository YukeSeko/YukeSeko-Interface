package com.wzy.api.schedule;

import cn.hutool.json.JSONUtil;
import com.wzy.api.common.RabbitUtils;
import common.constant.LockConstant;
import common.constant.RabbitMqConstant;
import common.to.SmsTo;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * @author YukeSeko
 */
@Slf4j
@Component
@EnableAsync
@EnableScheduling
public class sendFailedSmsSchedule {

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private RabbitUtils rabbitUtils;



    /**
     * 每30s，从生产者redis中重新发送发送短信失败的消息
     */
    @Scheduled(cron = "*/30 * * * * ?")
    public void sendFailedSms(){
        RLock lock = redissonClient.getLock(LockConstant.sms_fail_lock);
        try {
            // 为加锁等待20秒时间，并在加锁成功10秒钟后自动解开
            boolean tryLock = lock.tryLock(20, 10, TimeUnit.SECONDS);
            if (tryLock){
                Map entries = redisTemplate.opsForHash().entries(RabbitMqConstant.MQ_PRODUCER);
                Set set = entries.keySet();
                for (Object key : set) {
                    Object o = entries.get(key);
                    SmsTo smsTo = JSONUtil.toBean(JSONUtil.toJsonStr(o), SmsTo.class);
                    rabbitUtils.sendSms(smsTo);
                    redisTemplate.opsForHash().delete(RabbitMqConstant.MQ_PRODUCER,key);
                }
            }
        } catch (InterruptedException e) {
            log.error("===定时任务:获取失败生产者发送消息redis出现bug===");
            throw new RuntimeException(e);
        }finally {
            lock.unlock();
        }
    }

    /**
     * 定时任务：每分钟从Redis中查询状态未：已投递，但长时间未消费的消息.
     * 将未消费的消息，取出然后重新发送短信
     */
    @Scheduled(cron = "*/60 * * * * ?")
    public void delSuccessSms(){
        RLock lock = redissonClient.getLock(LockConstant.sms_waitToLong_lock);
        try {
            boolean tryLock = lock.tryLock(20, 10, TimeUnit.SECONDS);
            if (tryLock){
                Set keys = redisTemplate.keys(RabbitMqConstant.SMS_HASH_PREFIX+"*");
                for (Object key : keys) {
                    int status = (int) redisTemplate.opsForHash().get(key.toString(), "status");
                    Long expire = redisTemplate.opsForHash().getOperations().getExpire(key.toString());
                    if (status == 1 && expire <480){
                        SmsTo smsTo = (SmsTo) redisTemplate.opsForHash().get(key.toString(), "smsTo");
                        redisTemplate.delete(key.toString());
                        log.info("消息 {} 长时间未消费",smsTo);
                        rabbitUtils.sendSms(smsTo);
                    }
                }
            }
        } catch (InterruptedException e) {
            log.error("===定时任务：获取长时间未消费出现bug===");
            throw new RuntimeException(e);
        }finally {
            lock.unlock();
        }
    }
}
