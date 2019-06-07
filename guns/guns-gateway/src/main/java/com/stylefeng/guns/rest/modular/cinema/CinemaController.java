package com.stylefeng.guns.rest.modular.cinema;


import com.alibaba.dubbo.config.annotation.Reference;
import com.baomidou.mybatisplus.plugins.Page;
import com.stylefeng.guns.api.cinema.CinemaServiceAPI;
import com.stylefeng.guns.api.cinema.vo.*;
import com.stylefeng.guns.api.cinema.vo.cinemaInfo.CinemaInfoVO;
import com.stylefeng.guns.api.cinema.vo.cinemaInfo.FilmInfoVO;
import com.stylefeng.guns.api.order.orderServiceAPI;
import com.stylefeng.guns.rest.modular.cinema.vo.CinemaConditionResponseVO;
import com.stylefeng.guns.rest.modular.cinema.vo.CinemaFieldResponseVO;
import com.stylefeng.guns.rest.modular.cinema.vo.CinemaFieldsResponseVO;
import com.stylefeng.guns.rest.modular.vo.ResponseVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/cinema/")
public class CinemaController {
    @Reference(interfaceClass = CinemaServiceAPI.class,connections = 10,check = false)
    private CinemaServiceAPI cinemaServiceAPI;

    @Reference(interfaceClass =orderServiceAPI.class,check = false)
    private orderServiceAPI orderServiceAPI;

    private static final String IMG_PRE="你的图片的前缀";
    /**
     * 根据组合条件分页查询所有影院
     *
     * @param cinemaQueryVO
     * @return
     */
    @GetMapping("getCinemas")
    public ResponseVO getCinemas(CinemaQueryVO cinemaQueryVO){
        try {
            //接口分析:
            //1.按照五个条件进行筛选,
            Page<CinemaVO> cinemas = cinemaServiceAPI.getCinemas(cinemaQueryVO);
            //2.判断查询出来的结果集是否为空
            if (cinemas.getRecords()==null||cinemas.getRecords().size()==0){
                return ResponseVO.serviceFail("没有影院可以查询");
            }else {
                return ResponseVO.success(cinemas.getCurrent(),(int)cinemas.getPages(),"",cinemas.getRecords());
            }
        } catch (Exception e) {
            //3.异常处理
            log.error("获取影院列表异常",e);
            return ResponseVO.serviceFail("查询影院列表失败");
        }


    }

    /**
     * 获取影院列表查询条件
     * 根据影院编号,影厅类型,行政区编号,多条件非必须组合查询影院列表
     * @param cinemaQueryVO
     * @return
     */
    @GetMapping("getCondition")
    public  ResponseVO getCondition(CinemaQueryVO cinemaQueryVO) {

        try {
            List<BrandVO> brands = cinemaServiceAPI.getBrands(cinemaQueryVO.getBrandId());
            List<AreaVO> areas = cinemaServiceAPI.getArea(cinemaQueryVO.getDistrictId());
            List<HallTypeVO> hallTypes = cinemaServiceAPI.getHallTypes(cinemaQueryVO.getHallType());

            CinemaConditionResponseVO cinemaConditionResponseVO = new CinemaConditionResponseVO();
            cinemaConditionResponseVO.setBrandList(brands);
            cinemaConditionResponseVO.setAreaList(areas);
            cinemaConditionResponseVO.setHalltypeList(hallTypes);

            return ResponseVO.success(cinemaConditionResponseVO);
        } catch (Exception e) {
            log.error("获取条件列表失败", e);
            return ResponseVO.serviceFail("获取影院查询条件失败");
        }
    }

    /**
     * 获取播放场次接口:
     * 根据影院编号查询
     * @param cinemaId
     * @return
     */
    @GetMapping("getFields")
    public ResponseVO getFilds(Integer cinemaId) {

        try {
            CinemaInfoVO cinemaInfoVOByCinemaId = cinemaServiceAPI.getCinemaInfoVOByCinemaId(cinemaId);
            List<FilmInfoVO> filmInfoVOByCinemaId = cinemaServiceAPI.getFilmInfoVOByCinemaId(cinemaId);



            CinemaFieldsResponseVO cinemaFieldResponseVO = new CinemaFieldsResponseVO();
            cinemaFieldResponseVO.setCinemaInfoVO(cinemaInfoVOByCinemaId);
            cinemaFieldResponseVO.setFilmList(filmInfoVOByCinemaId);

            return ResponseVO.success(IMG_PRE, cinemaFieldResponseVO);
        } catch (Exception e) {
            log.error("获取播放场次失败", e);
            return ResponseVO.serviceFail("获取播放场次失败");
        }

    }
    /**
     * 获取影院详情信息
     * 根据影院编号与场次编号查询影院的详情信息
     * @param cinemaId
     * @param fieldId
     * @return
     */
    @PostMapping("getFieldInfo")
    public ResponseVO getFieldInfo(Integer cinemaId,Integer fieldId){
        try {
            CinemaInfoVO cinemaInfoVOByCinemaId = cinemaServiceAPI.getCinemaInfoVOByCinemaId(cinemaId);
            FilmInfoVO filmInfoByFieldId = cinemaServiceAPI.getFilmInfoByFieldId(fieldId);
            HallInfoVO hallInfoVOByFieldId = cinemaServiceAPI.getHallInfoVOByFieldId(fieldId);

              //从订单接口获取销售的座位数据
            hallInfoVOByFieldId.setSoldSeats(orderServiceAPI.getSoldSeatsByFieldId(fieldId));


            CinemaFieldResponseVO cinemaFieldResponseVO = new CinemaFieldResponseVO();
            cinemaFieldResponseVO.setCinemaInfo(cinemaInfoVOByCinemaId);
            cinemaFieldResponseVO.setFilmInfo(filmInfoByFieldId);
            cinemaFieldResponseVO.setHallInfo(hallInfoVOByFieldId);


            return ResponseVO.success(IMG_PRE,cinemaFieldResponseVO);
        } catch (Exception e) {
            log.error("获取场次详细信息失败", e);
            return ResponseVO.serviceFail("获取场次详细信息失败");
        }
    }
}
