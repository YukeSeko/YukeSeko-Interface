package com.wzy.api.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wzy.api.common.RedisTemplateUtils;
import com.wzy.api.mapper.InterfaceChargingMapper;
import com.wzy.api.model.entity.InterfaceCharging;
import com.wzy.api.service.InterfaceChargingService;
import common.BaseResponse;
import common.ErrorCode;
import common.Utils.ResultUtils;
import common.vo.LockChargingVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
* @author 12866
* @description 针对表【interface_charging】的数据库操作Service实现
* @createDate 2023-03-13 14:31:45
*/
@Service
public class InterfaceChargingServiceImpl extends ServiceImpl<InterfaceChargingMapper, InterfaceCharging>
    implements InterfaceChargingService {

    @Autowired
    private InterfaceChargingMapper interfaceChargingMapper;

    @Autowired
    private RedisTemplateUtils redisTemplateUtils;

    /**
     * 更新接口剩余可购买数量
     * @param lockChargingVo
     * @return
     */
    @Override
    @Transactional
    public BaseResponse updateAvailablePieces(LockChargingVo lockChargingVo) {
        Long interfaceId = lockChargingVo.getInterfaceid();
        Long orderNum = lockChargingVo.getOrderNum();
        try {
            this.update(new UpdateWrapper<InterfaceCharging>().eq("interfaceid",interfaceId)
                    .setSql("availablePieces = availablePieces - "+orderNum));
        }catch (Exception e){
            return ResultUtils.error(ErrorCode.PARAMS_ERROR,"更新库存失败");
        }
        redisTemplateUtils.delAllOnlinePage();
        return ResultUtils.success(null);
    }

    /**
     * 获取当前最新剩余库存
     * @param interfaceId
     * @return
     */
    @Override
    public BaseResponse getPresentAvailablePieces(Long interfaceId) {
        String availablePieces  =  interfaceChargingMapper.getPresentAvailablePieces(interfaceId);
        return ResultUtils.success(availablePieces);
    }

    /**
     * 解锁库存
     * @param lockChargingVo
     * @return
     */
    @Override
    @Transactional
    public BaseResponse unlockAvailablePieces(LockChargingVo lockChargingVo) {
        Long interfaceId = lockChargingVo.getInterfaceid();
        Long orderNum = lockChargingVo.getOrderNum();
        try {
            this.update(new UpdateWrapper<InterfaceCharging>().eq("interfaceid",interfaceId)
                    .setSql("availablePieces = availablePieces + "+orderNum));
        }catch (Exception e){
            return ResultUtils.error(ErrorCode.PARAMS_ERROR,"解锁库存失败");
        }
        redisTemplateUtils.delAllOnlinePage();
        return ResultUtils.success(null);
    }
}




