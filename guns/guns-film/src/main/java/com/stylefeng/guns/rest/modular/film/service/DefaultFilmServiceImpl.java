package com.stylefeng.guns.rest.modular.film.service;

import com.alibaba.dubbo.config.annotation.Service;
import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.plugins.Page;
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
public class DefaultFilmServiceImpl implements FilmServiceApi {
    //引入首页的条件分页查询的Mapper接口
    @Autowired
    private MoocBannerTMapper moocBannerTMapper;
    @Autowired
    private MoocFilmTMapper moocFilmTMapper;
    //======引入条件列表查询的的Mapper接口
    @Autowired
    private MoocCatDictTMapper moocCatDictTMapper;
    @Autowired
    private MoocYearDictTMapper moocYearDictTMapper;
    @Autowired
    private MoocSourceDictTMapper moocSourceDictTMapper;
    //引入查询电影主表与电影演员表的Mapper
    @Autowired
    private MoocFilmInfoTMapper moocFilmInfoTMapper;
    @Autowired
    private MoocActorTMapper moocActorTMapper;

    /**
     * banner图的接口:得到所有banner
     *
     * @return
     */
    @Override
    public List<BannerVO> getBanners() {
        List<BannerVO> result = new ArrayList<>();
        //1.调用MybatisPLUS提供的selcetLis方法,没有条件,直接查询所有的Bannerb表所有数据,我们拿list封装banner对象
        List<MoocBannerT> moocBanners = moocBannerTMapper.selectList(null);
        //2.将得到的List遍历,并且取出数据,放入API模块中的BannerVO对象中
        //其实这俩数据是一一对应的,直接无脑get和set就好
        for (MoocBannerT moocBannerT : moocBanners) {
            //创建bannerVO对象
            BannerVO bannerVO = new BannerVO();
            //注意数据库直接对应的bannerid是Intger类型,而我们自定义返回前端的是String类型
            //所以这里需要转换一下,就是intger-->string,只需要在intger类型后面+" "就好
            bannerVO.setBannerId(moocBannerT.getUuid() + "");
            bannerVO.setBannerAddress(moocBannerT.getBannerAddress());
            bannerVO.setBannerUrl(moocBannerT.getBannerUrl());
            //将转换好的数据放入List中,每遍历一个,就转换,然后放入BannerVo模块的List
            result.add(bannerVO);
        }
        return result;
    }

    /**
     * 这里定义一个将数据库连接对象转换为API接口所需对象的方法,主要是遍历,get和set
     * 将数据库电影主表的数据传递给FilmInfo对象(FilmInfo还有很多其他字段,这里只是一部分)
     *
     * @param moocFilms
     * @return
     */
    private List<FilmInfo> getFilmInfos(List<MoocFilmT> moocFilms) {
        List<FilmInfo> filmInfos = new ArrayList<>();
        for (MoocFilmT moocFilmT : moocFilms) {
            FilmInfo filmInfo = new FilmInfo();
            filmInfo.setScore(moocFilmT.getFilmScore());
            filmInfo.setImgAddress(moocFilmT.getImgAddress());
            filmInfo.setFilmType(moocFilmT.getFilmType());
            filmInfo.setFilmScore(moocFilmT.getFilmScore());
            filmInfo.setFilmName(moocFilmT.getFilmName());
            //fuilmID这里是intger转存储string,需要加" "
            filmInfo.setFilmId(moocFilmT.getUuid() + "");
            filmInfo.setExpectNum(moocFilmT.getFilmPresalenum());
            filmInfo.setBoxNum(moocFilmT.getFilmBoxOffice());
            //展示时间这里需要格式化一下,数据库是时间戳类型,转换成yyyy-MM-dd的大众Date类型
            //这里用了guns的工具类,我们可以自己写,或者实体类直接使用注解进行格式刷
            filmInfo.setShowTime(DateUtil.getDay(moocFilmT.getFilmTime()));

            // 将转换的对象放入结果集
            filmInfos.add(filmInfo);
        }

        return filmInfos;
    }

