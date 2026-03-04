package com.tjoeun.boxmon.feature.admin.service;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.tjoeun.boxmon.exception.DuplicateAdminException;
import com.tjoeun.boxmon.exception.InvalidPasswordException;
import com.tjoeun.boxmon.exception.UserNotFoundException;


import com.tjoeun.boxmon.feature.admin.domain.Admin;
import com.tjoeun.boxmon.feature.admin.domain.AdminEventType;
import com.tjoeun.boxmon.feature.admin.domain.EventLog;
import com.tjoeun.boxmon.feature.admin.dto.AdminLogin;
import com.tjoeun.boxmon.feature.admin.dto.AdminRequest;
import com.tjoeun.boxmon.feature.admin.repository.AdminRepository;
import com.tjoeun.boxmon.feature.admin.repository.EventLogRepository;
import com.tjoeun.boxmon.security.jwt.JwtProvider;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminService {

    private final AdminRepository adminRepository;
    private final PasswordEncoder encoder;
    private  final JwtProvider jwtProvider;
    private final PasswordEncoder passwordEncoder;
    private final EntityManager em;
    private final EventLogRepository eventLogRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    // 관리자 계정 생성
    @Transactional
    public void createAdmin(Long adminId, AdminRequest adminRequest) {
        Admin currentAdmin = adminRepository.findByAdminId(adminId)
                .orElseThrow(() -> new UserNotFoundException("관리자 없음"));

        if (adminRepository.existsByLoginId(adminRequest.getLoginId())) {
            throw new DuplicateAdminException("이미 존재하는 관리자 ID");
        }

        Admin admin = new Admin(
                adminRequest.getLoginId(),
                encoder.encode(adminRequest.getPassword()),
                adminRequest.getName()
        );

        adminRepository.save(admin);

        // %d(ID:%d) 형식을 맞추기 위해 이름과 ID를 각각 넣어줍니다.
        String logMessage = String.format("%s(ID:%d)가 %s(ID:%d)를 생성",
                currentAdmin.getName(), currentAdmin.getAdminId(),
                admin.getName(), admin.getAdminId());

        // 팀원 방식대로 objectMapper를 사용해 JsonNode로 변환합니다.
        JsonNode payload = objectMapper.valueToTree(logMessage);

        eventLogRepository.save(EventLog.builder()
                .admin(currentAdmin)
                .eventType(AdminEventType.ADMIN_REGISTERED)
                .payload(payload)
                .build());
    }

    //로그인
    public AdminLogin login(AdminRequest request){
        Admin admin = adminRepository.findByLoginId(request.getLoginId())
                .orElseThrow(()-> new UserNotFoundException("사용자 없음"));

        if(!passwordEncoder.matches(request.getPassword(), admin.getPassword())){
            throw new InvalidPasswordException("비밀번호 불일치");
        }

        // Access Token 생성 (15분 만료)
        String accessToken = jwtProvider.createAccessToken(admin.getAdminId(), true);

        // Refresh Token 생성 (14일 만료)
        String refreshToken = jwtProvider.createRefreshToken(admin.getAdminId(), true);

        return new AdminLogin(accessToken, refreshToken);
    }

    // 이름 출력
    public String getName(Long adminId){
        Admin admin = adminRepository.findByAdminId(adminId)
                .orElseThrow();
        return admin.getName();
    }

    //관리자 목록 조회
    public List<Admin> getAdminList() {
        List<Admin> admins = adminRepository.findAll();
        return admins;
    }

    //관리자 계정 탈퇴
    @Transactional
    public void deleteAdmin(Long adminId, String pw){
        Admin admin = adminRepository.findByAdminId(adminId)
                .orElseThrow();
        if(!passwordEncoder.matches(pw, admin.getPassword())){
            throw new InvalidPasswordException("비밀번호 불일치");
        }
        adminRepository.deleteById(adminId);
        em.flush();
    }


}
