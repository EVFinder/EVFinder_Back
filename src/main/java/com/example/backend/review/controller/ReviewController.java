package com.example.backend.review.controller;

import com.example.backend.review.dto.ReviewCreateRequest;
import com.example.backend.review.dto.ReviewResponse;
import com.example.backend.review.service.ReviewService;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/review")
public class ReviewController {
    private final ReviewService reviewService;
    private final Firestore firestore;

    //리뷰 작성
    @PostMapping("/add/{uid}")
    public ReviewResponse add(@PathVariable String uid,
                              @RequestBody ReviewCreateRequest req) throws Exception {
        return reviewService.add(uid, req);
    }

    // 리뷰 수정
    @PutMapping("/update/{uid}/{reviewId}")
    public ReviewResponse update(@PathVariable String uid,
                                @PathVariable String reviewId,
                                @RequestBody Map<String, Object> body) throws Exception {
        int rating = (int) body.get("rating");
        String content = (String) body.get("content");
        return reviewService.update(uid, reviewId, rating, content);
    }

    //사용자별 리뷰 조회
    @GetMapping("/list/user/{uid}")
    public List<ReviewResponse> listByUser(@PathVariable String uid) throws ExecutionException, InterruptedException {
        return reviewService.listByUser(uid);
    }

    // 충전소별 리뷰 조회 (정렬)
    @GetMapping("/list/station/{id}")
    public List<ReviewResponse> listByStation(@PathVariable String id,
                                            @RequestParam(defaultValue = "createdAt") String orderBy,
                                            @RequestParam(defaultValue = "0") int limit)
            throws ExecutionException, InterruptedException {
        return reviewService.listByStation(id, orderBy, limit);
    }

    // 충전소 리뷰 통계
    @GetMapping("/stats/{id}")
    public Map<String, Object> getStats(@PathVariable String id) throws ExecutionException, InterruptedException {
        return reviewService.getStats(id);
    }

    //리뷰 삭제
    @DeleteMapping("/delete/{uid}/{reviewId}")
    public void delete(@PathVariable String uid,
                       @PathVariable String reviewId) throws ExecutionException, InterruptedException {
        reviewService.delete(uid, reviewId);
    }
}
