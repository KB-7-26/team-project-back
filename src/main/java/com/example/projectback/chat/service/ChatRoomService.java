package com.example.projectback.chat.service;

import com.example.projectback.chat.dto.ChatRoomCreateRequest;
import com.example.projectback.chat.dto.ChatRoomCreateResponse;
import com.example.projectback.chat.dto.ChatRoomListResponse;
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

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;
    private final ProductRepository productRepository;
    private final CurrentUserProvider currentUserProvider;

    @Transactional(readOnly = true)
    public List<ChatRoomListResponse> getChatRooms() {
        User currentUser = currentUserProvider.getCurrentUser();
        Long userId = currentUser.getId();

        return chatRoomRepository
                .findBySellerIdOrBuyerIdOrderByCreatedAtDesc(userId, userId)
                .stream()
                .map(room -> {
                    // 상대방 = 내가 판매자면 구매자, 내가 구매자면 판매자
                    User opponent = room.getSeller().getId().equals(userId)
                            ? room.getBuyer()
                            : room.getSeller();

                    return new ChatRoomListResponse(
                            room.getId(),
                            room.getProduct().getId(),
                            room.getProduct().getTitle(),
                            opponent.getId(),
                            opponent.getNickname(),
                            room.getLastMessageAt(),
                            room.getCreatedAt()
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
