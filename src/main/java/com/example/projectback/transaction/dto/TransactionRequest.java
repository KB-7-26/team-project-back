package com.example.projectback.transaction.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class TransactionRequest {
    private Long chatRoomId; // 어떤 채팅방에서 거래완료 눌렀는지
}
