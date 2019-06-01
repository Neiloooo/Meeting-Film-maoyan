package com.stylefeng.guns.api.film.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 定义电影详情接口返回实体类
 */
@Data
public class FilmDetailVO implements Serializable {

    //首先是单纯的电影详情表里的数据
    private String filmId; //电影Id(主表和详情表都有)
    private String filmName; //电影名(主表没有,详情表有)
    private String filmEnName; //电影英文名(详情表)
    private String imgAddress;  //电影图片地址(详情表)
    private String score; //电影分数(详情表主表都有)
    private String scoreNum; //电影评分人数(详情表)
    private String totalBox; //电影总票房(电影主表)
    private String info01; //电影类型(电影主表外联cat表)
    private String info02; //电影来源拼接播放时长(主表和详情表拼接?)
    private String info03; //电影上映时间拼接上映地区 (主表和区域表?)
    private  InfoRequestVO info04; //info04的返回结果集封装在了这里,就拿这个当直接返回前端的对象
}
