package com.example.backend.community.dto;

import com.example.backend.common.json.TimestampSerializer;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.cloud.Timestamp;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PostSummaryResponse {
    private String postId;
    private String title;
    private String authorName;
    private String createdAt;
}