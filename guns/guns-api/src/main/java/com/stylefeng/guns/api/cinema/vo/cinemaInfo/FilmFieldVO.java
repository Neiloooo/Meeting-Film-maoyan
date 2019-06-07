package com.stylefeng.guns.api.cinema.vo.cinemaInfo;

import lombok.Data;

import java.io.Serializable;

/**
 * 接口三之,filmInfo下的FilmFields集合的中的FilmField对象
 * 放映场次的信息
 */
@Data
public class FilmFieldVO implements Serializable {

    private String fieldId;
    private String beginTime;
    private String endTime;
    private String language;
    private String hallName;
    private String price;
}
