package com.wzy.api.controller;


import com.wzy.api.model.vo.AuthVo;
import com.wzy.api.service.AuthService;
import common.BaseResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * @author YukeSeko
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    @Resource
    private AuthService authService;

    /**
     * 根据用户id获取用户的密钥
     * @param id
     * @param request
     * @return
     */
    @GetMapping("/getAuthByUserId")
    public BaseResponse<AuthVo> getAuthByUserId(@RequestParam Long id , HttpServletRequest request){
        return authService.getAuthByUserId(id,request);
    }

    /**
     * 修改API密钥的状态
     * @param id
     * @param request
     * @return
     */
    @GetMapping("/updateAuthStatus")
    public BaseResponse updateAuthStatus(@RequestParam Long id , HttpServletRequest request){
        return authService.updateAuthStatus(id,request);
    }
}
