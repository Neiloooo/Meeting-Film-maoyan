package com.stylefeng.guns.api.alipay.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 支付宝支付返回结果集
 */
@Data
public class AliPayInfoVO implements Serializable {


        private String orderId;
        private String QRCodeAddress;
}
