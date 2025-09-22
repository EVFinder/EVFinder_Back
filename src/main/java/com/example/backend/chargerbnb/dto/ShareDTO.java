package com.example.backend.chargerbnb.dto;

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
    private String id;
    private String address;
    private double lat;
    private double lon;

    private String chargerType;   // 급속/완속
    private String power;         // 50kW, 100kW 등
    private int pricePerHour;     // 시간당 가격

    private String status;        // available / reserved / unavailable

    @JsonSerialize(using = TimestampSerializer.class)
    @JsonDeserialize(using = TimestampDeserializer.class)
    private Timestamp createdAt;
}