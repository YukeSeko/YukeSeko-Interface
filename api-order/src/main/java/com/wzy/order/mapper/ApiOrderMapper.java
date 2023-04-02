package com.wzy.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wzy.order.model.entity.ApiOrder;
import com.wzy.order.model.vo.ApiOrderStatusVo;
import common.vo.EchartsVo;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
* @author 12866
* @description 针对表【order】的数据库操作Mapper
* @createDate 2023-03-14 17:17:35
* @Entity generator.domain.Order
*/
public interface ApiOrderMapper extends BaseMapper<ApiOrder> {


    /**
     * 查询当前用户的订单信息
     * @param objectPage
     * @param userId
     * @param status
     * @return
     */
    Page<ApiOrderStatusVo> getCurrentOrderInfo(Page<Object> objectPage, @Param("userId") Long userId, @Param("status") Integer status);

    /**
     * 获取echarts图中最近7天的交易数
     * @param dateList
     * @return
     */
    List<EchartsVo> getOrderEchartsData(@Param("dateList") List<String> dateList);

}




