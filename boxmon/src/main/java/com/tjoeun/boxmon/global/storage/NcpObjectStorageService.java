package com.tjoeun.boxmon.global.storage;

import com.tjoeun.boxmon.exception.ExternalServiceException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
public class NcpObjectStorageService implements ObjectStorageService {
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
        validateImageFile(file);

        String extension = resolveExtension(file);
        String objectKey = generateCargoObjectKey(extension);

        try {
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(objectKey)
                    .contentType(file.getContentType())
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
        String trimmedEndpoint = endpoint.endsWith("/") ? endpoint.substring(0, endpoint.length() - 1) : endpoint;
        return trimmedEndpoint + "/" + bucket + "/" + objectKey;
    }

    @Override
    public void deleteObject(String objectKey) {
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

    private String resolveExtension(MultipartFile file) {
        String contentType = file.getContentType();
        if ("image/png".equalsIgnoreCase(contentType)) {
            return "png";
        }
        if ("image/webp".equalsIgnoreCase(contentType)) {
            return "webp";
        }
        return "jpg";
    }

    private String generateCargoObjectKey(String extension) {
        String yyyymm = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMM"));
        return "shipments/cargo/" + yyyymm + "/" + UUID.randomUUID() + "." + extension;
    }
}

