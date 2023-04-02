package com.wzy.thirdParty.model.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class AliPayDto implements Serializable {
    private String traceNo;
    private double totalAmount;
    private String subject;
    private String alipayTraceNo;
}

