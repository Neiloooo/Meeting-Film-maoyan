package com.stylefeng.guns.api.film;

import com.stylefeng.guns.api.film.vo.*;

import java.util.List;

public interface FilmServiceApi {
    //1.获取banner
    List<BannerVO> getBanners();

    //接口3:影片分页查询接口(首页和普通页写在一起)
    //2.获取热映影片,
    //这个需要问前端究竟想要多少影片,第一个参数是判断是否有限制
    //第二个参数是限制多少个(默认按照受欢迎程度做排序)
    FilmVO getHotFilms(boolean isLimit,int nums,int nowPage,int sortId,
                       int yearId,int catId,int sourceId);
    //3.获取即将上映的影片(默认按照受欢迎程度做排序)
    FilmVO getSonnFilms(boolean isLimit, int nums, int nowPage,int sortId,
                        int yearId,int catId,int sourceId);
    //3.5.获取经典的影片
    FilmVO getClassicFilms(int nowPage, int nums, int sortId,
                        int yearId,int catId,int sourceId);



    //4.获取票房排行榜,简单的一对多结构
    List<FilmInfo> getBoxRanking();
    //5.获取人气排行榜
    List<FilmInfo> getExpectRanking();
    //6.获取人气排行top100
    List<FilmInfo> getTop();


//===========================================
    //接口2:
    // 获取影片条件接口:影片最上层的导航条
    //分类条件
    List<CatVO> getCat();
    //片源条件
    List<SourceVO> getSources();
    //获取年代条件
    List<YearVO> getYears();
    //============================================
    //接口4:
    //      根据影片id获取影片名称获取影片的全部详情(包含演员表信息2,图片地址)
    //  1.根据影片Id或者影片名称获取影片信息
        //参数是搜索类型(按照名字还是id搜索,url路径上的参数)
        FilmDetailVO getFilmDetail(int searchType,String searchParam);

    // 2.获取影片相关的其他信息(演员表,图片地址....)
        //根据id获取影片描述信息,这里的filmId是我们根据(id或姓名)查询影片基本信息(影片表和影片详情表)
        //封装返回给filmDetailVO实体的,我们从这里面取出来,当做条件继续查询
        //这样分层次查询的好处是(效率高,减少垃圾sql影响效率)
        FilmDescVO getFilmDesc(String filmId);
        //根据id获取影片的图片信息
        ImgVO getImgs(String filmId);

        //获取导演信息
        ActorVO getDectInfo(String filmId);
        //获取演员信息(一部电影有多个演员)
        List<ActorVO> getActors(String filmId);
}
