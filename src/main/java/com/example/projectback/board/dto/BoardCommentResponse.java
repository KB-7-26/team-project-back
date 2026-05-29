package com.example.projectback.board.dto;

import com.example.projectback.entity.BoardComment;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
public class BoardCommentResponse {
    private final Long id;
    private final String displayName;
    private final String content;
    private final LocalDateTime createdAt;
    private final List<BoardCommentResponse> replies;

    public BoardCommentResponse(BoardComment comment, String displayName, List<BoardCommentResponse> replies) {
        this.id = comment.getId();
        this.displayName = displayName;
        this.content = comment.getContent();
        this.createdAt = comment.getCreatedAt();
        this.replies = replies;
    }
}
