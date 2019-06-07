package com.stylefeng.guns.api.cinema.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 返回前端的影院实体
 * */
@Data
public class CinemaVO implements Serializable {

    private String uuid;
    private String cinemaName;
    private String address;
    private String minimumPrice;



}
