package com.example.upload.global.springDoc;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(info = Info(title = "API 서버", version = "v1"))
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT"
)
class SpringDocConfig {

    @Bean
    fun groupApiV1(): GroupedOpenApi {
        return GroupedOpenApi.builder()
                .group("apiV1")
                .pathsToMatch("/api/v1/**")
                .build()
    }

    @Bean
    fun groupController(): GroupedOpenApi {
        return GroupedOpenApi.builder()
                .group("controller")
                .pathsToExclude("/api/**")
                .build()
    }
}
