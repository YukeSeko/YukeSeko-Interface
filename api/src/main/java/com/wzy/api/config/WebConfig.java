package com.wzy.api.config;

import com.wzy.api.interceptor.AuthenticationInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;
import java.util.List;

/**
 * @author YukeSeko
 * 注册拦截器
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {


    /**
     * 放行接口
     */
    private List<String> pathPatterns = Arrays.asList("/userInterfaceInfo/updateUserLeftNum","/user/checkUserLogin","/user/oauth2/**","/user/captcha", "/user/register","/user/login","/user/loginBySms","/user/getCaptcha","/v3/api-docs","/user/logoutSuccess","/user/getpassusertype","/user/sendPassUserCode","/user/authPassUserCode","/user/updateUserPass");

    /**
     * 放行静态资源
     */
    private List<String> staticPath = Arrays.asList("/charging/**","/swagger-ui.html", "/swagger-ui/*", "/swagger-resources/**", "/v2/api-docs", "/v3/api-docs", "/webjars/**","/doc.html");

    @Autowired
    private AuthenticationInterceptor authenticationInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authenticationInterceptor)
                .excludePathPatterns(pathPatterns)
                .excludePathPatterns(staticPath);
    }
}
