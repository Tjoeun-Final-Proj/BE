package com.tjoeun.boxmon.feature.payment.service;

import com.tjoeun.boxmon.exception.UserNotFoundException;
import com.tjoeun.boxmon.feature.payment.client.TossApiClient;
import com.tjoeun.boxmon.feature.payment.domain.PaymentMethod;
import com.tjoeun.boxmon.feature.user.domain.Shipper;
import com.tjoeun.boxmon.feature.user.repository.PaymentMethodRepository;
import com.tjoeun.boxmon.feature.user.repository.ShipperRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaymentService {

    private final ShipperRepository shipperRepository;
    private final PaymentMethodRepository paymentMethodRepository;
    private final TossApiClient tossApiClient;
    
    // authKey를 받아 billingKey를 생성하고 PaymentMethod에 저장하는 메서드
    @Transactional
    public void registerBillingKey(Long shipperId, String authKey) {
        // 1. shipperId로 Shipper 엔티티 조회
        Shipper shipper = shipperRepository.findById(shipperId)
                .orElseThrow(() -> new UserNotFoundException("ID가 " + shipperId + "인 화주(Shipper)를 찾을 수 없습니다."));

        // 2. authKey를 사용하여 billingKey를 받아오는 로직 (현재는 Mocking)
        // 이 부분은 나중에 실제 Toss Payments API를 호출하는 getKey 메서드로 대체되어야 합니다.
        String billingKey = tossApiClient.requestBillingKey(shipperId.toString(), authKey); // 임시 메서드 호출
        

        // 3. PaymentMethod 엔티티 생성 및 저장
        PaymentMethod paymentMethod = PaymentMethod.builder()
                .shipper(shipper)
                .billingKey(billingKey)
                .build();

        paymentMethodRepository.save(paymentMethod);
    }
}
