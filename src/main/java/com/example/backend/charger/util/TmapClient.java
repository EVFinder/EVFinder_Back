package com.example.backend.charger.util;

import com.example.backend.charger.config.TmapConfig;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
public class TmapClient {

    private final RestTemplate restTemplate;
    private final TmapConfig tmapConfig;

    public TmapClient(RestTemplate restTemplate, TmapConfig tmapConfig) {
        this.restTemplate = restTemplate;
        this.tmapConfig = tmapConfig;
    }
    // 전기차 충전소 검색
    public String getNearbyChargers(double lat, double lon) {
        String categories = URLEncoder.encode("EV충전소", StandardCharsets.UTF_8);

        String url = tmapConfig.getBaseUrl() + "/tmap/pois/search/around"
                + "?version=1"
                + "&centerLat=" + lat
                + "&centerLon=" + lon
                + "&radius=2"
                + "&categories=" + categories
                + "&reqCoordType=WGS84GEO"
                + "&resCoordType=WGS84GEO"
                + "&page=1"
                + "&count=20";

        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");
        headers.set("appKey", tmapConfig.getAppKey());

        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
                URI.create(url),
                HttpMethod.GET,
                entity,
                String.class
        );

        System.out.println("Tmap Raw Response: " + response.getBody());
        return response.getBody();
    }

}
