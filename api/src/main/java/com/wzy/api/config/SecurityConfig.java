package com.wzy.api.config;

import com.wzy.api.common.SimpleAccessDeniedHandler;
import com.wzy.api.common.SimpleAuthenticationEntryPoint;
import com.wzy.api.common.UserDetailsImpl;
import common.constant.CookieConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.session.HttpSessionEventPublisher;

/**
 * @author YukeSeko
 */
@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private String[] pathPatterns = {"/user/oauth2/**", "/user/register","/user/login","/user/loginBySms","/v3/api-docs","/user/logoutSuccess","/user/getpassusertype","/user/sendPassUserCode","/user/authPassUserCode","/user/updateUserPass"};

    private String[] adminPath = {"/user/list/page",
            "/user/list",
            "/userInterfaceInfo/add",
            "/userInterfaceInfo/delete",
            "/userInterfaceInfo/update",
            "/userInterfaceInfo/get",
            "/userInterfaceInfo/list",
            "/userInterfaceInfo/list/page",
            "/interfaceInfo/list",
            "/interfaceInfo/list/AllPage",
            "/interfaceInfo/online",
            "/interfaceInfo/online",};

    @Autowired
    private SimpleAuthenticationEntryPoint simpleAuthenticationEntryPoint;

    @Autowired
    private SimpleAccessDeniedHandler simpleAccessDeniedHandler;

    @Autowired
    @Lazy
    private UserDetailsImpl userDetails;

    /**
     * 配置PasswordEncoder
     * @return
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }


    /**
     * 防止session 清理用户不及时
     * @return
     */
    @Bean
    HttpSessionEventPublisher httpSessionEventPublisher() {
        return new HttpSessionEventPublisher();
    }

    /**
     * 让admin继承user的所有权限
     * @return
     */
    @Bean
    RoleHierarchy roleHierarchy(){
        RoleHierarchyImpl roleHierarchy = new RoleHierarchyImpl();
        String hierarchy = "ROLE_admin > ROLE_user";
        roleHierarchy.setHierarchy(hierarchy);
        return roleHierarchy;
    }


    /**
     * 放行静态资源
     * @param web
     * @throws Exception
     */
    @Override
    public void configure(WebSecurity web) throws Exception {
        //所需要用到的静态资源，允许访问
        web.ignoring().antMatchers( "/swagger-ui.html",
                "/swagger-ui/*",
                "/swagger-resources/**",
                "/v2/api-docs",
                "/v3/api-docs",
                "/webjars/**",
                "/doc.html");
    }
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                //允许跨域
                .cors()
                .and()
                //关闭csrf
                .csrf().disable()
                .authorizeRequests()
                // 管理员才可访问的接口
                .antMatchers(adminPath).hasRole("admin")
                // 对于登录接口 允许匿名访问.anonymous()，即未登陆时可以访问，登陆后携带了token就不能再访问了
                .antMatchers(pathPatterns).anonymous()
                .antMatchers("/userInterfaceInfo/updateUserLeftNum","/user/checkUserLogin","/user/getCaptcha","/user/captcha","/charging/**").permitAll()
                // 除上面外的所有请求全部需要鉴权认证,.authenticated()表示认证之后可以访问
                .anyRequest().authenticated();
        //注册自定义异常响应
        http.exceptionHandling()
                .accessDeniedHandler(simpleAccessDeniedHandler)
                .authenticationEntryPoint(simpleAuthenticationEntryPoint);
        //开启配置注销登录功能
        http.logout()
                .logoutUrl("/user/logout") //指定用户注销登录时请求访问的地址
                .deleteCookies(CookieConstant.headAuthorization)//指定用户注销登录后删除的 Cookie。
                .deleteCookies(CookieConstant.autoLoginAuthCheck)
                .logoutSuccessUrl("http://122.9.148.119:88/api/user/logoutSuccess");//指定退出登录后跳转的地址
        //每个浏览器最多同时只能登录1个用户
        http.sessionManagement()
                .maximumSessions(1)
                .maxSessionsPreventsLogin(true);

    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService()).passwordEncoder(passwordEncoder());
    }
}
