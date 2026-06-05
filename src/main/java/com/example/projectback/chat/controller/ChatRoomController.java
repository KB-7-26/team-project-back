package com.example.projectback.chat.controller;

import com.example.projectback.chat.dto.ChatRoomCreateRequest;
import com.example.projectback.chat.dto.ChatRoomCreateResponse;
import com.example.projectback.chat.dto.ChatRoomListResponse;
import com.example.projectback.chat.service.ChatRoomService;
import com.example.projectback.common.ApiResponse;
import com.example.projectback.security.CurrentUserProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

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

    // 채팅방 입장 시 읽음 처리 → 읽은 사람의 userId 포함해서 이벤트 전송
    @PatchMapping("/{roomId}/read")
    public ResponseEntity<ApiResponse<Void>> markAsRead(@PathVariable Long roomId) {
        chatRoomService.markAsRead(roomId);
        Long readerId = currentUserProvider.getCurrentUser().getId();
        messagingTemplate.convertAndSend("/topic/chat/" + roomId + "/read", readerId);
        return ResponseEntity.ok(ApiResponse.success(null, "읽음 처리 성공"));
    }

    // 전체 안 읽은 메시지 수 (네비바 뱃지용)
    @GetMapping("/unread-count")
    public ResponseEntity<ApiResponse<Long>> getUnreadCount() {
        long count = chatRoomService.getTotalUnreadCount();
        return ResponseEntity.ok(ApiResponse.success(count, "안 읽은 메시지 수 조회 성공"));
    }
}
