package com.stylefeng.guns.api.user.vo;

import java.io.Serializable;

//注册使用的实体
//gateWAY和user模块之间来回传输,需要序列化后才能传输
public class UserModel implements Serializable {

    //注册所需字段,也就是注册需要填写的字段
    private String username;
    private  String password;
    private  String email;
    private String phone;
    private  String address;


    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }





}
