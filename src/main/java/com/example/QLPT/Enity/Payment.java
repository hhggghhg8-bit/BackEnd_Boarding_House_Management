package com.example.QLPT.Enity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Payment {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "invoice_id", nullable = false)
    private Invoice invoice;

    private String transactionId;
    private BigDecimal amountPaid;
    private String paymentMethod;
    private LocalDateTime paymentTime;
    private String transferContent;

    private LocalDateTime createdAt;
}
