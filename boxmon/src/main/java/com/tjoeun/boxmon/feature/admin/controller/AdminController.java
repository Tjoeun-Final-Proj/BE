package com.tjoeun.boxmon.feature.admin.controller;

import com.tjoeun.boxmon.feature.admin.dto.AdminRequest;
import com.tjoeun.boxmon.feature.admin.service.AdminService;
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
    public void createAdmin(@RequestBody AdminRequest request) {
        adminService.createAdmin(request);
    }

    @PostMapping("LoginAdmin")
    public void loginAdmin(@RequestBody AdminRequest request){
        adminService.login(request);
    }
}
