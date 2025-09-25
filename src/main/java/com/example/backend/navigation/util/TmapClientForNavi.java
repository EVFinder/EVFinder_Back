package com.example.backend.navigation.util;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.Value;

@Component
public class TmapClientForNavi {

    @Value("${tmap.app.key}")
    private String apiKey;

    private static final String BASE_URL =
            "https://apis.openapi.sk.com/tmap/routes?version=1&format=json";

    public String getNavigation(double startLat, double startLon, double endLat, double endLon) {
        String url = BASE_URL +
                "&appKey=" + apiKey +
                "&startX=" + startLon +
                "&startY=" + startLat +
                "&endX=" + endLon +
                "&endY=" + endLat +
                "&reqCoordType=WGS84GEO&resCoordType=WGS84GEO";

        RestTemplate restTemplate = new RestTemplate();
        return restTemplate.getForObject(url, String.class);
    }
}
