package com.ssafy.batch;

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

// 배치 기능 활성화
// @EnableBatchProcessing : Spring Batch의 여러 기능들을 사용할 수 있다.
@EnableBatchProcessing
@SpringBootApplication
@EnableJpaAuditing
@EnableCaching
@ComponentScan({"com.ssafy.core"})
@ComponentScan({"com.ssafy.batch"})
@EntityScan("com.ssafy.core")
@EnableJpaRepositories("com.ssafy.core")
public class SpringbatchApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringbatchApplication.class, args);
    }

}
