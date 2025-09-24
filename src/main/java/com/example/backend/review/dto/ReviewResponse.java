package com.example.backend.review.dto;

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
    private Timestamp createdAt; // Firestore Timestamp
}