package common.constant;

/**
 * @author YukeSeko
 */
public class OrderConstant {

    public static final String USER_ORDER_TOKEN_PREFIX = "api:order:token";

    //【0->待付款；1->已完成；2->已关闭；3->无效订单】
    public static final int toBePaid = 0;
    public static final int finish = 1;
    public static final int close = 2;
    public static final int invalid= 3;
}