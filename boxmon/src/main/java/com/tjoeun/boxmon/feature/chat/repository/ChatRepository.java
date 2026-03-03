package com.tjoeun.boxmon.feature.chat.repository;

import com.tjoeun.boxmon.feature.chat.domain.Chat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatRepository extends JpaRepository<Chat, Long> {
    List<Chat> findByShipment_ShipmentIdOrderByCreatedAtAscChatIdAsc(Long shipmentId);
}
