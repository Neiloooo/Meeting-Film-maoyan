package com.stylefeng.guns.rest.modular.order;


import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.rpc.RpcContext;
import com.baomidou.mybatisplus.plugins.Page;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixProperty;
import com.stylefeng.guns.api.alipay.AlipayServiceAPI;
import com.stylefeng.guns.api.alipay.vo.AliPayInfoVO;
import com.stylefeng.guns.api.alipay.vo.AliPayResultVO;
import com.stylefeng.guns.api.order.orderServiceAPI;
import com.stylefeng.guns.api.order.vo.OrderVO;
import com.stylefeng.guns.core.util.ToolUtil;
import com.stylefeng.guns.rest.common.CurrentUser;
import com.stylefeng.guns.rest.modular.vo.ResponseVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@Slf4j
@RestController
@RequestMapping("/order/")
public class OrderController {

    private static final String IMG_PRE="这里是二维码的所在地址的前缀,公司域名";

    @Reference(interfaceClass = orderServiceAPI.class,check = false)
    private orderServiceAPI orderServiceAPI;

    @Reference(interfaceClass = AlipayServiceAPI.class,check = false)
    private AlipayServiceAPI alipayServiceAPI;

    //定义Hystrix的核心fallbackMethod,也就是返回方法
    //要求返回值和类型必须和注解的方法完全一致,服务降级
    public ResponseVO error(
            Integer fieldId,String soldSeats,String seatsName
    ){
        return ResponseVO.serviceFail("抱歉,下单的人太多了,请稍后重置");
    }


    /**
     * 1、	用户下单购票接口
     * 注意如果使用Hystrix提供服务降级功能需要自定义fallbackMethod方法
     * @return
     */
    @HystrixCommand(fallbackMethod = "error", commandProperties = {
            @HystrixProperty(name="execution.isolation.strategy", value = "THREAD"),
            @HystrixProperty(name = "execution.isolation.thread.timeoutInMilliseconds", value = "4000"),
            @HystrixProperty(name = "circuitBreaker.requestVolumeThreshold", value = "10"),
            @HystrixProperty(name = "circuitBreaker.errorThresholdPercentage", value = "50")},
                    threadPoolProperties = {
                    @HystrixProperty(name = "coreSize", value = "1"),
                    @HystrixProperty(name = "maxQueueSize", value = "10"),
                    @HystrixProperty(name = "keepAliveTimeMinutes", value = "1000"),
                    @HystrixProperty(name = "queueSizeRejectionThreshold", value = "8"),
                    @HystrixProperty(name = "metrics.rollingStats.numBuckets", value = "12"),
                    @HystrixProperty(name = "metrics.rollingStats.timeInMilliseconds", value = "1500")
            })
    @PostMapping("buyTickets")
    public ResponseVO buyTicket(
            Integer fieldId,String soldSeats,String seatsName
    ) {

        try {
            //1.验证售出的票是否为真
            boolean isTrue = orderServiceAPI.isTrueSeats(fieldId, soldSeats);
            //2.验证已经销售的座位,有没有这些作为(验证前端是否卖出了已经售出座位)
            boolean isnotSoldSeats = orderServiceAPI.isNotSoldSeats(fieldId, soldSeats);
            //验证上述两个内容有一个不为真,就不创建订单
            if (isnotSoldSeats&&isTrue) {

                //3.如果前个步骤验证成功了,就创建订单信息,注意获取登录人
                String userId = CurrentUser.getCurrentUser();
                //如果用户登录了
                if (userId != null && userId.trim().length() >0) {

                    Integer userId1 = Integer.parseInt(userId);
                    OrderVO orderVO = orderServiceAPI.saveOrderInfo(fieldId, soldSeats, seatsName, userId1);

                    if (orderVO==null){
                        log.error("购票业务异常");
                        return ResponseVO.serviceFail("购票业务异常");
                    }else {
                        //成功返回最终实体
                        return ResponseVO.success(orderVO);
                    }
                }else {
                    return ResponseVO.serviceFail("用户未登录");
                }
                //如果其中有一个不为真,就报错返回信息
            }else {
                log.error("用户输入订单信息异常");
                return ResponseVO.serviceFail("用户输入订单信息异常");
            }
        } catch (Exception e) {
            log.error("购票业务异常",e);
            return  ResponseVO.serviceFail("购票业务异常");
        }

    }



