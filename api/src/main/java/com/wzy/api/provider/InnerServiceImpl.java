/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.wzy.api.provider;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import common.Exception.BusinessException;
import com.wzy.api.model.entity.UserInterfaceInfo;
import com.wzy.api.service.UserInterfaceInfoService;
import com.wzy.api.service.UserService;
import common.ErrorCode;
import org.apache.dubbo.config.annotation.DubboService;

import javax.annotation.Resource;

/**
 * @author YukeSeko
 */
@DubboService
public class InnerServiceImpl implements InnerService {

    @Resource
    private UserInterfaceInfoService userInterfaceInfoService;


    @Resource
    private UserService userService;



    /**
     * 调用接口次数统计
     * @param interfaceInfoId
     * @param userId
     * @return
     */
    @Override
    public boolean invokeCount(long interfaceInfoId, long userId) {
        //todo 该部分需要加锁，实现一个分布式锁，保证数据的一致性

        // 判断
        if (interfaceInfoId <= 0 || userId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        UpdateWrapper<UserInterfaceInfo> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("interfaceInfoId", interfaceInfoId);
        updateWrapper.eq("userId", userId);
        updateWrapper.setSql("leftNum = leftNum - 1, totalNum = totalNum + 1");
        return userInterfaceInfoService.update(updateWrapper);
    }

    /**
     * 判断用户在该接口上是否还有调用次数
     * @param interfaceInfoId
     * @param userId
     * @return
     */
    @Override
    public boolean hasCount(long interfaceInfoId, long userId) {
        //判空
        if (interfaceInfoId <= 0 || userId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        UserInterfaceInfo one = userInterfaceInfoService.getOne(new QueryWrapper<UserInterfaceInfo>()
                .eq("interfaceInfoId", interfaceInfoId)
                .eq("userId", userId)
                .gt("leftNum", 0));
        return one == null ? false :true;
    }

}
