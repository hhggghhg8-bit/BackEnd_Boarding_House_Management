package com.example.QLPT.Repository;

import com.example.QLPT.Enity.Contract;
import com.example.QLPT.Enity.Enum.Enum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ContractRepository extends JpaRepository<Contract, Long> {
    List<Contract> findByTenantId(Long tenantId);
    Optional<Contract> findByRoomIdAndStatus(Long roomId, Enum.ContractStatus status);
    List<Contract> findByTenantIdAndStatus(Long tenantId, Enum.ContractStatus status);

}
