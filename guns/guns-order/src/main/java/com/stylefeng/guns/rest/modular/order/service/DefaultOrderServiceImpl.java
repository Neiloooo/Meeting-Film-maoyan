package com.stylefeng.guns.rest.modular.order.service;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.plugins.Page;
import com.stylefeng.guns.api.cinema.CinemaServiceAPI;
import com.stylefeng.guns.api.cinema.vo.OrderQueryVO;
import com.stylefeng.guns.api.cinema.vo.cinemaInfo.FilmInfoVO;
import com.stylefeng.guns.api.order.orderServiceAPI;
import com.stylefeng.guns.api.order.vo.OrderVO;
import com.stylefeng.guns.core.util.UUIDUtil;
import com.stylefeng.guns.rest.common.persistence.dao.MoocOrderTMapper;
import com.stylefeng.guns.rest.common.persistence.model.MoocOrderT;
import com.stylefeng.guns.rest.common.util.FTPUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import sun.net.ftp.FtpClient;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@Service(interfaceClass = orderServiceAPI.class)
public class DefaultOrderServiceImpl implements orderServiceAPI {
    @Autowired
    private MoocOrderTMapper moocOrderTMapper;
    @Autowired
    private FTPUtil ftpUtil;
    @Reference(interfaceClass = CinemaServiceAPI.class,check = false)
    private CinemaServiceAPI cinemaServiceAPI;


