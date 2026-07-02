package com.example.projectback.transaction.controller;

import com.example.projectback.common.ApiResponse;
import com.example.projectback.transaction.dto.ReviewRequest;
import com.example.projectback.transaction.dto.ReviewResponse;
import com.example.projectback.transaction.dto.TransactionRequest;
import com.example.projectback.transaction.dto.TransactionResponse;
import com.example.projectback.transaction.repository.TransactionRepository;
import com.example.projectback.transaction.service.ReviewService;
import com.example.projectback.transaction.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;
    private final ReviewService reviewService;
    private final SimpMessagingTemplate messagingTemplate;
    private final TransactionRepository transactionRepository;

    @PostMapping("/transactions")
    public ResponseEntity<ApiResponse<TransactionResponse>> completeTransaction(
            @RequestBody TransactionRequest request) {
        TransactionResponse response = transactionService.completeTransaction(request);

        Map<String, Long> reviewPayload = Map.of(
                "transactionId", response.getTransactionId(),
                "chatRoomId", response.getChatRoomId()
        );
        sendReviewRequest(response.getBuyerId(), reviewPayload);
        if (!response.getSellerId().equals(response.getBuyerId())) {
            sendReviewRequest(response.getSellerId(), reviewPayload);
        }

        return ResponseEntity.ok(ApiResponse.success(response, "거래완료 처리 성공"));
    }

    private void sendReviewRequest(Long userId, Map<String, Long> reviewPayload) {
        messagingTemplate.convertAndSend("/topic/review/" + userId, reviewPayload);
        messagingTemplate.convertAndSend("/topic/notification/" + userId, true);
    }

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

    @PostMapping("/reviews")
    public ResponseEntity<ApiResponse<ReviewResponse>> createReview(
            @RequestBody ReviewRequest request) {
        ReviewResponse response = reviewService.createReview(request);
        return ResponseEntity.ok(ApiResponse.success(response, "리뷰 작성 성공"));
    }
}