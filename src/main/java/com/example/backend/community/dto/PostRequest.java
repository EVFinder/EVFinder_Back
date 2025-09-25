package com.example.backend.community.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PostRequest {
    private String title;   // 게시글 제목
    private String content; // 게시글 내용
}
