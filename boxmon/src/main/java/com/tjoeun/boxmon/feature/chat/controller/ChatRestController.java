package com.tjoeun.boxmon.feature.chat.controller;

import com.tjoeun.boxmon.feature.chat.domain.ChatSenderRole;
import com.tjoeun.boxmon.feature.chat.dto.ChatHistoryResponse;
import com.tjoeun.boxmon.feature.chat.dto.ChatImageUploadResponse;
import com.tjoeun.boxmon.feature.chat.dto.ChatMessageResponse;
import com.tjoeun.boxmon.feature.chat.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatRestController {

    private final ChatService chatService;

    @GetMapping("/{shipmentId}/messages")
    public ResponseEntity<ChatHistoryResponse> getMessages(
            @PathVariable Long shipmentId,
            @RequestHeader("X-USER-ID") Long userId,
            @RequestHeader("X-USER-ROLE") String userRole
    ) {
        ChatSenderRole role = ChatSenderRole.from(userRole);
        List<ChatMessageResponse> messages = chatService.getMessages(shipmentId, userId, role);
        return ResponseEntity.ok(new ChatHistoryResponse(messages));
    }

    @PostMapping(value = "/{shipmentId}/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ChatImageUploadResponse> uploadImage(
            @PathVariable Long shipmentId,
            @RequestHeader("X-USER-ID") Long userId,
            @RequestHeader("X-USER-ROLE") String userRole,
            @RequestPart("image") MultipartFile image
    ) {
        ChatSenderRole role = ChatSenderRole.from(userRole);
        ChatImageUploadResponse response = chatService.uploadImage(shipmentId, userId, role, image);
        return ResponseEntity.ok(response);
    }
}
