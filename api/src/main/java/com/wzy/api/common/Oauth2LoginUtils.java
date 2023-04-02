package com.wzy.api.common;

import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.wzy.api.mapper.AuthMapper;
import com.wzy.api.mapper.UserMapper;
import com.wzy.api.model.entity.Auth;
import com.wzy.api.model.entity.User;
import common.vo.LoginUserVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author YukeSeko
 */
@Component
public class Oauth2LoginUtils {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private TokenUtils tokenUtils;

    @Autowired
    private GenerateAuthUtils generateAuthUtils;

    @Autowired
    private AuthMapper authMapper;

    /**
     * 通过gitee 或者 github 进行登录
     * @param response
     * @return
     */
    public LoginUserVo giteeOrGithubOauth2Login(HttpResponse response){
        JSONObject obj = JSONUtil.parseObj(response.body());
        String userAccount = String.valueOf(obj.get("login"));
        String name = String.valueOf(obj.get("name"));
        String userAvatar = String.valueOf(obj.get("avatar_url"));
        User user = userMapper.selectOne(new QueryWrapper<User>().eq("userAccount", userAccount));
        LoginUserVo loginUserVo = new LoginUserVo();
        if (null != user){
            user.setUserPassword(null);
            String mobile = user.getMobile();
            String newMobile = mobile.substring(0, 3) + "****" + mobile.substring(7);
            user.setMobile(newMobile);
            BeanUtils.copyProperties(user,loginUserVo);
            String token = tokenUtils.generateToken(String.valueOf(loginUserVo.getId()),loginUserVo.getUserAccount());
            loginUserVo.setToken(token);
        }else {
            String appId = String.valueOf((int) ((Math.random() * 9 + 1) * Math.pow(10, 9 - 1)));
            String accessKey = generateAuthUtils.accessKey(appId);
            String secretKey = generateAuthUtils.secretKey(appId,userAccount);
            String token = generateAuthUtils.token(appId,userAccount, accessKey, secretKey);
            Auth auth = new Auth();
            auth.setUseraccount(userAccount);
            auth.setAppid(Integer.valueOf(appId));
            auth.setAccesskey(accessKey);
            auth.setSecretkey(secretKey);
            auth.setToken(token);
            User user1 = new User();
            user1.setUserAccount(userAccount);
            user1.setUserName(name);
            user1.setUserAvatar(userAvatar);
            userMapper.insert(user1);
            auth.setUserid(user1.getId());
            authMapper.insert(auth);
            // 进行登录操作
            User user2 = userMapper.selectById(user1.getId());
            BeanUtils.copyProperties(user2,loginUserVo);
            String token1 = tokenUtils.generateToken(String.valueOf(loginUserVo.getId()),loginUserVo.getUserAccount());
            loginUserVo.setToken(token1);
        }
        return loginUserVo;
    }
}
