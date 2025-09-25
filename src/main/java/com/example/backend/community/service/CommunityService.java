package com.example.backend.community.service;

import com.example.backend.community.dto.CategoryRequest;
import com.example.backend.community.dto.CategoryResponse;
import com.example.backend.community.dto.CommentRequest;
import com.example.backend.community.dto.CommentResponse;
import com.example.backend.community.dto.PostRequest;
import com.example.backend.community.dto.PostResponse;
import com.example.backend.common.json.TimestampSerializer;
import com.google.api.core.ApiFuture;
import com.google.cloud.Timestamp;
import com.google.cloud.firestore.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ExecutionException;

@Service
@RequiredArgsConstructor
public class CommunityService {

    private final Firestore firestore;

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


    //게시글 목록 조회
    public List<PostResponse> getPostsByCategory(String categoryId, String uid) throws Exception {
        CollectionReference postsRef = firestore.collection("community")
                .document("categories")
                .collection("items")
                .document(categoryId)
                .collection("posts");

        ApiFuture<QuerySnapshot> future = postsRef.orderBy("createdAt", Query.Direction.DESCENDING).get();
        List<QueryDocumentSnapshot> documents = future.get().getDocuments();

        List<PostResponse> result = new ArrayList<>();
        for (DocumentSnapshot doc : documents) {
            Map<String, Object> data = doc.getData();
            if (data == null) continue;

            String authorUid = (String) data.get("uid");

            result.add(PostResponse.builder()
                    .postId(doc.getId())
                    .title((String) data.get("title"))
                    .content((String) data.get("content"))
                    .uid(authorUid)
                    .authorName((String) data.get("authorName"))
                    .createdAt((Timestamp) data.get("createdAt"))
                    .updatedAt((Timestamp) data.get("updatedAt"))
                    .views(((Long) data.get("views")).intValue())
                    .likes(((Long) data.get("likes")).intValue())
                    .isOwner(uid.equals(authorUid))
                    .build());
        }
        return result;
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

        postRef.update("title", request.getTitle(),
                       "content", request.getContent(),
                       "updatedAt", now).get();

        Map<String, Object> data = snapshot.getData();

        return PostResponse.builder()
                .postId(postId)
                .title(request.getTitle())
                .content(request.getContent())
                .uid(authorUid)
                .authorName((String) data.get("authorName"))
                .createdAt((Timestamp) data.get("createdAt"))
                .updatedAt(now)
                .views(((Long) data.get("views")).intValue())
                .likes(((Long) data.get("likes")).intValue())
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

        // 1. 전체 게시판에서 삭제
        postRef.delete().get();

        // 2. 마이페이지에서도 삭제
        firestore.collection("users")
                .document(uid)
                .collection("community")
                .document(categoryId)
                .collection("posts")
                .document(postId)
                .delete();
    }


    //===============댓글=============
    //댓글 작성
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

        DocumentReference commentRef = firestore.collection("community")
                 .document("categories")
                .collection("items")
                .document(categoryId)
                .collection("posts")
                .document(postId)
                .collection("comments")
                .document(commentId);

        commentRef.set(commentData).get();

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

    
    //댓글 목록 조회
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
        commentRef.update("content", request.getContent(), "updatedAt", now).get();

        Map<String, Object> data = snapshot.getData();

        return CommentResponse.builder()
                .commentId(commentId)
                .content(request.getContent())
                .uid(authorUid)
                .authorName((String) data.get("authorName"))
                .parentId((String) data.get("parentId"))
                .createdAt((Timestamp) data.get("createdAt"))
                .updatedAt(now)
                .isOwner(true)
                .build();
    }

    
    //댓글 삭제
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

        commentRef.delete().get();
    }





    //==============카테고리===============
    //카테고리 생성
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


    //카테고리 전체 조회(카테고리 목록)
    public List<CategoryResponse> getCategories() throws Exception {
        CollectionReference categoriesRef = firestore.collection("community")
                .document("categories")
                .collection("items");

        ApiFuture<QuerySnapshot> future = categoriesRef.get();
        List<QueryDocumentSnapshot> documents = future.get().getDocuments();

        List<CategoryResponse> result = new ArrayList<>();
        for (DocumentSnapshot doc : documents) {
            result.add(CategoryResponse.builder()
                    .categoryId(doc.getId())
                    .name(doc.getString("name"))
                    .description(doc.getString("description"))
                    .build());
        }
        return result;
    }


    //카테고리 단일 조회
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

    
    //카테고리 수정
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

    
    // 카테고리 삭제 (기존 코드 수정: 하위 게시글도 같이 삭제)
    public void deleteCategory(String categoryId) throws Exception {
        // 카테고리 안의 posts 컬렉션
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
            for (DocumentSnapshot commentDoc : commentsFuture.get().getDocuments()) {
                commentDoc.getReference().delete();
            }

            // 게시글 삭제
            postDoc.getReference().delete();
        }

        // 카테고리 문서 삭제
        firestore.collection("community")
                .document("categories")
                .collection("items")
                .document(categoryId)
                .delete()
                .get();
    }
}
