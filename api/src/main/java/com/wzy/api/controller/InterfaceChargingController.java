package com.wzy.api.controller;

import com.wzy.api.service.InterfaceChargingService;
import common.BaseResponse;
import common.to.GetAvailablePiecesTo;
import common.vo.LockChargingVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @author YukeSeko
 */
@RestController
@RequestMapping("/charging")
public class InterfaceChargingController {

    @Autowired
    private InterfaceChargingService interfaceChargingService;


    /**
     * 解锁库存
     * @param lockChargingVo
     * @return
     */
    @PostMapping("/unlockAvailablePieces")
    public BaseResponse unlockAvailablePieces(@RequestBody LockChargingVo lockChargingVo){
        return interfaceChargingService.unlockAvailablePieces(lockChargingVo);
    }

    /**
     * 获取当前最新剩余库存
     * @param getAvailablePiecesTo
     * @return
     */
    @PostMapping("/getPresentAvailablePieces")
    public BaseResponse getPresentAvailablePieces(@RequestBody GetAvailablePiecesTo getAvailablePiecesTo){
        return interfaceChargingService.getPresentAvailablePieces(getAvailablePiecesTo.getInterfaceId());
    }


    /**
     * 更新接口剩余可购买数量
     * @param lockChargingVo
     * @return
     */
    @PostMapping("/updateAvailablePieces")
    public BaseResponse updateAvailablePieces(@RequestBody LockChargingVo lockChargingVo){
        return interfaceChargingService.updateAvailablePieces(lockChargingVo);
    }
}
