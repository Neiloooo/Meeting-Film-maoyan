package com.stylefeng.guns.rest.modular.cinema.vo;

import com.stylefeng.guns.api.cinema.vo.HallInfoVO;
import com.stylefeng.guns.api.cinema.vo.cinemaInfo.CinemaInfoVO;
import com.stylefeng.guns.api.cinema.vo.cinemaInfo.FilmInfoVO;
import lombok.Data;

/**
 * 4.	获取场次详细信息接口
 */
@Data
public class CinemaFieldResponseVO {

    private CinemaInfoVO cinemaInfo;
    private FilmInfoVO filmInfo;
    private HallInfoVO hallInfo;

}
