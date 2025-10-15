package com.example.backend.chargerbnb.dto;

import java.util.List;
import java.util.Map;

import com.example.backend.common.json.TimestampDeserializer;
import com.example.backend.common.json.TimestampSerializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.cloud.Timestamp;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ShareDTO {
    
    private String id; // 파이어베이스
    
    private String ownerUid;  // 공유 충전기 등록자

    private String address;
    private double lat;
    private double lon;

    private String hostName;    // 호스트 이름
    private String hostContact; // 호스트 연락처
    private String stationName; // 충전소 이름

    private String chargerType;   // 급속/완속
    private String power;         // 50kW, 100kW 등
    private int pricePerHour;     // 시간당 가격

    private String status;        // available / reserved / unavailable

    //요일, 시간, 날짜 추가
    private List<String> availableDays; // 요일 기준
    private Map<String, List<String>> availableHours; // 예: "MON": ["09:00-12:00", "13:00-18:00"]
    private List<String> disabledDates; // 특정 날짜 제외

    @JsonSerialize(using = TimestampSerializer.class)
    @JsonDeserialize(using = TimestampDeserializer.class)
    private Timestamp createdAt;
}