package common.constant;

/**
 * cookie 过期时间
 * @author YukeSeko
 */
public class CookieConstant {


    public static final String headAuthorization = "authorization";

    public static final String autoLoginAuthCheck = "_Wu2ia_remember";

    public static final int expireTime = 2592000 ;//30天过期

    public static final String orderToken = "api-order-token";

    public static final int orderTokenExpireTime = 1800; // 30分钟过期
    public static final byte[] autoLoginKey = "Wzy-ApiAutoLogin".getBytes();
}
