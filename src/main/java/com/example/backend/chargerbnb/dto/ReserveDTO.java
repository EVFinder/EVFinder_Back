package com.example.backend.chargerbnb.dto;

import com.example.backend.common.json.TimestampDeserializer;
import com.example.backend.common.json.TimestampSerializer;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.cloud.Timestamp;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReserveDTO {
    
    private String id;
    
    private String shareId;
    private String ownerUid; // 공유 충전기 주인 uid

    private String userName;
    private String userPNumber;

    @JsonSerialize(using = TimestampSerializer.class)
    @JsonDeserialize(using = TimestampDeserializer.class)
    private Timestamp startTime;

    @JsonSerialize(using = TimestampSerializer.class)
    @JsonDeserialize(using = TimestampDeserializer.class)
    private Timestamp endTime;

    @JsonSerialize(using = TimestampSerializer.class)
    @JsonDeserialize(using = TimestampDeserializer.class)
    private Timestamp createdAt;
}
