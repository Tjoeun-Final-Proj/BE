package com.tjoeun.boxmon.feature.admin.service;

<<<<<<< Updated upstream
import com.tjoeun.boxmon.exception.BusinessException;
=======

import com.tjoeun.boxmon.exception.DuplicateAdminException;
import com.tjoeun.boxmon.exception.InvalidPasswordException;
import com.tjoeun.boxmon.exception.UserNotFoundException;
>>>>>>> Stashed changes
import com.tjoeun.boxmon.feature.admin.domain.Admin;
import com.tjoeun.boxmon.feature.admin.dto.AdminLogin;
import com.tjoeun.boxmon.feature.admin.dto.AdminRequest;
import com.tjoeun.boxmon.feature.admin.repository.AdminRepository;
import com.tjoeun.boxmon.feature.user.domain.User;
import com.tjoeun.boxmon.feature.user.dto.LoginResponse;
import com.tjoeun.boxmon.security.jwt.JwtProvider;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AdminService {

    private final AdminRepository adminRepository;
    private final PasswordEncoder encoder;
    private  final JwtProvider jwtProvider;
    private final PasswordEncoder passwordEncoder;

    public AdminService(AdminRepository adminRepository, PasswordEncoder encoder, JwtProvider jwtProvider, PasswordEncoder passwordEncoder) {
        this.adminRepository = adminRepository;
        this.encoder = encoder;
        this.jwtProvider = jwtProvider;
        this.passwordEncoder = passwordEncoder;
    }

    // 관리자 계정 생성
    public void createAdmin(AdminRequest adminRequest) {
        if (adminRepository.existsByLoginId(adminRequest.getLoginId())) {
            throw new DuplicateAdminException("이미 존재하는 관리자 ID");
        }

        Admin admin = new Admin(
                adminRequest.getLoginId(),
                encoder.encode(adminRequest.getPassword()),
                adminRequest.getName()
        );

        adminRepository.save(admin);
    }

    //로그인
    public AdminLogin login(AdminRequest request){
        Admin admin = adminRepository.findByLoginId(request.getLoginId())
                .orElseThrow(()-> new UserNotFoundException("사용자 없음"));

        if(!passwordEncoder.matches(request.getPassword(), admin.getPassword())){
            throw new InvalidPasswordException("비밀번호 불일치");
        }

        // Access Token 생성 (15분 만료)
        String accessToken = jwtProvider.createAccessToken(admin.getAdminId());

        // Refresh Token 생성 (14일 만료)
        String refreshToken = jwtProvider.createRefreshToken(admin.getAdminId());

        return new AdminLogin(accessToken, refreshToken);
    }
}
