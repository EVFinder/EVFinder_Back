package com.example.backend.fcm.service;

import com.google.cloud.firestore.Firestore;
import com.google.api.core.ApiFuture;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FcmService {

    private final Firestore firestore;

    public void updateToken(String uid, String token) throws Exception {
        ApiFuture<?> writeResult = firestore.collection("users")
                .document(uid)
                .update("fcmToken", token);
        writeResult.get();
        System.out.println("(디버깅 용) uid: " + uid);
    }
}
