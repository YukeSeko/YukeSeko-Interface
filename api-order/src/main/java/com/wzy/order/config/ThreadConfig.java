package com.wzy.order.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 线程池配置
 * @author YukeSeko
 */
@Configuration
public class ThreadConfig {

    @Value("${order.thread.coreSize}")
    private Integer coreSize;
    @Value("${order.thread.maxSize}")
    private Integer maxSize;
    @Value("${order.thread.keepAliveTime}")
    private Integer keepAliveTime;

    @Bean
    public ThreadPoolExecutor threadPoolExecutor(){
        return new ThreadPoolExecutor(coreSize, maxSize, keepAliveTime, TimeUnit.SECONDS,
                new LinkedBlockingDeque<>(100000), Executors.defaultThreadFactory(),
                new ThreadPoolExecutor.AbortPolicy());
    }
}