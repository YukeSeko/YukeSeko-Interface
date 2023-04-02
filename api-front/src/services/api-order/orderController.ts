// @ts-ignore
/* eslint-disable */
import { request } from '@umijs/max';

/** cancelOrderSn POST /api/order/cancelOrderSn */
export async function cancelOrderSnUsingPOST(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.cancelOrderSnUsingPOSTParams,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponse>('/api/order/cancelOrderSn', {
    method: 'POST',
    params: {
      ...params,
    },
    ...(options || {}),
  });
}

/** generateOrderSn POST /api/order/generateOrderSn */
export async function generateOrderSnUsingPOST(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.generateOrderSnUsingPOSTParams,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponseOrderSnVo_>('/api/order/generateOrderSn', {
    method: 'POST',
    params: {
      ...params,
    },
    ...(options || {}),
  });
}

/** generateToken GET /api/order/generateToken */
export async function generateTokenUsingGET(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.generateTokenUsingGETParams,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponse>('/api/order/generateToken', {
    method: 'GET',
    params: {
      ...params,
    },
    ...(options || {}),
  });
}

/** getCurrentOrderInfo POST /api/order/getCurrentOrderInfo */
export async function getCurrentOrderInfoUsingPOST(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.getCurrentOrderInfoUsingPOSTParams,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponsePageApiOrderStatusVo_>('/api/order/getCurrentOrderInfo', {
    method: 'POST',
    params: {
      ...params,
    },
    ...(options || {}),
  });
}

/** getSuccessOrder GET /api/order/getSuccessOrder */
export async function getSuccessOrderUsingGET(options?: { [key: string]: any }) {
  return request<API.BaseResponse>('/api/order/getSuccessOrder', {
    method: 'GET',
    ...(options || {}),
  });
}
