import {PageContainer, ProFormCaptcha, ProFormText} from '@ant-design/pro-components';

import {
  getInterfaceCountUsingGET,
} from '@/services/api-backend/interfaceInfoController';
import {Alert, Button, Card, Col, List, message, Row, Statistic} from 'antd';
import React, {useEffect, useRef, useState} from 'react';
import {useModel} from "@@/exports";
import {ModalForm, ProFormInstance} from "@ant-design/pro-form/lib";
import {
  AccountBookOutlined,
  LikeOutlined,
  LockOutlined,
  MobileOutlined,
  RiseOutlined, StarOutlined,
  UserOutlined
} from "@ant-design/icons";
import {
  bindPhoneUsingPOST,
  captchaUsingGET,
  getActiveUserUsingGET,
  getCaptchaUsingGET, getEchartsDataUsingGET, getGitHubStars
} from "@/services/api-backend/userController";
import {randomStr} from "@antfu/utils";
import {getSuccessOrderUsingGET} from "@/services/api-order/orderController";
import Echarts from "@/pages/index/components/echartsComponents";

const Index: React.FC = () => {
  const [loading, setLoading] = useState(false);
  const [list, setList] = useState<API.InterfaceInfo[]>([]);
  const [total, setTotal] = useState<number>(0);
  const { initialState ,setInitialState} = useModel('@@initialState');
  const [showModal,setShowModal] =useState(false);
  const formRef = useRef<ProFormInstance>();
  const [imageUrl,setImageUrl] =useState<any>(null);
  const [activeUser,setActiveUser] = useState<any>()
  const [interfaceCount,setInterfaceCount] =useState<any>()
  const [gitHubCount,setGitHubCount] = useState<any>()
  const [orderSuccess,setOrderSuccess] = useState<any>()
  const [xAxisData,setXAxisData] = useState<any>()
  const [seriesDataUser,setSeriesDataUser] = useState<any>()
  const [seriesDataInterface,setSeriesDataInterface] = useState<any>()
  const [seriesDataOrder,setSeriesDataOrder] = useState<any>()
  useEffect(() => {
    checkLoginUserPhone();
    loadData()
  }, []);

  const loadData =async()=>{
    try {
      //获取活跃用户数
      const resUser = await getActiveUserUsingGET()
      //获取可调用接口数
      const resInterface = await getInterfaceCountUsingGET()
      //获取订单交易成功的数据
      const resOrderSuccess =  await getSuccessOrderUsingGET()
      //获取echarts中的数据
      const resEcharts = await getEchartsDataUsingGET()
      //获取GitHub上该项目的stars
      const resGithub = await getGitHubStars()
      setInterfaceCount(resInterface?.data)
      setGitHubCount(resGithub?.data)
      setOrderSuccess(resOrderSuccess?.data)
      setActiveUser(resUser?.data)
      if (resEcharts?.data){
        setXAxisData(resEcharts?.data[0])
        setSeriesDataUser(resEcharts?.data[1])
        setSeriesDataInterface(resEcharts?.data[2])
        setSeriesDataOrder(resEcharts?.data[3])
      }
    }catch (e) {}
  }

  const checkLoginUserPhone = () =>{
    if (initialState?.loginUser?.mobile ===null){
      setShowModal(true)
      getCaptcha()
    }
  }
  const waitTime = (time: number = 100) => {
    return new Promise((resolve) => {
      setTimeout(() => {
        resolve(true);
      }, time);
    });
  };

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

  return (
    <PageContainer>
      <Alert type="success" message="开发不易，给孩子点个Stars吧(˚ ˃̣̣̥᷄⌓˂̣̣̥᷅ )，please~ →→→→→→→→→"  showIcon
       action={
         <Button  size="small" onClick={()=>{
           window.open("https://github.com/YukeSeko/YukeSeko-Interface")
         }} type="link">
           这次一定！
         </Button>
       }
      />
      <Row style={{marginTop:20}} gutter={16}>
        <Col span={6}>
          <Card bordered={false}>
            <Statistic
              title="全 站 用 户 数"
              value={activeUser}
              valueStyle={{ color: '#0000FF' }}
              prefix={<UserOutlined />}
              suffix="位"
            />
          </Card>
        </Col>
        <Col span={6}>
          <Card bordered={false}>
            <Statistic
              title="全 站 可 调 用 接 口 数"
              value={interfaceCount}
              valueStyle={{ color: '#cf1322' }}
              prefix={<RiseOutlined />}
              suffix="个"
            />
          </Card>
        </Col>
        <Col span={6}>
          <Card bordered={false}>
            <Statistic
              title="全 站 订 单 成 交 数"
              value={orderSuccess}
              valueStyle={{ color: '#3f8600' }}
              prefix={<AccountBookOutlined />}
            />
          </Card>
        </Col>
        <Col span={6}>
          <Card bordered={false}>
            <Statistic
              title="GitHub Stars"
              value={gitHubCount}
              prefix={<LikeOutlined />}
            />
          </Card>
        </Col>
      </Row>

      <Echarts seriesDataInterface={seriesDataInterface} seriesDataOrder={seriesDataOrder} seriesDataUser={seriesDataUser} xAxisData={xAxisData}/>

      {/*第一次进入未绑定手机号的需要进行绑定*/}
      <ModalForm
        open={showModal}
        formRef={formRef}
        modalProps={{closable:false,style:{maxWidth:500}}}
        submitter={{
          searchConfig: {
            resetText: '重置',
          },
          resetButtonProps: {
            onClick: () => {
              formRef.current?.resetFields();
              //   setModalVisible(false);
            },
          },
        }}
        onFinish={async (values) => {
          try {
            await waitTime(2000);
            const signature = localStorage.getItem("api-open-platform-randomString")
            const res = await bindPhoneUsingPOST({
              id:initialState?.loginUser?.id,
              userAccount:initialState?.loginUser?.userAccount,
              ...values},{
              headers: {
                "signature": signature
              },
            })
            if (res?.data){
              const mobile = res.data.mobile
              const loginUser = initialState?.loginUser
              if (loginUser){
                loginUser.mobile=mobile
              }
              setInitialState({ loginUser: loginUser })
              message.success('提交成功');
              setShowModal(false)
            }
            return true;
          }catch (e) {
            return false;
          }
        }}
      >
        <Alert
          message="绑定手机号"
          description="绑定手机号才能继续其他操作"
          type="info"
          showIcon
          style={{marginBottom:20}}
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
          ]}
          onGetCaptcha={async (mobile) => {
            //获取验证成功后才会进行倒计时
            const result = await captchaUsingGET({
              mobile,
            });
            if (!result) {
              return;
            }
            message.success('获取验证码成功！');
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
        <Alert message="注意！绑定手机号后不可修改！" type="warning" showIcon />
      </ModalForm>
    </PageContainer>

  );
};

export default Index;
