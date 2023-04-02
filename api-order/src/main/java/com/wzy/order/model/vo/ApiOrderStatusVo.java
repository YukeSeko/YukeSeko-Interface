package com.wzy.order.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @author YukeSeko
 */
@Data
public class ApiOrderStatusVo implements Serializable {

    /**
     * 接口id
     */
    private Long interfaceId;
    /**
     * 接口名称
     */
    private String name;

    /**
     * 接口描述
     */
    private String description;

    /**
     * 订单号
     */
    private String orderSn;

    /**
     * 购买数量
     */
    private Long orderNum;

    /**
     * 交易状态【0->待付款；1->已完成；2->无效订单】
     */
    private Integer status;
    /**
     * 交易金额
     */
    private Double totalAmount;
    /**
     * 支付宝交易凭证号
     */
    private String tradeNo;

    /**
     * 买家付款时间
     */
    private Date gmtPayment;

    /**
     * 订单创建时间
     */
    private Date createTime;

    /**
     * 过期时间
     */
    private Date expirationTime;

    /**
     * 单价
     */
    private Double charging;
}
