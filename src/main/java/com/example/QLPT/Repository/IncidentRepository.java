package com.example.QLPT.Repository;


import com.example.QLPT.Enity.Incident;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface IncidentRepository extends JpaRepository<Incident, Long> {
    List<Incident> findByRoomId(Long roomId);
    List<Incident> findByTenantId(Long tenantId);
}
