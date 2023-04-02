package com.wzy.api.model.dto.user;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户注册请求体
 *
 * 
 */
@Data
public class UserRegisterRequest implements Serializable {
    /**
     * 用户账号
     */
    private String userAccount;
    /**
     * 密码
     */
    private String userPassword;
    /**
     * 确认密码
     */
    private String checkPassword;
    /**
     * 用户手机号
     */
    private String mobile;
    /**
     * 手机验证码
     */
    private String code;
    /**
     * 图形验证码
     */
    private String captcha;
}
