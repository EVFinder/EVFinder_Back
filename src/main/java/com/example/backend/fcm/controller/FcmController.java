package com.example.backend.fcm.controller;

import com.example.backend.fcm.dto.FcmTokenRequest;
import com.example.backend.fcm.service.FcmService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/fcm")
@RequiredArgsConstructor
public class FcmController {

    private final FcmService fcmService;

    @PostMapping("/updateToken")
    public ResponseEntity<String> updateToken(@RequestBody FcmTokenRequest request) {
        try {
            fcmService.updateToken(request.getUid(), request.getToken());
            return ResponseEntity.ok("FCM 토큰 업데이트 완료");
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("FCM 토큰 업데이트 실패: " + e.getMessage());
        }
    }
}
