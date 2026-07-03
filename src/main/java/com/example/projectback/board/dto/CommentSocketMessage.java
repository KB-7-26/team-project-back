package com.example.projectback.board.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CommentSocketMessage {
    private final String type;
    private final CommentSocketPayload comment;
}