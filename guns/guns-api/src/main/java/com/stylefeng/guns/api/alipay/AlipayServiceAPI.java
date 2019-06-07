package com.stylefeng.guns.api.alipay;

import com.stylefeng.guns.api.alipay.vo.AliPayInfoVO;
import com.stylefeng.guns.api.alipay.vo.AliPayResultVO;

/**
 * 支付接口
 */
public interface AlipayServiceAPI {

    /**
     * 获取支付二位码接口:
     * 主要还是调用阿里巴巴的工具类
     * @param orderId
     * @return
     */
    AliPayInfoVO getQRCode(String orderId);

    /**
     * 获取支付结果:
     * 这里只需要订单id,如果重试4次,直接在消费方返回错误(Hystrix进行服务降级)
     * @param orderId
     * @return
     */
    AliPayResultVO getOrderStatus(String orderId);

}
