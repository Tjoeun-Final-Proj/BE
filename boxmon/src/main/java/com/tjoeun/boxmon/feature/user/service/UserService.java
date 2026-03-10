package com.tjoeun.boxmon.feature.user.service;

import com.tjoeun.boxmon.exception.DuplicateEmailException;
import com.tjoeun.boxmon.exception.InvalidPasswordException;
import com.tjoeun.boxmon.exception.UserNotFoundException;
import com.tjoeun.boxmon.feature.settlement.service.DriverRegisterUseCase;
import com.tjoeun.boxmon.feature.user.domain.*;
import com.tjoeun.boxmon.feature.user.dto.*;
import com.tjoeun.boxmon.feature.user.repository.DriverRepository;
import com.tjoeun.boxmon.feature.user.repository.ShipperRepository;
import com.tjoeun.boxmon.feature.user.repository.UserRepository;

import com.tjoeun.boxmon.feature.user.repository.VehicleRepository;
import com.tjoeun.boxmon.security.jwt.JwtProvider;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final ShipperRepository shipperRepository;
    private final DriverRepository driverRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final VehicleRepository vehicleRepository;
    private final DriverRegisterUseCase driverRegisterUsecase;


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
                "",
                Boolean.TRUE,
                Boolean.FALSE
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
                "",
                Boolean.TRUE,
                Boolean.FALSE
        );
        user = userRepository.save(user);

        Driver driver = new Driver(user);
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
        String accessToken = jwtProvider.createAccessToken(user.getUserId(), false);
        
        // Refresh Token 생성 (14일 만료)
        String refreshToken = jwtProvider.createRefreshToken(user.getUserId(), false);

        UserType userType = user.getUserType();

        return new LoginResponse(accessToken, refreshToken, userType, user.getName(), user.getEmail());
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
        Driver driver = driverRepository.findByUser_UserId(userId)
                .orElseThrow(()-> new UserNotFoundException("차주 없음"));;
        driver.setBankCode(request.getBankCode());
        driver.setAccountNumber(request.getAccountNumber());
        driver.setHolderName(request.getHolderName());

        String tossSellerId = driverRegisterUsecase.registerDriver(driver);
        driver.setTossSellerId(tossSellerId);

        driverRepository.save(driver);
    }

    // 사용자 정보 조회
    public List<User> getUserList() {
        List<User> users = userRepository.findAll();
        return users;
    }

    //회원 탈퇴 (소프트삭제)
    public void deleteUser(Long userId, String pw){
        User user = userRepository.findByUserId(userId)
                .orElseThrow(()-> new UserNotFoundException("사용자 없음"));
        if(!passwordEncoder.matches(pw, user.getPassword())){
            throw new InvalidPasswordException("비밀번호 불일치");
        }
        if(user.getIsDelete()){
            return;
        }
        
        // 삭제된 사용자 수를 세어서 다음 번호 결정
        long deletedCount = userRepository.countDeletedUsers();
        String deletedEmail = (deletedCount + 1) + "@delete.com";

        user.setEmail(deletedEmail);
        user.setName("탈퇴한 사용자");
        user.setPhone("탈퇴한 사용자");
        user.setIsDelete(true);
        
        userRepository.save(user);
    }


    public ShipperDetail getShipperDetail(Long userId) {
        User user = userRepository.findByUserId(userId).orElseThrow(()-> new UserNotFoundException("사용자 없음"));
        ShipperDetail shipperDetail = new ShipperDetail(
                user.getEmail(),
                user.getName(),
                user.getPhone(),
                user.getCreatedAt(),
                user.getBirth(),
                user.getIsPushEnabled(),
                user.getBusinessNumber(),
                user.getAccountStatus()
        );
                return shipperDetail;
    }

    public DriverDetail getDriverDetail(Long userId) {
        User user = userRepository.findByUserId(userId).orElseThrow(() -> new UserNotFoundException("사용자 없음"));
        Driver driver = driverRepository.findByUser_UserId(userId).orElseThrow(() -> new UserNotFoundException("사용자 없음"));
        Vehicle vehicle = vehicleRepository.findByDriver_User_UserId(userId);

        return new DriverDetail(
                user.getEmail(),
                user.getName(),
                user.getPhone(),
                user.getCreatedAt(),
                user.getBirth(),
                user.getIsPushEnabled(),
                user.getBusinessNumber(),
                user.getAccountStatus(),
                driver != null ? driver.getBankCode() : null,
                driver != null ? driver.getAccountNumber() : null,
                driver != null ? driver.getHolderName() : null,
                vehicle != null ? vehicle.getVehicleNumber() : null,
                vehicle != null ? vehicle.getVehicleType() : null,
                vehicle != null ? vehicle.getCanRefrigerate() : false,
                vehicle != null ? vehicle.getCanFreeze() : false,
                vehicle != null ? vehicle.getWeightCapacity() : null
        );
    }

}
