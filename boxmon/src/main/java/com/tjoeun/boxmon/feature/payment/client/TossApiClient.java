package com.tjoeun.boxmon.feature.payment.client;

import com.tjoeun.boxmon.exception.ExternalServiceException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

@Slf4j
@Component
public class TossApiClient {
    private final String secretKey = "test_sk_zXLkKEypNArWmo50nX3lmeaxYG5R";
    private final RestClient client;

    public TossApiClient(RestClient.Builder builder, @Value("${toss-api-key}") String secretKey) {
        client = builder
                .baseUrl("https://api.tosspayments.com")
                .defaultHeader(HttpHeaders.AUTHORIZATION, 
                        "Basic " + Base64.getEncoder().encodeToString((secretKey + ":").getBytes(StandardCharsets.UTF_8)))
                .build();
    }

    public void confirmPayment(String paymentKey, String orderId, long amount) {
        log.info("결제 승인을 요청합니다...");
        Map<String, Object> requestBody = Map.of(
                "paymentKey", paymentKey,
                "orderId", orderId,
                "amount", amount
        );
        

        ResponseEntity<Map<String,Object>> result = client.post()
                .uri("/v1/payments/confirm")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .body(requestBody)
                .exchange((req, resp) -> {
                    int raw = resp.getStatusCode().value();

                    // 성공/실패 상관없이 Map으로 읽기 시도
                    Map<String, Object> body = null;
                    try {
                        body = resp.bodyTo(Map.class);
                    } catch (Exception ignore) {
                        // 바디가 JSON이 아니거나 비어있으면 null 유지
                    }

                    return ResponseEntity.status(raw).headers(resp.getHeaders()).body(body);
                });
        
        log.debug("결제 승인 성공. 응답: result={}",result);
        
        if (result.getStatusCode().value()>=400)
            throw new ExternalServiceException("토스 서버 통신 실패. 응답: " + result.getBody());
    }
}
