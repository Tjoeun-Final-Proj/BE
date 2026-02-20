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

    private static final String NAVER_DIRECTIONS_API_URL = "https://maps.apigw.ntruss.com/map-direction/v1/driving";

    public NaverDirectionsApiClient(
            @Value("${naver.maps.client-id}") String clientId,
            @Value("${naver.maps.client-secret}") String clientSecret,
            RestClient.Builder restClientBuilder,
            Gson gson) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.restClient = restClientBuilder.build(); // RestClient 인스턴스 생성
        this.gson = gson;
    }

    /**
     * 출발지, 목적지, 경유지를 입력받아 최적 경로 정보를 조회합니다.
     *
     * @param start 출발지 좌표 ("경도,위도")
     * @param goal 목적지 좌표 ("경도,위도")
     * @param waypoints 경유지 좌표 리스트 (각 항목은 "경도,위도" 형식)
     * @return NaverDirectionsResponse를 포함한 Optional 객체
     */
    public Optional<NaverDirectionsResponse> getDirections(
            String start, // "경도,위도"
            String goal,  // "경도,위도"
            List<String> waypoints) { // "경도,위도|경도,위도" 형식
        try {
            // 경유지가 있는 경우 파이프(|) 문자로 연결하여 문자열 생성
            StringBuilder waypointString = new StringBuilder();
            if (waypoints != null && !waypoints.isEmpty()) {
                for (int i = 0; i < waypoints.size(); i++) {
                    waypointString.append(waypoints.get(i));
                    if (i < waypoints.size() - 1) {
                        waypointString.append("|");
                    }
                }
            }

            // 기본 URI 구성 (최적 경로 탐색 옵션인 trafast 사용)
            String uri = NAVER_DIRECTIONS_API_URL +
                    "?start=" + start +
                    "&goal=" + goal +
                    "&option=trafast";

            // 경유지 파라미터 추가
            if (waypointString.length() > 0) {
                uri += "&waypoints=" + waypointString.toString();
            }

            log.info("Naver Directions API 호출 URI: {}", uri);

            // API 호출 및 응답 수신
            String responseBody = restClient.get()
                    .uri(uri)
                    .header("x-ncp-apigw-api-key-id", clientId)
                    .header("x-ncp-apigw-api-key", clientSecret)
                    .retrieve()
                    .body(String.class);

            log.debug("Naver Directions API 응답: {}", responseBody);

            // JSON 응답을 DTO 객체로 변환하여 반환
            return Optional.ofNullable(gson.fromJson(responseBody, NaverDirectionsResponse.class));

        } catch (Exception e) {
            log.error("Naver Directions API 호출 중 오류 발생: {}", e.getMessage(), e);
            return Optional.empty();
        }
    }
}
