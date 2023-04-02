package com.wzy.api.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.wzy.api.model.entity.UserInterfaceInfo;
import common.BaseResponse;
import common.to.LeftNumUpdateTo;

import javax.servlet.http.HttpServletRequest;

/**
* @author 12866
* @description 针对表【user_interface_info(用户调用接口关系)】的数据库操作Service
* @createDate 2023-02-06 14:16:25
*/
public interface UserInterfaceInfoService extends IService<UserInterfaceInfo> {

    void validUserInterfaceInfo(UserInterfaceInfo userInterfaceInfo, boolean b);

    /**
     * 获取当前登录用户的接口剩余调用次数
     * @param request
     * @return
     */
    BaseResponse getUserInterfaceLeftNum(HttpServletRequest request);

    /**
     * 更新用户可调用次数
     * @param leftNumUpdateTo
     * @return
     */
    BaseResponse updateUserLeftNum(LeftNumUpdateTo leftNumUpdateTo);
}
