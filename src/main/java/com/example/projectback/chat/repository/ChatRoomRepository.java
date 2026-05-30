package com.example.projectback.chat.repository;

import com.example.projectback.entity.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

    Optional<ChatRoom> findByProductIdAndBuyerId(Long productId, Long buyerId);

    List<ChatRoom> findBySellerIdOrBuyerIdOrderByCreatedAtDesc(Long sellerId, Long buyerId);
}
