package com.stylefeng.guns.rest.common.persistence.dao;

import com.stylefeng.guns.api.film.vo.ActorVO;
import com.stylefeng.guns.rest.common.persistence.model.MoocActorT;
import com.baomidou.mybatisplus.mapper.BaseMapper;import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 演员表 Mapper 接口
 * </p>
 *
 * @author liushuo
 * @since 2019-05-28
 */
public interface MoocActorTMapper extends BaseMapper<MoocActorT> {

    //自定义关联查询,根据电影id,查询演员,一对多,且有中间表,主要是这里的LIST体现多的一面
    //一个电影id会查出多个演员对象,我们只需要在接口层用List<演员>接受就可以了,Mybatis会自动
    //帮我们封装进List集合汇总
    List<ActorVO> getActors(@Param("filmId")String filmId);

}
