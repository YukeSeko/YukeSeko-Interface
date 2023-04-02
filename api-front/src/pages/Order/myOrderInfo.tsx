import {
  ActionType,
  PageContainer, ProColumns, ProTable,
} from '@ant-design/pro-components';
import React, {useEffect, useRef, useState} from 'react';
import {Badge, Button, message, Popconfirm, Popover, Space, Tag, Tooltip} from "antd";
import ProList from "@ant-design/pro-list/lib";
import {cancelOrderSnUsingPOST, getCurrentOrderInfoUsingPOST} from "@/services/api-order/orderController";
import {history, useModel} from "@@/exports";
import {request} from "@umijs/max";

const myOrderInfo: React.FC = () => {
  const [activeKey, setActiveKey] = useState<React.Key | undefined>('tab1');
  const { initialState } = useModel('@@initialState');
  const [tableListDataSource ,setTableListDataSource ]= useState<any>([]);
  const actionRef = useRef<ActionType>();
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

  const columns: ProColumns<API.ApiOrderStatusVo>[] = [
    {
      title: '排序',
      dataIndex: 'index',
      valueType: 'indexBorder',
      width: 48,
    },
    {
      title: '订单号',
      dataIndex: 'orderSn',
      width:100
    },
    {
      title: '名称',
      dataIndex: 'name',
    },
    {
      title: '状态',
      dataIndex: 'status',
      filters: true,
      onFilter: true,
      valueEnum: {
        all: { text: '全部', status: 'status' },
        1: { text: '已完成', status: 'Success' },
        0: { text: '待支付', status: 'Processing' },
        2: { text: '无效订单', status: 'Error' },
      },
    },
    {
      title: '数量',
      dataIndex: 'orderNum',
    },
    {
      title: '付款金额',
      dataIndex: 'totalAmount',
      // 前端排序
      sorter: (a, b) =>{
        if (a.totalAmount&&b.totalAmount){
          return a.totalAmount - b.totalAmount
        }
        return 1
      } ,
      valueType: 'money',
    },
    {
      title: '支付时间',
      dataIndex: 'gmtPayment',
      sorter:(a,b)=>{
        if (a.gmtPayment&&b.gmtPayment){
          return Number(a.gmtPayment) - Number(b.gmtPayment)
        }
        return 1
      },
      render: (_, current) => {
        if (current.status===0){
          return [
            <Popconfirm
              title="取 消 订 单"
              description="您确定要取消订单吗？"
              onConfirm={
                async ()=>{
                  await cancelOrderSnUsingPOST({interfaceId:current?.interfaceId,orderNum:current?.orderNum,orderSn:current?.orderSn}),
                    message.success("订单取消成功"),
                    actionRef.current?.reload
                }
              }
              okText="Yes"
              cancelText="No"
            >
              <Button danger type="text" >取消订单</Button>
            </Popconfirm>,
            <Button type={"primary"} onClick={()=>{history.push("/Order/order",current)}}>去付款</Button>]
        }
        return current.gmtPayment===null ? null:transformTimestamp(current.gmtPayment);
      },
    },
  ]
  return (
    <PageContainer style={{position:"relative",height:'100%',width:'100%'}}>
      <ProTable<API.ApiOrderStatusVo>
        columns={columns}
        actionRef={actionRef}
        dataSource={tableListDataSource}
        pagination={{showSizeChanger: true}}
        request={async (params) =>{
          const res = await getCurrentOrderInfoUsingPOST({userId:initialState?.loginUser?.id,...params})
          if (res?.data) {
            setTableListDataSource(res?.data?.records)
            return {
              data: tableListDataSource,
              success: true,
              total: res?.data.total || 0,
            };
          } else {
            return {
              data: [],
              success: false,
              total: 0,
            };
          }
        }}
        rowKey="id"
        search={false}
        dateFormatter="string"
        headerTitle="订单列表"
      />
    </PageContainer>
  );
};

export default myOrderInfo;
