// @ts-ignore
/* eslint-disable */
import { request } from '@umijs/max';

/** error GET /api/error */
export async function errorUsingGET(options?: { [key: string]: any }) {
  return request<Record<string, any>>('/api/error', {
    method: 'GET',
    ...(options || {}),
  });
}

/** error PUT /api/error */
export async function errorUsingPUT(options?: { [key: string]: any }) {
  return request<Record<string, any>>('/api/error', {
    method: 'PUT',
    ...(options || {}),
  });
}

/** error POST /api/error */
export async function errorUsingPOST(options?: { [key: string]: any }) {
  return request<Record<string, any>>('/api/error', {
    method: 'POST',
    ...(options || {}),
  });
}

/** error DELETE /api/error */
export async function errorUsingDELETE(options?: { [key: string]: any }) {
  return request<Record<string, any>>('/api/error', {
    method: 'DELETE',
    ...(options || {}),
  });
}

/** error PATCH /api/error */
export async function errorUsingPATCH(options?: { [key: string]: any }) {
  return request<Record<string, any>>('/api/error', {
    method: 'PATCH',
    ...(options || {}),
  });
}
