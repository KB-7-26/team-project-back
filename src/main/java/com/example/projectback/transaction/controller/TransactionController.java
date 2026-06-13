package com.example.projectback.transaction.controller;

import com.example.projectback.common.ApiResponse;
import com.example.projectback.transaction.dto.ReviewRequest;
import com.example.projectback.transaction.dto.ReviewResponse;
import com.example.projectback.transaction.dto.TransactionRequest;
import com.example.projectback.transaction.dto.TransactionResponse;
import com.example.projectback.transaction.service.ReviewService;
import com.example.projectback.transaction.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;
    private final ReviewService reviewService;
    private final SimpMessagingTemplate messagingTemplate;
    private final com.example.projectback.transaction.repository.TransactionRepository transactionRepository;

    // 거래완료 - 채팅방에서 거래완료 버튼 클릭 시
    @PostMapping("/transactions")
    public ResponseEntity<ApiResponse<TransactionResponse>> completeTransaction(
            @RequestBody TransactionRequest request) {
        TransactionResponse response = transactionService.completeTransaction(request);

        // 구매자에게 별점 요청 푸시 (transactionId + chatRoomId)
        java.util.Map<String, Long> reviewPayload = java.util.Map.of(
            "transactionId", response.getTransactionId(),
            "chatRoomId", response.getChatRoomId()
        );
        messagingTemplate.convertAndSend("/topic/review/" + response.getBuyerId(), reviewPayload);
        // 구매자 네비바 빨간 점 표시 (채팅 온 것처럼)
        messagingTemplate.convertAndSend(
            "/topic/notification/" + response.getBuyerId(),
            true
        );

        return ResponseEntity.ok(ApiResponse.success(response, "거래완료 처리 성공"));
    }

    // 채팅방의 거래 정보 조회 (거래완료 여부 확인용)
    @GetMapping("/transactions/by-room/{chatRoomId}")
    public ResponseEntity<ApiResponse<TransactionResponse>> getTransactionByRoom(
            @PathVariable Long chatRoomId) {
        return transactionRepository.findByChatRoomId(chatRoomId)
                .map(t -> ResponseEntity.ok(ApiResponse.success(
                        new TransactionResponse(
                                t.getId(), chatRoomId, t.getProduct().getId(), t.getProduct().getTitle(),
                                t.getSeller().getId(), t.getBuyer().getId(),
                                t.getStatus(), t.getCreatedAt(), t.getCompletedAt()
                        ), "거래 정보 조회 성공")))
                .orElse(ResponseEntity.ok(ApiResponse.success(null, "거래 없음")));
    }

    // 리뷰 작성 - 거래완료 후 별점 제출 시
    @PostMapping("/reviews")
    public ResponseEntity<ApiResponse<ReviewResponse>> createReview(
            @RequestBody ReviewRequest request) {
        ReviewResponse response = reviewService.createReview(request);
        return ResponseEntity.ok(ApiResponse.success(response, "리뷰 작성 성공"));
    }
}
