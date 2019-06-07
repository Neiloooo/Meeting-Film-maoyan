package com.stylefeng.guns.api.cinema;


import com.baomidou.mybatisplus.plugins.Page;
import com.stylefeng.guns.api.cinema.vo.*;
import com.stylefeng.guns.api.cinema.vo.cinemaInfo.CinemaInfoVO;
import com.stylefeng.guns.api.cinema.vo.cinemaInfo.FilmFieldVO;
import com.stylefeng.guns.api.cinema.vo.cinemaInfo.FilmInfoVO;

import java.util.List;

public interface CinemaServiceAPI {

    //1.根据cinemaVO分页查询影院列表(多条件组合查询)
    //page对象中已经帮我们封装好好了总页数,我们只要传入当前页与一页多少个就行
    //和pageHelper其实本质上差不多,其实这里有个细节,你都page了肯定是多个
    //所以分页查询的话用Page对象接收就好了
    Page<CinemaVO> getCinemas(CinemaQueryVO cinemaQueryVO);

    //2.根据条件获取品牌列表[除了数字99意外,其他的数字为isActive]
    List<BrandVO> getBrands(int bradnId);
    //3.获取行政区域列表
    List<AreaVO> getArea(int areaId);
    //4.获取影院类型列表
    List<HallTypeVO> getHallTypes(int hallType);

    //5.根据影院编号获取影院信息
    CinemaInfoVO getCinemaInfoVOByCinemaId(int cinemaId);
    //6.获取所有电影的信息和对应的放映场次信息,根据影院编号
    List<FilmInfoVO> getFilmInfoVOByCinemaId(int cinemaId);

    //7.根据影院编号获取影院信息和接口5一样,所以忽略

    //7.根据放映场次ID获取放映信息
    HallInfoVO getHallInfoVOByFieldId(int fieldId);
    //8.根据放映场次查询播放的电影编号,然后根据电影编号获取对应的电影信息
    FilmInfoVO getFilmInfoByFieldId(int fieldId);

    /**
     * 订单模块所需内容
     * @param fieldId
     * @return
     */
    public OrderQueryVO getOrderNeeds(int fieldId);
}
