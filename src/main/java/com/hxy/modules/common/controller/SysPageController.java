package com.hxy.modules.common.controller;

import com.hxy.component.shiro.VelocityShiro;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

/**
 * 系统页面视图
 * 
 * @author hxy
 * @date 2017年3月24日 下午11:05:27
 */
@Controller
public class SysPageController {

	@Autowired
	private VelocityShiro velocityShiro;

	@RequestMapping("{module}/{url}.html")
	public ModelAndView page(@PathVariable("module") String module, @PathVariable("url") String url){
		String viewName = "modules/"+ module + "/" + url + ".html";
		ModelAndView modelAndView = new ModelAndView();
		modelAndView.setViewName(viewName);
		modelAndView.addObject("shiro",velocityShiro);
		return  modelAndView;
	}

	@RequestMapping("/")
	public String index(){
		return "login.html";
	}


}
