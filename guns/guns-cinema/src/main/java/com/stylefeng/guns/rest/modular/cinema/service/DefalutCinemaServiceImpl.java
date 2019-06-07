package com.stylefeng.guns.rest.modular.cinema.service;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.config.annotation.Service;
import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.plugins.Page;
import com.stylefeng.guns.api.cinema.CinemaServiceAPI;
import com.stylefeng.guns.api.cinema.vo.*;
import com.stylefeng.guns.api.cinema.vo.cinemaInfo.CinemaInfoVO;
import com.stylefeng.guns.api.cinema.vo.cinemaInfo.FilmInfoVO;
import com.stylefeng.guns.rest.common.persistence.dao.*;
import com.stylefeng.guns.rest.common.persistence.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
//dubbo的service注解中executes表示对线程的控制executes=10表示只能同时启用10个线程
@Service(interfaceClass = CinemaServiceAPI.class,executes = 10)
public class DefalutCinemaServiceImpl implements CinemaServiceAPI {
    //引入接口所需Mapper
    @Autowired
    private MoocCinemaTMapper moocCinemaTMapper;
    @Autowired
    private MoocAreaDictTMapper moocAreaDictTMapper;
    @Autowired
    private MoocBrandDictTMapper moocBrandDictTMapper;
    @Autowired
    private MoocHallDictTMapper moocHallDictTMapper;
    @Autowired
    private MoocHallFilmInfoTMapper moocHallFilmInfoTMapper;
    @Autowired
    private MoocFieldTMapper moocFieldTMapper;


    //1.根据cinemaVO分页查询影院列表(多条件组合查询单表)
    //page对象中已经帮我们封装好好了总页数,我们只要传入当前页与一页多少个就行
    //和pageHelper其实本质上差不多,其实这里有个细节,你都page了肯定是多个
    //所以分页查询的话用Page对象接收就好了
    @Override
   public Page<CinemaVO> getCinemas(CinemaQueryVO cinemaQueryVO){

       //业务实体集合(用于拼接返回报文的实体)
        List<CinemaVO> cinemas = new ArrayList<>();
        //创建分页对象,根据分页条件创建分页对象Page<被分页的对象,或者说表>
        Page<MoocCinemaT> page = new Page<>(cinemaQueryVO.getNowPage(), cinemaQueryVO.getPageSize());


        //判断是否传入查询条件-->bradnId,distId,halltype,是否=99,如果等于99就是没有传入参数
        EntityWrapper<MoocCinemaT> entityWrapper = new EntityWrapper<>();
        //如果查询条件中BrandId不等于99,也就是用户传入了参数,我们将参数作为条件赋值给条件对象
        if (cinemaQueryVO.getBrandId() !=99){
            //相当于where brand_id=#{cinemaQueryVO.getBrandId()}
            entityWrapper.eq("brand_id",cinemaQueryVO.getBrandId());
        }
        if (cinemaQueryVO.getDistrictId() !=99){
            entityWrapper.eq("area_id",cinemaQueryVO.getDistrictId());
        }
        //一个影院可以有多个影厅类型,比如2d,3dmx,啥的,所以数据库中存储的形式是#1#3#5#6#
        //前端传过来是1,或者3,或者5,这种数字,所以我们需要进行模糊查询,且需要为其拼接上#,这样用户输入1就能查出来有1类型影厅的影院了
        if (cinemaQueryVO.getHallType() !=99){
            //模糊查询like
            entityWrapper.like("hall_ids","%#"+cinemaQueryVO.getHallType()+"#%");
        }
        //调用mybatisplus提供的mapper接口进行分页查询
        //传入page对象与entityWrapper条件对象
        List<MoocCinemaT> moocCinemaTS = moocCinemaTMapper.selectPage(page, entityWrapper);
        //将数据实体转换为业务实体,Mybatisplus直连对象转换成返回前端报文对象
        for (MoocCinemaT moocCinemaT:moocCinemaTS) {
            CinemaVO cinemaVO = new CinemaVO();
            //注意数据库对应的实体有些字段的数据类型为Intger,我们这里需要转成String类型,+" "就行
            cinemaVO.setUuid(moocCinemaT.getUuid() + "");
            cinemaVO.setMinimumPrice(moocCinemaT.getMinimumPrice() + "");
            cinemaVO.setCinemaName(moocCinemaT.getCinemaName());
            cinemaVO.setAddress(moocCinemaT.getCinemaAddress());

            //将转换的对象添加到cinemas集合里,毕竟是前端要的是集合
            cinemas.add(cinemaVO);
        }

        //现在我们使用MybatisPLUS的Page对象获取关于分页的所有数据,有点像PageHelper的pageInfo对象
        //1.根据条件获取影院列表总数
        long counts = moocCinemaTMapper.selectCount(entityWrapper);
        //2.创建Page对象,传入参数,返回对象
        Page<CinemaVO> reuslt = new Page<>();
        //①这是我们进行分页查询返回的全部分页结果集
        reuslt.setRecords(cinemas);
        //②这是前端传来的,要求一页有多少个
        reuslt.setSize(cinemaQueryVO.getPageSize());
        //③这是我们刚刚查出来的总共有多少条
        reuslt.setTotal(counts);
        //返回分页结果集对象
        return  reuslt;
    }


