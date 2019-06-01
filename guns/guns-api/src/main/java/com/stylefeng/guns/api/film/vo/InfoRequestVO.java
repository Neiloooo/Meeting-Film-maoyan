package com.stylefeng.guns.api.film.vo;

import com.stylefeng.guns.api.film.vo.ImgVO;
import lombok.Data;

/**
 * 与前端做交互的info4最终返回的结果集对象
 */
@Data
public class InfoRequestVO {
    //对应返回报文data:的info4结果集对象
    private String biography;
    private ActorRequestVO actors;
    private ImgVO imgVO;
    private String filmId;


}
