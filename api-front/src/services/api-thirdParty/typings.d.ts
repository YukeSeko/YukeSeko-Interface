declare namespace API {
  type AliPayDto = {
    alipayTraceNo?: string;
    subject?: string;
    totalAmount?: number;
    traceNo?: string;
  };

  type AlipayInfo = {
    buyerId?: string;
    buyerPayAmount?: number;
    gmtPayment?: string;
    orderSn?: string;
    subject?: string;
    totalAmount?: number;
    tradeNo?: string;
    tradeStatus?: string;
  };

  type BaseResponseAlipayInfo_ = {
    code?: number;
    data?: AlipayInfo;
    message?: string;
  };

  type queryTradeStatusUsingGETParams = {
    /** orderSn */
    orderSn?: string;
  };
}
