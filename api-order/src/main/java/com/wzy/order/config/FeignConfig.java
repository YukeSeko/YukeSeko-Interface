package com.wzy.order.config;

import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

/**
 * 解决feign远程调用丢失请求头的问题
 * @author YukeSeko
 */
@Configuration
public class FeignConfig {

    @Bean("requestInterceptor")
    public RequestInterceptor requestInterceptor() {
        RequestInterceptor requestInterceptor = template -> {
            //1、使用RequestContextHolder拿到刚进来的请求数据
            ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (requestAttributes != null) {
                //老请求
                HttpServletRequest request = requestAttributes.getRequest();
                if (request != null) {
                    //2、同步请求头的数据
                    //把老请求的cookie值放到新请求上来，进行一个同步
                    String cookie = request.getHeader("Cookie");
                    template.header("Cookie", cookie);
                }
            }
        };
        return requestInterceptor;
    }
}
