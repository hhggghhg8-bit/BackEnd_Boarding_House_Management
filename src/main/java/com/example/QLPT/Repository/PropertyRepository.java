package com.example.QLPT.Repository;

import com.example.QLPT.Enity.Property;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PropertyRepository extends JpaRepository<Property, Long> {
    List<Property> findByLandlordId(Long landlordId); // Lấy danh sách tòa nhà của 1 chủ trọ
    List<Property> findByLandlordIdAndDeletedAtIsNull(Long landlordId);
}
