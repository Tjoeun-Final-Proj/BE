package com.tjoeun.boxmon.feature.admin.controller;

import com.tjoeun.boxmon.feature.admin.domain.Admin;
import com.tjoeun.boxmon.feature.admin.dto.AdminLogin;
import com.tjoeun.boxmon.feature.admin.dto.AdminRequest;
import com.tjoeun.boxmon.feature.admin.service.AdminService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;


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

    @GetMapping("/me")
    public ResponseEntity<String> getName(@AuthenticationPrincipal Long adminId) {
        String response = adminService.getName(adminId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("list")
    public ResponseEntity<?> getAdminList() {
        List<Admin> admins = adminService.getAdminList();
        return ResponseEntity.ok(admins);
    }

}
