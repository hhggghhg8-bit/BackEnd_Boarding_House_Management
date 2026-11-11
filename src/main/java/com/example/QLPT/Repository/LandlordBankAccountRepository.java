package com.example.QLPT.Repository;

import com.example.QLPT.Enity.LandlordBankAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface LandlordBankAccountRepository extends JpaRepository<LandlordBankAccount, Long> {
    List<LandlordBankAccount> findByUserId(Long userId); // Lấy các STK của 1 chủ trọ
}
