package com.stylefeng.guns.api.film.vo;

import com.stylefeng.guns.api.film.vo.ActorVO;
import lombok.Data;

import java.util.List;

/**
 * 返回报文中的actors所对应的对象
 */
@Data
public class ActorRequestVO {

    private ActorVO director;
    private List<ActorVO> actors;

}
