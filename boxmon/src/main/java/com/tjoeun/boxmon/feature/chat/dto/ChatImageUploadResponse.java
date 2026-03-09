package com.tjoeun.boxmon.feature.chat.dto;

import com.tjoeun.boxmon.feature.chat.domain.ChatContentType;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ChatImageUploadResponse {
    private String imageUrl;
    private ChatContentType contentType;
}
