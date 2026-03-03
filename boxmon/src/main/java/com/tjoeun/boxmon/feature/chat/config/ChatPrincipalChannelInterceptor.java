package com.tjoeun.boxmon.feature.chat.config;

import com.tjoeun.boxmon.exception.ChatValidationException;
import com.tjoeun.boxmon.feature.chat.domain.ChatSenderRole;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class ChatPrincipalChannelInterceptor implements ChannelInterceptor {

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null || accessor.getCommand() == null) {
            return message;
        }

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            Long userId = parseRequiredUserId(accessor);
            ChatSenderRole role = ChatSenderRole.from(getRequiredHeader(accessor, "X-USER-ROLE"));

            Map<String, Object> sessionAttributes = accessor.getSessionAttributes();
            if (sessionAttributes == null) {
                throw new ChatValidationException("채팅 세션을 초기화할 수 없습니다.");
            }
            sessionAttributes.put("userId", userId);
            sessionAttributes.put("userRole", role);
        }

        return message;
    }

    private Long parseRequiredUserId(StompHeaderAccessor accessor) {
        String rawUserId = getRequiredHeader(accessor, "X-USER-ID");
        try {
            return Long.valueOf(rawUserId);
        } catch (NumberFormatException e) {
            throw new ChatValidationException("X-USER-ID는 숫자여야 합니다.");
        }
    }

    private String getRequiredHeader(StompHeaderAccessor accessor, String name) {
        List<String> values = accessor.getNativeHeader(name);
        if (values == null || values.isEmpty() || values.get(0) == null || values.get(0).isBlank()) {
            throw new ChatValidationException(name + " 헤더가 필요합니다.");
        }
        return values.get(0);
    }
}
