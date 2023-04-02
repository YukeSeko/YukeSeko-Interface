package com.wzy.api.common;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.wzy.api.model.entity.User;
import com.wzy.api.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * @author YukeSeko
 */
@Service
public class UserDetailsImpl implements UserDetailsService {

    @Autowired
    private UserService userService;


    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userService.getOne(new QueryWrapper<User>().eq("userAccount", username).or().eq("mobile", username));
        if (null == user){
            throw new RuntimeException("用户不存在");
        }
        //隐藏手机号
        String mobile = user.getMobile();
        String newMobile = mobile.substring(0, 3) + "****" + mobile.substring(7);
        user.setMobile(newMobile);
        return user;
    }
}