    //2.根据条件获取品牌列表,后端给前端设置哪项是能用的,如果前端没传参数,则将99置为true,也就是默认全部
    //如果前端传入了参数,就给那个参数的active置为true,标识用户选了这个
    //实现思路,根据标识符flag影响遍历后的对象,到底给哪个置true,再封装回我们的对象
    @Override
    public List<BrandVO> getBrands(int bradnId){

        List<BrandVO> brandVOs = new ArrayList<>();

        boolean flag= false;
        //判断用户是否传入id
        //首先根据id查询实体
        MoocBrandDictT moocBrandDictT = moocBrandDictTMapper.selectById(bradnId);
        //1.用户没传入参数,2.用户传入参数为99,3.用户想要查询的品牌没有数据的时候
        if (bradnId ==99|| moocBrandDictT==null|| moocBrandDictT.getUuid()==null){
           //前端都将99置为true
            flag=true;
        }
        //查询所有列表,将数据库中所有品牌都查询出来
        List<MoocBrandDictT> moocBrandDictTS = moocBrandDictTMapper.selectList(null);
        //判断flag如果为true,则将99置为true
        for (MoocBrandDictT brand:moocBrandDictTS){
            BrandVO brandVO = new BrandVO();
            brandVO.setBrandId(brand.getUuid()+"");
            brandVO.setBradnName(brand.getShowName());
            //判断如果falg为true,将99置为true,如果为false,则将其匹配的内容置为true
            if (flag){
                if (brand.getUuid()==99){
                    brandVO.setActive(true);
                }
            }else {
                if (brand.getUuid()==bradnId){
                    brandVO.setActive(true);
                }
            }
            brandVOs.add(brandVO);
        }
        return brandVOs;
   }



   //3.获取行政区域列表
   @Override
   public  List<AreaVO> getArea(int areaId){
       boolean flag = false;
       List<AreaVO> areaVOS = new ArrayList<>();
       // 判断brandId是否存在
       MoocAreaDictT moocAreaDictT = moocAreaDictTMapper.selectById(areaId);
       // 判断brandId 是否等于 99
       if(areaId == 99 || moocAreaDictT==null || moocAreaDictT.getUuid() == null){
           flag = true;
       }
       // 查询所有列表
       List<MoocAreaDictT> moocAreaDictTS = moocAreaDictTMapper.selectList(null);
       // 判断flag如果为true，则将99置为isActive
       for(MoocAreaDictT area : moocAreaDictTS){
           AreaVO areaVO = new AreaVO();
           areaVO.setAreaName(area.getShowName());
           areaVO.setAreaId(area.getUuid()+"");
           // 如果flag为true，则需要99，如为false，则匹配上的内容为active
           if(flag){
               if(area.getUuid() == 99){
                   areaVO.setActive(true);
               }
           }else{
               if(area.getUuid() == areaId){
                   areaVO.setActive(true);
               }
           }

           areaVOS.add(areaVO);
       }

       return areaVOS;
   }

