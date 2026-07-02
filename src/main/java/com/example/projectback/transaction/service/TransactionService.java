package com.example.projectback.transaction.service;

import com.example.projectback.chat.repository.ChatRoomRepository;
import com.example.projectback.entity.ChatRoom;
import com.example.projectback.entity.Product;
import com.example.projectback.entity.Transaction;
import com.example.projectback.entity.User;
import com.example.projectback.security.CurrentUserProvider;
import com.example.projectback.transaction.dto.TransactionRequest;
import com.example.projectback.transaction.dto.TransactionResponse;
import com.example.projectback.transaction.repository.TransactionRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final CurrentUserProvider currentUserProvider;

    @Transactional
    public TransactionResponse completeTransaction(TransactionRequest request) {
        User currentUser = currentUserProvider.getCurrentUser();

        ChatRoom chatRoom = chatRoomRepository.findById(request.getChatRoomId())
                .orElseThrow(() -> new EntityNotFoundException("채팅방을 찾을 수 없습니다."));

        // 판매자만 거래완료 가능
        if (!chatRoom.getSeller().getId().equals(currentUser.getId())) {
            throw new IllegalStateException("판매자만 거래완료를 할 수 있습니다.");
        }

        // 이미 완료된 거래면 기존 거래 반환
        java.util.Optional<Transaction> existing = transactionRepository.findByChatRoomId(chatRoom.getId());
        if (existing.isPresent() && existing.get().getStatus().equals("completed")) {
            Transaction t = existing.get();
            return new TransactionResponse(
                    t.getId(), chatRoom.getId(), t.getProduct().getId(), t.getProduct().getTitle(),
                    t.getSeller().getId(), t.getBuyer().getId(),
                    t.getStatus(), t.getCreatedAt(), t.getCompletedAt()
            );
        }

        Transaction transaction = Transaction.builder()
                .product(chatRoom.getProduct())
                .seller(chatRoom.getSeller())
                .buyer(chatRoom.getBuyer())
                .chatRoom(chatRoom)
                .status("completed")
                .build();

        // completedAt 설정을 위해 저장 후 업데이트
        transactionRepository.save(transaction);
        transaction.complete();

        // 상품 상태를 판매완료로 변경
        Product product = chatRoom.getProduct();
        product.updateSaleStatus("sold");

        return new TransactionResponse(
                transaction.getId(),
                chatRoom.getId(),
                transaction.getProduct().getId(),
                transaction.getProduct().getTitle(),
                transaction.getSeller().getId(),
                transaction.getBuyer().getId(),
                transaction.getStatus(),
                transaction.getCreatedAt(),
                transaction.getCompletedAt()
        );
    }
}
