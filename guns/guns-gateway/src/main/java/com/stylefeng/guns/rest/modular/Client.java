package com.stylefeng.guns.rest.modular;


import com.alibaba.dubbo.config.annotation.Reference;
import com.stylefeng.guns.api.user.UserAPI;
import org.springframework.stereotype.Component;

@Component
public class Client {
    //如果想要调用已注册的服务,只要使用dubbo的注解,并且在注解中添加接口所在的类即可
    //这样就可以找到对外暴漏的接口-->进而找到接口的实现类,实现了远程调用
    @Reference(interfaceClass = UserAPI.class)
    private UserAPI userAPI;
    //这里只是演示,是否能进行远程调用,其实远程调用可以理解为,Controller层与Service的分离
    //Controller层远程调用Service层的接口,进而调用其实现类,达到远程调用与复用的方式
    public void run(){
        userAPI.login("admin","password");

    }
}
