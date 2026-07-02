package com.example.projectback.chat.controller;

import com.example.projectback.chat.dto.ChatRoomCreateRequest;
import com.example.projectback.chat.dto.ChatRoomCreateResponse;
import com.example.projectback.chat.dto.ChatRoomListResponse;
import com.example.projectback.chat.dto.ChatRoomReadResponse;
import com.example.projectback.chat.dto.ReadEventPayload;
import com.example.projectback.chat.service.ChatRoomService;
import com.example.projectback.common.ApiResponse;
import com.example.projectback.security.CurrentUserProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/chat/rooms")
@RequiredArgsConstructor
public class ChatRoomController {

    private final ChatRoomService chatRoomService;
    private final SimpMessagingTemplate messagingTemplate;
    private final CurrentUserProvider currentUserProvider;

    @PostMapping
    public ResponseEntity<ApiResponse<ChatRoomCreateResponse>> createChatRoom(@RequestBody ChatRoomCreateRequest request) {
        ChatRoomCreateResponse response = chatRoomService.createChatRoom(request);
        return ResponseEntity.ok(ApiResponse.success(response, "채팅방 생성 성공"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ChatRoomListResponse>>> getChatRooms() {
        List<ChatRoomListResponse> response = chatRoomService.getChatRooms();
        return ResponseEntity.ok(ApiResponse.success(response, "채팅방 목록 조회 성공"));
    }

    @PatchMapping("/{roomId}/read")
    public ResponseEntity<ApiResponse<ChatRoomReadResponse>> markAsRead(@PathVariable Long roomId) {
        LocalDateTime opponentLastReadAt = chatRoomService.markAsRead(roomId);
        Long readerId = currentUserProvider.getCurrentUser().getId();

        messagingTemplate.convertAndSend(
                "/topic/chat/" + roomId + "/read",
                new ReadEventPayload(readerId, LocalDateTime.now())
        );

        return ResponseEntity.ok(ApiResponse.success(
                new ChatRoomReadResponse(opponentLastReadAt), "읽음 처리 성공"));
    }

    @PatchMapping("/{roomId}/leave")
    public ResponseEntity<ApiResponse<Boolean>> leaveChatRoom(@PathVariable Long roomId) {
        chatRoomService.leaveChatRoom(roomId);
        return ResponseEntity.ok(ApiResponse.success(true, "채팅방 나가기 성공"));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<ApiResponse<Long>> getUnreadCount() {
        long count = chatRoomService.getTotalUnreadCount();
        return ResponseEntity.ok(ApiResponse.success(count, "안 읽은 메시지 수 조회 성공"));
    }
}