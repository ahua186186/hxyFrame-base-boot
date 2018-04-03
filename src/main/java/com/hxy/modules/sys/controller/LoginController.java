package com.hxy.modules.sys.controller;

import com.google.code.kaptcha.Constants;
import com.google.code.kaptcha.Producer;
import com.hxy.component.shiro.KickoutSessionControlFilter;
import com.hxy.modules.common.annotation.SysLog;
import com.hxy.modules.common.controller.BaseController;
import com.hxy.modules.common.utils.*;
import com.hxy.modules.sys.entity.UserEntity;
import org.apache.shiro.authc.*;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.imageio.ImageIO;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayDeque;
import java.util.Deque;

/**
 * 类的功能描述.
 * 登陆控制
 * @Auther hxy
 * @Date 2017/4/28
 */
@Controller
public class LoginController extends BaseController {
    @Autowired
    private RedisBean redisBean;
    @Autowired
    private Producer captchaProducer;
    @Autowired
    private RedisUtil2 redisUtil2;
    @Autowired
    private RedisClusterUtil redisClusterUtil;

    @RequestMapping("/login/captcha")
    public void captcha(HttpServletRequest request , HttpServletResponse response)throws ServletException, IOException {
        // Set to expire far in the past.
        response.setDateHeader("Expires", 0);
        // Set standard HTTP/1.1 no-cache headers.
        response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");
        // Set IE extended HTTP/1.1 no-cache headers (use addHeader).
        response.addHeader("Cache-Control", "post-check=0, pre-check=0");
        // Set standard HTTP/1.0 no-cache header.
        response.setHeader("Pragma", "no-cache");

        // return a jpeg
        response.setContentType("image/jpeg");

        // create the text for the image
        String capText = captchaProducer.createText();

        // store the text in the session
        request.getSession().setAttribute(Constants.KAPTCHA_SESSION_KEY, capText);

        //保存到shiro session
        ShiroUtils.setSessionAttribute(Constants.KAPTCHA_SESSION_KEY, capText);

        // create the image with the text
        BufferedImage bi = captchaProducer.createImage(capText);

        ServletOutputStream out = response.getOutputStream();

        // write the data out
        ImageIO.write(bi, "jpg", out);
        try {
            out.flush();
        } finally {
            out.close();
        }

    }

    /**
     * 登录
     */
    @ResponseBody
    @RequestMapping(value = "/login/login", method = RequestMethod.POST)
    public Result login(String username, String password, String captcha, final boolean isRememberMe)throws IOException {
        //redisBean.setStringValue("test111","1");
       // System.out.println("test redisBean:" + redisBean.getStringValue("test111"));
        String kaptcha = ShiroUtils.getKaptcha(Constants.KAPTCHA_SESSION_KEY);
        if(!captcha.equalsIgnoreCase(kaptcha)){
            return Result.error("验证码不正确");
        }
        try{
            Subject subject = ShiroUtils.getSubject();
            UsernamePasswordToken token = new UsernamePasswordToken(username, password);
            token.setRememberMe(isRememberMe);
            subject.login(token);

            Deque<Serializable> deque = (Deque<Serializable>)redisClusterUtil.getObject(KickoutSessionControlFilter.kickOutCache);
            if(deque == null) {
                deque = new ArrayDeque<Serializable>();
            }
            Session session = subject.getSession();
            Serializable sessionId = session.getId();
            deque.push(sessionId);
            redisClusterUtil.setObject(KickoutSessionControlFilter.kickOutCache+username,deque);
        }catch (UnknownAccountException e) {
            return Result.error(e.getMessage());
        }catch (IncorrectCredentialsException e) {
            return Result.error("账号或密码不正确");
        }catch (LockedAccountException e) {
            return Result.error("账号已被锁定,请联系管理员");
        }catch (AuthenticationException e) {
            return Result.error("账户验证失败");
        }

        return Result.ok();
    }

    /**
     * 方法logOut的功能描述:
     * 退出登陆
     * @params []
     * @return java.lang.String
     * @auther hxy
     * @date 2017-05-02 14:01:23
     */
    @RequestMapping(value="logout",method = RequestMethod.GET)
    @SysLog("退出系统")
    public String logOut(){
        String loginName= UserUtils.getCurrentUser().getLoginName();

        ShiroUtils.logout();
        return "redirect:/login.html";
    }



}
