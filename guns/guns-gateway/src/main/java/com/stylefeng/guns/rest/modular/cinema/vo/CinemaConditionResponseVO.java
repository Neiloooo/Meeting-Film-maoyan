package com.stylefeng.guns.rest.modular.cinema.vo;

import com.stylefeng.guns.api.cinema.vo.AreaVO;
import com.stylefeng.guns.api.cinema.vo.BrandVO;
import com.stylefeng.guns.api.cinema.vo.HallTypeVO;
import lombok.Data;

import java.util.List;

/**
 * 2、获取影院列表查询条件接口最终返回值
 */
@Data
public class CinemaConditionResponseVO {
        private List<BrandVO> brandList;
        private List<AreaVO> areaList;
        private List<HallTypeVO> halltypeList;

}