    /**
     * 获取热搜影片接口,分两种情况:
     * 1.如果是首页,限制查询的返回的条数为nums,且内容必须为热映影片
     * 2.如果不用限制,则为正常的分页查询,参数需要当前页,和要的一页多少条
     *如果不是首页,这是 一个多条件混合排序分页查询
     * @param isLimit
     * @param nums
     * @return
     */
    @Override
    public FilmVO getHotFilms(boolean isLimit, int nums,int nowPage,int sortId,
                              int yearId,int catId,int sourceId) {
        //1.创建需要被赋值的API接口中的对象
        FilmVO filmVO = new FilmVO();
        List<FilmInfo> filmInfos = new ArrayList<>();

        //2.为热映影片添加限制条件
        EntityWrapper<MoocFilmT> entityWrapper = new EntityWrapper<>();
        entityWrapper.eq("film_status", "1");
        //3.判断是否是要首页的东西:(根据isLimit字段判断,有限制了就是首页,没有就是正常分页)
        if (isLimit) {
            //如果是,则限制返还条数,限制内容为热映影片,且当前页填写1就好(这里分页分的是直接与数据库返回的对象)
            //这里大概逻辑就是按照页数,限制每一页返回的数量,比如:限制条件为一页5个,当前页就只给前五个,
            //其余的页数分配,按照返回个数每每隔断,当然我们用起来的话,这里分页插件直接利用page对象,
            //page对象就是分页的要求其中:
            //          泛型:要求分页的结果(对象),参数:当前页和一页多少个.
            Page<MoocFilmT> page = new Page<>(1, nums);
            //调用MybatisPLUS进行分页查询
            List<MoocFilmT> moocFilms = moocFilmTMapper.selectPage(page, entityWrapper);
            //将MoocFilmT对象,即影片主表的内容放到api模块对象中,组织filmInfos
            filmInfos = getFilmInfos(moocFilms);
            //这里将返回的对象的个数传给影片总对象
            filmVO.setFilmNum(moocFilms.size());
            filmVO.setFilmInfo(filmInfos);
        } else {
            //如果不是,则是列表页(普通的分页查询),限制条件同样为热映影片,且
            //还需要按照不同的字段进行排序orderby
            Page<MoocFilmT> page = null;
            //根据sortId不同,进行组织不同的page对象
            //1.按热门排序,2.按时间排序,3按评价排序
            switch (sortId) {
                case 1:
                    //1.按照热门进行排序的,当前页,一页多少,按照影片票房排序
                    page = new Page<>(nowPage, nums, "film_box_office");
                    break;
                case 2:
                    //2.按照时间进行排序
                    page = new Page<>(nowPage, nums, "film_time");
                    break;
                case 3:
                    //3.按照分数进行排序
                    page = new Page<>(nowPage, nums, "film_score");
                    break;
                default:
                    //默认按照票房进行排序
                    page = new Page<>(nowPage, nums, "film_box_office");
                    break;
            }
            //且除了分页对象外,还有明确的条件列表,所以我们也需要构造条件对象
            //如果sourceId,yearId,catId,不为99,则表示要表示要按照对应的编号进行查询
            if (sourceId !=99){
                //添加条件
                entityWrapper.eq("film_source",sortId);
            }
            if (yearId !=99){
                entityWrapper.eq("film_date",yearId);
            }
            if (catId !=99){
                //模糊查询
                //分类id比较特殊,因为一个电影可能有多个类别,比如阿甘正传,爱情,剧情,战争
                //所以数据库采取的存储方式为#2#4#22#这种方式存储的
                //所以我们查的时候需要重新对其定义
                //%代表模糊查询
                String catStr = "%#"+catId+"#%";
                entityWrapper.like("film_cats",catStr);
            }
            //根据条件进行分页查询,获得当前页结果,但是前端还需要总页数进行展示
            List<MoocFilmT> moocFilms = moocFilmTMapper.selectPage(page, entityWrapper);
            //转换成filmInfos
            filmInfos = getFilmInfos(moocFilms);
            //将一页多少个也给前端
            filmVO.setFilmNum(moocFilms.size());

            //总页数的计算:总条数/每页多少条,取余数+1(因为java的默认是向下取整,而我们只要有一条,就应该算1页)
            //totalCounts/nums +1
            //1.计算总条数
            Integer totalCounts = moocFilmTMapper.selectCount(entityWrapper);
            //2.计算总页数
            int totalPages = (totalCounts/nums)+1; //每页10条,我们现在有6条->1

            filmVO.setFilmInfo(filmInfos);
            filmVO.setTotalPage(totalPages);
            filmVO.setNowPage(nowPage);

        }
        return filmVO;
    }

