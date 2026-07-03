package com.example.projectback.board.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CommentSocketPayload {
    private final Long commentId;
    private final Long parentCommentId;
    private final Long writerId;
    private final String displayName;
    private final String content;
    private final LocalDateTime createdAt;
}