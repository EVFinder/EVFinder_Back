package com.example.backend.payment.service;

import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.messaging.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final Firestore firestore;

    public void sendCancelNotification(String ownerUid, String shareId) throws Exception {
        DocumentSnapshot userDoc = firestore.collection("users").document(ownerUid).get().get();
        String token = userDoc.getString("fcmToken");

        if (token == null || token.isEmpty()) {
            System.out.println(" FCM 토큰 없음: " + ownerUid);
            return;
        }

        Message message = Message.builder()
                .setToken(token)
                .setNotification(Notification.builder()
                        .setTitle("결제 취소 요청 발생")
                        .setBody("공유한 충전소(" + shareId + ")의 결제가 취소되었습니다.")
                        .build())
                .putData("type", "cancel")
                .build();

        String response = FirebaseMessaging.getInstance().send(message);
        System.out.println("FCM 알림 전송 완료: " + response);
    }
}