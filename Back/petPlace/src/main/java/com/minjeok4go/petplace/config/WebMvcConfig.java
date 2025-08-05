package com.minjeok4go.petplace.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // http://서버:8080/images/xxx.jpg 요청시 실제 EC2의 해당 폴더에서 제공
        registry.addResourceHandler("/images/**")
                .addResourceLocations("file:/home/ubuntu/upload/images/");
    }
}
