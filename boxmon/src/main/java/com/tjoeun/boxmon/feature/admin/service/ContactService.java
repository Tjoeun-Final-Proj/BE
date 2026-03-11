package com.tjoeun.boxmon.feature.admin.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tjoeun.boxmon.exception.ContactNotFoundException;
import com.tjoeun.boxmon.exception.UserNotFoundException;
import com.tjoeun.boxmon.feature.admin.domain.*;
import com.tjoeun.boxmon.feature.admin.dto.ContactAnswerDto;
import com.tjoeun.boxmon.feature.admin.dto.ContactDetailDto;
import com.tjoeun.boxmon.feature.admin.dto.ContactDto;
import com.tjoeun.boxmon.feature.admin.repository.AdminRepository;
import com.tjoeun.boxmon.feature.admin.repository.AttatchmentRepository;
import com.tjoeun.boxmon.feature.admin.repository.ContactRepository;
import com.tjoeun.boxmon.feature.admin.repository.EventLogRepository;
import com.tjoeun.boxmon.feature.user.domain.User;
import com.tjoeun.boxmon.feature.user.repository.UserRepository;
import com.tjoeun.boxmon.global.storage.NcpObjectStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ContactService {

    private final ContactRepository contactRepository;
    private final UserRepository userRepository;
    private final AdminRepository adminRepository;
    private final NcpObjectStorageService storageService;
    private final AttatchmentRepository attatchmentRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final EventLogRepository eventLogRepository;

    //문의 생성
    public void createContact(Long userId, ContactDto request) {

        User user = userRepository.findById(userId)
                .orElseThrow(()-> new UserNotFoundException("사용자 없음"));
         Contact contact = new Contact(
                 user,
                 request.getContactContent()
         );
         contactRepository.save(contact);
        if (request.getImages() != null && !request.getImages().isEmpty()) {
            for (MultipartFile file : request.getImages()) {
                if (!file.isEmpty()) {
                    // 스토리지 업로드
                    String objectKey = storageService.uploadInquiryPhoto(file);
                    String publicUrl = storageService.buildPublicUrl(objectKey);

                    // 첨부파일 DB 저장
                    ContactAttatchment attatchment = new ContactAttatchment(contact, publicUrl);
                    attatchmentRepository.save(attatchment);
                }
            }
        }




    }

    //문의 답변
    @Transactional
    public void createAnswer(Long adminId, ContactAnswerDto request){
        Admin admin = adminRepository.findByAdminId(adminId)
                .orElseThrow(()-> new UserNotFoundException("관리자 없음"));
        Contact contact = contactRepository.findById(request.getContactId())
                .orElseThrow(()-> new ContactNotFoundException("문의 데이터 없음"));
        contact.setAnswererId(admin);
        contact.setAnsweredAt(LocalDateTime.now());
        contact.setAnswerContent(request.getAnswerContent());
        contactRepository.save(contact);

        String logMessage = String.format("%s번 문의 답변 완료", contact.getContactId());
        JsonNode payload = objectMapper.valueToTree(logMessage);


        eventLogRepository.save(EventLog.builder()
                .admin(admin)
                .eventType(AdminEventType.CONTACT_ANSWERED)
                .payload(payload)
                .build());
    }

    //문의 목록 조회
    public List<Contact> contactList(){
        List<Contact> contacts = contactRepository.findAll();
        return contacts;
    }

    //상세조회 (content + image)
    @Transactional(readOnly = true)
    public ContactDetailDto getContactDetail(Long contactId) {
        Contact contact = contactRepository.findById(contactId)
                .orElseThrow(() -> new ContactNotFoundException("문의글을 찾을 수 없습니다."));

        List<ContactAttatchment> attachments = attatchmentRepository.findByContactId(contact);

        List<String> imageUrls = attachments.stream()
                // 변경 전 코드 : .map(attachment -> storageService.buildPublicUrl(attachment.getContent()))
                .map(ContactAttatchment::getContent)
                .toList();

        return ContactDetailDto.builder()
                .contact(contact)
                .contactAttatchment(imageUrls)
                .build();
    }

    //사용자 문의 조회
    @Transactional(readOnly = true)
    public Map<Contact, List<ContactAttatchment>> getContact(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(()-> new UserNotFoundException("사용자 없음"));

        List<Contact> contacts = contactRepository.findAllByUserId(user);
        List<ContactAttatchment> attatchments = attatchmentRepository.findByContactIdIn(contacts);
        Map<Contact, List<ContactAttatchment>> attachmentMap = attatchments.stream()
                .collect(Collectors.groupingBy(ContactAttatchment::getContactId));
        return attachmentMap;
    }


}
