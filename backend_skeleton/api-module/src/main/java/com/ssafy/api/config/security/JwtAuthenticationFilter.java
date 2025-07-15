//package com.ssafy.api.config.security;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.ssafy.api.service.common.CommonResult;
//import com.ssafy.core.exception.CUserNotFoundException;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.web.filter.GenericFilterBean;
//
//import jakarta.servlet.FilterChain;
//import jakarta.servlet.ServletException;
//import jakarta.servlet.ServletRequest;
//import jakarta.servlet.ServletResponse;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import java.io.IOException;
//import java.io.PrintWriter;
//
//public class JwtAuthenticationFilter extends GenericFilterBean {
//
//    private JwtTokenProvider jwtTokenProvider;
//
//    // Jwt Provier 주입
//    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider) {
//        this.jwtTokenProvider = jwtTokenProvider;
//    }
//
//    // Request로 들어오는 Jwt Token의 유효성을 검증(jwtTokenProvider.validateToken)하는 filter를 filterChain에 등록합니다.
//    @Override
//    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws IOException, ServletException {
//        String token = jwtTokenProvider.resolveToken((HttpServletRequest) request);
//
//
//        HttpServletRequest req = (HttpServletRequest) request;
//        HttpServletResponse res = (HttpServletResponse) response;
//        if (token != null && jwtTokenProvider.validateToken(token)) {
//            try {
//                Authentication auth = jwtTokenProvider.getAuthentication(token);
//                SecurityContextHolder.getContext().setAuthentication(auth);
//            } catch (CUserNotFoundException e) {
//                CommonResult result = new CommonResult();
//                result.setOutput(-1000);
//                result.setMsg("This member not exist");
//                res.setContentType("application/json");
//                ObjectMapper mapper = new ObjectMapper();
//                PrintWriter out = res.getWriter();
//                out.print(mapper.writeValueAsString(result));
//                out.flush();
//                return;
//            }
//        }
//        filterChain.doFilter(request, response);
//    }
//}
package com.ssafy.api.config.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssafy.api.service.common.CommonResult;
import com.ssafy.core.exception.CUserNotFoundException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final List<String> skipPaths = Arrays.asList("/api/sign/", "/api/auth/");

    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        // /api/sign/**, /api/auth/** 은 필터 건너뛴다
        return skipPaths.stream().anyMatch(path::startsWith);
    }
    public class CommonResult {
        private int output;
        private String msg;
        // (기존 필드·getter·setter 생략)

        // ① 인자를 바로 받을 수 있는 생성자 추가
        public CommonResult(int output, String msg) {
            this.output = output;
            this.msg = msg;
        }
    }
    @Override
    protected void doFilterInternal(HttpServletRequest req,
                                    HttpServletResponse res,
                                    FilterChain chain)
            throws ServletException, IOException {
        String token = jwtTokenProvider.resolveToken(req);
        if (token != null && jwtTokenProvider.validateToken(token)) {
            try {
                Authentication auth = jwtTokenProvider.getAuthentication(token);
                SecurityContextHolder.getContext().setAuthentication(auth);
            } catch (CUserNotFoundException e) {
                res.setContentType("application/json");
                CommonResult result = new CommonResult(-1000, "This member not exist");
                String body = new ObjectMapper().writeValueAsString(result);
                res.getWriter().print(body);
                return;
            }
        }
        chain.doFilter(req, res);
    }
}
