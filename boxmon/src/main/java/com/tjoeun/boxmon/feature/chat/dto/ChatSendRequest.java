package com.tjoeun.boxmon.feature.chat.dto;

import com.tjoeun.boxmon.feature.chat.domain.ChatContentType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChatSendRequest {
    private ChatContentType contentType;
    private String content;
}
