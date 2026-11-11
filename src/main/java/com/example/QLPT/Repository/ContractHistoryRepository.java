package com.example.QLPT.Repository;
import com.example.QLPT.Enity.ContractHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ContractHistoryRepository extends JpaRepository<ContractHistory, Long> {
    List<ContractHistory> findByContractId(Long contractId);
}
