package com.tjoeun.boxmon.feature.admin.domain;

import com.tjoeun.boxmon.feature.user.domain.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Setter
@Getter
public class Contact {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long contactId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false, name = "user_id")
    private User userId;

    @Column(nullable = false, name = "contact_content")
    private String contactContent;

    @Column(nullable = false, name = "created_at")
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "answerer_id")
    private Admin answererId;

    @Column(name = "answer_content")
    private String answerContent;

    @Column(name = "answered_at")
    private LocalDateTime answeredAt;


    //생성
    public Contact(User userId, String contactContent) {
        this.userId = userId;
        this.contactContent = contactContent;
        this.createdAt = LocalDateTime.now();
    }

}
