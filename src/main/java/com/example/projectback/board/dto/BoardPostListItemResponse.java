package com.example.projectback.board.dto;

import com.example.projectback.entity.BoardPost;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class BoardPostListItemResponse {
    private final Long id;
    private final String title;
    private final String displayName;
    private final Integer viewCount;
    private final LocalDateTime createdAt;
    private final long commentCount;
    private final long likeCount;

    public BoardPostListItemResponse(BoardPost post, long commentCount, long likeCount) {
        this.id = post.getId();
        this.title = post.getTitle();
        this.displayName = "익명";
        this.viewCount = post.getViewCount();
        this.createdAt = post.getCreatedAt();
        this.commentCount = commentCount;
        this.likeCount = likeCount;
    }
}