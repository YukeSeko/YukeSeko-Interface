package com.wzy.api.common;

import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.web.util.UriBuilder;

import java.net.URI;

/**
 * 兼容微信的oauth2 端点.
 *
 * @author YukeSeko
 */
public class WechatOAuth2AuthRequestBuilderCustomizer {
   private static final String WECHAT_ID= "wechat";

    /**
     * Customize.
     *
     * @param builder the builder
     */
    public static void customize(OAuth2AuthorizationRequest.Builder builder) {
       String regId = (String) builder.build()
               .getAttributes()
               .get(OAuth2ParameterNames.REGISTRATION_ID);
       if (WECHAT_ID.equals(regId)){
           builder.authorizationRequestUri(WechatOAuth2RequestUriBuilderCustomizer::customize);
       }
    }

    /**
     * 定制微信OAuth2请求URI
     *
     * @author n1
     * @since 2021 /8/11 15:31
     */
    private static class WechatOAuth2RequestUriBuilderCustomizer {

        /**
         * 默认情况下Spring Security会生成授权链接：
         * {@code https://open.weixin.qq.com/connect/oauth2/authorize?response_type=code
         * &client_id=wxdf9033184b238e7f
         * &scope=snsapi_userinfo
         * &state=5NDiQTMa9ykk7SNQ5-OIJDbIy9RLaEVzv3mdlj8TjuE%3D
         * &redirect_uri=https%3A%2F%2Fmovingsale-h5-test.nashitianxia.com}
         * 缺少了微信协议要求的{@code #wechat_redirect}，同时 {@code client_id}应该替换为{@code app_id}
         *
         * @param builder the builder
         * @return the uri
         */
        public static URI customize(UriBuilder builder) {
            String reqUri = builder.build().toString()
                    .replaceAll("client_id=", "appid=")
                    .concat("#wechat_redirect");
            return URI.create(reqUri);
        }
    }
}