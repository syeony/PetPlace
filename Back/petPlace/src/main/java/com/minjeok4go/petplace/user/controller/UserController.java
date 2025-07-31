// src/main/java/com/minjeok4go/petplace/user/controller/UserController.java
package com.minjeok4go.petplace.user.controller;

import com.minjeok4go.petplace.user.dto.UserSignupRequestDto;
import com.minjeok4go.petplace.user.dto.AutoLoginResponseDto;
import com.minjeok4go.petplace.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@Tag(name = "User API", description = "사용자 관련 API 명세서입니다.")
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // 회원가입
    @Operation(summary = "회원가입", description = "회원가입 합니다. 아직은 MVP 정도 ,,, 나중에 본인인증, 동네인증, 카카오계정 연동 예정.")
    @PostMapping("/signup")
    public ResponseEntity<String> signup(@RequestBody UserSignupRequestDto requestDto) {
        userService.signup(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body("회원가입 성공");
    }

    // 자동 로그인 API 추가
    @PostMapping("/auto-login")
    @Operation(summary = "자동 로그인", description = "JWT 토큰으로 자동 로그인을 수행합니다. Body 없이 토큰만으로 로그인됩니다.")
    @ApiResponse(responseCode = "200", description = "자동 로그인 성공")
    @ApiResponse(responseCode = "401", description = "토큰이 유효하지 않음")
    public ResponseEntity<AutoLoginResponseDto> autoLogin() {
        // SecurityContext에서 현재 인증된 사용자 정보 가져오기
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated()) {
            String userId = authentication.getName();

            // 사용자 정보 조회
            AutoLoginResponseDto response = userService.getAutoLoginInfo(userId);

            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    // 아이디 중복 체크
    @Operation(summary = "아이디 중복 체크", description = "입력한 아이디가 이미 사용 중인지 확인합니다.")
    @ApiResponse(responseCode = "200", description = "사용 가능한 아이디")
    @ApiResponse(responseCode = "409", description = "이미 존재하는 아이디 (중복)")
    @PostMapping("/check-userid")
    public ResponseEntity<String> checkUserIdDuplicate(@RequestParam("user_id") String userId) {
        boolean isDuplicate = userService.checkUserIdDuplicate(userId).isDuplicate();

        if (isDuplicate) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("이미 사용 중인 아이디입니다.");
        } else {
            return ResponseEntity.ok("사용 가능한 아이디입니다.");
        }
    }

    // 닉네임 중복 체크
    @Operation(summary = "닉네임 중복 체크", description = "입력한 닉네임이 이미 사용 중인지 확인합니다.")
    @ApiResponse(responseCode = "200", description = "사용 가능한 닉네임")
    @ApiResponse(responseCode = "409", description = "이미 존재하는 닉네임 (중복)")
    @PostMapping("/check-nickname")
    public ResponseEntity<String> checkNicknameDuplicate(@RequestParam("nickname") String nickname) {
        boolean isDuplicate = userService.checkNicknameDuplicate(nickname).isDuplicate();

        if (isDuplicate) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("이미 사용 중인 닉네임입니다.");
        } else {
            return ResponseEntity.ok("사용 가능한 닉네임입니다.");
        }
    }

    @GetMapping("/test-auth")
    @Operation(summary = "토큰 인증 테스트", description = "JWT 토큰으로 인증된 사용자 정보를 확인합니다.")
    public ResponseEntity<String> testAuth() {
        // 현재 인증된 사용자 정보 가져오기
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated()) {
            String username = authentication.getName();
            return ResponseEntity.ok("토큰 인증 성공! 사용자: " + username);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("인증 실패");
        }
    }
}

