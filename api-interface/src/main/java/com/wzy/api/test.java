package com.wzy.api;

import cn.hutool.crypto.digest.DigestAlgorithm;
import cn.hutool.crypto.digest.Digester;
import cn.hutool.jwt.JWT;
import cn.hutool.jwt.JWTUtil;
import cn.hutool.jwt.signers.JWTSigner;
import cn.hutool.jwt.signers.JWTSignerUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


/**
 * ak 、sk 生成算法
 */
public class test {
    public static void main(String[] args) {
        final JWTSigner signer = JWTSignerUtil.hs512("wzyApiAuth".getBytes());

        Digester digester = new Digester(DigestAlgorithm.SHA512);
        String accessKey = digester.digestHex("12345"); //应用公钥，即appId

        //通过用户的appid和账号生成secretKey
        String s = "12345" +"admin" ;
        UUID uuid = UUID.nameUUIDFromBytes(s.getBytes());
        String uid = uuid.toString().replaceAll("-", "");
        Map<String, Object> secMap = new HashMap<String, Object>() {
            private static final long serialVersionUID = 1L;
            {
                put("uuid", uid);
            }
        };
        String secretKey = JWTUtil.createToken(secMap, signer);

        Map<String, Object> map = new HashMap<String, Object>() {
            private static final long serialVersionUID = 1L;
            {
                put("appId", "12345");
                put("userAccount","admin");
                put("accessKey",accessKey);
                put("secretKey",secretKey);
            }
        };
        String token = JWTUtil.createToken(map, signer);//生成token，并设置签名
        System.out.println("token："+token);


        final JWT jwt = JWTUtil.parseToken(token);
        System.out.println("accessKey："+jwt.getPayload("accessKey"));
        System.out.println("secretKey："+jwt.getPayload("secretKey"));
    }

}
