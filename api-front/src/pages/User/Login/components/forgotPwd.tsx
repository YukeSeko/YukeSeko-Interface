import {
  ExclamationCircleOutlined,
  LockOutlined,
} from '@ant-design/icons';
import {ProFormCaptcha,
  ProFormText,
  StepsForm,
} from '@ant-design/pro-components';
import { Input, message, Modal, Tag} from 'antd';
import React, { useRef, useState} from 'react';
import {ProFormInstance} from "@ant-design/pro-form/lib";
import {
  authPassUserCodeUsingPOST,
  getPassUserTypeUsingPOST,
  sendPassUserCodeUsingPOST
} from "@/services/api-backend/userController";
import {FormattedMessage} from "@@/exports";

export type Props = {
  PwdVisible: boolean;
  onCancel: () => void;
  onFinish:(values: any) => Promise<any>;
};

const FogotPwd: React.FC<Props> = (props) => {
  const {PwdVisible,onCancel,onFinish} = props
  const formRef = useRef<ProFormInstance>();
  const [initialValue,setInitialValue] = useState<any>(null)
  const waitTime = (time: number = 100) => {
    return new Promise((resolve) => {
      setTimeout(() => {
        resolve(true);
      }, time);
    });
  };

  return (
    <>
      <StepsForm

        onFinish={async (values) => {
          await waitTime(1000);
          const res = onFinish?.(values);
          return res;
        }}
        formRef={formRef}
        formProps={{
          validateMessages: {
            required: '此项为必填项',
          },
        }}
        stepsFormRender={(dom, submitter) => {
          return (
            <Modal

              title="忘记密码"
              width={800}
              onCancel={() =>{
                onCancel?.()
              }}
              open={PwdVisible}
              footer={submitter}
              destroyOnClose={true}
              keyboard={false}
            >
              {dom}
            </Modal>
          );
        }}
      >
        <StepsForm.StepForm
          name="base"
          title="输入账号"
          onFinish={async (values) => {
            await waitTime(2000);
            try {
              const res = await getPassUserTypeUsingPOST(values)
              if (res.code === 0){
                setInitialValue(res?.data)
                formRef.current?.setFieldValue("mobile",res.data)
                return true;
              }
            }catch (e) {
              return false;
            }

          }}
        >
          <ProFormText
            name="username"
            width="md"
            label="请输入要找回的账号"
            tooltip="您可以输入注册的手机号或者账号"
            placeholder="请您输入手机号或者账号"
            rules={[{ required: true }]}
          />
        </StepsForm.StepForm>

        <StepsForm.StepForm name="check" title="安全验证"
          onFinish={async (values) => {
            try {
              await waitTime(2000);
              const res =  await authPassUserCodeUsingPOST(values)
              if (res.code === 0){
                message.success(res.data)
                return true;
              }
            }catch (e) {
              return false;
            }

          }}>
          <Tag icon={<ExclamationCircleOutlined />} color="warning" style={{fontSize:15,marginBottom:20,overflow:"hidden"}}>
            您的帐号可能存在安全风险，为了确保为您本人操作，请先进行安全验证。
          </Tag>
          <div style={{display:"horizontal",marginBottom:20,marginLeft:110}}>
            验证手机号：<Input style={{maxWidth:200}} disabled={true} name="mobile" value={initialValue}/>
          </div>
          <div style={{maxWidth:300,marginLeft:110}}>
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
              onGetCaptcha={async () => {
                //获取验证成功后才会进行倒计时
                try {
                  const result = await sendPassUserCodeUsingPOST();
                  if (!result) {
                    return;
                  }
                  message.success(result.data);
                }catch (e) {
                }
              }}
            />
          </div>
        </StepsForm.StepForm>

        <StepsForm.StepForm name="change" title="修改密码">
          <ProFormText.Password
            name="password"
            label="新密码"
            fieldProps={{
              size: 'large',
              prefix: <LockOutlined />,
            }}
            placeholder="密码"
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
            label="确认密码"
            placeholder="确认密码"
            rules={[
              {
                validator(role,value){
                  if (value !==formRef.current?.getFieldValue("password")){
                    return Promise.reject("两次密码输入不一致")
                  }
                  return Promise.resolve()
                },
              }
            ]}
          />
        </StepsForm.StepForm>
      </StepsForm>
    </>
  );
};

export default FogotPwd;
