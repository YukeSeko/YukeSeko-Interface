package com.wzy.api.common;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * @author YukeSeko
 */
@Component
public class RedisTokenBucket {

    @Autowired
    private RedisTemplate<String,String> redisTemplate;

    private final long EXPIRE_TIME = 400; //400秒后过期

    /**
     * 令牌桶算法，一分钟以内，每个手机号只能发送一次
     * @param phoneNum
     * @return
     */
    public boolean tryAcquire(String phoneNum) {
        // 每个手机号码一分钟内只能发送一条短信
        int permitsPerMinute = 1;
        // 令牌桶容量
        int maxPermits = 1;
        // 获取当前时间戳
        long now = System.currentTimeMillis();
        // 计算令牌桶内令牌数
        int tokens = Integer.parseInt(redisTemplate.opsForValue().get(phoneNum + "_tokens") == null ? "0" : redisTemplate.opsForValue().get(phoneNum + "_tokens"));
        // 计算令牌桶上次填充的时间戳
        long lastRefillTime = Long.parseLong(redisTemplate.opsForValue().get(phoneNum + "_last_refill_time") == null ? "0" : redisTemplate.opsForValue().get(phoneNum + "_last_refill_time"));
        // 计算当前时间与上次填充时间的时间差
        long timeSinceLast = now - lastRefillTime;
        // 计算需要填充的令牌数
        int refill = (int) (timeSinceLast / 1000 * permitsPerMinute / 60);
        // 更新令牌桶内令牌数
        tokens = Math.min(refill + tokens, maxPermits);
        // 更新上次填充时间戳
        redisTemplate.opsForValue().set(phoneNum + "_last_refill_time", String.valueOf(now),EXPIRE_TIME, TimeUnit.SECONDS);
        // 如果令牌数大于等于1，则获取令牌
        if (tokens >= 1) {
            tokens--;
            redisTemplate.opsForValue().set(phoneNum + "_tokens", String.valueOf(tokens),EXPIRE_TIME, TimeUnit.SECONDS);
            // 如果获取到令牌，则返回true
            return true;
        }
        // 如果没有获取到令牌，则返回false
        return false;
    }
}
