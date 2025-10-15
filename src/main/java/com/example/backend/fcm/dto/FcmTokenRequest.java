package com.example.backend.fcm.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FcmTokenRequest {
    private String uid;     // 사용자 UID
    private String token;   // FCM 기기 토큰
}
