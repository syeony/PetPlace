package com.minjeok4go.petplace.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.*;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // 클라이언트가 연결할 엔드포인트
        registry.addEndpoint("/ws/chat")
                .setAllowedOriginPatterns("*") // 개발시 전체 허용, 운영시 수정 권장
                .withSockJS();                 // JS 클라이언트도 지원
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // 서버에서 클라이언트로 메시지 전달 주소 (subscribe)
        registry.enableSimpleBroker("/topic"); // /topic/roomId 식으로 구독 가능 ("특정 채널"의 메세지를 실시간으로 받음)
        // 클라이언트에서 서버로 메시지 보낼 때 prefix
        registry.setApplicationDestinationPrefixes("/app"); // /app/xxx
    }
}
