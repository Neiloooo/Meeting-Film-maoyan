package com.stylefeng.guns.api.film.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 这些实体是用来有与数据库交互的
 * banner实体类对应banner表
 */
@Data
public class BannerVO implements Serializable {
    //stirng类型的id
    private String bannerId;
    private String bannerAddress;
    private String bannerUrl;
}
