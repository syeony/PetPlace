package com.minjeok4go.petplace.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
    info = @Info(
        title = "Pet Place API",
        version = "1.0.0",
        description = """
            # Pet Place API 문서
            
            반려동물을 위한 종합 커뮤니티 플랫폼 **Pet Place**의 API 문서입니다.
            
            ## 📋 주요 기능
            - **AI 실종 반려동물 매칭 시스템** - 실종된 반려동물을 AI로 찾아주는 서비스
            - **돌봄 서비스 매칭** - 반려동물 산책, 임시보호, 응급상황 도움 서비스
            - **커뮤니티** - 반려동물 자랑, 정보공유, 동네 모임
            - **펫 호텔 예약** - 반려동물 호텔 검색 및 예약
            - **결제 시스템** - 포트원 연동 결제 서비스
            
            ## 🔐 인증 방식
            - **JWT Bearer Token** 사용
            - Authorization 헤더에 `Bearer {토큰}` 형식으로 전달
            - 로그인 시 Access Token과 Refresh Token 발급
            
            ## 📝 응답 형식
            모든 API는 다음과 같은 공통 응답 형식을 사용합니다:
            ```json
            {
              "success": true,
              "message": "성공 메시지",
              "data": {} // 실제 데이터
            }
            ```
            
            ## 📞 문의사항
            - **팀**: SSAFY 2학기 공통 PJT D104팀
            - **개발자**: 김민, 송정현, 오승연, 이도형, 정유진, 조경호
            """,
        contact = @Contact(
            name = "Pet Place 개발팀",
            email = "petplace@ssafy.com"
        )
    ),
    servers = {
        @Server(url = "https://api.petplace.com", description = "Production Server"),
        @Server(url = "http://localhost:8080", description = "Development Server")
    }
)
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .components(new Components()
                        .addSecuritySchemes("bearerAuth",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("JWT 토큰을 입력하세요. 'Bearer ' 접두사는 자동으로 추가됩니다.")
                        )
                )
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
    }
}
