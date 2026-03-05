package com.tjoeun.boxmon.feature.shipment.service;

import com.tjoeun.boxmon.exception.RoleAccessDeniedException;
import com.tjoeun.boxmon.exception.ShipmentNotFoundException;
import com.tjoeun.boxmon.exception.ShipmentStateConflictException;
import com.tjoeun.boxmon.feature.notification.service.NotificationUseCase;
import com.tjoeun.boxmon.feature.payment.service.PaymentCancelUseCase;
import com.tjoeun.boxmon.feature.shipment.domain.Shipment;
import com.tjoeun.boxmon.feature.shipment.domain.ShipmentStatus;
import com.tjoeun.boxmon.feature.shipment.repository.ShipmentRepository;
import jakarta.persistence.PessimisticLockException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
/**
 * Shipment 취소/철회 흐름을 담당하는 서비스.
 * 상호 취소 확정, 동시성 제어, 결제 취소 연동을 처리합니다.
 */
public class ShipmentCancelService {

    private final ShipmentRepository shipmentRepository;
    private final NotificationUseCase notificationUseCase;
    private final PaymentCancelUseCase paymentCancelUseCase;

    /**
     * 배송 취소를 요청합니다.
     * REQUESTED는 화주 단독 즉시 취소, ASSIGNED/IN_TRANSIT는 상호 합의(양측 토글 true)로 확정됩니다.
     */
    public void cancelShipment(Long userId, Long shipmentId) {
        try {
            // 취소/철회 경합 시 상태 일관성을 보장하기 위해 비관적 락으로 조회
            Shipment shipment = shipmentRepository.findByShipmentIdForUpdate(shipmentId)
                    .orElseThrow(() -> new ShipmentNotFoundException("배송을 찾을 수 없습니다."));

            UserRoleInShipment role = resolveUserRole(shipment, userId);
            ShipmentStatus status = shipment.getShipmentStatus();

            if (status == ShipmentStatus.CANCELED) {
                return; // 멱등성 보장: 이미 취소된 상태라면 성공으로 처리
            }

            if (status == ShipmentStatus.DONE) {
                throw new ShipmentStateConflictException("이미 완료된 배송은 취소할 수 없습니다.");
            }

            if (status == ShipmentStatus.REQUESTED) {
                if (role != UserRoleInShipment.SHIPPER) {
                    throw new RoleAccessDeniedException("Only shipper can cancel REQUESTED shipment.");
                }
                // REQUESTED는 즉시 취소 확정
                finalizeCancellation(shipment);
                shipmentRepository.save(shipment);
                return;
            }

            if (status != ShipmentStatus.ASSIGNED && status != ShipmentStatus.IN_TRANSIT) {
                throw new ShipmentStateConflictException("취소할 수 없는 상태입니다.");
            }

            boolean isRequesterAlreadySet = setCancelToggle(shipment, role, true);
            if (isRequesterAlreadySet) {
                return;
            }

            if (isBothCancelRequested(shipment)) {
                // 상호 취소 승인 완료 -> 취소 확정 + 결제 취소 연동
                finalizeCancellation(shipment);
                shipmentRepository.save(shipment);
                cancelPaymentOnMutualCancellation(shipmentId);
                return;
            }

            // 상대방 동의 대기 상태 저장 + 알림 시도
            shipmentRepository.save(shipment);
            if (shipment.getDriver() != null) {
                try {
                    notificationUseCase.notifyAssignmentCancellationRequested(shipmentId, userId);
                } catch (Exception e) {
                    log.warn("취소 요청은 성공했지만 알림 전송은 건너뜁니다. shipmentId={}", shipmentId, e);
                }
            }
        } catch (PessimisticLockException | PessimisticLockingFailureException e) {
            throw new ShipmentStateConflictException("동시 요청 충돌로 취소 요청을 처리하지 못했습니다. 잠시 후 다시 시도해주세요.");
        }
    }

    /**
     * 배송 취소 요청을 철회합니다.
     * ASSIGNED/IN_TRANSIT에서만 본인 토글을 false로 되돌릴 수 있습니다.
     */
    public void withdrawShipmentCancel(Long userId, Long shipmentId) {
        try {
            // 취소와 동일하게 락 기반 직렬화
            Shipment shipment = shipmentRepository.findByShipmentIdForUpdate(shipmentId)
                    .orElseThrow(() -> new ShipmentNotFoundException("배송을 찾을 수 없습니다."));

            UserRoleInShipment role = resolveUserRole(shipment, userId);
            ShipmentStatus status = shipment.getShipmentStatus();

            if (status == ShipmentStatus.DONE || status == ShipmentStatus.CANCELED) {
                throw new ShipmentStateConflictException("철회할 수 없는 상태입니다.");
            }
            if (status == ShipmentStatus.REQUESTED) {
                throw new ShipmentStateConflictException("REQUESTED 상태 취소는 즉시 확정되므로 철회할 수 없습니다.");
            }
            if (status != ShipmentStatus.ASSIGNED && status != ShipmentStatus.IN_TRANSIT) {
                throw new ShipmentStateConflictException("철회할 수 없는 상태입니다.");
            }

            setCancelToggle(shipment, role, false);
            shipmentRepository.save(shipment);
        } catch (PessimisticLockException | PessimisticLockingFailureException e) {
            throw new ShipmentStateConflictException("동시 요청 충돌로 취소 철회를 처리하지 못했습니다. 잠시 후 다시 시도해주세요.");
        }
    }

    private UserRoleInShipment resolveUserRole(Shipment shipment, Long userId) {
        boolean isShipper = shipment.getShipper() != null && shipment.getShipper().getShipperId().equals(userId);
        boolean isDriver = shipment.getDriver() != null && shipment.getDriver().getDriverId().equals(userId);

        if (isShipper) {
            return UserRoleInShipment.SHIPPER;
        }
        if (isDriver) {
            return UserRoleInShipment.DRIVER;
        }
        throw new RoleAccessDeniedException("Only shipment shipper or assigned driver can request cancellation.");
    }

    private boolean setCancelToggle(Shipment shipment, UserRoleInShipment role, boolean value) {
        if (role == UserRoleInShipment.SHIPPER) {
            boolean already = Boolean.TRUE.equals(shipment.getShipperCancelToggle()) == value;
            shipment.setShipperCancelToggle(value);
            return already;
        }

        boolean already = Boolean.TRUE.equals(shipment.getDriverCancelToggle()) == value;
        shipment.setDriverCancelToggle(value);
        return already;
    }

    private boolean isBothCancelRequested(Shipment shipment) {
        return Boolean.TRUE.equals(shipment.getShipperCancelToggle())
                && Boolean.TRUE.equals(shipment.getDriverCancelToggle());
    }

    private void finalizeCancellation(Shipment shipment) {
        // 취소 확정 처리: 상태 전환 + 토글 초기화
        shipment.setShipmentStatus(ShipmentStatus.CANCELED);
        shipment.setShipperCancelToggle(false);
        shipment.setDriverCancelToggle(false);
    }

    private void cancelPaymentOnMutualCancellation(Long shipmentId) {
        paymentCancelUseCase.cancelPayment(shipmentId, "화주/차주 상호 취소 승인으로 운송 취소");
    }

    private enum UserRoleInShipment {
        SHIPPER,
        DRIVER
    }
}
