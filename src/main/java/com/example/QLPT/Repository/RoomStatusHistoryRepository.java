package com.example.QLPT.Repository;


import com.example.QLPT.Enity.RoomStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface RoomStatusHistoryRepository extends JpaRepository<RoomStatusHistory, Long> {
    List<RoomStatusHistory> findByRoomId(Long roomId);
}
