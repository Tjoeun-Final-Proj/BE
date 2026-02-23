package com.tjoeun.boxmon.feature.payment;

import com.tjoeun.boxmon.feature.payment.domain.PaymentLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentLogRepository extends JpaRepository<PaymentLog, String> {
}
