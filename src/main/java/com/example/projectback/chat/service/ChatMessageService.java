package com.example.projectback.chat.service;

import com.example.projectback.chat.dto.ChatMessageRequest;
import com.example.projectback.chat.dto.ChatMessageResponse;
import com.example.projectback.entity.ChatMessage;
import com.example.projectback.chat.repository.ChatMessageRepository;
import com.example.projectback.chat.repository.ChatRoomRepository;
import com.example.projectback.entity.ChatRoom;
import com.example.projectback.entity.User;
import com.example.projectback.security.CurrentUserProvider;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatMessageService {

    private final ChatMessageRepository chatMessageRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final CurrentUserProvider currentUserProvider;

    // 메시지 저장 + 응답 반환 (WebSocket용 - User 직접 전달)
    @Transactional
    public ChatMessageResponse saveMessage(Long chatRoomId, ChatMessageRequest request, User sender) {

        // 채팅방 조회
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new EntityNotFoundException("채팅방을 찾을 수 없습니다."));

        // 메시지 저장
        ChatMessage message = ChatMessage.builder()
                .chatRoom(chatRoom)
                .sender(sender)
                .content(request.getContent())
                .build();

        chatMessageRepository.save(message);

        return new ChatMessageResponse(
                message.getId(),
                chatRoomId,
                sender.getId(),
                sender.getNickname(),
                message.getContent(),
                message.getCreatedAt()
        );
    }

    // 채팅방 메시지 목록 조회
    @Transactional(readOnly = true)
    public List<ChatMessageResponse> getMessages(Long chatRoomId) {
        return chatMessageRepository
                .findByChatRoomIdOrderByCreatedAtAsc(chatRoomId)
                .stream()
                .map(msg -> new ChatMessageResponse(
                        msg.getId(),
                        chatRoomId,
                        msg.getSender().getId(),
                        msg.getSender().getNickname(),
                        msg.getContent(),
                        msg.getCreatedAt()
                ))
                .toList();
    }
}
