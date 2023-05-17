package com.wzy.api.service.impl;

import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.LineCaptcha;
import cn.hutool.captcha.generator.RandomGenerator;
import cn.hutool.core.util.DesensitizedUtil;
import cn.hutool.core.util.ReUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import cn.hutool.jwt.JWT;
import cn.hutool.jwt.JWTUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wzy.api.common.*;
import com.wzy.api.constant.UserConstant;
import com.wzy.api.feign.ApiOrderFeignClient;
import com.wzy.api.mapper.InterfaceInfoMapper;
import common.Exception.BusinessException;
import com.wzy.api.mapper.AuthMapper;
import com.wzy.api.mapper.UserMapper;
import com.wzy.api.model.dto.user.UserBindPhoneRequest;
import com.wzy.api.model.dto.user.UserLoginBySmsRequest;
import com.wzy.api.model.dto.user.UserQueryRequest;
import com.wzy.api.model.dto.user.UserRegisterRequest;
import com.wzy.api.model.entity.Auth;
import com.wzy.api.model.entity.User;
import com.wzy.api.model.vo.UserVO;
import com.wzy.api.service.UserService;
import common.AuthPhoneNumber;
import common.BaseResponse;
import common.ErrorCode;
import common.Utils.CookieUtils;
import common.Utils.ResultUtils;
import common.constant.CookieConstant;
import common.to.Oauth2ResTo;
import common.to.SmsTo;
import common.vo.EchartsVo;
import common.vo.LoginUserVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


