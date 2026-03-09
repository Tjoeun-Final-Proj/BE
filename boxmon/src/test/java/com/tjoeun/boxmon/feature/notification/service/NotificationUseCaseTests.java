package com.tjoeun.boxmon.feature.notification.service;

import com.tjoeun.boxmon.feature.notification.repository.NotificationRepository;
import com.tjoeun.boxmon.feature.shipment.domain.Shipment;
import com.tjoeun.boxmon.feature.shipment.repository.ShipmentRepository;
import com.tjoeun.boxmon.feature.user.domain.Shipper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationUseCaseTests {

    @InjectMocks
    private NotificationUseCase notificationUseCase;

    @Mock
    private ShipmentRepository shipmentRepository;

    @Mock
    private NotificationSender notificationSender;

    @Mock
    private NotificationRepository notificationRepository;

    @Test
    @DisplayName("채팅 알림에서 상대방이 없으면 전송을 스킵한다.")
    void notifyChatMessage_shouldSkipWhenOpponentMissing() {
        long shipmentId = 1L;
        long senderId = 10L;

        Shipper shipper = mock(Shipper.class);
        when(shipper.getShipperId()).thenReturn(senderId);

        Shipment shipment = new Shipment();
        shipment.setShipper(shipper);
        shipment.setDriver(null);

        when(shipmentRepository.findById(shipmentId)).thenReturn(Optional.of(shipment));

        notificationUseCase.notifyChatMessage(shipmentId, senderId);

        verify(notificationSender, never()).send(
                org.mockito.ArgumentMatchers.anyLong(),
                org.mockito.ArgumentMatchers.anyLong(),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.any()
        );
    }
}
