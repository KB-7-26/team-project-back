package com.example.projectback.board.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class BoardPostCreateResponse {
    private Long id;
    private String title;
    private LocalDateTime createdAt;
}