package com.stylefeng.guns.rest.modular.user;

import com.alibaba.dubbo.config.annotation.Service;
import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.stylefeng.guns.api.user.UserAPI;
import com.stylefeng.guns.api.user.vo.UserInfoModel;
import com.stylefeng.guns.api.user.vo.UserModel;
import com.stylefeng.guns.core.util.MD5Util;
import com.stylefeng.guns.rest.common.persistence.dao.MoocUserTMapper;
import com.stylefeng.guns.rest.common.persistence.model.MoocUserT;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.Date;
/**
 * 对外暴露的接口的实现类
 * @author AK
 */
//交给spring容器管理
@Component
//对外暴露服务,这里使用的是dubbo的注解,绑定接口UserAPI的全限定类名
@Service(interfaceClass = UserAPI.class)
public class UserServiceimpl implements UserAPI {
    //注入Dao接口,本质上注入的是Mybatis-plus帮我们写好的基础增删改查
    @Autowired
    private MoocUserTMapper moocUserTMapper;


    /**
     * 注册
     * 只需要把用户信息存入到数据库就好
     * 说白了就是单表增加
     * @param userModel
     * @return
     */
    @Override
    public boolean register(UserModel userModel) {
        //1.获取注册信息,这springmvc和fastjson已经帮我们封装好了,就是上面被封装好的对象
        //2.将注册信息实体转换为数据实体
        // 将注册信息实体转换为数据实体[mooc_user_t],这里其实就是将用户传递进来的实体数据取出来,放进我们和数据库交互的实体中
        //然后在数据库的实体通过mybatis-plus,调用dao接口方法写入到数据库中
        MoocUserT moocUserT = new MoocUserT();
        moocUserT.setUserName(userModel.getUsername());
        moocUserT.setEmail(userModel.getEmail());
        moocUserT.setAddress(userModel.getAddress());
        moocUserT.setUserPhone(userModel.getPhone());
        // 创建时间和修改时间 -> current_timestamp
        // 这里创建时间和修改时间的字段交给数据库来做了,我们也可以自己new Date,然后在实体上加格式化注解,使用自己的格式
        // 5.7版本以后数据库可以自动帮我们创建写入时间,但是是时间戳

        // 密码数据加密 【MD5混淆加密 + 盐值 -> Shiro加密,这种是最安全的】
        //数据加密主要还是对密码的加密,存入到数据库中的是加密后的用户密码
        //这里还是简单的MD5加密,不怎么安全,企业来讲要还是盐加密,换个工具类
        String md5Password = MD5Util.encrypt(userModel.getPassword());
        moocUserT.setUserPwd(md5Password); // 注意存入数据库的是加密后的密码

        //3.将数据实体中的数据插入数据库2
        Integer insert = moocUserTMapper.insert(moocUserT);
        //可以通过返回的数据判断新增方法是否成功
        if (insert > 0) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 登录
     * @param username
     * @param password
     * @return
     */
    @Override
    public int login(String username, String password) {
        //1.根据登录账号获取数据库信息,也就是根据用户名查询用户信息
        //可以看出与数据库连接的实体,我们都使用MoocUserT,因为它与数据库有了映射
        MoocUserT moocUserT = new MoocUserT();
        //将条件username放入实体中,MybatisPLus可以自动根据实体中存入的数据,进行条件查询
        moocUserT.setUserName(username);
        //调用Mybatis-plus的的selectOne方法进行查询,根据非主键查询整个User实体
        MoocUserT result = moocUserTMapper.selectOne(moocUserT);

        //2.将前端传递进来的结果与加密后的密码进行匹配
        if (result!=null &&result.getUuid()>0){
            //这里采用的方法是将密码进行再次md5加密,进而对比,这种方法不是很安全,后期建议换成盐加密
            String md5Password = MD5Util.encrypt(password);
            if ((result.getUserPwd().equals(md5Password))){
                //3.如果登录密码与数据库密码相同,返回用户的ID,这里是Uuid,其实返回用户的id后,会被拦截器与随机数做成令牌发往前端
                return result.getUuid();
            }
        }
        //如果密码有问题,直接reutn0,拜拜了您
        return 0;
    }

    /**
     * 验证是否存在用户名,
     * 接口:通过用户名,查询数据库中此用户名的用户有几个
     * @param username
     * @return
     */
    @Override
    public boolean checkUsername(String username) {
        //1.创建MyBATIS-plus的条件构造器,注意这里条件的类型一定要标记好
        EntityWrapper<MoocUserT> entityWrapper = new EntityWrapper<>();
        //2.添加条件,eq代表相等,前面是表字段,后面是传入的参数
        entityWrapper.eq("user_name",username);
        //3.通过条件对象查询数量.selectCount方法
        Integer result = moocUserTMapper.selectCount(entityWrapper);
        //4.正常来讲返回1,不过可能存在重名现象这里就判断大于0,具体看你自己的业务
        if (result!=null&&result>0){
            return false;
        }
            return true;
    }

    //这里写一个MoocUserT转换成UserInfoModel的方法
    //本质上就是一取一存,将与数据库对应的对象数据一部分转换成我们平时模块传输的数据
    private UserInfoModel do2UserInfo(MoocUserT moocUserT) {
        UserInfoModel userInfoModel = new UserInfoModel();

        userInfoModel.setUuid(moocUserT.getUuid());
        userInfoModel.setHeadAddress(moocUserT.getHeadUrl());
        userInfoModel.setPhone(moocUserT.getUserPhone());
        //从数据库取出的时间是时间戳形式(数据库自动帮我们存的),而我们需要的是Long形式,所以需要.getTime转换一下(java的date工具类)
        userInfoModel.setUpdateTime(moocUserT.getUpdateTime().getTime());
        userInfoModel.setEmail(moocUserT.getEmail());
        userInfoModel.setUsername(moocUserT.getUserName());
        userInfoModel.setNickname(moocUserT.getNickName());
        //这个是Intger转成StringL类型,注意前面加上" "就可以转换(因为数据库里一般存的都是数字,而我们实体类中定义的类型为string,所以需要转换一下)
        userInfoModel.setLifeState("" + moocUserT.getLifeState());
        userInfoModel.setBirthday(moocUserT.getBirthday());
        userInfoModel.setAddress(moocUserT.getAddress());
        userInfoModel.setSex(moocUserT.getUserSex());
        userInfoModel.setBeginTime(moocUserT.getBeginTime().getTime());
        userInfoModel.setBiography(moocUserT.getBiography());

        return userInfoModel;
    }


    /**
     * 根据id查询用户信息(非敏感信息)的接口
     * @param uuid
     * @return
     */
    @Override
    public UserInfoModel getUserInfo(int uuid) {
        //1.根据主键查询用户信息[MoocUserT]
        MoocUserT moocUserT = moocUserTMapper.selectById(uuid);
        //2.将MoocUserT转换成UserInfoModel
        UserInfoModel userInfoModel = do2UserInfo(moocUserT);
        //3.返回没有用户敏感信息的userInfoModel
        return userInfoModel;
    }

    //将long类型时间转换成java.sql.Date类型数据
    private Date time2Date(long time){
        Date date =new java.sql.Date(time);
        return date;
    }

    /**
     * 根据主键更新用户信息的接口:
     * 采用了先更新数据,再将数据查出来的方法,避免脏数据
     * 这里其实也等于前端只需要调用这一个方法就可以得到更新后的结果集
     * @param userInfoModel
     * @return
     */
    @Override
    public UserInfoModel updateUserInfo(UserInfoModel userInfoModel) {
        // 1.将传入的参数转换为DO 【MoocUserT】,就是接受的前端对象转换成与数据库对接的对象
        MoocUserT moocUserT = new MoocUserT();
        moocUserT.setUuid(userInfoModel.getUuid());
        moocUserT.setNickName(userInfoModel.getNickname());
        //这里使用了parseInt方法,将接收的前端传递过来的string数据转换成Intger类型
        moocUserT.setLifeState(Integer.parseInt(userInfoModel.getLifeState()));
        moocUserT.setBirthday(userInfoModel.getBirthday());
        moocUserT.setBiography(userInfoModel.getBiography());
        //将前端传递进来的long类型时间转换成数据库的时间戳类型数据
        moocUserT.setBeginTime(null);
        moocUserT.setHeadUrl(userInfoModel.getHeadAddress());
        moocUserT.setEmail(userInfoModel.getEmail());
        moocUserT.setAddress(userInfoModel.getAddress());
        moocUserT.setUserPhone(userInfoModel.getPhone());
        moocUserT.setUserSex(userInfoModel.getSex());
        //将修改时间变更为现在的时间,long类型时间戳转成mysql认的时间戳类型
        moocUserT.setUpdateTime(time2Date(System.currentTimeMillis()));

        //2.将数据存入数据库,这里不是更新所有字段,只需要更新部分数据,所以用updateById方法
        Integer isSuccess = moocUserTMapper.updateById(moocUserT);
        //3.如果修改成功,即返回数字为1,根据id将用户信息查出来,调用一开始写的根据id查询用户
        if (isSuccess>0){
            UserInfoModel userInfo = getUserInfo(moocUserT.getUuid());
            //返回前端
            return userInfo;
        }else {
            return userInfoModel;
        }
    }
}