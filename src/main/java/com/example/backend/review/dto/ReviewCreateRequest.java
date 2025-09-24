package com.example.backend.review.dto;

import lombok.Getter;

@Getter
public class ReviewCreateRequest {
    private String id;       // 충전소 고유번호 (statId)
    private String name;     // 충전소 이름
    private int rating;      // 별점 (1~5)
    private String content;  // 리뷰 내용
}