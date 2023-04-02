package com.wzy.api.common;

import cn.hutool.crypto.digest.Digester;
import cn.hutool.jwt.JWTUtil;
import cn.hutool.jwt.signers.JWTSigner;
import cn.hutool.jwt.signers.JWTSignerUtil;
import com.wzy.api.constant.AuthConstant;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @author YukeSeko
 */
@Component
public class GenerateAuthUtils {

    private final JWTSigner signer = JWTSignerUtil.hs512(AuthConstant.key);

    /**
     * 生成 accessKey
     *
     * @return
     */
    public String accessKey(String appId) {
        Digester digester = new Digester(AuthConstant.algorithm);
        return digester.digestHex(appId);
    }

    /**
     * 生成 secretKey
     *
     * @param userAccount
     * @return
     */
    public String secretKey(String appId,String userAccount) {
        String s = appId + userAccount;
        UUID uuid = UUID.nameUUIDFromBytes(s.getBytes());
        String uid = uuid.toString().replaceAll("-", "");
        Map<String, Object> secMap = new HashMap<String, Object>() {
            private static final long serialVersionUID = 1L;

            {
                put("uuid", uid);
            }
        };
        return JWTUtil.createToken(secMap, signer);
    }

    public String token(String appId,String userAccount, String accessKey, String secretKey) {
        Map<String, Object> map = new HashMap<String, Object>() {
            private static final long serialVersionUID = 1L;
            {
                put("appId", appId);
                put("userAccount", userAccount);
                put("accessKey", accessKey);
                put("secretKey", secretKey);
            }
        };
        //生成token，并设置签名
        return JWTUtil.createToken(map, signer);
    }
}
