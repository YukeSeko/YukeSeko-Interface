import Footer from '@/components/Footer';


import {
  AlipayCircleOutlined, GithubFilled,
  LockOutlined,
  MobileOutlined,
  TaobaoCircleOutlined,
  UserOutlined, WechatFilled,
  WeiboCircleOutlined,
} from '@ant-design/icons';
import {
  LoginForm,
  ProFormCaptcha,
  ProFormCheckbox,
  ProFormText,
} from '@ant-design/pro-components';
import { useEmotionCss } from '@ant-design/use-emotion-css';
import { FormattedMessage, history, SelectLang, useIntl, useModel } from '@umijs/max';
import {Alert, Button, Input, message, Modal, QRCode, Steps, Tabs, Tooltip} from 'antd';
import React, {useRef, useState} from 'react';
import { flushSync } from 'react-dom';
import {RequestOptions} from "@@/plugin-request/request";
import {randomStr} from "@antfu/utils";
import {size, values} from "lodash";
import {ProFormInstance} from "@ant-design/pro-form/lib";
import {Vertify} from "@alex_xu/react-slider-vertify";
import {
  captchaUsingGET,
  getCaptchaUsingGET,
  loginBySmsUsingPOST, updateUserPassUsingPOST,
  userLoginByPwdUsingPOST,
  userRegisterUsingPOST
} from "@/services/api-backend/userController";
import FogotPwd from "@/pages/User/Login/components/forgotPwd";

const Lang = () => {
  const langClassName = useEmotionCss(({ token }) => {
    return {
      width: 42,
      height: 42,
      lineHeight: '42px',
      position: 'fixed',
      right: 16,
      borderRadius: token.borderRadius,
      ':hover': {
        backgroundColor: token.colorBgTextHover,
      },
    };
  });

  return (
    <div className={langClassName} data-lang>
      {SelectLang && <SelectLang />}
    </div>
  );
};

const LoginMessage: React.FC<{
  content: string;
}> = ({ content }) => {
  return (
    <Alert
      style={{
        marginBottom: 24,
      }}
      message={content}
      type="error"
      showIcon
    />
  );
};

