package com.wzy.api.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wzy.api.model.entity.InterfaceCharging;
import common.BaseResponse;
import common.vo.LockChargingVo;

/**
* @author 12866
* @description 针对表【interface_charging】的数据库操作Service
* @createDate 2023-03-13 14:31:45
*/
public interface InterfaceChargingService extends IService<InterfaceCharging> {

    /**
     * 更新接口剩余可购买数量
     * @param lockChargingVo
     * @return
     */
    BaseResponse updateAvailablePieces(LockChargingVo lockChargingVo);

    /**
     * 获取当前最新剩余库存
     * @param interfaceId
     * @return
     */
    BaseResponse getPresentAvailablePieces(Long interfaceId);

    /**
     * 解锁库存
     * @param lockChargingVo
     * @return
     */
    BaseResponse unlockAvailablePieces(LockChargingVo lockChargingVo);
}
