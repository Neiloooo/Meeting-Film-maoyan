package com.stylefeng.guns.api.order;

import com.baomidou.mybatisplus.plugins.Page;
import com.stylefeng.guns.api.order.vo.OrderVO;

import java.util.List;

public interface orderServiceAPI {

    //1.验证售出的票是否为真(根据影厅id与座位号,判断数据库的座位字段中是否存在这么个作为)
    boolean isTrueSeats(Integer fieldId,String seats);
    //2.已经销售的作为里,有没有这些作为(判断那个座位是不是已经被卖了)
    boolean isNotSoldSeats(Integer fieldId,String seats);
    //3.创建订单信息,前两个都为true,才能创建订单信息
    OrderVO saveOrderInfo(Integer fieldId,String seats,String seatsName,Integer userId);


    //4.使用当前登录人的信息获取已经购买的订单
   Page<OrderVO> getOrderByUserId(Integer userId, Page<OrderVO> page);
    //5.根据FieldId获取所有已经销售的作为编号
    String getSoldSeatsByFieldId(Integer fieldId);

    //================支付模块接口==========================
    //1,根据订单id获取订单信息
    OrderVO getOrderInfoById(String orderId);
    //2.根据订单id修改订单状态(参数为:订单,需要修改成什么状态)
 //这种是正常写法   boolean  updateOrderStatus(String orderId,Integer orderStatus);
    //隐蔽写法:定义成功与失败方法,供前端调用,这样外人就不知道订单状态是什么了,假如有人拦截了你们与前端之间的信息的话
    boolean paySuccess(String orderId);
    boolean payFail(String orderId);

}
