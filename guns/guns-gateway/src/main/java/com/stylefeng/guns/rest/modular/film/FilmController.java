package com.stylefeng.guns.rest.modular.film;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.rpc.RpcContext;
import com.stylefeng.guns.api.film.FilmAsyncServiceApi;
import com.stylefeng.guns.api.film.FilmServiceApi;
import com.stylefeng.guns.api.film.vo.*;
import com.stylefeng.guns.rest.modular.film.vo.FilmConditionVO;
import com.stylefeng.guns.rest.modular.film.vo.FilmIndexVO;
import com.stylefeng.guns.rest.modular.film.vo.FilmRequestVO;
import com.stylefeng.guns.rest.modular.vo.ResponseVO;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

@RestController
@RequestMapping("/film/")
public class FilmController {
    //定义静态变量的图片前缀
    private static final String IMG_PRE = "WWW.121212.cn/";

    //引入服务层暴漏的接口
    @Reference(interfaceClass = FilmServiceApi.class,check = false)
    private FilmServiceApi filmServiceApi;


    @Reference(interfaceClass = FilmAsyncServiceApi.class,async = true,check = false)
    private FilmAsyncServiceApi filmAsyncServiceApi;

    /**
     * 访问首页的接口:(查询方法,本来是可以六个请求,现在六合一)
     * API网关:(和cloud里略有区别)
     * 1.功能聚合(API聚合)(前端调一次接口->后端给六个接口的信息)
     * 好处:
     * 1.六个接口,一次请求,同一时刻节省饿了五次http请求
     * 2.同一个接口对外暴漏,降低了前后端分离开发的难度和复杂度
     * 坏处:
     * 无法精确控制各个接口的数据,要么数据全有,要么一个数据没有
     * 而且一次获取数据过多,容易出现问题
     *
     * @return
     */
    @GetMapping("getIndex")
    public ResponseVO getIndex() {
        //将六个接口的数据聚合在一起返回
        FilmIndexVO filmIndexVO = new FilmIndexVO();
        //获取banner信息
        filmIndexVO.setBanners(filmServiceApi.getBanners());
        //获取正在热映的电影(因为是首页获取的信息,所以有限制,而且只获取八条),这里其实默认有用条件只有第一个和第二个,所以我们其他选项设置成默认项
        filmIndexVO.setHotFilms(filmServiceApi.getHotFilms(true, 8, 1, 99, 99, 99, 99));
        //获取即将上映的电影
        filmIndexVO.setSoonFilms(filmServiceApi.getSonnFilms(true, 8, 1, 99, 99, 99, 99));
        //票房排行榜
        filmIndexVO.setBoxRanking(filmServiceApi.getBoxRanking());
        //获取受欢迎的榜单
        filmIndexVO.setExpectRanking(filmServiceApi.getExpectRanking());
        //获取前一百
        filmIndexVO.setTop100(filmServiceApi.getTop());

        return ResponseVO.success(IMG_PRE, filmIndexVO);
    }

