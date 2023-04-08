package com.wzy.apiclient.client;

import cn.hutool.core.date.DateUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONUtil;
import com.wzy.apiclient.constant.Method;
import com.wzy.apiclient.model.Api;

import java.util.HashMap;
import java.util.Map;

public class ApiClient {

    private Integer appId;
    private String accessKey;
    private String secretKey;
    private String url = "http://localhost:88/api/main";

    public ApiClient() {
    }

    public ApiClient(Integer appId, String accessKey, String secretKey) {
        this.appId = appId;
        this.accessKey = accessKey;
        this.secretKey = secretKey;
    }

    public String getResult(Api api) {
        String json = JSONUtil.toJsonStr(api.getBody());
        if ("get".equals(api.getMethod()) || "GET".equals(api.getMethod())) {
            return HttpRequest.get(url)
                    .header("Accept","application/json;charset=UTF-8")
                    .addHeaders(getHeaders(api.getUrl(),api.getInterfaceId(),json, api.getId(), api.getUserAccount()))
                    .charset("UTF-8")
                    .body(json)
                    .execute().body();
        } else {
            return HttpRequest.post(url)
                    .header("Accept","application/json;charset=UTF-8")
                    .addHeaders(getHeaders(api.getUrl(),api.getInterfaceId(),json, api.getId(), api.getUserAccount()))
                    .charset("UTF-8")
                    .body(json)
                    .execute().body();
        }
    }

    /**
     * 设置请求参数
     * @param body
     * @param userId
     * @param userAccount
     * @return
     */
    private Map<String, String> getHeaders(String url,String interfaceId,String body, Long userId, String userAccount) {
        Map<String, String> map = new HashMap<>();
        map.put("userId", String.valueOf(userId));
        map.put("userAccount", userAccount);
        map.put("interfaceId", interfaceId);
        map.put("url", url);
        map.put("appId", String.valueOf(appId));
        map.put("accessKey", accessKey);
        map.put("secretKey", secretKey);
        map.put("body", body);
        map.put("timestamp", String.valueOf(DateUtil.date(System.currentTimeMillis())));
        return map;
    }


}
