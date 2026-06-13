package com.example.projectback.user.controller;

import com.example.projectback.board.dto.BoardPostListItemResponse;
import com.example.projectback.common.ApiResponse;
import com.example.projectback.user.service.UserBoardActivityService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users/me")
@RequiredArgsConstructor
public class UserBoardActivityController {

    private final UserBoardActivityService userBoardActivityService;

    @GetMapping("/board-posts")
    public ResponseEntity<ApiResponse<Page<BoardPostListItemResponse>>> getMyPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = createPageable(page, size);
        Page<BoardPostListItemResponse> response = userBoardActivityService.getMyPosts(pageable);
        return ResponseEntity.ok(ApiResponse.success(response, "내가 쓴 글 조회 성공"));
    }

    @GetMapping("/commented-board-posts")
    public ResponseEntity<ApiResponse<Page<BoardPostListItemResponse>>> getMyCommentedPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = createPageable(page, size);
        Page<BoardPostListItemResponse> response = userBoardActivityService.getMyCommentedPosts(pageable);
        return ResponseEntity.ok(ApiResponse.success(response, "댓글 단 글 조회 성공"));
    }

    private Pageable createPageable(int page, int size) {
        return PageRequest.of(page, size, Sort.by("createdAt").descending());
    }
}
