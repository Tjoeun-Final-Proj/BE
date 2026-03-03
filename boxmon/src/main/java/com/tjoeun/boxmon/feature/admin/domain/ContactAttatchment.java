package com.tjoeun.boxmon.feature.admin.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class ContactAttatchment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long attatchmentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false, name = "contact_id")
    private Contact contactId;

    private String content;

    public ContactAttatchment(Contact contactId, String content) {
        this.contactId = contactId;
        this.content = content;
    }


}
