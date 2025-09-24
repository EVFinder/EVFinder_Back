package com.example.backend.favorite.service;

import com.example.backend.favorite.dto.FavoriteRequest;
import com.example.backend.favorite.dto.ChargerInfoDto;
import com.google.cloud.firestore.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FavoriteService {
    private final Firestore firestore;

     // 즐겨찾기 추가
    public void addFavorite(FavoriteRequest request) throws ExecutionException, InterruptedException {
        DocumentReference favRef = firestore.collection("users")
                .document(request.getUid())
                .collection("favorite")
                .document(request.getId());

        Map<String, Object> favData = new HashMap<>();
        favData.put("id", request.getId());
        favData.put("name", request.getName());
        favData.put("address", request.getAddress());
        favData.put("lat", request.getLat());
        favData.put("lon", request.getLon());

        favRef.set(favData).get();

        // chargers 하위 저장
        for (ChargerInfoDto c : request.getChargers()) {
            Map<String, Object> chargerData = new HashMap<>();
            chargerData.put("stationId", c.getStationId());
            chargerData.put("chargerId", c.getChargerId());
            chargerData.put("status", c.getStatus());
            chargerData.put("isAvailable", c.getIsAvailable());

            favRef.collection("chargers").document(c.getChargerId()).set(chargerData).get();
        }
    }

    // 즐겨찾기 삭제
    public void removeFavorite(String uid, String stationId) throws ExecutionException, InterruptedException {
        DocumentReference favRef = firestore.collection("users")
                .document(uid)
                .collection("favorite")
                .document(stationId);

        // chargers 하위 문서 삭제
        CollectionReference chargersRef = favRef.collection("chargers");
        List<QueryDocumentSnapshot> chargerDocs = chargersRef.get().get().getDocuments();
        for (QueryDocumentSnapshot chargerDoc : chargerDocs) {
            chargersRef.document(chargerDoc.getId()).delete().get();
        }

        // 마지막으로 favorite 문서 삭제
        favRef.delete().get();
    }

    // 즐겨찾기 조회
    public List<Map<String, Object>> getFavorites(String uid) throws ExecutionException, InterruptedException {
        CollectionReference favs = firestore.collection("users").document(uid).collection("favorite");
        List<QueryDocumentSnapshot> docs = favs.get().get().getDocuments();

        List<Map<String, Object>> result = new ArrayList<>();
        for (QueryDocumentSnapshot doc : docs) {
            Map<String, Object> data = doc.getData();

            List<Map<String, Object>> chargers = favs.document(doc.getId())
                    .collection("chargers").get().get().getDocuments()
                    .stream().map(QueryDocumentSnapshot::getData).collect(Collectors.toList());

            data.put("chargers", chargers);
            result.add(data);
        }
        return result;
    }
}