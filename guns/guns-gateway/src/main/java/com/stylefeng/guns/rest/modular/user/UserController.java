package com.stylefeng.guns.rest.modular.user;

import com.alibaba.dubbo.config.annotation.Reference;
import com.stylefeng.guns.api.user.UserAPI;
import com.stylefeng.guns.api.user.vo.UserInfoModel;
import com.stylefeng.guns.api.user.vo.UserModel;
import com.stylefeng.guns.rest.common.CurrentUser;
import com.stylefeng.guns.rest.modular.vo.ResponseVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
@RequestMapping("/user/")
@RestController
public class UserController {

    //注入UserApi的接口,远程调用user模块里的实现类
    @Reference(interfaceClass = UserAPI.class,check = false)
    private UserAPI userAPI;

    /**
     * 注册的完整接口,
     * 主要在Controller层就是判断是否为空与null,返回前端要的结果集
     *
     * @param userModel
     * @return
     */
    @PostMapping("register")
    public ResponseVO reigeter(UserModel userModel) {
        //如果用户名是null或去除掉前后空格后长度是0的话
        if (userModel.getUsername() == null || userModel.getUsername().trim().length() == 0) {
            return ResponseVO.serviceFail("用户名不能为空");
        }
        if (userModel.getPassword() == null || userModel.getPassword().trim().length() == 0) {
            return ResponseVO.serviceFail("密码为空");
        }

        boolean isSuccess = userAPI.register(userModel);
        if (isSuccess) {
            return ResponseVO.success("注册成功");
        } else {
            return ResponseVO.serviceFail("注册失败");
        }
    }

    /**
     * 验证用户名是否存在的接口,(注册的时候需要)
     * 如果用户名存在返回false
     *
     * @param username
     * @return
     */
    @PostMapping("check")
    public ResponseVO reigeter(String username) {
        //如果用户名不等于null且用户名长度不为0
        if (username != null && username.trim().length() > 0) {
            //当返回true的时候,表示用户名可用
            boolean notExists = userAPI.checkUsername(username);
            if (notExists) {
                return ResponseVO.success("用户名不存在,可以使用");
            } else {
                return ResponseVO.serviceFail("用户名已存在,请换一个");
            }
        } else {
            return ResponseVO.serviceFail("用户名不能为空");
        }
    }

    /**
     * 退出登录接口
     * @return
     */
    @PostMapping("logout")
    public ResponseVO logout() {
        /**
         * 企业应用:
         * 单点登录时效性:
         * 1.前端存储JWT[七天]:只要有JWT就可以访问后端API
         * 但是这样有个JWT的刷新问题,且无法精准控制JWT
         * 2.所以需要配合redis,以UserId为key,以用户信息为value,对用户数据进行存储,并设置时效[1天]
         * 3.这样当用户访问我们,我们就可以取出前端JWT的UserID,去查询redis数据库,如果redis数据库中的缓存存在,
         * (一天内),用户不需要登录,直接访问后端即可,如果用户缓存不存在,跳转页面让用户登录
         * 单点登录的退出:
         * 如果用户想退出,前端清楚JWT
         * 后端直接删除redis中的缓存
         * 都能完成退出操作
         *
         * 但是现阶段没有引入redis:
         * 所以采取做法是单纯的前端清除JWT
         */
        //这样默认只要调用这个接口,就是退出成功了,因为前端JWT没了,进不来的
        return ResponseVO.success("用户退出成功");
    }

    /**
     * 用户查询自己信息的接口
     * @return
     */
    @GetMapping("getUserInfo")
    public ResponseVO getUserInfo() {
        //1.从当前线程变量中获取usrId
        //线程中的userId是我们从JWT中获取存入到线程中的
        String userId = CurrentUser.getCurrentUser();
        if (userId !=null && userId.trim().length()>0){
            //将用户ID传入后端进行查询
            //将前端传递进来的userId转换成int类型
            int uuid = Integer.parseInt(userId);
            //调用写好的api,根据id查询用户的信息
            UserInfoModel userInfo = userAPI.getUserInfo(uuid);
            if (userInfo!=null){
                //如果查询出来的结果不是null,将其返回给前端
                return ResponseVO.success(userInfo);
            }else {
                //这个是我们代码问题
                return ResponseVO.appFil("用户查询失败");
            }
        }else {
            //这个不算问题
            return  ResponseVO.serviceFail("用户未登录");
        }
    }

    /**
     * 用户修改信息的接口
     * @param userInfoModel
     * @return
     */
    @PostMapping("updateUserInfo")
    public ResponseVO updateUserInfo(UserInfoModel userInfoModel) {
        //1.从当前线程变量中获取usrId
        //线程中的userId是我们从JWT中获取存入到线程中的
        String userId = CurrentUser.getCurrentUser();
        if (userId !=null && userId.trim().length()>0){
            //将前端传递进来的userId转换成int类型
            int uuid = Integer.parseInt(userId);
            //2.判断当前登录人员的ID与修改的结果ID是否一致
            if(uuid !=userInfoModel.getUuid()){
                //不一致的话警告一下
                return  ResponseVO.serviceFail("请修改您自己的个人信息");
            }
            //一致的话调用我们的更新API进行更新
            UserInfoModel userInfo = userAPI.updateUserInfo(userInfoModel);
            if (userInfo!=null){
                //如果返回的结果不是null,将其返回给前端
                return ResponseVO.success(userInfo);
            }else {
                //这个是我们代码问题
                return ResponseVO.appFil("用户修改失败");
            }
        }else {
            //这个不算问题
            return  ResponseVO.serviceFail("用户未登录");
        }
    }

}