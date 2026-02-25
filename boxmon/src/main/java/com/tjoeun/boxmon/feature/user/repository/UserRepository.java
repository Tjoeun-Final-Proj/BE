package com.tjoeun.boxmon.feature.user.repository;

import com.tjoeun.boxmon.feature.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    Optional<User> findByUserId(Long userId);
    
    @Query("SELECT COUNT(u) FROM User u WHERE u.email LIKE '%@delete.com'")
    long countDeletedUsers();
}
