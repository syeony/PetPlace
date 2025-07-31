package com.minjeok4go.petplace.auth.repository;

import com.minjeok4go.petplace.auth.domain.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByRefreshToken(String refreshToken);

    Optional<RefreshToken> findByUserId(String userId);

    void deleteByUserId(String userId);

    void deleteByRefreshToken(String refreshToken);
}
