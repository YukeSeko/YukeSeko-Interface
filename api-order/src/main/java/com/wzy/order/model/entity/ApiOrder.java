package com.wzy.order.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 
 * @TableName order
 */
@TableName(value ="api_order")
@Data
public class ApiOrder implements Serializable {
    /**
     * 主键:雪花算法生成id
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 接口id
     */
    private Long interfaceId;

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 订单号
     */
    private String orderSn;

    /**
     * 购买数量
     */
    private Long orderNum;

    /**
     * 单价
     */
    private Double charging;

    /**
     * 交易金额
     */
    private Double totalAmount;

    /**
     * 交易状态【0->待付款；1->已完成；2->无效订单】
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
    private Integer isDelete;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}