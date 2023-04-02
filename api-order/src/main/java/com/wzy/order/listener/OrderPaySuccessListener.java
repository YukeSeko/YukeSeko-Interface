package com.wzy.order.listener;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.rabbitmq.client.Channel;
import com.wzy.order.feign.UserFeignServices;
import com.wzy.order.model.entity.ApiOrder;
import com.wzy.order.model.entity.ApiOrderLock;
import com.wzy.order.service.ApiOrderLockService;
import com.wzy.order.service.ApiOrderService;
import common.BaseResponse;
import common.ErrorCode;
import common.Exception.BusinessException;
import common.constant.RabbitMqConstant;
import common.constant.RedisConstant;
import common.to.LeftNumUpdateTo;
import common.vo.LockChargingVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * 订单支付成功的监听
 * @author YukeSeko
 */
@Slf4j
@Component
public class OrderPaySuccessListener {

    @Autowired
    private ApiOrderLockService apiOrderLockService;

    @Autowired
    private ApiOrderService apiOrderService;

    @Autowired
    private UserFeignServices userFeignServices;

    @Autowired
    private RedisTemplate redisTemplate;

    @Transactional(rollbackFor = Exception.class)
    @RabbitListener(queues = RabbitMqConstant.order_pay_success)
    public void orderPaySuccessListener(String orderSn, Message message, Channel channel) throws IOException {
        try{
            String replace = orderSn.replace("\"", "");
            //消息抵达队列后，就进行删除操作
            redisTemplate.delete(RedisConstant.ORDER_PAY_SUCCESS_INFO + replace.toString());
            // 解决重复投递问题
            Object o = redisTemplate.opsForValue().get(RedisConstant.ORDER_PAY_RABBITMQ + replace.toString());
            if (null != o){
                //已经成功处理过了，直接放行
                channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
            }
            log.info("监听到《订单支付成功》的消息：{}",replace);
            if (null != replace){
                //修改订单和订单锁定状态，天然幂等
                apiOrderLockService.orderPaySuccess(replace);
                apiOrderService.orderPaySuccess(replace);
                ApiOrderLock orderLock = apiOrderLockService.getOne(new QueryWrapper<ApiOrderLock>().eq("orderSn",replace.toString()));
                ApiOrder order = apiOrderService.getOne(new QueryWrapper<ApiOrder>().eq("orderSn", replace.toString()));
                Long lockNum = orderLock.getLockNum();
                Long userId = order.getUserId();
                Long interfaceId = order.getInterfaceId();
                LeftNumUpdateTo leftNumUpdateTo = new LeftNumUpdateTo();
                leftNumUpdateTo.setLockNum(lockNum);
                leftNumUpdateTo.setUserId(userId);
                leftNumUpdateTo.setInterfaceInfoId(interfaceId);
                //远程调用，增加剩余用户剩余调用次数
                BaseResponse baseResponse = userFeignServices.updateUserLeftNum(leftNumUpdateTo);
                if (baseResponse.getData().equals("true")){
                    throw new BusinessException(ErrorCode.OPERATION_ERROR);
                }
            }
            redisTemplate.opsForValue().set(RedisConstant.ORDER_PAY_RABBITMQ + replace.toString(),"true",30, TimeUnit.MINUTES);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
        }catch (Exception e){
            redisTemplate.delete(RedisConstant.ORDER_PAY_RABBITMQ +orderSn.replace("\"", ""));
            channel.basicReject(message.getMessageProperties().getDeliveryTag(),true);
        }
    }
}
