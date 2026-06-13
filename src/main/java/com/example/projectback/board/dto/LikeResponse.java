package com.example.projectback.board.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class LikeResponse {
    private final boolean liked;
    private final long likeCount;
}