package com.stylefeng.guns.rest.modular.vo;

import lombok.Data;

/**
 * 接口通用的返回结果集
 * @param <M>
 */
@Data
public class ResponseVO<M> {

    //返回状态[200-成功,500-业务失败,999-系统异常]
    private int status;
    //返回信息
    private String msg;
    //返回数据实体,因为返回数据不固定所以使用泛型进行占位符,到时候我们返回什么类型就写什么类型就好
    private M data;
    //为图片添加前缀
    private String imgPre;

    //返回参数之分页参数:
    private Integer nowPage;
    private Integer totalPage;

    //不允许别的类创建实体,只能使用我们自己定义好的类型进行返回
    //有点类似枚举,其实定义一个枚举更简单方便
    private ResponseVO(){
    }
    //静态方法,直接.出来就可以的那种,这里写的是成功的结果集
    public static <M> ResponseVO success(int nowPage,int totalPage,String imgPre,M m){
        //1.新建结果集对象
        ResponseVO responseVO = new ResponseVO();
        //为状态置200
        responseVO.setStatus(200);
        //传入方法传进来的数据参数
        responseVO.setData(m);
        //为图片设置前缀
        responseVO.setImgPre(imgPre);
        //设置分页的参数
        responseVO.setNowPage(nowPage);
        responseVO.setTotalPage(totalPage);
        //返回
        return responseVO;
    }







    //静态方法,直接.出来就可以的那种,这里写的是成功的结果集
    public static <M> ResponseVO success(String imgPre,M m){
        //1.新建结果集对象
        ResponseVO responseVO = new ResponseVO();
        //为状态置200
        responseVO.setStatus(200);
        //传入方法传进来的数据参数
        responseVO.setData(m);
        //为图片设置前缀
        responseVO.setImgPre(imgPre);
        //返回
        return responseVO;
    }






    //静态方法,直接.出来就可以的那种,这里写的是成功的结果集
    public static <M> ResponseVO success(M m){
        //1.新建结果集对象
        ResponseVO responseVO = new ResponseVO();
        //为状态置200
        responseVO.setStatus(200);
        //传入方法传进来的数据参数
        responseVO.setData(m);
        //返回
        return responseVO;
    }

    //静态方法,直接.出来就可以的那种,这里写的是成功的结果集
    public static <M> ResponseVO success(String msg){
        //1.新建结果集对象
        ResponseVO responseVO = new ResponseVO();
        //为状态置200
        responseVO.setStatus(200);
        //传入方法传进来的数据参数
        responseVO.setMsg(msg);
        //返回
        return responseVO;
    }





    //业务失败的结果集
    public static <M> ResponseVO serviceFail(String msg){
        //1.新建结果集对象
        ResponseVO responseVO = new ResponseVO();
        //为状态置500
        responseVO.setStatus(500);
        //传入失败信息
        responseVO.setMsg(msg);
        //返回
        return responseVO;
    }

    //系统异常的结果集(try catch中的异常)
    public static <M> ResponseVO appFil(String msg){
        //1.新建结果集对象
        ResponseVO responseVO = new ResponseVO();
        //为状态置999
        responseVO.setStatus(999);
        //传入失败信息
        responseVO.setMsg(msg);
        //返回
        return responseVO;
    }

}
