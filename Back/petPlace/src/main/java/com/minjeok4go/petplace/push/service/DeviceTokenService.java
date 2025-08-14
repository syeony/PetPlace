package com.minjeok4go.petplace.push.service;

import com.minjeok4go.petplace.auth.service.AuthService;
import com.minjeok4go.petplace.push.dto.CreateTokenRequest;
import com.minjeok4go.petplace.push.dto.TokenResponse;
import com.minjeok4go.petplace.push.dto.UpdateTokenRequest;
import com.minjeok4go.petplace.push.entity.UserDeviceToken;
import com.minjeok4go.petplace.push.repository.UserDeviceTokenRepository;
import com.minjeok4go.petplace.user.entity.User;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DeviceTokenService {

    private final AuthService authService;

    private final UserDeviceTokenRepository userDeviceTokenRepository;

    @Transactional
    public TokenResponse register(String userId, CreateTokenRequest req) {

        User me = authService.getUserFromToken(userId);

        UserDeviceToken token = new UserDeviceToken(me.getId(), req);

        if (req.getToken().isBlank()) throw new IllegalArgumentException("token is blank");

        UserDeviceToken saved = userDeviceTokenRepository.findByUserIdAndToken(me.getId(), token.getToken())
            .map(t -> {
                t.refresh(req);
                userDeviceTokenRepository.flush();
                return t;
            })
            .orElseGet(() -> {
                try {
                    return userDeviceTokenRepository.saveAndFlush(token);
                } catch (DataIntegrityViolationException e) {
                    UserDeviceToken t = userDeviceTokenRepository
                            .findByUserIdAndToken(me.getId(), token.getToken())
                            .orElseThrow(() -> e);
                    t.refresh(req);
                    userDeviceTokenRepository.flush();
                    return t;
                }
            });

        return new TokenResponse(saved);
    }

    @Transactional
    public void unregister(String userId, String token) {

        User me = authService.getUserFromToken(userId);

        userDeviceTokenRepository.findByUserIdAndToken(me.getId(), token.trim())
                .ifPresent(UserDeviceToken::deactivate);
    }

    @Transactional
    public void deactivateAll(String userId) {

        User me = authService.getUserFromToken(userId);

        var tokens = userDeviceTokenRepository.findAllByUserIdAndActiveTrue(me.getId());
        tokens.forEach(UserDeviceToken::deactivate);
    }

    @Transactional
    public TokenResponse replaceToken(String userId, UpdateTokenRequest req) {

        unregister(userId, req.getOldToken());

        return register(userId, new CreateTokenRequest(req.getNewToken(), req.getAppVersion()));
    }

    public List<TokenResponse> list(String userId) {

        User me = authService.getUserFromToken(userId);

        return userDeviceTokenRepository.findAllByUserIdAndActiveTrue(me.getId())
                .stream().map(TokenResponse::new).toList();
    }
}
