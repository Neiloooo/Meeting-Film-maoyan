package com.stylefeng.guns.rest.modular.auth.filter;

import com.stylefeng.guns.core.base.tips.ErrorTip;
import com.stylefeng.guns.core.util.RenderUtil;
import com.stylefeng.guns.rest.common.CurrentUser;
import com.stylefeng.guns.rest.common.exception.BizExceptionEnum;
import com.stylefeng.guns.rest.config.properties.JwtProperties;
import com.stylefeng.guns.rest.modular.auth.util.JwtTokenUtil;
import com.stylefeng.guns.rest.modular.vo.ResponseVO;
import io.jsonwebtoken.JwtException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 对客户端请求的jwt token验证过滤器
 *
 * @author fengshuonan
 * @Date 2017/8/24 14:04
 */
public class AuthFilter extends OncePerRequestFilter {

    private final Log logger = LogFactory.getLog(this.getClass());

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private JwtProperties jwtProperties;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
        if (request.getServletPath().equals("/" + jwtProperties.getAuthPath())) {
            chain.doFilter(request, response);
            return;
        }

        //配置忽略列表,不再拦截哪些请求(比如注册与静态资源)
        //1.取出配置文件中忽略列表的地址
        String ignoreUrl = jwtProperties.getIgnoreUrl();
        //2.将其以,逗号为分割切开,并且放进数组中
        String[] ignoreUrls = ignoreUrl.split(",");
        //3.遍历循环,将数组中的忽略列表的数据遍历出来与前端传递过来的请求进行对比
        for (int i=0;i<ignoreUrls.length;i++){
            //如果请求路径等于忽略列表的某一个
            if (request.getServletPath().startsWith(ignoreUrls[i])){
                //1.一般filter都是一个链,web.xml 里面配置了几个就有几个。一个一个的连在一起
                //request -> filter1 -> filter2 ->filter3 -> …. -> request resource.
                //2.chain.doFilter将请求转发给过滤器链下一个filter , 如果没有filter那就是你请求的资源
                //简称:放行
                chain.doFilter(request,response);
                return;
            }
        }


        final String requestHeader = request.getHeader(jwtProperties.getHeader());
        String authToken = null;
        //从请求头中获取jwt
        if (requestHeader != null && requestHeader.startsWith("Bearer ")) {
            authToken = requestHeader.substring(7);
            //通过Token获取UserId,并且将其存入到ThreadLocal中,以方便后续业务调用
            //1.直接调取JWT的工具类的方法,获取UserId
            //这里之前是通过useranme验证,所以从token取出是username,
            //经过我们修改后是通过userId验证,所以token取出来的是userid
            //至于为什么,因为我们后端传递给前端jwt的时候,就是传入的userID,而不再是username
            //具体在AuthController里有写
            String userId =jwtTokenUtil.getUsernameFromToken(authToken);
            //验证出来userID是null,说明前端发过来的令牌有问题,直接返回
            if (userId == null){
                    //因为没有返回值,所以直接return直接返回表示失败
                    return ;
                }else {
                //成功了的话,将userID放入当前线程中,谢谢
                CurrentUser.saveUserId(userId);

            }
            //验证token是否过期,包含了验证jwt是否正确
            try {
                boolean flag = jwtTokenUtil.isTokenExpired(authToken);
                if (flag) {
                    RenderUtil.renderJson(response, new ErrorTip(BizExceptionEnum.TOKEN_EXPIRED.getCode(), BizExceptionEnum.TOKEN_EXPIRED.getMessage()));
                    return;
                }
            } catch (JwtException e) {
                //有异常就是token解析失败
                RenderUtil.renderJson(response, new ErrorTip(BizExceptionEnum.TOKEN_ERROR.getCode(), BizExceptionEnum.TOKEN_ERROR.getMessage()));
                return;
            }
        } else {
            //header没有带Bearer字段
            RenderUtil.renderJson(response, new ErrorTip(BizExceptionEnum.TOKEN_ERROR.getCode(), BizExceptionEnum.TOKEN_ERROR.getMessage()));
            return;
        }
        chain.doFilter(request, response);
    }
}