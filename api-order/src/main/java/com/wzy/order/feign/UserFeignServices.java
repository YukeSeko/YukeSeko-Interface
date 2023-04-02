package com.wzy.order.feign;

import common.BaseResponse;
import common.to.GetAvailablePiecesTo;
import common.to.LeftNumUpdateTo;
import common.vo.LockChargingVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * @author YukeSeko
 */
@FeignClient(value = "api",url = "http://localhost:7529/api/")
public interface UserFeignServices {

    /**
     * 获取用户登录信息
     * @return
     */
    @PostMapping("/user/checkUserLogin")
    BaseResponse checkUserLogin();


    /**
     * 获取当前接口的剩余库存
     * @param getAvailablePiecesTo
     * @return
     */
    @PostMapping("/charging/getPresentAvailablePieces")
    BaseResponse getPresentAvailablePieces(@RequestBody GetAvailablePiecesTo getAvailablePiecesTo);

    /**
     * 更新库存
     * @param lockChargingVo
     * @return
     */
    @PostMapping("/charging/updateAvailablePieces")
    BaseResponse updateAvailablePieces(@RequestBody LockChargingVo lockChargingVo);

    /**
     * 远程解锁库存
     * @param lockChargingVo
     * @return
     */
    @PostMapping("/charging/unlockAvailablePieces")
    BaseResponse unlockAvailablePieces(@RequestBody LockChargingVo lockChargingVo);

    /**
     * 远程获取接口信息
     * @param availablePiecesTo
     * @return
     */
    @PostMapping("/interfaceInfo/getOrderInterfaceInfo")
    BaseResponse getOrderInterfaceInfo(@RequestBody GetAvailablePiecesTo availablePiecesTo);


    /**
     * 更新用户剩余可调用次数
     * @param leftNumUpdateTo
     * @return
     */
    @PostMapping("/userInterfaceInfo/updateUserLeftNum")
    BaseResponse updateUserLeftNum(@RequestBody LeftNumUpdateTo leftNumUpdateTo);
}
