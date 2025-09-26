package com.example.backend.community.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CategoryResponse {
    private String categoryId;   // 카테고리 ID
    private String name;         // 카테고리명
    private String description;  // 설명
}
