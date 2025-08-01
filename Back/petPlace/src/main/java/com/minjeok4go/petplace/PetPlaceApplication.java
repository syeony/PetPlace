package com.minjeok4go.petplace;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class PetPlaceApplication {

    public static void main(String[] args) {
        SpringApplication.run(PetPlaceApplication.class, args);
    }

}
