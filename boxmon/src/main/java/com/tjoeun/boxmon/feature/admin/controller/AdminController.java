package com.tjoeun.boxmon.feature.admin.controller;

import com.tjoeun.boxmon.feature.admin.dto.AdminLogin;
import com.tjoeun.boxmon.feature.admin.dto.AdminRequest;
import com.tjoeun.boxmon.feature.admin.service.AdminService;
import com.tjoeun.boxmon.feature.user.dto.LoginRequest;
import com.tjoeun.boxmon.feature.user.dto.LoginResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @PostMapping("/create")
    public ResponseEntity<Void> createAdmin(@RequestBody AdminRequest request) {
        adminService.createAdmin(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("LoginAdmin")
    public ResponseEntity<?> loginAdmin(@RequestBody @Valid AdminRequest request) {
        AdminLogin response = adminService.login(request);
        return ResponseEntity.ok(response);
    }

}
