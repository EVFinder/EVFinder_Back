package com.example.backend.charger.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class TmapConfig {

    @Value("${tmap.app.key}")
    private String appKey;

    @Value("${tmap.api.base-url}")
    private String baseUrl;

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    public String getAppKey() {
        return appKey;
    }

    public String getBaseUrl() {
        return baseUrl;
    }
}
