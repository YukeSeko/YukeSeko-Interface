import CreateModal from '@/pages/Admin/InterfaceInfo/components/CreateModal';
import UpdateModal from '@/pages/Admin/InterfaceInfo/components/UpdateModal';

import {
  addInterfaceInfoUsingPOST,
  deleteInterfaceInfoUsingPOST,
  getAllInterfaceInfoByPageUsingGET,
  offlineInterfaceInfoUsingPOST,
  onlineInterfaceInfoUsingPOST,
  updateInterfaceInfoUsingPOST,
} from '@/services/api-backend/interfaceInfoController';
import { PlusOutlined } from '@ant-design/icons';
import type { ActionType, ProColumns, ProDescriptionsItemProps } from '@ant-design/pro-components';
import {
  FooterToolbar,
  PageContainer,
  ProDescriptions,
  ProTable,
} from '@ant-design/pro-components';
import { FormattedMessage, useIntl } from '@umijs/max';
import { Button, Drawer, message, Tag } from 'antd';
import { SortOrder } from 'antd/es/table/interface';
import React, { useRef, useState } from 'react';

const TableList: React.FC = () => {
  /**
   * @en-US Pop-up window of new window
   * @zh-CN 新建窗口的弹窗
   *  */
  const [createModalOpen, handleModalOpen] = useState<boolean>(false);
  /**
   * @en-US The pop-up window of the distribution update window
   * @zh-CN 分布更新窗口的弹窗
   * */
  const [updateModalOpen, handleUpdateModalOpen] = useState<boolean>(false);

  const [showDetail, setShowDetail] = useState<boolean>(false);

  const actionRef = useRef<ActionType>();
  const [currentRow, setCurrentRow] = useState<API.InterfaceInfo>();
  const [selectedRowsState, setSelectedRows] = useState<API.InterfaceInfo[]>([]);

  /**
   * @en-US International configuration
   * @zh-CN 国际化配置
   * */
  const intl = useIntl();

  /**
   * @en-US Add node
   * @zh-CN 添加节点
   * @param fields
   */
  const handleAdd = async (fields: API.InterfaceInfo) => {
    const hide = message.loading('正在添加');
    try {
      await addInterfaceInfoUsingPOST({ ...fields });
      hide();
      message.success('添加成功');
      actionRef.current?.reload();
      handleModalOpen(false);
    } catch (error) {
      hide();
      //message.error('添加失败');
    }
  };

  /**
   * @en-US Update node
   * @zh-CN 更新节点
   *
   * @param fields
   */
  const handleUpdate = async (fields: API.InterfaceInfo) => {
    const hide = message.loading('操作中');
    try {
      await updateInterfaceInfoUsingPOST({
        id: currentRow?.id,
        ...fields,
      });
      hide();
      message.success('操作成功');
      actionRef.current?.reload();
      return true;
    } catch (error) {
      hide();
      //message.error('操作失败');
      return false;
    }
  };

  /**
   *  Delete node
   * @zh-CN 删除节点
   *
   * @param selectedRows
   */
  const handleRemove = async (selectedRows: API.InterfaceInfo) => {
    const hide = message.loading('正在删除');
    if (!selectedRows) return true;
    try {
      await deleteInterfaceInfoUsingPOST({
        id: selectedRows.id,
      });
      hide();
      message.success('删除成功');
      actionRef.current?.reload();
      return true;
    } catch (error) {
      hide();
      //message.error('删除失败');
      return false;
    }
  };

  /**
   * 发布接口
   * @param IdRequest
   */
  const handleOnline = async (selectedRows: API.IdRequest) => {
    const hide = message.loading('上线接口中');
    if (!selectedRows) return true;
    try {
      await onlineInterfaceInfoUsingPOST({
        id: selectedRows.id,
      });
      hide();
      message.success('操作成功');
      actionRef.current?.reload();
      return true;
    } catch (error) {
      hide();
      //message.error('操作失败');
      return false;
    }
  };

  /**
   * 下线接口
   * @param IdRequest
   */
  const handleOffline = async (selectedRows: API.IdRequest) => {
    const hide = message.loading('下线接口中');
    if (!selectedRows) return true;
    try {
      await offlineInterfaceInfoUsingPOST({
        id: selectedRows.id,
      });
      hide();
      message.success('操作成功');
      actionRef.current?.reload();
      return true;
    } catch (error) {
      hide();
      //message.error('操作失败');
      return false;
    }
  };

  // @ts-ignore
  const columns: ProColumns<API.InterfaceInfo>[] = [
    {
      title: '序号',
      dataIndex: 'id',
      valueType: 'index',
    },
    {
      title: (
        <FormattedMessage
          id="pages.searchTable.updateForm.ruleName.nameLabel"
          defaultMessage="接口名称"
        />
      ),
      dataIndex: 'name',
      valueType: 'text',
      formItemProps: {
        rules: [{ required: true }],
      },
    },
    {
      title: <FormattedMessage id="pages.searchTable.titleDesc" defaultMessage="描述" />,
      dataIndex: 'description',
      valueType: 'textarea',
      formItemProps: {
        rules: [{ required: true }],
      },
    },
    {
      title: '请求方法',
      dataIndex: 'method',
      valueType: 'text',
      render: (_, { method }) => {
        let color = method === 'GET' ? 'green' : 'geekblue';
        if (method === 'POST') {
          color = 'volcano';
        }
        return <Tag color={color}>{method}</Tag>;
      },
      formItemProps: {
        rules: [{ required: true }],
      },

    },
    {
      title: '计费规则(元/条)',
      dataIndex: 'charging',
      valueType: 'text',
      formItemProps: {
        rules: [{ required: true }],
      },

    },
    {
      title: '接口剩余次数',
      dataIndex: 'availablePieces',
      valueType: 'text',
      formItemProps: {
        rules: [{ required: true }],
      },
    },
    {
      title: '接口地址',
      dataIndex: 'url',
      valueType: 'text',
      formItemProps: {
        rules: [{ required: true }],
      },
    },
    {
      title: '请求头',
      dataIndex: 'requestHeader',
      valueType: 'textarea',
      formItemProps: {
        rules: [{ required: true }],
      },
    },
    {
      title: '请求参数',
      dataIndex: 'requestParams',
      valueType: 'textarea',
      formItemProps: {
        rules: [{ required: true }],
      },
    },
    {
      title: '响应头',
      dataIndex: 'responseHeader',
      valueType: 'textarea',
      formItemProps: {
        rules: [{ required: true }],
      },
    },
    {
      title: <FormattedMessage id="pages.searchTable.titleStatus" defaultMessage="状态" />,
      dataIndex: 'status',
      hideInForm: true,
      valueEnum: {
        0: {
          text: (
            <Tag color="#f50">
              <FormattedMessage id="pages.searchTable.nameStatus.default" defaultMessage="关闭" />
            </Tag>
          ),
          status: 'Default',
        },
        1: {
          text: (
            <Tag color="#87d068">
              <FormattedMessage id="pages.searchTable.nameStatus.running" defaultMessage="开启" />
            </Tag>
          ),
          status: 'Processing',
        },
      },
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

        record.status === 0 ? (
          <Button
            type="primary"
            key={record.id}
            onClick={() => {
              handleOnline(record);
            }}
          >
            上线
          </Button>
        ) : (
          false
        ),

        record.status === 1 ? (
          <Button
            type="text"
            danger
            key={record.id}
            onClick={() => {
              handleOffline(record);
            }}
          >
            下线
          </Button>
        ) : (
          false
        ),

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

  return (
    <PageContainer>
      <ProTable<API.RuleListItem, API.PageParams>
        headerTitle={intl.formatMessage({
          id: 'pages.searchTable.title',
          defaultMessage: '接口详情',
        })}
        actionRef={actionRef}
        rowKey="key"
        search={{
          labelWidth: 120,
        }}
        pagination={{ pageSize: 5 }}
        toolBarRender={() => [
          <Button
            type="primary"
            key="primary"
            onClick={() => {
              handleModalOpen(true);
            }}
          >
            <PlusOutlined /> <FormattedMessage id="pages.searchTable.new" defaultMessage="新增" />
          </Button>,
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
          const res = await getAllInterfaceInfoByPageUsingGET({ ...params,...sort});
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
        scroll={{ x: 1300 }}
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
      <UpdateModal
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

      <Drawer
        width={600}
        open={showDetail}
        onClose={() => {
          setCurrentRow(undefined);
          setShowDetail(false);
        }}
        closable={false}
      >
        {currentRow?.name && (
          <ProDescriptions<API.RuleListItem>
            column={2}
            title={currentRow?.name}
            request={async () => ({
              data: currentRow || {},
            })}
            params={{
              id: currentRow?.name,
            }}
            columns={columns as ProDescriptionsItemProps<API.RuleListItem>[]}
          />
        )}
      </Drawer>
      <CreateModal
        columns={columns}
        onCancel={() => {
          handleModalOpen(false);
        }}
        onSubmit={(values) => {
          return handleAdd(values);
        }}
        visible={createModalOpen}
      />
    </PageContainer>
  );
};

export default TableList;
