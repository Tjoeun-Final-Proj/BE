package com.tjoeun.boxmon.global.storage;

import org.springframework.web.multipart.MultipartFile;

public interface ObjectStorageService {
    String uploadCargoPhoto(MultipartFile file);

    String buildPublicUrl(String objectKey);

    void deleteObject(String objectKey);
}

