package com.tjoeun.boxmon.global.client;

import com.tjoeun.boxmon.exception.ExternalServiceException;
import com.tjoeun.boxmon.feature.settlement.util.TossJweCrypto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

@Slf4j
@Component
public class TossApiClient {
    private final RestClient client;
    private final TossJweCrypto tossJweCrypto;

    public TossApiClient(RestClient.Builder builder, @Value("${toss-api-key}") String secretKey, TossJweCrypto tossJweCrypto) {
        client = builder
                .baseUrl("https://api.tosspayments.com")
                .defaultHeader(HttpHeaders.AUTHORIZATION, 
                        "Basic " + Base64.getEncoder().encodeToString((secretKey + ":").getBytes(StandardCharsets.UTF_8)))
                .build();
        this.tossJweCrypto = tossJweCrypto;
    }
    
    //평문 body를 쓰는 post 요청
    private ResponseEntity<Map<String,Object>> postPlain(Map<String, Object> requestBody, String uri){
        ResponseEntity<Map<String,Object>> result = client.post()
                .uri(uri)
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

        if (result.getStatusCode().value()>=400)
            throw new ExternalServiceException("토스 서버 통신 실패. 응답: " + result.getBody());
        
        return result;
    }

    //JWE 암호화를 사용하는 post 요청
    private Map<String,Object> postEncrypted(Map<String, Object> requestBody, String uri) {
        String jweBody = tossJweCrypto.encryptToCompactJwe(requestBody);
        ResponseEntity<String> encryptedResponse = client.post()
                .uri(uri)
                .contentType(MediaType.TEXT_PLAIN)
                .header("TossPayments-api-security-mode", "ENCRYPTION")
                .body(jweBody)
                .exchange((request, response) -> {
                    HttpStatusCode status = response.getStatusCode();
                    String body = response.bodyTo(String.class); // JWE 문자열
                    return ResponseEntity.status(status).body(body);
                });
        Map<String, Object> decrypted = tossJweCrypto.decryptCompactJwe(encryptedResponse.getBody());
        
        if (encryptedResponse.getStatusCode().value()>=400)
            throw new ExternalServiceException("토스 서버 통신 실패. 응답: " + decrypted);
        
        return decrypted;
    }

    public void confirmPayment(String paymentKey, String orderId, long amount) {
        log.info("결제 승인을 요청합니다...");
        Map<String, Object> requestBody = Map.of(
                "paymentKey", paymentKey,
                "orderId", orderId,
                "amount", amount
        );

        ResponseEntity<Map<String,Object>> result = postPlain(requestBody, "/v1/payments/confirm");
        
        log.debug("결제 승인 성공. 응답: result={}",result);
    }
    
    public void cancelPayment(String paymentKey, String cancelReason) {
        log.info("결제 취소를 요청합니다...");
        Map<String, Object> requestBody = Map.of(
                "cancelReason", cancelReason
        );

        ResponseEntity<Map<String,Object>> result = postPlain(requestBody, "/v1/payments/"+paymentKey+"/cancel");

        log.debug("결제 취소 성공. 응답: result={}",result);
    }
    
    //차주의 셀러등록 요청
    public void registerDriver(String driverId, String name, String email, String phone, String bankCode, String accountNumber, String accountHolderName){
        log.info("차주의 셀러등록을 요청합니다...");
        Map<String, Object> requestBody = Map.of(
                "refSellerId", driverId,
                "businessType", "INDIVIDUAL",
                "individual", Map.of(
                        "name", name,
                        "email", email,
                        "phone", phone
                ),
                "account", Map.of(
                        "bankCode", bankCode,
                        "accountNumber", accountNumber,
                        "holderName", accountHolderName
                )
        );

        Map<String,Object> result = postEncrypted(requestBody,"/v2/sellers");

        log.debug("셀러 등록 성공. 응답: result={}",result);
    }
}
