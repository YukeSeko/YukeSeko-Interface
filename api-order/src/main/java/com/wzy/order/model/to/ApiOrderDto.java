package com.wzy.order.model.to;

import lombok.Data;

import java.io.Serializable;

/**
 * 创建订单dto
 * @author YukeSeko
 */
@Data
public class ApiOrderDto implements Serializable {

    /**
     * 接口id
     */
    private Long interfaceId;

    /**
     * 用户id
     */
    private Long userId;

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
}
