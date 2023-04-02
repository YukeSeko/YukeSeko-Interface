package com.wzy.order.service.impl;

import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wzy.order.common.RabbitOrderUtils;
import com.wzy.order.feign.UserFeignServices;
import com.wzy.order.mapper.ApiOrderMapper;
import com.wzy.order.model.entity.ApiOrder;
import com.wzy.order.model.entity.ApiOrderLock;
import com.wzy.order.model.to.ApiOrderCancelDto;
import com.wzy.order.model.to.ApiOrderDto;
import com.wzy.order.model.to.ApiOrderStatusInfoDto;
import com.wzy.order.model.vo.ApiOrderStatusVo;
import com.wzy.order.service.ApiOrderLockService;
import com.wzy.order.service.ApiOrderService;
import common.BaseResponse;
import common.ErrorCode;
import common.Exception.BusinessException;
import common.Utils.ResultUtils;
import common.constant.CookieConstant;
import common.constant.OrderConstant;
import common.model.entity.InterfaceInfo;
import common.to.GetAvailablePiecesTo;
import common.vo.*;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
* @author 12866
* @description 针对表【order】的数据库操作Service实现
* @createDate 2023-03-14 17:17:35
*/
@Service
public class ApiOrderServiceImpl extends ServiceImpl<ApiOrderMapper, ApiOrder>
    implements ApiOrderService {

    @Autowired
    private UserFeignServices userFeignServices;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private ApiOrderLockService apiOrderLockService;

    @Autowired
    private RabbitOrderUtils rabbitOrderUtils;

    @Autowired
    private ApiOrderMapper apiOrderMapper;

    @Autowired
    private ThreadPoolExecutor executor;

    @Autowired
    private Snowflake snowflake;

    /**
     * 生成订单
     * @param apiOrderDto
     * @param request
     * @param response
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResponse<OrderSnVo> generateOrderSn(@RequestBody ApiOrderDto apiOrderDto, HttpServletRequest request, HttpServletResponse response) throws ExecutionException, InterruptedException {
        //1、远程获取当前登录用户
        BaseResponse baseResponse = userFeignServices.checkUserLogin();
        LoginUserVo loginUserVo = JSONUtil.toBean(JSONUtil.parseObj(baseResponse.getData()), LoginUserVo.class);
        if (null == loginUserVo){
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        //2、健壮性校验
        Long userId = apiOrderDto.getUserId();
        Double totalAmount = apiOrderDto.getTotalAmount();
        Long orderNum = apiOrderDto.getOrderNum();
        Double charging = apiOrderDto.getCharging();
        Long interfaceId = apiOrderDto.getInterfaceId();
        if (null == userId || null==totalAmount || null == orderNum || null ==charging || null==interfaceId){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        if (!userId.equals(loginUserVo.getId())){
            throw new BusinessException(ErrorCode.FORBIDDEN_ERROR);
        }
        //保留两位小数
        Double temp = orderNum * charging;
        BigDecimal two = new BigDecimal(temp);
        Double three = two.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
        if (!three.equals(totalAmount)){
            throw new BusinessException(ErrorCode.OPERATION_ERROR);
        }
        //3、验证令牌是否合法【令牌的对比和删除必须保证原子性】
        Cookie[] cookies = request.getCookies();
        String token = null;
        for (Cookie cookie : cookies) {
            if (CookieConstant.orderToken.equals(cookie.getName())){
                token = cookie.getValue();
            }
        }
        String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
        Long result = (Long) redisTemplate.execute(new DefaultRedisScript<Long>(script, Long.class),
                Arrays.asList(OrderConstant.USER_ORDER_TOKEN_PREFIX + loginUserVo.getId()),
                token);
        if (result == 0L){
            throw new BusinessException(ErrorCode.OPERATION_ERROR,"提交太快了，请重新提交");
        }
        //4、远程查询是否还有库存、远程异步调用查询接口信息
        GetAvailablePiecesTo getAvailablePiecesTo = new GetAvailablePiecesTo();
        getAvailablePiecesTo.setInterfaceId(interfaceId);
        BaseResponse presentAvailablePieces = userFeignServices.getPresentAvailablePieces(getAvailablePiecesTo);
        String availablePiecesData = (String) presentAvailablePieces.getData();
        if (Integer.parseInt(availablePiecesData) < orderNum){
            throw new BusinessException(ErrorCode.OPERATION_ERROR,"库存不足，请刷新页面,当前剩余库存为："+availablePiecesData);
        }
        //拿到主线程中的请求头，解决异步请求丢失请求头的问题
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        OrderInterfaceInfoVo orderInterfaceInfoVo = new OrderInterfaceInfoVo();
        CompletableFuture<Void> voidCompletableFuture = CompletableFuture.runAsync(() -> {
            //异步查询
            RequestContextHolder.setRequestAttributes(requestAttributes);
            BaseResponse orderInterfaceInfo = userFeignServices.getOrderInterfaceInfo(getAvailablePiecesTo);
            JSONObject entries = JSONUtil.parseObj(orderInterfaceInfo.getData());
            InterfaceInfo interfaceInfo = JSONUtil.toBean(entries, InterfaceInfo.class);
            orderInterfaceInfoVo.setName(interfaceInfo.getName());
            orderInterfaceInfoVo.setDescription(interfaceInfo.getDescription());
        }, executor);

        //5、使用雪花算法生成订单id，并保存订单
        String orderSn = generateOrderSn(loginUserVo.getId().toString());
        ApiOrder apiOrder = new ApiOrder();
        apiOrder.setTotalAmount(totalAmount);
        apiOrder.setOrderSn(orderSn);
        apiOrder.setOrderNum(orderNum);
        apiOrder.setStatus(OrderConstant.toBePaid);
        apiOrder.setInterfaceId(interfaceId);
        apiOrder.setUserId(userId);
        apiOrder.setCharging(charging);
        try {
            apiOrderMapper.insert(apiOrder);
        }catch (Exception e){
            throw new BusinessException(ErrorCode.OPERATION_ERROR,"订单保存失败");
        }
        //6、锁定剩余库存
        ApiOrderLock apiOrderLock = new ApiOrderLock();
        apiOrderLock.setUserId(userId);
        apiOrderLock.setLockNum(orderNum);
        apiOrderLock.setLockStatus(1);
        apiOrderLock.setChargingId(interfaceId);
        apiOrderLock.setOrderSn(orderSn);
        try {
            apiOrderLockService.save(apiOrderLock);
        }catch (Exception e){throw new BusinessException(ErrorCode.OPERATION_ERROR,"库存锁定失败");}
        //7、远程更新剩余可调用接口数量
        LockChargingVo lockChargingVo = new LockChargingVo();
        lockChargingVo.setOrderNum(orderNum);
        lockChargingVo.setInterfaceid(interfaceId);
        BaseResponse updateAvailablePieces = userFeignServices.updateAvailablePieces(lockChargingVo);
        if (updateAvailablePieces.getCode()!=0){
            throw new BusinessException(ErrorCode.OPERATION_ERROR,"库存更新失败");
        }
        //8、全部锁定完成后，向mq延时队列发送订单消息，且30分钟过期
        rabbitOrderUtils.sendOrderSnInfo(apiOrder);
        //等待异步任务完成
        CompletableFuture.allOf(voidCompletableFuture).get();
        //9、构建返回给前端页面的数据
        OrderSnVo orderSnVo = new OrderSnVo();
        BeanUtils.copyProperties(apiOrder,orderSnVo);
        DateTime date = DateUtil.date();
        orderSnVo.setCreateTime(date);
        orderSnVo.setExpirationTime(DateUtil.offset(date, DateField.MINUTE,30));
        orderSnVo.setName(orderInterfaceInfoVo.getName());
        orderSnVo.setDescription(orderInterfaceInfoVo.getDescription());
        return ResultUtils.success(orderSnVo);
    }

    /**
     * 生成防重令牌：保证创建订单的接口幂等性
     * @param id
     * @param response
     * @return
     */
    @Override
        public BaseResponse generateToken(Long id,HttpServletResponse response) {
        if (null == id){
            throw new BusinessException(ErrorCode.FORBIDDEN_ERROR);
        }
        //防重令牌
        String token = IdUtil.simpleUUID();
        redisTemplate.opsForValue().set(OrderConstant.USER_ORDER_TOKEN_PREFIX + id,token,30, TimeUnit.MINUTES);
        Cookie cookie = new Cookie(CookieConstant.orderToken,token);
        cookie.setPath("/");
        cookie.setMaxAge(CookieConstant.orderTokenExpireTime);
        response.addCookie(cookie);
        return ResultUtils.success(null);
    }

    /**
     * 取消订单
     * @param apiOrderCancelDto
     * @param request
     * @param response
     * @return
     */
    @Override
    public BaseResponse cancelOrderSn(ApiOrderCancelDto apiOrderCancelDto, HttpServletRequest request, HttpServletResponse response) {
        Long orderNum = apiOrderCancelDto.getOrderNum();
        String orderSn = apiOrderCancelDto.getOrderSn();
        //订单已经被取消的情况
        ApiOrder orderSn1 = this.getOne(new QueryWrapper<ApiOrder>().eq("orderSn", orderSn));
        if (orderSn1.getStatus() == 2){
            return ResultUtils.success("取消订单成功");
        }
        //更新库存状态信息表
        apiOrderLockService.update(new UpdateWrapper<ApiOrderLock>().eq("orderSn", orderSn).set("lockStatus",0));
        //更新订单表状态
        this.update(new UpdateWrapper<ApiOrder>().eq("orderSn", orderSn).set("status",2));
        //远程调用 - 解锁库存
        LockChargingVo lockChargingVo = new LockChargingVo();
        lockChargingVo.setOrderNum(orderNum);
        lockChargingVo.setInterfaceid(apiOrderCancelDto.getInterfaceId());
        BaseResponse res = userFeignServices.unlockAvailablePieces(lockChargingVo);
        if (res.getCode() != 0 ){
            throw new RuntimeException();
        }
        return ResultUtils.success("取消订单成功");
    }

    /**
     * 扣减库存相关操作
     * @param orderSn
     */
    @Override
    public void orderPaySuccess(String orderSn) {
        this.update(new UpdateWrapper<ApiOrder>().eq("orderSn",orderSn).set("status",1));
    }

    /**
     * 获取当前登录用户的status订单信息
     * @param statusInfoDto
     * @param request
     * @return
     */
    @Override
    public BaseResponse<Page<ApiOrderStatusVo>> getCurrentOrderInfo(ApiOrderStatusInfoDto statusInfoDto, HttpServletRequest request) {
        Long userId = statusInfoDto.getUserId();
        //前端筛选即可
        Integer status = statusInfoDto.getStatus();
        if (null == userId){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        long current = statusInfoDto.getCurrent();
        // 限制爬虫
        long size = statusInfoDto.getPageSize();
        if (size > 20) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Page<ApiOrderStatusVo> apiOrderStatusVo = apiOrderMapper.getCurrentOrderInfo(new Page<>(current, size),userId,status);
        List<ApiOrderStatusVo> records = apiOrderStatusVo.getRecords();
        List<ApiOrderStatusVo> collect = records.stream().map(record -> {
            Integer status1 = record.getStatus();
            if (status1 == 0) {
                Date date = record.getCreateTime();
                record.setExpirationTime(DateUtil.offset(date, DateField.MINUTE, 30));
            }
            return record;
        }).collect(Collectors.toList());
        apiOrderStatusVo.setRecords(collect);
        return ResultUtils.success(apiOrderStatusVo);
    }

    /**
     * 获取echarts图中最近7天的交易数
     * @param dateList
     * @return
     */
    @Override
    public BaseResponse getOrderEchartsData(List<String> dateList) {
        List<EchartsVo> list=apiOrderMapper.getOrderEchartsData(dateList);
        return ResultUtils.success(list);
    }


    /**
     * 生成订单号
     * @return
     */
    private String generateOrderSn(String userId){
        String timeId = IdWorker.getTimeId();
        String substring = timeId.substring(0, timeId.length() - 15);
        return substring + RandomUtil.randomNumbers(5) + userId.substring(userId.length()-2,userId.length());
    }
}




