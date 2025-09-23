package com.example.backend.weather.service;

import com.example.backend.weather.dto.WeatherDTO;
import com.example.backend.weather.util.WeatherClient;
import org.springframework.stereotype.Service;

@Service
public class WeatherService {
    private final WeatherClient weatherClient;

    public WeatherService(WeatherClient weatherClient) {
        this.weatherClient = weatherClient;
    }

    public WeatherDTO getWeather(double lat, double lon) throws Exception{
        return weatherClient.getWeather(lat, lon);
    }
}
