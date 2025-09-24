package com.example.backend.review.service;

import com.example.backend.review.dto.ReviewCreateRequest;
import com.example.backend.review.dto.ReviewResponse;
import com.example.backend.review.util.ReviewMapper;
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
        Timestamp now = Timestamp.now();

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("reviewId", reviewId);
        data.put("id", req.getId());
        data.put("name", req.getName());
        data.put("uid", uid);
        data.put("userName", userName);
        data.put("rating", req.getRating());
        data.put("content", req.getContent());
        data.put("createdAt", now);
        data.put("updatedAt", now);
        

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
                .createdAt(now)
                .updatedAt(now)
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
    
    //충전소별 리뷰 목록 조회 (정렬)
    public List<ReviewResponse> listByStation(String stationId, String orderBy, int limit)
        throws ExecutionException, InterruptedException {

        CollectionReference col = firestore.collection("stations").document(stationId).collection("reviews");

        Query query = col;
        if ("createdAt".equals(orderBy)) {
            query = query.orderBy("createdAt", Query.Direction.DESCENDING);
        } else if ("rating".equals(orderBy)) {
            query = query.orderBy("rating", Query.Direction.DESCENDING);
        }

        if (limit > 0) {
            query = query.limit(limit);
        }

        List<QueryDocumentSnapshot> docs = query.get().get().getDocuments();

        List<ReviewResponse> result = new ArrayList<>();
        for (DocumentSnapshot d : docs) {
            result.add(ReviewMapper.toResponse(d));
        }
        return result;
    }


    //리뷰 수정
    public ReviewResponse update(String uid, String reviewId, int rating, String content)
            throws ExecutionException, InterruptedException {

        DocumentReference userReviewRef = firestore.collection("users")
                .document(uid).collection("reviews").document(reviewId);

        DocumentSnapshot snap = userReviewRef.get().get();
        if (!snap.exists()) {
            throw new IllegalArgumentException("리뷰가 존재하지 않습니다.");
        }

        String stationId = snap.getString("id");
        String name = snap.getString("name");
        String userName = snap.getString("userName");

        Timestamp now = Timestamp.now();

        Map<String, Object> updates = new HashMap<>();
        updates.put("rating", rating);
        updates.put("content", content);
        updates.put("updatedAt", now);

        WriteBatch batch = firestore.batch();
        batch.update(userReviewRef, updates);

        if (stationId != null) {
            DocumentReference stationReviewRef = firestore.collection("stations")
                    .document(stationId).collection("reviews").document(reviewId);
            batch.update(stationReviewRef, updates);
        }

        batch.commit().get();

        return ReviewResponse.builder()
                .reviewId(reviewId)
                .id(stationId)
                .name(name)
                .uid(uid)
                .userName(userName)
                .rating(rating)
                .content(content)
                .createdAt(snap.getTimestamp("createdAt")) // 기존 생성 시각 유지
                .updatedAt(now)
                .build();
    }

    //리뷰 통계
    public Map<String, Object> getStats(String stationId) throws ExecutionException, InterruptedException {
        CollectionReference col = firestore.collection("stations").document(stationId).collection("reviews");
        List<QueryDocumentSnapshot> docs = col.get().get().getDocuments();

        int count = docs.size();
        double avg = 0.0;
        if (count > 0) {
            int sum = 0;
            for (DocumentSnapshot d : docs) {
                Long ratingValue = d.getLong("rating");
                if (ratingValue != null) sum += ratingValue.intValue();
            }
            avg = (double) sum / count;
        }

        Map<String, Object> stats = new HashMap<>();
        stats.put("count", count);
        stats.put("averageRating", avg);
        return stats;
    }
}