package com.ssafy.api.controller;

import com.ssafy.api.dto.req.EmailCheckRequest;
import com.ssafy.api.dto.req.EmailSendRequest;
import com.ssafy.api.dto.res.GenericResponse;
import com.ssafy.core.service.EmailAuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final EmailAuthService authService;

    // 1) 인증코드 발송
    @PostMapping("/send/email")
    public GenericResponse sendEmail(@RequestBody @Valid EmailSendRequest req) {
        authService.sendCode(req.getEmail());
        return new GenericResponse(1, "인증코드 발송 성공");
    }

    // 2) 인증코드 확인
    @GetMapping("/check/email")
    public GenericResponse checkEmail(@Valid EmailCheckRequest req) {
        boolean ok = authService.verifyCode(req.getEmail(), req.getAuthNum());
        return new GenericResponse(ok ? 1 : 0,
                ok ? "인증 성공" : "인증 실패");
    }
}
