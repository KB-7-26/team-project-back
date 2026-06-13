package com.example.projectback.transaction.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class TransactionResponse {
    private Long transactionId;
    private Long chatRoomId;
    private Long productId;
    private String productTitle;
    private Long sellerId;
    private Long buyerId;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
}
