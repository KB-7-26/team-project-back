package com.example.projectback.chat.repository;

import com.example.projectback.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    List<ChatMessage> findByChatRoomIdOrderByCreatedAtAsc(Long chatRoomId);

    // lastReadAt 이후에 상대방이 보낸 메시지 수 (안 읽은 메시지 수)
    long countByChatRoomIdAndCreatedAtAfterAndSenderIdNot(Long chatRoomId, LocalDateTime after, Long senderId);

    // lastReadAt이 null인 경우 (한 번도 읽지 않은 경우) - 상대방 메시지 전체 수
    long countByChatRoomIdAndSenderIdNot(Long chatRoomId, Long senderId);
}
