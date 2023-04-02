package com.wzy.order.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.wzy.order.model.entity.ApiOrder;
import com.wzy.order.model.to.ApiOrderCancelDto;
import com.wzy.order.model.to.ApiOrderDto;
import com.wzy.order.model.to.ApiOrderStatusInfoDto;
import com.wzy.order.model.vo.ApiOrderStatusVo;
import common.BaseResponse;
import common.vo.OrderSnVo;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.concurrent.ExecutionException;


/**
* @author 12866
* @description 针对表【order】的数据库操作Service
* @createDate 2023-03-14 17:17:35
*/
public interface ApiOrderService extends IService<ApiOrder> {

    /**
     * 生成订单接口
     * @param apiOrderDto
     * @param request
     * @param response
     * @return
     */
    BaseResponse<OrderSnVo> generateOrderSn(ApiOrderDto apiOrderDto, HttpServletRequest request, HttpServletResponse response) throws ExecutionException, InterruptedException;

    /**
     * 生成防重令牌：保证创建订单的接口幂等性
     * @param id
     * @param response
     * @return
     */
    BaseResponse generateToken(Long id,HttpServletResponse response);

    /**
     * 取消订单
     * @param apiOrderCancelDto
     * @param request
     * @param response
     * @return
     */
    BaseResponse cancelOrderSn(ApiOrderCancelDto apiOrderCancelDto, HttpServletRequest request, HttpServletResponse response);

    /**
     * 扣减库存相关操作
     * @param orderSn
     */
    void orderPaySuccess(String orderSn);

    /**
     * 获取当前登录用户的status订单信息
     * @param statusInfoDto
     * @param request
     * @return
     */
    BaseResponse<Page<ApiOrderStatusVo>> getCurrentOrderInfo(ApiOrderStatusInfoDto statusInfoDto, HttpServletRequest request);

    /**
     * 获取echarts图中最近7天的交易数
     * @param dateList
     * @return
     */
    BaseResponse getOrderEchartsData(List<String> dateList);
}
