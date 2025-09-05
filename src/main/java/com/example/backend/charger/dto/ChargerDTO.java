package com.example.backend.charger.dto;

import lombok.Data;
import java.util.List;

@Data
public class ChargerDTO {
    private String id;           // POI ID
    private String name;         // 충전소 명칭
    private String address;      // 주소 (roadName + buildingNo 등 합쳐서 구성)
    private double lat;          // 위도 (frontLat)
    private double lon;          // 경도 (frontLon)
//    private String tel;          // 전화번호 (telNo) *
//    private String upperAddrName;   // 시/도 *
//    private String middleAddrName;  // 시/군/구 *
//    private String lowerAddrName;   // 읍/면/동 *
//    private String roadName;        // 도로명 *
//    private String buildingNo1;     // 건물번호1 *
//    private String buildingNo2;     // 건물번호2 *

    private List<EvChargerDTO> evChargers; // 하위 충전기 목록
}
