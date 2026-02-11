package com.tjoeun.boxmon.feature.user.repository;

import com.tjoeun.boxmon.feature.payment.domain.PaymentMethod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

// PaymentMethod 엔티티를 위한 JPA Repository 인터페이스
@Repository
public interface PaymentMethodRepository extends JpaRepository<PaymentMethod, Long> {
}
