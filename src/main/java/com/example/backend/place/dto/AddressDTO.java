package com.example.backend.place.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// 좌표에서 주소 변환용 DTO 임니다.
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddressDTO {
    private String AddressName; // 도로명 주소
}
