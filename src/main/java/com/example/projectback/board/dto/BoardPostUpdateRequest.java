package com.example.projectback.board.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class BoardPostUpdateRequest {
    private String title;
    private String content;
    private Boolean isAnonymous;
}
