package com.example.backend.community.service;

import com.example.backend.community.dto.CommentRequest;
import com.example.backend.community.dto.CommentResponse;
import com.google.api.core.ApiFuture;
import com.google.cloud.Timestamp;
import com.google.cloud.firestore.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class CommentService {
    private final Firestore firestore;

    // 댓글 작성
    public CommentResponse createComment(String categoryId, String postId, CommentRequest request, String uid, String authorName) throws Exception {
        String commentId = UUID.randomUUID().toString();
        Timestamp now = Timestamp.now();

        Map<String, Object> commentData = new HashMap<>();
        commentData.put("content", request.getContent());
        commentData.put("uid", uid);
        commentData.put("authorName", authorName);
        commentData.put("parentId", request.getParentId());
        commentData.put("createdAt", now);
        commentData.put("updatedAt", now);

        //메인구조 community저장
        DocumentReference commentRef = firestore.collection("community")
                .document("categories")
                .collection("items")
                .document(categoryId)
                .collection("posts")
                .document(postId)
                .collection("comments")
                .document(commentId);

        commentRef.set(commentData).get();

        //사용자 전용 구조에 저장
        DocumentReference userCommentRef = firestore.collection("users")
                .document(uid)
                .collection("community")
                .document(categoryId)
                .collection("posts")
                .document(postId)
                .collection("comments")
                .document(commentId);
        userCommentRef.set(commentData).get();

        return CommentResponse.builder()
                .commentId(commentId)
                .content(request.getContent())
                .uid(uid)
                .authorName(authorName)
                .parentId(request.getParentId())
                .createdAt(now)
                .updatedAt(now)
                .isOwner(true)
                .build();
    }

    // 단일 댓글 조회
    public CommentResponse getCommentById(String categoryId, String postId, String commentId, String uid) throws Exception {
        DocumentReference commentRef = firestore.collection("community")
                .document("categories")
                .collection("items")
                .document(categoryId)
                .collection("posts")
                .document(postId)
                .collection("comments")
                .document(commentId);

        DocumentSnapshot snapshot = commentRef.get().get();
        if (!snapshot.exists()) {
            throw new IllegalStateException("댓글이 존재하지 않습니다.");
        }

        Map<String, Object> data = snapshot.getData();
        if (data == null) {
            throw new IllegalStateException("댓글 데이터가 비어있습니다.");
        }

        String authorUid = (String) data.get("uid");

        return CommentResponse.builder()
                .commentId(snapshot.getId())
                .content((String) data.get("content"))
                .uid(authorUid)
                .authorName((String) data.get("authorName"))
                .parentId((String) data.get("parentId"))
                .createdAt((Timestamp) data.get("createdAt"))
                .updatedAt((Timestamp) data.get("updatedAt"))
                .isOwner(uid.equals(authorUid))
                .build();
    }

    // 댓글 목록 조회
    public List<CommentResponse> getCommentsByPost(String categoryId, String postId, String uid) throws Exception {
        CollectionReference commentsRef = firestore.collection("community")
                .document("categories")
                .collection("items")
                .document(categoryId)
                .collection("posts")
                .document(postId)
                .collection("comments");

        ApiFuture<QuerySnapshot> future = commentsRef.orderBy("createdAt", Query.Direction.ASCENDING).get();
        List<QueryDocumentSnapshot> documents = future.get().getDocuments();

        List<CommentResponse> result = new ArrayList<>();
        for (DocumentSnapshot doc : documents) {
            Map<String, Object> data = doc.getData();
            if (data == null) continue;
            String authorUid = (String) data.get("uid");
            result.add(CommentResponse.builder()
                    .commentId(doc.getId())
                    .content((String) data.get("content"))
                    .uid(authorUid)
                    .authorName((String) data.get("authorName"))
                    .parentId((String) data.get("parentId"))
                    .createdAt((Timestamp) data.get("createdAt"))
                    .updatedAt((Timestamp) data.get("updatedAt"))
                    .isOwner(uid.equals(authorUid))
                    .build());
        }
        return result;
    }

    // 내가 작성한 댓글
    public List<CommentResponse> getMyComments(String uid) throws Exception {
        CollectionReference userCommunityRef = firestore
                .collection("users")
                .document(uid)
                .collection("community");

        List<CommentResponse> myComments = new ArrayList<>();

        // categories 순회
        for (DocumentReference categoryRef : userCommunityRef.listDocuments()) {
                String categoryId = categoryRef.getId();

                CollectionReference postsRef = categoryRef.collection("posts");
                for (DocumentReference postRef : postsRef.listDocuments()) {
                String postId = postRef.getId();

                CollectionReference commentsRef = postRef.collection("comments");
                List<QueryDocumentSnapshot> docs = commentsRef.get().get().getDocuments();

                for (QueryDocumentSnapshot doc : docs) {
                        myComments.add(CommentResponse.builder()
                                .commentId(doc.getId())
                                .categoryId(categoryId)
                                .postId(postId)
                                .content(doc.getString("content"))
                                .authorName(doc.getString("authorName"))
                                .createdAt(doc.contains("createdAt") ? doc.getTimestamp("createdAt") : null)
                                .updatedAt(doc.contains("updatedAt") ? doc.getTimestamp("updatedAt") : null)
                                .parentId(doc.getString("parentId"))
                                .uid(uid)
                                .isOwner(true)
                                .build());
                }
                }
        }
        return myComments;
    }

    //댓글 수정
    public CommentResponse updateComment(String categoryId, String postId, String commentId, CommentRequest request, String uid) throws Exception {
        DocumentReference commentRef = firestore.collection("community")
                .document("categories")
                .collection("items")
                .document(categoryId)
                .collection("posts")
                .document(postId)
                .collection("comments")
                .document(commentId);

        DocumentSnapshot snapshot = commentRef.get().get();
        if (!snapshot.exists()) throw new IllegalStateException("댓글이 존재하지 않습니다.");

        String authorUid = snapshot.getString("uid");
        if (!uid.equals(authorUid)) throw new SecurityException("본인만 수정할 수 있습니다.");

        Timestamp now = Timestamp.now();
        Map<String, Object> updateData = new HashMap<>();
        updateData.put("content", request.getContent());
        updateData.put("updatedAt", now);

        // 1. 메인 경로 업데이트
        commentRef.update(updateData).get();

        // 2. 사용자 전용 경로 업데이트
        firestore.collection("users")
                .document(uid)
                .collection("community")
                .document(categoryId)
                .collection("posts")
                .document(postId)
                .collection("comments")
                .document(commentId)
                .update(updateData).get();

        return CommentResponse.builder()
                .commentId(commentId)
                .content(request.getContent())
                .uid(authorUid)
                .authorName((String) snapshot.get("authorName"))
                .parentId((String) snapshot.get("parentId"))
                .createdAt((Timestamp) snapshot.get("createdAt"))
                .updatedAt(now)
                .isOwner(true)
                .build();
    }

    //댓글 삭제 (대댓글 포함 삭제)
    public void deleteComment(String categoryId, String postId, String commentId, String uid) throws Exception {
        DocumentReference commentRef = firestore.collection("community")
                .document("categories")
                .collection("items")
                .document(categoryId)
                .collection("posts")
                .document(postId)
                .collection("comments")
                .document(commentId);

        DocumentSnapshot snapshot = commentRef.get().get();
        if (!snapshot.exists()) throw new IllegalStateException("댓글이 존재하지 않습니다.");

        String authorUid = snapshot.getString("uid");
        if (!uid.equals(authorUid)) throw new SecurityException("본인만 삭제할 수 있습니다.");

        // 1. 대댓글 재귀 삭제 (양방향 삭제)
        deleteChildComments(categoryId, postId, commentId);

        // 2. 메인 경로 삭제
        commentRef.delete().get();

        // 3. 사용자 전용 경로 삭제
        firestore.collection("users")
                .document(uid)
                .collection("community")
                .document(categoryId)
                .collection("posts")
                .document(postId)
                .collection("comments")
                .document(commentId)
                .delete().get();
    }

    // 게시글 삭제 시 모든 댓글 삭제
    public void deleteAllComments(String categoryId, String postId) throws Exception {
        CollectionReference commentsRef = firestore.collection("community")
                .document("categories")
                .collection("items")
                .document(categoryId)
                .collection("posts")
                .document(postId)
                .collection("comments");

        List<QueryDocumentSnapshot> allComments = commentsRef.get().get().getDocuments();

        for (QueryDocumentSnapshot comment : allComments) {
            String commentId = comment.getId();
            String uid = comment.getString("uid"); // 댓글 작성자 uid 가져오기

            // 대댓글 포함 재귀 삭제
            deleteChildComments(categoryId, postId, commentId);

            // 최상위 댓글 삭제 (메인)
            commentsRef.document(commentId).delete().get();

            // 최상위 댓글 삭제 (사용자 전용)
            firestore.collection("users")
                    .document(uid)
                    .collection("community")
                    .document(categoryId)
                    .collection("posts")
                    .document(postId)
                    .collection("comments")
                    .document(commentId)
                    .delete().get();
        }
    }

    //특정 댓글 삭제시, 대댓글 재귀 삭제해줌
    private void deleteChildComments(String categoryId, String postId, String parentId) throws Exception {
        CollectionReference commentsRef = firestore.collection("community")
                .document("categories")
                .collection("items")
                .document(categoryId)
                .collection("posts")
                .document(postId)
                .collection("comments");

        List<QueryDocumentSnapshot> childComments = commentsRef
                .whereEqualTo("parentId", parentId)
                .get()
                .get()
                .getDocuments();

        for (QueryDocumentSnapshot child : childComments) {
            String childId = child.getId();
            String uid = child.getString("uid"); // 작성자 uid 가져오기

            // 재귀 호출 (손자 댓글까지 모두 삭제)
            deleteChildComments(categoryId, postId, childId);

            // 자식 댓글 삭제 (메인)
            commentsRef.document(childId).delete().get();

            // 자식 댓글 삭제 (사용자 전용)
            firestore.collection("users")
                    .document(uid)
                    .collection("community")
                    .document(categoryId)
                    .collection("posts")
                    .document(postId)
                    .collection("comments")
                    .document(childId)
                    .delete().get();
        }
    }

}
