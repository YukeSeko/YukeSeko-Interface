// @ts-ignore
/* eslint-disable */
import { request } from '@umijs/max';

/** addPost POST /api/post/add */
export async function addPostUsingPOST(body: API.PostAddRequest, options?: { [key: string]: any }) {
  return request<API.BaseResponselong>('/api/post/add', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  });
}

/** deletePost POST /api/post/delete */
export async function deletePostUsingPOST(
  body: API.DeleteRequest,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponseboolean>('/api/post/delete', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  });
}

/** getPostById GET /api/post/get */
export async function getPostByIdUsingGET(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.getPostByIdUsingGETParams,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponsePost>('/api/post/get', {
    method: 'GET',
    params: {
      ...params,
    },
    ...(options || {}),
  });
}

/** listPost GET /api/post/list */
export async function listPostUsingGET(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.listPostUsingGETParams,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponseListPost>('/api/post/list', {
    method: 'GET',
    params: {
      ...params,
    },
    ...(options || {}),
  });
}

/** listPostByPage GET /api/post/list/page */
export async function listPostByPageUsingGET(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.listPostByPageUsingGETParams,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponsePagePost>('/api/post/list/page', {
    method: 'GET',
    params: {
      ...params,
    },
    ...(options || {}),
  });
}

/** updatePost POST /api/post/update */
export async function updatePostUsingPOST(
  body: API.PostUpdateRequest,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponseboolean>('/api/post/update', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  });
}
