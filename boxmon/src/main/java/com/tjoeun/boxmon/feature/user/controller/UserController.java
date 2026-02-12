package com.tjoeun.boxmon.feature.user.controller;

import com.tjoeun.boxmon.feature.user.dto.*;
import com.tjoeun.boxmon.feature.user.service.UserService;
import com.tjoeun.boxmon.security.jwt.RefreshTokenService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import com.tjoeun.boxmon.feature.user.dto.AccountDto;

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
    public ResponseEntity<Void> shipperSignup(@RequestBody @Valid ShipperSignupRequest request){
        userService.shipperSignup(request);
        return ResponseEntity.ok().build();
    }

    // 차주 회원가입
    @PostMapping("/driverSignup")
    public ResponseEntity<Void> driverSignup(@RequestBody @Valid DriverSignupRequest request){
        userService.driverSignup(request);
        return ResponseEntity.ok().build();
    }

    // 로그인
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody @Valid LoginRequest request){
        LoginResponse response = userService.login(request);
        return ResponseEntity.ok(response);
    }

    // Access Token 갱신
    @PostMapping("/refresh")
    public ResponseEntity<TokenRefreshResponse> refreshToken(@RequestBody @Valid TokenRefreshRequest request) {
        TokenRefreshResponse response = refreshTokenService.refreshToken(request);
        return ResponseEntity.ok(response);
    }

    // 토근테스트
    @GetMapping("/test")
    public String test(@AuthenticationPrincipal Long principal) {
        return "인증 성공 userId = " +principal;
    }

    // 회원 정보 수정
    @PostMapping("/modify")
    public ResponseEntity<Void> userModify(@AuthenticationPrincipal Long userId, @RequestBody @Valid UserModify request){
        userService.UserModify(userId, request);
        return ResponseEntity.ok().build();
    }

    //차주 입금 계좌 정보 입력
    @PostMapping("account")
    public ResponseEntity<Void> account(@AuthenticationPrincipal Long userId, @RequestBody @Valid AccountDto request){
        userService.Account(userId, request);
        return ResponseEntity.ok().build();
    }


}
