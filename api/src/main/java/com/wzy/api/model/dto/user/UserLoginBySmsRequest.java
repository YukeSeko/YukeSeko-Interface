package com.wzy.api.model.dto.user;

import lombok.Data;

import java.io.Serializable;

/**
 * @author YukeSeko
 * 通过手机号登录
 */
@Data
public class UserLoginBySmsRequest implements Serializable {
    /**
     * 手机号
     */
    private String mobile;

    /**
     * 验证码
     */
    private String code;
}
