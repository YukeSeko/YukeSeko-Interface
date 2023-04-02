package com.wzy.api.common;


import com.tencentcloudapi.common.exception.TencentCloudSDKException;
import com.tencentcloudapi.sms.v20210111.SmsClient;
import com.tencentcloudapi.sms.v20210111.models.SendSmsRequest;
import com.tencentcloudapi.sms.v20210111.models.SendSmsResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
/**
 * @author YukeSeko
 */
@Component
public class SendSmsUtils {

    @Autowired
    private TencentClient tencentClient;

    @Value("${tencent.sdkAppId}")
    private String sdkAppId;
    @Value("${tencent.signName}")
    private String signName;
    @Value("${tencent.templateId}")
    private String templateId;

    /**
     * 发送短信工具
     * @param phone
     * @return
     * @throws TencentCloudSDKException
     */
    public SendSmsResponse sendSmsResponse (String phone,String code) throws TencentCloudSDKException {
        SendSmsRequest req = new SendSmsRequest();

        /* 短信应用ID */
        // 应用 ID 可前往 [短信控制台](https://console.cloud.tencent.com/smsv2/app-manage) 查看
        req.setSmsSdkAppId(sdkAppId);

        /* 短信签名内容: 使用 UTF-8 编码，必须填写已审核通过的签名 */
        // 签名信息可前往 [国内短信](https://console.cloud.tencent.com/smsv2/csms-sign) 或 [国际/港澳台短信](https://console.cloud.tencent.com/smsv2/isms-sign) 的签名管理查看
        req.setSignName(signName);

        /* 模板 ID: 必须填写已审核通过的模板 ID */
        // 模板 ID 可前往 [国内短信](https://console.cloud.tencent.com/smsv2/csms-template) 或 [国际/港澳台短信](https://console.cloud.tencent.com/smsv2/isms-template) 的正文模板管理查看
        req.setTemplateId(templateId);

        /* 模板参数: 模板参数的个数需要与 TemplateId 对应模板的变量个数保持一致，若无模板参数，则设置为空 */
        String[] templateParamSet = {code,"5"};
        req.setTemplateParamSet(templateParamSet);

        /* 下发手机号码，采用 E.164 标准，+[国家或地区码][手机号]
         * 示例如：+8613711112222， 其中前面有一个+号 ，86为国家码，13711112222为手机号，最多不要超过200个手机号 */
        String[] phoneNumberSet = new String[]{"+86" + phone};
        req.setPhoneNumberSet(phoneNumberSet);

        /* 用户的 session 内容（无需要可忽略）: 可以携带用户侧 ID 等上下文信息，server 会原样返回
        String sessionContext = "";
        req.setSessionContext(sessionContext);
        */

        /* 通过 client 对象调用 SendSms 方法发起请求。注意请求方法名与请求对象是对应的
         * 返回的 res 是一个 SendSmsResponse 类的实例，与请求对象对应 */
        SmsClient client = tencentClient.client();
        return client.SendSms(req);
    }
}
