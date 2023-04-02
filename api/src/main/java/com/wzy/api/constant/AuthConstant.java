package com.wzy.api.constant;


import cn.hutool.crypto.digest.DigestAlgorithm;

/**
 * 接口验证信息
 */
public interface AuthConstant {
    byte[] key = "wzyApiAuth".getBytes();

    DigestAlgorithm algorithm = DigestAlgorithm.SHA512;


}
