package com.wzy.order.model.to;

import common.PageRequest;
import lombok.Data;

import java.io.Serializable;

/**
 * @author YukeSeko
 */
@Data
public class ApiOrderStatusInfoDto extends PageRequest implements Serializable {
    /**
     * 用户id
     */
    private Long userId;

    /**
     * 交易状态【0->待付款；1->已完成；2->无效订单】
     */
    private Integer status;
}