    /**
     * 即将上映的电影的接口:
     *
     * @param isLimit
     * @param nums
     * @return
     */
    @Override
    public FilmVO getSonnFilms(boolean isLimit, int nums,int nowPage,int sortId,
                               int yearId,int catId,int sourceId) {
        //1.创建需要被赋值的API接口中的对象
        FilmVO filmVO = new FilmVO();
        List<FilmInfo> filmInfos = new ArrayList<>();

        //2.为热映影片添加限制条件
        EntityWrapper<MoocFilmT> entityWrapper = new EntityWrapper<>();
        entityWrapper.eq("film_status", "1");
        //3.判断是否是要首页的东西:(根据isLimit字段判断,有限制了就是首页,没有就是正常分页)
        if (isLimit) {
            //如果是,则限制返还条数,限制内容为热映影片,且当前页填写1就好(这里分页分的是直接与数据库返回的对象)
            //这里大概逻辑就是按照页数,限制每一页返回的数量,比如:限制条件为一页5个,当前页就只给前五个,
            //其余的页数分配,按照返回个数每每隔断,当然我们用起来的话,这里分页插件直接利用page对象,
            //page对象就是分页的要求其中:
            //          泛型:要求分页的结果(对象),参数:当前页和一页多少个.
            Page<MoocFilmT> page = new Page<>(2, nums);
            //调用MybatisPLUS进行分页查询
            List<MoocFilmT> moocFilms = moocFilmTMapper.selectPage(page, entityWrapper);
            //将MoocFilmT对象,即影片主表的内容放到api模块对象中,组织filmInfos
            filmInfos = getFilmInfos(moocFilms);
            //这里将返回的对象的个数传给影片总对象
            filmVO.setFilmNum(moocFilms.size());
            filmVO.setFilmInfo(filmInfos);
        } else {
            //如果不是,则是列表页(普通的分页查询),限制条件同样为热映影片,且
            //还需要按照不同的字段进行排序orderby
            Page<MoocFilmT> page = null;
            //根据sortId不同,进行组织不同的page对象
            //1.按热门排序,2.按时间排序,3按评价排序
            switch (sortId) {
                case 1:
                    //1.按照热门进行排序的,当前页,一页多少,按照影片票房排序
                    page = new Page<>(nowPage, nums, "film_box_office");
                    break;
                case 2:
                    //2.按照时间进行排序
                    page = new Page<>(nowPage, nums, "film_time");
                    break;
                case 3:
                    //3.按照分数进行排序
                    page = new Page<>(nowPage, nums, "film_score");
                    break;
                default:
                    //默认按照票房进行排序
                    page = new Page<>(nowPage, nums, "film_box_office");
                    break;
            }
            //如果sourceId,yearId,catId,不为99,则表示要表示要按照对应的编号进行查询
            if (sourceId !=99){
                //添加条件
                entityWrapper.eq("film_source",sortId);
            }
            if (yearId !=99){
                entityWrapper.eq("film_date",yearId);
            }
            if (catId !=99){
                //模糊查询
                //分类id比较特殊,因为一个电影可能有多个类别,比如阿甘正传,爱情,剧情,战争
                //所以数据库采取的存储方式为#2#4#22#这种方式存储的
                //所以我们查的时候需要重新对其定义
                //%代表模糊查询
                String catStr = "%#"+catId+"#%";
                entityWrapper.like("film_cats",catStr);
            }
            //根据条件进行分页查询,获得当前页结果,但是前端还需要总页数进行展示
            List<MoocFilmT> moocFilms = moocFilmTMapper.selectPage(page, entityWrapper);
            //转换成filmInfos
            filmInfos = getFilmInfos(moocFilms);
            //将一页多少个也给前端
            filmVO.setFilmNum(moocFilms.size());

            //总页数的计算:总条数/每页多少条,取余数+1(因为java的默认是向下取整,而我们只要有一条,就应该算1页)
            //totalCounts/nums +1
            //1.计算总条数
            Integer totalCounts = moocFilmTMapper.selectCount(entityWrapper);
            //2.计算总页数
            int totalPages = (totalCounts/nums)+1; //每页10条,我们现在有6条->1

            filmVO.setFilmInfo(filmInfos);
            filmVO.setTotalPage(totalPages);
            filmVO.setNowPage(nowPage);

        }
        return filmVO;
    }


