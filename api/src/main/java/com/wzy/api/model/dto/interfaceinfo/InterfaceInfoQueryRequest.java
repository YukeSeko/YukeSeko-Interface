package com.wzy.api.model.dto.interfaceinfo;

import common.PageRequest;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 查询请求
 *
 * @author YukeSeko
 */
@Data
public class InterfaceInfoQueryRequest extends PageRequest implements Serializable {

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
     * 请求参数
     */
    private String requestParams;
    /**
     * 请求头
     */
    private String requestHeader;

    /**
     * 响应头
     */
    private String responseHeader;

    /**
     * 接口状态（0-关闭，1-开启）
     */
    private Integer status;

    /**
     * 请求类型
     */
    private String method;

    /**
     * 创建人
     */
    private Long userId;

    /**
     * 计费规则（元/条）
     */
    private Double charging;

    /**
     * 接口剩余可调用次数
     */
    private String availablePieces;
    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

}