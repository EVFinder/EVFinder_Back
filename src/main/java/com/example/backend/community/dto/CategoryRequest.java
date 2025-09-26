package com.example.backend.community.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CategoryRequest {
    private String name;        // 카테고리명
    private String description; // 카테고리 설명
}
