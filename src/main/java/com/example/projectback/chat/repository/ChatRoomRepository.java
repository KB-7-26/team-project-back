package com.example.projectback.chat.repository;

import com.example.projectback.entity.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

    Optional<ChatRoom> findByProductIdAndBuyerId(Long productId, Long buyerId);

    boolean existsByProductIdAndBuyerId(Long productId, Long buyerId);

    List<ChatRoom> findBySellerIdOrBuyerIdOrderByCreatedAtDesc(Long sellerId, Long buyerId);

    // 최신 메시지 기준 정렬 (lastMessageAt이 없으면 createdAt 기준)
    @org.springframework.data.jpa.repository.Query(
        "SELECT r FROM ChatRoom r WHERE r.seller.id = :userId OR r.buyer.id = :userId " +
        "ORDER BY COALESCE(r.lastMessageAt, r.createdAt) DESC"
    )
    List<ChatRoom> findByUserIdOrderByLastMessageDesc(@org.springframework.data.repository.query.Param("userId") Long userId);

    @Query("SELECT r.id FROM ChatRoom r WHERE r.seller.id = :userId OR r.buyer.id = :userId")
    List<Long> findIdsByUserId(@Param("userId") Long userId);

    void deleteByIdIn(List<Long> ids);
}
