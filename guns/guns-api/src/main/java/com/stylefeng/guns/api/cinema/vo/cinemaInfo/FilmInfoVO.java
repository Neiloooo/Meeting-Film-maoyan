package com.stylefeng.guns.api.cinema.vo.cinemaInfo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * cinemaInfo下的filmInfo对象集
 */
@Data
public class FilmInfoVO implements Serializable {

    private String filmId;
    private String filmName;
    private String filmLength;
    private String filmType;
    private String filmCats;
    private String actors;
    private String imgAddress;
    private List<FilmFieldVO> filmFields;
}
