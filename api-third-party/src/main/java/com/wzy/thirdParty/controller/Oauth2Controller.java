package com.wzy.thirdParty.controller;


import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONUtil;
import com.wzy.thirdParty.feign.UserFeignServices;
import common.BaseResponse;
import common.Utils.CookieUtils;
import common.constant.CookieConstant;
import common.to.Oauth2ResTo;
import common.vo.LoginUserVo;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;


/**
 * 第三方登录
 */
@RequestMapping("/oauth")
@Controller
public class Oauth2Controller {


    @Value("${gitee.client_id}")
    private String gitee_client_id;

    @Value("${gitee.client_secret}")
    private String gitee_client_secret;

    @Value("${gitee.redirect_uri}")
    private String gitee_redirect_uri;

    @Value("${github.client_id}")
    private String github_client_id;

    @Value("${github.client_secret}")
    private String github_client_secret;

    @Value("${github.redirect_uri}")
    private String github_redirect_uri;

    @Autowired
    private UserFeignServices userFeignServices;

    @GetMapping("/gitee")
    public String gitee(@RequestParam("code") String code, HttpServletResponse res) throws IOException {
        String url = "https://gitee.com/oauth/token?grant_type=authorization_code&code=" + code +
                "&client_id=" + gitee_client_id +
                "&redirect_uri=" + gitee_redirect_uri +
                "&client_secret=" + gitee_client_secret;
        HttpResponse response =null;
        try {
            response = HttpRequest.post(url)
                    .timeout(20000)//超时，毫秒
                    .execute();
        }catch (Exception e){
            PrintWriter writer = res.getWriter();
            writer.println("<script>alert('请求超时，请重试')</script>");
            return  "redirect:http://localhost:8000/user/login";
        }
        if (response.getStatus() == 200){
            Oauth2ResTo oauth2ResTo = JSONUtil.toBean(response.body(), Oauth2ResTo.class);
            BaseResponse baseResponse = userFeignServices.oauth2Login(oauth2ResTo,"gitee");
            //拿到token，远程调用查询用户是否注册、未注册的自动进行注册，已经完成注册的，则进行登录
            if (cookieResUtils(res, baseResponse)) return "redirect:http://localhost:8000/user/login";
        }
        return "redirect:http://localhost:8000/";
    }


    @GetMapping("/github")
    public String github(@RequestParam("code") String code, HttpServletResponse res) throws IOException {
        String url = "https://github.com/login/oauth/access_token?client_id=" + github_client_id +
                "&client_secret=" + github_client_secret +
                "&code=" + code;
        HttpResponse response = null;
        try {
             response = HttpRequest.post(url)
                    .timeout(20000)//超时，毫秒
                    .execute();
        }catch (Exception e){
            PrintWriter writer = res.getWriter();
            writer.println("<script>alert('请求超时，请重试')</script>");
            return  "redirect:http://localhost:8000/user/login";
        }
        if (response.getStatus() == 200){
            String s = response.body().toString();
            String[] split = s.split("&");
            String s1 = split[0];
            String[] split1 = s1.split("=");
            String token = split1[1];
            Oauth2ResTo oauth2ResTo = new Oauth2ResTo();
            oauth2ResTo.setAccess_token(token);
            BaseResponse baseResponse = userFeignServices.oauth2Login(oauth2ResTo,"github");
            //拿到token，远程调用查询用户是否注册、未注册的自动进行注册，已经完成注册的，则进行登录
            if (cookieResUtils(res, baseResponse)) return "redirect:http://localhost:8000/user/login";
        }
        return "redirect:http://localhost:8000/";
    }

    private boolean cookieResUtils(HttpServletResponse res, @NotNull BaseResponse baseResponse) throws IOException {
        if (baseResponse.getCode() != 0){
            PrintWriter writer = res.getWriter();
            writer.println("<script>alert('登录失败')</script>");
            return true;
        }
        Object data = baseResponse.getData();
        LoginUserVo loginUserVo = JSONUtil.toBean(JSONUtil.parseObj(data), LoginUserVo.class);
        Cookie cookie = new Cookie(CookieConstant.headAuthorization,loginUserVo.getToken());
        cookie.setPath("/");
        cookie.setMaxAge(CookieConstant.expireTime);
        CookieUtils cookieUtils = new CookieUtils();
        String autoLoginContent = cookieUtils.generateAutoLoginContent(loginUserVo.getId().toString(), loginUserVo.getUserAccount());
        Cookie cookie1 = new Cookie(CookieConstant.autoLoginAuthCheck, autoLoginContent);
        cookie1.setPath("/");
        cookie.setMaxAge(CookieConstant.expireTime);
        res.addCookie(cookie);
        res.addCookie(cookie1);
        return false;
    }
}
