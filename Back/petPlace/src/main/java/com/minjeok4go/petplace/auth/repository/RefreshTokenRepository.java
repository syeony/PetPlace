package com.minjeok4go.petplace.auth.repository;

import com.minjeok4go.petplace.auth.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    //  리프레시 토큰 문자열로 RefreshToken 엔티티 찾기
    // 토큰 갱신이나 검증할 때 사용
    Optional<RefreshToken> findByRefreshToken(String refreshToken);

    //  사용자 ID로 RefreshToken 엔티티 찾기
    // 특정 사용자의 기존 토큰이 있는지 확인할 때 사용 (로그인 시)
    Optional<RefreshToken> findByUserId(Long userId);

    //  사용자 ID로 해당 사용자의 모든 RefreshToken 삭제
    // 로그아웃할 때 사용
    void deleteByUserId(Long userId);

    //  특정 리프레시 토큰 문자열로 RefreshToken 삭제
    // 토큰 갱신 후 기존 토큰 삭제할 때 사용
    void deleteByRefreshToken(String refreshToken);

    List<RefreshToken> userId(Long userId);

    // ✅ 고급 기능 - 나중에 필요할 때 사용
    // 🧹 만료된 토큰들을 일괄 삭제하는 메서드 (스케줄링으로 주기적 정리용)
    // @Modifying
    // @Query("DELETE FROM RefreshToken rt WHERE rt.expiresAt < :expiresAt")
    // int deleteByExpiresAtBefore(@Param("expiresAt") LocalDateTime expiresAt);

    // 📊 특정 사용자의 활성 토큰 개수 확인 (보안 모니터링용)
    // 한 사용자가 너무 많은 기기에서 로그인했는지 체크
    // long countByUserIdAndExpiresAtAfter(Long userId, LocalDateTime expiresAt);
}