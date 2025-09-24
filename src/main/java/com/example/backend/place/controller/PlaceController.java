package com.example.backend.place.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.backend.place.dto.PlaceDTO;
import com.example.backend.place.service.PlaceService;

@RestController
@RequestMapping("/place")
public class PlaceController {

    private final PlaceService placeService;

    public PlaceController(PlaceService placeService) {
        this.placeService = placeService;
    }

    @GetMapping("/placelist")
    public ResponseEntity<?> findByKeyword(@RequestParam String query) {
        try {
            List<PlaceDTO> places = placeService.getPlaceListByKeyword(query);
            return ResponseEntity.ok(places);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }
}
