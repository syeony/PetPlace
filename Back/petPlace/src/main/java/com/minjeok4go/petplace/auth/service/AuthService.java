
package com.minjeok4go.petplace.auth.service;

import com.minjeok4go.petplace.auth.dto.TokenDto;
import com.minjeok4go.petplace.auth.jwt.JwtTokenProvider;
import com.minjeok4go.petplace.user.domain.User;
import com.minjeok4go.petplace.user.dto.UserLoginRequestDto;
import com.minjeok4go.petplace.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenService refreshTokenService;

    public TokenDto login(UserLoginRequestDto requestDto) {
        // 1. 아이디로 사용자 조회 (UserService 위임)
        User user = userService.findByUserId(requestDto.getUserId());

        // 2. 비밀번호 일치 여부 확인
        if (!passwordEncoder.matches(requestDto.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        // 3. Access Token과 Refresh Token 생성
        String accessToken = jwtTokenProvider.createAccessToken(user.getUserId());
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getUserId());

        // 4. Refresh Token 저장
        refreshTokenService.saveOrUpdate(user.getUserId(), refreshToken);

        // 5. 토큰 및 사용자 정보 반환
        return TokenDto.of(
                accessToken,
                refreshToken,
                user.getUserId(),
                user.getNickname(),
                user.getUserImgSrc(),
                user.getLevel(),
                user.getDefaultPetId(),
                user.getRid()
        );
    }
}
