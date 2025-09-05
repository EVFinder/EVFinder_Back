package com.example.backend.charger.dto;

import lombok.Data;

@Data
public class EvChargerDTO {
    private String operatorId;        // 운영사 코드
    private String operatorName;      // 운영사 이름
    private String stationId;         // 충전소 ID
    private String chargerId;         // 충전기 ID
    private String type;              // 충전기 타입 코드
    private String status;            // 상태 (2=대기중, 3=사용중, 9=점검중 등)
    private String powerType;         // 충전 방식 (급속/완속, 전력)
    private String chargingDateTime;  // 충전 시작 시각
    private String updateDateTime;    // 상태 갱신 시각
    private String isFast;            // 급속 여부 (Y/N)
    private String isAvailable;       // 사용 가능 여부 (Y/N)
}
