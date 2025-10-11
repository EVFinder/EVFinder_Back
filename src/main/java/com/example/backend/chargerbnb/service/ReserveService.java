package com.example.backend.chargerbnb.service;

import com.example.backend.chargerbnb.dto.ReserveDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.core.ApiFuture;
import com.google.cloud.Timestamp;
import com.google.cloud.firestore.*;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@Service
public class ReserveService {

    private final Firestore firestore;

    public ReserveService(Firestore firestore) {
        this.firestore = firestore;
    }

    // 예약 추가 (겹침 검사 + share 상태 확인)
    public String addReserve(String uid, ReserveDTO reserve) throws ExecutionException, InterruptedException {
        Firestore db = firestore;

        // 1. 해당 share 의 상태 확인
        DocumentReference shareRef = db.collection("users")
                .document(reserve.getOwnerUid())   // 공유 주인(owner)의 uid
                .collection("share")
                .document(reserve.getShareId());

        DocumentSnapshot shareDoc = shareRef.get().get();
        if (!shareDoc.exists()) {
            throw new IllegalArgumentException("해당 shareId가 존재하지 않습니다.");
        }

        String status = shareDoc.getString("status");
        if (!"available".equals(status)) {
            throw new IllegalStateException("해당 충전기는 현재 예약이 불가능합니다. (status=" + status + ")");
        }

        // 2. 해당 shareId 의 기존 예약들과 겹침 검사
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

        // 3. 시간이 겹치지 않으면 예약 등록
         DocumentReference reserveRef = db.collection("users")
            .document(uid)
            .collection("reserve")
            .document();

        reserve.setId(reserveRef.getId());
        reserve.setCreatedAt(Timestamp.now());

        // ApiFuture<DocumentReference> newReserve =
        //         db.collection("users").document(uid).collection("reserve").add(reserve);
        reserveRef.set(reserve).get();

        // 4. 현재 시간 기준으로 상태 업데이트
        Timestamp now = Timestamp.now();
        if (now.compareTo(reserve.getStartTime()) >= 0 && now.compareTo(reserve.getEndTime()) <= 0) {
            updateShareStatus(reserve.getOwnerUid(), reserve.getShareId(), "reserved");
        }

        return reserveRef.getId();
    }

    // // 예약 추가 (겹침 검사 포함)
    // public String addReserve(String uid, ReserveDTO reserve) throws ExecutionException, InterruptedException {
    //     Firestore db = firestore;

    //     // 해당 shareId의 기존 예약들 불러오기
    //     ApiFuture<QuerySnapshot> future = db.collectionGroup("reserve")
    //             .whereEqualTo("shareId", reserve.getShareId())
    //             .get();

    //     List<QueryDocumentSnapshot> existingReservations = future.get().getDocuments();

    //     for (QueryDocumentSnapshot doc : existingReservations) {
    //         ReserveDTO existing = doc.toObject(ReserveDTO.class);
    //         if (reserve.getStartTime().compareTo(existing.getEndTime()) < 0 &&
    //             reserve.getEndTime().compareTo(existing.getStartTime()) > 0) {
    //             throw new IllegalStateException("이미 예약된 시간과 겹칩니다.");
    //         }
    //     }

    //     // 시간이 겹치지 않으면 예약 등록
    //     reserve.setCreatedAt(Timestamp.now());
    //     ApiFuture<DocumentReference> newReserve =
    //             db.collection("users").document(uid).collection("reserve").add(reserve);

    //     // 현재 시간 기준으로 상태 업데이트
    //     Timestamp now = Timestamp.now();
    //     if (now.compareTo(reserve.getStartTime()) >= 0 && now.compareTo(reserve.getEndTime()) <= 0) {
    //         updateShareStatus(reserve.getOwnerUid(), reserve.getShareId(), "reserved");
    //     }

    //     return newReserve.get().getId();
    // }

