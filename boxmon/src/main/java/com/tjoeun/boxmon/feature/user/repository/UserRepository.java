package com.tjoeun.boxmon.feature.user.repository;

import com.tjoeun.boxmon.feature.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    Optional<User> findByUserId(Long userId);
}
