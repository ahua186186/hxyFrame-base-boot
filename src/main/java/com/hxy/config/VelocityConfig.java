package com.hxy.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.view.velocity.VelocityConfigurer;
import org.springframework.web.servlet.view.velocity.VelocityViewResolver;

import java.util.HashMap;
import java.util.Map;

/**
 * 类VelocityConfig的功能描述:
 * Velocity配置
 * @auther hxy
 * @date 2017-11-13 17:03:49
 */
@Configuration
public class VelocityConfig {

    @Bean
    public VelocityViewResolver velocityViewResolver(){
        /**
         * spring.velocity.suffix=.html
         spring.velocity.cache=false
         */
        VelocityViewResolver viewResolver = new VelocityViewResolver();
        //<!--解决中文乱码-->
        viewResolver.setContentType("text/html;charset=UTF-8");
        viewResolver.setViewNames("*.html");
        viewResolver.setSuffix("");
        viewResolver.setCache(false);
        viewResolver.setDateToolAttribute("date");
        viewResolver.setNumberToolAttribute("number");
        //<!-- 配置 velocity工具类 MAIN 方法启动模式没法玩，这个坑没必要去填，改成从ModelAndView传入对象VelocityShiro-->
        //viewResolver.setToolboxConfigLocation("WEB-INF/velocity-toolbox.xml");
        viewResolver.setRequestContextAttribute("rc");
        //<!-- Spring4 支持  velocity-tools2.0  MAIN 方法启动模式没法玩，这个坑没必要去填 改成从ModelAndView传入对象VelocityShiro-->
        viewResolver.setViewClass(VelocityToolbox2View.class);
        viewResolver.setOrder(Integer.MIN_VALUE);
        return viewResolver;
    }

    @Bean
    public VelocityConfigurer velocityConfigurer() {
        VelocityConfigurer velocityConfigurer = new VelocityConfigurer();
        velocityConfigurer.setResourceLoaderPath("classpath:/views/");
        Map<String,Object> map = new HashMap<>();
        map.put("input.encoding","UTF-8");
        map.put("output.encoding","UTF-8");
        map.put("contentType","text/html;charset=UTF-8");
        velocityConfigurer.setVelocityPropertiesMap(map);
        return velocityConfigurer;
    }
}
