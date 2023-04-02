package com.wzy.api.common;

import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateTime;
import cn.hutool.jwt.JWT;
import cn.hutool.jwt.JWTUtil;
import cn.hutool.jwt.RegisteredPayload;
import cn.hutool.jwt.signers.JWTSigner;
import cn.hutool.jwt.signers.JWTSignerUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * @author YukeSeko
 */
@Component
@Slf4j
public class TokenUtils {

    @Autowired
    private RedisTemplate<String,String> redisTemplate;

    private final byte[] data = "API.YukeSeko.WZY".getBytes();
    //签名
    private final JWTSigner signer = JWTSignerUtil.hs512(data);
    private final String TOKEN_PREFIX = "api:token:string:";

    /**
     * 生成token
     * @param id
     * @param userAccount
     * @return
     */
    public String generateToken(String id,String userAccount){
        DateTime now = DateTime.now();
        DateTime newTime = now.offsetNew(DateField.HOUR, 720); //过期时间720小时
        Map<String, Object> payload  = new HashMap<String, Object>() {
            private static final long serialVersionUID = 1L;

            {
                put("id", id);
                put("userAccount",userAccount);
                //签发时间
                put(RegisteredPayload.ISSUED_AT, now);
                //过期时间
                put(RegisteredPayload.EXPIRES_AT, newTime);
            }
        };
        String token = JWTUtil.createToken(payload, signer);
        return token;
    }


    /**
     * 验证token是否合法
     * @param token
     * @return
     */
    public boolean verifyToken(String token){
        try {
            JWT jwt = JWTUtil.parseToken(token);
            String algorithm = jwt.getAlgorithm();
            if ( null == algorithm || !"HS512".equals(algorithm)){
                return false;
            }
            boolean verifyKey = jwt.setSigner(signer).verify();
            if (!verifyKey ){
                return false;
            }
            return true;
        }catch (Exception e){
            log.error("verifyKey方法error--->{}",String.valueOf(e));
            return false;
        }
    }

    /**
     * 验证token是否过期
     * @param token
     * @return
     */
    public  boolean verifyTime (String token){
        JWT jwt = JWTUtil.parseToken(token);
        boolean verifyTime = jwt.validate(0);
        if (verifyTime){
            return false;
        }
        return true;
    }

    /**
     * 刷新token
     * @param token
     * @return
     */
    public String refreshToken(String token){
        JWT jwt = JWTUtil.parseToken(token);
        String userAccount = (String) jwt.getPayload("userAccount");
        String id = (String) jwt.getPayload("id");
        String generateToken = generateToken(id, userAccount);
        return generateToken;
    }
}
