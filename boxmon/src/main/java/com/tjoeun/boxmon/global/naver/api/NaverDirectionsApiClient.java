package com.tjoeun.boxmon.global.naver.api;

import com.google.gson.Gson;
import com.tjoeun.boxmon.global.naver.dto.NaverDirectionsResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value; // @Value import 추가
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Optional;

@Slf4j
@Component
public class NaverDirectionsApiClient {

    private final String clientId;
    private final String clientSecret;
    private final RestClient restClient;
    private final Gson gson;

    private static final String NAVER_DIRECTIONS_API_URL = "https://naveropenapi.apigw.ntruss.com/map-direction/v1/driving";

    public NaverDirectionsApiClient(
            @Value("${naver.maps.client-id}") String clientId,
            @Value("${naver.maps.client-secret}") String clientSecret,
            RestClient.Builder restClientBuilder,
            Gson gson) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.restClient = restClientBuilder.build(); // RestClient 빌더를 사용하여 인스턴스 생성
        this.gson = gson;
    }

    public Optional<NaverDirectionsResponse> getDirections(
            String start, // "경도,위도"
            String goal,  // "경도,위도"
            List<String> waypoints) { // "경도,위도|경도,위도" 형식
        try {
            StringBuilder waypointString = new StringBuilder();
            if (waypoints != null && !waypoints.isEmpty()) {
                for (int i = 0; i < waypoints.size(); i++) {
                    waypointString.append(waypoints.get(i));
                    if (i < waypoints.size() - 1) {
                        waypointString.append("|");
                    }
                }
            }

            String uri = NAVER_DIRECTIONS_API_URL +
                    "?start=" + start +
                    "&goal=" + goal;

            if (waypointString.length() > 0) {
                uri += "&waypoints=" + waypointString.toString();
            }

            log.info("Calling Naver Directions API with URI: {}", uri);

            String responseBody = restClient.get()
                    .uri(uri)
                    .header("x-ncp-apigw-api-key-id", clientId)
                    .header("x-ncp-apigw-api-key", clientSecret)
                    .retrieve()
                    .body(String.class);

            log.debug("Naver Directions API Response: {}", responseBody);

            return Optional.ofNullable(gson.fromJson(responseBody, NaverDirectionsResponse.class));

        } catch (Exception e) {
            log.error("Error calling Naver Directions API: {}", e.getMessage(), e);
            return Optional.empty();
        }
    }
}
