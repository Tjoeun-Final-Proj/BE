package com.tjoeun.boxmon.feature.chat.service;

import com.tjoeun.boxmon.exception.ChatValidationException;
import com.tjoeun.boxmon.exception.InvalidChatAccessException;
import com.tjoeun.boxmon.exception.ShipmentNotFoundException;
import com.tjoeun.boxmon.feature.chat.domain.Chat;
import com.tjoeun.boxmon.feature.chat.domain.ChatContentType;
import com.tjoeun.boxmon.feature.chat.domain.ChatSenderRole;
import com.tjoeun.boxmon.feature.chat.dto.ChatImageUploadResponse;
import com.tjoeun.boxmon.feature.chat.dto.ChatMessageResponse;
import com.tjoeun.boxmon.feature.chat.dto.ChatSendRequest;
import com.tjoeun.boxmon.feature.chat.repository.ChatRepository;
import com.tjoeun.boxmon.feature.notification.service.NotificationUseCase;
import com.tjoeun.boxmon.feature.shipment.domain.Shipment;
import com.tjoeun.boxmon.feature.shipment.domain.ShipmentStatus;
import com.tjoeun.boxmon.feature.shipment.repository.ShipmentRepository;
import com.tjoeun.boxmon.global.storage.ObjectStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatService {

    private final ChatRepository chatRepository;
    private final ShipmentRepository shipmentRepository;
    private final ObjectStorageService objectStorageService;
    private final NotificationUseCase notificationUseCase;

    public List<ChatMessageResponse> getMessages(Long shipmentId, Long userId, ChatSenderRole role) {
        Shipment shipment = getShipment(shipmentId);
        validateParticipant(shipment, userId, role);

        return chatRepository.findByShipment_ShipmentIdOrderByCreatedAtAscChatIdAsc(shipmentId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public ChatMessageResponse sendMessage(Long shipmentId, Long senderId, ChatSenderRole senderRole, ChatSendRequest request) {
        Shipment shipment = getShipment(shipmentId);
        validateShipmentStatus(shipment.getShipmentStatus());
        validateParticipant(shipment, senderId, senderRole);
        validateRequest(request);

        Chat saved = chatRepository.save(
                Chat.builder()
                        .shipment(shipment)
                        .senderId(senderId)
                        .senderRole(senderRole)
                        .content(request.getContent().trim())
                        .contentType(request.getContentType())
                        .build()
        );

        try {
            // 채팅 저장/전송 흐름은 유지하고 알림 실패는 경고 로그로만 처리
            notificationUseCase.notifyChatMessage(shipmentId, senderId);
        } catch (Exception e) {
            log.warn("채팅 전송은 성공했지만 알림 전송은 건너뜁니다. shipmentId={}", shipmentId, e);
        }

        return toResponse(saved);
    }

    @Transactional
    public ChatImageUploadResponse uploadImage(Long shipmentId, Long userId, ChatSenderRole role, MultipartFile image) {
        Shipment shipment = getShipment(shipmentId);
        validateShipmentStatus(shipment.getShipmentStatus());
        validateParticipant(shipment, userId, role);

        String objectKey = objectStorageService.uploadChatImage(image);
        String imageUrl = objectStorageService.buildPublicUrl(objectKey);
        return new ChatImageUploadResponse(imageUrl, ChatContentType.IMG_URL);
    }

    private Shipment getShipment(Long shipmentId) {
        return shipmentRepository.findById(shipmentId)
                .orElseThrow(() -> new ShipmentNotFoundException("운송건을 찾을 수 없습니다."));
    }

    private void validateShipmentStatus(ShipmentStatus status) {
        if (status == ShipmentStatus.REQUESTED) {
            throw new ChatValidationException("배차 전 상태에서는 채팅을 사용할 수 없습니다.");
        }
    }

    private void validateParticipant(Shipment shipment, Long userId, ChatSenderRole role) {
        if (userId == null || role == null) {
            throw new InvalidChatAccessException("채팅 사용자 정보가 올바르지 않습니다.");
        }

        Long shipperId = shipment.getShipper() != null ? shipment.getShipper().getShipperId() : null;
        Long driverId = shipment.getDriver() != null ? shipment.getDriver().getDriverId() : null;

        switch (role) {
            case SHIPPER -> {
                if (shipperId == null || !shipperId.equals(userId)) {
                    throw new InvalidChatAccessException("해당 운송건의 화주만 접근할 수 있습니다.");
                }
            }
            case DRIVER -> {
                if (driverId == null || !driverId.equals(userId)) {
                    throw new InvalidChatAccessException("해당 운송건의 차주만 접근할 수 있습니다.");
                }
            }
        }
    }

    private void validateRequest(ChatSendRequest request) {
        if (request == null) {
            throw new ChatValidationException("채팅 요청 본문이 필요합니다.");
        }
        if (request.getContentType() == null) {
            throw new ChatValidationException("contentType 값이 필요합니다.");
        }
        if (request.getContent() == null || request.getContent().isBlank()) {
            throw new ChatValidationException("content 값이 비어있습니다.");
        }
        if (request.getContent().length() > 2000) {
            throw new ChatValidationException("content 길이는 2000자를 초과할 수 없습니다.");
        }

        if (request.getContentType() == ChatContentType.IMG_URL && !isValidHttpUrl(request.getContent().trim())) {
            throw new ChatValidationException("IMG_URL 타입은 http/https URL이어야 합니다.");
        }
    }

    private boolean isValidHttpUrl(String value) {
        try {
            URI uri = new URI(value);
            String scheme = uri.getScheme();
            return uri.getHost() != null && (scheme.equalsIgnoreCase("http") || scheme.equalsIgnoreCase("https"));
        } catch (URISyntaxException | NullPointerException e) {
            return false;
        }
    }

    private ChatMessageResponse toResponse(Chat chat) {
        return ChatMessageResponse.builder()
                .chatId(chat.getChatId())
                .shipmentId(chat.getShipment().getShipmentId())
                .senderId(chat.getSenderId())
                .senderRole(chat.getSenderRole())
                .contentType(chat.getContentType())
                .content(chat.getContent())
                .createdAt(chat.getCreatedAt())
                .build();
    }
}
