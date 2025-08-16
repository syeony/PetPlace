package com.minjeok4go.petplace;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableCaching
@ConfigurationPropertiesScan(basePackages = "com.minjeok4go.petplace")
@SpringBootApplication
@EnableJpaAuditing
public class PetPlaceApplication {

    public static void main(String[] args) {
        SpringApplication.run(PetPlaceApplication.class, args);
    }

}
