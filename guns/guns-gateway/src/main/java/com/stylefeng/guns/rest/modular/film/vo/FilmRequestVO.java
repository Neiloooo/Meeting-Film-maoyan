package com.stylefeng.guns.rest.modular.film.vo;

import lombok.Data;

@Data
public class FilmRequestVO {

    private  Integer showType=1; //(不是必填)默认1:查询类型:1正在上映,2即将上映,3经典影片
    private  Integer sortId=1; //(否必填)默认1 排序方式:1热门2时间3评价排序
    private  Integer sourceId=99;//(否) 区域编号 默认99
    private  Integer catId=99;//(否) 类型编号 默认99
    private  Integer yearId=99;//(否) 年代编号 默认99

    private  Integer nowPage=1;//(否) 影片列表当前页数 默认1 分页参数
    private  Integer pageSize=18;//(否) 每页显示条数 默认18



}
