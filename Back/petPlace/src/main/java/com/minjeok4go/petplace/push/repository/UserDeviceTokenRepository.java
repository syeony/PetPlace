package com.minjeok4go.petplace.push.repository;

import com.minjeok4go.petplace.push.entity.UserDeviceToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserDeviceTokenRepository extends JpaRepository<UserDeviceToken, Long> {

    List<UserDeviceToken> findAllByUserIdAndActiveTrue(Long userId);

    Optional<UserDeviceToken> findByUserIdAndToken(Long userId, String token);

    boolean existsByUserIdAndToken(Long userId, String token);
}
