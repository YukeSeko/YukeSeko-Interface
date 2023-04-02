package com.wzy.api.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wzy.api.model.entity.InterfaceCharging;
import org.apache.ibatis.annotations.Param;

/**
* @author 12866
* @description 针对表【interface_charging】的数据库操作Mapper
* @createDate 2023-03-13 14:31:45
* @Entity generator.domain.InterfaceCharging
*/
public interface InterfaceChargingMapper extends BaseMapper<InterfaceCharging> {

    String getPresentAvailablePieces(@Param("interfaceId") Long interfaceId);
}