    //1验证是否为真实的座位编号
    @Override
    public boolean isTrueSeats(Integer fieldId, String seats) {

        //1.根据FieldId
        String seatPath = moocOrderTMapper.getSeatsByFieldId(fieldId);
        //2.根据路径读取位置,判断是否存在前端传入的seats(这里选择去ftp存储地读)
        String fileStrByAddress = ftpUtil.getFileStrByAddress(seatPath);

        //3.将json字符串转换成json对象,我们才能获取里面的属性
        JSONObject jsonObject = JSONObject.parseObject(fileStrByAddress);
        //4.通过json对象的属性名获取其中数值,并且将其转成字符串
        //类似这样ids="1,3,4,5,6,99"
        String ids = jsonObject.get("ids").toString();
        //判断前端传来的字符串组seats=1,2,3与我们的ids对象中的数值:1,3,4,5,6,99能不能都对应上
        String[] seatArrs = seats.split(",");
        String[] idArrs = ids.split(",");
        //每一次匹配上都给isTrue+1,如果匹配上的数量与前端传来
        int isTrue = 0;
        for (String id : idArrs) {
            for (String seat : seatArrs) {
                if (seat.equalsIgnoreCase(id)) {
                    isTrue++;
                }
            }
        }
        //如果匹配上的数量与前端传来的购买座位数一致,那么就认为购买的座位,影厅总都有而不是奇葩的9999号座位
        if (seatArrs.length == isTrue) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 判断前端传过来的座位是否已经被卖掉
     *
     * @param fieldId
     * @param seats
     * @return
     */
    @Override
    public boolean isNotSoldSeats(Integer fieldId, String seats) {
        EntityWrapper<MoocOrderT> entityWrapper = new EntityWrapper<>();
        entityWrapper.eq("field_id", fieldId);
        //将前端与后端的座位数分别遍历并且进行挨个对比,如果有一个相等,直接返回false
        List<MoocOrderT> moocOrderTS = moocOrderTMapper.selectList(entityWrapper);
        String[] seatArrs = seats.split(",");
        for (MoocOrderT moocOrderT : moocOrderTS) {
            String[] ids = moocOrderT.getSeatsIds().split(",");
            for (String id : ids) {
                for (String seat : seatArrs) {
                    if (id.equalsIgnoreCase(seat)) {
                        return false;
                    }
                }
            }

        }
        return true;
    }


    /**
     * 创建新订单
     *
     * @param fieldId
     * @param seats
     * @param userId
     * @return
     */
    @Override
    public OrderVO saveOrderInfo(Integer fieldId, String seats, String seatsName, Integer userId) {

        //订单编号,一般不使用自增,使用UUID等随机数
        String uuid = UUIDUtil.getUuid();
        //影片信息
        FilmInfoVO filmInfoVO = cinemaServiceAPI.getFilmInfoByFieldId(fieldId);
        int filmId = Integer.parseInt(filmInfoVO.getFilmId());

        //获取影院信息
        OrderQueryVO orderQueryVO = cinemaServiceAPI.getOrderNeeds(fieldId);
        Integer cinemaId = Integer.parseInt(orderQueryVO.getCinemaId());
        double filmPrice = Double.parseDouble(orderQueryVO.getFilmPrice());

        //求订单总金额//1,2,3,4,5
        int solds = seats.split(",").length;
        double totalPrice = getTotalPrice(solds, filmPrice);

        MoocOrderT moocOrderT = new MoocOrderT();
        moocOrderT.setUuid(uuid);
        moocOrderT.setSeatsName(seatsName);
        moocOrderT.setSeatsIds(seats);
        moocOrderT.setOrderUser(userId);
        moocOrderT.setOrderPrice(totalPrice);
        moocOrderT.setFilmPrice(filmPrice);
        moocOrderT.setFilmId(filmId);
        moocOrderT.setFieldId(fieldId);
        moocOrderT.setCinemaId(cinemaId);

        Integer insert = moocOrderTMapper.insert(moocOrderT);
        if (insert > 0) {
            //返回查询结果
            OrderVO orderVO = moocOrderTMapper.getOrderInfoById(uuid);
            //如果返回回来的订单与订单编号有一个为null,那么就代表插入失败,返回null
            if (orderVO == null || orderVO.getOrderId() == null) {
                log.error("订单信息查询失败,,订单标号为{ }", uuid);
                return null;
            } else {
                //插入正常,返回,就是插入后再查询一遍,防止脏读
                return orderVO;
            }
        } else {
            log.error("订单插入失败");
            return null;
        }
    }


    //价钱不能直接使用*进行科学计数法,需要使用bigDecimal进行精确计算
    private static double getTotalPrice(int solds, double filmPrice) {
        BigDecimal soldsDeci = new BigDecimal(solds);
        BigDecimal filmPriceDeci = new BigDecimal(filmPrice);
        //使用bigDecimal的乘法进行计算
        BigDecimal result = soldsDeci.multiply(filmPriceDeci);
        //对乘积后的结果,四舍五入,取小数点后两位
        BigDecimal bigDecimal = result.setScale(2, RoundingMode.HALF_UP);
        //将bigDecimal转成double
        return bigDecimal.doubleValue();
    }


    @Override
    public Page<OrderVO> getOrderByUserId(Integer userId,Page<OrderVO> page) {
        Page<OrderVO> result = new Page<>();

        if (userId == null) {
            log.error("订单查询业务失败,用户编号未传入");
            return null;
        } else {
            List<OrderVO> orderInfoByUsers = moocOrderTMapper.getOrderInfoByUserId(userId, page);
            //如果查询出来的集合为空且为null
            if (orderInfoByUsers == null && orderInfoByUsers.size() == 0) {

                //返回一个空结果集
                result.setTotal(0);
                result.setRecords(new ArrayList<>());
                //返回空集合,就是告诉前端这个订单没有,你这订单编号有问题?
                return result;
            } else {
                //获取订单总数
                EntityWrapper<MoocOrderT> entityWrapper = new EntityWrapper<>();
                entityWrapper.eq("order_user",userId);
                //总条数需要我们自己查出来
                Integer counts = moocOrderTMapper.selectCount(entityWrapper);
                //将结果总条数与分页结果集放入Page对象中
                result.setTotal(counts);
                result.setRecords(orderInfoByUsers);
                //返回分页对象
                return result;
            }
        }
    }

    /**
     * 根据厅次获取所有已售座位
     *结果集要列转行
     * @param fieldId
     * @return
     */
    @Override
    public String getSoldSeatsByFieldId(Integer fieldId) {
        if (fieldId == null) {
            log.error("查询已售座位错误,未传入任何场次编号");
            return "";
        } else {
            String soldSeatsByFieldId = moocOrderTMapper.getSoldSeatsByFieldId(fieldId);
            return soldSeatsByFieldId;
        }
    }
    //++++++++++++++支付模块所需接口------------
    @Override
    public OrderVO getOrderInfoById(String orderId) {

        OrderVO orderInfoById = moocOrderTMapper.getOrderInfoById(orderId);

        return orderInfoById;
    }

    /**
     * 支付成功的方法
     * @param orderId
     * @return
     */
    @Override
    public boolean paySuccess(String orderId) {
        //修改订单表的status字段
        MoocOrderT moocOrderT = new MoocOrderT();
        //通过数据库直连对象,可以直接将对象传入参数,再将整个对象传入方法进行修改
        moocOrderT.setUuid(orderId);
        moocOrderT.setOrderStatus(1);


        //修改非整个表的字段用updateById
        //判断返回结果,查看是否修改成功
        Integer i = moocOrderTMapper.updateById(moocOrderT);
        if (i >= 1) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean payFail(String orderId) {
        //修改订单表的status字段
        MoocOrderT moocOrderT = new MoocOrderT();
        //通过数据库直连对象,可以直接将对象传入参数,再将整个对象传入方法进行修改
        moocOrderT.setUuid(orderId);
        moocOrderT.setOrderStatus(2);


        //修改非整个表的字段用updateById
        //判断返回结果,查看是否修改成功
        Integer i = moocOrderTMapper.updateById(moocOrderT);
        if (i >= 1) {
            return true;
        } else {
            return false;
        }
    }


}