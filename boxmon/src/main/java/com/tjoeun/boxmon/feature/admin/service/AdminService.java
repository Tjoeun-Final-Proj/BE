package com.tjoeun.boxmon.feature.admin.service;


import com.tjoeun.boxmon.exception.DuplicateAdminException;
import com.tjoeun.boxmon.feature.admin.domain.Admin;
import com.tjoeun.boxmon.feature.admin.dto.CreateRequest;
import com.tjoeun.boxmon.feature.admin.repository.AdminRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AdminService {

    private final AdminRepository adminRepository;
    private final PasswordEncoder encoder;

    public AdminService(AdminRepository adminRepository, PasswordEncoder encoder) {
        this.adminRepository = adminRepository;
        this.encoder = encoder;
    }

    public void createAdmin(CreateRequest request) {
        if (adminRepository.existsByLoginId(request.getLoginId())) {
            throw new DuplicateAdminException("이미 존재하는 관리자 ID");
        }

        Admin admin = new Admin(
                request.getLoginId(),
                encoder.encode(request.getPassword()),
                request.getName()
        );

        adminRepository.save(admin);
    }
}
