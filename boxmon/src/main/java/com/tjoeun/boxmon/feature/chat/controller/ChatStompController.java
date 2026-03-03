package com.tjoeun.boxmon.feature.chat.controller;

import com.tjoeun.boxmon.exception.ChatValidationException;
import com.tjoeun.boxmon.feature.chat.domain.ChatSenderRole;
import com.tjoeun.boxmon.feature.chat.dto.ChatMessageResponse;
import com.tjoeun.boxmon.feature.chat.dto.ChatSendRequest;
import com.tjoeun.boxmon.feature.chat.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.Map;

@Controller
@RequiredArgsConstructor
public class ChatStompController {

    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/chat.send.{shipmentId}")
    public void sendMessage(
            @DestinationVariable Long shipmentId,
            @Payload ChatSendRequest request,
            SimpMessageHeaderAccessor headerAccessor
    ) {
        Map<String, Object> attributes = headerAccessor.getSessionAttributes();
        if (attributes == null) {
            throw new ChatValidationException("세션 사용자 정보가 없습니다.");
        }

        Object userIdValue = attributes.get("userId");
        Object userRoleValue = attributes.get("userRole");

        if (!(userIdValue instanceof Long userId) || !(userRoleValue instanceof ChatSenderRole senderRole)) {
            throw new ChatValidationException("채팅 사용자 정보가 올바르지 않습니다.");
        }

        ChatMessageResponse response = chatService.sendMessage(shipmentId, userId, senderRole, request);
        messagingTemplate.convertAndSend("/sub/chat.room." + shipmentId, response);
    }
}