/**
 * 用户服务实现类
 *
 * 
 */
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
        implements UserService {

    @Resource
    private UserMapper userMapper;

    @Resource
    private AuthMapper authMapper;

    @Resource
    private InterfaceInfoMapper interfaceInfoMapper;

    @Autowired
    private GenerateAuthUtils generateAuthUtils;

    @Autowired
    private SmsLimiter smsLimiter;

    @Autowired
    private TokenUtils tokenUtils;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    @Lazy
    private UserDetailsService userDetailsService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private MobileSignature mobileSignature;

    @Autowired
    private RabbitUtils rabbitUtils;

    @Autowired
    private Oauth2LoginUtils oauth2LoginUtils;

    @Autowired
    private ApiOrderFeignClient apiOrderFeignClient;

    private static final String CAPTCHA_PREFIX = "api:captchaId:";

    /**
     * 用户注册
     * @param userRegisterRequest
     * @param request
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public long userRegister(UserRegisterRequest userRegisterRequest ,HttpServletRequest request) {
        if (userRegisterRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String userAccount = userRegisterRequest.getUserAccount();
        String userPassword = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();
        String mobile = userRegisterRequest.getMobile();
        String code = userRegisterRequest.getCode();
        String captcha = userRegisterRequest.getCaptcha();
        // 校验
        AuthPhoneNumber authPhoneNumber = new AuthPhoneNumber();
        if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword,mobile,code,captcha)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        // 图形验证码是否正确
        String signature = request.getHeader("signature");
        if (null == signature){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String picCaptcha = (String) redisTemplate.opsForValue().get(CAPTCHA_PREFIX + signature);
        if (null == picCaptcha || authPhoneNumber.isCaptcha(captcha) ||   !captcha.equals(picCaptcha)){
            throw new BusinessException(ErrorCode.OPERATION_ERROR,"图形验证码错误或已经过期，请重新刷新验证码");
        }
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户账号过短");
        }
        if (userPassword.length() < 8 || checkPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户密码过短");
        }
        // 密码和校验密码相同
        if (!userPassword.equals(checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "两次输入的密码不一致");
        }
        if(!authPhoneNumber.isPhoneNum(mobile)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"手机号非法");
        }
        // 手机号和验证码是否匹配
        boolean verify = smsLimiter.verifyCode(mobile, code);
        if (!verify){
            throw new BusinessException(ErrorCode.SMS_CODE_ERROR);
        }
        synchronized (userAccount.intern()) {
            // 账户不能重复
            QueryWrapper<User> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("userAccount", userAccount);
            long count = userMapper.selectCount(queryWrapper);
            if (count > 0) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号重复");
            }
            //手机号不能重复
            String getMapperPhone = userMapper.selectPhone(mobile);
            if (null != getMapperPhone){
                throw new BusinessException(ErrorCode.PARAMS_ERROR,"手机号已经被注册");
            }
            // 2. 加密
            String encryptPassword = passwordEncoder.encode(userPassword);
            // 3. 生成初始密钥
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
            // 4. 插入数据
            User user = new User();
            user.setUserAccount(userAccount);
            user.setUserPassword(encryptPassword);
            user.setMobile(mobile);
            boolean saveResult = this.save(user);
            if (!saveResult) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "注册失败，数据库错误");
            }
            auth.setUserid(user.getId());
            authMapper.insert(auth);
            return user.getId();
        }
    }

    /**
     * 用户通过账号和密码登录
     * @param userAccount  用户账户
     * @param userPassword 用户密码
     * @param response
     * @return
     */
    @Override
    public LoginUserVo userLogin(String userAccount, String userPassword, HttpServletResponse response) {
        // 1. 校验
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号错误");
        }
        if (userPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码错误");
        }
        UserDetails userDetails = userDetailsService.loadUserByUsername(userAccount);
        // 2. 加密
        String password = userDetails.getPassword();
        if (!passwordEncoder.matches(userPassword,password)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号或密码错误");
        }
        User user=  (User) userDetails;
        user.setUserPassword(null);
        return initUserLogin(user,response);
    }

    /**
     * 获取当前登录用户
     *
     * @param request
     * @return
     */
    @Override
    public User getLoginUser(HttpServletRequest request) {
        // 先判断是否已登录
        Principal userPrincipal = request.getUserPrincipal();
        if(userPrincipal == null){
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        String name = userPrincipal.getName();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser =(User) authentication.getPrincipal();
        if (currentUser == null || currentUser.getId() == null || null == name || !currentUser.getUserAccount().equals(name)) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        // 从数据库查询（追求性能的话可以注释，直接走缓存）
        long userId = currentUser.getId();
        currentUser = this.getById(userId);
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        return currentUser;
    }

    /**
     * 是否为管理员
     *
     * @param request
     * @return
     */
    @Override
    public boolean isAdmin(HttpServletRequest request) {
        // 仅管理员可查询
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = (User) principal;
        return user != null && UserConstant.ADMIN_ROLE.equals(user.getUserRole());
    }

    /**
     * 发送验证码
     * @param mobile
     * @return
     */
    @Override
    public BaseResponse captcha(String mobile) {
        if (mobile == null ){
            return ResultUtils.error(ErrorCode.PARAMS_ERROR);
        }
        AuthPhoneNumber authPhoneNumber = new AuthPhoneNumber();
        //验证手机号的合法性
        if(!authPhoneNumber.isPhoneNum(mobile.toString())){
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "手机号非法");
        }
        int code = (int)((Math.random() * 9 + 1) * 10000);
        // 使用redis来存储手机号和验证码 ，同时使用令牌桶算法来实现流量控制
        boolean sendSmsAuth = smsLimiter.sendSmsAuth(mobile, String.valueOf(code));
        if(!sendSmsAuth){
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "发送频率过高，请稍后再试");
        }
        SmsTo smsTo = new SmsTo(mobile,String.valueOf(code));
        try {
            //实际发送短信的功能交给第三方服务去实现
            rabbitUtils.sendSms(smsTo);
        }catch (Exception e){
            //发送失败，删除令牌桶
            redisTemplate.delete("sms:"+mobile+"_last_refill_time");
            redisTemplate.delete("sms:"+mobile+"_tokens");
            throw new BusinessException(ErrorCode.OPERATION_ERROR,"发送验证码失败，请稍后再试");
        }
        log.info("发送验证码成功---->手机号为{}，验证码为{}",mobile,code);
        return ResultUtils.success("发送成功");
    }

    /**
     * 获取用户列表
     * @param userQueryRequest
     * @return
     */
    @Override
    public BaseResponse<List<UserVO>> lisUser(UserQueryRequest userQueryRequest) {
        User userQuery = new User();
        if (userQueryRequest != null) {
            BeanUtils.copyProperties(userQueryRequest, userQuery);
        }
        QueryWrapper<User> queryWrapper = new QueryWrapper<>(userQuery);
        List<User> userList = this.list(queryWrapper);
        List<UserVO> userVOList = userList.stream().map(user -> {
            UserVO userVO = new UserVO();
            BeanUtils.copyProperties(user, userVO);
            return userVO;
        }).collect(Collectors.toList());
        return ResultUtils.success(userVOList);
    }

    /**
     * 分页获取用户列表
     * @param userQueryRequest
     * @return
     */
    @Override
    public BaseResponse<Page<UserVO>> listUserByPage(UserQueryRequest userQueryRequest) {
        long current = 1;
        long size = 10;
        User userQuery = new User();
        if (userQueryRequest != null) {
            BeanUtils.copyProperties(userQueryRequest, userQuery);
            current = userQueryRequest.getCurrent();
            size = userQueryRequest.getPageSize();
        }
        QueryWrapper<User> queryWrapper = new QueryWrapper<>(userQuery);
        Page<User> userPage = this.page(new Page<>(current, size), queryWrapper);
        Page<UserVO> userVOPage = new PageDTO<>(userPage.getCurrent(), userPage.getSize(), userPage.getTotal());
        List<UserVO> userVOList = userPage.getRecords().stream().map(user -> {
            UserVO userVO = new UserVO();
            BeanUtils.copyProperties(user, userVO);
            userVO.setUserName(user.getNickName());
            return userVO;
        }).collect(Collectors.toList());
        userVOPage.setRecords(userVOList);
        return ResultUtils.success(userVOPage);
    }

    /**
     * 用户通过手机号进行登录
     * @param loginBySms
     * @param response
     * @return
     */
    @Override
    public BaseResponse loginBySms(UserLoginBySmsRequest loginBySms, HttpServletResponse response) {
        String mobile = loginBySms.getMobile();
        String code = loginBySms.getCode();
        if ( null == mobile || null == code){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //验证手机号的合法性
        AuthPhoneNumber authPhoneNumber = new AuthPhoneNumber();
        if(!authPhoneNumber.isPhoneNum(mobile.toString())){
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "手机号非法");
        }
        //验证用户输入的手机号和验证码是否匹配
        boolean verify = smsLimiter.verifyCode(mobile, code);
        if (!verify){
            throw new BusinessException(ErrorCode.SMS_CODE_ERROR);
        }
        //验证该手机号是否完成注册
        UserDetails userDetails = userDetailsService.loadUserByUsername(mobile);
        User user=  (User) userDetails;
        user.setUserPassword(null);
        LoginUserVo loginUserVo = initUserLogin(user,response);
        return ResultUtils.success(loginUserVo);
    }

    /**
     * 生成图形验证码
     * @param request
     * @param response
     */
    @Override
    public void getCaptcha(HttpServletRequest request, HttpServletResponse response) {
        // 随机生成 4 位验证码
        RandomGenerator randomGenerator = new RandomGenerator("0123456789", 4);
        // 定义图片的显示大小
        LineCaptcha lineCaptcha = null;
        lineCaptcha = CaptchaUtil.createLineCaptcha(100, 30);
        response.setContentType("image/jpeg");
        response.setHeader("Pragma", "No-cache");
        // 在前端发送请求时携带captchaId，用于标识不同的用户。
        String signature = request.getHeader("signature");
        if (null == signature){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        try {
            // 调用父类的 setGenerator() 方法，设置验证码的类型
            lineCaptcha.setGenerator(randomGenerator);
            // 输出到页面
            lineCaptcha.write(response.getOutputStream());
            // 打印日志
            log.info("captchaId：{} ----生成的验证码:{}", signature,lineCaptcha.getCode());
            // 关闭流
            response.getOutputStream().close();
            //将对应的验证码存入redis中去，2分钟后过期
            redisTemplate.opsForValue().set(CAPTCHA_PREFIX+signature,lineCaptcha.getCode(),4, TimeUnit.MINUTES);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 用户忘记密码，返回用户注册时的手机号
     * @param username
     * @return
     */
    @Override
    public BaseResponse getPassUserType(String username,HttpServletResponse response) {
        if (null == username){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //sql优化：由原来的查询时间1.15s降低至714ms，还可以通过建立索引来进一部降低时间
        String mobile = userMapper.getMobile(username);
        if (null == mobile){
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR,"未注册");
        }
        //手机号签名时长为10分钟
        long expiryTime = System.currentTimeMillis() + 1000L * (long) 600;
        String signature =null;
        try {
            // 将手机号进行加密后，存入redis
            signature = mobileSignature.makeMobileSignature(username);
            String encryptHex = mobileSignature.makeEncryptHex(username, mobile);
            redisTemplate.opsForValue().set(signature,encryptHex,600,TimeUnit.SECONDS);
        } catch (NoSuchAlgorithmException e) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR);
        }
        Cookie cookie = new Cookie("api-mobile-signature", signature);
        cookie.setMaxAge(600);
        cookie.setPath("/");//登陆页面下才可以访问
        response.addCookie(cookie);
        String newMobile = mobile.substring(0, 3) + "****" + mobile.substring(7);
        return ResultUtils.success(newMobile);
    }

    /**
     * 忘记密码请求第二步，发送验证码
     * @param request
     * @return
     */
    @Override
    public BaseResponse sendPassUserCode(HttpServletRequest request) throws NoSuchAlgorithmException {
        Cookie[] cookies = request.getCookies();
        String value = null;
        for (Cookie cookie : cookies) {
            String name = cookie.getName();
            if ("api-mobile-signature".equals(name)){
                value = cookie.getValue();
            }
        }
        if (null == value){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //从redis中拿到加密后的手机号
        String s = (String) redisTemplate.opsForValue().get(value);
        String[] strings = mobileSignature.decodeHex(s);
        //验证签名
        String username = strings[0];
        String signature = mobileSignature.makeMobileSignature(username);
        if (!signature.equals(value)){
            throw new BusinessException(ErrorCode.FORBIDDEN_ERROR,"请求数据非法");
        }
        //发送对应的验证码
        String mobile = strings[1];
        BaseResponse response = this.captcha(mobile);
        if (response.getCode() != 0){
            throw new BusinessException(ErrorCode.OPERATION_ERROR,"发送验证码失败");
        }
        return ResultUtils.success("发送验证码成功");
    }

    /**
     * 忘记密码部分-验证手机号和验证码输入是否正确
     * @param code
     * @param request
     * @return
     */
    @Override
    public BaseResponse authPassUserCode(String code, HttpServletRequest request) {
        if (null == code){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Cookie[] cookies = request.getCookies();
        String value = null;
        for (Cookie cookie : cookies) {
            String name = cookie.getName();
            if ("api-mobile-signature".equals(name)){
                value = cookie.getValue();
            }
        }
        // value为空会停止执行下面的语句
        assert value != null;
        //从redis中拿到加密后的手机号
        String s = (String) redisTemplate.opsForValue().get(value);
        String[] strings = mobileSignature.decodeHex(s);
        String mobile = strings[1];
        boolean verifyCode = smsLimiter.verifyCode(mobile, code);
        if (!verifyCode){
            throw new BusinessException(ErrorCode.SMS_CODE_ERROR);
        }
        return ResultUtils.success("验证成功");
    }

    /**
     * 修改用户密码
     * @param password
     * @param request
     * @return
     */
    @Override
    public BaseResponse updateUserPass(String password, HttpServletRequest request) {
        if (null == password){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Cookie[] cookies = request.getCookies();
        String value = null;
        for (Cookie cookie : cookies) {
            String name = cookie.getName();
            if ("api-mobile-signature".equals(name)){
                value = cookie.getValue();
            }
        }
        // value为空会停止执行下面的语句
        assert value != null;
        //从redis中拿到加密后的信息
        String s = (String) redisTemplate.opsForValue().get(value);
        String[] strings = mobileSignature.decodeHex(s);
        String username = strings[0];
        if (null == username){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //更新密码
        String encode = passwordEncoder.encode(password);
        boolean update = this.update(new UpdateWrapper<User>().eq("userAccount", username).set("userPassword", encode));
        if (!update){
            throw new BusinessException(ErrorCode.OPERATION_ERROR);
        }
        redisTemplate.delete(value);
        return ResultUtils.success("修改成功");
    }

    /**
     * 通过第三方登录
     * @param oauth2ResTo
     * @param type
     * @param httpServletResponse
     * @return
     */
    @Transactional
    @Override
    public BaseResponse oauth2Login(Oauth2ResTo oauth2ResTo, String type, HttpServletResponse httpServletResponse) {
        String accessToken = oauth2ResTo.getAccess_token();
        if (null == accessToken){
            return ResultUtils.error(ErrorCode.PARAMS_ERROR);
        }
        //拿到用户的信息
        LoginUserVo loginUserVo =null;
        if ("gitee".equals(type)){
            HttpResponse response = HttpRequest.get("https://gitee.com/api/v5/user?access_token=" + accessToken).execute();
            loginUserVo = oauth2LoginUtils.giteeOrGithubOauth2Login(response);
        }else {
            HttpResponse userInfo = HttpRequest.get("https://api.github.com/user")
                    .header("Authorization","Bearer "+accessToken)
                    .timeout(20000)
                    //超时，毫秒
                    .execute();
            loginUserVo = oauth2LoginUtils.giteeOrGithubOauth2Login(userInfo);
        }
        return ResultUtils.success(loginUserVo);
    }

    /**
     * 检查用户登录状态
     * @param request
     * @param response
     * @return
     */
    @Override
    public BaseResponse checkUserLogin(HttpServletRequest request, HttpServletResponse response) {
        // 先判断是否已登录
        Principal userPrincipal = request.getUserPrincipal();
        if (userPrincipal!=null){
            String name = userPrincipal.getName();
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User currentUser =(User) authentication.getPrincipal();
            if (currentUser == null || currentUser.getId() == null || null == name || !currentUser.getUserAccount().equals(name)) {
                throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
            }
            LoginUserVo loginUserVo = new LoginUserVo();
            BeanUtils.copyProperties(currentUser,loginUserVo);
            return ResultUtils.success(loginUserVo);
        }else {
            Cookie[] cookies = request.getCookies();
            if ( null == cookies||cookies.length == 0){
                return ResultUtils.success(null);
            }
            String authorization =null;
            String remember = null;
            for (Cookie cookie : cookies) {
                String name = cookie.getName();
                if (CookieConstant.headAuthorization.equals(name)){
                    authorization = cookie.getValue();
                }
                if (CookieConstant.autoLoginAuthCheck.equals(name)){
                    remember = cookie.getValue();
                }
            }
            if (null == authorization || null ==remember ){
                throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
            }
            CookieUtils cookieUtils = new CookieUtils();
            String[] strings = cookieUtils.decodeAutoLoginKey(remember);
            if (strings.length!=3){
                throw new BusinessException(ErrorCode.ILLEGAL_ERROR,"请重新登录");
            }
            String sId = strings[0];
            String sUserAccount = strings[1];
            JWT jwt = JWTUtil.parseToken(authorization);
            String id = (String) jwt.getPayload("id");
            String userAccount = (String) jwt.getPayload("userAccount");
            if (!sId.equals(id) || !sUserAccount.equals(userAccount)){
                throw new BusinessException(ErrorCode.PARAMS_ERROR,"请重新登录");
            }
            User byId = this.getById(id);
            byId.setUserPassword(null);
            String phone = DesensitizedUtil.mobilePhone(byId.getMobile());
            byId.setMobile(phone);
            return ResultUtils.success(initUserLogin(byId,response));
        }
    }

    /**
     * 绑定用户手机号
     * @param userBindPhoneRequest
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResponse bindPhone(UserBindPhoneRequest userBindPhoneRequest,HttpServletRequest request) {
        String captcha = userBindPhoneRequest.getCaptcha();
        Long id = userBindPhoneRequest.getId();
        String userAccount = userBindPhoneRequest.getUserAccount();
        String mobile = userBindPhoneRequest.getMobile();
        String code = userBindPhoneRequest.getCode();
        String signature = request.getHeader("signature");
        if (StringUtils.isAnyBlank(userAccount, String.valueOf(id),mobile,code,captcha,signature)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String picCaptcha = (String) redisTemplate.opsForValue().get(CAPTCHA_PREFIX + signature);
        AuthPhoneNumber authPhoneNumber = new AuthPhoneNumber();
        //验证图形验证码是否正确
        if (null == picCaptcha || authPhoneNumber.isCaptcha(captcha) || !captcha.equals(picCaptcha)){
            throw new BusinessException(ErrorCode.OPERATION_ERROR,"图形验证码错误或已经过期，请重新刷新验证码");
        }
        //验证手机号是否正确
        if(!authPhoneNumber.isPhoneNum(mobile)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"手机号非法");
        }
        // 手机号和验证码是否匹配
        boolean verify = smsLimiter.verifyCode(mobile, code);
        if (!verify){
            throw new BusinessException(ErrorCode.SMS_CODE_ERROR);
        }
        synchronized (userAccount.intern()){
            //手机号不能重复
            String getMapperPhone = userMapper.selectPhone(mobile);
            if (null != getMapperPhone){
                throw new BusinessException(ErrorCode.PARAMS_ERROR,"手机号已经被注册");
            }
            boolean update = this.update(new UpdateWrapper<User>().eq("id", id).eq("userAccount", userAccount).set("mobile", mobile));
            if (!update){
                throw new BusinessException(ErrorCode.OPERATION_ERROR);
            }
        }
        String phone = DesensitizedUtil.mobilePhone(mobile);
        //更新全局对象中的用户信息
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser =(User) authentication.getPrincipal();
        currentUser.setMobile(phone);
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(currentUser, null, currentUser.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
        return ResultUtils.success(phone);
    }

    /**
     * 获取GitHub上这个项目的stars
     * @return
     */
    @Override
    public BaseResponse getGithubStars() {
        String listContent = null;
        try {
            listContent=HttpUtil.get("https://img.shields.io/github/stars/YukeSeko?style=social");
        }catch (Exception e){
            throw new BusinessException(ErrorCode.OPERATION_ERROR,"获取GitHub Starts 超时");
        }
        //该操作查询时间较长
        List<String> titles = ReUtil.findAll("<title>(.*?)</title>", listContent, 1);
        String stars = null;
        for (String title : titles) {
            //打印标题
            String[] split = title.split(":");
            stars = split[1];
        }
        return ResultUtils.success(stars);
    }

    /**
     * 获取echarts需要展示的数据
     * @return
     */
    @Override
    public BaseResponse getEchartsData() {
        //1、获取最近7天的日期
        List<String> dateList = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        for (int i = 0; i < 7; i++) {
            Date date = DateUtils.addDays(new Date(), -i);
            String formatDate = sdf.format(date);
            dateList.add(formatDate);
        }
        ArrayList<Object> objects = new ArrayList<>();
        //2、查询最近7天的交易成功信息
        BaseResponse orderEchartsData = apiOrderFeignClient.getOrderEchartsData(dateList);
        List<EchartsVo> data = (List<EchartsVo>) orderEchartsData.getData();
        //3、根据最近七天的日期去数据库中查询用户信息
        ArrayList<Long> userList = extracted(dateList, userMapper.getUserList(dateList),false);
        //4、查询最近7天的接口信息
        ArrayList<Long> interfaceList = extracted(dateList, interfaceInfoMapper.getInterfaceList(dateList),false);
        ArrayList<Long> orderList = extracted(dateList, data,true);
        Collections.reverse(dateList);
        objects.add(dateList);
        objects.add(userList);
        objects.add(interfaceList);
        objects.add(orderList);
        return ResultUtils.success(objects);
    }

    /**
     * 封装echarts返回数据
     * @param dateList
     * @param list
     * @return
     */
    private static ArrayList<Long> extracted(List<String> dateList, List<EchartsVo> list,boolean isChange) {
        ArrayList<Long> echartsVos = new ArrayList<>();
        for (int i=0;i<7;i++){
            boolean bool=false;
            //创建内循环 根据查询出已有的数量 循环次数
            for (int m = 0; m< list.size(); m++){
                if (!isChange){
                    EchartsVo echartsVo = list.get(m);
                    if (dateList.get(i).equals(echartsVo.getDate())){
                        echartsVos.add(echartsVo.getCount());
                        bool=true;
                        break;
                    }
                }else {
                    //处理数据转化问题
                    String s = JSONUtil.toJsonStr(list.get(m));
                    EchartsVo echartsVo = JSONUtil.toBean(s, EchartsVo.class);
                    if (dateList.get(i).equals(echartsVo.getDate())){
                        echartsVos.add(echartsVo.getCount());
                        bool=true;
                        break;
                    }
                }
            }
            if (!bool) {
                echartsVos.add(0L);
            }
        }
        Collections.reverse(echartsVos);
        return echartsVos;
    }


    /**
     * 初始化用户登录状态
     * @param user
     */
    private LoginUserVo initUserLogin(UserDetails user,HttpServletResponse response){
        //设置到Security 全局对象中去
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
        LoginUserVo loginUserVo = new LoginUserVo();
        BeanUtils.copyProperties(user,loginUserVo);
        //生成token并存入redis
        String token = tokenUtils.generateToken(String.valueOf(loginUserVo.getId()),loginUserVo.getUserAccount());
        loginUserVo.setToken(token);
        Cookie cookie = new Cookie(CookieConstant.headAuthorization,token);
        cookie.setPath("/");
        cookie.setMaxAge(CookieConstant.expireTime);
        response.addCookie(cookie);
        CookieUtils cookieUtils = new CookieUtils();
        String autoLoginContent = cookieUtils.generateAutoLoginContent(loginUserVo.getId().toString(), loginUserVo.getUserAccount());
        Cookie cookie1 = new Cookie(CookieConstant.autoLoginAuthCheck, autoLoginContent);
        cookie1.setPath("/");
        cookie.setMaxAge(CookieConstant.expireTime);
        response.addCookie(cookie1);
        return loginUserVo;
    }

}




