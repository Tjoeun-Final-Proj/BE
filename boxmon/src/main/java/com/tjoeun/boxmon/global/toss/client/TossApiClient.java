package com.tjoeun.boxmon.global.toss.client;

import com.tjoeun.boxmon.exception.ExternalServiceException;
import com.tjoeun.boxmon.feature.settlement.util.TossJweCrypto;
import com.tjoeun.boxmon.global.toss.dto.TossPayment;
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
import java.util.Collections;
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
    //평문 body를 쓰는 get 요청
    private <T> ResponseEntity<T> getPlainWith(Map<String, Object> requestParameter, String uri, Class<T> responseType){
        //요청 파라미터 세팅
        StringBuilder uriBuilder = new StringBuilder(uri);
        if(!requestParameter.isEmpty()){
            uriBuilder.append("?");
            for(Map.Entry<String, Object> entry : requestParameter.entrySet()){
                uriBuilder.append(entry.getKey()).append("=").append(entry.getValue());
                uriBuilder.append("&");
            }
            uriBuilder.deleteCharAt(uriBuilder.length() - 1);
        }
        
        ResponseEntity<T> result = client.get()
                .uri(uriBuilder.toString())
                .accept(MediaType.APPLICATION_JSON)
                .exchange((req, resp) -> {
                    int raw = resp.getStatusCode().value();

                    // 성공/실패 상관없이 Map으로 읽기 시도
                    T body = null;
                    try {
                        body = resp.bodyTo(responseType);
                    } catch (Exception ignore) {
                        // 바디가 JSON이 아니거나 비어있으면 null 유지
                    }

                    return ResponseEntity.status(raw).headers(resp.getHeaders()).body(body);
                });

        if (result.getStatusCode().value()>=400)
            throw new ExternalServiceException(String.format("토스 서버 통신 실패. 응답: %d %s", result.getStatusCode().value(), result.getBody()));

        return result;
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
            throw new ExternalServiceException(String.format("토스 서버 통신 실패. 응답: %d %s", result.getStatusCode().value(), result.getBody()));
        
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
            throw new ExternalServiceException(
                    String.format("토스 서버 통신 실패. 응답: %d %s", encryptedResponse.getStatusCode().value(), encryptedResponse.getBody()));
        
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
    
    //특정 결제의 PG→플랫폼 정산 완료 여부 확인 
    public boolean checkSettled(String orderId) {
        log.info("결제 조회 API를 이용해 정산 여부를 확인합니다...");

        TossPayment tossPaymentInfo = getPlainWith(Collections.emptyMap(), "/v1/payments/orders/"+orderId, TossPayment.class).getBody();
        log.debug("정산여부 확인 성공. 응답: {}",tossPaymentInfo);

        try {
            return switch (tossPaymentInfo.getMethod()) {
                case "계좌이체" -> 
                        tossPaymentInfo.getTransfer()
                                .getSettlementStatus()
                                .equals("COMPLETED");
                
                case "휴대폰" -> 
                        tossPaymentInfo.getMobilePhone()
                                .getSettlementStatus()
                                .equals("COMPLETED");
                
                case "문화상품권", "도서문화상품권", "게임문화상품권" ->
                        tossPaymentInfo.getGiftCertificate()
                                .getSettlementStatus()
                                .equals("COMPLETED");

                case "간편결제" -> //간편결제는 섞인 결제라서 정산여부 확인이 어려움 -> 일단 정산해주고 안 되면 토스 니가 잘못한거야ㅡㅡ
                        true;
                
                case "카드" -> //카드는 결제 조회만으로는 정산여부 확인이 어려움 -> 결제조회에서 매입여부만 확인하고 매입이 끝났으면 더 이상 확인할 방법이 없음
                    //매입 완료 여부 확인
                    tossPaymentInfo.getCard()
                            .getAcquireStatus()
                            .equals("COMPLETED");
                
                default -> throw new IllegalStateException(
                        "알 수 없는 결제 수단으로 진행된 결제에 대한 조회가 발생했습니다. 결제 로그를 확인해주세요. orderId: " + orderId);
            };
        }
        catch (NullPointerException e){
            throw new ExternalServiceException("토스 api 응답 파싱에 실패했습니다. 응답: " + tossPaymentInfo);
        }
    }
}
