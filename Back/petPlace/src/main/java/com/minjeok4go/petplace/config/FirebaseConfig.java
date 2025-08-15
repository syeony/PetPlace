package com.minjeok4go.petplace.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.io.InputStream;

@Configuration
@Profile("!test") //
public class FirebaseConfig {

    // 1) 파일로 관리하는 경우: classpath 또는 파일 시스템 경로
    @Value("${firebase.credentials.location:classpath:firebase-service-account.json}")
    private Resource serviceAccount;

    @Bean
    public FirebaseApp firebaseApp() throws Exception {
        GoogleCredentials credentials;

        try (InputStream in = serviceAccount.getInputStream()) {
            credentials = GoogleCredentials.fromStream(in);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load Firebase service account from: "
                    + serviceAccount + " (check path/profiles)", e);
        }

        FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(credentials)
                .build();

        if (FirebaseApp.getApps().isEmpty()) {
            return FirebaseApp.initializeApp(options);
        }
        return FirebaseApp.getInstance();
    }

    @Bean
    public FirebaseMessaging firebaseMessaging(FirebaseApp app) {
        return FirebaseMessaging.getInstance(app);
    }
}
