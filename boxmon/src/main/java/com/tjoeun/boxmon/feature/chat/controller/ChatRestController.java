package com.tjoeun.boxmon.feature.chat.controller;

import com.tjoeun.boxmon.feature.chat.domain.ChatSenderRole;
import com.tjoeun.boxmon.feature.chat.dto.ChatHistoryResponse;
import com.tjoeun.boxmon.feature.chat.dto.ChatMessageResponse;
import com.tjoeun.boxmon.feature.chat.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
