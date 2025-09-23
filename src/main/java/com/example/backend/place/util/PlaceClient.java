package com.example.backend.place.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.example.backend.place.dto.PlaceDTO;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class PlaceClient {

    @Value("${kakao.api-key}")
    private String kakaoApiKey;

    private final ObjectMapper mapper = new ObjectMapper();

    public List<PlaceDTO> searchPlaces(String query) throws Exception {
        String apiUrl = "https://dapi.kakao.com/v2/local/search/keyword.json?query="
                + URLEncoder.encode(query, StandardCharsets.UTF_8);

        HttpURLConnection conn = (HttpURLConnection) new URL(apiUrl).openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Authorization", "KakaoAK " + kakaoApiKey);
        conn.setRequestProperty("Accept", "application/json");

        try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
            String response = br.lines().collect(Collectors.joining());
            Map<String, Object> fullJson = mapper.readValue(response, Map.class);

            List<Map<String, Object>> documents = (List<Map<String, Object>>) fullJson.get("documents");

            if (documents == null || documents.isEmpty()) {
                throw new Exception("위치 정보를 찾을 수 없습니다.");
            }

            List<PlaceDTO> results = new ArrayList<>();
            int limit = Math.min(10, documents.size());

            for (int i = 0; i < limit; i++) {
                Map<String, Object> doc = documents.get(i);

                PlaceDTO dto = new PlaceDTO(
                        (String) doc.get("place_name"),
                        (String) doc.get("address_name"),
                        (String) doc.get("road_address_name"),
                        (String) doc.get("x"),
                        (String) doc.get("y")
                );
                results.add(dto);
            }
            return results;
        }
    }
}
