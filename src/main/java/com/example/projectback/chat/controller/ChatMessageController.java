package com.example.projectback.chat.controller;

import com.example.projectback.chat.dto.ChatMessageRequest;
import com.example.projectback.chat.dto.ChatMessageResponse;
import com.example.projectback.chat.service.ChatMessageService;
import com.example.projectback.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ChatMessageController {

    private final ChatMessageService chatMessageService;
    private final SimpMessagingTemplate messagingTemplate;

    // WebSocket: 메시지 전송
    // 프론트가 /app/chat/{roomId}/send 로 보내면 여기서 처리
    @MessageMapping("/chat/{roomId}/send")
    public void sendMessage(@DestinationVariable Long roomId,
                            ChatMessageRequest request) {
        // DB에 저장
        ChatMessageResponse response = chatMessageService.saveMessage(roomId, request);

        // 같은 채팅방에 있는 모든 사람에게 브로드캐스트
        messagingTemplate.convertAndSend("/topic/chat/" + roomId, response);
    }

    // REST: 채팅방 메시지 목록 조회 (채팅방 입장 시 이전 메시지 불러오기)
    @GetMapping("/api/chat/rooms/{roomId}/messages")
    public ResponseEntity<ApiResponse<List<ChatMessageResponse>>> getMessages(
            @PathVariable Long roomId) {
        List<ChatMessageResponse> response = chatMessageService.getMessages(roomId);
        return ResponseEntity.ok(ApiResponse.success(response, "메시지 목록 조회 성공"));
    }
}
