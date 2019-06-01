package com.stylefeng.guns.api.film.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 这些实体是用来有与数据库交互的,需要进行远程传递,所以需要序列化
 * 影片列表表?
 */
@Data
public class FilmVO implements Serializable {
    //影片的的编号
    private int filmNum;
    //影片详情的集合
    private List<FilmInfo> filmInfo;
    //分页列表所需对象
    private int totalPage;
    private int nowPage;

}
