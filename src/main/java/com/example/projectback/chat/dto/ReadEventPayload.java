package com.example.projectback.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class ReadEventPayload {
    private Long readerId;
    private LocalDateTime readAt;
}