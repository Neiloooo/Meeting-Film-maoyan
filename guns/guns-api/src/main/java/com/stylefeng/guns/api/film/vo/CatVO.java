package com.stylefeng.guns.api.film.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 这里是根据条件条件查询的分类实体,对应前端catInfo接口中的一个一个对象
 */
@Data
public class CatVO implements Serializable {

    private String catId;
    private String catName;
    private  boolean isActive;


}
