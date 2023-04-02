package com.wzy.api.model.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * @author YukeSeko
 */
@Data
public class AuthVo implements Serializable {

    /**
     * 客户端应用id
     */
    private Integer appid;

    /**
     *  accessKey
     */
    private String accesskey;

    /**
     * secretKey
     */
    private String secretkey;

    /**
     * 接口状态(0-启用, 1-未启用)
     */
    private Integer  status;;

}
