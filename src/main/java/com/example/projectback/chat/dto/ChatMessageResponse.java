package com.example.projectback.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class ChatMessageResponse {

    private Long messageId;
    private Long chatRoomId;
    private Long senderId;
    private String senderNickname;
    private String content;
    private LocalDateTime createdAt;
}
