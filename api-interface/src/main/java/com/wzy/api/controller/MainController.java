package com.wzy.api.controller;

import cn.hutool.http.HttpRequest;
import com.wzy.api.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@RestController
public class MainController {
    @Autowired
    private AuthService authService;

    /**
     * 请求转发
     *
     * @param request
     */
    @RequestMapping("/main")
    public String MainRedirect(HttpServletRequest request) {
        return authService.mainRedirect(request);
    }

}
