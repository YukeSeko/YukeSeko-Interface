package common.constant;

/**
 * 分布式锁常量
 * @author YukeSeko
 */
public class LockConstant {

    public static final String sms_fail_lock = "sendSms:fail:lock";

    public static final String sms_waitToLong_lock = "sendSms:wait:lock";

    public static final String interface_onlinePage_lock = "interface:onlinePage:lock";

    public static final String order_fail_lock = "sendOrderInfo:fail:lock";

    public static final String order_pay_success = "order:paySuccess:lock";
}
