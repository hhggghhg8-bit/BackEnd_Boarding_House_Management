package com.example.QLPT.Enity;

import com.example.QLPT.Enity.Enum.Enum;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "invoices")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Invoice {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "contract_id", nullable = false)
    private Contract contract;

    private String billingCycle;

    private BigDecimal roomAmount;
    private BigDecimal serviceAmount;
    private BigDecimal discountAmount;
    private BigDecimal surchargeAmount;
    private BigDecimal totalAmount;
    private BigDecimal paidAmount;

    private LocalDate dueDate;
    @Column(unique = true)
    private String paymentCode;

    @Enumerated(EnumType.STRING)
    private Enum.InvoiceStatus status;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;

}
