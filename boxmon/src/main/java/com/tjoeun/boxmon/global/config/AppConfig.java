package com.tjoeun.boxmon.global.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.checksums.RequestChecksumCalculation;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;

import java.net.URI;

/**
 * 전역 공통 Bean 설정 클래스입니다.
 * 외부 API 호출(RestClient), JSON 파싱(Gson), Object Storage 연동(S3Client)을 제공합니다.
 */
@Configuration
public class AppConfig {

    // 외부 HTTP API 호출에서 공통으로 재사용하는 RestClient Builder
    @Bean
    public RestClient.Builder restClientBuilder() {
        return RestClient.builder();
    }

    // Naver Directions 응답 파싱 등에서 사용하는 Gson Bean
    @Bean
    public Gson gson() {
        return new GsonBuilder().setPrettyPrinting().create();
    }

    // NCP Object Storage는 S3 호환 API를 사용하므로 S3Client로 연결합니다.
    // path-style 접근과 체크섬 비활성 옵션을 적용해 호환성 이슈를 회피합니다.
    @Bean
    public S3Client s3Client(
            @Value("${ncp.object.endpoint}") String endpoint,
            @Value("${ncp.object.region}") String region,
            @Value("${ncp.object.access-key}") String accessKey,
            @Value("${ncp.object.secret-key}") String secretKey
    ) {
        return S3Client.builder()
                .endpointOverride(URI.create(endpoint))
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey)))
                .serviceConfiguration(S3Configuration.builder().pathStyleAccessEnabled(true).build())
                // AWS SDK v2(특히 2.30+ 계열)은 PutObject 시 체크섬 관련 헤더/계산을 기본으로 적용하는 경우가 있음.
                // NCP Object Storage(S3 호환)에서는 이 체크섬 동작이 완전 호환되지 않아 403 AccessDenied가 발생할 수 있어,
                // "필수일 때만" 체크섬을 계산/전송하도록 설정해 호환성 문제를 피한다.
                .requestChecksumCalculation(RequestChecksumCalculation.WHEN_REQUIRED)
                .build();
    }
}
