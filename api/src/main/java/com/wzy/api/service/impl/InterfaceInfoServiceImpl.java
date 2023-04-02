package com.wzy.api.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wzy.api.common.DeleteRequest;
import com.wzy.api.common.IdRequest;
import com.wzy.api.common.RedisTemplateUtils;
import common.Exception.BusinessException;
import com.wzy.api.mapper.InterfaceInfoMapper;
import com.wzy.api.model.dto.interfaceinfo.InterfaceInfoAddRequest;
import com.wzy.api.model.dto.interfaceinfo.InterfaceInfoQueryRequest;
import com.wzy.api.model.dto.interfaceinfo.InterfaceInfoUpdateRequest;
import com.wzy.api.model.entity.InterfaceCharging;
import com.wzy.api.model.entity.InterfaceInfo;
import com.wzy.api.model.entity.User;
import com.wzy.api.model.entity.UserInterfaceInfo;
import com.wzy.api.model.enums.InterFaceInfoEnum;
import com.wzy.api.model.vo.AllInterfaceInfoVo;
import com.wzy.api.model.vo.InterfaceInfoVo;
import com.wzy.api.service.InterfaceChargingService;
import com.wzy.api.service.InterfaceInfoService;
import com.wzy.api.service.UserInterfaceInfoService;
import com.wzy.api.service.UserService;
import common.BaseResponse;
import common.ErrorCode;
import common.Utils.ResultUtils;
import common.constant.LockConstant;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.concurrent.TimeUnit;

