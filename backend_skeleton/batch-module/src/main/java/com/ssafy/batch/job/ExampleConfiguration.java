package com.ssafy.batch.job;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Job 예제 
 */
@Slf4j // log 사용을 위한 lombok 어노테이션
@RequiredArgsConstructor // Constructor DI를 위한 lombok 어노테이션
@Configuration // Spring Batch의 모든 Job은 @Configuration으로 등록해서 사용
public class ExampleConfiguration {
    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    // exampleJob 이란 이름의 Batch Job 생성
    @Bean
    public Job exampleJob(){
        return jobBuilderFactory.get("exampleJob")
                .start(exampleStep())
                .build();
    }

    // Step 생성
    @Bean
    public Step exampleStep(){
        return stepBuilderFactory.get("exampleStep")
                .tasklet((contribution, chunkContext) -> {
                    log.info(">>>>> This is exampleStep");
                    // 수행할 로직 작성

                    return RepeatStatus.FINISHED;
                })
                .build();
    }
}
