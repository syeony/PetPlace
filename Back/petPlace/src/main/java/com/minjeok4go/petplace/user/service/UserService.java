// src/main/java/com/minjeok4go/petplace/user/service/UserService.java
package com.minjeok4go.petplace.user.service;

import com.minjeok4go.petplace.auth.dto.TokenDto;
import com.minjeok4go.petplace.user.domain.User;
import com.minjeok4go.petplace.user.dto.UserLoginRequestDto;
import com.minjeok4go.petplace.user.dto.UserSignupRequestDto;
import com.minjeok4go.petplace.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.minjeok4go.petplace.auth.jwt.JwtTokenProvider;
import org.springframework.security.crypto.password.PasswordEncoder;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder; // ⚠️
    private final JwtTokenProvider jwtTokenProvider; // ⚠️

    // 회원가입
    public void signup(UserSignupRequestDto requestDto) {
        // 1. 아이디 중복 확인
        if (userRepository.findByUserId(requestDto.getUserId()).isPresent()) {
            throw new IllegalArgumentException("이미 사용 중인 아이디입니다.");
        }
        // 2. 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(requestDto.getPassword());

        // 3. 사용자 정보로 User 엔티티 생성
        User user = User.builder()
                .userId(requestDto.getUserId())
                .password(encodedPassword) // 암호화된 비밀번호 저장
                .name(requestDto.getName())
                .nickname(requestDto.getNickname())
                .rid(requestDto.getRid())
                .ci(requestDto.getCi())
                .phoneNumber(requestDto.getPhoneNumber())
                .gender(requestDto.getGender())
                .birthday(requestDto.getBirthday())
                .build();

        // 4. 데이터베이스에 저장
        userRepository.save(user);
    }

    // 로그인
    @Transactional(readOnly = true)
    public TokenDto login(UserLoginRequestDto requestDto) {
        // 1. 아이디로 사용자 조회
        User user = userRepository.findByUserId(requestDto.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("가입되지 않은 아이디입니다."));

        // 2. 비밀번호 일치 여부 확인
        if (!passwordEncoder.matches(requestDto.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        // 3. JWT 토큰 생성
        String accessToken = jwtTokenProvider.createToken(user.getUserId());

        return new TokenDto(accessToken);
    }
}