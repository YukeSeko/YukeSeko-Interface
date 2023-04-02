// @ts-ignore
/* eslint-disable */
import { request } from '@umijs/max';

/** 忘记密码部分-验证手机号和验证码输入是否正确 POST /api/user/authPassUserCode */
export async function authPassUserCodeUsingPOST(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.authPassUserCodeUsingPOSTParams,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponse>('/api/user/authPassUserCode', {
    method: 'POST',
    params: {
      ...params,
    },
    ...(options || {}),
  });
}

/** 绑定用户手机号 POST /api/user/bindPhone */
export async function bindPhoneUsingPOST(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.bindPhoneUsingPOSTParams,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponse>('/api/user/bindPhone', {
    method: 'POST',
    params: {
      ...params,
    },
    ...(options || {}),
  });
}

/** 向手机号发送短信 GET /api/user/captcha */
export async function captchaUsingGET(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.captchaUsingGETParams,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponse>('/api/user/captcha', {
    method: 'GET',
    params: {
      ...params,
    },
    ...(options || {}),
  });
}
/** userLogout POST /api/user/logout */
export async function userLogoutUsingPOST(options?: { [key: string]: any }) {
  return request<API.BaseResponseboolean>('/api/user/logout', {
    method: 'POST',
    ...(options || {}),
  });
}
/** 验证用户的登录状态 POST /api/user/checkUserLogin */
export async function checkUserLoginUsingPOST(options?: { [key: string]: any }) {
  return request<API.BaseResponse>('/api/user/checkUserLogin', {
    method: 'POST',
    ...(options || {}),
  });
}

/** getActiveUser GET /api/user/getActiveUser */
export async function getActiveUserUsingGET(options?: { [key: string]: any }) {
  return request<API.BaseResponse>('/api/user/getActiveUser', {
    method: 'GET',
    ...(options || {}),
  });
}

/** getEchartsData GET /api/user/getEchartsData */
export async function getEchartsDataUsingGET(options?: { [key: string]: any }) {
  return request<API.BaseResponse>('/api/user/getEchartsData', {
    method: 'GET',
    ...(options || {}),
  });
}


/** 删除用户 POST /api/user/delete */
export async function deleteUserUsingPOST(
  body: API.DeleteRequest,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponseboolean>('/api/user/delete', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  });
}

/** 获取当前登录用户 GET /api/user/get/login */
export async function getLoginUserUsingGET(options?: { [key: string]: any }) {
  return request<API.BaseResponseUserVO>('/api/user/get/login', {
    method: 'GET',
    ...(options || {}),
  });
}

/** 生成图形验证码 GET /api/user/getCaptcha */
export async function getCaptchaUsingGET(options?: { [key: string]: any }) {
  return request<any>('/api/user/getCaptcha', {
    method: 'GET',
    ...(options || {}),
  });
}

/** 用户忘记密码，返回用户注册时的手机号 POST /api/user/getpassusertype */
export async function getPassUserTypeUsingPOST(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.getPassUserTypeUsingPOSTParams,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponse>('/api/user/getpassusertype', {
    method: 'POST',
    params: {
      ...params,
    },
    ...(options || {}),
  });
}

/** 获取用户列表 GET /api/user/list */
export async function listUserUsingGET(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.listUserUsingGETParams,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponseListUserVO>('/api/user/list', {
    method: 'GET',
    params: {
      ...params,
    },
    ...(options || {}),
  });
}

/** 分页获取用户列表 GET /api/user/list/page */
export async function listUserByPageUsingGET(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.listUserByPageUsingGETParams,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponsePageUserVO>('/api/user/list/page', {
    method: 'GET',
    params: {
      ...params,
    },
    ...(options || {}),
  });
}

/** 用户通过 用户名和密码 登录 POST /api/user/login */
export async function userLoginByPwdUsingPOST(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.userLoginByPwdUsingPOSTParams,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponseLoginUserVo>('/api/user/login', {
    method: 'POST',
    params: {
      ...params,
    },
    ...(options || {}),
  });
}

/** 用户通过手机号进行登录 POST /api/user/loginBySms */
export async function loginBySmsUsingPOST(
  body: API.UserLoginBySmsRequest,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponse>('/api/user/loginBySms', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  });
}

/** 用户注销 GET /api/user/logoutSuccess */
export async function logoutSuccessUsingGET(options?: { [key: string]: any }) {
  return request<API.BaseResponse>('/api/user/logoutSuccess', {
    method: 'GET',
    ...(options || {}),
  });
}

/** 通过第三方登录 POST /api/user/oauth2/login */
export async function oauth2LoginUsingPOST(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.oauth2LoginUsingPOSTParams,
  body: API.Oauth2ResTo,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponse>('/api/user/oauth2/login', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    params: {
      ...params,
    },
    data: body,
    ...(options || {}),
  });
}

/** 用户注册 POST /api/user/register */
export async function userRegisterUsingPOST(
  body: API.UserRegisterRequest,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponselong>('/api/user/register', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  });
}

/** 忘记密码请求第二步，发送验证码 POST /api/user/sendPassUserCode */
export async function sendPassUserCodeUsingPOST(options?: { [key: string]: any }) {
  return request<API.BaseResponse>('/api/user/sendPassUserCode', {
    method: 'POST',
    ...(options || {}),
  });
}

/** 更新用户 POST /api/user/update */
export async function updateUserUsingPOST(
  body: API.UserUpdateRequest,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponseboolean>('/api/user/update', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  });
}

/** 修改用户密码 POST /api/user/updateUserPass */
export async function updateUserPassUsingPOST(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.updateUserPassUsingPOSTParams,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponse>('/api/user/updateUserPass', {
    method: 'POST',
    params: {
      ...params,
    },
    ...(options || {}),
  });
}

/** 获取GitHub上该项目的stars GET /api/user/getGithubStars */
export async function getGitHubStars(options?: { [key: string]: any }) {
  return request<any>('/api/user/getGithubStars', {
    method: 'GET',
    ...(options || {}),
  });
}
