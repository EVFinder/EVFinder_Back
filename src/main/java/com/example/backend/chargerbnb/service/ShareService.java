package com.example.backend.chargerbnb.service;

import com.example.backend.chargerbnb.dto.ReserveDTO;
import com.example.backend.chargerbnb.dto.ShareDTO;
import com.google.api.core.ApiFuture;
import com.google.cloud.Timestamp;
import com.google.cloud.firestore.*;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
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
}
