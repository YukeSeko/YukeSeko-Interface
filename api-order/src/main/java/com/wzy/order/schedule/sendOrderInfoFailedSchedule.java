package com.wzy.order.schedule;

import com.wzy.order.common.RabbitOrderUtils;
import com.wzy.order.model.entity.ApiOrder;
import common.constant.LockConstant;
import common.constant.RedisConstant;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * @author YukeSeko
 */
@Slf4j
@Component
@EnableAsync
@EnableScheduling
public class sendOrderInfoFailedSchedule {

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private RedisTemplate redisTemplate;


    @Autowired
    private RabbitOrderUtils rabbitOrderUtils;

    /**
     * 消息可靠性保证（发送端可靠性保证）
     * 每分钟从生产者redis中重新发送发送短信失败的消息
     */
    @Scheduled(cron = "*/60 * * * * ?")
    public void sendFailedOrderInfo(){
        RLock lock = redissonClient.getLock(LockConstant.order_fail_lock);
        try {
            // 为加锁等待20秒时间，并在加锁成功10秒钟后自动解开
            boolean tryLock = lock.tryLock(20, 10, TimeUnit.SECONDS);
            if (tryLock){
                //重新向mq中发送订单消息
                Set keys = redisTemplate.keys(RedisConstant.SEND_ORDER_PREFIX + "*");
                for (Object key : keys) {
                    ApiOrder apiOrder = (ApiOrder) redisTemplate.opsForValue().get(key);
                    //删除reids中的该条记录
                    redisTemplate.delete(key.toString());
                    rabbitOrderUtils.sendOrderSnInfo(apiOrder);
                }
            }
        } catch (InterruptedException e) {
            log.error("===定时任务:获取失败生产者发送消息redis出现bug===");
            throw new RuntimeException(e);
        }finally {
            lock.unlock();
        }
    }
}
