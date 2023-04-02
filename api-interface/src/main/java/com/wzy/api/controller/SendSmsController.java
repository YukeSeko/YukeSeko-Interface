package com.wzy.api.controller;

import com.tencentcloudapi.common.exception.TencentCloudSDKException;
import com.tencentcloudapi.sms.v20210111.models.SendSmsResponse;
import com.tencentcloudapi.sms.v20210111.models.SendStatus;
import com.wzy.api.common.SendSmsUtils;
import common.AuthPhoneNumber;
import common.BaseResponse;
import common.ErrorCode;
import common.Utils.ResultUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author YukeSeko
 */
@Slf4j
@RequestMapping("/sms")
@RestController
public class SendSmsController {

    @Autowired
    private SendSmsUtils sendSmsUtils;

    /**
     * 通过get方式发送测试短信
     * 该部分发送验证码不需要发送到mq，因为该接口是用户购买了次数的接口，则不需要进行限流，也不需要存入redis
     * 直接发送验证码即可
     * @param phone
     * @return
     */
    @GetMapping("/get")
    public BaseResponse sendSmsByGet(@RequestParam Object phone){
        if (phone == null ){
            return ResultUtils.error(ErrorCode.PARAMS_ERROR);
        }
        //验证手机号的合法性
        AuthPhoneNumber authPhoneNumber = new AuthPhoneNumber();
        if(!authPhoneNumber.isPhoneNum(phone.toString())){
            return ResultUtils.error(ErrorCode.PARAMS_ERROR,"手机号非法");
        }
        SendSmsResponse response = null;
        int code = (int)((Math.random() * 9 + 1) * 10000);
        try {
            response = sendSmsUtils.sendSmsResponse(phone.toString(), String.valueOf(code));
            log.info("发送验证码成功---->手机号为{}，验证码为{}",phone,code);
        } catch (TencentCloudSDKException e) {
            throw new RuntimeException(e);
        }
        SendStatus[] sendStatusSet = response.getSendStatusSet();
        SendStatus sendStatus = sendStatusSet[0];
        String statusCode = sendStatus.getCode();
        String message = sendStatus.getMessage();
        if(!"OK".equals(statusCode) || "send success".equals(message)){
            return ResultUtils.error(ErrorCode.OPERATION_ERROR,message);
        }
        return ResultUtils.success("发送成功");
    }
}
