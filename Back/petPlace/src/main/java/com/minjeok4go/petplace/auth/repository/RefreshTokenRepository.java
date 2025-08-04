package com.minjeok4go.petplace.auth.repository;

import com.minjeok4go.petplace.auth.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByRefreshToken(String refreshToken);

    Optional<RefreshToken> findByUserName(String userName);  // userId → userName

    void deleteByUserName(String userName);  // userId → userName

    void deleteByRefreshToken(String refreshToken);

//    // ✅ 고급 기능 - 나중에 필요할 때 사용
//    // 만료된 토큰 자동 정리용
//    @Modifying
//    @Query("DELETE FROM RefreshToken rt WHERE rt.expiresAt < :expiresAt")
//    int deleteByExpiresAtBefore(@Param("expiresAt") LocalDateTime expiresAt);
//
//    // 사용자별 활성 토큰 개수 확인 (보안 모니터링용)
//    long countByUserNameAndExpiresAtAfter(String userName, LocalDateTime expiresAt);  // userId → userName
}