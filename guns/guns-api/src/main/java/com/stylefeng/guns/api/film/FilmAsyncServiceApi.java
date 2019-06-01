package com.stylefeng.guns.api.film;

import com.stylefeng.guns.api.film.vo.*;

import java.util.List;

/**
 * duboo的异步调用专用接口,且只有根据id查询详情,图片,导演,演员,这四个接口是需要异步调用的
 *也可以理解为同时调用这四个接口,只需要等待这里面最长的时间就好,不用相加执行时间
 */
public interface FilmAsyncServiceApi {


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
