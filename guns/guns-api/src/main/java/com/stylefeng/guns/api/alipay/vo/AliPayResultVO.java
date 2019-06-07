package com.stylefeng.guns.api.alipay.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 阿里支付结果集最终返回对象
 */
@Data
public class AliPayResultVO implements Serializable {

    private String orderId;
    private Integer orderStatus;
    private String orderMsg;

}
