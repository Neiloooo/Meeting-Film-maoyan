package com.stylefeng.guns.api.cinema.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 影院接口2之查询影院列表条件接口:
 * 品牌实体类
 */
@Data
public class BrandVO implements Serializable {

    private String brandId;
    private String bradnName;
    private boolean isActive;
}
