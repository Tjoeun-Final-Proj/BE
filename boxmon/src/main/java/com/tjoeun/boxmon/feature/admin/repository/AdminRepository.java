package com.tjoeun.boxmon.feature.admin.repository;

import com.tjoeun.boxmon.feature.admin.domain.Admin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface AdminRepository extends JpaRepository<Admin, Long> {
    boolean existsByLoginId(String loginId);
    Optional<Admin> findByLoginId(String loginId);
    Optional<Admin> findByAdminId(Long adminId);

    @Query("SELECT COUNT(a) FROM Admin a WHERE a.isDelete = false")
    long countDeletedAdmins();

    @Query("select a from Admin a where a.isDelete=false ")
    List<Admin> findAllByIsDelete();
}