/**
* @author WZY
* @description 针对表【interface_info(接口信息)】的数据库操作Service实现
* @createDate 2023-01-12 10:45:11
*/
@Service
public class InterfaceInfoServiceImpl extends ServiceImpl<InterfaceInfoMapper, InterfaceInfo>
    implements InterfaceInfoService {


    @Autowired
    private InterfaceInfoMapper interfaceInfoMapper;

    @Resource
    private UserService userService;

    @Autowired
    private InterfaceChargingService interfaceChargingService;

    @Autowired
    private RedisTemplateUtils redisTemplateUtils;

    @Autowired
    private UserInterfaceInfoService userInterfaceInfoService;

    @Autowired
    private RedissonClient redissonClient;

    /**
     * 校验接口是否可用
     * @param interfaceInfo
     * @param add
     */
    @Override
    public void validInterfaceInfo(InterfaceInfo interfaceInfo, boolean add) {
        if (interfaceInfo == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String name = interfaceInfo.getName();
        // 创建时，所有参数必须非空
        if (add) {
            if (StringUtils.isAnyBlank(name)) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR);
            }
        }
        if (StringUtils.isNotBlank(name) && name.length() > 50) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "名称过长");
        }
    }

    /**
     * 管理员---分页获取已所有的列表（包括已下线）
     * @param interfaceInfoQueryRequest
     * @param request
     * @return
     */
    @Override
    public BaseResponse<Page<AllInterfaceInfoVo>> getAllInterfaceInfoByPage(InterfaceInfoQueryRequest interfaceInfoQueryRequest, HttpServletRequest request) {
        if (interfaceInfoQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        long current = interfaceInfoQueryRequest.getCurrent();
        long size = interfaceInfoQueryRequest.getPageSize();
        // 限制爬虫
        if (size > 50) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Page<AllInterfaceInfoVo> allInterfaceInfoVoPage = interfaceInfoMapper.selectAllPage(new Page<>(current, size), interfaceInfoQueryRequest);
        return ResultUtils.success(allInterfaceInfoVoPage);
    }

    /**
     * 管理员 - 更新操作
     * @param interfaceInfoUpdateRequest
     * @param request
     * @return
     */
    @Override
    @Transactional
    public BaseResponse<Boolean> updateInterfaceInfo(InterfaceInfoUpdateRequest interfaceInfoUpdateRequest, HttpServletRequest request) {
        if (interfaceInfoUpdateRequest == null || interfaceInfoUpdateRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        InterfaceInfo interfaceInfo = new InterfaceInfo();
        BeanUtils.copyProperties(interfaceInfoUpdateRequest, interfaceInfo);
        // 参数校验
        this.validInterfaceInfo(interfaceInfo, false);
        User user = userService.getLoginUser(request);
        long id = interfaceInfoUpdateRequest.getId();
        // 判断是否存在
        InterfaceInfo oldInterfaceInfo = this.getById(id);
        if (oldInterfaceInfo == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        // 仅本人或管理员可修改
        if (!oldInterfaceInfo.getUserId().equals(user.getId()) && !userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        boolean result = this.updateById(interfaceInfo);
        Long id1 = interfaceInfo.getId();
        String availablePieces = interfaceInfoUpdateRequest.getAvailablePieces();
        Double charging = interfaceInfoUpdateRequest.getCharging();
        if (org.springframework.util.StringUtils.hasLength(availablePieces) || charging!=null){
            UpdateWrapper<InterfaceCharging> interfaceChargingUpdateWrapper = new UpdateWrapper<>();
            if (org.springframework.util.StringUtils.hasLength(availablePieces)){
                interfaceChargingUpdateWrapper.set("availablePieces",availablePieces);
            }
            if (charging!=null){
                interfaceChargingUpdateWrapper.set("charging",charging);
            }
            interfaceChargingService.update(interfaceChargingUpdateWrapper.eq("interfaceid",id1));
        }
        redisTemplateUtils.delAllOnlinePage();
        return ResultUtils.success(result);
    }

    /**
     * 管理员 - 添加接口
     * @param interfaceInfoAddRequest
     * @param request
     * @return
     */
    @Override
    @Transactional
    public BaseResponse<Long> addInterfaceInfo(InterfaceInfoAddRequest interfaceInfoAddRequest, HttpServletRequest request) {
        if (interfaceInfoAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        InterfaceInfo interfaceInfo = new InterfaceInfo();
        BeanUtils.copyProperties(interfaceInfoAddRequest, interfaceInfo);
        // 校验
        this.validInterfaceInfo(interfaceInfo, true);
        User loginUser = userService.getLoginUser(request);
        interfaceInfo.setUserId(loginUser.getId());
        boolean result = this.save(interfaceInfo);
        Double charging = interfaceInfoAddRequest.getCharging();
        String availablePieces = interfaceInfoAddRequest.getAvailablePieces();
        InterfaceCharging interfaceCharging = new InterfaceCharging();
        interfaceCharging.setCharging(charging);
        interfaceCharging.setAvailablePieces(availablePieces);
        interfaceCharging.setUserId(loginUser.getId());
        interfaceChargingService.save(interfaceCharging);
        if (!result) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR);
        }
        long newInterfaceInfoId = interfaceInfo.getId();
        redisTemplateUtils.delAllOnlinePage();
        return ResultUtils.success(newInterfaceInfoId);
    }

    /**
     * 删除操作
     * @param deleteRequest
     * @param request
     * @return
     */
    @Override
    @Transactional
    public BaseResponse<Boolean> deleteInterfaceInfo(DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.getLoginUser(request);
        long id = deleteRequest.getId();
        // 判断是否存在
        InterfaceInfo oldInterfaceInfo = this.getById(id);
        if (oldInterfaceInfo == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        // 仅本人或管理员可删除
        if (!oldInterfaceInfo.getUserId().equals(user.getId()) && !userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        boolean b = this.removeById(id);
        boolean interfaceid = interfaceChargingService.remove(new QueryWrapper<InterfaceCharging>().eq("interfaceid", id));
        if (!b || ! interfaceid){
            throw new BusinessException(ErrorCode.OPERATION_ERROR);
        }
        redisTemplateUtils.delAllOnlinePage();
        return ResultUtils.success(b);
    }


    /**
     * 分页获取已上线的列表
     * @param interfaceInfoQueryRequest
     * @return
     */
    @Override
    public BaseResponse<Page<AllInterfaceInfoVo>> listInterfaceInfoByPage(InterfaceInfoQueryRequest interfaceInfoQueryRequest) {
        if (interfaceInfoQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        long current = interfaceInfoQueryRequest.getCurrent();
        Page<AllInterfaceInfoVo> onlinePage = redisTemplateUtils.getOnlinePage(current);
        if (onlinePage!=null){
            //加入缓存后，请求时间由原来的平均68ms ，降低到平局36ms
            return ResultUtils.success(onlinePage);
        }
        long size = interfaceInfoQueryRequest.getPageSize();
        // 限制爬虫
        if (size > 20) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 加分布式锁，同一时刻只能有一个请求数据库，其他的请求循环等待，解决缓存击穿问题.
        for (;;){
            try {
                RLock lock = redissonClient.getLock(LockConstant.interface_onlinePage_lock);
                //尝试加锁，最多等待20秒，上锁以后10秒自动解锁
                boolean b = lock.tryLock(20, 10, TimeUnit.SECONDS);
                if (b){
                    //加锁成功,再次检查
                    Page<AllInterfaceInfoVo> onlinePageLock = redisTemplateUtils.getOnlinePage(current);
                    if (onlinePageLock!=null){
                        lock.unlock();
                        return ResultUtils.success(onlinePageLock);
                    }
                    //仍未命中,查询数据库
                    Page<AllInterfaceInfoVo> allInterfaceInfoVoPage = interfaceInfoMapper.selectOnlinePage(new Page<>(current, size), interfaceInfoQueryRequest);
                    redisTemplateUtils.onlinePageCache(allInterfaceInfoVoPage);
                    lock.unlock();
                    return ResultUtils.success(allInterfaceInfoVoPage);
                }
                //竞争不到锁，暂时让出CPU资源
                Thread.yield();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * 上线接口
     * @param idRequest
     * @return
     */
    @Override
    public BaseResponse<Boolean> onlineInterfaceInfo(IdRequest idRequest) {
        if (idRequest == null || idRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        long id = idRequest.getId();
        // 判断是否存在
        InterfaceInfo oldInterfaceInfo = this.getById(id);
        if (oldInterfaceInfo == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }

        // 判断接口是否可以调用
//        Object userObj = request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);
//        User currentUser = (User) userObj;
//        String res = apiClient.getResult(currentUser.getId(), currentUser.getUserAccount(), "woshinidie", Method.GET);
//        if (res == null) {
//            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"接口调用失败");
//        }
        //更新数据库
        InterfaceInfo interfaceInfo = new InterfaceInfo();
        interfaceInfo.setId(id);
        interfaceInfo.setStatus(InterFaceInfoEnum.ONLINE.getValue());
        boolean result = this.updateById(interfaceInfo);
        redisTemplateUtils.delAllOnlinePage();
        return ResultUtils.success(result);
    }

    /**
     * 下线接口
     * @param idRequest
     * @return
     */
    @Override
    public BaseResponse<Boolean> offlineInterfaceInfo(IdRequest idRequest) {
        if (idRequest == null || idRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        long id = idRequest.getId();
        // 判断是否存在
        InterfaceInfo oldInterfaceInfo = this.getById(id);
        if (oldInterfaceInfo == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }

        //更新数据库
        InterfaceInfo interfaceInfo = new InterfaceInfo();
        interfaceInfo.setId(id);
        interfaceInfo.setStatus(InterFaceInfoEnum.OFFLINE.getValue());
        boolean result = this.updateById(interfaceInfo);
        redisTemplateUtils.delAllOnlinePage();
        return ResultUtils.success(result);
    }

    /**
     * 根据 id 获取接口详细信息和用户调用次数
     * @param id
     * @param request
     * @return
     */
    @Override
    public BaseResponse<InterfaceInfoVo> getInterfaceInfoById(long id, HttpServletRequest request) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.getLoginUser(request);
        if(user == null){
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        Long userId = user.getId();
        InterfaceInfo interfaceInfo = this.getById(id);
        InterfaceInfoVo interfaceInfoVo = new InterfaceInfoVo();
        BeanUtils.copyProperties(interfaceInfo,interfaceInfoVo);
        UserInterfaceInfo one = userInterfaceInfoService.getOne(new QueryWrapper<UserInterfaceInfo>().eq("userId", userId).eq("interfaceInfoId", id));
        if (null == one){
            //请求数据不存在时，创建一个新的，并将调用次数设置为0
            UserInterfaceInfo userInterfaceInfo = new UserInterfaceInfo();
            userInterfaceInfo.setUserId(userId);
            userInterfaceInfo.setInterfaceInfoId(id);
            userInterfaceInfoService.save(userInterfaceInfo);
            interfaceInfoVo.setLeftNum(0);
        }else {
            interfaceInfoVo.setLeftNum(one.getLeftNum());
        }
        return ResultUtils.success(interfaceInfoVo);
    }
}




