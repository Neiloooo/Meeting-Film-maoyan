package com.stylefeng.guns.api.film.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * info4的最外层参数:filmId和biography
 */
@Data
public class FilmDescVO implements Serializable {

    private String biography;
    private String filmId;
}
