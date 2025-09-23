package com.example.backend.weather.controller;

import com.example.backend.weather.dto.WeatherDTO;
import com.example.backend.weather.service.WeatherService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/weather")
public class WeatherController {
    private final WeatherService weatherService;

    public WeatherController(WeatherService weatherService) {
        this.weatherService = weatherService;
    }

    @GetMapping
    public WeatherDTO getWeather(@RequestParam double lat, @RequestParam double lon) throws Exception{
        return weatherService.getWeather(lat, lon);
    }
}