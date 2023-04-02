import {
  ActionType,
  PageContainer,
  ProColumns,
  ProTable
} from '@ant-design/pro-components';

import { listInterfaceInfoByPageUsingGET } from '@/services/api-backend/interfaceInfoController';
import {
  Button,
  Descriptions,
  InputNumber, message,
  Modal,
  Popover,
  Switch,
  Tag,
  Tooltip
} from 'antd';
import React, { useRef, useState} from 'react';
import {history, useModel} from "@@/exports";
import {getUserInterfaceLeftNumUsingGET} from "@/services/api-backend/userInterfaceInfoController";
import {generateOrderSnUsingPOST, generateTokenUsingGET} from "@/services/api-order/orderController";

const Index: React.FC = () => {
  const [loading, setLoading] = useState(false);
  const { initialState } = useModel('@@initialState');
  const [activeKey, setActiveKey] = useState<string>('tab1');
  const actionRef = useRef<ActionType>();
  const [tableListDataSource ,setTableListDataSource ]= useState<any>([]);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [data, setData] = useState<API.InterfaceInfo>();
  const [confirmOrderModal ,setConfirmOrderModal] = useState(false);
  const [confirmData,setConfirmData] = useState<API.listInterfaceInfoByPageUsingGETParams>();
  const [totalMoney,setTotalMoney] = useState<any>()
  const [inputNumberValue ,setInputNumberValue] =useState<number>(1)
  const [confirmLoading,setConfirmLoading] = useState(false)
  const columnsMap: Record<string, ProColumns<API.listInterfaceInfoByPageUsingGETParams>[]> = {
    tab1: [
      {
        title: '请求id',
        dataIndex: 'id',
        align: 'center',
      },
      {
        title: '请求名称',
        dataIndex: 'name',
        align: 'center',
        render: (_,record) =><Tooltip title="详细信息"><a onClick={()=>{setIsModalOpen(true),setData(record)}}>{_}</a></Tooltip>
      },
      {
        title: '描述',
        dataIndex: 'description',
        align: 'center',
      },
      {
        title: '请求地址',
        key: 'status1',
        dataIndex: 'url',
        align: 'center',
      },
      {
        title: '请求方式',
        dataIndex: 'method',
        align: 'center',
      },
      {
        title: '计费规则(元/条)',
        dataIndex: 'charging',
        align: 'center',
      },
      {
        title: '可购买次数',
        dataIndex: 'availablePieces',
        align: 'center',
      },
      {
        title: '操作',
        key: 'option',
        valueType: 'option',
        align: 'center',
        width: 120,
        render: (_,record) => [
          <Button type="primary" shape="round" loading={loading} onClick={async ()=>{ await getOrderToken(),setConfirmOrderModal(true) ,setTotalMoney(record.charging),actionRef.current?.reload(),setConfirmData(record)}} >
            购买次数
          </Button>
        ],
      },
    ],
    tab2: [
      {
        title: '请求id',
        dataIndex: 'id',
        align: 'center',
      },
      {
        title: '请求名称',
        dataIndex: 'name',
        render: (_,record) => <Popover content={record.charging+'元/条'} ><a >{_}</a></Popover>,
        align: 'center',
      },
      {
        title: '描述',
        key: 'status2',
        dataIndex: 'description',
        align: 'center',
      },
      {
        title: '请求地址',
        dataIndex: 'url',
        valueType: 'select',
        align: 'center',
      },
      {
        title: '请求方式',
        dataIndex: 'method',
        valueType: 'select',
        align: 'center',
      },
      {
        title: '剩余请求次数',
        dataIndex: 'leftNum',
        valueType: 'select',
        align: 'center',
      },
      {
        title: '操作',
        key: 'option',
        valueType: 'option',
        width: 150,
        render: (_,record) => [
          <Popover content={'剩余可购买次数：'+record.availablePieces+'条'} >
          <Button type="ghost" onClick={async ()=>{await getOrderToken(),setConfirmOrderModal(true) ,setTotalMoney(record.charging),actionRef.current?.reload(),setConfirmData(record)}}  style={{ background: "#13c2c2" }} shape="round"  >
            追加次数
          </Button>
          </Popover>
        ],
      },
    ],
  };

  /**
   * 提交订单
   */
  const submitOrder = async ()=>{
    setConfirmLoading(true)
    try {
      const  res = await generateOrderSnUsingPOST({
        userId:initialState?.loginUser?.id,
        totalAmount:totalMoney,
        orderNum:inputNumberValue,
        interfaceId:confirmData?.id,
        charging:confirmData?.charging
      })
      if (res.code ===0 ){
        actionRef.current?.reload()
        message.success("提交成功")
        setConfirmLoading(false)
        setConfirmOrderModal(false)
        setInputNumberValue(1)
        history.push("/Order/order",res.data)
      }
    }catch (e) {
      message.error("提交失败，请刷新后重试")
      setConfirmLoading(false)
      setConfirmOrderModal(false)
      setInputNumberValue(1)
    }
  }

  /**
   * 获取防重令牌
   */
  const getOrderToken = async ()=>{
    setLoading(true)
    await generateTokenUsingGET({id:initialState?.loginUser?.id})
    setLoading(false)
  }

  /**
   * 设置总金额
   * @param num
   * @param price
   */
  const setTotalNumMoney = (num:any,price:any)=>{
    setTotalMoney((num * price).toFixed(2))
  }

  /**
   * 时间转换(js将 “2021-07-06T06:23:57.000+00:00” 转换为年月日时分秒)
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
  // @ts-ignore
  return (
    <PageContainer>
      <ProTable<API.InterfaceInfo>
        columns={columnsMap[activeKey]}
        actionRef={actionRef}
        dataSource={tableListDataSource} //加载数据
        pagination={{showSizeChanger: true}}
        request={async (params, sorter, filter) => {
          // 表单搜索项会从 params 传入，传递给后端接口。
          if (activeKey === "tab1"){
            const res = await listInterfaceInfoByPageUsingGET({...params,...sorter});
            if (res?.data) {
              setTableListDataSource(res?.data?.records)
              return {
                data: res?.data?.records,
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
          }else {
            const  res = await getUserInterfaceLeftNumUsingGET()
            if (res?.data) {
              setTableListDataSource(res?.data)
              return {
                data: res?.data?.records,
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
          }
        }}
        toolbar={{
          title: '请求次数',
          multipleLine: true,
          tabs: {
            activeKey,
            onChange: async (key) => {
              setActiveKey(key as string)
              setTableListDataSource([])
              actionRef.current?.reload()
            },
            items: [
              {
                key: 'tab1',
                tab: '全部请求',
              },
              {
                key: 'tab2',
                tab: '已购买请求',
              },
            ],
          },
        }}
        rowKey="key"
        search={false}
      />
      <Modal footer={
        <Button  type="primary"  onClick={()=>{setIsModalOpen(false)}}>
          确认
        </Button>
      } destroyOnClose={true} title="详细信息" open={isModalOpen}  onCancel={()=>{setIsModalOpen(false)}}>
        <Descriptions column={1}>
          <Descriptions.Item label="接口状态">
            {<Switch disabled={true} checked={data?.status === 0 ? false : true} />}
          </Descriptions.Item>
          <Descriptions.Item label="描述">{data?.description}</Descriptions.Item>
          <Descriptions.Item label="请求地址">{data?.url}</Descriptions.Item>
          <Descriptions.Item label="请求方法">
            {<Tag color="success">{data?.method}</Tag>}
          </Descriptions.Item>
          <Descriptions.Item label="请求头">{data?.requestHeader}</Descriptions.Item>
          <Descriptions.Item label="请求参数">{data?.requestParams}</Descriptions.Item>
          <Descriptions.Item label="响应头">{data?.responseHeader}</Descriptions.Item>
          <Descriptions.Item label="创建时间">{transformTimestamp(data?.createTime)}</Descriptions.Item>
          <Descriptions.Item label="修改时间">{transformTimestamp(data?.updateTime)}</Descriptions.Item>
        </Descriptions>
      </Modal>


      <Modal title="确认订单" open={confirmOrderModal} onCancel={()=>{setConfirmOrderModal(false),setInputNumberValue(1)}} footer={[
        <Button  onClick={()=>{setConfirmOrderModal(false),setInputNumberValue(1)}}>
          取消
        </Button>,
        <Button type="primary"  loading={confirmLoading} onClick={ async ()=>{
          await submitOrder()
        }}>
          提交订单
        </Button>
      ]}>
        <Descriptions column={1} bordered>
          <Descriptions.Item label="接口名称">{confirmData?.name}</Descriptions.Item>
          <Descriptions.Item label="描述">{confirmData?.description}</Descriptions.Item>
          <Descriptions.Item label="计费规则">{confirmData?.charging} 元/条</Descriptions.Item>
          <Descriptions.Item label="剩余可购买次数">{confirmData?.availablePieces}</Descriptions.Item>
          <Descriptions.Item label="购买次数" >
            <InputNumber min={1} value={inputNumberValue} max={Number(confirmData?.availablePieces)} defaultValue={1} onChange={ (value:any) => {setInputNumberValue(value),setTotalNumMoney(value,confirmData?.charging)}} />
          </Descriptions.Item>
          <Descriptions.Item label="总价" >
            {totalMoney} 元
          </Descriptions.Item>
        </Descriptions>
      </Modal>
    </PageContainer>
  );
};

export default Index;
