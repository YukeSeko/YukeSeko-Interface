package com.wzy.api.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import common.Exception.BusinessException;
import com.wzy.api.mapper.UserInterfaceInfoMapper;
import com.wzy.api.model.entity.User;
import com.wzy.api.model.entity.UserInterfaceInfo;
import com.wzy.api.model.vo.UserInterfaceLeftNumVo;
import com.wzy.api.service.UserInterfaceInfoService;
import com.wzy.api.service.UserService;
import common.BaseResponse;
import common.ErrorCode;
import common.Utils.ResultUtils;
import common.to.LeftNumUpdateTo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
* @author wzy
* @description 针对表【user_interface_info(用户调用接口关系)】的数据库操作Service实现
* @createDate 2023-02-06 14:16:25
*/
@Service
public class UserInterfaceInfoServiceImpl extends ServiceImpl<UserInterfaceInfoMapper, UserInterfaceInfo>
    implements UserInterfaceInfoService {


    @Autowired
    private UserService userService;

    @Autowired
    private UserInterfaceInfoMapper userInterfaceInfoMapper;

    @Override
    public void validUserInterfaceInfo(UserInterfaceInfo userInterfaceInfo, boolean add) {
        if (userInterfaceInfo == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 创建时，所有参数必须非空
        if (add) {
            if (userInterfaceInfo.getInterfaceInfoId() <= 0 || userInterfaceInfo.getUserId() <= 0) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "接口或用户不存在");
            }
        }
        if (userInterfaceInfo.getLeftNum() < 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "剩余次数不能小于 0");
        }
    }

    /**
     * 获取当前登录用户的接口剩余调用次数
     * @param request
     * @return
     */
    @Override
    public BaseResponse getUserInterfaceLeftNum(HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        Long id = loginUser.getId();
        List<UserInterfaceLeftNumVo>  userInterfaceLeftNumVo =  userInterfaceInfoMapper.getUserInterfaceLeftNum(id);
        return ResultUtils.success(userInterfaceLeftNumVo);
    }

    /**
     * 更新用户可调用次数
     * @param leftNumUpdateTo
     * @return
     */
    @Override
    public BaseResponse updateUserLeftNum(LeftNumUpdateTo leftNumUpdateTo) {
        boolean update = this.update(new UpdateWrapper<UserInterfaceInfo>().eq("userId", leftNumUpdateTo.getUserId()).eq("interfaceInfoId", leftNumUpdateTo.getInterfaceInfoId())
                .setSql("leftNum = leftNum +"+leftNumUpdateTo.getLockNum()));
        return ResultUtils.success(update);
    }
}




