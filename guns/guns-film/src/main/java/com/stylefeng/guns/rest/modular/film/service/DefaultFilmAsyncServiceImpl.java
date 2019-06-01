package com.stylefeng.guns.rest.modular.film.service;

import com.alibaba.dubbo.config.annotation.Service;
import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.plugins.Page;
import com.stylefeng.guns.api.film.FilmAsyncServiceApi;
import com.stylefeng.guns.api.film.FilmServiceApi;
import com.stylefeng.guns.api.film.vo.*;
import com.stylefeng.guns.core.util.DateUtil;
import com.stylefeng.guns.rest.common.persistence.dao.*;
import com.stylefeng.guns.rest.common.persistence.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@Service
public class DefaultFilmAsyncServiceImpl implements FilmAsyncServiceApi {

    //引入查询电影主表与电影演员表的Mapper
    @Autowired
    private MoocFilmInfoTMapper moocFilmInfoTMapper;
    @Autowired
    private MoocActorTMapper moocActorTMapper;

    //==============info4的内容:
    //我们从前面的info123,中查询出结果后,并且返回主键id,这里作为参数查询影片主表(数据库直连的)
    //根据电影id查询电影详情(表)实体的所有信息  info4中需要调用四个接口,怎样同时调用接口?--->dubbo的异步调用
    //因为下面每个方法都需要根据电影id获取电影详情对象的全部信息,所以这里就单独提出来了
    private MoocFilmInfoT getFilmInfo(String filmId){
        MoocFilmInfoT moocFilmInfoT = new MoocFilmInfoT();
        moocFilmInfoT.setFilmId(filmId);
        //selectone,是根据对象查询对象,类似于补全对象属性
        MoocFilmInfoT moocFilmInfo = moocFilmInfoTMapper.selectOne(moocFilmInfoT);
        return moocFilmInfo;
    }
    //获取电影详情信息:全都在电影详情表里,也就是电影详情实体里
    @Override
    public FilmDescVO getFilmDesc(String filmId) {
        MoocFilmInfoT filmInfo = getFilmInfo(filmId);

        FilmDescVO filmDescVO = new FilmDescVO();
        filmDescVO.setBiography(filmInfo.getBiography());
        //其实这里可以看出,,都是一个电影,,都是同一个id
        filmDescVO.setFilmId(filmId);
        return filmDescVO;
    }
    //电影图片信息的获取,也在电影详情表里,但是需要拆分一下图片地址,再分别放入图片实体中
    @Override
    public ImgVO getImgs(String filmId) {
        MoocFilmInfoT filmInfo = getFilmInfo(filmId);
        //从电影详情表汇总获取图片地址,前端要的是一张图片是一个参数,而数据库中存储的是以
        //逗号,为间隔存储在一个字段的图片
        //所以我们需要对取出来的字段以逗号为拆分,截取每张图片地址放进数组里
        String filmImgsStr = filmInfo.getFilmImgs();
        String[] filmImgs = filmImgsStr.split(",");
        //放入我们的imgvo实体中
        ImgVO imgVO = new ImgVO();
        imgVO.setManinImg(filmImgs[0]);
        imgVO.setImg01(filmImgs[1]);
        imgVO.setImg02(filmImgs[2]);
        imgVO.setImg03(filmImgs[3]);
        imgVO.setImg04(filmImgs[4]);
        return imgVO;
    }
    //导演信息的返回,需要在电影详情表表获取导演id,然后去导演表获取导演姓名与导演图片
    @Override
    public ActorVO getDectInfo(String filmId) {
        //获取电影详情表信息
        MoocFilmInfoT filmInfo = getFilmInfo(filmId);

        //获取导演编号
        Integer directorId = filmInfo.getDirectorId();
        //根据导演编号查询导演表的具体信息
        MoocActorT moocActorT = moocActorTMapper.selectById(directorId);
        //将信息封装到导演组合实体类中
        ActorVO actorVO = new ActorVO();
        actorVO.setImgAddress(moocActorT.getActorImg());
        actorVO.setDirectorName(moocActorT.getActorName());

        return actorVO;
    }
    //演员信息的返回(多表,自定义Mapper接口与sql)
    @Override
    public List<ActorVO> getActors(String filmId) {
        //Mapper接口中是多,实现类也是多的演员用演员类接受,List表示多
        List<ActorVO> actors = moocActorTMapper.getActors(filmId);
        return actors;
    }
}