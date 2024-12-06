package com.gtcafe.asimov.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class HelloControoler {
    
    @RequestMapping("/hello") // 處理請求的映射
    public ModelAndView helloWorld() {

        // 建立一個 ModelAndView 物件，並指定視圖名稱
        ModelAndView modelAndView = new ModelAndView("helloView");

        // 向模型添加數據，可以在視圖中使用
        modelAndView.addObject("message", "Hello, Spring MVC!");

        return modelAndView; // 回傳 ModelAndView 物件
    }


	@GetMapping("/greeting")
	public String greeting(@RequestParam(name="name", required=false, defaultValue="World") String name, Model model) {
		model.addAttribute("name", name);
		return "greeting";
	}
}
