package com.tjoeun.boxmon.global.storage;

import org.springframework.web.multipart.MultipartFile;

/**
 * 오브젝트 스토리지 업로드/URL 생성/삭제를 추상화한 인터페이스입니다.
 * 도메인 서비스는 구현체 세부사항(NCP, S3 호환 API 등)을 몰라도 이 계약만 사용합니다.
 */
public interface ObjectStorageService {
    /**
     * 화물 사진 파일을 업로드하고 저장된 객체 키를 반환합니다.
     */
    String uploadCargoPhoto(MultipartFile file);

    /**
     * 배송 완료 사진 파일을 업로드하고 저장된 객체 키를 반환합니다.
     */
    String uploadDropoffPhoto(MultipartFile file);

    /**
     * 문의 등록 시 첨부된 이미지를 업로드하고 저장된 객체 키를 반환합니다.
     */
    String uploadInquiryPhoto(MultipartFile file);

    /**
     * 객체 키를 외부에서 접근 가능한 공개 URL 형태로 변환합니다.
     */
    String buildPublicUrl(String objectKey);

    /**
     * 업로드 보상 처리 등에 사용하는 객체 삭제 메서드입니다.
     */
    void deleteObject(String objectKey);
}
