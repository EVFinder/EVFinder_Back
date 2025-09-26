package com.example.backend.community.service;

import com.example.backend.community.dto.PostRequest;
import com.example.backend.community.dto.PostResponse;
import com.example.backend.community.dto.PostSummaryResponse;
import com.example.backend.community.service.CommentService;

import com.google.api.core.ApiFuture;
import com.google.cloud.Timestamp;
import com.google.cloud.firestore.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class PostService {
        private final Firestore firestore;
        private final CommentService commentService;

        //게시글 작성
        public PostResponse createPost(String categoryId, PostRequest request, String uid, String authorName) throws Exception {
        String postId = UUID.randomUUID().toString();
                        Timestamp now = Timestamp.now();

                        // Firestore에 저장할 데이터
                        Map<String, Object> postData = new HashMap<>();
                        postData.put("title", request.getTitle());
                        postData.put("content", request.getContent());
                        postData.put("uid", uid);
                        postData.put("authorName", authorName);
                        postData.put("createdAt", now);
                        postData.put("updatedAt", now);
                        postData.put("views", 0);
                        postData.put("likes", 0);

                        // 1. 전체 게시판에 저장
                        DocumentReference postRef = firestore
                                .collection("community")
                                .document("categories")
                                .collection("items")
                                .document(categoryId)
                                .collection("posts")
                                .document(postId);

                        ApiFuture<WriteResult> future = postRef.set(postData);
                        future.get(); // 동기 처리

                        // 2. 사용자 전용 마이페이지에 저장
                        DocumentReference userPostRef = firestore
                                .collection("users")
                                .document(uid)
                                .collection("community")
                                .document(categoryId)
                                .collection("posts")
                                .document(postId);

                        userPostRef.set(postData);

                        // 응답 DTO 생성
                        return PostResponse.builder()
                                .postId(postId)
                                .title(request.getTitle())
                                .content(request.getContent())
                                .uid(uid)
                                .authorName(authorName)
                                .createdAt(now)
                                .updatedAt(now)
                                .views(0)
                                .likes(0)
                                .isOwner(true) // 방금 작성했으므로 true
                                .build();
        }

        //게시글 목록 조회 (간단 조회)
        public List<PostSummaryResponse> getPostSummariesByCategory(String categoryId, String uid) throws Exception {
                CollectionReference postsRef = firestore
                        .collection("community")
                        .document("categories")
                        .collection("items")
                        .document(categoryId)
                        .collection("posts");

                List<QueryDocumentSnapshot> documents = postsRef.get().get().getDocuments();

                List<PostSummaryResponse> summaries = new ArrayList<>();
                for (QueryDocumentSnapshot doc : documents) {
                        summaries.add(PostSummaryResponse.builder()
                                .postId(doc.getId())
                                .title(doc.getString("title"))
                                .authorName(doc.getString("authorName"))
                                .createdAt(doc.get("createdAt") != null ? doc.get("createdAt").toString() : null)
                                .build()
                        );
                }
                return summaries;
        }


        //게시글 조회 (내가 작성한)
        public List<PostSummaryResponse> getMyPosts(String uid) throws Exception {
                CollectionReference categoriesRef = firestore
                        .collection("community")
                        .document("categories")
                        .collection("items");

                List<PostSummaryResponse> myPosts = new ArrayList<>();

                for (DocumentReference categoryRef : categoriesRef.listDocuments()) {
                        CollectionReference postsRef = categoryRef.collection("posts");
                        List<QueryDocumentSnapshot> docs = postsRef.whereEqualTo("uid", uid).get().get().getDocuments();

                        for (QueryDocumentSnapshot doc : docs) {
                        myPosts.add(PostSummaryResponse.builder()
                                .postId(doc.getId())
                                .title(doc.getString("title"))
                                .authorName(doc.getString("authorName"))
                                .createdAt(doc.get("createdAt") != null ? doc.get("createdAt").toString() : null)
                                .build());
                        }
                }
                return myPosts;
        }

        //게시글 단일 상세 조회
        public PostResponse getPost(String categoryId, String postId, String uid) throws Exception {
                DocumentReference postRef = firestore.collection("community")
                        .document("categories")
                        .collection("items")
                        .document(categoryId)
                        .collection("posts")
                        .document(postId);

                DocumentSnapshot snapshot = postRef.get().get();
                if (!snapshot.exists()) throw new IllegalStateException("게시글이 존재하지 않습니다.");

                Map<String, Object> data = snapshot.getData();
                if (data == null) throw new IllegalStateException("게시글 데이터가 비어있습니다.");

                // 조회수 증가
                postRef.update("views", FieldValue.increment(1));

                String authorUid = (String) data.get("uid");

                return PostResponse.builder()
                        .postId(postId)
                        .title((String) data.get("title"))
                        .content((String) data.get("content"))
                        .uid(authorUid)
                        .authorName((String) data.get("authorName"))
                        .createdAt((Timestamp) data.get("createdAt"))
                        .updatedAt((Timestamp) data.get("updatedAt"))
                        .views(((Long) data.get("views")).intValue() + 1) // 증가 반영
                        .likes(((Long) data.get("likes")).intValue())
                        .isOwner(uid.equals(authorUid))
                        .build();
        }

        // 게시글 수정
        public PostResponse updatePost(String categoryId, String postId, PostRequest request, String uid) throws Exception {
                DocumentReference postRef = firestore.collection("community")
                        .document("categories")
                        .collection("items")
                        .document(categoryId)
                        .collection("posts")
                        .document(postId);

                DocumentSnapshot snapshot = postRef.get().get();
                if (!snapshot.exists()) throw new IllegalStateException("게시글이 존재하지 않습니다.");

                String authorUid = snapshot.getString("uid");
                if (!uid.equals(authorUid)) throw new SecurityException("본인만 수정할 수 있습니다.");

                Timestamp now = Timestamp.now();

                Map<String, Object> updateData = new HashMap<>();
                updateData.put("title", request.getTitle());
                updateData.put("content", request.getContent());
                updateData.put("updatedAt", now);

                // 1. 메인 경로 업데이트
                postRef.update(updateData).get();

                // 2. 사용자 전용 경로도 업데이트
                firestore.collection("users")
                        .document(uid)
                        .collection("community")
                        .document(categoryId)
                        .collection("posts")
                        .document(postId)
                        .update(updateData).get();

                return PostResponse.builder()
                        .postId(postId)
                        .title(request.getTitle())
                        .content(request.getContent())
                        .uid(authorUid)
                        .authorName((String) snapshot.get("authorName"))
                        .createdAt((Timestamp) snapshot.get("createdAt"))
                        .updatedAt(now)
                        .views(((Long) snapshot.get("views")).intValue())
                        .likes(((Long) snapshot.get("likes")).intValue())
                        .isOwner(true)
                        .build();
                }

        // 게시글 삭제
        public void deletePost(String categoryId, String postId, String uid) throws Exception {
                DocumentReference postRef = firestore.collection("community")
                        .document("categories")
                        .collection("items")
                        .document(categoryId)
                        .collection("posts")
                        .document(postId);

                DocumentSnapshot snapshot = postRef.get().get();
                if (!snapshot.exists()) throw new IllegalStateException("게시글이 존재하지 않습니다.");

                String authorUid = snapshot.getString("uid");
                if (!uid.equals(authorUid)) throw new SecurityException("본인만 삭제할 수 있습니다.");

                //댓글 전체 삭제 (양방향 처리)
                commentService.deleteAllComments(categoryId, postId);

                //메인 경로 삭제
                postRef.delete().get();

                //사용자 전용 경로 삭제
                firestore.collection("users")
                        .document(uid)
                        .collection("community")
                        .document(categoryId)
                        .collection("posts")
                        .document(postId)
                        .delete().get();
                }
}