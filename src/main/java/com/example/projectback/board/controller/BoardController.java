package com.example.projectback.board.controller;

import com.example.projectback.board.dto.BoardPostCreateRequest;
import com.example.projectback.board.dto.BoardPostCreateResponse;
import com.example.projectback.board.dto.BoardPostListItemResponse;
import com.example.projectback.board.service.BoardPostService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class BoardController {

    private final BoardPostService boardPostService;

    @GetMapping
    public ResponseEntity<Page<BoardPostListItemResponse>> getPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(boardPostService.getPosts(pageable));
    }

    @PostMapping
    public ResponseEntity<BoardPostCreateResponse> createPost(
            @RequestBody BoardPostCreateRequest request,
            HttpSession session) {

        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }

        BoardPostCreateResponse response = boardPostService.createPost(userId, request);
        return ResponseEntity.ok(response);
    }
}