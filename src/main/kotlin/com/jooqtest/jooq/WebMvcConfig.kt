package com.jooqtest.jooq

import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer


@Configuration
class WebMvcConfig :WebMvcConfigurer{
    override fun addCorsMappings(registry: CorsRegistry) {
        registry.addMapping("/**")      // 모든 주소에 대해서
            .allowedOriginPatterns("*") // 모든 출처(Origin) 허용
            .allowedMethods("*")        // GET, POST, OPTIONS 등 모든 메서드 허용
            .allowedHeaders("*")        // 모든 헤더 허용
            .allowCredentials(true)     // 쿠키/인증정보 포함 허용
    }
}