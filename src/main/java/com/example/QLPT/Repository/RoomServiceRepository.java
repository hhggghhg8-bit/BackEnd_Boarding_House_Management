package com.example.QLPT.Repository;

import com.example.QLPT.Enity.RoomService;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface RoomServiceRepository extends JpaRepository<RoomService, Long> {
    List<RoomService> findByRoomId(Long roomId); // Lấy danh sách dịch vụ áp dụng cho 1 phòng
    boolean existsByRoomIdAndServiceId(Long roomId, Long serviceId); // Kiểm tra xem phòng đã có dịch vụ này chưa
}
