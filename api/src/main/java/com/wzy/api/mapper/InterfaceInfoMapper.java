package com.wzy.api.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wzy.api.model.dto.interfaceinfo.InterfaceInfoQueryRequest;
import com.wzy.api.model.entity.InterfaceInfo;
import com.wzy.api.model.vo.AllInterfaceInfoVo;
import common.vo.EchartsVo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
* @author WZY
* @description 针对表【interface_info(接口信息)】的数据库操作Mapper
* @createDate 2023-01-12 10:45:11
* @Entity com.wzy.api.model.entity.InterfaceInfo
*/
public interface InterfaceInfoMapper extends BaseMapper<InterfaceInfo> {

    Page<AllInterfaceInfoVo> selectAllPage(Page<AllInterfaceInfoVo> objectPage, @Param("dto") InterfaceInfoQueryRequest interfaceInfoQueryRequest);

    Page<AllInterfaceInfoVo> selectOnlinePage(Page<Object> objectPage, @Param("dto") InterfaceInfoQueryRequest interfaceInfoQueryRequest);

    List<EchartsVo> getInterfaceList(@Param("dateList") List<String> dateList);
}




