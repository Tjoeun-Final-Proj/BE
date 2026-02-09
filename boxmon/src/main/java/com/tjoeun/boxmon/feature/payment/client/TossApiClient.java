package com.tjoeun.boxmon.feature.payment.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

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

    public Map requestBillingKey(String customerKey, String authKey){
        //TODO:map 인코딩된 데이터 dto로 교체
        Map<String, String> data = Map.of("customerKey", customerKey, "authKey", authKey);
        
        Map result = client.post()
                .uri("/v1/billing/authorizations/issue")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .body(data)
                .retrieve()
                .body(Map.class);
        return result;
    }
}
