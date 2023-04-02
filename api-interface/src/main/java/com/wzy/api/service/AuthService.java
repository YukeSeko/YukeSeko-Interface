package com.wzy.api.service;


import com.baomidou.mybatisplus.extension.service.IService;
import common.model.entity.Auth;

import javax.servlet.http.HttpServletRequest;

/**
* @author WZY
* @description 针对表【auth】的数据库操作Service
* @createDate 2023-01-17 10:33:59
*/
public interface AuthService extends IService<Auth> {


    String mainRedirect(HttpServletRequest request);
}
