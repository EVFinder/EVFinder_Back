package com.example.backend.review.service;

import com.example.backend.review.dto.ReviewCreateRequest;
import com.example.backend.review.dto.ReviewResponse;
import com.example.backend.review.util.ReviewMapper;

import com.google.api.core.ApiFuture;
import com.google.cloud.Timestamp;
import com.google.cloud.firestore.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ExecutionException;

@Service
@RequiredArgsConstructor
public class ReviewService {
    private final Firestore firestore;


    //리뷰 추가 (users/{uid}, stations/{id} 양방향 저장)
    public ReviewResponse add(String uid, ReviewCreateRequest req) throws ExecutionException, InterruptedException {

        DocumentSnapshot userDoc = firestore.collection("users").document(uid).get().get();
        String userName = userDoc.getString("userName");
        if (userName == null) userName = "사용자";

        String reviewId = UUID.randomUUID().toString();
        Timestamp createdAt = Timestamp.now();

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("reviewId", reviewId);
        data.put("id", req.getId());
        data.put("name", req.getName());
        data.put("uid", uid);
        data.put("userName", userName);
        data.put("rating", req.getRating());
        data.put("content", req.getContent());
        data.put("createdAt", createdAt);

        WriteBatch batch = firestore.batch();

        //users/{uid}/reviews/{reviewId}
        DocumentReference userReviewRef = firestore.collection("users")
                .document(uid).collection("reviews").document(reviewId);
        batch.set(userReviewRef, data);

        //stations/{id}/reviews/{reviewId}
        DocumentReference stationReviewRef = firestore.collection("stations")
                .document(req.getId()).collection("reviews").document(reviewId);
        batch.set(stationReviewRef, data);

        batch.commit().get();

        return ReviewResponse.builder()
                .reviewId(reviewId)
                .id(req.getId())
                .name(req.getName())
                .uid(uid)
                .userName(userName)
                .rating(req.getRating())
                .content(req.getContent())
                .createdAt(createdAt)
                .build();
    }


    //리뷰 삭제 (users, stations 양쪽 삭제)
    public void delete(String uid, String reviewId) throws ExecutionException, InterruptedException {
        DocumentReference userReviewRef = firestore.collection("users")
                .document(uid).collection("reviews").document(reviewId);

        DocumentSnapshot snap = userReviewRef.get().get();
        if (!snap.exists()) return;

        String stationId = snap.getString("id");

        WriteBatch batch = firestore.batch();
        batch.delete(userReviewRef);

        if (stationId != null) {
            DocumentReference stationReviewRef = firestore.collection("stations")
                    .document(stationId).collection("reviews").document(reviewId);
            batch.delete(stationReviewRef);
        }

        batch.commit().get();
    }


    //사용자별 리뷰 목록 조회

    public List<ReviewResponse> listByUser(String uid) throws ExecutionException, InterruptedException {
        CollectionReference col = firestore.collection("users").document(uid).collection("reviews");
        List<QueryDocumentSnapshot> docs = col.get().get().getDocuments();

        List<ReviewResponse> result = new ArrayList<>();
        for (DocumentSnapshot d : docs) {
            result.add(ReviewMapper.toResponse(d));
        }
        return result;
    }


    //충전소별 리뷰 목록 조회
    public List<ReviewResponse> listByStation(String stationId) throws ExecutionException, InterruptedException {
        CollectionReference col = firestore.collection("stations").document(stationId).collection("reviews");
        List<QueryDocumentSnapshot> docs = col.get().get().getDocuments();

        List<ReviewResponse> result = new ArrayList<>();
        for (DocumentSnapshot d : docs) {
            result.add(ReviewMapper.toResponse(d));
        }
        return result;
    }
}