    /**
     * 条件列表查询的接口:
     * 且条件可能不存在,默认为99
     * 年分,种类,片源
     *
     * @param catId
     * @param sourceId
     * @param yearId
     * @return
     */
    @GetMapping("getConditionList")
    public ResponseVO getConditionList(
            @RequestParam(name = "catId", required = false, defaultValue = "99") String catId,
            @RequestParam(name = "sourceId", required = false, defaultValue = "99") String sourceId,
            @RequestParam(name = "yearId", required = false, defaultValue = "99") String yearId
    ) {
        //这里解释一下active字段的意义,当用户选中哪个参数的时候,比如选中了中国,中国这个参数就是active=true,
        //其他的片源,比如美国,日本actvie字段就是false
        FilmConditionVO filmConditionVO = new FilmConditionVO();
        // 标识位
        boolean flag = false;
        // 类型集合
        List<CatVO> cats = filmServiceApi.getCat();
        List<CatVO> catResult = new ArrayList<>();
        CatVO cat = null;
        //先将查出来的集合遍历
        for (CatVO catVO : cats) {
            // 判断集合是否存在catId，如果存在，则将对应的实体变成active状态
            // 6
            // 1,2,3,99,4,5 ->
            /*
                优化：【理论上】
                    1、数据层查询按Id进行排序【有序集合 -> 有序数组】
                    2、通过二分法查找
             */
            //一开始就先把99摘出来,供以后判断,防止乱序引起bug
            if (catVO.getCatId().equals("99")) {
                cat = catVO;
                continue;
            }
            //如果遍历出来的对象有和前端传递进来的id相同
            if (catVO.getCatId().equals(catId)) {
                flag = true;
                //将其Active属性置为true
                catVO.setActive(true);

            } else {
                //否则置为false
                catVO.setActive(false);
            }
            //然后将对象放回返回的集合当中
            catResult.add(catVO);
        }
        //我们需要在所有对象全部遍历后,除(99),才进行99的flag的判断
        //如果全部置为了flase,我们仍然需要把id等于99置为true
        //且通过第三方flag,判断是否有id置为了ture,如果有的话,就将99置为false,如果
        //没有的话,就将99置为true,同样返回到结果集对象中
        if (!flag) {
            cat.setActive(true);
            catResult.add(cat);
        } else {
            cat.setActive(false);
            catResult.add(cat);
        }


        // 片源集合
        flag = false;
        List<SourceVO> sources = filmServiceApi.getSources();
        List<SourceVO> sourceResult = new ArrayList<>();
        SourceVO sourceVO = null;
        for (SourceVO source : sources) {
            if (source.getSourceId().equals("99")) {
                sourceVO = source;
                continue;
            }
            if (source.getSourceId().equals(catId)) {
                flag = true;
                source.setActive(true);

            } else {
                source.setActive(false);
            }

            sourceResult.add(source);

        }
        // 如果不存在，则默认将全部变为Active状态
        if (!flag) {
            sourceVO.setActive(true);
            sourceResult.add(sourceVO);
        } else {
            sourceVO.setActive(false);
            sourceResult.add(sourceVO);
        }

        // 年代集合
        flag = false;
        List<YearVO> years = filmServiceApi.getYears();
        List<YearVO> yearResult = new ArrayList<>();
        YearVO yearVO = null;
        for (YearVO year : years) {
            if (year.getYearId().equals("99")) {
                yearVO = year;
                continue;
            }
            if (year.getYearId().equals(catId)) {
                flag = true;
                year.setActive(true);

            } else {
                year.setActive(false);
            }

            yearResult.add(year);

        }
        // 如果不存在，则默认将全部变为Active状态
        if (!flag) {
            yearVO.setActive(true);
            yearResult.add(yearVO);
        } else {
            yearVO.setActive(false);
            yearResult.add(yearVO);
        }

        //将三个条件的集合信息封装到一起,返回
        filmConditionVO.setCatInfo(catResult);
        filmConditionVO.setSourceInfo(sourceResult);
        filmConditionVO.setYearInfo(yearResult);

        return ResponseVO.success(filmConditionVO);
    }

    /**
     * 获取影片列表的接口(非首页)
     * 采用对象接收参数的方式
     *
     * @return
     */
    @GetMapping("getFilms")
    public ResponseVO getFilms(FilmRequestVO filmRequestVO) {
       String img_pre="你服务的图片前缀";

        //将前端传递过来的参数传入到我们的api接口中
        //多条件组合分页查询:
        //1.根据showType判断影片查询类型
        FilmVO filmVO = null;
        switch (filmRequestVO.getShowType()) {
            case 1:
                filmVO = filmServiceApi.getHotFilms(
                        false, filmRequestVO.getPageSize(), filmRequestVO.getNowPage(),
                        filmRequestVO.getSortId(), filmRequestVO.getSourceId(), filmRequestVO.getYearId(),
                        filmRequestVO.getCatId());
                break;

            case 2:
                filmVO = filmServiceApi.getSonnFilms(
                        false, filmRequestVO.getPageSize(), filmRequestVO.getNowPage(),
                        filmRequestVO.getSortId(), filmRequestVO.getSourceId(), filmRequestVO.getYearId(),
                        filmRequestVO.getCatId());
                break;
            case 3:
                filmVO = filmServiceApi.getClassicFilms(
                        filmRequestVO.getPageSize(), filmRequestVO.getNowPage(),
                        filmRequestVO.getSortId(), filmRequestVO.getSourceId(),
                        filmRequestVO.getYearId(), filmRequestVO.getCatId());
                break;
            default:
                filmVO = filmServiceApi.getHotFilms(
                        false, filmRequestVO.getPageSize(), filmRequestVO.getNowPage(),
                        filmRequestVO.getSortId(), filmRequestVO.getSourceId(), filmRequestVO.getYearId(),
                        filmRequestVO.getCatId());
                break;
        }
        return ResponseVO.success(filmVO.getNowPage(),filmVO.getTotalPage(),img_pre,filmVO.getFilmInfo());
    }


