declare namespace API {
  type ApiOrderStatusVo = {
    description?: string;
    gmtPayment?: string;
    name?: string;
    orderNum?: number;
    orderSn?: string;
    status?: number;
    totalAmount?: number;
    tradeNo?: string;
    expirationTime?: string;
    charging?: number;
    createTime?: string;
    interfaceId?: number;
  };

  type BaseResponse = {
    code?: number;
    data?: Record<string, any>;
    message?: string;
  };

  type BaseResponseOrderSnVo_ = {
    code?: number;
    data?: OrderSnVo;
    message?: string;
  };

  type BaseResponsePageApiOrderStatusVo_ = {
    code?: number;
    data?: PageApiOrderStatusVo_;
    message?: string;
  };

  type cancelOrderSnUsingPOSTParams = {
    interfaceId?: number;
    orderNum?: number;
    orderSn?: string;
  };

  type generateOrderSnUsingPOSTParams = {
    charging?: number;
    interfaceId?: number;
    orderNum?: number;
    totalAmount?: number;
    userId?: number;
  };

  type generateTokenUsingGETParams = {
    /** id */
    id?: number;
  };

  type getCurrentOrderInfoUsingPOSTParams = {
    current?: number;
    pageSize?: number;
    sortField?: string;
    sortOrder?: string;
    status?: number;
    userId?: number;
  };

  type OrderItem = {
    asc?: boolean;
    column?: string;
  };

  type OrderSnVo = {
    charging?: number;
    createTime?: string;
    description?: string;
    expirationTime?: string;
    interfaceId?: number;
    name?: string;
    orderNum?: number;
    orderSn?: string;
    totalAmount?: number;
    userId?: number;
  };

  type PageApiOrderStatusVo_ = {
    countId?: string;
    current?: number;
    maxLimit?: number;
    optimizeCountSql?: boolean;
    orders?: OrderItem[];
    pages?: number;
    records?: ApiOrderStatusVo[];
    searchCount?: boolean;
    size?: number;
    total?: number;
  };
}
