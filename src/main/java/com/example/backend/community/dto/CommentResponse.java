package com.example.backend.community.dto;

import com.example.backend.common.json.TimestampSerializer;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.cloud.Timestamp;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CommentResponse {
    private String commentId;    // 댓글 ID
    private String content;      // 댓글 내용
    private String uid;          // 작성자 UID (내부 검증용)
    private String authorName;   // 작성자 이름

    private boolean isOwner;     // JWT.uid == comment.uid ? true : false
    private String parentId;     // 대댓글일 경우 부모 댓글 ID

    @JsonSerialize(using = TimestampSerializer.class)
    private Timestamp createdAt; // 최초 작성 시각

    @JsonSerialize(using = TimestampSerializer.class)
    private Timestamp updatedAt; // 마지막 수정 시각
}
