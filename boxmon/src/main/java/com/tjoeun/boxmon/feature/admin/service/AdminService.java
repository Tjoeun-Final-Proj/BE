package com.tjoeun.boxmon.feature.admin.service;

import com.tjoeun.boxmon.feature.admin.domain.Admin;
import com.tjoeun.boxmon.feature.admin.dto.CreateRequest;
import com.tjoeun.boxmon.feature.admin.repository.AdminRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AdminService {

    private final AdminRepository adminRepository;
    private final BCryptPasswordEncoder encoder;

    public AdminService(AdminRepository adminRepository, BCryptPasswordEncoder encoder) {
        this.adminRepository = adminRepository;
        this.encoder = encoder;
    }

    public void createAdmin(CreateRequest request) {
        if (adminRepository.existsByLoginId(request.getLoginId())) {
            throw new IllegalArgumentException("이미 존재하는 관리자 ID");
        }

        Admin admin = new Admin(
                request.getLoginId(),
                encoder.encode(request.getPassword()),
                request.getName()
        );

        adminRepository.save(admin);
    }
}
