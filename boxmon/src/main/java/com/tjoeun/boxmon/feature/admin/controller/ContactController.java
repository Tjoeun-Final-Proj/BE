package com.tjoeun.boxmon.feature.admin.controller;

import com.tjoeun.boxmon.feature.admin.domain.Contact;
import com.tjoeun.boxmon.feature.admin.dto.ContactAnswerDto;
import com.tjoeun.boxmon.feature.admin.dto.ContactDto;
import com.tjoeun.boxmon.feature.admin.service.ContactService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/contact")
@RequiredArgsConstructor
public class ContactController {
    private final ContactService contactService;

    //문의 생성
    @PostMapping("/create")
    public ResponseEntity<Void> createContact (@AuthenticationPrincipal Long userId, @ModelAttribute ContactDto request){
        contactService.createContact(userId, request);
        return ResponseEntity.ok().build();
    }

    //문의 답변
    @PostMapping("/answer")
    public ResponseEntity<Void> answerContact(@AuthenticationPrincipal Long adminId,@RequestBody ContactAnswerDto request){
        contactService.createAnswer(adminId, request);
        return ResponseEntity.ok().build();
    }

    //문의 목록 조회
    @GetMapping("/list")
    public ResponseEntity<?> contactList(){
        List<Contact> contacts =  contactService.contactList();
        return ResponseEntity.ok(contacts);
    }

    //상세조회
    @GetMapping("/{contactId}")
    public ResponseEntity<?> getContact(@PathVariable Long contactId) {
        return ResponseEntity.ok(contactService.getContactDetail(contactId));
    }

}