    /**
     * 根据传入的影片名或影片id查询影片详情:
     * 多表
     * @param searchParam
     * @param searchType
     * @return
     */
    @GetMapping("films/{searchParam}")
    public ResponseVO films(@PathVariable("searchParam") String searchParam, int searchType) throws ExecutionException, InterruptedException {
     //根据searchType,判断查询类型(是按电影名还是id查询,名字一般去solr,id一般去缓存或数据库)

        //得到info1,2,3的内容,而且返回了uuid
        FilmDetailVO filmDetail = filmServiceApi.getFilmDetail(searchType, searchParam);

        //我们需要判断一下查询出来的影片详情对象是否为空,如果为空,需要我们返回前端没有可查询信息
        if (filmDetail == null){
            return ResponseVO.serviceFail("没有可查询的影片");
            //我们同样需要再判断一下查询出来的id是否为null为空
        }else if (filmDetail.getFilmId()==null || filmDetail.getFilmId().trim().length()==0){
            //如果id同样为null为空,同样返回没有可查询的对象
            return ResponseVO.serviceFail("没有可查询的影片");
        }

        String filmId= filmDetail.getFilmId();
        //查询影片的详细信息->(多表) Dubbo的异步获取机制,根据返回给实体的uuid,查询info4中,与主表关联的其他表的内容
        //获取inof04中影片描述信息:对应返回json的biography,filmId
      //  FilmDescVO filmDescVO = filmAsyncServiceApi.getFilmDesc(filmId);
        filmAsyncServiceApi.getFilmDesc(filmId);
        //异步调用之获取future对象
        Future<FilmDescVO> filmDescVOFuture = RpcContext.getContext().getFuture();

        //获取info04中影片的图片信息:对应返回json的:	imgs
        //ImgVO imgVO = filmAsyncServiceApi.getImgs(filmId);
        filmAsyncServiceApi.getImgs(filmId);
        //异步调用之获取future对象
        Future<ImgVO> imgVOFuture = RpcContext.getContext().getFuture();

        //获取导演信息
//        ActorVO directorVO = filmAsyncServiceApi.getDectInfo(filmId);
        filmAsyncServiceApi.getDectInfo(filmId);
        //异步调用之获取future对象
        Future<ActorVO> actorVOFuture = RpcContext.getContext().getFuture();


        //获取info04中影片的演员信息:对应返回json的: actors
        //且返回的是多信息
       // List<ActorVO> actors = filmAsyncServiceApi.getActors(filmId);
        filmAsyncServiceApi.getActors(filmId);
        //异步调用之获取future对象
        Future<List<ActorVO>> actorsVOFuture = RpcContext.getContext().getFuture();



        //组织返回给前端的Info04对象
        InfoRequestVO infoRequestVO = new InfoRequestVO();

        //组织Actor属性,因为info04下的actors属性有两个字段,所以我们需要再封装一层,将导演和演员放在一起
        ActorRequestVO actorRequestVO = new ActorRequestVO();
        actorRequestVO.setActors(actorsVOFuture.get());
        actorRequestVO.setDirector(actorVOFuture.get());

        //封装info04属性
        infoRequestVO.setActors(actorRequestVO);
        infoRequestVO.setBiography(filmDescVOFuture.get().getBiography());
        infoRequestVO.setFilmId(filmId);
        infoRequestVO.setImgVO(imgVOFuture.get());

        //封装info04到最终返回值里
        filmDetail.setInfo04(infoRequestVO);

        //返回前端的结果集当中,还需要封装图片前缀字段
        return  ResponseVO.success("我的图片前缀",filmDetail);


    }
}
















