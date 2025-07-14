package com.ssafy.cms.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * MVC Configuration
 */
@Configuration
public class MvcConfig implements WebMvcConfigurer {

    private static final String[] CLASSPATH_RESOURCE_LOCATIONS = { "classpath:/static/", "classpath:/public/", "classpath:/"
        , "classpath:/resources/", "classpath:/META-INF/resources/", "classpath:/META-INF/resources/webjars/"};

    /**
     * 컨트롤러 로직 없이 바로 뷰를 리턴
     * 인덱스 페이지 설정
     * @param registry
     */
    public void addViewControllers(ViewControllerRegistry registry){
        registry.addViewController("/").setViewName("index");

        registry.setOrder(Ordered.HIGHEST_PRECEDENCE);
    }

    /**
     * 정적 리소스 설정
     * @param registry
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/**").addResourceLocations(CLASSPATH_RESOURCE_LOCATIONS);
    }
}
