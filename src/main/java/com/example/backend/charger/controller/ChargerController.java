package com.example.backend.charger.controller;

import com.example.backend.charger.dto.ChargerDTO;
import com.example.backend.charger.service.ChargerService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/charger")
public class ChargerController {

    private final ChargerService chargerService;

    public ChargerController(ChargerService chargerService) {
        this.chargerService = chargerService;
    }

    // 사용자 위도, 경도 기준 근처 충전소 반환 API
    @GetMapping("/nearby")
    public List<ChargerDTO> getNearbyChargers(
            @RequestParam double lat,
            @RequestParam double lon) {
        System.out.println("Received lat = " + lat + ", lon = " + lon);
        return chargerService.findNearbyChargers(lat, lon);
    }
}
