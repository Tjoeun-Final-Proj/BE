package com.tjoeun.boxmon.feature.user.service;

import com.tjoeun.boxmon.feature.user.domain.User;
import com.tjoeun.boxmon.feature.user.dto.LoginRequest;
import com.tjoeun.boxmon.feature.user.dto.LoginResponse;
import com.tjoeun.boxmon.feature.user.dto.SignupRequest;
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
                .orElseThrow(()-> new RuntimeException("사용자 없음"));

        if(!passwordEncoder.matches(request.getPassword(), user.getPassword())){
            throw new RuntimeException("비밀번호 불일치");
        }

        String token = jwtProvider.createAccessToken(user.getUserId());
        return new LoginResponse(token);
    }

}
