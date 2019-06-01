package com.stylefeng.guns.rest.modular.film.vo;

import com.stylefeng.guns.api.film.vo.BannerVO;
import com.stylefeng.guns.api.film.vo.FilmInfo;
import com.stylefeng.guns.api.film.vo.FilmVO;
import lombok.Data;


import java.util.List;

/**
 * 首页的实体类,这里封装了所有和首页信息有关的信息
 * 这个实体是用于最后返还给前端指定的格式的最终实体
 */
@Data
public class FilmIndexVO {
    //对应首页查询的多个banner图
    //单纯一对多,一个首页俩图
    private List<BannerVO> banners;
    //返还前端正在热映电影信息的的实体
    //一对一对多,一个热映榜对应一个编号,对应多个电影
    private FilmVO hotFilms;
    //还前端即将上映电影信息的实体
    private  FilmVO soonFilms;
    //返还前端票房排行榜信息的实体
    //单纯的一对多,一个排行榜有多个详细的电影信息
    private List<FilmInfo> boxRanking;
    //返还前端受欢迎的榜单的实体
    private List<FilmInfo> expectRanking;
    //返还端top100的信息
    private List<FilmInfo> top100;
}
