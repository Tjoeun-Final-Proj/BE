package com.tjoeun.boxmon.feature.user.service;

import com.tjoeun.boxmon.exception.DuplicateEmailException;
import com.tjoeun.boxmon.exception.InvalidPasswordException;
import com.tjoeun.boxmon.exception.UserNotFoundException;
import com.tjoeun.boxmon.feature.user.domain.Driver;
import com.tjoeun.boxmon.feature.user.domain.Shipper;
import com.tjoeun.boxmon.feature.user.domain.User;
import com.tjoeun.boxmon.feature.user.domain.UserType;
import com.tjoeun.boxmon.feature.user.dto.*;
import com.tjoeun.boxmon.feature.user.repository.DriverRepository;
import com.tjoeun.boxmon.feature.user.repository.ShipperRepository;
import com.tjoeun.boxmon.feature.user.repository.UserRepository;

import com.tjoeun.boxmon.security.jwt.JwtProvider;
import jakarta.transaction.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


@Service
public class UserService {

    private final UserRepository userRepository;
    private final ShipperRepository shipperRepository;
    private final DriverRepository driverRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    public UserService(UserRepository userRepository, UserRepository userRepository1, ShipperRepository shipperRepository, DriverRepository driverRepository, PasswordEncoder passwordEncoder, JwtProvider jwtProvider) {
        this.userRepository = userRepository1;
        this.shipperRepository = shipperRepository;
        this.driverRepository = driverRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtProvider = jwtProvider;
    }

    //화주 회원가입
    @Transactional
    public void shipperSignup(ShipperSignupRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateEmailException("이미 존재하는 이메일입니다");
        }
        String encodedPW = passwordEncoder.encode(request.getPassword());
        User user = new User(
                request.getEmail(),
                encodedPW,
                request.getName(),
                request.getPhone(),
                request.getBirth(),
                request.getIsPushEnabled(),
                request.getUserType(),
                request.getBusinessNumber(),
                ""

        );
        user = userRepository.save(user);
        Shipper shipper = new Shipper(user);
        shipperRepository.save(shipper);
    }

    @Transactional
    //차주 회원가입
    public void driverSignup(DriverSignupRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateEmailException("이미 존재하는 이메일입니다");
        }

        //자격증 확인 넣어야함

        String encodedPW = passwordEncoder.encode(request.getPassword());
        User user = new User(
                request.getEmail(),
                encodedPW,
                request.getName(),
                request.getPhone(),
                request.getBirth(),
                request.getIsPushEnabled(),
                request.getUserType(),
                request.getBusinessNumber(),
                ""
        );
        user = userRepository.save(user);

        Driver driver = new Driver(
                user,
                request.getCertNumber()
                );
        driverRepository.save(driver);
    }


    //로그인
    public LoginResponse login(LoginRequest request){
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(()-> new UserNotFoundException("사용자 없음"));

        if(!passwordEncoder.matches(request.getPassword(), user.getPassword())){
            throw new InvalidPasswordException("비밀번호 불일치");
        }

        user.setDeviceToken(request.getDeviceToken());
        userRepository.save(user);

        // Access Token 생성 (15분 만료)
        String accessToken = jwtProvider.createAccessToken(user.getUserId());
        
        // Refresh Token 생성 (14일 만료)
        String refreshToken = jwtProvider.createRefreshToken(user.getUserId());

        UserType userType = user.getUserType();

        return new LoginResponse(accessToken, refreshToken, userType);
    }

    // 회원 정보 수정
    public void UserModify(Long userId, UserModify request) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(()-> new UserNotFoundException("사용자 없음"));
        user.setName(request.getName());
        user.setPhone(request.getPhone());
        user.setBusinessNumber(request.getBusinessNumber());
        user.setIsPushEnabled(request.getIsPushEnabled());

        userRepository.save(user);
    }

    //차주 입금 계좌 정보 입력 및 수정
    public void Account(Long userId, AccountDto request) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(()-> new UserNotFoundException("사용자 없음"));
        user.setName(request.getBankCode());
        user.setPhone(request.getAccountNumber());
        user.setBusinessNumber(request.getHolderName());
        userRepository.save(user);
    }





}
