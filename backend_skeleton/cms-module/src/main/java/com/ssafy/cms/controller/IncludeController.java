package com.ssafy.cms.controller;

import com.ssafy.cms.constant.DataMap;
import com.ssafy.cms.util.RequestUtil;
import lombok.RequiredArgsConstructor;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Controller
@RequiredArgsConstructor
public class IncludeController {

    private static Log log = LogFactory.getLog(IncludeController.class);

    /**
     * 사이드 바
     * @param request
     * @param response
     * @param model
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/common/inc/sidebar.do")
    public ModelAndView sidebar(HttpServletRequest request, HttpServletResponse response, Model model) throws Exception {

        DataMap param = RequestUtil.getDataMap(request);

        ModelAndView mav = new ModelAndView("fragments/sidebar");
        return mav;
    }

}
