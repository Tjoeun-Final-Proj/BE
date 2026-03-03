package com.tjoeun.boxmon.feature.settlement.repository;

import com.tjoeun.boxmon.feature.settlement.domain.Settlement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SettlementRepository extends JpaRepository<Settlement, Long> {
}
