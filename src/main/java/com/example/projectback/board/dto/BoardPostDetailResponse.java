package com.example.projectback.board.dto;

import com.example.projectback.entity.BoardPost;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class BoardPostDetailResponse {
    private final Long id;
    private final String category;
    private final String title;
    private final String content;
    private final String displayName;
    private final Integer viewCount;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;
    @JsonProperty("isOwner")
    private final boolean isOwner;
    private final boolean liked;
    private final long likeCount;

    public BoardPostDetailResponse(BoardPost post, Long currentUserId, boolean liked, long likeCount) {
        this.id = post.getId();
        this.category = post.getCategory();
        this.title = post.getTitle();
        this.content = post.getContent();
        this.displayName = "익명";
        this.viewCount = post.getViewCount();
        this.createdAt = post.getCreatedAt();
        this.updatedAt = post.getUpdatedAt();
        this.isOwner = currentUserId != null && currentUserId.equals(post.getAuthor().getId());
        this.liked = liked;
        this.likeCount = likeCount;
    }
}