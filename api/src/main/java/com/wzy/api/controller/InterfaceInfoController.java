package com.wzy.api.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wzy.api.annotation.AuthCheck;
import com.wzy.api.common.DeleteRequest;
import com.wzy.api.common.IdRequest;
import com.wzy.api.model.dto.interfaceinfo.InterfaceInfoAddRequest;
import com.wzy.api.model.dto.interfaceinfo.InterfaceInfoQueryRequest;
import com.wzy.api.model.dto.interfaceinfo.InterfaceInfoUpdateRequest;
import com.wzy.api.model.entity.InterfaceInfo;
import com.wzy.api.model.vo.AllInterfaceInfoVo;
import com.wzy.api.model.vo.InterfaceInfoVo;
import com.wzy.api.service.InterfaceInfoService;
import common.BaseResponse;
import common.Utils.ResultUtils;
import common.to.GetAvailablePiecesTo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 接口管理
 */
@RestController
@RequestMapping("/interfaceInfo")
@Slf4j
public class InterfaceInfoController {

    @Resource
    private InterfaceInfoService interfaceInfoService;


    /**
     * 获取全站可调用接口数
     * @return
     */
    @GetMapping("/getInterfaceCount")
    public BaseResponse getInterfaceCount(){
        return ResultUtils.success(interfaceInfoService.count(new QueryWrapper<InterfaceInfo>().eq("isDelete",0).eq("status",1)));
    }

    /**
     * 获取订单页面所需要的接口信息
     * @param availablePiecesTo
     * @return
     */
    @PostMapping("/getOrderInterfaceInfo")
    public BaseResponse getOrderInterfaceInfo(@RequestBody GetAvailablePiecesTo availablePiecesTo){
        return ResultUtils.success(interfaceInfoService.getById(availablePiecesTo.getInterfaceId()));
    }

    /**
     * 创建
     *
     * @param interfaceInfoAddRequest
     * @param request
     * @return
     */
    @PostMapping("/add")
    public BaseResponse<Long> addInterfaceInfo(@RequestBody InterfaceInfoAddRequest interfaceInfoAddRequest, HttpServletRequest request) {
        return interfaceInfoService.addInterfaceInfo(interfaceInfoAddRequest,request);
    }

    /**
     * 删除
     * @param deleteRequest
     * @param request
     * @return
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteInterfaceInfo(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        return interfaceInfoService.deleteInterfaceInfo(deleteRequest,request) ;
    }

    /**
     * 更新
     *
     * @param interfaceInfoUpdateRequest
     * @param request
     * @return
     */
    @PostMapping("/update")
    public BaseResponse<Boolean> updateInterfaceInfo(@RequestBody InterfaceInfoUpdateRequest interfaceInfoUpdateRequest,
                                                     HttpServletRequest request) {
        return interfaceInfoService.updateInterfaceInfo(interfaceInfoUpdateRequest,request);
    }

    /**
     * 根据 id 获取接口详细信息和用户调用次数
     *
     * @param id
     * @return
     */
    @GetMapping("/get")
    public BaseResponse<InterfaceInfoVo> getInterfaceInfoById(long id,HttpServletRequest request) {
        return interfaceInfoService.getInterfaceInfoById(id,request);
    }

    /**
     * 获取列表（仅管理员可使用）
     *
     * @param interfaceInfoQueryRequest
     * @return
     */
    @AuthCheck(mustRole = "admin")
    @GetMapping("/list")
    public BaseResponse<List<InterfaceInfo>> listInterfaceInfo(InterfaceInfoQueryRequest interfaceInfoQueryRequest) {
        InterfaceInfo interfaceInfoQuery = new InterfaceInfo();
        if (interfaceInfoQueryRequest != null) {
            BeanUtils.copyProperties(interfaceInfoQueryRequest, interfaceInfoQuery);
        }
        QueryWrapper<InterfaceInfo> queryWrapper = new QueryWrapper<>(interfaceInfoQuery);
        List<InterfaceInfo> interfaceInfoList = interfaceInfoService.list(queryWrapper);
        return ResultUtils.success(interfaceInfoList);
    }

    /**
     * 分页获取已开启的列表
     * @param interfaceInfoQueryRequest
     * @return
     */
    @GetMapping("/list/page")
    public BaseResponse<Page<AllInterfaceInfoVo>> listInterfaceInfoByPage(InterfaceInfoQueryRequest interfaceInfoQueryRequest) {
        return interfaceInfoService.listInterfaceInfoByPage(interfaceInfoQueryRequest);
    }


    /**
     * 分页获取已所有的列表（包括已下线）
     *
     * @param interfaceInfoQueryRequest
     * @param request
     * @return
     */
    @GetMapping("/list/AllPage")
    @AuthCheck(mustRole = "admin")
    public BaseResponse<Page<AllInterfaceInfoVo>> getAllInterfaceInfoByPage(InterfaceInfoQueryRequest interfaceInfoQueryRequest, HttpServletRequest request) {
        return interfaceInfoService.getAllInterfaceInfoByPage(interfaceInfoQueryRequest,request);
    }


    /**
     * 上线接口 todo ,需要修改接口上线逻辑
     *
     * @param idRequest
     * @param request
     * @return
     */
    @PostMapping("/online")
    @AuthCheck(mustRole = "admin")
    public BaseResponse<Boolean> onlineInterfaceInfo(@RequestBody IdRequest idRequest, HttpServletRequest request) {
        return interfaceInfoService.onlineInterfaceInfo(idRequest);
    }


    /**
     * 下线接口
     *
     * @param idRequest
     * @return
     */
    @PostMapping("/offline")
    @AuthCheck(mustRole = "admin")
    public BaseResponse<Boolean> offlineInterfaceInfo(@RequestBody IdRequest idRequest) {
        return interfaceInfoService.offlineInterfaceInfo(idRequest);
    }

}
