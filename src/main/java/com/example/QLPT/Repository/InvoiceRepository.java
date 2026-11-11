package com.example.QLPT.Repository;

import com.example.QLPT.Enity.Enum.Enum;
import com.example.QLPT.Enity.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

    List<Invoice> findByContractId(Long contractId);

    List<Invoice> findByStatus(Enum.InvoiceStatus status);

    // Tìm hóa đơn theo Hợp đồng và Tháng/Năm (VD: "10-2026")
    Optional<Invoice> findByContractIdAndBillingCycle(Long contractId, String billingCycle);
    Optional<Invoice> findByPaymentCode(String paymentCode);
}