    /**
     * 2.用户获取订单详情的接口
     * @return
     */
     @PostMapping("getOrderInfo")
     public  ResponseVO getOrderInfo(
             //非必填的话,我们可以通过@RequestParam进行控制
             @RequestParam(name = "nowPage",required = false,defaultValue ="1" ) Integer nowPage,
             @RequestParam(name="pageSize",required = false,defaultValue = "5") Integer pageSize
     ) {
         //获取当前登录人的信息
         String userId = CurrentUser.getCurrentUser();

         //使用当前登录人的信息获取(查询)已经购买的订单
         Page<OrderVO> page = new Page<>(nowPage, pageSize);
         //判断前端传入的用户id不为null,不为0
         if (userId != null && userId.trim().length() > 0) {
             Page<OrderVO> result = orderServiceAPI.getOrderByUserId(Integer.parseInt(userId), page);
             //返回前端需要的分页后的结果集,以及其他分页信息
             return ResponseVO.success(nowPage, (int) result.getPages(), "", result.getRecords());
         } else {
             return ResponseVO.serviceFail("用户未登录");
         }
     }

    /**
     * 根据订单号获取二维码
     * 订单号必须传
     * @param orderId
     * @return
     */
     @PostMapping("getPayInfo")
     public ResponseVO getPayInfo(@RequestParam("orderId") String orderId){

      //获取当前登录人信息从JWT存入到的localStroage中
         String userId = CurrentUser.getCurrentUser();
      //判断用户id是否为空
        if (StringUtils.isAllEmpty(userId)){
            return ResponseVO.serviceFail("用户请登录");
        }
      //根据订单id获取二维码的返回结果
         AliPayInfoVO qrCode = alipayServiceAPI.getQRCode(orderId);
         return ResponseVO.success(IMG_PRE,qrCode);
     }

    /**
     * 根据订单ID和重试支付次数,返回支付结果(成功就是订单结果,失败就是支付失败)
     * @param orderId
     * @param tryNums
     * @return
     */
        @PostMapping("getPayResult")
        public ResponseVO  getPayResult (@RequestParam("orderId") String orderId,
                                        @RequestParam(name = "tryNums",required = false,defaultValue = "1") Integer tryNums
                                ){

            //获取当前登录人信息从JWT存入到的threadLocal中
            String userId = CurrentUser.getCurrentUser();
            if (StringUtils.isEmpty(userId)){
                return ResponseVO.success("抱歉您未登录");
            }

            //仅仅通过OrderId查询订单是不安全的,还需要用户ID,一起,让用户只能查询自己的订单
            //将当前登录人的信息传递到后端(服务方),通过隐式传参的方法
            RpcContext.getContext().setAttachment("userId",userId);



            //支付宝端发我们的信息,告诉我们这个订单的id和重试次数,如果重试大于4就返回用户这个订单支付失败
            if (tryNums>=4){
                return ResponseVO.serviceFail("订单支付失败,请稍后重试");
            }else {
                //重试次数没有大于4,我们去调用支付宝接口查询是否被扫码了
                //并且根据订单Id得到结果
                AliPayResultVO aliPayResultVO = alipayServiceAPI.getOrderStatus(orderId);
                //如果返回的对象为null或者订单编号为空(就是没有输入订单编号的情况下查支付状态),就是用户支付不成功
                if(aliPayResultVO==null|| StringUtils.isEmpty(aliPayResultVO.getOrderId())){
                    AliPayResultVO serviceFailVO = new AliPayResultVO();
                    serviceFailVO.setOrderId(orderId);
                    serviceFailVO.setOrderMsg("支付不成功");
                    serviceFailVO.setOrderStatus(0);
                    //告诉前端支付没成功,而且支付状态是0
                    return ResponseVO.success(serviceFailVO);
                }
                //否则的话就是支付成功了,返回前端索要数据就行
                return ResponseVO.success(aliPayResultVO);

            }
        }













}
