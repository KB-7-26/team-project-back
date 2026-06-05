package com.example.projectback.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class ChatRoomListResponse {

    private Long chatRoomId;
    private Long productId;
    private String productTitle;
    private Long opponentId;
    private String opponentNickname;
    private LocalDateTime lastMessageAt;
    private LocalDateTime createdAt;
    private long unreadCount;
    private LocalDateTime opponentLastReadAt; // 상대방이 마지막으로 읽은 시간
}
