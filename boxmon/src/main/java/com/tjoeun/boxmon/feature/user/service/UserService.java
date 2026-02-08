package com.tjoeun.boxmon.feature.user.service;

import com.tjoeun.boxmon.exception.DuplicateEmailException;
import com.tjoeun.boxmon.exception.InvalidPasswordException;
import com.tjoeun.boxmon.exception.InvalidTokenException;
import com.tjoeun.boxmon.exception.TokenTypeMismatchException;
import com.tjoeun.boxmon.exception.UserNotFoundException;
import com.tjoeun.boxmon.feature.user.domain.User;
import com.tjoeun.boxmon.feature.user.dto.LoginRequest;
import com.tjoeun.boxmon.feature.user.dto.LoginResponse;
import com.tjoeun.boxmon.feature.user.dto.SignupRequest;
import com.tjoeun.boxmon.feature.user.dto.TokenRefreshRequest;
import com.tjoeun.boxmon.feature.user.dto.TokenRefreshResponse;
import com.tjoeun.boxmon.feature.user.repository.UserRepository;

import com.tjoeun.boxmon.security.jwt.JwtProvider;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtProvider jwtProvider) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtProvider = jwtProvider;
    }

    //회원가입
    public void signup(SignupRequest request) {
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
                request.getUserType()
        );
        userRepository.save(user);
    }


    //로그인
    public LoginResponse login(LoginRequest request){
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(()-> new UserNotFoundException("사용자 없음"));

        if(!passwordEncoder.matches(request.getPassword(), user.getPassword())){
            throw new InvalidPasswordException("비밀번호 불일치");
        }

        // Access Token 생성 (15분 만료)
        String accessToken = jwtProvider.createAccessToken(user.getUserId());
        
        // Refresh Token 생성 (14일 만료)
        String refreshToken = jwtProvider.createRefreshToken(user.getUserId());

        return new LoginResponse(accessToken, refreshToken);
    }

    // Access Token 갱신
    public TokenRefreshResponse refreshToken(TokenRefreshRequest request) {
        String refreshTokenValue = request.getRefreshToken();

        // Refresh Token 유효성 검증 (서명, 만료 확인)
        if (!jwtProvider.validateToken(refreshTokenValue)) {
            throw new InvalidTokenException("유효하지 않은 Refresh Token");
        }

        // Refresh Token 타입 확인
        String tokenType = jwtProvider.getTokenType(refreshTokenValue);
        if (!"REFRESH".equals(tokenType)) {
            throw new TokenTypeMismatchException("Refresh Token이 아닙니다");
        }

        // Refresh Token에서 userId 추출
        Long userId = jwtProvider.getUserIdFromToken(refreshTokenValue);

        // 새로운 Access Token 생성
        String newAccessToken = jwtProvider.createAccessToken(userId);

        // 새로운 Refresh Token 생성
        String newRefreshToken = jwtProvider.createRefreshToken(userId);

        return new TokenRefreshResponse(newAccessToken, newRefreshToken);
    }

}
