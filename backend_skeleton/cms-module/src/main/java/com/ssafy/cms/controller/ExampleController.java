package com.ssafy.cms.controller;

import com.ssafy.cms.constant.DataMap;
import com.ssafy.cms.service.ExampleService;
import com.ssafy.cms.util.RequestUtil;
import com.ssafy.cms.util.page.pageNavigationUtil;
import com.ssafy.core.entity.AdminSecurity;
import lombok.RequiredArgsConstructor;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * Controller 예제
 */
@Controller
@RequiredArgsConstructor
public class ExampleController {
    private static Log log = LogFactory.getLog(ExampleController.class);

    private final ExampleService exampleService;

    /**
     * 목록
     * @param request
     * @param model
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/cms/example/listExample.do")
    public String listExample(HttpServletRequest request, ModelMap model) throws Exception {

        // request 객체의 데이터를 DataMap에 담습니다.
        DataMap param = RequestUtil.getDataMap(request);

        // 목록 페이지 검색 타입 기본값 설정 
        if(param.getString("sch_type") == null){
            param.put("sch_type", "기본 설정 값 입력");
        }

        // DB에서 목록 총 개수 조회
        int exampleCnt = exampleService.countExample(param);
        // 페이징 처리를 위하여 목록 총 개수 "totalCount" 라는 이름으로 파라미터에 담습니다.
        param.put("totalCount", exampleCnt);
        // 페이징 처리를 위한 정보 생성
        param = pageNavigationUtil.createNavigationInfo(model, param);
        // DB에서 목록 조회
        List exampleList = exampleService.listExample(param);

        // ModelMap에 attribute 추가
        model.addAttribute("resultList", exampleList);
        model.addAttribute("param", param);

        // listExample.html 로 이동
        return "example/listExample";
    }

    /**
     * 등록
     * @param request
     * @param authentication
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/cms/example/insertExample.do")
    public String insertExample(HttpServletRequest request, Authentication authentication) throws Exception {

        // request 객체의 데이터를 DataMap에 담습니다.
        DataMap param = RequestUtil.getDataMap(request);

        // spring security를 이용한 세션정보 (등록자 정보 조회)
        AdminSecurity adminSecurity = (AdminSecurity) authentication.getPrincipal();
        param.put("ss_user_no", adminSecurity.getAdminSeq());

        // DB에 request 데이터 및 등록자 정보 저장
        exampleService.insertExample(param);

        // 목록 url로 redirect
        return "redirect:/cms/example/listExample.do";
    }

    /**
     * 상세
     * @param request
     * @param model
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/cms/example/detailExample.do")
    public String detailExample(HttpServletRequest request, ModelMap model) throws Exception {

        // request 객체의 데이터를 DataMap에 담습니다.
        DataMap param = RequestUtil.getDataMap(request);
        // DB에서 상세정보 조회
        DataMap resultMap = exampleService.detailExample(param);

        // ModelMap에 attribute 추가
        model.addAttribute("resultMap", resultMap);
        model.addAttribute("param", param);

        // detailExample.html 로 이동
        return "example/detailExample";
    }
}
