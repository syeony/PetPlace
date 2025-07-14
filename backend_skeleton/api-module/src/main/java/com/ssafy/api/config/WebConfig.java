package com.ssafy.api.config;


import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.PathResourceResolver;

import java.util.Arrays;
import java.util.List;

@Log4j2
@Configuration
public class WebConfig implements WebMvcConfigurer {

    // 이미지 실제 경로
    private final String uploadImagesRootPath;
    // 클라이언트의 이미지 요청 url (정적 리소스 설정 커스터마이징)
    private final String uploadImagesReqPath;

    public WebConfig(
            @Value("${spring.servlet.multipart.location}") String uploadImagesRootPath
            , @Value("${image.handler.url}") String uploadImagesReqPath
    ){

        this.uploadImagesRootPath = uploadImagesRootPath;
        this.uploadImagesReqPath = uploadImagesReqPath;
    }

    @Override
    public void addResourceHandlers (ResourceHandlerRegistry registry){

        List<String> imageFolders = Arrays.asList("studyclass","user","temp");
        for(String imageFolder : imageFolders) {
            registry.addResourceHandler(uploadImagesReqPath + "/" +imageFolder +"/**")
                    .addResourceLocations("file:///" + uploadImagesRootPath + "/" + imageFolder +"/")
                    .setCachePeriod(3600)
                    .resourceChain(true)
                    .addResolver(new PathResourceResolver());
        }
    }
}
