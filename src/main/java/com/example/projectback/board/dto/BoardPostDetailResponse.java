package com.example.projectback.board.dto;

import com.example.projectback.entity.BoardPost;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class BoardPostDetailResponse {
    private final Long id;
    private final String title;
    private final String content;
    private final String displayName;
    private final Integer viewCount;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;
    private final boolean isOwner;

    public BoardPostDetailResponse(BoardPost post, Long currentUserId) {
        this.id = post.getId();
        this.title = post.getTitle();
        this.content = post.getContent();
        this.displayName = "익명";
        this.viewCount = post.getViewCount();
        this.createdAt = post.getCreatedAt();
        this.updatedAt = post.getUpdatedAt();
        this.isOwner = currentUserId != null && currentUserId.equals(post.getAuthor().getId());
    }
}