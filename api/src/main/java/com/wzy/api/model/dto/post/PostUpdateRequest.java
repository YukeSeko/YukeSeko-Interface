package com.wzy.api.model.dto.post;

import lombok.Data;

import java.io.Serializable;

/**
 * 更新请求
 *
 * @TableName product
 */
@Data
public class PostUpdateRequest implements Serializable {

    /**
     * id
     */
    private long id;

    /**
     * 年龄
     */
    private Integer age;

    /**
     * 性别（0-男, 1-女）
     */
    private Integer gender;

    /**
     * 学历
     */
    private String education;

    /**
     * 地点
     */
    private String place;

    /**
     * 职业
     */
    private String job;

    /**
     * 联系方式
     */
    private String contact;

    /**
     * 感情经历
     */
    private String loveExp;

    /**
     * 内容（个人介绍）
     */
    private String content;

    /**
     * 照片地址
     */
    private String photo;

    /**
     * 状态（0-待审核, 1-通过, 2-拒绝）
     */
    private Integer reviewStatus;

    /**
     * 审核信息
     */
    private String reviewMessage;

    private static final long serialVersionUID = 1L;
}