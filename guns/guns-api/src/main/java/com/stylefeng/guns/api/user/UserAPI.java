package com.stylefeng.guns.api.user;

import com.stylefeng.guns.api.user.vo.UserInfoModel;
import com.stylefeng.guns.api.user.vo.UserModel;

public interface UserAPI {
    /**
     * 定义登录接口
     * 为什么要返回int?
     *验证登录成功以后,会返回用户id,
     * 并且通过JTW的形式传到客户端,客户端携带着JWT,就相当于
     * 携带userId,然后每次进来的时候,我们可以把userID缓存到redis中
     * 而且可以在redis中设置过期时间,通过判断redis中是否有userId,
     * 进而判断JWT是否是否在有效期内,从而达到控制用户登录时间的功能
     * 这样就弥补了无法控制JWT有效时长的缺点,因为我们以缓存里的数据为基准.
     * 变相解决了JWT更新的问题
     * @param username
     * @param password
     * @return
     */
    int login(String username, String password);

    /**
     * 判断是否注册
     * @param userModel
     * @return
     */
    boolean register(UserModel userModel);

    /**
     * 判断username是否存在
     * @param username
     * @return
     */
    boolean checkUsername(String username);

    /**
     * 查询用户信息,通过id
     * @param uuid
     * @return
     */
    UserInfoModel getUserInfo(int uuid);

    /**
     * 修改基本的用户信息(不包含敏感信息比如:密码)
     * @param userInfoModel
     * @return
     */
    UserInfoModel updateUserInfo(UserInfoModel userInfoModel);


}
