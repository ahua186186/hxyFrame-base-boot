package com.hxy.component.shiro;

import com.alibaba.fastjson.JSON;
import com.hxy.modules.common.utils.ShiroUtils;
import com.hxy.modules.common.utils.UserUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.filter.authc.FormAuthenticationFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

/**
 * ajax shiro form auth处理类
 *
 * @author Tom Deng
 * @date 2017-03-25
 */
public class AjaxFormAuthenticationFilter extends FormAuthenticationFilter {
    private static final Logger log = LoggerFactory.getLogger(AjaxFormAuthenticationFilter.class);

    @Override
    protected boolean isAccessAllowed(ServletRequest request, ServletResponse response, Object mappedValue) {
        try {
            String username = this.getUsername(request); //没什么卵用  ShiroConfig 先配置setUsernameParam
            String pwd = this.getPassword(request); //没什么卵用

            String accNo = ShiroUtils.getUserEntity() ==null ? null : UserUtils.getCurrentUserId();
            log.debug("username[{}] mappedValue[{}] SimpleAuthFilter isAccessAllowed ", accNo, mappedValue);
            Map<String, String> resultMap = new HashMap<String, String>();
            Subject subject = SecurityUtils.getSubject();
            if (!subject.isAuthenticated()) {
                // 判断是不是Ajax请求
                if (ShiroFilterUtils.isAjax(request)) {
                    log.debug("当前用户已经被踢出,并且是Ajax请求！");
                    resultMap.put("user_status", "300");
                    resultMap.put("message", "请重新登录！");
                    out(response, resultMap);
                }
                log.debug("username[{}] SimpleAuthFilter isAccessAllowed false", accNo);
                return Boolean.FALSE;
            }
            log.debug("username[{}] SimpleAuthFilter isAccessAllowed true", accNo);
        } catch (Exception e) {
            log.error("SimpleAuthFilter isAccessAllowed Exception", e);
        }

        return Boolean.TRUE;
    }


    @Override
    protected boolean onAccessDenied(final ServletRequest request, final ServletResponse response) throws Exception {
        if (isLoginRequest(request, response)) {
            String username = this.getUsername(request);
            String pwd = this.getPassword(request);
            if (isLoginSubmission(request, response)) {
                if (log.isTraceEnabled()) {
                    log.trace("Login submission detected.  Attempting to execute login.");
                }
                return executeLogin(request, response);
            } else {
                if (log.isTraceEnabled()) {
                    log.trace("Login page view.");
                }
                // allow them to see the login page ;)
                return true;
            }
        } else {
            if (log.isTraceEnabled()) {
                log.trace("Attempting to access a path which requires authentication.  Forwarding to the " +
                    "Authentication url [" + getLoginUrl() + "]");
            }

            final HttpServletRequest req = (HttpServletRequest)request;
            final HttpServletResponse res = (HttpServletResponse)response;

            // 如果是ajax请求响应头会有，x-requested-with 在响应头设置session状态
            if (req.getHeader("x-requested-with") != null
                && "XMLHttpRequest".equalsIgnoreCase(req.getHeader("x-requested-with"))) {
                res.setHeader("sessionstatus", "timeout");
            } else {
                saveRequestAndRedirectToLogin(request, response);
            }
            return false;
        }
    }

    private void out(ServletResponse hresponse, Map<String, String> resultMap) throws IOException {
        hresponse.setCharacterEncoding("UTF-8");
        PrintWriter out = hresponse.getWriter();
        out.println(JSON.toJSONString(resultMap));
        out.flush();
        out.close();
    }
}
