package com.tjoeun.boxmon.feature.admin.service;

import com.tjoeun.boxmon.exception.UserNotFoundException;
import com.tjoeun.boxmon.feature.admin.domain.Admin;
import com.tjoeun.boxmon.feature.admin.domain.Penalties;
import com.tjoeun.boxmon.feature.admin.dto.PenaltiesDto;
import com.tjoeun.boxmon.feature.admin.repository.AdminRepository;
import com.tjoeun.boxmon.feature.admin.repository.PenaltiesRepository;
import com.tjoeun.boxmon.feature.user.domain.User;
import com.tjoeun.boxmon.feature.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PenaltyService {

    private final UserRepository userRepository;
    private final PenaltiesRepository penaltiesRepository;
    private final AdminRepository adminRepository;

    //계정 정지
    public void accountSuspension(Long userId){
        User user = userRepository.findById(userId)
                .orElseThrow(()-> new UserNotFoundException("사용자 없음"));
        user.setAccountStatus(Boolean.FALSE);
    }

    //계정 복구
    public void accountRestoration(Long userId){
        User user = userRepository.findById(userId)
                .orElseThrow(()-> new UserNotFoundException("사용자 없음"));
        user.setAccountStatus(Boolean.TRUE);
    }

    //패널티 추가
    public void createPenalty(PenaltiesDto request){
        Admin admin = adminRepository.findByAdminId(request.getAdminId())
                .orElseThrow(()->new UserNotFoundException("관리자를 찾을 수 없습니다"));
        User user = userRepository.findByUserId(request.getUserId())
                .orElseThrow(()->new UserNotFoundException("사용자를 찾을 수 없습니다"));
        Penalties penalties = new Penalties(
                admin,
                user,
                request.getPayload()
        );
        penaltiesRepository.save(penalties);
    }

    //패널티 삭제
    public void deletePenalty(Long penaltyId){
        Penalties penalties = penaltiesRepository.findById(penaltyId)
                .orElseThrow();
        penaltiesRepository.delete(penalties);
    }
}
