import {
  PageContainer,
} from '@ant-design/pro-components';
import React, {useEffect, useState} from 'react';
import {history} from "@umijs/max";
import {Button, Descriptions, message, Modal, Popconfirm, Result, Spin, Tag, Tooltip} from "antd";
import ProCard from "@ant-design/pro-card";
import { LoadingOutlined} from "@ant-design/icons";
import Paragraph from "antd/es/typography/Paragraph";
import {queryTradeStatusUsingGET} from "@/services/api-thirdParty/aliPayController";
const payStatus: React.FC = () => {
  const [orderData,setOrderData] = useState<API.OrderSnVo>()
  const antIcon = <LoadingOutlined style={{ fontSize: 70 }} spin />;
  const [loading, setLoading] = useState(true);
  const [payInfo,setPayInfo] = useState<API.AlipayInfo>()
  /**
   * 初始化数据
   */
  useEffect(  ()=>{
    if (history.location.state === null){
      history.push("/getRequestCounts")
      message.warning("请重新下单")
    }
    //@ts-ignore
    setOrderData(history.location.state)
    queryTradeStatus()
  })


  /**
   * 轮询 redis 来查询支付状态
   */
  const queryTradeStatus = async () =>{
    while (loading){
      if (history.location.pathname!=='/Order/paymentStatus'){
        break
      }
      if (orderData!==null){
        //每秒请求一次
        await waitTime(2000)
        const res = await queryTradeStatusUsingGET({orderSn:orderData?.orderSn})
        console.log(res)
        if (res.data !==null){
          setPayInfo(res.data)
          setLoading(!loading)
          break
        }
      }
    }
  }
  /**
   * 倒计时
   * @param time
   */
  const waitTime = (time: number = 100) => {
    return new Promise((resolve) => {
      setTimeout(() => {
        resolve(true);
      }, time);
    });
  };

  /**
   * 日期格式化
   * @param timestamp
   */
  const transformTimestamp = (timestamp:any) => {
    let a = new Date(timestamp).getTime();
    const date = new Date(a);
    const Y = date.getFullYear() + '-';
    const M = (date.getMonth() + 1 < 10 ? '0' + (date.getMonth() + 1) : date.getMonth() + 1) + '-';
    const D = (date.getDate() < 10 ? '0' + date.getDate() : date.getDate()) + '  ';
    const h = (date.getHours() < 10 ? '0' + date.getHours() : date.getHours()) + ':';
    const m = (date.getMinutes() < 10 ? '0' + date.getMinutes() : date.getMinutes());
    const dateString = Y + M + D + h + m;
    return dateString;
  }

  return (
    <PageContainer style={{position:"relative",height:'100%',width:'100%'}}>
      <ProCard
        bordered={true}
        direction="column"
        headStyle={{fontSize:20}}
        style={{background:'rgb(240, 242, 245)',width:'50%',height:"90%",overflow:'auto',margin:'auto',position:'fixed',top: 0, left: 0,bottom: 0,right: 0}}
      >
        <div hidden={!loading}>
          <Result
            style={{marginTop:35}}
            icon={<Spin size="large" indicator={antIcon} />}
            title="支 付 结 果 查 询 中 ..."
            subTitle={"订 单 号: "+orderData?.orderSn+"   查询操作需要耗费一段时间，请耐心等候！"}
            extra={[
              <Popconfirm
                title="重 新 提 交 "
                description="您确定要重新提交支付请求吗？"
                onConfirm={async ()=>{history.go(-1)}}
              >
                <Button  key="console" style={{marginTop:20}} danger>
                  重 新 提 交
                </Button>
              </Popconfirm>,
            ]}
          />
        </div>
        <div hidden={loading}>
          <Result
            status="success"
            title="购  买  成  功"
            extra={[
              <Button type="primary" key="console" onClick={()=>{history.push("/myOrderInfo")}}>
                查 看 订 单
              </Button>,
              <Button key="buy" onClick={()=>{history.push("/getRequestCounts")}}>再 次 购 买</Button>,
            ]}
          >
            <Descriptions column={1}>
              <Descriptions.Item labelStyle={{fontSize:17}} contentStyle={{fontSize:17}} label="订单号">{orderData?.orderSn}</Descriptions.Item>
              <Descriptions.Item labelStyle={{fontSize:17}} contentStyle={{fontSize:17}} label="交易凭证号">{payInfo?.tradeNo}</Descriptions.Item>
              <Descriptions.Item labelStyle={{fontSize:17}} contentStyle={{fontSize:17}} label="付款时间">{transformTimestamp(payInfo?.gmtPayment)}</Descriptions.Item>
              <Descriptions.Item labelStyle={{fontSize:17}} contentStyle={{fontSize:17}} label="付款金额">{payInfo?.totalAmount}</Descriptions.Item>
              <Descriptions.Item labelStyle={{fontSize:17}} contentStyle={{fontSize:17}} label="支付宝id">{payInfo?.buyerId}</Descriptions.Item>
            </Descriptions>
          </Result>
        </div>
      </ProCard>

    </PageContainer>
  );
};

export default payStatus;
