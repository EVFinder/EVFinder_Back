package com.example.backend.weather.util;

import com.example.backend.weather.dto.WeatherDTO;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class WeatherClient {

    @Value("${weatherAPIkey}")
    private String apiKey; 

    private static final String BASE_URL =
            "https://api.openweathermap.org/data/2.5/weather?lat=%f&lon=%f&units=metric&appid=%s";

    private final ObjectMapper objectMapper = new ObjectMapper();

    public WeatherDTO getWeather(double lat, double lon) throws Exception {
        String url = String.format(BASE_URL, lat, lon, apiKey);

        RestTemplate restTemplate = new RestTemplate();
        String response = restTemplate.getForObject(url, String.class);

        JsonNode root = objectMapper.readTree(response);

        // JSON 파싱
        String main = root.path("weather").get(0).path("main").asText();
        String description = root.path("weather").get(0).path("description").asText();
        double temp = root.path("main").path("temp").asDouble();
        double feelsLike = root.path("main").path("feels_like").asDouble();
        int humidity = root.path("main").path("humidity").asInt();

        return new WeatherDTO(main ,description, temp, feelsLike, humidity);
    }
}