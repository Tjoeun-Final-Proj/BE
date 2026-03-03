package com.tjoeun.boxmon.feature.admin.controller;

import com.tjoeun.boxmon.feature.admin.domain.Penalties;
import com.tjoeun.boxmon.feature.admin.dto.PenaltiesDto;
import com.tjoeun.boxmon.feature.admin.service.PenaltyService;
import com.tjoeun.boxmon.feature.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/penalty")
@RequiredArgsConstructor
public class PenaltyController {

    private final PenaltyService penaltyService;

    //계정 정지
    @PostMapping("/suspension")
    public ResponseEntity<Void> accountSuspension (@RequestBody Long userId){
        penaltyService.accountSuspension(userId);
        return ResponseEntity.ok().build();
    }

    //계정 복구
    @PostMapping("/restoration")
    public ResponseEntity<Void> accountRestoration (@RequestBody Long userId){
        penaltyService.accountRestoration(userId);
        return ResponseEntity.ok().build();
    }

    //패널티 추가
    @PostMapping("/create")
    public ResponseEntity<Void> createPenalty (@RequestBody PenaltiesDto request){
        penaltyService.createPenalty(request);
        return ResponseEntity.ok().build();
    }

    //페널티 삭제
    @PostMapping("/delete")
    public ResponseEntity<Void> deletePenalty (@RequestBody Long penaltyId){
        penaltyService.deletePenalty(penaltyId);
        return ResponseEntity.ok().build();
    }

    // 패널티 목록 조회
    @GetMapping("list")
    public ResponseEntity<?> getUserList(@RequestParam Long userId) {
        List<Penalties> penalties =penaltyService.PenaltyList(userId);
        return ResponseEntity.ok(penalties);
    }

}
