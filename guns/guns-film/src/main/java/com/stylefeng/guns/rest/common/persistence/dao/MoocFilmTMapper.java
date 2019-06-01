package com.stylefeng.guns.rest.common.persistence.dao;

import com.stylefeng.guns.api.film.vo.FilmDetailVO;
import com.stylefeng.guns.rest.common.persistence.model.MoocFilmT;
import com.baomidou.mybatisplus.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

/**
 * <p>
 * 影片主表 Mapper 接口
 * </p>
 *
 * @author liushuo
 * @since 2019-05-28
 */
//其实可以看出MybatisPLUS帮我们指定映射的DAO接口加实体类一套是它特有的,(dao,实体,Mapper.xml文件),这里提供了多种多样的单表增删改查
public interface MoocFilmTMapper extends BaseMapper<MoocFilmT> {

    //而如果我们想完全自定义多表Dao接口增删改查,需要自己重新写(实体,Dao接口,Mapper映射,以及sql1)
    //多表查询的话,需要我们自己定义接口并且写sql(就和正常MyBatis一样)
    //通过电影名查询电影详情的接口
    //使用@Param注解可以不用加parameterType,但是我们一般还是加上好
    FilmDetailVO getFilmDetailByName(@Param("filmName")String filmName);

    FilmDetailVO getFilmDetailById(@Param("uuid") String uuid);


}
