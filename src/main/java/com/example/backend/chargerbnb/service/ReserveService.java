package com.example.backend.chargerbnb.service;

import com.example.backend.chargerbnb.dto.ReserveDTO;
import com.google.api.core.ApiFuture;
import com.google.cloud.Timestamp;
import com.google.cloud.firestore.*;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ExecutionException;

@Service
public class ReserveService {

    private final Firestore firestore;

    public ReserveService(Firestore firestore) {
        this.firestore = firestore;
    }

    // 예약 추가 (겹침 검사 포함)
    public String addReserve(String uid, ReserveDTO reserve) throws ExecutionException, InterruptedException {
        Firestore db = firestore;

        // 해당 shareId의 기존 예약들 불러오기 (collectionGroup 사용)
        ApiFuture<QuerySnapshot> future = db.collectionGroup("reserve")
                .whereEqualTo("shareId", reserve.getShareId())
                .get();

        List<QueryDocumentSnapshot> existingReservations = future.get().getDocuments();

        for (QueryDocumentSnapshot doc : existingReservations) {
            ReserveDTO existing = doc.toObject(ReserveDTO.class);

            if (reserve.getStartTime().compareTo(existing.getEndTime()) < 0 &&
                reserve.getEndTime().compareTo(existing.getStartTime()) > 0) {
                throw new IllegalStateException("이미 예약된 시간과 겹칩니다.");
            }
        }

        // 시간이 겹치지 않으면 예약 등록
        reserve.setCreatedAt(Timestamp.now());
        ApiFuture<DocumentReference> newReserve =
                db.collection("users").document(uid).collection("reserve").add(reserve);

        // 현재 시간 기준으로 상태 업데이트
        Timestamp now = Timestamp.now();
        if (now.compareTo(reserve.getStartTime()) >= 0 && now.compareTo(reserve.getEndTime()) <= 0) {
            updateShareStatus(reserve.getShareId(), "reserved");
        }

        return newReserve.get().getId();
    }

    // 예약 조회
    public List<ReserveDTO> getReservesByUser(String uid) throws ExecutionException, InterruptedException {
        ApiFuture<QuerySnapshot> future =
                firestore.collection("users").document(uid).collection("reserve").get();

        return future.get().toObjects(ReserveDTO.class);
    }

    // share 상태 업데이트
    private void updateShareStatus(String shareId, String status) throws ExecutionException, InterruptedException {
        ApiFuture<QuerySnapshot> future = firestore.collectionGroup("share")
                .whereEqualTo(FieldPath.documentId(), shareId)
                .get();

        for (DocumentSnapshot doc : future.get().getDocuments()) {
            doc.getReference().update("status", status).get();
        }
    }
}
