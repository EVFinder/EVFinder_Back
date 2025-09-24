package com.example.backend.place.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlaceDTO {
    private String placeName;       // 장소명
    private String addressName;     // 지번 주소
    private String roadAddressName; // 도로명 주소
    private String x;               // 경도
    private String y;               // 위도
}
