package com.example.projectback.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class ChatRoomCreateResponse {

    private Long chatRoomId;
    private Long productId;
    private Long sellerId;
    private Long buyerId;
    private LocalDateTime createdAt;
}
