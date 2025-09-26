package com.example.backend.community.controller;

import com.example.backend.community.dto.*;
import com.example.backend.community.service.PostService;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.example.backend.community.service.CommentService;
import com.example.backend.community.service.CategoryService;

import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/community")
@RequiredArgsConstructor
public class CommunityController {

    private final PostService postService;
    private final CommentService commentService;
    private final CategoryService categoryService;

    // =========게시글========
    //게시글 작성
    @PostMapping("/categories/{categoryId}/posts")
    public ResponseEntity<PostResponse> createPost(
            @PathVariable String categoryId,
            @RequestBody PostRequest request,
            @RequestAttribute("uid") String uid,
            @RequestAttribute("name") String authorName   // JWT 필터에서 name도 넣어주면 좋음
    ) throws Exception {
        return ResponseEntity.ok(postService.createPost(categoryId, request, uid, authorName));
    }

    //게시글 목록 조회
    @GetMapping("/categories/{categoryId}/posts/list")
    public ResponseEntity<List<PostSummaryResponse>> getPostsByCategory(
            @PathVariable String categoryId,
            @RequestAttribute("uid") String uid
    ) throws Exception {
        return ResponseEntity.ok(postService.getPostSummariesByCategory(categoryId, uid));
    }

    //게시글 상세 조회
    @GetMapping("/categories/{categoryId}/posts/{postId}")
    public ResponseEntity<PostResponse> getPost(
            @PathVariable String categoryId,
            @PathVariable String postId,
            @RequestAttribute("uid") String uid
    ) throws Exception {
        return ResponseEntity.ok(postService.getPost(categoryId, postId, uid));
    }

    // 내가 작성한 게시글 조회
    @GetMapping("/my/posts")
    public ResponseEntity<List<PostSummaryResponse>> getMyPosts(
            @RequestAttribute("uid") String uid
    ) throws Exception {
        return ResponseEntity.ok(postService.getMyPosts(uid));
    }

    //게시글 수정
    @PutMapping("/categories/{categoryId}/posts/{postId}")
    public ResponseEntity<PostResponse> updatePost(
            @PathVariable String categoryId,
            @PathVariable String postId,
            @RequestBody PostRequest request,
            @RequestAttribute("uid") String uid
    ) throws Exception {
        return ResponseEntity.ok(postService.updatePost(categoryId, postId, request, uid));
    }

    //게시글 삭제
    @DeleteMapping("/categories/{categoryId}/posts/{postId}")
    public ResponseEntity<Void> deletePost(
            @PathVariable String categoryId,
            @PathVariable String postId,
            @RequestAttribute("uid") String uid
    ) throws Exception {
        postService.deletePost(categoryId, postId, uid);
        return ResponseEntity.noContent().build();
    }

    // =========댓글========
    //댓글 작성
    @PostMapping("/categories/{categoryId}/posts/{postId}/comments")
    public ResponseEntity<CommentResponse> createComment(
            @PathVariable String categoryId,
            @PathVariable String postId,
            @RequestBody CommentRequest request,
            @RequestAttribute("uid") String uid,
            @RequestAttribute("name") String authorName
    ) throws Exception {
        return ResponseEntity.ok(commentService.createComment(categoryId, postId, request, uid, authorName));
    }

    // 단일 댓글 조회
    @GetMapping("/categories/{categoryId}/posts/{postId}/comments/{commentId}")
    public ResponseEntity<CommentResponse> getCommentById(
            @PathVariable String categoryId,
            @PathVariable String postId,
            @PathVariable String commentId,
            @RequestAttribute("uid") String uid
    ) throws Exception {
        return ResponseEntity.ok(commentService.getCommentById(categoryId, postId, commentId, uid));
    }

    //댓글 목록 조회
    @GetMapping("/categories/{categoryId}/posts/{postId}/comments/list")
    public ResponseEntity<List<CommentResponse>> getCommentsByPost(
            @PathVariable String categoryId,
            @PathVariable String postId,
            @RequestAttribute("uid") String uid
    ) throws Exception {
        return ResponseEntity.ok(commentService.getCommentsByPost(categoryId, postId, uid));
    }

    // 내가 작성한 댓글 조회
    @GetMapping("/my/comments")
    public ResponseEntity<List<CommentResponse>> getMyComments(
            @RequestAttribute("uid") String uid
    ) throws Exception {
        return ResponseEntity.ok(commentService.getMyComments(uid));
    }

    //댓글 수정
    @PutMapping("/categories/{categoryId}/posts/{postId}/comments/{commentId}")
    public ResponseEntity<CommentResponse> updateComment(
            @PathVariable String categoryId,
            @PathVariable String postId,
            @PathVariable String commentId,
            @RequestBody CommentRequest request,
            @RequestAttribute("uid") String uid
    ) throws Exception {
        return ResponseEntity.ok(commentService.updateComment(categoryId, postId, commentId, request, uid));
    }

    //댓글 삭제
    @DeleteMapping("/categories/{categoryId}/posts/{postId}/comments/{commentId}")
    public ResponseEntity<Void> deleteComment(
            @PathVariable String categoryId,
            @PathVariable String postId,
            @PathVariable String commentId,
            @RequestAttribute("uid") String uid
    ) throws Exception {
        commentService.deleteComment(categoryId, postId, commentId, uid);
        return ResponseEntity.noContent().build();
    }

    //=========카테고리=========
    //카테고리 생성
    @PostMapping("/categories")
    public ResponseEntity<CategoryResponse> createCategory(
            @RequestBody CategoryRequest request,
            @RequestAttribute("role") String role
    ) throws Exception {
        if (!"ADMIN".equals(role)) {
            throw new SecurityException("관리자만 카테고리 생성이 가능합니다.");
        }
        return ResponseEntity.ok(categoryService.createCategory(request));
    }

    //카테고리 목록 조회
    @GetMapping("/categories")
    public ResponseEntity<List<CategoryResponse>> getCategories() throws Exception {
        return ResponseEntity.ok(categoryService.getCategories());
    }

    //카테고리 수정
    @PutMapping("/categories/{categoryId}")
    public ResponseEntity<CategoryResponse> updateCategory(
            @PathVariable String categoryId,
            @RequestBody CategoryRequest request,
            @RequestAttribute("role") String role
    ) throws Exception {
        if (!"ADMIN".equals(role)) {
            throw new SecurityException("관리자만 카테고리 수정이 가능합니다.");
        }
        return ResponseEntity.ok(categoryService.updateCategory(categoryId, request));
    }

    //카테고리 삭제
    @DeleteMapping("/categories/{categoryId}")
    public ResponseEntity<Void> deleteCategory(
            @PathVariable String categoryId,
            @RequestAttribute("role") String role
    ) throws Exception {
        if (!"ADMIN".equals(role)) {
            throw new SecurityException("관리자만 카테고리 삭제가 가능합니다.");
        }
        categoryService.deleteCategory(categoryId);
        return ResponseEntity.noContent().build();
    }
}
