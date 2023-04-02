package com.wzy.api.model.dto.user;

import lombok.Data;

import java.io.Serializable;

/**
 * @author YukeSeko
 */
@Data
public class UserBindPhoneRequest implements Serializable {

    /**
     * 用户id
     */
    private Long id;

    /**
     * 账号
     */
    private String userAccount;

    /**
     * 手机号
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
