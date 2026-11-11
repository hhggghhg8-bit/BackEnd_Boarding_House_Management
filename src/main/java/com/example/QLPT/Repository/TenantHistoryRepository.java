package com.example.QLPT.Repository;


import com.example.QLPT.Enity.TenantHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TenantHistoryRepository extends JpaRepository<TenantHistory, Long> {
    List<TenantHistory> findByTenantId(Long tenantId);
    List<TenantHistory> findByRoomId(Long roomId);
}
