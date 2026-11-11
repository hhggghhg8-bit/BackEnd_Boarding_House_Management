package com.example.QLPT.Repository;

import com.example.QLPT.Enity.Roommate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface RoommateRepository extends JpaRepository<Roommate, Long> {
    List<Roommate> findByContractId(Long contractId);
}
