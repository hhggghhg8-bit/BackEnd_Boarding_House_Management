package com.example.QLPT.Enity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "invoice_details")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class InvoiceDetail {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "invoice_id", nullable = false)
    private Invoice invoice;

    private String description;
    private BigDecimal amount;

    private LocalDateTime createdAt;
}
