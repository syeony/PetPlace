package com.minjeok4go.petplace.auth.repository;

import com.minjeok4go.petplace.auth.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByRefreshToken(String refreshToken);

    Optional<RefreshToken> findByUserId(String userId);

    void deleteByUserId(String userId);

    void deleteByRefreshToken(String refreshToken);

    // ✅ 고급 기능은 나중에 필요할 때 주석 해제
    /*
    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.expiresAt < :expiresAt")
    int deleteByExpiresAtBefore(@Param("expiresAt") LocalDateTime expiresAt);

    long countByUserIdAndExpiresAtAfter(String userId, LocalDateTime expiresAt);
    */
}