    /**
     * 分页查询经典影片:
     * @param nowPage
     * @param nums
     * @param sortId
     * @param yearId
     * @param catId
     * @return
     */
    @Override
    public FilmVO getClassicFilms(int nowPage, int nums, int sortId,
                                  int yearId,int catId,int sourceId) {
        FilmVO filmVO = new FilmVO();
        List<FilmInfo> filmInfos = new ArrayList<>();

        // 即将上映影片的限制条件
        EntityWrapper<MoocFilmT> entityWrapper = new EntityWrapper<>();
        entityWrapper.eq("film_status","3");

        // 少了一层首页判断,因为只有列表页，同样需要限制内容为即将上映影片
        Page<MoocFilmT> page = null;
        // 根据sortId的不同，来组织不同的Page对象
        // 1-按热门搜索，2-按时间搜索，3-按评价搜索
        switch (sortId){
            case 1 :
                page = new Page<>(nowPage,nums,"film_box_office");
                break;
            case 2 :
                page = new Page<>(nowPage,nums,"film_time");
                break;
            case 3 :
                page = new Page<>(nowPage,nums,"film_score");
                break;
            default:
                page = new Page<>(nowPage,nums,"film_box_office");
                break;
        }

        // 如果sourceId,yearId,catId 不为99 ,则表示要按照对应的编号进行查询
        if(sourceId != 99){
            entityWrapper.eq("film_source",sourceId);
        }
        if(yearId != 99){
            entityWrapper.eq("film_date",yearId);
        }
        if(catId != 99){
            // #2#4#22#
            String catStr = "%#"+catId+"#%";
            entityWrapper.like("film_cats",catStr);
        }

        List<MoocFilmT> moocFilms = moocFilmTMapper.selectPage(page, entityWrapper);
        // 组织filmInfos
        filmInfos = getFilmInfos(moocFilms);
        filmVO.setFilmNum(moocFilms.size());

        // 需要总页数 totalCounts/nums -> 0 + 1 = 1
        // 每页10条，我现在有6条 -> 1
        int totalCounts = moocFilmTMapper.selectCount(entityWrapper);
        int totalPages = (totalCounts/nums)+1;

        filmVO.setFilmInfo(filmInfos);
        filmVO.setTotalPage(totalPages);
        filmVO.setNowPage(nowPage);

        return filmVO;
    }











    /**
     * 现阶段正在上映电影票房前10排行榜:(单表就够)
     * 分页查询:条件1页,10个,并且进行倒序排序
     * 最后需要将直连对象转换成我们api模块需要的对象
     *
     * @return
     */
    @Override
    public List<FilmInfo> getBoxRanking() {
        //条件是:正在上映的,票房前10
        //MybatisPlus创建条件对象
        EntityWrapper<MoocFilmT> entityWrapper = new EntityWrapper<>();
        entityWrapper.eq("film_status", "1");
        //同样引入分页对象,参数为:
        //1页,一页展示10个,通过票房正序排序(order by),默认倒叙
        Page<MoocFilmT> page = new Page<>(1, 10, "film_box_office");
        //根据条件查询,并且进行分页(传入分页对象)
        List<MoocFilmT> moocFilms = moocFilmTMapper.selectPage(page, entityWrapper);
        //将dao层直连对象数据,赋值给我们的api(拼接对象中)
        List<FilmInfo> filmInfos = getFilmInfos(moocFilms);

        return filmInfos;
    }

    /**
     * 期待电影排行榜接口:
     * 分页查询,根据预售值进行排序
     *
     * @return
     */
    @Override
    public List<FilmInfo> getExpectRanking() {
        //条件是:即将上映的,预售前10
        EntityWrapper<MoocFilmT> entityWrapper = new EntityWrapper<>();
        //条件是即将上映
        entityWrapper.eq("film_status", "2");
        //同样引入分页对象,参数为:
        //1页,一页展示10个,通过预售值倒序排序(order by),默认倒叙
        Page<MoocFilmT> page = new Page<>(1, 10, "film_preSaleNum");
        //根据条件查询,并且进行分页(传入分页对象)
        List<MoocFilmT> moocFilms = moocFilmTMapper.selectPage(page, entityWrapper);
        //将dao层直连对象数据,赋值给我们的api(拼接对象中)
        List<FilmInfo> filmInfos = getFilmInfos(moocFilms);
        return filmInfos;
    }

