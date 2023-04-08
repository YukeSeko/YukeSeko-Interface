package com.wzy.apiclient.model;

import lombok.Data;

import java.io.Serializable;

/**
 * @author YukeSeko
 */
@Data
public class Api implements Serializable {
    /**
     * 用户id
     */
    Long id;
    /**
     * 用户账号
     */
    String userAccount;
    /**
     * 接口id
     */
    String interfaceId;
    /**
     * 请求地址
     */
    String url;
    /**
     * 请求体
     */
    Object body;
    /**
     * 请求方法
     */
    String method;
}
