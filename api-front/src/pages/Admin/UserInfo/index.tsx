// import CreateUserModal from '@/pages/Admin/UserInfo/components/CreateUserModal';
import UpdateUserModal from '@/pages/Admin/UserInfo/components/UpdateUserModal';


import { PlusOutlined } from '@ant-design/icons';
import type { ActionType, ProColumns } from '@ant-design/pro-components';
import { PageContainer, ProTable } from '@ant-design/pro-components';
import { FormattedMessage, useIntl } from '@umijs/max';
import { Button, message } from 'antd';
import { SortOrder } from 'antd/es/table/interface';
import React, { useRef, useState } from 'react';
import {deleteUserUsingPOST, listUserByPageUsingGET, updateUserUsingPOST} from "@/services/api-backend/userController";

const TableList: React.FC = () => {
  /**
   * @en-US Pop-up window of new window
   * @zh-CN 新建窗口的弹窗
   *  */
  // const [createModalOpen, handleModalOpen] = useState<boolean>(false);
  /**
   * @en-US The pop-up window of the distribution update window
   * @zh-CN 分布更新窗口的弹窗
   * */
  const [updateModalOpen, handleUpdateModalOpen] = useState<boolean>(false);

  const [showDetail, setShowDetail] = useState<boolean>(false);

  const actionRef = useRef<ActionType>();
  const [currentRow, setCurrentRow] = useState<API.User>();
  const [selectedRowsState, setSelectedRows] = useState<API.User[]>([]);

  /**
   * @en-US International configuration
   * @zh-CN 国际化配置
   * */
  const intl = useIntl();

  /**
   * @en-US Add node
   * @zh-CN 用户
   * @param fields
   */
  // const handleAdd = async (fields: API.UserAddRequest) => {
  //   const hide = message.loading('正在添加');
  //   try {
  //     const res = await addUserUsingPOST({ ...fields });
  //     hide();
  //     message.success('添加成功');
  //     actionRef.current?.reload();
  //     handleModalOpen(false);
  //   } catch (error) {
  //     hide();
  //   }
  // };

  /**
   * @en-US Update node
   * @zh-CN 更新节点
   *
   * @param fields
   */
  const handleUpdate = async (fields: API.UserUpdateRequest) => {
    const hide = message.loading('操作中');
    let res = null;
    try {
      res = await updateUserUsingPOST({
        id: currentRow?.id,
        ...fields,
      });
      hide();
      message.success('操作成功');
      actionRef.current?.reload();
      return true;
    } catch (error) {
      hide();
      //message.error(res?.message);
      return false;
    }
  };

  /**
   *  Delete node
   * @zh-CN 删除节点
   *
   * @param selectedRows
   */
  const handleRemove = async (selectedRows: API.User) => {
    const hide = message.loading('正在删除');
    let res;
    if (!selectedRows) return true;
    try {
      res = await deleteUserUsingPOST({
        id: selectedRows.id,
      });
      hide();
      message.success('删除成功');
      actionRef.current?.reload();
      return true;
    } catch (error) {
      hide();
      //message.error(res?.message);
      return false;
    }
  };

  // @ts-ignore
  const columns: ProColumns<API.User>[] = [
    {
      title: '序号',
      valueType: 'index',
    },
    {
      title: '用户账号',
      dataIndex: 'userAccount',
      valueType: 'text',
    },
    {
      title: (
        <FormattedMessage
          id="pages.searchTable.updateForm.ruleName.userName"
          defaultMessage="昵称"
        />
      ),
      dataIndex: 'userName',
      valueType: 'text',
    },
    {
      title: <FormattedMessage id="pages.searchTable.titleDesc" defaultMessage="用户头像" />,
      dataIndex: 'userAvatar',
      valueType: 'textarea',
    },
    {
      title: '手机号',
      dataIndex: 'mobile',
      valueType: 'text',
    },
    {
      title: '性别',
      dataIndex: 'gender',
      valueType: 'text',
    },
    {
      title: '用户角色',
      dataIndex: 'userRole',
      valueType: 'text',
    },
    {
      title: '创建时间',
      dataIndex: 'createTime',
      valueType: 'dateTime',
      hideInForm: true,
    },
    {
      title: '更新时间',
      dataIndex: 'updateTime',
      valueType: 'dateTime',
      hideInForm: true,
    },
    {
      title: <FormattedMessage id="pages.searchTable.titleOption" defaultMessage="操作" />,
      dataIndex: 'option',
      valueType: 'option',
      fixed: 'right',
      render: (_, record) => [
        <a
          key="config"
          onClick={() => {
            handleUpdateModalOpen(true);
            setCurrentRow(record);
          }}
        >
          <FormattedMessage id="pages.searchTable.config" defaultMessage="修改" />
        </a>,
        <Button
          danger
          key="config.delete"
          onClick={() => {
            handleRemove(record);
          }}
        >
          <FormattedMessage id="pages.searchTable.config" defaultMessage="删除" />
        </Button>,
      ],
    },
  ];

  // const addColumns: ProColumns<API.UserAddRequest>[] = [
  //   {
  //     title: '用户账号',
  //     dataIndex: 'userAccount',
  //     valueType: 'text',
  //     formItemProps: {
  //       rules: [{ required: true }],
  //     },
  //   },
  //   {
  //     title: '密码',
  //     dataIndex: 'userPassword',
  //     valueType: 'password',
  //     formItemProps: {
  //       rules: [{ required: true }],
  //     },
  //   },
  //   {
  //     title: '手机号',
  //     dataIndex: 'mobile',
  //     valueType: 'text',
  //     formItemProps: {
  //       rules: [{ required: true }, { pattern: /^1[3-9]\d{9}$/, message: '手机号格式错误！' }],
  //     },
  //   },
  //   {
  //     title: '昵称',
  //     dataIndex: 'userName',
  //     valueType: 'text',
  //   },
  //   {
  //     title: '用户头像',
  //     dataIndex: 'userAvatar',
  //     valueType: 'textarea',
  //   },
  //   {
  //     title: '性别',
  //     dataIndex: 'gender',
  //     valueType: 'text',
  //   },
  // ];

  return (
    <PageContainer>
      <ProTable<API.User, API.PageParams>
        headerTitle={intl.formatMessage({
          id: 'pages.searchTable.title',
          defaultMessage: '用户详情',
        })}
        actionRef={actionRef}
        rowKey="key"
        search={{
          labelWidth: 120,
        }}
        pagination={{ pageSize: 5 }}
        toolBarRender={() => [
          // <Button
          //   type="primary"
          //   key="primary"
          //   onClick={() => {
          //     handleModalOpen(true);
          //   }}
          // >
          //   <PlusOutlined /> <FormattedMessage id="pages.searchTable.new" defaultMessage="新增" />
          // </Button>,
        ]}
        request={async (
          params: {
            pageSize?: number;
            current?: number;
            keyword?: string;
          },
          sort: Record<string, SortOrder>,
          filter: Record<string, React.ReactText[] | null>,
        ) => {
          const res = await listUserByPageUsingGET({ ...params });
          if (res?.data) {
            return {
              data: res?.data.records || [],
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
        columns={columns}
        scroll={{ x: 700 }}
        options={{
          reload: true,
          setting: {
            draggable: true,
            checkable: true,
            checkedReset: false,
            extra: [<a key="confirm">确认</a>],
          },
        }}
      />
      <UpdateUserModal
        columns={columns}
        onSubmit={async (value) => {
          const success = await handleUpdate(value);
          if (success) {
            handleUpdateModalOpen(false);
            setCurrentRow(undefined);
            if (actionRef.current) {
              actionRef.current.reload();
            }
          }
        }}
        onCancel={() => {
          handleUpdateModalOpen(false);
          if (!showDetail) {
            setCurrentRow(undefined);
          }
        }}
        visible={updateModalOpen}
        values={currentRow || {}}
      />
      {/*<CreateUserModal*/}
      {/*  columns={addColumns}*/}
      {/*  onCancel={() => {*/}
      {/*    handleModalOpen(false);*/}
      {/*  }}*/}
      {/*  onSubmit={(values) => {*/}
      {/*    return handleAdd(values);*/}
      {/*  }}*/}
      {/*  visible={createModalOpen}*/}
      {/*/>*/}
    </PageContainer>
  );
};

export default TableList;
