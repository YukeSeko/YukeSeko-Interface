package com.wzy.thirdParty.config;

import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 返回支付宝客户端
 * @author YukeSeko
 */
@Configuration
public class AliPayClientConfig {
    @Value("${alipay.CHARSET}")
    public String CHARSET ;

    @Value("${alipay.SIGN_TYPE}")
    public String SIGN_TYPE;

    @Value("${alipay.APP_ID}")
    private String APP_ID ;

    @Value("${alipay.PRIVATE_KEY}")
    public String PRIVATE_KEY;

    @Value("${alipay.ALIPAY_PUBLIC_KEY}")
    public String ALIPAY_PUBLIC_KEY;

    @Value("${alipay.ALIPAY_GATEWAY}")
    private String ALIPAY_GATEWAY ;

    @Value("${alipay.NotifyUrl}")
    public String NotifyUrl ;

    @Value("${alipay.tradeSuccessUrl}")
    public String tradeSuccessUrl ;

    @Bean
    public AlipayClient alipayClient(){
        return new DefaultAlipayClient(ALIPAY_GATEWAY, APP_ID, PRIVATE_KEY, "json", CHARSET, ALIPAY_PUBLIC_KEY, SIGN_TYPE);
    }
}
