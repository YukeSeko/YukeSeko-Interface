package com.wzy.gateway.config;

import cn.hutool.core.text.AntPathMatcher;
import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.alibaba.nacos.common.model.RestResult;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wzy.api.provider.InnerService;
import common.BaseResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 全局请求拦截过滤
 * 设计思路：
 * 1、每一个接口都设计一个appId，每一个接口都给用户分配不同的secretKey
 * 2、所有的接口都设计只有一个appId，每个用户只有一个初始的secretKey
 * @author YukeSeko
 */
@Component
@Slf4j
public class CustomGlobalFilter implements GlobalFilter, Ordered {


    @DubboReference
    private InnerService innerService;

    private static final List<String>  PATH_WHITE_LIST = Arrays.asList("/api/apiclient","/api/user/**");

    //需要登录才能进行访问
    private static final List<String> PATH_LOGIN_LIST = Arrays.asList("/api/userInterfaceInfo/**" ,"/api/interfaceInfo/**","/api/auth/**","/api/oauth/**","/api/order/**","/api/alipay/**");

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();
        String path = request.getPath().toString();
        // 1. 打印请求日志
        logPrint(request);
        //查询用户是否登录时、用户登录等请求，直接放行
        List<Boolean> collect = PATH_WHITE_LIST.stream().map(item -> {
            AntPathMatcher antPathMatcher = new AntPathMatcher();
            return antPathMatcher.match(item, path);
        }).collect(Collectors.toList());
        if(collect.contains(true)){
            return chain.filter(exchange);
        }
        // 2、网关统一鉴权：其他接口需要判断用户是否登录
        List<Boolean> collectLogin = PATH_LOGIN_LIST.stream().map(item -> {
            AntPathMatcher antPathMatcher = new AntPathMatcher();
            return antPathMatcher.match(item, path);
        }).collect(Collectors.toList());
        HttpHeaders headers = request.getHeaders();
        String cookie = headers.getFirst("Cookie");
        if(collectLogin.contains(true)){
            String loginUserVo = HttpRequest.post("http://localhost:7529/api/user/checkUserLogin")
                    .header("Cookie", cookie)
                    .timeout(20000)
                    .execute().body();
            JSONObject entries = JSONUtil.parseObj(loginUserVo);
            BaseResponse baseResponse = JSONUtil.toBean(entries, BaseResponse.class);
            Object data = baseResponse.getData();
            if (null == data){
                response.setStatusCode(HttpStatus.FORBIDDEN);
                return response.setComplete();
            }else {
                return chain.filter(exchange);
            }
        }
        // 3. 用户鉴权在服务端鉴别，这里只做判空
        String accessKey = headers.getFirst("accessKey");
        String appId = headers.getFirst("appId");
        String secretKey = headers.getFirst("secretKey");
        String userId = headers.getFirst("userId");
        String interfaceId = headers.getFirst("interfaceId");
        if(appId.isEmpty() || secretKey.isEmpty() || accessKey.isEmpty() || userId.isEmpty()){
            response.setStatusCode(HttpStatus.FORBIDDEN);
            return response.setComplete();
        }
        // 4。判断用户剩余调用次数是否足够
        boolean hasCount = innerService.hasCount(Long.parseLong(interfaceId), Long.parseLong(userId));
        if(!hasCount){
            //调用次数不足，自定义返回结果
            response.setStatusCode(HttpStatus.FORBIDDEN);
            DataBufferFactory bufferFactory = response.bufferFactory();
            ObjectMapper objectMapper = new ObjectMapper();
            DataBuffer wrap = null;
            try {
                wrap = bufferFactory.wrap(objectMapper.writeValueAsBytes(new RestResult<>(403, "接口调用次数不足，禁止调用，请联系管理员")));
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
            DataBuffer finalWrap = wrap;
            return response.writeWith(Mono.fromSupplier(() -> finalWrap));
        }
        return handleResponse(exchange, chain, Long.parseLong(interfaceId), Long.parseLong(userId));
    }


    @Override
    public int getOrder() {
        return -1;
    }

    /**
     * 打印基本日志信息
     *
     * @param request
     */
    private void logPrint(ServerHttpRequest request) {
        log.info("=====  {} 请求开始 =====", request.getId());
        String path = request.getPath().value();
        String method = request.getMethod().toString();
        log.info("请求路径：" + path);
        log.info("请求方法：" + method);
        log.info("请求参数：" + request.getQueryParams());
        String sourceAddress = request.getLocalAddress().getHostString();
        log.info("请求来源地址：" + sourceAddress);
        //log.info("请求来源地址：" + request.getRemoteAddress());
    }

    /**
     * 处理响应
     * 使用装饰者模式
     *
     * @param exchange
     * @param chain
     * @return
     */
    private Mono<Void> handleResponse(ServerWebExchange exchange, GatewayFilterChain chain, long interfaceInfoId, long userId) {
        ServerHttpRequest request = exchange.getRequest();
        try {
            ServerHttpResponse originalResponse = exchange.getResponse();
            // 缓存数据的工厂
            DataBufferFactory bufferFactory = originalResponse.bufferFactory();
            // 拿到响应码
            HttpStatus statusCode = originalResponse.getStatusCode();
            if (statusCode == HttpStatus.OK) {
                // 装饰，增强能力
                ServerHttpResponseDecorator decoratedResponse = new ServerHttpResponseDecorator(originalResponse) {
                    // 等调用完转发的接口后才会执行
                    @Override
                    public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {
                        log.info("body instanceof Flux: {}", (body instanceof Flux));
                        if (body instanceof Flux) {
                            Flux<? extends DataBuffer> fluxBody = Flux.from(body);
                            // 往返回值里写数据
                            // 拼接字符串
                            return super.writeWith(
                                    fluxBody.map(dataBuffer -> {
                                        // 7. 调用成功，接口调用次数 + 1 invokeCount
                                        try {
                                            boolean b = innerService.invokeCount(interfaceInfoId, userId);
                                            log.info("<-------修改接口调用次数：{}", b == true ? "成功" : "失败");
                                        } catch (Exception e) {
                                            log.error("invokeCount error", e);
                                        }
                                        byte[] content = new byte[dataBuffer.readableByteCount()];
                                        dataBuffer.read(content);
                                        DataBufferUtils.release(dataBuffer);//释放掉内存
                                        // 构建日志
                                        StringBuilder sb2 = new StringBuilder(200);
                                        List<Object> rspArgs = new ArrayList<>();
                                        rspArgs.add(originalResponse.getStatusCode());
                                        String data = new String(content, StandardCharsets.UTF_8); //data
                                        sb2.append(data);
                                        // 打印日志
                                        log.info("响应结果：" + data);
                                        log.info("=====  {} 结束 =====", request.getId());
                                        return bufferFactory.wrap(content);
                                    }));
                        } else {
                            // 8. 调用失败，返回一个规范的错误码
                            log.error("<--- {} 响应code异常", getStatusCode());
                        }
                        return super.writeWith(body);
                    }
                };
                //设置response对象为装饰过的
                return chain.filter(exchange.mutate().response(decoratedResponse).build());
            }
            // 降级处理返回数据
            log.info("=====  {} 结束 =====", request.getId());
            return chain.filter(exchange);
        } catch (Exception e) {
            log.error("网关处理响应异常" + e);
            log.info("=====  {} 结束 =====", request.getId());
            return chain.filter(exchange);
        }
    }
}
