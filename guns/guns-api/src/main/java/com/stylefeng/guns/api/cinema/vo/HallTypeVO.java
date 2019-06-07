package com.stylefeng.guns.api.cinema.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 接口二之获取影院列表查询条件
 * hallType接口:
 */
@Data
public class HallTypeVO implements Serializable {

    private String halltypeId;
    private String halltypeName;
    private boolean isActive;
}
