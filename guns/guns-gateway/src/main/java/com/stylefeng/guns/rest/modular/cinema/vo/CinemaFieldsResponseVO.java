package com.stylefeng.guns.rest.modular.cinema.vo;

import com.stylefeng.guns.api.cinema.vo.cinemaInfo.CinemaInfoVO;
import com.stylefeng.guns.api.cinema.vo.cinemaInfo.FilmInfoVO;
import lombok.Data;

import java.util.List;

/**
 * 3.获取播放场次接口的最终返回结果集
 */
@Data
public class CinemaFieldsResponseVO {
    private CinemaInfoVO cinemaInfoVO;
    private List<FilmInfoVO> filmList;
}
