package com.tjoeun.boxmon.feature.user.service;

import com.tjoeun.boxmon.exception.DuplicateVehicleException;
import com.tjoeun.boxmon.exception.UserNotFoundException;
import com.tjoeun.boxmon.feature.user.domain.Driver;
import com.tjoeun.boxmon.feature.user.domain.Vehicle;
import com.tjoeun.boxmon.feature.user.dto.VehicleRegistrationRequest;
import com.tjoeun.boxmon.feature.user.repository.DriverRepository;
import com.tjoeun.boxmon.feature.user.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class VehicleServiceImpl implements VehicleService {

    private final VehicleRepository vehicleRepository;
    private final DriverRepository driverRepository;

    @Override
    public void registerVehicle(Long driverId, VehicleRegistrationRequest request) {
        // 1. 차주 조회
        Driver driver = driverRepository.findById(driverId)
                .orElseThrow(() -> new UserNotFoundException("차주를 찾을 수 없습니다."));

        // 2. 이미 등록된 차량 번호인지 확인 (선택 사항: DB unique 제약 조건이 처리할 수도 있음)
         vehicleRepository.findByVehicleNumber(request.getVehicleNumber()).ifPresent(v -> {
             try {
                 throw new DuplicateVehicleException("이미 등록된 차량 번호입니다.");
             } catch (DuplicateVehicleException e) {
                 throw new RuntimeException(e);
             }
         });

        // 3. Vehicle 엔티티 생성
        Vehicle vehicle = Vehicle.builder()
                .driver(driver)
                .vehicleNumber(request.getVehicleNumber())
                .vehicleType(request.getVehicleType())
                .canRefrigerate(request.getCanRefrigerate())
                .canFreeze(request.getCanFreeze())
                .weightCapacity(request.getWeightCapacity())
                .build();

        // 4. 저장
        vehicleRepository.save(vehicle);
    }
}
