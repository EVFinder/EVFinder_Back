package com.example.backend.community.service;

import com.example.backend.community.dto.CategoryRequest;
import com.example.backend.community.dto.CategoryResponse;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final Firestore firestore;

    // 카테고리 생성
    public CategoryResponse createCategory(CategoryRequest request) throws Exception {
        String categoryId = UUID.randomUUID().toString();

        Map<String, Object> categoryData = new HashMap<>();
        categoryData.put("name", request.getName());
        categoryData.put("description", request.getDescription());

        DocumentReference categoryRef = firestore.collection("community")
                .document("categories")
                .collection("items") // categories/{id} 하위로 관리
                .document(categoryId);

        categoryRef.set(categoryData).get();

        return CategoryResponse.builder()
                .categoryId(categoryId)
                .name(request.getName())
                .description(request.getDescription())
                .build();
    }

    // 카테고리 (전체)목록 조회
    public List<CategoryResponse> getCategories() throws Exception {
        CollectionReference categoriesRef = firestore.collection("community")
                .document("categories")
                .collection("items");

        ApiFuture<QuerySnapshot> future = categoriesRef.get();
        List<QueryDocumentSnapshot> docs = future.get().getDocuments();

        List<CategoryResponse> result = new ArrayList<>();
        for (DocumentSnapshot doc : docs) {
            result.add(CategoryResponse.builder()
                    .categoryId(doc.getId())
                    .name(doc.getString("name"))
                    .description(doc.getString("description"))
                    .build());
        }
        return result;
    }

    // 카테고리 단일 조회
    public CategoryResponse getCategory(String categoryId) throws Exception {
        DocumentReference categoryRef = firestore.collection("community")
                .document("categories")
                .collection("items")
                .document(categoryId);

        DocumentSnapshot snapshot = categoryRef.get().get();
        if (!snapshot.exists()) throw new IllegalStateException("카테고리가 존재하지 않습니다.");

        return CategoryResponse.builder()
                .categoryId(snapshot.getId())
                .name(snapshot.getString("name"))
                .description(snapshot.getString("description"))
                .build();
    }

    // 카테고리 수정
    public CategoryResponse updateCategory(String categoryId, CategoryRequest request) throws Exception {
        DocumentReference categoryRef = firestore.collection("community")
                .document("categories")
                .collection("items")
                .document(categoryId);

        DocumentSnapshot snapshot = categoryRef.get().get();
        if (!snapshot.exists()) throw new IllegalStateException("카테고리가 존재하지 않습니다.");

        Map<String, Object> updateData = new HashMap<>();
        updateData.put("name", request.getName());
        updateData.put("description", request.getDescription());

        categoryRef.update(updateData).get();

        return CategoryResponse.builder()
                .categoryId(categoryId)
                .name(request.getName())
                .description(request.getDescription())
                .build();
    }

    // 카테고리 삭제
    public void deleteCategory(String categoryId) throws Exception {
        CollectionReference postsRef = firestore.collection("community")
                .document("categories")
                .collection("items")
                .document(categoryId)
                .collection("posts");

        ApiFuture<QuerySnapshot> postsFuture = postsRef.get();
        for (DocumentSnapshot postDoc : postsFuture.get().getDocuments()) {
            // 댓글 삭제
            CollectionReference commentsRef = postDoc.getReference().collection("comments");
            ApiFuture<QuerySnapshot> commentsFuture = commentsRef.get();
            for (DocumentSnapshot commentDoc : commentsRef.get().get().getDocuments()) {
                commentDoc.getReference().delete();
            }
            // 게시글 삭제
            postDoc.getReference().delete();
        }

        firestore.collection("community")
                .document("categories")
                .collection("items")
                .document(categoryId)
                .delete()
                .get();
    }
}
