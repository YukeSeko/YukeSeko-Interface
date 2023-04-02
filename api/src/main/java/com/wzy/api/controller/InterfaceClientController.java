package com.wzy.api.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import common.Exception.BusinessException;
import com.wzy.api.model.dto.interfaceinfo.InterfaceInfoInvokRequest;
import com.wzy.api.model.entity.Auth;
import com.wzy.api.model.entity.InterfaceInfo;
import com.wzy.api.model.entity.User;
import com.wzy.api.model.enums.InterFaceInfoEnum;
import com.wzy.api.service.AuthService;
import com.wzy.api.service.InterfaceInfoService;
import com.wzy.apiclient.client.ApiClient;
import com.wzy.apiclient.model.Api;
import common.BaseResponse;
import common.ErrorCode;
import common.Utils.ResultUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@RestController
public class InterfaceClientController {

    @Autowired
    private AuthService authService;

    @Resource
    private InterfaceInfoService interfaceInfoService;

    /**
     * 接口在线调用
     * @param userRequestParams
     * @param request
     * @return
     */
    @PostMapping("/apiclient")
    public BaseResponse<Object> apiClient(@RequestBody InterfaceInfoInvokRequest userRequestParams, HttpServletRequest request) {
        if (userRequestParams == null || userRequestParams.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        long id = userRequestParams.getId();
        // 判断是否存在
        InterfaceInfo oldInterfaceInfo = interfaceInfoService.getById(id);
        if (oldInterfaceInfo == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        if(oldInterfaceInfo.getStatus() != InterFaceInfoEnum.ONLINE.getValue()){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"接口关闭");
        }
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User currentUser = (User) principal;
        if(currentUser == null ){
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        String params = userRequestParams.getUserRequestParams();
        String method = userRequestParams.getMethod();
        String url = userRequestParams.getUrl();
        if(params == null || method == null || url ==null){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"请求参数错误");
        }
        Api api = new Api();
        api.setInterfaceId(String.valueOf(id));
        api.setId(currentUser.getId());
        api.setUserAccount(currentUser.getUserAccount());
        api.setBody(params);
        api.setUrl(url);
        api.setMethod(method);
        Auth auth = authService.getOne(new QueryWrapper<Auth>()
                .eq("userid", currentUser.getId())
                .ne("status", 1));
        if (auth==null){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"API密钥不存在 或 API密钥已经被关闭");
        }
        ApiClient apiClient = new ApiClient(auth.getAppid(),auth.getAccesskey(),auth.getSecretkey());
        String result = apiClient.getResult(api);
        return ResultUtils.success(result);
    }
}
