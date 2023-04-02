package com.wzy.api.util;

import cn.hutool.http.HttpRequest;
import org.springframework.stereotype.Component;

@Component
public class HttpUtil {

    private String baseUrl = "http://localhost:8100/api";

    public String httpGet(String url){
        String s = baseUrl + url;
        return HttpRequest.get(s).execute().body();
    }
}
