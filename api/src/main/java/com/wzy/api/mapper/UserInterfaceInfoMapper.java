package com.wzy.api.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wzy.api.model.entity.UserInterfaceInfo;
import com.wzy.api.model.vo.UserInterfaceLeftNumVo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
* @author 12866
* @description 针对表【user_interface_info(用户调用接口关系)】的数据库操作Mapper
* @createDate 2023-02-06 14:16:25
* @Entity generator.domain.UserInterfaceInfo
*/
public interface UserInterfaceInfoMapper extends BaseMapper<UserInterfaceInfo> {

    List<UserInterfaceLeftNumVo> getUserInterfaceLeftNum(@Param("id") Long id);
}




