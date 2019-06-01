package com.stylefeng.guns.api.film.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 根据id或name查影片详情的info4参数:
 *                               imgs对象
 *
 */
@Data
public class ImgVO implements Serializable {

    private String maninImg;
    private String img01;
    private String img02;
    private String img03;
    private String img04;
}
