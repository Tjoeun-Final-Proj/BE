package com.tjoeun.boxmon.security.jwt;

import com.tjoeun.boxmon.feature.user.domain.User;
import com.tjoeun.boxmon.feature.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class JwtService {
    private final UserRepository userRepository;

    public Boolean statusCheck(Long accountId){
        return userRepository.findByUserId(accountId)
                .orElseThrow(() -> new RuntimeException("유저 없음"))
                .getAccountStatus();
    }
}
