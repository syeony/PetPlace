// src/main/java/com/minjeok4go/petplace/user/service/UserService.java
package com.minjeok4go.petplace.user.service;

import com.minjeok4go.petplace.auth.dto.TokenDto;
import com.minjeok4go.petplace.user.domain.User;
import com.minjeok4go.petplace.user.dto.AutoLoginResponseDto;
import com.minjeok4go.petplace.user.dto.CheckDuplicateResponseDto;
import com.minjeok4go.petplace.user.dto.UserLoginRequestDto;
import com.minjeok4go.petplace.user.dto.UserSignupRequestDto;
import com.minjeok4go.petplace.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.minjeok4go.petplace.auth.jwt.JwtTokenProvider;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    // 회원가입
    public void signup(UserSignupRequestDto requestDto) {
        // 1. 아이디 중복 확인
        if (userRepository.existsByUserId(requestDto.getUserId())) {
            throw new IllegalArgumentException("이미 사용 중인 아이디입니다.");
        }
        
        // 1-2. 닉네임 중복 확인
        if (userRepository.existsByNickname(requestDto.getNickname())) {
            throw new IllegalArgumentException("이미 사용 중인 닉네임입니다.");
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

    // 아이디 중복 체크
    @Transactional(readOnly = true)
    public CheckDuplicateResponseDto checkUserIdDuplicate(String userId) {
        boolean isDuplicate = userRepository.existsByUserId(userId);
        String message = isDuplicate ? "이미 사용 중인 아이디입니다." : "사용 가능한 아이디입니다.";
        return new CheckDuplicateResponseDto(isDuplicate, message);
    }

    // 닉네임 중복 체크
    @Transactional(readOnly = true)
    public CheckDuplicateResponseDto checkNicknameDuplicate(String nickname) {
        boolean isDuplicate = userRepository.existsByNickname(nickname);
        String message = isDuplicate ? "이미 사용 중인 닉네임입니다." : "사용 가능한 닉네임입니다.";
        return new CheckDuplicateResponseDto(isDuplicate, message);
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

        return new TokenDto(
                accessToken,
                user.getUserId(),
                user.getNickname(),
                user.getUserImgSrc(),
                user.getLevel(),
                user.getDefaultPetId(),
                user.getRid()
        );
    }
    public AutoLoginResponseDto getAutoLoginInfo(String userId) {
        // 사용자 정보 조회
        Optional<User> userOptional = userRepository.findByUserId(userId);

        if (userOptional.isPresent()) {
            User user = userOptional.get();
            return AutoLoginResponseDto.success(user.getUserId(), user.getNickname());
        } else {
            throw new RuntimeException("사용자를 찾을 수 없습니다: " + userId);
        }
    }

}