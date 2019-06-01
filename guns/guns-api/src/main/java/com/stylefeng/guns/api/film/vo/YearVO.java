package com.stylefeng.guns.api.film.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 返回前端的年限对象
 */
@Data
public class YearVO implements Serializable {

    private String yearId;
    private String yearName;
    private  boolean isActive;
}
