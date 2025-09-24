package com.example.backend.review.dto;

import com.example.backend.common.json.TimestampSerializer;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.cloud.Timestamp;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ReviewResponse {
    private String reviewId;   // 리뷰 ID
    private String id;         // 충전소 고유번호
    private String name;       // 충전소 이름
    private String uid;        // 작성자 UID
    private String userName;   // 작성자 닉네임
    private int rating;        // 별점
    private String content;    // 내용

    @JsonSerialize(using = TimestampSerializer.class)
    private Timestamp createdAt; // 최초 작성 시각

    @JsonSerialize(using = TimestampSerializer.class)
    private Timestamp updatedAt; // 마지막 수정 시각
}