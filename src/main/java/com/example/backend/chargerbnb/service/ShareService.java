package com.example.backend.chargerbnb.service;

import com.example.backend.chargerbnb.dto.ShareDTO;
import com.google.api.core.ApiFuture;
import com.google.cloud.Timestamp;
import com.google.cloud.firestore.*;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Service
public class ShareService {

    private final Firestore firestore;

    public ShareService(Firestore firestore) {
        this.firestore = firestore;
    }

    // 공유 충전기 등록
    public String addShare(String uid, ShareDTO share) throws ExecutionException, InterruptedException {
        share.setCreatedAt(Timestamp.now());
        if (share.getStatus() == null) {
            share.setStatus("available");
        }

        ApiFuture<DocumentReference> future =
                firestore.collection("users").document(uid).collection("share").add(share);

        return future.get().getId();
    }

    // 내 공유 충전기 조회
    public List<ShareDTO> getSharesByUser(String uid) throws ExecutionException, InterruptedException {
        List<ShareDTO> list = new ArrayList<>();
        ApiFuture<QuerySnapshot> future =
                firestore.collection("users").document(uid).collection("share").get();

        for (DocumentSnapshot doc : future.get().getDocuments()) {
            ShareDTO share = doc.toObject(ShareDTO.class);
            if (share != null) {
                share.setId(doc.getId());
                list.add(share);
            }
        }
        return list;
    }
    // 상태 업데이트 기능
    public void updateShareStatus(String uid, String shareId, String status)
            throws ExecutionException, InterruptedException {
        DocumentReference shareRef =
                firestore.collection("users").document(uid).collection("share").document(shareId);

        shareRef.update("status", status).get();
    }
}
