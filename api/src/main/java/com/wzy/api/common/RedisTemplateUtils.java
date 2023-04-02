package com.wzy.api.common;

import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wzy.api.model.vo.AllInterfaceInfoVo;
import common.constant.RedisConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * @author YukeSeko
 */
@Component
public class RedisTemplateUtils {

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 缓存首页和请求次数中所有接口
     * @param allInterfaceInfoVoPage
     */
    public void onlinePageCache(Page<AllInterfaceInfoVo> allInterfaceInfoVoPage){
        //设置随机过期时间，解决缓存雪崩问题
        int c = RandomUtil.randomInt(60,120);
        //数据为空也缓存数据，解决缓存穿透问题
        redisTemplate.opsForValue().set(RedisConstant.onlinePageCacheKey+allInterfaceInfoVoPage.getCurrent(),allInterfaceInfoVoPage,c, TimeUnit.MINUTES);

    }

    /**
     * 根据当前页面数获取缓存
     * @param current
     * @return
     */
    public Page<AllInterfaceInfoVo> getOnlinePage(long current){
        Page<AllInterfaceInfoVo> onlinePage = (Page<AllInterfaceInfoVo>) redisTemplate.opsForValue().get(RedisConstant.onlinePageCacheKey + current);
        return onlinePage;
    }

    /**
     * 删除所有缓存
     */
    public void delAllOnlinePage(){
        Set<String> keys = redisTemplate.keys(RedisConstant.onlinePageCacheKey + "*");
        redisTemplate.delete(keys);
    }
}
