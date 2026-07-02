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
    private String productImageUrl;
    private Integer productPrice;
    private Boolean productIsFree;
    private Long opponentId;
    private String opponentNickname;
    private String opponentProfileImageUrl;
    private LocalDateTime lastMessageAt;
    private LocalDateTime createdAt;
    private long unreadCount;
    private LocalDateTime opponentLastReadAt;
    private Long sellerId;
    private String lastMessage; // 마지막 메시지 내용 미리보기
}
