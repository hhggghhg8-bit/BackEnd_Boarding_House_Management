package com.example.QLPT.Enity;

import com.example.QLPT.Enity.Enum.Enum;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "price_histories")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PriceHistory {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long referenceId;

    @Enumerated(EnumType.STRING)
    private Enum.ReferenceType referenceType;

    private BigDecimal oldPrice;
    private BigDecimal newPrice;
    private LocalDate effectiveDate;

    private LocalDateTime createdAt;
}