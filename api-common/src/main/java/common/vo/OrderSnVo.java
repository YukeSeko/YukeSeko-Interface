package common.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @author YukeSeko
 */
@Data
public class OrderSnVo implements Serializable {

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
     * 创建时间
     */
    private Date createTime;

    /**
     * 过期时间
     */
    private Date expirationTime;
}