    //4.获取影院类型列表
    @Override
    public  List<HallTypeVO> getHallTypes(int hallType){
        boolean flag = false;
        List<HallTypeVO> hallTypeVOS = new ArrayList<>();
        // 判断brandId是否存在
        MoocHallDictT moocHallDictT = moocHallDictTMapper.selectById(hallType);
        // 判断brandId 是否等于 99
        if(hallType == 99 || moocHallDictT==null || moocHallDictT.getUuid() == null){
            flag = true;
        }
        // 查询所有列表
        List<MoocHallDictT> moocHallDictTS = moocHallDictTMapper.selectList(null);
        // 判断flag如果为true，则将99置为isActive
        for(MoocHallDictT hall : moocHallDictTS){
            HallTypeVO hallTypeVO = new HallTypeVO();
            hallTypeVO.setHalltypeName(hall.getShowName());
            hallTypeVO.setHalltypeId(hall.getUuid()+"");
            // 如果flag为true，则需要99，如为false，则匹配上的内容为active
            if(flag){
                if(hall.getUuid() == 99){
                    hallTypeVO.setActive(true);
                }
            }else{
                if(hall.getUuid() == hallType){
                    hallTypeVO.setActive(true);
                }
            }

            hallTypeVOS.add(hallTypeVO);
        }

        return hallTypeVOS;
    }

    //5.根据影院编号获取影院信息
    @Override
    public CinemaInfoVO getCinemaInfoVOByCinemaId(int cinemaId){
        //单纯的单表查询
        MoocCinemaT moocCinemaT = moocCinemaTMapper.selectById(cinemaId);
        //将数据实体转换成业务实体
        //如果用户没有输入影院编号,返回空的影院信息
        if(moocCinemaT == null){
            return new CinemaInfoVO();
        }
        CinemaInfoVO cinemaInfoVO = new CinemaInfoVO();
        cinemaInfoVO.setImgUrl(moocCinemaT.getImgAddress());
        cinemaInfoVO.setCinemaPhone(moocCinemaT.getCinemaPhone());
        cinemaInfoVO.setCinemaName(moocCinemaT.getCinemaName());
        cinemaInfoVO.setCinemaId(moocCinemaT.getUuid()+"");
        cinemaInfoVO.setCinemaId(moocCinemaT.getCinemaAddress());

        return cinemaInfoVO;
    }

    //6.获取所有电影的信息和对应的放映场次信息,根据影院编号
    //一个电影可能有多个场次播放,一对多,需要自定义Collection映射
    @Override
    public List<FilmInfoVO> getFilmInfoVOByCinemaId(int cinemaId){

        List<FilmInfoVO> filmInfos = moocFieldTMapper.getFilmInfos(cinemaId);
        return filmInfos;
    }

    //7.根据影院编号获取影院信息和接口5一样,所以忽略

    //7.根据放映场次ID获取放映信息
    @Override
    public HallInfoVO getHallInfoVOByFieldId(int fieldId){
        HallInfoVO hallInfo = moocFieldTMapper.getHallInfo(fieldId);
        return hallInfo;
    }
    //8.根据放映场次查询播放的电影编号,然后根据电影编号获取对应的电影信息
    @Override
    public FilmInfoVO getFilmInfoByFieldId(int fieldId){
        FilmInfoVO filmInfoById = moocFieldTMapper.getFilmInfoById(fieldId);
        return filmInfoById;
    }

    @Override
    public OrderQueryVO getOrderNeeds(int fieldId) {

        OrderQueryVO orderQueryVO = new OrderQueryVO();
        MoocFieldT moocFieldT = moocFieldTMapper.selectById(fieldId);
        orderQueryVO.setCinemaId(moocFieldT.getCinemaId()+"");
        orderQueryVO.setFilmPrice(moocFieldT.getPrice()+"");

        return orderQueryVO;
    }


}
