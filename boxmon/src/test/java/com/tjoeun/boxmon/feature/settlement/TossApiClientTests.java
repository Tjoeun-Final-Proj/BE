package com.tjoeun.boxmon.feature.settlement;

import com.tjoeun.boxmon.feature.settlement.util.TossJweCrypto;
import com.tjoeun.boxmon.global.toss.client.TossApiClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.util.Map;


public class TossApiClientTests {
    private TossApiClient tossApiClient;
    
    @BeforeEach
    public void setup() {
        tossApiClient = new TossApiClient(
                RestClient.builder(),
                "test_sk_24xLea5zVAz5EjDaBn70rQAMYNwW",
                new TossJweCrypto(
                        "2e9f69bff61fd3ec201f9521e85919a0e5d1636b10be551e65703d868fb87dbd",
                        new ObjectMapper()
                )
        );
    }
    
    @Test
    void test(){
        Map<String, Object> payoutRequestMap = Map.of(
                "payoutId", "PAYOUT-20260310-0001",
                "orderId", "ORDER-20260310-0001",
                "driverUserId", "driver-1001",
                "amount", new BigDecimal("150000"),
                "bankName", "KB국민은행",
                "accountNumber", "12345678901234",
                "currency", "KRW",
                "description", "정산 테스트 지급 요청"
        );
        ResponseEntity<Map<String, Object>> result = tossApiClient.postPlain(payoutRequestMap, "/v1/payouts");
        System.out.println(result);
    }
}
