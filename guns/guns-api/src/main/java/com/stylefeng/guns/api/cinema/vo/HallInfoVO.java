package com.stylefeng.guns.api.cinema.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 接口四:获取场次详细信息
 * 的hallInfo对象
 */
@Data
public class HallInfoVO implements Serializable {

    private String hallFieldId;
    private String hallName;
    private String price;
    //存的是具体的座位信息
    private String seatFile;
    private String soldSeats;

}
