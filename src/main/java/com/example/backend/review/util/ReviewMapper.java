package com.example.backend.review.util;

import com.example.backend.review.dto.ReviewResponse;
import com.google.cloud.firestore.DocumentSnapshot;

public class ReviewMapper {

    public static ReviewResponse toResponse(DocumentSnapshot doc) {
        Long ratingValue = doc.getLong("rating");
        int rating = ratingValue != null ? ratingValue.intValue() : 0;

        return ReviewResponse.builder()
                .reviewId(doc.getString("reviewId"))
                .id(doc.getString("id"))
                .name(doc.getString("name"))
                .uid(doc.getString("uid"))
                .userName(doc.getString("userName"))
                .rating(rating)
                .content(doc.getString("content"))
                .createdAt(doc.getTimestamp("createdAt"))
                .updatedAt(doc.getTimestamp("updatedAt"))
                .build();
    }
}
