package com.tjoeun.boxmon.feature.payment.service;

import com.tjoeun.boxmon.exception.UserNotFoundException;
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

    // TODO: 성욱씨가 구현할 getKey 메서드 호출 로직으로 대체 필요
    // authKey를 받아 billingKey를 생성하고 PaymentMethod에 저장하는 메서드
    @Transactional
    public PaymentMethod registerBillingKey(Long shipperId, String authKey) {
        // 1. shipperId로 Shipper 엔티티 조회
        Shipper shipper = shipperRepository.findById(shipperId)
                .orElseThrow(() -> new UserNotFoundException("ID가 " + shipperId + "인 화주(Shipper)를 찾을 수 없습니다."));

        // 2. authKey를 사용하여 billingKey를 받아오는 로직 (현재는 Mocking)
        // 이 부분은 나중에 실제 Toss Payments API를 호출하는 getKey 메서드로 대체되어야 합니다.
        String billingKey = getBillingKeyFromAuthKey(authKey); // 임시 메서드 호출

        // 3. PaymentMethod 엔티티 생성 및 저장
        PaymentMethod paymentMethod = PaymentMethod.builder()
                .shipper(shipper)
                .billingKey(billingKey)
                .build();

        return paymentMethodRepository.save(paymentMethod);
    }

    // 다른 백엔드 개발자가 구현할 Toss Payments API 호출을 가정한 Mock 메서드
    // 실제 구현 시 Toss Payments SDK 또는 RestTemplate/WebClient를 사용하여 외부 API를 호출해야 합니다.
    private String getBillingKeyFromAuthKey(String authKey) {
        // 여기서는 authKey와 customerKey를 사용하여 Toss Payments API를 호출하고
        // billingKey를 받아오는 로직이 들어갈 예정입니다.
        // flow.txt에 따르면 POST https://api.tosspayments.com/v1/billing/authorizations/issue
        // Headers: Authorization: Basic {base64-encoded secretKey}
        // Body: { "authKey": "...", "customerKey": "..." }

        // 현재는 임의의 billingKey를 반환하도록 Mocking 합니다.
        System.out.println("DEBUG: AuthKey received: " + authKey);
        // 실제로는 authKey 유효성 검사 및 외부 API 호출 로직이 필요
        return "mock_billing_key_" + UUID.randomUUID().toString().substring(0, 8);
    }
}
