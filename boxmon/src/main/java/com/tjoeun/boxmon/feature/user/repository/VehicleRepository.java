package com.tjoeun.boxmon.feature.user.repository;

import com.tjoeun.boxmon.feature.user.domain.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, Long> {
    // 차량 번호로 차량 존재 여부를 확인하기 위한 메소드
    Optional<Vehicle> findByVehicleNumber(String vehicleNumber);
}
