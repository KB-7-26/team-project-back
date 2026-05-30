package com.example.projectback.chat.controller;

import com.example.projectback.chat.dto.ChatRoomCreateRequest;
import com.example.projectback.chat.dto.ChatRoomCreateResponse;
import com.example.projectback.chat.service.ChatRoomService;
import com.example.projectback.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chat/rooms")
@RequiredArgsConstructor
public class ChatRoomController {

    private final ChatRoomService chatRoomService;

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
}
