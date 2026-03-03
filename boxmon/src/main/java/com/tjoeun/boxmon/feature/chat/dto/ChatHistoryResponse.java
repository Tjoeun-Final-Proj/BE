package com.tjoeun.boxmon.feature.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class ChatHistoryResponse {
    private List<ChatMessageResponse> messages;
}
