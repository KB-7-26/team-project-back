package com.example.projectback.board.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class BoardCommentCreateRequest {
    private String content;
    private Long parentCommentId;
}
