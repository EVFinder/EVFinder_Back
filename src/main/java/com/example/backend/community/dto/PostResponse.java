package com.example.backend.community.dto;

import com.example.backend.common.json.TimestampSerializer;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.cloud.Timestamp;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PostResponse {
    private String postId;      // 게시글 ID
    private String title;       // 제목
    private String content;     // 내용
    private String uid;         // 작성자 UID (내부 검증용)
    private String authorName;  // 작성자 이름 (users/{uid}.name)

    private int views;          // 조회수
    private int likes;          // 좋아요 수
    private boolean isOwner;    // JWT.uid == post.uid ? true : false

    private boolean isLiked;    // 현재 사용자가 좋아요 눌렀는지 여부

    @JsonSerialize(using = TimestampSerializer.class)
    private Timestamp createdAt; // 최초 작성 시각

    @JsonSerialize(using = TimestampSerializer.class)
    private Timestamp updatedAt; // 마지막 수정 시각
}
