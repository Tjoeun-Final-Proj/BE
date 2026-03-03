package com.tjoeun.boxmon.feature.chat.dto;

import com.tjoeun.boxmon.feature.chat.domain.ChatContentType;
import com.tjoeun.boxmon.feature.chat.domain.ChatSenderRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessageResponse {
    private Long chatId;
    private Long shipmentId;
    private Long senderId;
    private ChatSenderRole senderRole;
    private ChatContentType contentType;
    private String content;
    private LocalDateTime createdAt;
}
