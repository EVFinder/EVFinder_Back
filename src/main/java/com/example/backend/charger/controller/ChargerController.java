package com.example.backend.charger.controller;

import com.example.backend.charger.dto.ChargerDTO;
import com.example.backend.charger.dto.EvChargerDTO;
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
        //System.out.println("Received lat = " + lat + ", lon = " + lon);
        return chargerService.findNearbyChargers(lat, lon);
    }
    // 사용자 위도, 경도 기준 근처 단일 충전소 반환 API
    @GetMapping("/nearbyOO")
    public List<ChargerDTO> getNearbyChargersOO(
            @RequestParam double lat,
            @RequestParam double lon) {
        //System.out.println("Received lat = " + lat + ", lon = " + lon);
        return chargerService.findNearbyChargersOO(lat, lon);
    }
    // 충전소만 반환(좌표에 해당하는, 단일 반환용)
    @GetMapping("/list")
    public List<ChargerDTO> getChargersOnly(
            @RequestParam double lat,
            @RequestParam double lon) {

        return chargerService.findNearbyChargers(lat, lon)
                .stream()
                .map(dto -> {
                    dto.setEvChargers(null); // 충전기 리스트는 제외
                    return dto;
                })
                .toList();
    }
    // 충전소만 반환(좌표에 해당하는, 단일 반환용)
    @GetMapping("/listOO")
    public List<ChargerDTO> getChargersOnlyOne(
            @RequestParam double lat,
            @RequestParam double lon) {

        return chargerService.findNearbyChargersOO(lat, lon)
                .stream()
                .map(dto -> {
                    dto.setEvChargers(null); // 충전기 리스트는 제외
                    return dto;
                })
                .toList();
    }
    // 모든 충전기 반환(해당 좌표의)
    @GetMapping("/evchargersOO")
    public List<EvChargerDTO> getAllEvChargers(
            @RequestParam double lat,
            @RequestParam double lon) {

        return chargerService.findNearbyChargersOO(lat, lon)
                .stream()
                .flatMap(dto -> dto.getEvChargers() != null
                        ? dto.getEvChargers().stream()
                        : java.util.stream.Stream.empty())
                .toList();
    }
}
