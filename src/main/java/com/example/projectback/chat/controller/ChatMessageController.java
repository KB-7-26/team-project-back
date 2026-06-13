package com.example.projectback.chat.controller;

import com.example.projectback.chat.dto.ChatMessageRequest;
import com.example.projectback.chat.dto.ChatMessageResponse;
import com.example.projectback.chat.service.ChatMessageService;
import com.example.projectback.chat.service.ChatRoomService;
import com.example.projectback.common.ApiResponse;
import com.example.projectback.security.FirebaseUserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class ChatMessageController {

    private final ChatMessageService chatMessageService;
    private final ChatRoomService chatRoomService;
    private final SimpMessagingTemplate messagingTemplate;

    // WebSocket: 메시지 전송
    @MessageMapping("/chat/{roomId}/send")
    public void sendMessage(@DestinationVariable Long roomId,
                            ChatMessageRequest request,
                            Principal principal) {
        // accessor.setUser()로 설정한 인증 정보에서 유저 추출
        FirebaseUserPrincipal userPrincipal = (FirebaseUserPrincipal)
                ((UsernamePasswordAuthenticationToken) principal).getPrincipal();

        ChatMessageResponse response = chatMessageService.saveMessage(roomId, request, userPrincipal.getUser());
        messagingTemplate.convertAndSend("/topic/chat/" + roomId, response);

        // 수신자에게 알림 푸시 (수신자가 채팅방 밖에 있어도 뱃지 갱신)
        Long receiverId = chatRoomService.getOpponentId(roomId, userPrincipal.getUser().getId());
        messagingTemplate.convertAndSend("/topic/notification/" + receiverId, true);

    }

    // REST: 채팅방 메시지 목록 조회 (채팅방 입장 시 이전 메시지 불러오기)
    @GetMapping("/api/chat/rooms/{roomId}/messages")
    public ResponseEntity<ApiResponse<List<ChatMessageResponse>>> getMessages(
            @PathVariable Long roomId) {
        List<ChatMessageResponse> response = chatMessageService.getMessages(roomId);
        return ResponseEntity.ok(ApiResponse.success(response, "메시지 목록 조회 성공"));
    }
}
