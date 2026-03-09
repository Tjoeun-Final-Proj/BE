package com.tjoeun.boxmon.feature.admin.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tjoeun.boxmon.exception.UserNotFoundException;
import com.tjoeun.boxmon.feature.admin.domain.Admin;
import com.tjoeun.boxmon.feature.admin.domain.AdminEventType;
import com.tjoeun.boxmon.feature.admin.domain.EventLog;
import com.tjoeun.boxmon.feature.admin.domain.Penalties;
import com.tjoeun.boxmon.feature.admin.dto.PenaltiesDto;
import com.tjoeun.boxmon.feature.admin.repository.AdminRepository;
import com.tjoeun.boxmon.feature.admin.repository.EventLogRepository;
import com.tjoeun.boxmon.feature.admin.repository.PenaltiesRepository;
import com.tjoeun.boxmon.feature.user.domain.User;
import com.tjoeun.boxmon.feature.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.apache.catalina.Store;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PenaltyService {

    private final UserRepository userRepository;
    private final PenaltiesRepository penaltiesRepository;
    private final AdminRepository adminRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final EventLogRepository eventLogRepository;

    //계정 정지
    @Transactional
    public void accountSuspension(Long adminId, Long userId){
        Admin admin = adminRepository.findByAdminId(adminId)
                .orElseThrow(()->new UserNotFoundException("관리자를 찾을 수 없습니다"));
        User user = userRepository.findById(userId)
                .orElseThrow(()-> new UserNotFoundException("사용자 없음"));
        user.setAccountStatus(Boolean.FALSE);
        userRepository.save(user);

        String logMessage = String.format("사용자 %s 계정 정지",user.getName() );
        JsonNode payload = objectMapper.valueToTree(logMessage);


        eventLogRepository.save(EventLog.builder()
                .admin(admin)
                .eventType(AdminEventType.MEMBER_SUSPENDED)
                .payload(payload)
                .build());
    }

    //계정 복구
    @Transactional
    public void accountRestoration(Long adminId, Long userId){
        Admin admin = adminRepository.findByAdminId(adminId)
                .orElseThrow(()->new UserNotFoundException("관리자를 찾을 수 없습니다"));
        User user = userRepository.findById(userId)
                .orElseThrow(()-> new UserNotFoundException("사용자 없음"));
        user.setAccountStatus(Boolean.TRUE);
        userRepository.save(user);

        String logMessage = String.format("사용자 %s 계정 복구", user.getName());
        JsonNode payload = objectMapper.valueToTree(logMessage);


        eventLogRepository.save(EventLog.builder()
                .admin(admin)
                .eventType(AdminEventType.MEMBER_RESTORED)
                .payload(payload)
                .build());
    }

    //패널티 추가
    @Transactional
    public void createPenalty(PenaltiesDto request){
        Admin admin = adminRepository.findByAdminId(request.getAdminId())
                .orElseThrow(()->new UserNotFoundException("관리자를 찾을 수 없습니다"));
        User user = userRepository.findByUserId(request.getUserId())
                .orElseThrow(()->new UserNotFoundException("사용자를 찾을 수 없습니다"));
        Penalties penalties = new Penalties(
                admin,
                user,
                request.getPayload()
        );
        penaltiesRepository.save(penalties);

        String logMessage = String.format("사용자 %s 경고, 사유 : %s",user.getName(), request.getPayload());
        JsonNode payload = objectMapper.valueToTree(logMessage);


        eventLogRepository.save(EventLog.builder()
                .admin(admin)
                .eventType(AdminEventType.MEMBER_WARNED)
                .payload(payload)
                .build());
    }

    //패널티 삭제
    @Transactional
    public void deletePenalty(Long penaltyId) {
        Penalties penalties = penaltiesRepository.findById(penaltyId)
                .orElseThrow();
        penaltiesRepository.delete(penalties);

        String logMessage = String.format("\"%s\"에 대한 사용자 %s 경고 제거", penalties.getPayload(),penalties.getUser().getName());
        JsonNode payload = objectMapper.valueToTree(logMessage);


        eventLogRepository.save(EventLog.builder()
                .admin(penalties.getAdminId())
                .eventType(AdminEventType.MEMBER_WARNING_REMOVED)
                .payload(payload)
                .build());
    }


    //패널티 조회
    public List<Penalties> PenaltyList(Long userId){
        List<Penalties> penalties = penaltiesRepository.findAllByUser_UserId(userId);
        return penalties;
    }

}
