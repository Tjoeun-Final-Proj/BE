package com.tjoeun.boxmon.feature.user.controller;

import com.tjoeun.boxmon.feature.user.dto.*;
import com.tjoeun.boxmon.feature.user.service.UserService;
import com.tjoeun.boxmon.security.jwt.RefreshTokenService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/user")
public class UserController {

    private final UserService userService;
    private final RefreshTokenService refreshTokenService;

    public UserController(UserService userService, RefreshTokenService refreshTokenService) {
        this.userService = userService;
        this.refreshTokenService = refreshTokenService;
    }

    // 화주 회원가입
    @PostMapping("/shipperSignup")
    public void shipperSignup(@RequestBody @Valid ShipperSignupRequest request){
        userService.shipperSignup(request);
    }

    // 차주 회원가입
    @PostMapping("/driverSignup")
    public void driverSignup(@RequestBody @Valid DriverSignupRequest request){
        userService.driverSignup(request);
    }

    // 로그인
    @PostMapping("/login")
    public LoginResponse login(@RequestBody @Valid LoginRequest request){
        return userService.login(request);
    }

    // Access Token 갱신
    @PostMapping("/refresh")
    public TokenRefreshResponse refreshToken(@RequestBody @Valid TokenRefreshRequest request) {
        return refreshTokenService.refreshToken(request);
    }

    // 토근테스트
    @GetMapping("/test")
    public String test(Authentication authentication) {
        return "인증 성공 userId = " + authentication.getPrincipal();
    }



}
