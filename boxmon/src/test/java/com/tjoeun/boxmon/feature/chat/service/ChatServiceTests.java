package com.tjoeun.boxmon.feature.chat.service;

import com.tjoeun.boxmon.feature.chat.domain.Chat;
import com.tjoeun.boxmon.feature.chat.domain.ChatContentType;
import com.tjoeun.boxmon.feature.chat.domain.ChatSenderRole;
import com.tjoeun.boxmon.feature.chat.dto.ChatMessageResponse;
import com.tjoeun.boxmon.feature.chat.dto.ChatSendRequest;
import com.tjoeun.boxmon.feature.chat.repository.ChatRepository;
import com.tjoeun.boxmon.feature.notification.service.NotificationUseCase;
import com.tjoeun.boxmon.feature.shipment.domain.Shipment;
import com.tjoeun.boxmon.feature.shipment.domain.ShipmentStatus;
import com.tjoeun.boxmon.feature.shipment.repository.ShipmentRepository;
import com.tjoeun.boxmon.feature.user.domain.Driver;
import com.tjoeun.boxmon.feature.user.domain.Shipper;
import com.tjoeun.boxmon.global.storage.ObjectStorageService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChatServiceTests {

    @InjectMocks
    private ChatService chatService;

    @Mock
    private ChatRepository chatRepository;

    @Mock
    private ShipmentRepository shipmentRepository;

    @Mock
    private ObjectStorageService objectStorageService;

    @Mock
    private NotificationUseCase notificationUseCase;

    @Test
    @DisplayName("TEXT 메시지 전송 시 채팅 저장 후 상대방 알림을 호출한다.")
    void sendMessage_text_shouldNotifyOpponent() {
        Long shipmentId = 100L;
        Long senderId = 1L;
        Shipment shipment = shipmentWithParticipants(1L, 2L, ShipmentStatus.ASSIGNED);

        ChatSendRequest request = new ChatSendRequest();
        request.setContentType(ChatContentType.TEXT);
        request.setContent(" hello ");

        when(shipmentRepository.findById(shipmentId)).thenReturn(Optional.of(shipment));
        when(chatRepository.save(any(Chat.class))).thenReturn(
                Chat.builder()
                        .chatId(10L)
                        .shipment(shipment)
                        .senderId(senderId)
                        .senderRole(ChatSenderRole.SHIPPER)
                        .content("hello")
                        .contentType(ChatContentType.TEXT)
                        .createdAt(LocalDateTime.now())
                        .build()
        );

        ChatMessageResponse response = chatService.sendMessage(shipmentId, senderId, ChatSenderRole.SHIPPER, request);

        assertNotNull(response);
        assertEquals(shipmentId, response.getShipmentId());
        verify(notificationUseCase).notifyChatMessage(shipmentId, senderId);
    }

    @Test
    @DisplayName("IMG_URL 메시지 전송 시에도 알림 실패가 채팅 전송을 막지 않는다.")
    void sendMessage_imgUrl_shouldIgnoreNotificationFailure() {
        Long shipmentId = 200L;
        Long senderId = 2L;
        Shipment shipment = shipmentWithParticipants(1L, 2L, ShipmentStatus.IN_TRANSIT);

        ChatSendRequest request = new ChatSendRequest();
        request.setContentType(ChatContentType.IMG_URL);
        request.setContent("https://example.com/chat/image.jpg");

        when(shipmentRepository.findById(shipmentId)).thenReturn(Optional.of(shipment));
        when(chatRepository.save(any(Chat.class))).thenReturn(
                Chat.builder()
                        .chatId(11L)
                        .shipment(shipment)
                        .senderId(senderId)
                        .senderRole(ChatSenderRole.DRIVER)
                        .content(request.getContent())
                        .contentType(ChatContentType.IMG_URL)
                        .createdAt(LocalDateTime.now())
                        .build()
        );
        doThrow(new RuntimeException("fcm unavailable"))
                .when(notificationUseCase).notifyChatMessage(shipmentId, senderId);

        ChatMessageResponse response = chatService.sendMessage(shipmentId, senderId, ChatSenderRole.DRIVER, request);

        assertNotNull(response);
        assertEquals(shipmentId, response.getShipmentId());
        verify(notificationUseCase).notifyChatMessage(shipmentId, senderId);
    }

    private Shipment shipmentWithParticipants(Long shipperId, Long driverId, ShipmentStatus status) {
        Shipper shipper = mock(Shipper.class);
        Driver driver = mock(Driver.class);
        when(shipper.getShipperId()).thenReturn(shipperId);
        when(driver.getDriverId()).thenReturn(driverId);

        Shipment shipment = new Shipment();
        shipment.setShipper(shipper);
        shipment.setDriver(driver);
        shipment.setShipmentStatus(status);
        return shipment;
    }
}
