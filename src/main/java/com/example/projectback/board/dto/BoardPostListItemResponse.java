package com.example.projectback.board.dto;

import com.example.projectback.entity.BoardPost;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class BoardPostListItemResponse {
    private final Long id;
    private final String title;
    private final String nickname;
    private final Integer viewCount;
    private final LocalDateTime createdAt;

    public BoardPostListItemResponse(BoardPost post) {
        this.id = post.getId();
        this.title = post.getTitle();
        this.nickname = post.getIsAnonymous() ? "익명" : post.getAuthor().getNickname();
        this.viewCount = post.getViewCount();
        this.createdAt = post.getCreatedAt();
    }
}