package com.tjoeun.boxmon.feature.user.repository;

import com.tjoeun.boxmon.feature.payment.domain.PaymentMethod;
import com.tjoeun.boxmon.feature.user.domain.Shipper;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

// Shipper 엔티티를 위한 JPA Repository 인터페이스
@Repository
public interface ShipperRepository extends JpaRepository<Shipper, Long> {
    // ID로 Shipper를 찾는 메서드
    Optional<Shipper> findById(Long id);
}
