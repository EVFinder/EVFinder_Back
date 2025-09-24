package com.example.backend.favorite.dto;

import lombok.Data;
import java.util.List;

@Data
public class FavoriteRequest {
    private String uid;     // 사용자 uid
    private String id;      // 즐겨찾기 id (충전소 id)
    private String name;
    private String address;
    private double lat;
    private double lon;
    private List<ChargerInfoDto> chargers;
}