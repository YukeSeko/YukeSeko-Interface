package com.wzy.api.service;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.wzy.api.model.dto.user.UserBindPhoneRequest;
import com.wzy.api.model.dto.user.UserLoginBySmsRequest;
import com.wzy.api.model.dto.user.UserQueryRequest;
import com.wzy.api.model.dto.user.UserRegisterRequest;
import com.wzy.api.model.entity.User;
import common.vo.LoginUserVo;
import com.wzy.api.model.vo.UserVO;
import common.BaseResponse;
import common.to.Oauth2ResTo;


import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.security.NoSuchAlgorithmException;
import java.util.List;

/**
 * 用户服务
 *
 * 
 */
public interface UserService extends IService<User> {

    /**
     * 用户注册
     * @param userRegisterRequest
     * @return
     */
    long userRegister(UserRegisterRequest userRegisterRequest ,HttpServletRequest request);

    /**
     * 用户登录
     *
     * @param userAccount  用户账户
     * @param userPassword 用户密码
     * @param response
     * @return 脱敏后的用户信息
     */
    LoginUserVo userLogin(String userAccount, String userPassword, HttpServletResponse response);

    /**
     * 获取当前登录用户
     *
     * @param request
     * @return
     */
    User getLoginUser(HttpServletRequest request);

    /**
     * 是否为管理员
     *
     * @param request
     * @return
     */
    boolean isAdmin(HttpServletRequest request);


    /**
     * 发送验证码
     * @param mobile
     * @return
     */
    BaseResponse captcha(String mobile);

    /**
     * 获取用户列表
     * @param userQueryRequest
     * @return
     */
    BaseResponse<List<UserVO>> lisUser(UserQueryRequest userQueryRequest);

    /**
     * 分页获取用户列表
     * @param userQueryRequest
     * @return
     */
    BaseResponse<Page<UserVO>> listUserByPage(UserQueryRequest userQueryRequest);

    /**
     * 用户通过手机号进行登录
     * @param loginBySms
     * @param response
     * @return
     */
    BaseResponse loginBySms(UserLoginBySmsRequest loginBySms, HttpServletResponse response);

    /**
     * 生成图形验证码
     * @param request
     * @param response
     */
    void getCaptcha(HttpServletRequest request, HttpServletResponse response);

    /**
     * 用户忘记密码，返回用户注册时的手机号
     * @param username
     * @return
     */
    BaseResponse getPassUserType(String username,HttpServletResponse response);

    /**
     * 忘记密码请求第二步，发送验证码
     * @param request
     * @return
     */
    BaseResponse sendPassUserCode(HttpServletRequest request) throws NoSuchAlgorithmException;

    /**
     * 忘记密码部分-验证手机号和验证码输入是否正确
     * @param code
     * @param request
     * @return
     */
    BaseResponse authPassUserCode(String code, HttpServletRequest request);

    /**
     * 修改用户密码
     * @param password
     * @param request
     * @return
     */
    BaseResponse updateUserPass(String password, HttpServletRequest request);

    /**
     * 通过第三方Gitee登录
     * @param oauth2ResTo
     * @param type
     * @param httpServletResponse
     * @return
     */
    BaseResponse oauth2Login(Oauth2ResTo oauth2ResTo, String type, HttpServletResponse httpServletResponse);

    /**
     * 检查用户当前登录状态
     * @param request
     * @param response
     * @return
     */
    BaseResponse checkUserLogin(HttpServletRequest request, HttpServletResponse response);

    /**
     * 绑定用户手机号
     * @param userBindPhoneRequest
     * @param request
     * @return
     */
    BaseResponse bindPhone(UserBindPhoneRequest userBindPhoneRequest,HttpServletRequest request);

    /**
     * 获取GitHub的stars
     * @return
     */
    BaseResponse getGithubStars();


    /**
     * 获取echarts需要展示的数据
     * @return
     */
    BaseResponse getEchartsData();

}
