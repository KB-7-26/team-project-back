package com.example.projectback.chat.repository;

import com.example.projectback.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    List<ChatMessage> findByChatRoomIdOrderByCreatedAtAsc(Long chatRoomId);

    long countByChatRoomIdAndCreatedAtAfterAndSenderIdNot(Long chatRoomId, LocalDateTime after, Long senderId);

    long countByChatRoomIdAndSenderIdNot(Long chatRoomId, Long senderId);

    // 채팅방의 가장 최신 메시지 1개
    java.util.Optional<ChatMessage> findTopByChatRoomIdOrderByCreatedAtDesc(Long chatRoomId);

    // 여러 채팅방의 최신 메시지를 한 번에 조회 (N+1 방지)
    @org.springframework.data.jpa.repository.Query(
        "SELECT cm FROM ChatMessage cm WHERE cm.id IN " +
        "(SELECT MAX(m.id) FROM ChatMessage m WHERE m.chatRoom.id IN :roomIds GROUP BY m.chatRoom.id)"
    )
    List<ChatMessage> findLatestMessagesByRoomIds(
        @org.springframework.data.repository.query.Param("roomIds") List<Long> roomIds
    );

    // 여러 채팅방의 안 읽은 메시지 수를 한 번에 조회 (N+1 방지)
    @org.springframework.data.jpa.repository.Query(
        "SELECT cm.chatRoom.id, COUNT(cm) FROM ChatMessage cm " +
        "WHERE cm.chatRoom.id IN :roomIds AND cm.sender.id <> :userId " +
        "AND cm.createdAt > :since GROUP BY cm.chatRoom.id"
    )
    List<Object[]> countUnreadByRoomIdsAndSince(
        @org.springframework.data.repository.query.Param("roomIds") List<Long> roomIds,
        @org.springframework.data.repository.query.Param("userId") Long userId,
        @org.springframework.data.repository.query.Param("since") java.time.LocalDateTime since
    );
}