const Login: React.FC = () => {
  const [userLoginState] = useState<API.LoginResult>({});
  const [type, setType] = useState<string>('account');
  const { initialState, setInitialState } = useModel('@@initialState');
  const [imageUrl,setImageUrl] =useState<any>(null);
  const formRef = useRef<ProFormInstance>();
  const [visible, setVisible] = useState(false);
  const [registerLoading,setRegisterLoading] =useState(false)
  const [registerForm,setRegisterForm] = useState<any>([])
  const { status, type: loginType } = userLoginState;
  const [loginLoading ,setLoginLoading] = useState(false)
  const [fogotPwd,setFogotPwd] = useState(false)
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [wxCurrent, setWxCurrent] = useState(0);
  const steps = [{title: '关注公众号',}, {title: '扫码登录',}];
  const containerClassName = useEmotionCss(() => {
    return {
      display: 'flex',
      flexDirection: 'column',
      height: '100vh',
      overflow: 'auto',
      backgroundImage:
        // https://mdn.alipayobjects.com/yuyan_qk0oxh/afts/img/V-_oS6r-i7wAAAAAAAAAAAAAFl94AQBr
        "url('https://mdn.alipayobjects.com/yuyan_qk0oxh/afts/img/V-_oS6r-i7wAAAAAAAAAAAAAFl94AQBr')",
      backgroundSize: '100% 100%',
    };
  });

  const intl = useIntl();
  const ActionIcons = () => {
    const langClassName = useEmotionCss(({ token }) => {
      return {
        marginLeft: '8px',
        color: 'rgba(0, 0, 0, 0.2)',
        fontSize: '24px',
        verticalAlign: 'middle',
        cursor: 'pointer',
        transition: 'color 0.3s',
        '&:hover': {
          color: token.colorPrimaryActive,
        },
      };
    });

    return (
      <>
        <Tooltip title="Github">
          <a href="https://github.com/login/oauth/authorize?client_id=4f2711fe7c282b1e2eef&redirect_uri=http://122.9.148.119:88/api/oauth/github&state=ture">
            <GithubFilled key="GithubOutlined" className={langClassName} />
          </a>
        </Tooltip>
        <Tooltip title="Gitee">
          <a href="https://gitee.com/oauth/authorize?client_id=7d9f0b44f3f5fe27722c5f54cbb5c8b66ee908de39d34f08204dea5b28a706e1&redirect_uri=http://122.9.148.119:88/api/oauth/gitee&response_type=code">
            <img  className={langClassName} style={{width:70,marginLeft:10}} src="https://gitee.com/static/images/logo-black.svg?t=158106664"/>
          </a>
        </Tooltip>
        <Tooltip title="微信公众号">
          <a onClick={()=>{setIsModalOpen(true)}}><WechatFilled className={langClassName} style={{width:70,marginLeft:10}} /></a>
        </Tooltip>
      </>
    );
  };
  const fetchUserInfo = (userInfo: API.UserVO) => {
    if (userInfo) {
      flushSync(() => {
        setInitialState({ loginUser: userInfo });
      });
    }
  };


  const items = steps.map((item) => ({ key: item.title, title: item.title }));
  /**
   * 跳转注册账号表单
   */
  const register = async () =>{
    await getCaptcha()
    setType("register")
    setRegisterLoading(false)
  }

  /**
   * 延迟动画价值
   * @param time
   */
  const waitTime = (time: number = 100) => {
    return new Promise((resolve) => {
      setLoginLoading(true)
      setTimeout(() => {
        resolve(true);
      }, time);
    });
  };
  /**
   * 获取图形验证码
   */
  const getCaptcha = async () =>{
    let randomString
    const temp = localStorage.getItem("api-open-platform-randomString")
    if (temp){
      randomString = temp
    }else {
      randomString = randomStr(32, '0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ');
      localStorage.setItem("api-open-platform-randomString",randomString)
    }
    //携带浏览器请求标识
    const  res = await getCaptchaUsingGET({
      headers: {
        "signature": randomString
      },
      responseType: 'blob' //必须指定为'blob'
    })
    let url = window.URL.createObjectURL(res)
    setImageUrl(url)
  }

  /**
   * 提交登录表单
   * @param values
   */
  const handleSubmit = async (values: any) => {
    try {
      // 登录
      await waitTime(1000);
      let res = null;
      if (type === 'account') {
        res = await userLoginByPwdUsingPOST({ ...values });
      } else {
        res = await loginBySmsUsingPOST({...values});
      }
      setLoginLoading(false)
      if (res?.data) {
        const defaultLoginSuccessMessage = intl.formatMessage({
          id: 'pages.login.success',
          defaultMessage: '登录成功！',
        });
        fetchUserInfo(res?.data);
        const urlParams = new URL(window.location.href).searchParams;
        history.push(urlParams.get('redirect') || '/'); //路由跳转
        message.success(defaultLoginSuccessMessage);
        return;
      }
    } catch (error) {
      setLoginLoading(false)
    }

  };

  /**
   * 验证注册表单
   * @param values
   */
  const registerSubmit = async (values: any) =>{
    setVisible(true)
    setRegisterLoading(true)
    setRegisterForm(values)
  }
  /**
   * 处理注册请求
   */
  const handleRegisterSubmit = async () =>{
    try {
      setVisible(false)
      const signature = localStorage.getItem("api-open-platform-randomString")
      setRegisterLoading(false)
      const res = await userRegisterUsingPOST({...registerForm},{
        headers: {
          "signature": signature
        },
      })
      if (res.code === 0){
        message.success("注册成功")
        setType("account")
        //注册成功后重置表单
        formRef.current?.resetFields()
      }
    }catch (error){}
  }


  return (

    <div className={containerClassName}>
      <title>
        {intl.formatMessage({
          id: 'menu.login',
          defaultMessage: '登录',
        })}
        - YukeSeko开放接口
      </title>
      <Lang />
      <div
        style={{
          flex: '1',
          padding: '32px 0',
        }}
      >
        <LoginForm
          contentStyle={{
            minWidth: 280,
            maxWidth: '75vw',
          }}
          formRef={formRef}
          logo={<img alt="logo" src="/logo.svg" />}
          title={<a href={'/'}>YukeSeko 接口</a>}
          subTitle={' '} //intl.formatMessage({id: 'pages.layouts.userLayout.title'})
          initialValues={{
            autoLogin: true,
          }}
          actions={[
            <FormattedMessage
              key="loginWith"
              id="pages.login.loginWith"
              defaultMessage="其他登录方式："
            />,
            <ActionIcons key="icons" />,
          ]}
          onFinish={async (values) => {
            if (type === "register"){
              await registerSubmit(values) //注册用户
            }else {
              await handleSubmit(values); // 用户登录
            }
          }}
          //自定义实现登录按钮
          submitter={{
            render: (props, doms) =>{
              if (type === 'register'){
                return [doms,<Button type="primary" onClick={()=>{formRef.current?.submit()}} loading={registerLoading}  style={{width:250,height:40,marginTop:10}}>
                  立即注册
                </Button>]
              }else return <Button type="primary" loading={loginLoading} onClick={()=>{formRef.current?.submit()}} style={{width:'100%',height:40,marginTop:15}}>
                登录
              </Button>
            }
          }}
        >
          <Tabs
            activeKey={type}
            onChange={setType}
            centered
            items={[
              {
                key: 'account',
                label: intl.formatMessage({
                  id: 'pages.login.accountLogin.tab',
                  defaultMessage: '账户密码登录',
                }),
              },
              {
                key: 'mobile',
                label: intl.formatMessage({
                  id: 'pages.login.phoneLogin.tab',
                  defaultMessage: '手机号登录',
                }),
              },
            ]}
          />

          {status === 'error' && loginType === 'account' && (
            <LoginMessage
              content={intl.formatMessage({
                id: 'pages.login.accountLogin.errorMessage',
                defaultMessage: '账户或密码错误(admin/ant.design)',
              })}
            />
          )}
          {type === 'account' && (
            <>
              <ProFormText
                name="userAccount"
                fieldProps={{
                  size: 'large',
                  prefix: <UserOutlined />,
                }}
                placeholder={intl.formatMessage({
                  id: 'pages.login.username.placeholder',
                  defaultMessage: '用户名: admin or user',
                })}
                rules={[
                  {
                    required: true,
                    message: (
                      <FormattedMessage
                        id="pages.login.username.required"
                        defaultMessage="请输入用户名!"
                      />
                    ),
                  },
                ]}
              />
              <ProFormText.Password
                name="userPassword"
                fieldProps={{
                  size: 'large',
                  prefix: <LockOutlined />,
                }}
                placeholder={intl.formatMessage({
                  id: 'pages.login.password.placeholder',
                  defaultMessage: '密码',
                })}
                rules={[
                  {
                    required: true,
                    message: (
                      <FormattedMessage
                        id="pages.login.password.required"
                        defaultMessage="请输入密码！"
                      />
                    ),
                  },
                ]}
              />
            </>
          )}

          {status === 'error' && loginType === 'mobile' && <LoginMessage content="验证码错误" />}
          {type === 'mobile' && (
            <>
              <ProFormText
                fieldProps={{
                  size: 'large',
                  prefix: <MobileOutlined className={'prefixIcon'} />,
                }}
                name="mobile"
                placeholder={'手机号'}
                rules={[
                  {
                    required: true,
                    message: '请输入手机号！',
                  },
                  {
                    pattern: /^1[3-9]\d{9}$/,
                    message: '手机号格式错误！',
                  },
                ]}
              />
              <ProFormCaptcha
                fieldProps={{
                  size: 'large',
                  prefix: <LockOutlined className={'prefixIcon'} />,
                }}
                captchaProps={{
                  size: 'large',
                }}
                placeholder={'请输入验证码'}
                captchaTextRender={(timing, count) => {
                  if (timing) {
                    return `${count} ${'后重新获取'}`;
                  }
                  return '获取验证码';
                }}
                name="code"
                // 手机号的 name，onGetCaptcha 会注入这个值
                phoneName="mobile"
                rules={[
                  {
                    required: true,
                    message: '请输入验证码！',
                  },
                ]}
                onGetCaptcha={async (mobile) => {
                  //获取验证成功后才会进行倒计时
                  try {
                    const result = await captchaUsingGET({
                      mobile,
                    });
                    if (!result) {
                      return;
                    }
                    message.success(result.data);
                  }catch (e) {
                  }
                }}
              />
            </>
          )}

          {type === 'register' && (
            <>
              <ProFormText
                fieldProps={{
                  size: 'large',
                  prefix: <UserOutlined />,
                }}
                name="userAccount"
                placeholder={'账号'}
                rules={[
                  {
                    required: true,
                    message: '请输入账号！',
                  },
                  {
                    min:4,
                    message:'账号长度不能小于4'
                  }
                ]}
              />
              <ProFormText.Password
                name="userPassword"
                fieldProps={{
                  size: 'large',
                  prefix: <LockOutlined />,
                }}
                placeholder={intl.formatMessage({
                  id: 'pages.login.password.placeholder',
                  defaultMessage: '密码',
                })}
                rules={[
                  {
                    required: true,
                    message: (
                      <FormattedMessage
                        id="pages.login.password.required"
                        defaultMessage="请输入密码！"
                      />
                    ),
                  },
                  {
                    min:8,
                    message:'密码长度不能小于8'
                  }
                ]}
              />
              <ProFormText.Password
                name="checkPassword"
                fieldProps={{
                  size: 'large',
                  prefix: <LockOutlined />,
                }}
                placeholder={intl.formatMessage({
                  id: 'pages.login.password.placeholder',
                  defaultMessage: '确认密码',
                })}
                rules={[
                  {
                    validator(role,value){
                      if (value !==formRef.current?.getFieldValue("userPassword")){
                        return Promise.reject("两次密码输入不一致")
                      }
                      return Promise.resolve()
                    },
                  }
                ]}
              />
              <ProFormText
                fieldProps={{
                  size: 'large',
                  prefix: <MobileOutlined className={'prefixIcon'} />,
                }}
                name="mobile"
                placeholder={'手机号'}
                rules={[
                  {
                    required: true,
                    message: '请输入手机号！',
                  },
                  {
                    pattern: /^1[3-9]\d{9}$/,
                    message: '手机号格式错误！',
                  },
                ]}
              />
              <ProFormCaptcha
                fieldProps={{
                  size: 'large',
                  prefix: <LockOutlined className={'prefixIcon'} />,
                }}
                captchaProps={{
                  size: 'large',
                }}
                placeholder={'请输入验证码'}
                captchaTextRender={(timing, count) => {
                  if (timing) {
                    return `${count} ${'后重新获取'}`;
                  }
                  return '获取验证码';
                }}
                name="code"
                // 手机号的 name，onGetCaptcha 会注入这个值
                phoneName="mobile"
                rules={[
                  {
                    required: true,
                    message: '请输入验证码！',
                  },
                  {
                    pattern: /^[0-9]\d{4}$/,
                    message: '验证码格式错误！',
                  },
                ]}
                onGetCaptcha={async (mobile) => {
                  //获取验证成功后才会进行倒计时
                  try {
                    const result = await captchaUsingGET({
                      mobile,
                    });
                    if (!result) {
                      return;
                    }
                    message.success(result.data);
                  }catch (e) {
                  }
                }}
              />
              <div style={{display:"flex"}}>
                <ProFormText
                  fieldProps={{
                    size: 'large',
                    prefix: <LockOutlined className={'prefixIcon'} />,
                  }}
                  name="captcha"
                  placeholder={'请输入右侧验证码'}
                  rules={[
                    {
                      required: true,
                      message: '请输入图形验证码！',
                    },
                    {
                      pattern: /^[0-9]\d{3}$/,
                      message: '验证码格式错误！',
                    },
                  ]}
                />
                <img src={imageUrl} onClick={getCaptcha} style={{marginLeft:18}} width="100px" height="39px"/>
              </div>
              <Vertify
                width={320}
                height={160}
                visible={visible}
                // 默认可以不用设置
                // imgUrl={'/失落深渊葬礼2_4k_b1c03.jpg'}
                onSuccess={handleRegisterSubmit}
                // onFail={() => alert('fail')}
                // onRefresh={() => alert('refresh')}
              />
            </>
          )}

          <div
            style={{
              marginBottom: 24,
            }}
          >
            {/*todo 自动登录功能以后再实现*/}
            {/*<ProFormCheckbox  noStyle name="remember-me" >*/}
            {/*  <FormattedMessage  id="pages.login.rememberMe" defaultMessage="自动登录" />*/}
            {/*</ProFormCheckbox>*/}

            <a hidden={type === 'register' ? true :false} style={{ float: "left" }} onClick={register}>
              <FormattedMessage id="pages.login.register" defaultMessage="注册账号" />
            </a>

            <a style={{ float: 'right' }} onClick={()=>{setFogotPwd(true)}}>
              <FormattedMessage id="pages.login.forgotPassword" defaultMessage="忘记密码？" />
            </a>

          </div>
        </LoginForm>
      </div>
      <Footer />
      <FogotPwd PwdVisible={fogotPwd} onCancel={()=>{setFogotPwd(false)}}
      onFinish={async (values)=>{
        try {
          const res = await updateUserPassUsingPOST(values)
          if (res.code === 0 ){
            setFogotPwd(false);
            message.success(res.data);
            return  true
          }
          return false
        }catch (e) {}
      }
      }/>
      {/*微信登录或公众号登录不再进行实现*/}
      <Modal footer={
        wxCurrent ===1 ? [
            <Button    onClick={()=>{setWxCurrent(wxCurrent - 1)}}>上一步</Button>,
            <Button  type="primary"  onClick={()=>{setIsModalOpen(false)}}>确认</Button>] :
          <Button  type="primary"  onClick={()=>{setWxCurrent(wxCurrent + 1);}}>
          下一步
        </Button>
      } destroyOnClose={true} title="公众号登录" open={isModalOpen}  onCancel={()=>{setIsModalOpen(false)}}>

        <Alert
          message="关注公众号"
          description="请先关注该公众号后再进行扫码登录，未关注公众号不能进行扫码登录（该功能不再实现）"
          type="warning"
          showIcon
        />
        <div style={{textAlign:"center" ,display:"flex"}}>
          <Steps style={{marginTop:20,width:150,height:260}} direction="vertical" current={wxCurrent} items={items} />
          {wxCurrent ===1 ?[
            <QRCode value="https://open.weixin.qq.com/connect/oauth2/authorize?appid=wx8e8edebb34b58e7a&redirect_uri=http://127.0.0.1:88&response_type=code&scope=snsapi_userinfo&state=STATE#wechat_redire" />,
          ]:[
            <div >
              <img style={{height:250}} src="/0.jpg"/>
            </div>
          ]}
        </div>
      </Modal>
    </div>
  );
};

export default Login;
