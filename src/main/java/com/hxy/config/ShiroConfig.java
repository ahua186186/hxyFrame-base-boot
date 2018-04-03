package com.hxy.config;

import com.google.common.collect.Maps;
import com.hxy.component.redis.CachingShiroSessionDao;
import com.hxy.component.shiro.AjaxFormAuthenticationFilter;
import com.hxy.component.shiro.KickoutSessionControlFilter;
import com.hxy.component.shiro.MyRealm;
import com.hxy.component.shiro.RetryLimitHashedCredentialsMatcher;
import org.apache.shiro.cache.MemoryConstrainedCacheManager;
import org.apache.shiro.codec.Base64;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.session.mgt.SessionManager;
import org.apache.shiro.spring.LifecycleBeanPostProcessor;
import org.apache.shiro.spring.security.interceptor.AuthorizationAttributeSourceAdvisor;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.web.mgt.CookieRememberMeManager;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.apache.shiro.web.servlet.SimpleCookie;
import org.apache.shiro.web.session.mgt.DefaultWebSessionManager;
import org.springframework.aop.framework.autoproxy.DefaultAdvisorAutoProxyCreator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.servlet.Filter;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 类ShiroConfig的功能描述:
 * Shiro配置
 * @auther hxy
 * @date 2017-11-15 21:50:12
 */
@Configuration
public class ShiroConfig {


    @Bean("shiroFilter")
    public ShiroFilterFactoryBean shirFilter(SecurityManager securityManager) {
        ShiroFilterFactoryBean shiroFilter = new ShiroFilterFactoryBean();
        //Shiro的核心安全接口,这个属性是必须的
        shiroFilter.setSecurityManager(securityManager);
        shiroFilter.setLoginUrl("/login.html");
        shiroFilter.setSuccessUrl("/index.html");
        /**
         * 另外对于过滤器，一般这样使用：
         访问一般网页，如个人在主页之类的，我们使用 user 拦截器即可(比如在request中设置当前用户)，user 拦截器只要用户登
         录(isRemembered()==true or isAuthenticated()==true)过即可访问成功；
         访问特殊网页，如我的订单，提交订单页面，我们使用 authc 拦截器即可，authc 拦截器会
         判断用户是否是通过 Subject.login（isAuthenticated()==true）登录的，如果是才放行，否则
         会跳转到登录页面叫你重新登录。
         */
        Map<String, Filter> filters = Maps.newHashMap();
        filters.put("authc", this.authcFilter());
        //filters.put("kickout",new KickoutSessionControlFilter());
        shiroFilter.setFilters(filters);

        Map<String, String> filterMap = new LinkedHashMap<>();
        filterMap.put("/druid/**", "anon");
        filterMap.put("/app/**", "anon");
        filterMap.put("/**/*.css", "anon");
        filterMap.put("/**/*.js", "anon");
        filterMap.put("/*.html", "anon");
        filterMap.put("/fonts/**", "anon");
        filterMap.put("/images/**", "anon");
        filterMap.put("/plugins/**", "anon");
        filterMap.put("/login/captcha", "anon");
        filterMap.put("/statics/**", "anon");
        filterMap.put("/swagger/**", "anon");
        filterMap.put("/favicon.ico", "anon");
        filterMap.put("/", "anon");
        filterMap.put("/sys/**", "authc");
        //filterMap.put("/sys/**", "kickout");
        filterMap.put("/login/login", "anon");
        shiroFilter.setFilterChainDefinitionMap(filterMap);

        return shiroFilter;
    }


    @Bean("sessionManager")
    public SessionManager sessionManager(CachingShiroSessionDao sessionDAO){
        sessionDAO.setPrefix("shiro-session:");
        //注意中央缓存有效时间要比本地缓存有效时间长
        sessionDAO.setSeconds(1800);
        DefaultWebSessionManager sessionManager = new DefaultWebSessionManager();
        //设置session过期时间为1小时(单位：毫秒)，默认为30分钟
        sessionManager.setGlobalSessionTimeout(60 * 60 * 1000);
        sessionManager.setSessionIdUrlRewritingEnabled(false);
        sessionManager.setSessionValidationSchedulerEnabled(true);
        sessionManager.setSessionDAO(sessionDAO);
        return sessionManager;
    }


    @Bean("securityManager")
    public SecurityManager securityManager(SessionManager sessionManager) {
        DefaultWebSecurityManager securityManager = new DefaultWebSecurityManager();
        securityManager.setRealm(shiroRealm());
        securityManager.setSessionManager(sessionManager);
        securityManager.setCacheManager(cacheManager());
        securityManager.setRememberMeManager(rememberMeManager());
        return securityManager;
    }


    @Bean
    public AjaxFormAuthenticationFilter authcFilter() {
        final AjaxFormAuthenticationFilter authcFilter = new AjaxFormAuthenticationFilter();
        authcFilter.setUsernameParam("username");
        authcFilter.setPasswordParam("password");
        authcFilter.setRememberMeParam("isRememberMe");
        authcFilter.setFailureKeyAttribute("shiroLoginFailure");
        return authcFilter;
    }


    @Bean
    public MemoryConstrainedCacheManager cacheManager() {
        return new MemoryConstrainedCacheManager();
    }

    @Bean
    public SimpleCookie rememberMeCookie() {
        final SimpleCookie simpleCookie = new SimpleCookie("rememberMe");
        simpleCookie.setHttpOnly(true);
        simpleCookie.setMaxAge(259200);//<!-- 30 天 -->
        return simpleCookie;
    }

    @Bean
    public CookieRememberMeManager rememberMeManager() {
        final CookieRememberMeManager cookieRememberMeManager = new CookieRememberMeManager();
        cookieRememberMeManager.setCipherKey(Base64.decode("ZUdsaGJuSmxibVI2ZHc9PQ=="));
        cookieRememberMeManager.setCookie(rememberMeCookie());
        return cookieRememberMeManager;
    }
    @Bean
    public RetryLimitHashedCredentialsMatcher credentialsMatcher() {
        final RetryLimitHashedCredentialsMatcher credentialsMatcher = new RetryLimitHashedCredentialsMatcher(
                cacheManager());
        credentialsMatcher.setHashAlgorithmName("MD5");
        credentialsMatcher.setHashIterations(2);
        credentialsMatcher.setStoredCredentialsHexEncoded(true);
        return credentialsMatcher;
    }
    @Bean
    public MyRealm shiroRealm() {
        final MyRealm realm = new MyRealm();
        realm.setCredentialsMatcher(credentialsMatcher());
        return realm;
    }

    /**
     * 保证实现了Shiro内部lifecycle函数的bean执行
     * @return
     */
    @Bean("lifecycleBeanPostProcessor")
    public LifecycleBeanPostProcessor lifecycleBeanPostProcessor() {
        return new LifecycleBeanPostProcessor();
    }

    /**
     * AOP式方法级权限检查
     * @return
     */
    @Bean
    public DefaultAdvisorAutoProxyCreator defaultAdvisorAutoProxyCreator() {
        DefaultAdvisorAutoProxyCreator proxyCreator = new DefaultAdvisorAutoProxyCreator();
        proxyCreator.setProxyTargetClass(true);
        return proxyCreator;
    }

    @Bean
    public AuthorizationAttributeSourceAdvisor authorizationAttributeSourceAdvisor(SecurityManager securityManager) {
        AuthorizationAttributeSourceAdvisor advisor = new AuthorizationAttributeSourceAdvisor();
        advisor.setSecurityManager(securityManager);
        return advisor;
    }

}
