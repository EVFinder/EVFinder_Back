package com.example.backend.favorite.service;

import com.example.backend.common.util.HttpUtil;
import com.example.backend.favorite.dto.FavoriteRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.example.backend.favorite.dto.ChargerInfoDto;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;


@Service
public class FavoriteService {

    private final Firestore firestore = FirestoreClient.getFirestore();

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
        firestore.collection("users")
                .document(uid)
                .collection("favorite")
                .document(stationId)
                .delete()
                .get();
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

    //status 업데이트
    public List<Map<String, Object>> updateFavoriteStatus(String uid) throws Exception {
        CollectionReference favs = firestore.collection("users").document(uid).collection("favorite");
        List<QueryDocumentSnapshot> favDocs = favs.get().get().getDocuments();

        List<Map<String, Object>> result = new ArrayList<>();
        ObjectMapper mapper = new ObjectMapper();

        for (QueryDocumentSnapshot favDoc : favDocs) {
            Map<String, Object> stationData = favDoc.getData();
            double lat = (double) stationData.get("lat");
            double lon = (double) stationData.get("lon");

            // 외부 API 호출 (evchargersOO)
            String url = String.format("http://localhost:8080/api/charger/evchargersOO?lat=%f&lon=%f", lat, lon);
            String response = HttpUtil.get(url);

            // JSON 파싱 → 충전기 리스트
            List<Map<String, Object>> evChargers =
                    mapper.readValue(response, new TypeReference<List<Map<String, Object>>>() {});

            // 현재 Firestore chargers
            CollectionReference chargersRef = favDoc.getReference().collection("chargers");
            List<QueryDocumentSnapshot> chargerDocs = chargersRef.get().get().getDocuments();

            for (QueryDocumentSnapshot chargerDoc : chargerDocs) {
                Map<String, Object> chargerData = chargerDoc.getData();
                String cId = (String) chargerData.get("chargerId");
                String sId = (String) chargerData.get("stationId");

                // evchargersOO 결과와 매칭 후 Firestore 업데이트
                evChargers.stream()
                        .filter(c -> c.get("stationId").equals(sId) && c.get("chargerId").equals(cId))
                        .findFirst()
                        .ifPresent(latest -> {
                            chargerData.put("status", latest.get("status"));
                            chargerData.put("isAvailable", latest.get("isAvailable"));
                            try {
                                chargersRef.document(cId).set(chargerData).get();
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                        });
            }

            // 최신 chargers 다시 읽어서 응답에 포함
            List<Map<String, Object>> chargers = chargersRef.get().get().getDocuments()
                    .stream().map(QueryDocumentSnapshot::getData).toList();

            stationData.put("chargers", chargers);
            result.add(stationData);
        }

        return result;
    }
}