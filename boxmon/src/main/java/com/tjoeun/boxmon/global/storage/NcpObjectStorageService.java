package com.tjoeun.boxmon.global.storage;

import com.tjoeun.boxmon.exception.ExternalServiceException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.ObjectCannedACL;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

/**
 * NCP Object Storage(S3 호환) 구현체.
 * 화물 이미지 업로드, 공개 URL 생성, 보상 삭제를 담당합니다.
 */
@Slf4j
@Service
public class NcpObjectStorageService implements ObjectStorageService {
    // 이미지 업로드 기본 정책: 최대 5MB, jpeg/png/webp만 허용
    private static final long MAX_UPLOAD_SIZE_BYTES = 5L * 1024 * 1024;
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/webp"
    );

    private final S3Client s3Client;
    private final String bucket;
    private final String endpoint;

    public NcpObjectStorageService(
            S3Client s3Client,
            @Value("${ncp.object.bucket}") String bucket,
            @Value("${ncp.object.endpoint}") String endpoint
    ) {
        this.s3Client = s3Client;
        this.bucket = bucket;
        this.endpoint = endpoint;
    }

    @Override
    public String uploadCargoPhoto(MultipartFile file) {
        return uploadImage(file, "shipments/cargo");
    }

    @Override
    public String uploadDropoffPhoto(MultipartFile file) {
        return uploadImage(file, "shipments/dropoff");
    }

    private String uploadImage(MultipartFile file, String prefix) {
        // 파일 기본 검증(비어있는지/용량/MIME 타입)
        validateImageFile(file);

        String extension = resolveExtension(file);
        String objectKey = generateObjectKey(prefix, extension);

        try {
            // Object Storage에 객체 저장 후 key를 반환합니다.
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(objectKey)
                    .contentType(file.getContentType())
                    // 업로드된 파일을 누구나 읽을 수 있도록 공개 읽기 권한을 설정합니다. (브라우저 등에서 URL로 바로 접근 가능)
                    .acl(ObjectCannedACL.PUBLIC_READ)
                    .build();

            s3Client.putObject(request, RequestBody.fromBytes(file.getBytes()));
            return objectKey;
        } catch (IOException e) {
            throw new IllegalArgumentException("업로드 파일을 읽을 수 없습니다.");
        } catch (S3Exception e) {
            log.error("NCP Object Storage 업로드 실패. bucket={}, key={}", bucket, objectKey, e);
            throw new ExternalServiceException("오브젝트 스토리지 업로드에 실패했습니다.");
        }
    }

    @Override
    public String buildPublicUrl(String objectKey) {
        // 공개 버킷 전제: endpoint/bucket/key 조합으로 정적 접근 URL 생성
        String trimmedEndpoint = endpoint.endsWith("/") ? endpoint.substring(0, endpoint.length() - 1) : endpoint;
        return trimmedEndpoint + "/" + bucket + "/" + objectKey;
    }


    @Override
    public void deleteObject(String objectKey) {
        // DB 저장 실패 등 보상 트랜잭션에서 사용합니다.
        try {
            s3Client.deleteObject(DeleteObjectRequest.builder()
                    .bucket(bucket)
                    .key(objectKey)
                    .build());
        } catch (S3Exception e) {
            log.warn("NCP Object Storage 삭제 보상 실패. bucket={}, key={}", bucket, objectKey, e);
        }
    }

    private void validateImageFile(MultipartFile file) {
        // 서비스 레벨에서 선제 검증해 스토리지 불필요 호출을 줄입니다.
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("이미지 파일이 비어 있습니다.");
        }

        if (file.getSize() > MAX_UPLOAD_SIZE_BYTES) {
            throw new IllegalArgumentException("이미지 파일 크기는 5MB 이하여야 합니다.");
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase(Locale.ROOT))) {
            throw new IllegalArgumentException("지원하지 않는 이미지 형식입니다. (jpeg/png/webp)");
        }
    }

    @Override
    public String uploadInquiryPhoto(MultipartFile file) {
        return uploadImage(file, "inquiries");
    }

    /**
     * 파일의 Content-Type을 분석하여 적절한 확장자를 반환합니다.
     */
    private String resolveExtension(MultipartFile file) {
        String contentType = file.getContentType();

        // PNG 이미지인 경우 png 확장자 반환
        if ("image/png".equalsIgnoreCase(contentType)) {
            return "png";
        }
        // WebP 이미지인 경우 webp 확장자 반환
        if ("image/webp".equalsIgnoreCase(contentType)) {
            return "webp";
        }
        // 그 외의 경우(주로 image/jpeg) 기본적으로 jpg 확장자 사용
        return "jpg";
    }

    private String generateObjectKey(String prefix, String extension) {
        // 월 단위 prefix + UUID 조합으로 충돌 가능성을 낮춥니다.
        String yyyymm = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMM"));
        return prefix + "/" + yyyymm + "/" + UUID.randomUUID() + "." + extension;
    }
}
