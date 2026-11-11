package com.example.QLPT.Repository;

import com.example.QLPT.Enity.UtilityReading;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

@Repository
public interface UtilityReadingRepository extends JpaRepository<UtilityReading, Long> {
    Optional<UtilityReading> findByRoomIdAndBillingCycle(Long roomId, String billingCycle); // Lấy số điện/nước của phòng trong chu kỳ thanh toán cụ thể
    List<UtilityReading> findByRoomId(Long roomId); // Lấy tất cả số điện/nước của phòng
    List<UtilityReading> findByBillingCycle(String billingCycle); // Lấy tất cả số điện/nước trong chu kỳ thanh toán cụ thể
}