package com.example.projectback.board.controller;

import com.example.projectback.board.dto.BoardPostCreateRequest;
import com.example.projectback.board.dto.BoardPostCreateResponse;
import com.example.projectback.board.dto.BoardPostDetailResponse;
import com.example.projectback.board.dto.BoardPostListItemResponse;
import com.example.projectback.board.dto.BoardPostUpdateRequest;
import com.example.projectback.board.service.BoardPostService;
import com.example.projectback.common.ApiResponse;
import com.example.projectback.security.CurrentUserProvider;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;

import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class BoardController {

    private final BoardPostService boardPostService;
    private final CurrentUserProvider currentUserProvider;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<BoardPostListItemResponse>>> getPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<BoardPostListItemResponse> response = boardPostService.getPosts(pageable);
        return ResponseEntity.ok(ApiResponse.success(response, "게시글 목록 조회 성공"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BoardPostDetailResponse>> getPostDetail(@PathVariable Long id) {
        Long currentUserId = null;
        try {
            currentUserId = currentUserProvider.getCurrentUserId();
        } catch (Exception ignored) {
        }

        try {
            BoardPostDetailResponse response = boardPostService.getPostDetail(id, currentUserId);
            return ResponseEntity.ok(ApiResponse.success(response, "게시글 상세 조회 성공"));
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.failure(e.getMessage()));
        }
    }

    @PostMapping
    public ResponseEntity<ApiResponse<BoardPostCreateResponse>> createPost(
            @RequestBody BoardPostCreateRequest request,
            HttpSession session) {

        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.failure("인증이 필요합니다."));
        }

        BoardPostCreateResponse response = boardPostService.createPost(userId, request);
        return ResponseEntity.ok(ApiResponse.success(response, "게시글 등록 성공"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<BoardPostDetailResponse>> updatePost(
            @PathVariable Long id,
            @RequestBody BoardPostUpdateRequest request,
            HttpSession session) {

        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.failure("인증이 필요합니다."));
        }

        try {
            BoardPostDetailResponse response = boardPostService.updatePost(id, userId, request);
            return ResponseEntity.ok(ApiResponse.success(response, "게시글 수정 성공"));
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.failure(e.getMessage()));
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.failure(e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deletePost(
            @PathVariable Long id,
            HttpSession session) {

        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.failure("인증이 필요합니다."));
        }

        try {
            boardPostService.deletePost(id, userId);
            return ResponseEntity.ok(ApiResponse.success("게시글 삭제 성공"));
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.failure(e.getMessage()));
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.failure(e.getMessage()));
        }
    }
}
