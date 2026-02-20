package com.tjoeun.boxmon.feature.notification.service;

import com.tjoeun.boxmon.feature.notification.domain.NotificationType;
import com.tjoeun.boxmon.feature.shipment.domain.Shipment;
import com.tjoeun.boxmon.feature.shipment.repository.ShipmentRepository;
import com.tjoeun.boxmon.global.util.AddressProcessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.function.Function;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationUseCase {
    private final ShipmentRepository shipmentRepository;
    private final NotificationSender notificationSender;
    
    /**
     * 배차완료 알림
     * @param shipmentId 배차된 운송건 id
     */
    public void notifyAssignmentCompleted(long shipmentId) {
        notifyShipper(
                shipmentId,
                NotificationType.ASSIGNMENT_CONFIRMED,
                "배차가 완료되었습니다.",
                shipment -> String.format("%s 배차 완료되었습니다.", summary(shipment)),
                null
        );
    }

    /**
     * 운송시작 알림
     * @param shipmentId 시작된 운송건 id
     */
    public void notifyTransportStarted(long shipmentId) {
        notifyShipper(
                shipmentId,
                NotificationType.TRANSPORT_STARTED,
                "운송이 시작되었습니다.",
                shipment -> String.format("%s 운송이 시작되었습니다.", summary(shipment)),
                null
        );
    }

    /**
     * 운송완료 알림
     * @param shipmentId 완료된 운송건 id
     */
    public void notifyTransportCompleted(long shipmentId) {
        notifyShipper(
                shipmentId,
                NotificationType.TRANSPORT_COMPLETED,
                "운송이 완료되었습니다.",
                shipment -> String.format("%s 운송이 완료되었습니다.", summary(shipment)),
                null
        );
    }

    /**
     * 배차 취소 요청 알림
     * @param shipmentId 완료된 운송건 id
     * @param userId 취소 요청자 id(취소 요청자의 거래 상대에게만 알림이 발송됨)
     */
    public void notifyAssignmentCancellationRequested(long shipmentId, long userId) {
        notifyOpponent(
                shipmentId,
                userId,
                NotificationType.ASSIGNMENT_CANCELLATION_REQUESTED,
                "상대가 운송취소를 요청했습니다.",
                shipment -> String.format("%s 운송 취소가 요청되었습니다.", summary(shipment)),
                null
        );
    }

    /**
     * 채팅 알림
     * @param shipmentId 완료된 운송건 id
     * @param userId 채팅 전송자 id(채팅 전송자의 거래 상대(=채팅수신자)에게만 알림이 발송됨)
     */
    public void notifyChatMessage(long shipmentId, long userId) {
        notifyOpponent(
                shipmentId,
                userId,
                NotificationType.CHAT_MESSAGE,
                "상대가 채팅을 보냈습니다.",
                shipment -> String.format("%s 방에 채팅이 전송되었습니다.", summary(shipment)),
                null
        );
    }

    //화주에게 알림 발송
    private void notifyShipper(
            long shipmentId,
            NotificationType type,
            String title,
            Function<Shipment, String> bodyMaker,
            Map<String, String> extraData
    ) {
        shipmentRepository.findById(shipmentId).ifPresentOrElse(
                shipment -> notificationSender.send(
                        shipment.getShipper().getShipperId(),
                        shipmentId,
                        type,
                        title,
                        bodyMaker.apply(shipment),
                        extraData
                ),
                () -> log.warn("알림 발송 실패. 운송기록을 찾을 수 없습니다. shipmentId={}", shipmentId)
        );
    }
    
    //거래 상대방에게 알림 발송
    private void notifyOpponent(
            long shipmentId,
            long userId,
            NotificationType type,
            String title,
            Function<Shipment, String> bodyMaker,
            Map<String, String> extraData
    ) {
        shipmentRepository.findById(shipmentId).ifPresentOrElse(
                shipment -> notificationSender.send(
                        shipment.getShipper().getShipperId().equals(userId) ? shipment.getDriver().getDriverId() : shipment.getShipper().getShipperId(),
                        shipmentId,
                        type,
                        title,
                        bodyMaker.apply(shipment),
                        extraData
                ),
                () -> log.warn("알림 발송 실패. 운송기록을 찾을 수 없습니다. shipmentId={}", shipmentId)
        );
    }

    private String summary(Shipment shipment){
        String startPoint = AddressProcessor.simplifiy(shipment.getPickupAddress());
        String endPoint = AddressProcessor.simplifiy(shipment.getDropoffAddress());

        return String.format("%s → %s %s", startPoint, endPoint, shipment.getCargoType().getDescription());
    }
}
