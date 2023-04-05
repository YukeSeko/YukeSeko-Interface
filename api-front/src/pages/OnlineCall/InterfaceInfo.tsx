import { PageContainer, ProDescriptions } from '@ant-design/pro-components';

import { getInterfaceInfoByIdUsingGET } from '@/services/api-backend/interfaceInfoController';

import { apiClientUsingPOST } from '@/services/api-backend/interfaceClientController';
import { useParams } from '@@/exports';
import { Button, Card, Descriptions, Empty, Form, Input, message, Switch, Tag } from 'antd';
import { createHashHistory } from 'history';
import React, { useEffect, useState } from 'react';

const InterfaceInfo: React.FC = () => {
  const [loading, setLoading] = useState(false);
  const [data, setData] = useState<API.InterfaceInfoVo>();
  const params = useParams(); //拿到路由中的参数，参考umi的文档
  const [invokeRes, setInvokeRes] = useState<any>();
  const history = createHashHistory(); //返回上一页这段代码段代码
  const loadData = async () => {
    if (!params.id) {
      //message.error('参数错误');
      return;
    }
    setLoading(true);
    try {
      const res = await getInterfaceInfoByIdUsingGET({
        id: Number(params.id),
      });
      setData(res.data);
    } catch (error: any) {
      //message.error('查询失败' + error.message);
    }
    setLoading(false);
  };

  useEffect(() => {
    loadData();
  }, []);

  const onFinish = async (values: any) => {
    setLoading(true);
    try {
      const res = await apiClientUsingPOST({
        id: data?.id,
        url: data?.url,
        method: data?.method,
        ...values,
      });
      //判断是否json
      let parse = null;
      if (res.data) {
        if (res.data.toString().startsWith('{') && res.data.toString().endsWith('}')) {
          parse = JSON.parse(res.data.toString());
        }
        if (parse && parse.code != 0) {
          message.error(parse.message);
          setInvokeRes(parse.message);
          return;
        }
        if (res.code == 0) {
          message.success('调用成功');
          // @ts-ignore
          data.leftNum = data.leftNum - 1;
          setData(data);
          setInvokeRes(res.data);
        }
      }
    } catch (error: any) {
      //message.error(error.message);
    }
    setLoading(false);
  };

  /**
   * 返回上一页面
   */
  const back = () => {
    history.go(-1);
  };

  return (
    <PageContainer
      header={{
        title: '接口详情',
        extra: [
          <Button type={'primary'} onClick={back}>
            返回
          </Button>,
        ],
      }}
    >
      <Card>
        {data ? (
          <ProDescriptions  title={data.name} column={1}>
            <ProDescriptions.Item label="接口状态">
              {<Switch disabled={true} checked={data.status === 0 ? false : true} />}
            </ProDescriptions.Item>
            <ProDescriptions.Item label="描述">{data.description}</ProDescriptions.Item>
            <ProDescriptions.Item label="请求地址">{data.url}</ProDescriptions.Item>
            <ProDescriptions.Item label="请求方法">
              {<Tag color="success">{data.method}</Tag>}
            </ProDescriptions.Item>
            <ProDescriptions.Item label="请求头" valueType="jsonCode">{data.requestHeader}</ProDescriptions.Item>
            <ProDescriptions.Item label="请求参数">{data.requestParams}</ProDescriptions.Item>
            <ProDescriptions.Item label="响应头" valueType="jsonCode">{data.responseHeader}</ProDescriptions.Item>
            <ProDescriptions.Item label="剩余调用次数">
              <Tag color={data.leftNum ? (data?.leftNum > 10 ? '#87d068' : '#f50') : '#f50'}>
                {data.leftNum}
              </Tag>
            </ProDescriptions.Item>
          </ProDescriptions >
        ) : (
          <Empty />
        )}
      </Card>
      <Card style={{ marginTop: 10 }}>
        <Form title={'测试调用'} name="basic" onFinish={onFinish} layout={'inline'}>
          <Form.Item label="请求参数" name="userRequestParams" style={{ width: 600 }}>
            <Input.TextArea />
          </Form.Item>
          <Form.Item wrapperCol={{ span: 16 }}>
            <Button type="primary" htmlType="submit">
              在线调用
            </Button>
          </Form.Item>
        </Form>
      </Card>
      <Card>{invokeRes}</Card>
    </PageContainer>
  );
};

export default InterfaceInfo;
