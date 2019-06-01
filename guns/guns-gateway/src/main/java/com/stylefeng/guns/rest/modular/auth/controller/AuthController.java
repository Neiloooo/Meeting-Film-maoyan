package com.stylefeng.guns.rest.modular.auth.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.stylefeng.guns.api.user.UserAPI;
import com.stylefeng.guns.rest.modular.auth.controller.dto.AuthRequest;
import com.stylefeng.guns.rest.modular.auth.controller.dto.AuthResponse;
import com.stylefeng.guns.rest.modular.auth.util.JwtTokenUtil;
import com.stylefeng.guns.rest.modular.vo.ResponseVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 请求验证的
 *
 * @author fengshuonan
 * @Date 2017/8/24 14:22
 */
@RestController
public class AuthController {

    @Autowired
    private JwtTokenUtil jwtTokenUtil;


    //如果想要调用已注册的服务,只要使用dubbo的注解,并且在注解中添加接口所在的类即可
    //这样就可以找到对外暴漏的接口-->进而找到接口的实现类,实现了远程调用
    @Reference(interfaceClass = UserAPI.class,check = false)
    private UserAPI userAPI;

    @RequestMapping(value = "${jwt.auth-path}")
    public ResponseVO createAuthenticationToken(AuthRequest authRequest) {
        //我们先默认给true全部放进来
        boolean validate = true;
        //去掉guns自身的用户名密码验证机制,使用我们自己的通过userId是否存在在我们的redis数据库来判断
        //userApi中的参数从authRequest中获取,
        // 网关之中调用服务端的接口,经过网关鉴权和认证后的数据,
        // 才会传递给user服务的接口中
        //调用登录接口,返回userId
        int userId =userAPI.login(authRequest.getUserName(), authRequest.getPassword());
        //数据库中的userId没有0,所以0的话置为false
        if (userId==0){
            validate = false;
        }
        if (validate) {
            //生成随机key
            final String randomKey = jwtTokenUtil.getRandomKey();
            //根据数据库返回的userId和随机key生成token,
            // 而且注意数据库返回的userID是int类型,我只需要在前面加上"　＂＋，它就可以拼接成字符串了
            //防止null的问题,尽量不要使用toString
            final String token = jwtTokenUtil.generateToken(""+userId, randomKey);
            //将随机值与token一起返回前端,通过我们自定义的返回值
            return ResponseVO.success(new AuthResponse(token,randomKey));
        } else {
            return ResponseVO.serviceFail("用户名或密码错误");
        }
    }
}
