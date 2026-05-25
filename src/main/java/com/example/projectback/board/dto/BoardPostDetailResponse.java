package com.example.projectback.board.dto;

import com.example.projectback.entity.BoardPost;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class BoardPostDetailResponse {
    private final Long id;
    private final String title;
    private final String content;
    private final String nickname;
    private final Boolean isAnonymous;
    private final Integer viewCount;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;
    private final boolean isOwner;

    public BoardPostDetailResponse(BoardPost post, Long currentUserId) {
        this.id = post.getId();
        this.title = post.getTitle();
        this.content = post.getContent();
        this.nickname = post.getIsAnonymous() ? "익명" : post.getAuthor().getNickname();
        this.isAnonymous = post.getIsAnonymous();
        this.viewCount = post.getViewCount();
        this.createdAt = post.getCreatedAt();
        this.updatedAt = post.getUpdatedAt();
        this.isOwner = currentUserId != null && currentUserId.equals(post.getAuthor().getId());
    }
}