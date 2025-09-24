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
        Firestore db = firestore;

        share.setCreatedAt(Timestamp.now());
        
        if (share.getStatus() == null) {
            share.setStatus("available");
        }

         DocumentReference docRef = db.collection("users")
            .document(uid)
            .collection("share")
            .document();

        share.setId(docRef.getId()); // DTO에 수동으로 ID 세팅

        // Firestore에 저장
        docRef.set(share).get();

        return docRef.getId();
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

    // 예약 가능한 모든 충전소 조회
    public List<ShareDTO> getAllAvailableShares() throws ExecutionException, InterruptedException {
        Firestore db = firestore;

        // status = available인 모든 share 조회
        ApiFuture<QuerySnapshot> future = db.collectionGroup("share")
                .whereEqualTo("status", "available")
                .get();

        List<QueryDocumentSnapshot> documents = future.get().getDocuments();
        List<ShareDTO> shares = new ArrayList<>();

        for (QueryDocumentSnapshot doc : documents) {
            ShareDTO share = doc.toObject(ShareDTO.class);

            // 문서 id -> DTO
            share.setId(doc.getId());

            shares.add(share);
        }

        return shares;
    }

    // 상태 업데이트 기능
    public void updateShareStatus(String uid, String shareId, String status)
            throws ExecutionException, InterruptedException {
        DocumentReference shareRef =
                firestore.collection("users").document(uid).collection("share").document(shareId);

        shareRef.update("status", status).get();
    }
}
