package com.example.projectback.chat.service;

import com.example.projectback.chat.dto.ChatMessageRequest;
import com.example.projectback.chat.dto.ChatMessageResponse;
import com.example.projectback.chat.repository.ChatMessageRepository;
import com.example.projectback.chat.repository.ChatRoomRepository;
import com.example.projectback.entity.ChatMessage;
import com.example.projectback.entity.ChatRoom;
import com.example.projectback.entity.User;
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

    @Transactional
    public ChatMessageResponse saveMessage(Long chatRoomId, ChatMessageRequest request, User sender) {
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new EntityNotFoundException("채팅방을 찾을 수 없습니다."));

        if (!chatRoom.isParticipant(sender.getId())) {
            throw new IllegalStateException("채팅방 참여자만 메시지를 보낼 수 있습니다.");
        }

        chatRoom.rejoin(sender.getId());

        ChatMessage message = ChatMessage.builder()
                .chatRoom(chatRoom)
                .sender(sender)
                .content(request.getContent())
                .build();

        chatMessageRepository.save(message);
        chatRoom.updateLastMessageAt();

        return new ChatMessageResponse(
                message.getId(),
                chatRoomId,
                sender.getId(),
                sender.getNickname(),
                sender.getProfileImageUrl(),
                message.getContent(),
                message.getCreatedAt()
        );
    }

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
                        msg.getSender().getProfileImageUrl(),
                        msg.getContent(),
                        msg.getCreatedAt()
                ))
                .toList();
    }
}