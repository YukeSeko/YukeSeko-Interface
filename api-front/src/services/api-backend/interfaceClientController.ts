// @ts-ignore
/* eslint-disable */
import { request } from '@umijs/max';

/** apiClient POST /api/apiclient */
export async function apiClientUsingPOST(
  body: API.InterfaceInfoInvokRequest,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponseobject>('/api/apiclient', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  });
}
