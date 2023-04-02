package com.wzy.thirdParty.feign;

import common.BaseResponse;
import common.to.Oauth2ResTo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @author YukeSeko
 */
@FeignClient(value = "api",url = "http://localhost:7529/api/")
public interface UserFeignServices {

    /**
     * 第三方登录
     * @param oauth2ResTo
     * @param type
     * @return
     */
    @PostMapping("/user/oauth2/login")
    BaseResponse oauth2Login(@RequestBody Oauth2ResTo oauth2ResTo,@RequestParam("type") String type);
}
