/**
 * @see https://umijs.org/zh-CN/plugins/plugin-access
 * */
export default function access(initialState: InitialState | undefined) {
  //InitialState ：为全局保存的对象
  const {loginUser} = initialState ?? {};
  return {
    canUser: loginUser,  //只要用户登录了就有该权限
    canAdmin: loginUser?.userRole === 'admin', //只有当用户的角色为admin的时候才具有管理员权限
  };
}
