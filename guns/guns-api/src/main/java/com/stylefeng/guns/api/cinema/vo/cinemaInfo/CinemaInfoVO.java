package com.stylefeng.guns.api.cinema.vo.cinemaInfo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 3、	获取播放场次接口
 *所需最外层实体:
 */
@Data
public class CinemaInfoVO implements Serializable {

    private String cinemaId;
    private String imgUrl;
    private String cinemaName;
    private String cinemaAdress;
    private String cinemaPhone;

}
