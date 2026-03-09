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
/**
 * 채팅 REST API 컨트롤러.
 * 채팅 이력 조회와 이미지 업로드 엔드포인트를 제공합니다.
 */
public class ChatRestController {

    private final ChatService chatService;

    /**
     * 특정 운송건 채팅 이력을 조회합니다.
     * 헤더의 사용자 정보(X-USER-ID, X-USER-ROLE)로 참여자 권한을 검증합니다.
     */
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

    /**
     * 채팅 이미지 파일을 업로드하고 공개 URL을 반환합니다.
     * 반환된 URL은 STOMP 채팅 메시지(IMG_URL) 전송에 사용됩니다.
     */
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
