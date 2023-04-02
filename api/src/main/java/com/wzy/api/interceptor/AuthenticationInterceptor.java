package com.wzy.api.interceptor;

import cn.hutool.jwt.JWT;
import cn.hutool.jwt.JWTUtil;
import com.wzy.api.common.TokenUtils;
import common.Exception.BusinessException;
import com.wzy.api.model.entity.User;
import common.ErrorCode;
import common.constant.CookieConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.security.Principal;

/**
 * @author YukeSeko
 * Security过滤器器
 */
@Component
public class AuthenticationInterceptor implements HandlerInterceptor {

    @Autowired
    private TokenUtils tokenUtils;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        Cookie[] cookies = request.getCookies();
        String authorization =null;
        for (Cookie cookie : cookies) {
            String name = cookie.getName();
            if (CookieConstant.headAuthorization.equals(name)){
                authorization = cookie.getValue();
            }
        }
        // 1、判断是否存在
        if (null == authorization){
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        // 2、验证token是否合法，判断当前登录用户和token中的用户是否相同
        boolean verifyToken = tokenUtils.verifyToken(authorization);
        if (!verifyToken){
            throw new BusinessException(ErrorCode.ILLEGAL_ERROR);
        }
        Principal userPrincipal = request.getUserPrincipal();
        String name = userPrincipal.getName();
        //从Security全局对象中获取当前登录用户
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();
        if (currentUser == null || null == name || !currentUser.getUserAccount().equals(name)){
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        JWT jwt = JWTUtil.parseToken(authorization);
        String userAccount = (String) jwt.getPayload("userAccount");
        String id = (String) jwt.getPayload("id");
        if (userAccount == null || id == null){
            throw new BusinessException(ErrorCode.ILLEGAL_ERROR);
        }
        if (!currentUser.getId().toString().equals(id) || !currentUser.getUserAccount().equals(userAccount)){
            throw new BusinessException(ErrorCode.ILLEGAL_ERROR);
        }
        // 3、验证token是否过期
        boolean verifyTime = tokenUtils.verifyTime(authorization);
        if (!verifyTime){
            //过期了需要重新登录
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR,"登录状态过期，请重新登录");
//            String refreshToken = tokenUtils.refreshToken(authorization);
//            Cookie authorization1 = new Cookie("authorization", refreshToken);
//            authorization1.setPath("/");
//            authorization1.setMaxAge(cookieExpireConstant.expireTime);
//            response.addCookie(authorization1);
//            SecurityContextHolder.getContext().setAuthentication(authentication);
        }
        return true;
    }
}
