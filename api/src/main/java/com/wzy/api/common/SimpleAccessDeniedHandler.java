package com.wzy.api.common;

import com.alibaba.fastjson.JSON;
import common.BaseResponse;
import common.ErrorCode;
import common.Utils.ResultUtils;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author YukeSeko
 * 自定义授权异常处理类 ：对应403
 */
@Component
public class SimpleAccessDeniedHandler implements AccessDeniedHandler {
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {
        BaseResponse error = ResultUtils.error(ErrorCode.ILLEGAL_ERROR, "请求被拒绝，请重试!");
        String json = JSON.toJSONString(error);
        response.setContentType("application/json");
        response.setCharacterEncoding("utf-8");
        response.getWriter().write(json);
    }
}
