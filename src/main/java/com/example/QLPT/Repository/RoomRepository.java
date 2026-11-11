package com.example.QLPT.Repository;

import com.example.QLPT.Enity.Enum.Enum;
import com.example.QLPT.Enity.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {
    // Filter management queries here so historical contracts can still resolve deleted rooms.
    @Override
    @Query("select r from Room r where r.id = :id and r.deletedAt is null")
    Optional<Room> findById(Long id);

    @Override
    @Query("select r from Room r where r.deletedAt is null")
    List<Room> findAll();

    @Query("select r from Room r where r.property.id = :propertyId and r.deletedAt is null")
    List<Room> findByPropertyId(Long propertyId); // Lấy tất cả phòng trong 1 tòa nhà
    @Query("select r from Room r where r.status = :status and r.deletedAt is null")
    List<Room> findByStatus(Enum.RoomStatus status);
    @Query("select (count(r) > 0) from Room r where r.property.id = :propertyId and r.roomNumber = :roomNumber and r.deletedAt is null")
    boolean existsByPropertyIdAndRoomNumber(Long propertyId, String roomNumber);
}
