import {PageContainer, ProFormCaptcha, ProFormText} from '@ant-design/pro-components';

import { listInterfaceInfoByPageUsingGET } from '@/services/api-backend/interfaceInfoController';
import {Alert, List, message} from 'antd';
import React, {useEffect, useRef, useState} from 'react';
import {useModel} from "@@/exports";
import {ModalForm, ProFormInstance} from "@ant-design/pro-form/lib";
import {LockOutlined, MobileOutlined} from "@ant-design/icons";
import {bindPhoneUsingPOST, captchaUsingGET, getCaptchaUsingGET} from "@/services/api-backend/userController";
import {rules} from "@typescript-eslint/eslint-plugin";
import {randomStr} from "@antfu/utils";

const Index: React.FC = () => {
  const [loading, setLoading] = useState(false);
  const [list, setList] = useState<API.InterfaceInfo[]>([]);
  const [total, setTotal] = useState<number>(0);
  const loadData = async (current = 1, pageSize = 7) => {
    setLoading(true);
    try {
      const res = await listInterfaceInfoByPageUsingGET({
        current,
        pageSize,
      });
      setList(res?.data?.records ?? []);
      setTotal(res?.data?.total ?? 0);
    } catch (error: any) {
    }
    setLoading(false);
  };
  useEffect(() => {
    loadData();
  }, []);
  return (
    <PageContainer>
      <List
        className="my-list"
        loading={loading}
        itemLayout="horizontal"
        dataSource={list}
        pagination={{
          pageSize: 7,
          total,
          showTotal(total: number) {
            return '共' + total + '条数据';
          },
          onChange(page, pageSize) {
            loadData(page, pageSize);
          },
        }}
        renderItem={(item) => {
          const router = `/OnlineCall/InterfaceInfo/${item.id}`;
          return (
            <List.Item
              actions={[
                <a key={item.id} href={router}>
                  查看
                </a>,
              ]}
            >
              <List.Item.Meta
                title={<a href={router}>{item.name}</a>}
                description={item.description}
              />
            </List.Item>
          );
        }}
      />
    </PageContainer>

  );
};

export default Index;
