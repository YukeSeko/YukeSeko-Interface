// @ts-ignore
/* eslint-disable */
import { request } from '@umijs/max';


/** pay POST /api/alipay/pay */
export async function payUsingPOST(body: API.AliPayDto, options?: { [key: string]: any }) {
  return request<any>('/api/alipay/pay', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  });
}

/** queryTradeStatus GET /api/alipay/queryTradeStatus */
export async function queryTradeStatusUsingGET(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.queryTradeStatusUsingGETParams,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponseAlipayInfo_>('/api/alipay/queryTradeStatus', {
    method: 'GET',
    params: {
      ...params,
    },
    ...(options || {}),
  });
}
