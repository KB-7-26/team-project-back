package com.example.projectback.chat.service;

import com.example.projectback.chat.dto.ChatRoomCreateRequest;
import com.example.projectback.chat.dto.ChatRoomCreateResponse;
import com.example.projectback.chat.dto.ChatRoomListResponse;
import com.example.projectback.chat.repository.ChatMessageRepository;
import com.example.projectback.chat.repository.ChatRoomRepository;
import com.example.projectback.entity.ChatRoom;
import com.example.projectback.entity.Product;
import com.example.projectback.entity.User;
import com.example.projectback.product.repository.ProductRepository;
import com.example.projectback.security.CurrentUserProvider;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ProductRepository productRepository;
    private final CurrentUserProvider currentUserProvider;

    // 채팅방에서 상대방 ID 반환
    @Transactional(readOnly = true)
    public Long getOpponentId(Long roomId, Long myId) {
        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new EntityNotFoundException("채팅방을 찾을 수 없습니다."));
        return chatRoom.getSeller().getId().equals(myId)
                ? chatRoom.getBuyer().getId()
                : chatRoom.getSeller().getId();
    }

    // 채팅방 입장 시 호출 → 내 lastReadAt 갱신
    @Transactional
    public void markAsRead(Long roomId) {
        User currentUser = currentUserProvider.getCurrentUser();
        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new EntityNotFoundException("채팅방을 찾을 수 없습니다."));

        boolean isSeller = chatRoom.getSeller().getId().equals(currentUser.getId());
        chatRoom.updateLastReadAt(isSeller);
    }

    // 내 전체 채팅방의 안 읽은 메시지 합산 (네비바 뱃지용)
    @Transactional(readOnly = true)
    public long getTotalUnreadCount() {
        User currentUser = currentUserProvider.getCurrentUser();
        Long userId = currentUser.getId();

        return chatRoomRepository
                .findBySellerIdOrBuyerIdOrderByCreatedAtDesc(userId, userId)
                .stream()
                .mapToLong(room -> {
                    boolean isSeller = room.getSeller().getId().equals(userId);
                    LocalDateTime lastReadAt = isSeller ? room.getSellerLastReadAt() : room.getBuyerLastReadAt();

                    if (lastReadAt == null) {
                        // 한 번도 읽지 않은 경우 → 상대방 메시지 전체 카운트
                        return chatMessageRepository.countByChatRoomIdAndSenderIdNot(room.getId(), userId);
                    }
                    return chatMessageRepository.countByChatRoomIdAndCreatedAtAfterAndSenderIdNot(
                            room.getId(), lastReadAt, userId);
                })
                .sum();
    }

    @Transactional(readOnly = true)
    public List<ChatRoomListResponse> getChatRooms() {
        User currentUser = currentUserProvider.getCurrentUser();
        Long userId = currentUser.getId();

        return chatRoomRepository
                .findByUserIdOrderByLastMessageDesc(userId)
                .stream()
                .map(room -> {
                    // 상대방 = 내가 판매자면 구매자, 내가 구매자면 판매자
                    User opponent = room.getSeller().getId().equals(userId)
                            ? room.getBuyer()
                            : room.getSeller();

                    boolean isSeller = room.getSeller().getId().equals(userId);
                    LocalDateTime myLastReadAt = isSeller ? room.getSellerLastReadAt() : room.getBuyerLastReadAt();
                    // 상대방의 마지막 읽은 시간 (내 메시지 읽음 표시 기준)
                    LocalDateTime opponentLastReadAt = isSeller ? room.getBuyerLastReadAt() : room.getSellerLastReadAt();

                    long unread = myLastReadAt == null
                            ? chatMessageRepository.countByChatRoomIdAndSenderIdNot(room.getId(), userId)
                            : chatMessageRepository.countByChatRoomIdAndCreatedAtAfterAndSenderIdNot(room.getId(), myLastReadAt, userId);

                    return new ChatRoomListResponse(
                            room.getId(),
                            room.getProduct().getId(),
                            room.getProduct().getTitle(),
                            opponent.getId(),
                            opponent.getNickname(),
                            room.getLastMessageAt(),
                            room.getCreatedAt(),
                            unread,
                            opponentLastReadAt
                    );
                })
                .toList();
    }

    @Transactional
    public ChatRoomCreateResponse createChatRoom(ChatRoomCreateRequest request) {
        // 현재 로그인한 사용자 = 구매자
        User buyer = currentUserProvider.getCurrentUser();

        // 상품 조회
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new EntityNotFoundException("상품을 찾을 수 없습니다."));

        // 판매자 = 상품 등록자
        User seller = product.getSeller();

        // 이미 채팅방이 존재하면 기존 채팅방 반환
        return chatRoomRepository.findByProductIdAndBuyerId(product.getId(), buyer.getId())
                .map(existing -> new ChatRoomCreateResponse(
                        existing.getId(),
                        existing.getProduct().getId(),
                        existing.getSeller().getId(),
                        existing.getBuyer().getId(),
                        existing.getCreatedAt()
                ))
                .orElseGet(() -> {
                    // 새 채팅방 생성
                    ChatRoom chatRoom = ChatRoom.builder()
                            .product(product)
                            .seller(seller)
                            .buyer(buyer)
                            .build();

                    chatRoomRepository.save(chatRoom);

                    return new ChatRoomCreateResponse(
                            chatRoom.getId(),
                            chatRoom.getProduct().getId(),
                            chatRoom.getSeller().getId(),
                            chatRoom.getBuyer().getId(),
                            chatRoom.getCreatedAt()
                    );
                });
    }
}
