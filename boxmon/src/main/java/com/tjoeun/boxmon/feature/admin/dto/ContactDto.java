package com.tjoeun.boxmon.feature.admin.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
public class ContactDto {

    private String contactContent;

    private List<MultipartFile> images;
    }