    /**
     * 正在上映(以打分为标准)总对比前10:
     *
     * @return
     */
    @Override
    public List<FilmInfo> getTop() {
        EntityWrapper<MoocFilmT> entityWrapper = new EntityWrapper<>();
        entityWrapper.eq("film_status", "1");
        //条件是:正在上映,打分前10
        //同样引入分页对象,参数为:
        //1页,一页展示10个,通过分数值倒序排序(order by),默认倒叙
        Page<MoocFilmT> page = new Page<>(1, 10, "FILM_SCORE");
        //根据条件查询,并且进行分页(传入分页对象)
        List<MoocFilmT> moocFilms = moocFilmTMapper.selectPage(page, entityWrapper);
        //将dao层直连对象数据,赋值给我们的api(拼接对象中)
        List<FilmInfo> filmInfos = getFilmInfos(moocFilms);
        return filmInfos;
    }

    //======================获取条件列表

    /**
     * 查询分类列表,返回全部数据:
     * @return
     */
    @Override
    public List<CatVO> getCat() {
        List<CatVO> cats = new ArrayList<>();

        //查询实体对象,实体对象可以为数据库的直连映射对象,-MoocCatDictT
        //默认直接查询全部就好
        List<MoocCatDictT> moocCats = moocCatDictTMapper.selectList(null);
        //将实体对象转换为业务对象 -CatVO
        for (MoocCatDictT moocCatDictT : moocCats) {
            CatVO catVO = new CatVO();
            catVO.setCatId(moocCatDictT.getUuid() + "");
            catVO.setCatName(moocCatDictT.getShowName());
            cats.add(catVO);
        }
        return cats;
    }

    /**
     * 返回片源列表的接口:
     * @return
     */
    @Override
    public List<SourceVO> getSources() {
        List<SourceVO> sources = new ArrayList<>();
        List<MoocSourceDictT> moocSourceDicts = moocSourceDictTMapper.selectList(null);
        for(MoocSourceDictT moocSourceDictT : moocSourceDicts){
            SourceVO sourceVO = new SourceVO();
            sourceVO.setSourceId(moocSourceDictT.getUuid()+"");
            sourceVO.setSourceName(moocSourceDictT.getShowName());
            sources.add(sourceVO);
        }
        return sources;
    }





    /**
     * 查询年代列表,返回全部数据:
     * @return
     */
    @Override
    public List<YearVO> getYears() {
        //一个集合的数据转换到另一个集合中
        List<YearVO> years = new ArrayList<>();
        //查询实体对象,实体对象可以为数据库的直连映射对象,-MoocCatDictT
        //默认直接查询全部就好
        List<MoocYearDictT> moocYears = moocYearDictTMapper.selectList(null);
        //将实体对象转换为业务对象 -CatVO
        //遍历循环转换,再将对象一个一个添加到新的集合当中
        for (MoocYearDictT moocYear : moocYears) {
            YearVO yearVO = new YearVO();
            yearVO.setYearId(moocYear.getUuid() + "");
            yearVO.setYearName(moocYear.getShowName());
            years.add(yearVO);
        }
        return years;
    }

    /**
     * 接口4:根据影片名或影片Id查询影片详情:
     * 多表
     * @param searchType
     * @param searchParam
     * @return
     */
    @Override
    public FilmDetailVO getFilmDetail(int searchType, String searchParam) {

       FilmDetailVO filmDetailVO = null;
        //按名称查找是模糊查询,剩下的全是按id查找
        //searchType,1=按名称查找,2-按id查找
        if(searchType == 1){
            //注意如果按文字搜索时,我们的输入也需要做模糊匹配
            filmDetailVO=moocFilmTMapper.getFilmDetailByName("%"+searchParam+"%");
        }else {
            filmDetailVO=moocFilmTMapper.getFilmDetailById(searchParam);
        }
        return filmDetailVO;
    }
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