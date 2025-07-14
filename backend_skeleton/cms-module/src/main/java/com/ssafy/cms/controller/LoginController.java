package com.ssafy.cms.controller;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Controller
@AllArgsConstructor
public class LoginController {

    /**
     * 로그인 폼
     * @return
     */
    @GetMapping("/loginform.do")
    public String loginform(){
        return "common/login";
    }

    /**
     * 로그인
     * @param request
     * @return
     * @throws Exception
     */
    @GetMapping(value = "/login.do")
    public ModelAndView login(HttpServletRequest request) throws Exception {

        ModelAndView mav = new ModelAndView("common/login");

        return mav;
    }

    /**
     * 로그인 성공 시 이동하는 페이지
     * @param request
     * @param response
     * @param model
     * @return
     * @throws Exception
     */
    @GetMapping(value = "/main.do")
    public String main(HttpServletRequest request, HttpServletResponse response, Model model) throws Exception {

        return "common/dashboard";
    }
}