    // 예약 조회
    public List<Map<String, Object>> getReservesByUser(String uid) throws ExecutionException, InterruptedException {
        Firestore db = firestore;

        // 유저의 예약 목록 가져오기
        ApiFuture<QuerySnapshot> future =
                db.collection("users").document(uid).collection("reserve").get();

        List<QueryDocumentSnapshot> docs = future.get().getDocuments();
        List<Map<String, Object>> results = new ArrayList<>();

        for (QueryDocumentSnapshot doc : docs) {
            
            ReserveDTO reserve = doc.toObject(ReserveDTO.class);
            reserve.setId(doc.getId());

            // DTO -> Map 변환
            Map<String, Object> data = new HashMap<>();
            data.putAll(new ObjectMapper().convertValue(reserve, Map.class));

            String ownerUid = reserve.getOwnerUid();
            String shareId = reserve.getShareId();

            if (ownerUid != null && shareId != null) {
                DocumentReference shareRef = db.collection("users")
                        .document(ownerUid)
                        .collection("share")
                        .document(shareId);

                DocumentSnapshot shareDoc = shareRef.get().get();
                if (shareDoc.exists()) {
                    Map<String, Object> shareData = shareDoc.getData();
                    if (shareData != null) {
                        data.put("stationName", shareData.get("stationName"));
                        data.put("address", shareData.get("address"));
                        data.put("ownerName", shareData.get("hostName"));
                        data.put("ownerContact", shareData.get("hostContact"));
                        data.put("pricePerHour", shareData.get("pricePerHour"));
                    }
                }
            }
            results.add(data);
        }
        return results;
    }

    // 예약 취소
    public void cancelReserve(String uid, String reserveId) throws ExecutionException, InterruptedException {
        DocumentReference reserveRef =
                firestore.collection("users").document(uid).collection("reserve").document(reserveId);

        DocumentSnapshot snapshot = reserveRef.get().get();
        if (!snapshot.exists()) {
            throw new IllegalArgumentException("예약이 존재하지 않습니다.");
        }

        ReserveDTO reserve = snapshot.toObject(ReserveDTO.class);

        // 예약 삭제
        reserveRef.delete().get();

        // share 상태를 available로 되돌리기
        if (reserve != null) {
            updateShareStatus(reserve.getOwnerUid(), reserve.getShareId(), "available");
        }
    }

    // 예약 날짜 수정
    public void updateReserve(String uid, String reserveId, ReserveDTO reserve)
            throws ExecutionException, InterruptedException {

        DocumentReference reserveRef =
                firestore.collection("users").document(uid).collection("reserve").document(reserveId);

        DocumentSnapshot snapshot = reserveRef.get().get();
        if (!snapshot.exists()) {
            throw new IllegalArgumentException("예약이 존재하지 않습니다.");
        }

        ReserveDTO existing = snapshot.toObject(ReserveDTO.class);
        if (existing == null) {
            throw new IllegalArgumentException("예약 데이터를 불러올 수 없습니다.");
        }

        Timestamp newStart = reserve.getStartTime();
        Timestamp newEnd   = reserve.getEndTime();

        // 겹침 검사
        ApiFuture<QuerySnapshot> future = firestore.collectionGroup("reserve")
                .whereEqualTo("shareId", existing.getShareId())
                .get();

        for (QueryDocumentSnapshot doc : future.get().getDocuments()) {
            if (doc.getId().equals(reserveId)) continue; // 자기 자신은 제외
            ReserveDTO other = doc.toObject(ReserveDTO.class);

            if (newStart.compareTo(other.getEndTime()) < 0 &&
                newEnd.compareTo(other.getStartTime()) > 0) {
                throw new IllegalStateException("이미 예약된 시간과 겹칩니다.");
            }
        }

        // Firestore에 수정 반영
        reserveRef.update("startTime", newStart, "endTime", newEnd).get();

        // 상태 갱신
        Timestamp now = Timestamp.now();
        if (now.compareTo(newStart) >= 0 && now.compareTo(newEnd) <= 0) {
            updateShareStatus(existing.getOwnerUid(), existing.getShareId(), "reserved");
        } else {
            updateShareStatus(existing.getOwnerUid(), existing.getShareId(), "available");
        }
    }

    public List<ReserveDTO> getReservesByShare(String shareId) throws ExecutionException, InterruptedException {
        Firestore db = firestore;

        ApiFuture<QuerySnapshot> future = db.collectionGroup("reserve")
                .whereEqualTo("shareId", shareId)
                .get();

        List<QueryDocumentSnapshot> documents = future.get().getDocuments();
        List<ReserveDTO> reserves = new ArrayList<>();

        for (QueryDocumentSnapshot doc : documents) {
            ReserveDTO reserve = doc.toObject(ReserveDTO.class);

            reserve.setId(doc.getId());

            reserves.add(reserve);
        }

        return reserves;
    }

    // share 상태 업데이트 (ownerUid 기반)
    private void updateShareStatus(String ownerUid, String shareId, String status)
            throws ExecutionException, InterruptedException {
        DocumentReference shareRef =
                firestore.collection("users").document(ownerUid).collection("share").document(shareId);

        DocumentSnapshot snapshot = shareRef.get().get();
        if (!snapshot.exists()) {
            throw new IllegalArgumentException("해당 uid(" + ownerUid + ") 안에 shareId(" + shareId + ") 문서가 없습니다.");
        }

        shareRef.update("status", status).get();
    }
}
