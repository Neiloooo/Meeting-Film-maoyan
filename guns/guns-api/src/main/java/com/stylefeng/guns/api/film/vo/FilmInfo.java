package com.stylefeng.guns.api.film.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 这些实体是用来有与数据库交互的
 * 影片详情,所有和影片具体数据有关的属性都被写在了这个实体中
 * 将来返回的时候为空的数据需要去除掉,否则赘余数据太多
 */
@Data
public class FilmInfo implements Serializable {
        //影片的id
        private String filmId;
        //影片的类型
        private int filmType;
        //影片的图片地址
        private String imgAddress;
        //影片名
        private String filmName;
        //影片得分
        private String filmScore;
        //影片的受欢迎人数
        private int expectNum;
        //影片上映时间
        private String showTime;
        //排名?
        private int boxNum;
        //影片分数,被排名接口使用
        private String score;

}
