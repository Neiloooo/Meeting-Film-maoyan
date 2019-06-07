package com.stylefeng.guns.api.cinema.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 接口2查询影院列表条件接口:
 * area实体类:
 */
@Data
public class AreaVO implements Serializable {

        private String areaId;
        private String areaName;
        private  boolean isActive;

}
