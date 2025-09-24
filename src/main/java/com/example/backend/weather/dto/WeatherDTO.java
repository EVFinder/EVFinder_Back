package com.example.backend.weather.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WeatherDTO {
    private String description; // 날씨 설명
    private double temperature; // 기온
    private double feelsLike;   // 체감온도
    private int humidity;       // 습도
}