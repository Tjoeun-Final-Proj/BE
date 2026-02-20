package com.tjoeun.boxmon.feature.notification.service;

import com.tjoeun.boxmon.feature.notification.domain.NotificationLog;
import com.tjoeun.boxmon.feature.notification.dto.NotificationResponse;
import com.tjoeun.boxmon.feature.notification.mapper.NotificationLogMapper;
import com.tjoeun.boxmon.feature.notification.repository.NotificationRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificationReadService {
    private final NotificationRepository notificationRepository;
    private final NotificationLogMapper mapper;

    public NotificationReadService(NotificationRepository notificationRepository, NotificationLogMapper notificationLogMapper) {
        this.notificationRepository = notificationRepository;
        this.mapper = notificationLogMapper;
    }

    public List<NotificationResponse> getListOf(Long userId){
        //db에서 로그 꺼내기
        List<NotificationLog> logs = notificationRepository.findByTarget_UserId(userId);
        
        return logs
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    //TODO 알림 유스케이스별 메소드 추가 ex) 채팅 알림보내기, 배차완료 알림 보내기 등...
}
