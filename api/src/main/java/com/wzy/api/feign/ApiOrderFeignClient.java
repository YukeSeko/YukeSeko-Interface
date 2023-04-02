package com.wzy.api.feign;

import common.BaseResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * @author YukeSeko
 */
@FeignClient(value = "api-order",url = "http://localhost:8300/api/")
public interface ApiOrderFeignClient {

    /**
     * 获取echarts图中最近7天的交易数
     * @return
     */
    @PostMapping("/order/getOrderEchartsData")
    BaseResponse getOrderEchartsData(@RequestBody List<String> dateList);
}
