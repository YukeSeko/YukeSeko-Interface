package com.wzy.order.model.to;

import lombok.Data;

import java.io.Serializable;

/**
 * 取消订单dto
 * @author YukeSeko
 */
@Data
public class ApiOrderCancelDto implements Serializable {

    /**
     * 接口id
     */
    private Long interfaceId;

    /**
     * 订单号
     */
    private String orderSn;

    /**
     * 购买数量
     */
    private Long orderNum;
}
