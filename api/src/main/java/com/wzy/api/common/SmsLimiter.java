package com.wzy.api.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * @author YukeSeko
 * 短信发送限制
 */
@Component
@Slf4j
public class SmsLimiter {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private RedisTokenBucket redisTokenBucket;

    private final String SMS_PREFIX = "sms:";
    private final String CODE_PREFIX = "code:";
    private final long CODE_EXPIRE_TIME = 300; // 5分钟

    /**
     * 尝试获取一个令牌，如果成功了，那么返回true ，失败返回false，表示限流
     * @param phoneNumber
     * @param code
     * @return
     */
    public boolean sendSmsAuth(String phoneNumber, String code) {
        if (redisTokenBucket.tryAcquire(SMS_PREFIX + phoneNumber)) {
            // 通过验证验证后，向redis中写入数据
            String key = CODE_PREFIX + phoneNumber;
            redisTemplate.opsForValue().set(key, code, CODE_EXPIRE_TIME, TimeUnit.SECONDS);
            return true;
        } else {
            log.info("send sms to " + phoneNumber + " rejected due to rate limiting");
            return false;
        }
    }

    /**
     * 验证手机号对应的验证码是否正确
     * @param phoneNumber
     * @param code
     * @return
     */
    public boolean verifyCode(String phoneNumber, String code) {
        String key = CODE_PREFIX + phoneNumber;
        String value = redisTemplate.opsForValue().get(key);
        if (value != null && value.equals(code)) {
            redisTemplate.delete(key);
            return true;
        }
        return false;
    }
}
