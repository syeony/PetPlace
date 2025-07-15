package com.ssafy.core.repository;

import com.ssafy.core.entity.EmailAuth;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EmailAuthRepository extends JpaRepository<EmailAuth, Long> {
    // 가장 최근에 발급된 인증 코드를 꺼내오기 위한 메서드
    Optional<EmailAuth> findTopByEmailAndAuthNumOrderByCreatedAtDesc(String email, String authNum);
}