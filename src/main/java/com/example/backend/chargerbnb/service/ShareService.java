package com.example.backend.chargerbnb.service;

import com.example.backend.chargerbnb.dto.ReserveDTO;
import com.example.backend.chargerbnb.dto.ShareDTO;
import com.google.api.core.ApiFuture;
import com.google.cloud.Timestamp;
import com.google.cloud.firestore.*;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
        share.setOwnerUid(uid);
        
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

    public List<ShareDTO> getAllAvailableShares(double userLat, double userLon, double radiusKm)
            throws ExecutionException, InterruptedException {

        List<ShareDTO> results = new ArrayList<>();

        ApiFuture<QuerySnapshot> future = firestore.collectionGroup("share")
                .whereEqualTo("status", "available")
                .get();

        for (DocumentSnapshot doc : future.get().getDocuments()) {
            ShareDTO share = doc.toObject(ShareDTO.class);
            if (share != null) {
                share.setId(doc.getId());

            String path = doc.getReference().getPath(); 
            String[] parts = path.split("/");
            if (parts.length >= 2) {
                String ownerUid = parts[1]; // "users/{ownerUid}/share/{shareId}" → index 1 이 ownerUid
                share.setOwnerUid(ownerUid);
            }

                double distance = haversine(userLat, userLon, share.getLat(), share.getLon());
                if (distance <= radiusKm) {
                    results.add(share);
                }
            }
        }

        results.sort(Comparator.comparingDouble(
                s -> haversine(userLat, userLon, s.getLat(), s.getLon())
        ));

        return results;
    }

    // Haversine 공식 (킬로미터 단위)
    private double haversine(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // 지구 반경 km
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }


    // 상태 업데이트 기능
    public void updateShareStatus(String uid, String shareId, String status)
            throws ExecutionException, InterruptedException {
        DocumentReference shareRef =
                firestore.collection("users").document(uid).collection("share").document(shareId);

        shareRef.update("status", status).get();
    }

    //비활성화 일자 정리
    public int cleanupOldDisabledDates(String uid, String shareId)
            throws ExecutionException, InterruptedException {

        DocumentReference shareRef = firestore
                .collection("users")
                .document(uid)
                .collection("share")
                .document(shareId);

        DocumentSnapshot doc = shareRef.get().get();
        if (!doc.exists()) {
            throw new IllegalArgumentException("해당 공유 충전소가 존재하지 않습니다.");
        }

        List<String> disabledDates = (List<String>) doc.get("disabledDates");
        if (disabledDates == null || disabledDates.isEmpty()) {
            return 0;
        }

        LocalDate cutoffDate = LocalDate.now().minusMonths(2).withDayOfMonth(1); // 저저번달 1일 이전
        List<String> updated = new ArrayList<>();
        int removedCount = 0;

        for (String dateStr : disabledDates) {
            try {
                LocalDate date = LocalDate.parse(dateStr);
                if (!date.isBefore(cutoffDate)) {
                    updated.add(dateStr); // 유지
                } else {
                    removedCount++; // 삭제된 날짜
                }
            } catch (Exception ignored) {}
        }

        shareRef.update("disabledDates", updated).get();
        return removedCount;
    }

    //사용 가능한 일자 가져오기
    public Map<String, Object> getAvailability(String uid, String shareId)
        throws ExecutionException, InterruptedException {

    DocumentReference shareRef = firestore
            .collection("users")
            .document(uid)
            .collection("share")
            .document(shareId);

    DocumentSnapshot doc = shareRef.get().get();
        if (!doc.exists()) {
            throw new IllegalArgumentException("해당 공유 충전소가 존재하지 않습니다.");
        }

        Map<String, Object> data = new HashMap<>();
        data.put("availableDays", doc.get("availableDays"));
        data.put("availableHours", doc.get("availableHours"));
        data.put("disabledDates", doc.get("disabledDates"));

        return data;
    }

    //이용 불가한 일자 추가
    public void addDisabledDates(String uid, String shareId, List<String> newDates)
        throws ExecutionException, InterruptedException {

        DocumentReference shareRef = firestore.collection("users")
                .document(uid)
                .collection("share")
                .document(shareId);

        DocumentSnapshot doc = shareRef.get().get();
        if (!doc.exists()) {
            throw new IllegalArgumentException("공유 충전소를 찾을 수 없습니다.");
        }

        List<String> existing = (List<String>) doc.get("disabledDates");
        if (existing == null) existing = new ArrayList<>();

        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        for (String d : newDates) {
            try {
                LocalDate date = LocalDate.parse(d, formatter);

                if (date.isBefore(today)) {
                    throw new IllegalArgumentException("과거 날짜(" + d + ")는 추가할 수 없습니다.");
                }

                if (!existing.contains(d)) {
                    existing.add(d);
                }
            } catch (Exception e) {
                throw new IllegalArgumentException("잘못된 날짜 형식입니다: " + d + " (yyyy-MM-dd 형식이어야 합니다.)");
            }
        }

        shareRef.update("disabledDates", existing).get();
    }

    // 이용 불가한 일자 삭제
    public int removeDisabledDates(String uid, String shareId, List<String> datesToRemove)
        throws ExecutionException, InterruptedException {

        DocumentReference shareRef = firestore.collection("users")
                .document(uid)
                .collection("share")
                .document(shareId);

        DocumentSnapshot doc = shareRef.get().get();
        if (!doc.exists()) throw new IllegalArgumentException("공유 충전소를 찾을 수 없습니다.");

        List<String> existing = (List<String>) doc.get("disabledDates");
        if (existing == null) existing = new ArrayList<>();

        int beforeSize = existing.size();

        // 실제로 존재하는 항목만 삭제
        existing.removeAll(datesToRemove);

        int removedCount = beforeSize - existing.size();

        // Firestore 업데이트
        shareRef.update("disabledDates", existing).get();

        if (removedCount == 0) {
            throw new IllegalStateException("삭제된 항목이 없습니다. 요청한 날짜가 목록에 존재하지 않습니다.");
        }

        return removedCount;
    }
}
