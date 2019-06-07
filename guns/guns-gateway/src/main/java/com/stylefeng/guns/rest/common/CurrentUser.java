package com.stylefeng.guns.rest.common;

/**
 * 通用的存入线程工具
 * 和创建List差不多,只不过随当前线程存在,随当前线程毁灭,作用域比List大一些,比锁机制更快
 */
public class CurrentUser {

    //创建一个UserInfoModel类型的线程存储空间
    private static final InheritableThreadLocal<String> threadLocal = new InheritableThreadLocal<>();
//第二种方式,我们只将我们所需的userId存入就好,这个体量就小了很多,使用方法和存入对象大同小异
    //1.向当前线程存入userID
    public static void saveUserId(String userId){
        threadLocal.set(userId);
    }
    //2.从当前线程中取出userID
    public static  String getCurrentUser(){
        return threadLocal.get();
    }


    //1.第一种方式直接存入对象,但是存入数据量太大,如果并发高,jvm内存爆炸
//    //将前端传入的当前实体放入当前线程中
//    public static void saveUserInfo(UserInfoModel userInfoModel)
//    {
//        threadLocal.set(userInfoModel);
//    }
//    //取出当前线程中的User实体对象
//    public static UserInfoModel getCurrentUser()
//    {
//       return threadLocal.get();
//    }
//}




}