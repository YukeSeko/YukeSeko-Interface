import {
  PageContainer,
} from '@ant-design/pro-components';
import React, {useEffect, useState} from 'react';
import {history} from "@umijs/max";
import {Button, Descriptions, message, Modal, Tag, Tooltip} from "antd";
import ProCard from "@ant-design/pro-card";
import { Statistic } from 'antd';
import {ExclamationCircleFilled} from "@ant-design/icons";
import {cancelOrderSnUsingPOST} from "@/services/api-order/orderController";
import {payUsingPOST} from "@/services/api-thirdParty/aliPayController";
const { Countdown } = Statistic;
const Order: React.FC = () => {
  const [orderData,setOrderData] = useState<API.OrderSnVo>()
  const [deadline,setDeadline] = useState<any>();
  const [countDownLoading,setCountDownLoading] = useState(true)
  const [isModalOpen, setIsModalOpen] = useState(false);
  const { confirm } = Modal;
  /**
   * 初始化数据
   */
  useEffect(()=>{
    if (history.location.state === null){
      history.push("/getRequestCounts")
      message.warning("请重新下单")
    }
    // @ts-ignore
    setOrderData(history.location.state)
    setDeadline(timeToTimestamp(transformTimestamp(orderData?.expirationTime)))
    setCountDownLoading(false)
  })

  /**
   * 时间转化为时间戳
   * @param time
   */
   const timeToTimestamp = (time:any)=>{
    let timestamp = Date.parse(new Date(time).toString());
    return timestamp;
  }
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


  const createAlipay =async () =>{
    const res = await payUsingPOST({alipayTraceNo:orderData?.orderSn,subject:orderData?.name,totalAmount:orderData?.totalAmount,traceNo:orderData?.orderSn})
    const div = document.createElement('div')
    history.push("/Order/paymentStatus",orderData)
    div.innerHTML = res
    document.body.appendChild(div)
    document.forms[0].setAttribute('target', '_blank')
    document.forms[0].submit()
  }

  return (
    <PageContainer style={{position:"relative",height:'100%',width:'100%'}}>
      <ProCard
        title="等待支付订单"
        bordered={true}
        direction="column"
        headStyle={{fontSize:20}}
        style={{backgroundColor:'rgb(240, 242, 245)', width:'60%',height:"90%",overflow:'auto',margin:'auto',position:'fixed',top: 0, left: 0,bottom: 0,right: 0}}
      >
        <Descriptions column={1}>
          <Descriptions.Item label="名称">{orderData?.name}</Descriptions.Item>
          <Descriptions.Item label="接口描述">{orderData?.description}</Descriptions.Item>
          <Descriptions.Item label="订单号">{orderData?.orderSn}</Descriptions.Item>
          <Descriptions.Item label="购买数量">{orderData?.orderNum}</Descriptions.Item>
          <Descriptions.Item label="单价">{orderData?.charging}</Descriptions.Item>
          <Descriptions.Item label="总价" labelStyle={{fontSize:15,marginTop:4}} contentStyle={{fontSize:20}}>{orderData?.totalAmount} 元</Descriptions.Item>
          <Descriptions.Item label="订单创建日期">{transformTimestamp(orderData?.createTime)}</Descriptions.Item>
          <Descriptions.Item label="订单过期时间">{transformTimestamp(orderData?.expirationTime)}</Descriptions.Item>
          <Descriptions.Item label="剩余支付时间" labelStyle={{marginTop:8 ,fontSize:15}}><Tag color="cyan"><Countdown loading={countDownLoading}  value={deadline} onFinish={()=>{history.push("/Order/order",null),message.warning("订单已失效，请重新提交订单")}} format="HH:mm:ss:SSS" /></Tag></Descriptions.Item>
          <Descriptions.Item label="支付方式" labelStyle={{fontSize:15,marginTop:8}}><img src="https://gw.alipayobjects.com/mdn/member_frontWeb/afts/img/A*oRlnSYAsgYQAAAAAAAAAAABkARQnAQ"/></Descriptions.Item>
        </Descriptions>

        <div style={{float:"right" ,display:"flex"}}>
          <Button style={{marginRight:15}} onClick={()=>{setIsModalOpen(true)}} >取消订单</Button>
          <Button type={"primary"}  onClick={createAlipay}> <Tooltip title={"需要支付："+orderData?.totalAmount+" 元"}>
            立即支付
          </Tooltip></Button>
        </div>
      </ProCard>
      <Modal title="取消订单"  open={isModalOpen} onOk={async ()=>{await cancelOrderSnUsingPOST({interfaceId:orderData?.interfaceId,orderNum:orderData?.orderNum,orderSn:orderData?.orderSn}) ,history.push("/getRequestCounts",null),message.success("订单取消成功")}} onCancel={()=>{setIsModalOpen(false)}}>
        您确定要取消该订单吗？
      </Modal>

    </PageContainer>
  );
};

export default Order;
