// src/main/java/com/minjeok4go/petplace/user/controller/UserController.java
package com.minjeok4go.petplace.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.minjeok4go.petplace.auth.dto.TokenDto;
import com.minjeok4go.petplace.user.dto.CheckDuplicateResponseDto;
import com.minjeok4go.petplace.user.dto.UserLoginRequestDto;
import com.minjeok4go.petplace.user.dto.UserSignupRequestDto;
import com.minjeok4go.petplace.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "User API", description = "사용자 관련 API 명세서입니다.") // 1. API 그룹 설정
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // 회원가입
    @PostMapping("/signup")
    public ResponseEntity<String> signup(@RequestBody UserSignupRequestDto requestDto) {
        userService.signup(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body("회원가입 성공");
    }

    // 로그인
    @PostMapping("/login")
    public ResponseEntity<TokenDto> login(@RequestBody UserLoginRequestDto requestDto) {
        TokenDto tokenDto = userService.login(requestDto);
        return ResponseEntity.ok(tokenDto);
    }

    // 아이디 중복 체크
    @Operation(summary = "아이디 중복 체크", description = "입력한 아이디가 이미 사용 중인지 확인합니다.") // 2. API 설명 추가
    @ApiResponse(responseCode = "200", description = "확인 성공 (true: 중복, false: 사용 가능)") // 3. 응답 설명 추가
    @GetMapping("/check-userid/{userId}")
    public ResponseEntity<CheckDuplicateResponseDto> checkUserIdDuplicate(@PathVariable String userId) {
        CheckDuplicateResponseDto response = userService.checkUserIdDuplicate(userId);
        return ResponseEntity.ok(response);
    }

    // 닉네임 중복 체크
    @Operation(summary = "닉네임 중복 체크", description = "입력한 닉네임이 이미 사용 중인지 확인합니다.")
    @ApiResponse(responseCode = "200", description = "확인 성공 (true: 중복, false: 사용 가능)")
    @GetMapping("/check-nickname/{nickname}")
    public ResponseEntity<CheckDuplicateResponseDto> checkNicknameDuplicate(@PathVariable String nickname) {
        CheckDuplicateResponseDto response = userService.checkNicknameDuplicate(nickname);
        return ResponseEntity.ok(response);
    }
}