package com.minjeok4go.petplace;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
// 이걸로 바꿔 끼우면 바로 돌아갑니다 (동작 OK, 경고만 있음)
import org.springframework.boot.test.mock.mockito.MockBean;

@SpringBootTest
@ActiveProfiles("test")
class PetPlaceApplicationTests {
    @MockBean
    private com.google.firebase.messaging.FirebaseMessaging firebaseMessaging;

    @Test
    void contextLoads() {
        // 애플리케이션 컨텍스트가 정상적으로 로드되는지 테스트
    }

}
