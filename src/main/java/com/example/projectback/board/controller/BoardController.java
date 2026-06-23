package com.example.projectback.board.controller;

import com.example.projectback.board.dto.BoardCommentCreateRequest;
import com.example.projectback.board.dto.BoardCommentResponse;
import com.example.projectback.board.dto.BoardCommentUpdateRequest;
import com.example.projectback.board.dto.BoardPostCreateRequest;
import com.example.projectback.board.dto.BoardPostCreateResponse;
import com.example.projectback.board.dto.BoardPostDetailResponse;
import com.example.projectback.board.dto.BoardPostImageResponse;
import com.example.projectback.board.dto.BoardPostListItemResponse;
import com.example.projectback.board.dto.BoardPostUpdateRequest;
import com.example.projectback.board.dto.BoardReportRequest;
import com.example.projectback.board.dto.LikeResponse;
import com.example.projectback.board.service.BoardCommentService;
import com.example.projectback.board.service.BoardLikeService;
import com.example.projectback.board.service.BoardPostService;
import com.example.projectback.board.service.BoardReportService;
import com.example.projectback.common.ApiResponse;
import com.example.projectback.security.CurrentUserProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import jakarta.validation.Valid;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class BoardController {

    private final BoardPostService boardPostService;
    private final BoardCommentService boardCommentService;
    private final BoardLikeService boardLikeService;
    private final BoardReportService boardReportService;
    private final CurrentUserProvider currentUserProvider;

    @GetMapping("/categories")
    public ResponseEntity<ApiResponse<List<String>>> getCategories() {
        return ResponseEntity.ok(ApiResponse.success(boardPostService.getCategories(), "카테고리 목록 조회 성공"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<BoardPostListItemResponse>>> getPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "title") String searchType,
            @RequestParam(required = false) String category) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<BoardPostListItemResponse> response = boardPostService.getPosts(keyword, searchType, category, pageable);
        return ResponseEntity.ok(ApiResponse.success(response, "게시글 목록 조회 성공"));
    }

    @GetMapping("/popular")
    public ResponseEntity<ApiResponse<List<BoardPostListItemResponse>>> getPopularPosts(
            @RequestParam(defaultValue = "5") int limit) {
        List<BoardPostListItemResponse> response = boardPostService.getPopularPosts(limit);
        return ResponseEntity.ok(ApiResponse.success(response, "인기글 조회 성공"));
    }

    @GetMapping("/most-viewed")
    public ResponseEntity<ApiResponse<List<BoardPostListItemResponse>>> getMostViewedPosts(
            @RequestParam(defaultValue = "5") int limit) {
        List<BoardPostListItemResponse> response = boardPostService.getMostViewedPosts(limit);
        return ResponseEntity.ok(ApiResponse.success(response, "조회수 많은 글 조회 성공"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BoardPostDetailResponse>> getPostDetail(@PathVariable Long id) {
        Long currentUserId = null;
        try {
            currentUserId = currentUserProvider.getCurrentUserId();
        } catch (AuthenticationCredentialsNotFoundException ignored) {
        }

        BoardPostDetailResponse response = boardPostService.getPostDetail(id, currentUserId);
        return ResponseEntity.ok(ApiResponse.success(response, "게시글 상세 조회 성공"));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<BoardPostCreateResponse>> createPost(
            @RequestBody BoardPostCreateRequest request) {

        Long userId = currentUserProvider.getCurrentUserId();
        BoardPostCreateResponse response = boardPostService.createPost(userId, request);
        return ResponseEntity.ok(ApiResponse.success(response, "게시글 등록 성공"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<BoardPostDetailResponse>> updatePost(
            @PathVariable Long id,
            @RequestBody BoardPostUpdateRequest request) {

        Long userId = currentUserProvider.getCurrentUserId();
        BoardPostDetailResponse response = boardPostService.updatePost(id, userId, request);
        return ResponseEntity.ok(ApiResponse.success(response, "게시글 수정 성공"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deletePost(@PathVariable Long id) {
        Long userId = currentUserProvider.getCurrentUserId();
        boardPostService.deletePost(id, userId);
        return ResponseEntity.ok(ApiResponse.success("게시글 삭제 성공"));
    }

    @GetMapping("/{id}/comments")
    public ResponseEntity<ApiResponse<List<BoardCommentResponse>>> getComments(@PathVariable Long id) {
        Long currentUserId = null;
        try {
            currentUserId = currentUserProvider.getCurrentUserId();
        } catch (AuthenticationCredentialsNotFoundException ignored) {
        }
        List<BoardCommentResponse> response = boardCommentService.getComments(id, currentUserId);
        return ResponseEntity.ok(ApiResponse.success(response, "댓글 목록 조회 성공"));
    }

    @PostMapping("/{id}/comments")
    public ResponseEntity<ApiResponse<BoardCommentResponse>> createComment(
            @PathVariable Long id,
            @Valid @RequestBody BoardCommentCreateRequest request) {

        Long userId = currentUserProvider.getCurrentUserId();
        BoardCommentResponse response = boardCommentService.createComment(id, userId, request);
        return ResponseEntity.ok(ApiResponse.success(response, "댓글 등록 성공"));
    }

    @PutMapping("/{id}/comments/{commentId}")
    public ResponseEntity<ApiResponse<BoardCommentResponse>> updateComment(
            @PathVariable Long id,
            @PathVariable Long commentId,
            @Valid @RequestBody BoardCommentUpdateRequest request) {

        Long userId = currentUserProvider.getCurrentUserId();
        BoardCommentResponse response = boardCommentService.updateComment(id, commentId, userId, request);
        return ResponseEntity.ok(ApiResponse.success(response, "댓글 수정 성공"));
    }

    @DeleteMapping("/{id}/comments/{commentId}")
    public ResponseEntity<ApiResponse<Void>> deleteComment(
            @PathVariable Long id,
            @PathVariable Long commentId) {

        Long userId = currentUserProvider.getCurrentUserId();
        boardCommentService.deleteComment(id, commentId, userId);
        return ResponseEntity.ok(ApiResponse.success("댓글 삭제 성공"));
    }

    @PostMapping("/{postId}/likes")
    public ResponseEntity<ApiResponse<LikeResponse>> togglePostLike(@PathVariable Long postId) {
        Long userId = currentUserProvider.getCurrentUserId();
        LikeResponse response = boardLikeService.togglePostLike(postId, userId);
        String message = response.isLiked() ? "게시글 좋아요 추가" : "게시글 좋아요 취소";
        return ResponseEntity.ok(ApiResponse.success(response, message));
    }

    @PostMapping("/{postId}/comments/{commentId}/likes")
    public ResponseEntity<ApiResponse<LikeResponse>> toggleCommentLike(
            @PathVariable Long postId,
            @PathVariable Long commentId) {
        Long userId = currentUserProvider.getCurrentUserId();
        LikeResponse response = boardLikeService.toggleCommentLike(postId, commentId, userId);
        String message = response.isLiked() ? "댓글 좋아요 추가" : "댓글 좋아요 취소";
        return ResponseEntity.ok(ApiResponse.success(response, message));
    }

    @PostMapping(value = "/{postId}/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<List<BoardPostImageResponse>>> uploadImages(
            @PathVariable Long postId,
            @RequestPart("images") List<MultipartFile> files) {
        Long userId = currentUserProvider.getCurrentUserId();
        List<BoardPostImageResponse> response = boardPostService.uploadImages(postId, userId, files);
        return ResponseEntity.ok(ApiResponse.success(response, "이미지 업로드 성공"));
    }

    @DeleteMapping("/{postId}/images/{imageId}")
    public ResponseEntity<ApiResponse<Void>> deleteImage(
            @PathVariable Long postId,
            @PathVariable Long imageId) {
        Long userId = currentUserProvider.getCurrentUserId();
        boardPostService.deleteImage(postId, imageId, userId);
        return ResponseEntity.ok(ApiResponse.success("이미지 삭제 성공"));
    }

    @PostMapping("/{postId}/reports")
    public ResponseEntity<ApiResponse<Void>> reportPost(
            @PathVariable Long postId,
            @Valid @RequestBody BoardReportRequest request) {
        Long userId = currentUserProvider.getCurrentUserId();
        boardReportService.reportPost(postId, userId, request.getReason());
        return ResponseEntity.ok(ApiResponse.success("신고가 접수되었습니다."));
    }

    @PostMapping("/{postId}/comments/{commentId}/reports")
    public ResponseEntity<ApiResponse<Void>> reportComment(
            @PathVariable Long postId,
            @PathVariable Long commentId,
            @Valid @RequestBody BoardReportRequest request) {
        Long userId = currentUserProvider.getCurrentUserId();
        boardReportService.reportComment(postId, commentId, userId, request.getReason());
        return ResponseEntity.ok(ApiResponse.success("신고가 접수되었습니다."));
    }
}