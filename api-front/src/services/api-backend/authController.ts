// @ts-ignore
/* eslint-disable */
import { request } from '@umijs/max';

/** getAuthByUserId GET /api/auth/getAuthByUserId */
export async function getAuthByUserIdUsingGET(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.getAuthByUserIdUsingGETParams,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponseAuthVo>('/api/auth/getAuthByUserId', {
    method: 'GET',
    params: {
      ...params,
    },
    ...(options || {}),
  });
}

/** updateAuthStatus GET /api/auth/updateAuthStatus */
export async function updateAuthStatusUsingGET(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.updateAuthStatusUsingGETParams,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponse>('/api/auth/updateAuthStatus', {
    method: 'GET',
    params: {
      ...params,
    },
    ...(options || {}),
  });
}
