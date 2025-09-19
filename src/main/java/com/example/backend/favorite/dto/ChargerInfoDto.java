package com.example.backend.favorite.dto;

import lombok.Data;

@Data
public class ChargerInfoDto {
    private String stationId;
    private String chargerId;
    private String status;
    private String isAvailable;
}