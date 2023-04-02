package com.wzy.api.util;

import cn.hutool.crypto.digest.DigestAlgorithm;
import cn.hutool.crypto.digest.Digester;
import cn.hutool.jwt.JWT;
import cn.hutool.jwt.JWTUtil;

import cn.hutool.jwt.signers.JWTSigner;
import cn.hutool.jwt.signers.JWTSignerUtil;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.UUID;

@Component
public class JwtUtils {

    /**
     * 生成签名
     */
    private final JWTSigner signer = JWTSignerUtil.hs512("wzyApiAuth".getBytes());

    /**
     * 验证全部参数是否正确
     * @param token
     * @param accessKey
     * @param secretKey
     * @return
     */
    public boolean isToken(String appId, String token, String accessKey, String secretKey) {
        if (StringUtils.hasLength(appId) && StringUtils.hasLength(token) && StringUtils.hasLength(accessKey) && StringUtils.hasLength(secretKey)) {
            if(!JWTUtil.verify(token,signer)){
                return false;
            }
            if(!isAccessKey(appId, accessKey)){
                return false;
            }
            final JWT jwt = JWTUtil.parseToken(token);
            String payloadId = (String) jwt.getPayload("appId");
            String userAccount = (String) jwt.getPayload("userAccount");
            String payloadAccessKey = (String) jwt.getPayload("accessKey");
            String payloadSecretKey = (String) jwt.getPayload("secretKey");
            if (!appId.equals(payloadId) || !accessKey.equals(payloadAccessKey) || !secretKey.equals(payloadSecretKey)) {
                return false;
            }
            if(!isSecretKey(appId,userAccount,secretKey)){
                return false;
            }
            return true;
        }
        return false;
    }

    /**
     * 验证公钥是否正确
     * @param appId
     * @param accessKey
     * @return
     */
    private boolean isAccessKey(String appId, String accessKey){
        Digester digester = new Digester(DigestAlgorithm.SHA512);
        String digestHex = digester.digestHex(appId);
        if(!digestHex.equals(accessKey)){
            return false;
        }
        return true;
    }

    /**
     * 验证用户的私钥是否正确
     * @param appId
     * @param userAccount
     * @param secretKey
     * @return
     */
    private boolean isSecretKey(String appId,String userAccount,String secretKey){
        if (!JWTUtil.verify(secretKey, signer)) {
            return false;
        }
        String s = appId + userAccount;
        UUID uuid = UUID.nameUUIDFromBytes(s.getBytes());
        String uid = uuid.toString().replaceAll("-", "");

        final JWT jwt = JWTUtil.parseToken(secretKey);
        String payload = (String) jwt.getPayload("uuid");
        if(!payload.equals(uid)){
            return false;
        }
        return true;
    }
}
