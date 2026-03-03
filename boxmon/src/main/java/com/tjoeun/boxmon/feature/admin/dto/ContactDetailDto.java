package com.tjoeun.boxmon.feature.admin.dto;

import com.tjoeun.boxmon.feature.admin.domain.Contact;
import com.tjoeun.boxmon.feature.admin.domain.ContactAttatchment;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class ContactDetailDto {
    private Contact contact;

    private List<String> contactAttatchment;


}
