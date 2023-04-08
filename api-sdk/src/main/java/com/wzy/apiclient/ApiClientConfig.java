package com.wzy.apiclient;


import com.wzy.apiclient.client.ApiClient;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import java.lang.reflect.Constructor;

@Data
@ComponentScan
@Configuration
@ConfigurationProperties("api.client")  //加上前缀 api.client
public class ApiClientConfig {

    private Integer appId;
    private String accessKey;
    private String secretKey;

    @Bean
    public ApiClient apiClient() {
        ApiClient client = null;
        try {
            Class<?> forName = Class.forName("com.wzy.apiclient.client.ApiClient");
            Constructor<?> declaredConstructor = forName.getDeclaredConstructor(Integer.class, String.class, String.class);
            declaredConstructor.setAccessible(true);
            client = (ApiClient) declaredConstructor.newInstance(appId, accessKey, secretKey);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return client;
    }
}
