package com.hxy.component.shiro;

import com.alibaba.fastjson.JSON;
import com.hxy.modules.common.utils.RedisClusterUtil;
import com.hxy.modules.common.utils.RedisUtil2;
import com.hxy.modules.common.utils.SerializeUtil;
import com.hxy.modules.common.utils.SpringContextUtil;
import com.hxy.modules.sys.entity.UserEntity;
import org.apache.shiro.cache.Cache;
import org.apache.shiro.cache.CacheManager;
import org.apache.shiro.session.Session;
import org.apache.shiro.session.mgt.DefaultSessionKey;
import org.apache.shiro.session.mgt.SessionManager;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.filter.AccessControlFilter;
import org.apache.shiro.web.util.WebUtils;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.Serializable;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.LinkedList;

/**
 * <p>User: Zhang Kaitao
 * <p>Date: 14-2-18
 * <p>Version: 1.0
 */
public class KickoutSessionControlFilter extends AccessControlFilter {

    public static final String kickOutCache ="kick-out-cache";
    private String kickoutUrl; //踢出后到的地址
    private boolean kickoutAfter = false; //踢出之前登录的/之后登录的用户 默认踢出之前登录的用户
    private int maxSession = 1; //同一个帐号最大会话数 默认1

    public void setKickoutUrl(String kickoutUrl) {
        this.kickoutUrl = kickoutUrl;
    }

    public void setKickoutAfter(boolean kickoutAfter) {
        this.kickoutAfter = kickoutAfter;
    }

    public void setMaxSession(int maxSession) {
        this.maxSession = maxSession;
    }


    @Override
    protected boolean isAccessAllowed(ServletRequest request, ServletResponse response, Object mappedValue) throws Exception {
        return false;
    }

    @Override
    protected boolean onAccessDenied(ServletRequest request, ServletResponse response) throws Exception {

        RedisUtil2 redisUtil2= (RedisUtil2)SpringContextUtil.getBean("redisUtil2",RedisUtil2.class);
        RedisClusterUtil redisClusterUtil= (RedisClusterUtil)SpringContextUtil.getBean("redisClusterUtil",RedisClusterUtil.class);
        SessionManager sessionManager= (SessionManager)SpringContextUtil.getBean("sessionManager",SessionManager.class);

        Subject subject = getSubject(request, response);
        if(!subject.isAuthenticated() && !subject.isRemembered()) {
            //如果没有登录，直接进行之后的流程
            return true;
        }

        Session session = subject.getSession();
        String username = ((UserEntity) subject.getPrincipal()).getUserName();
        Serializable sessionId = session.getId();

        //test redis
        String value = JSON.toJSONString(session);
        redisUtil2.lLeftPush("session-login-cache",value);
        redisUtil2.lLeftPush("session-login-cache",value);
        System.out.println("=====redisUtil2 session-login-cache====" + redisUtil2.lLen("session-login-cache"));
        //
        //TODO  同步控制 ,
        //TODO  方案：MQ解耦解决并发问题 ：
        //TODO  通过redis自增（addAndGet） >maxSession判断是否触发踢出用户规则： 增加1个队列，存储待踢出的用户sessionId。
        Deque<Serializable> deque = (Deque<Serializable>)redisClusterUtil.getObject(KickoutSessionControlFilter.kickOutCache+username);
        //最好用redis的数据机构，存Deque不能保证原子性，但是实际场景一个人登陆基本不存在并发。
        if(deque == null) {
            return true;
        }
        //如果队列里的sessionId数超出最大会话数，开始踢人
        if(deque.size() > maxSession) {
            Serializable kickoutSessionId = null;
            if(kickoutAfter) { //如果踢出后者
                kickoutSessionId = deque.removeFirst();
            } else { //否则踢出前者
                kickoutSessionId = deque.removeLast();
            }
            try {
                Session kickoutSession = sessionManager.getSession(new DefaultSessionKey(kickoutSessionId));
                if(kickoutSession != null) {
                    //设置会话的kickout属性表示踢出了
                    kickoutSession.setAttribute("kickout", true);
                    redisClusterUtil.setObject(KickoutSessionControlFilter.kickOutCache,deque);
                }
            } catch (Exception e) {//ignore exception
            }
        }

        //如果被踢出了，直接退出，重定向到踢出后的地址
        if (session.getAttribute("kickout") != null) {
            //会话被踢出了
            try {
                subject.logout();
            } catch (Exception e) { //ignore
            }
            saveRequest(request);
            if(kickoutUrl !=null){
                WebUtils.issueRedirect(request, response, kickoutUrl);
            }else{
                saveRequestAndRedirectToLogin(request, response);
            }
            return false;
        }

        return true;
    }
}
