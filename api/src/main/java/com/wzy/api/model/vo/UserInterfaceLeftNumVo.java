package com.wzy.api.model.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户接口剩余调用次数vo
 * @author YukeSeko
 */
@Data
public class UserInterfaceLeftNumVo implements Serializable {
    /**
     * 主键
     */
    private Long id;

    /**
     * 名称
     */
    private String name;

    /**
     * 描述
     */
    private String description;
    /**
     * 接口地址
     */
    private String url;

    /**
     * 请求类型
     */
    private String method;

    /**
     * 剩余调用次数
     */
    private int leftNum;

    /**
     * 计费规则（元/条）
     */
    private Double charging;

    /**
     * 接口剩余可调用次数
     */
    private String availablePieces;
}
