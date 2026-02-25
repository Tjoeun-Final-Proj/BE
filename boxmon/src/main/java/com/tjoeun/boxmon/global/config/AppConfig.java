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
                .requestChecksumCalculation(RequestChecksumCalculation.WHEN_REQUIRED)
                .build();
    }
}
