
import { useEmotionCss } from '@ant-design/use-emotion-css';
import { SelectLang, useModel } from '@umijs/max';
import { Alert, Button, Descriptions, Drawer, message, Modal, Switch } from 'antd';
import Paragraph from 'antd/es/typography/Paragraph';
import React, { useState } from 'react';
import Avatar from './AvatarDropdown';
import {getAuthByUserIdUsingGET, updateAuthStatusUsingGET} from "@/services/api-backend/authController";
import {userLogoutUsingPOST} from "@/services/api-backend/userController";

export type SiderTheme = 'light' | 'dark';

const GlobalHeaderRight: React.FC = () => {
  const [open, setOpen] = useState(false);
  type auth = { appid: number; accesskey: String; secretkey: String; status: number };
  const [check, setCheck] = useState(false);
  const [auth, setAuth] = useState<any>();
  const [openModal, setOpenModal] = useState(false);
  const [confirmLoading, setConfirmLoading] = useState(false);
  const [loading, setLoading] = useState(false);
  const className = useEmotionCss(() => {
    return {
      display: 'flex',
      height: '48px',
      marginLeft: 'auto',
      overflow: 'hidden',
      gap: 8,
    };
  });

  const actionClassName = useEmotionCss(({ token }) => {
    return {
      display: 'flex',
      float: 'right',
      height: '48px',
      marginLeft: 'auto',
      overflow: 'hidden',
      cursor: 'pointer',
      padding: '0 12px',
      borderRadius: token.borderRadius,
      '&:hover': {
        backgroundColor: token.colorBgTextHover,
      },
    };
  });

  const { initialState, setInitialState } = useModel('@@initialState');

  const showDrawer = async () => {
    setOpen(true);
    if (!initialState?.loginUser) {
      //message.error('非法请求');
      return;
    }
    const res = await getAuthByUserIdUsingGET({
      id: initialState?.loginUser.id,
    });
    if (res.data) {
      setAuth(res.data);
      setCheck(res.data.status ? false : true);
    }
  };

  /**
   * 开启按钮修改
   * @param checked
   */
  const onChange = (checked: boolean) => {
    setOpenModal(true);
    setLoading(true);
  };
  // ===修改对话框开始===
  const handleOk = () => {
    setConfirmLoading(true);
    setTimeout(() => {
      if (!initialState?.loginUser) {
        //message.error('非法请求');
        return;
      }
      updateAuthStatusUsingGET({
        id: initialState?.loginUser.id,
      }).then((res) => {
        if (res.code == 0) {
          setOpenModal(false);
          setConfirmLoading(false);
          setCheck(!check);
          setLoading(false);
        }
        message.success(res.data);
      });
    }, 2000);
  };
  const handleCancel = () => {
    console.log('Clicked cancel button');
    setOpenModal(false);
    setLoading(false);
  };
  // ===修改对话框结束===

  const onClose = () => {
    setOpen(false);
  };
  /**
   * 退出登录
   */
  const loginOut = async () => {
    setInitialState({ loginUser: undefined });
    await userLogoutUsingPOST();
  };

  if (!initialState) {
    return null;
  }

  return (
    <div style={{ display: 'flex' }}>
      <Avatar />
      <>
        <Button
          style={{ alignItems: 'center', marginLeft: 15, marginTop: 10 }}
          type="primary"
          onClick={showDrawer}
        >
          API密钥管理
        </Button>
        <Drawer title="API密钥信息" placement="right" onClose={onClose} open={open} >
          <Alert
            message="安全提示"
            banner
            description="您的 API 密钥代表您的账号身份和所拥有的权限，使用 API 密钥可以操作您名下的所有平台上的资源。
                 为了您的财产和服务安全，请妥善保存密钥，请勿通过任何方式（如 GitHub）上传或者分享您的密钥信息。"
          />
          <Descriptions column={1} style={{ marginTop: 20 }}>
            <Descriptions.Item label="AppId">
              <Paragraph copyable>{auth?.appid}</Paragraph>
            </Descriptions.Item>
            <Descriptions.Item label="accessKey">
              <Paragraph copyable>{auth?.accesskey}</Paragraph>
            </Descriptions.Item>
            <Descriptions.Item label="secretKey">
              <Paragraph copyable>{auth?.secretkey}</Paragraph>
            </Descriptions.Item>
          </Descriptions>
          <p>
            状态：
            <Switch
              loading={loading}
              checkedChildren="开启"
              unCheckedChildren="关闭"
              checked={check}
              onChange={onChange}
            />{' '}
          </p>
        </Drawer>
        <Modal
          title="修改API密钥状态"
          open={openModal}
          onOk={handleOk}
          confirmLoading={confirmLoading}
          onCancel={handleCancel}
        >
          <p>{'您确定要执行该操作吗？'}</p>
        </Modal>
      </>
      <SelectLang className={actionClassName} />
    </div>
  );
};
export default GlobalHeaderRight;
