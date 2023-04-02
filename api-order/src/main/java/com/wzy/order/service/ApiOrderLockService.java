package com.wzy.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wzy.order.model.entity.ApiOrderLock;

/**
* @author 12866
* @description 针对表【order_lock】的数据库操作Service
* @createDate 2023-03-16 11:17:54
*/
public interface ApiOrderLockService extends IService<ApiOrderLock> {

    /**
     * 扣减库存
     * @param orderSn
     */
    void orderPaySuccess(String orderSn);
}
