package com.example.backend.community.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CommentRequest {
    private String content;   // 댓글 내용
    private String parentId;  // 대댓글일 경우 부모 댓글 ID (없으면 null)
}
