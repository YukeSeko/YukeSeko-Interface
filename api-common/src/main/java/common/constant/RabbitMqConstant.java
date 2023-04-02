package common.constant;

/**
 * rabbitmq 常量
 * @author YukeSeko
 */
public class RabbitMqConstant {
    public static final String sms_exchange= "sms-exchange";

    public static final String sms_routingKey= "sms-send";

    public static final String MQ_PRODUCER="api:mq:producer:fail";

    public static final String SMS_HASH_PREFIX = "api:sms_hash_";

    public static final String order_exchange = "order.exchange";

    public static final String order_delay_queue = "order.delay.queue";

    public static final String order_queue = "api-order-queue";

    public static final String order_pay_success = "order-pay-success";

}
