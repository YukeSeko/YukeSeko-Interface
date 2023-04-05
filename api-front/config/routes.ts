export default [
  {
    path: '/',
    name: '首页',
    access: 'canUser',
    icon: 'AppstoreOutlined',
    routes: [{ path: '/', component: './index/index'}],
  },
  { path: '/OnlineCall', name: '在线调用', icon: 'table', access: 'canUser',
    routes: [
      {path: '/OnlineCall', name: '在线调用', icon: 'table', access: 'canUser', component: './OnlineCall/index',hideInMenu: true},
      {
        path: '/OnlineCall/InterfaceInfo/:id',
        name: '接口详情',
        access: 'canUser',
        component: './OnlineCall/InterfaceInfo',
        hideInMenu: true,
      },
    ]
  },
  {
    path: '/getRequestCounts',
    name: '请求次数',
    access: 'canUser',
    icon: 'crown',
    routes: [{ path: '/getRequestCounts', component: './PurchaseRequests/index'}],
  },
  {
    name: '我的订单',
    path: '/myOrderInfo',
    access: 'canUser',
    icon: 'ContainerOutlined',
    routes: [{ path: '/myOrderInfo', component: './Order/myOrderInfo'}],
  },
  {
    path:'/Order',
    layout: false,
    access: 'canUser',
    routes: [{name: '订单支付',path:'/Order/order',component:'./Order/order'},
             {name: '支付状态查询',path: '/Order/paymentStatus',component: './Order/payStatus'}]
  },
  {
    path: '/user',
    layout: false,
    routes: [{ name: '登录', path: '/user/login', component: './User/Login' }],
  },
  {
    path: '/admin',
    name: '管理页',
    icon: 'lock',
    access: 'canAdmin',
    routes: [
      { name: '接口管理', path: '/admin/interface_info', component: './Admin/InterfaceInfo' },
      { name: '用户管理', path: '/admin/user_info', component: './Admin/UserInfo' },
    ],
  },
  { path: '*', layout: false, component: './404' },
];
