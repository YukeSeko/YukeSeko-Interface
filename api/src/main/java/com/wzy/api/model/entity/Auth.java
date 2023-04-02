package com.wzy.api.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 
 * @TableName auth
 */
@TableName(value ="auth")
@Data
public class Auth implements Serializable {
    /**
     * id
     */
    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 用户id
     */
    private Long userid;

    /**
     * 用户账号
     */
    private String useraccount;

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
     * 应用token
     */
    private String token;

    /**
     * api的状态(0-启用，1-未启用)
     */
    private Integer status;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 是否删除(0-未删, 1-已删)
     */
    @TableLogic
    private Integer isDelete;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}