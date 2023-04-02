package com.wzy.api.common;

import com.tencentcloudapi.common.Credential;
import com.tencentcloudapi.sms.v20210111.SmsClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
/**
 * @author YukeSeko
 */
@Component
public class TencentClient {

    @Value("${tencent.secretId}")
    private String secretId;

    @Value("${tencent.secretKey}")
    private String secretKey;

    /**
     * Tencent应用客户端
     * @return
     */
    @Bean
    public SmsClient client(){
        Credential cred = new Credential(secretId, secretKey);
        SmsClient smsClient = new SmsClient(cred, "ap-guangzhou");
        return smsClient;
    }
}
